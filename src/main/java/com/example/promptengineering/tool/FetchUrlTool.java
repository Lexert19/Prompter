package com.example.promptengineering.tool;

import com.example.promptengineering.model.ToolDefinition;
import com.example.promptengineering.service.ToolExecutor;
import com.example.promptengineering.service.ToolRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class FetchUrlTool implements ToolExecutor {

    private static final int MAX_REDIRECTS = 3;
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(15);
    private static final int MAX_RESPONSE_BYTES = 2 * 1024 * 1024;
    private static final int DEFAULT_MAX_CHARS = 8000;
    private static final int HARD_MAX_CHARS = 20000;
    private static final String USER_AGENT =
            "Mozilla/5.0 (compatible; PromptEngineeringBot/1.0)";
    @Value("${tools.fetch.ssrf-protection.enabled:true}")
    private boolean ssrfProtectionEnabled;

    private final ToolRegistry registry;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(CONNECT_TIMEOUT)
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();

    @Override
    public String name() {
        return "fetch_url";
    }

    @PostConstruct
    void register() {
        registry.register(new ToolDefinition(name(),
                        "Fetches an absolute http(s) URL and returns its readable text content. "
                                + "Use when the user shares a link and asks about its contents.",
                        Map.of("type", "object", "properties", Map.of(
                                        "url", Map.of("type", "string", "description", "Absolute http(s) URL"),
                                        "max_chars", Map.of("type", "integer", "default", DEFAULT_MAX_CHARS,
                                                "minimum", 500, "maximum", HARD_MAX_CHARS)),
                                "required", List.of("url"))),
                this);
    }

    @Override
    public String execute(Map<String, Object> args) throws Exception {
        Object urlArg = args.get("url");
        if (urlArg == null || urlArg.toString().isBlank()) {
            throw new IllegalArgumentException("Missing required argument: url");
        }
        int maxChars = ((Number) args.getOrDefault("max_chars", DEFAULT_MAX_CHARS)).intValue();
        maxChars = Math.max(500, Math.min(HARD_MAX_CHARS, maxChars));

        String html = fetchWithRedirects(urlArg.toString());
        String text = extractText(html);
        return truncate(text, maxChars);
    }

    private String fetchWithRedirects(String rawUrl) throws Exception {
        URI uri = validateAndParse(rawUrl);

        for (int hop = 0; hop <= MAX_REDIRECTS; hop++) {
            log.debug("fetch_url GET {} (hop {})", uri, hop);

            HttpRequest request = HttpRequest.newBuilder(uri)
                    .timeout(REQUEST_TIMEOUT)
                    .header("User-Agent", USER_AGENT)
                    .header("Accept",
                            "text/html,application/xhtml+xml,text/plain;q=0.9,*/*;q=0.5")
                    .header("Accept-Encoding", "identity")
                    .GET()
                    .build();

            HttpResponse<byte[]> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofByteArray());
            int status = response.statusCode();

            if (status >= 300 && status < 400) {
                String location = response.headers().firstValue("Location")
                        .orElseThrow(() -> new IllegalStateException(
                                "Redirect without Location header"));
                uri = validateAndParse(uri.resolve(location).toString());
                continue;
            }

            if (status < 200 || status >= 300) {
                throw new IllegalStateException("HTTP " + status + " for " + uri);
            }

            String contentType = response.headers().firstValue("Content-Type").orElse("");
            if (!isTextual(contentType)) {
                throw new IllegalArgumentException(
                        "Unsupported content type: " + contentType);
            }

            byte[] body = response.body();
            if (body.length > MAX_RESPONSE_BYTES) {
                body = Arrays.copyOf(body, MAX_RESPONSE_BYTES);
            }

            return new String(body, detectCharset(contentType));
        }

        throw new IllegalStateException("Too many redirects (max " + MAX_REDIRECTS + ")");
    }

    private URI validateAndParse(String raw) {
        URI uri;
        try {
            uri = URI.create(raw);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid URL: " + raw);
        }

        String scheme = uri.getScheme();
        if (scheme == null
                || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
            throw new IllegalArgumentException("Only http and https URLs are allowed");
        }

        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException("URL has no host");
        }

        InetAddress[] addresses;
        try {
            addresses = InetAddress.getAllByName(host);
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Cannot resolve host: " + host);
        }

        if (ssrfProtectionEnabled) {
            for (InetAddress address : addresses) {
                if (isBlocked(address)) {
                    throw new IllegalArgumentException(
                            "URL host resolves to a blocked address: " + host);
                }
            }
        }

        return uri;
    }

    private boolean isBlocked(InetAddress address) {
        return address.isAnyLocalAddress()
                || address.isLoopbackAddress()
                || address.isLinkLocalAddress()
                || address.isSiteLocalAddress()
                || address.isMulticastAddress()
                || isUniqueLocalIPv6(address);
    }

    private boolean isUniqueLocalIPv6(InetAddress address) {
        byte[] bytes = address.getAddress();
        return bytes.length == 16 && (bytes[0] & 0xFE) == 0xFC;
    }

    private boolean isTextual(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return true;
        }
        String ct = contentType.toLowerCase();
        return ct.startsWith("text/")
                || ct.contains("html")
                || ct.contains("xml")
                || ct.contains("json")
                || ct.contains("javascript");
    }

    private Charset detectCharset(String contentType) {
        if (contentType != null) {
            for (String part : contentType.split(";")) {
                String trimmed = part.trim();
                if (trimmed.toLowerCase().startsWith("charset=")) {
                    String cs = trimmed.substring("charset=".length())
                            .trim().replace("\"", "");
                    try {
                        return Charset.forName(cs);
                    } catch (Exception ignored) {
                    }
                }
            }
        }
        return StandardCharsets.UTF_8;
    }

    private String extractText(String html) {
        if (html == null || html.isBlank()) {
            return "";
        }
        Document doc = Jsoup.parse(html);
        doc.select("script,style,noscript,iframe,svg").remove();
        String text = doc.body() != null ? doc.body().text() : doc.text();
        return text.replaceAll("\\s+", " ").trim();
    }

    private String truncate(String text, int max) {
        return text.length() <= max ? text : text.substring(0, max) + "…";
    }
}