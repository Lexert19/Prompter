package com.example.promptengineering.tool;

import com.example.promptengineering.BaseIntegrationTest;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.*;

@TestPropertySource(properties = "tools.fetch.ssrf-protection.enabled=false")
class FetchUrlToolIntegrationTest extends BaseIntegrationTest {

    static WireMockServer wm;

    @Autowired
    FetchUrlTool tool;

    @BeforeAll
    static void start() {
        wm = new WireMockServer(0);
        wm.start();
    }

    @AfterAll
    static void stop() {
        wm.stop();
    }

    @BeforeEach
    void reset() {
        wm.resetAll();
    }

    private String url(String path) {
        return wm.baseUrl() + path;
    }

    @Test
    void fetchesAndExtractsReadableText() throws Exception {
        wm.stubFor(get("/article").willReturn(ok("""
                <html><head>
                  <title>t</title>
                  <script>var secret = 'nope';</script>
                  <style>.x{color:red}</style>
                </head><body>
                  <h1>Hello</h1>
                  <p>World from Jsoup.</p>
                  <noscript>nope</noscript>
                </body></html>
                """)));

        String out = tool.execute(Map.of("url", url("/article")));

        assertThat(out).contains("Hello").contains("World from Jsoup.");
        assertThat(out).doesNotContain("secret", "color:red", "nope");
        assertThat(out).doesNotContain("\n", "  ");

        wm.verify(getRequestedFor(urlEqualTo("/article"))
                .withHeader("User-Agent", containing("PromptEngineeringBot")));
    }

    @Test
    void truncatesToMaxChars() throws Exception {
        String body = "x".repeat(50_000);
        wm.stubFor(get("/big").willReturn(html("<html><body>" + body + "</body></html>")));

        String out = tool.execute(Map.of("url", url("/big"), "max_chars", 1000));

        assertThat(out).hasSize(1001);
        assertThat(out).endsWith("…");
    }

    @Test
    void clampsMaxCharsToBounds() throws Exception {
        wm.stubFor(get("/page").willReturn(html("<html><body>" + "a".repeat(30_000) + "</body></html>")));

        String out = tool.execute(Map.of("url", url("/page"), "max_chars", 999_999));
        assertThat(out).hasSize(20_001);
    }

    @Test
    void defaultsMaxCharsWhenMissing() throws Exception {
        wm.stubFor(get("/p").willReturn(html("<html><body>" + "a".repeat(20_000) + "</body></html>")));

        String out = tool.execute(Map.of("url", url("/p")));
        assertThat(out).hasSize(8_001);
    }

    @Test
    void followsRedirects() throws Exception {
        wm.stubFor(get("/old").willReturn(aResponse().withStatus(301)
                .withHeader("Location", "/new")));
        wm.stubFor(get("/new").willReturn(html("<html><body>final content</body></html>")));

        String out = tool.execute(Map.of("url", url("/old")));

        assertThat(out).contains("final content");
        wm.verify(1, getRequestedFor(urlEqualTo("/old")));
        wm.verify(1, getRequestedFor(urlEqualTo("/new")));
    }

    @Test
    void failsOnTooManyRedirects() throws Exception {
        wm.stubFor(get("/loop").willReturn(aResponse().withStatus(302)
                .withHeader("Location", "/loop")));

        var ex = catchThrowable(() -> tool.execute(Map.of("url", url("/loop"))));
        assertThat(ex).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Too many redirects");
    }

    @Test
    void surfacesHttpErrorStatus() {
        wm.stubFor(get("/bad").willReturn(aResponse().withStatus(500).withBody("boom")));

        var ex = catchThrowable(() -> tool.execute(Map.of("url", url("/bad"))));
        assertThat(ex).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("HTTP 500");
    }

    @Test
    void rejectsBinaryContentType() {
        wm.stubFor(get("/img").willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "image/png")
                .withBody(new byte[]{(byte) 0x89, 'P', 'N', 'G'})));

        var ex = catchThrowable(() -> tool.execute(Map.of("url", url("/img"))));
        assertThat(ex).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported content type");
    }

    @Test
    void acceptsPlainTextAndJson() throws Exception {
        wm.stubFor(get("/txt").willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "text/plain; charset=utf-8")
                .withBody("zażółć gęślą jaźń")));

        String out = tool.execute(Map.of("url", url("/txt")));
        assertThat(out).isEqualTo("zażółć gęślą jaźń");
    }

    @Test
    void respectsCharsetFromContentType() throws Exception {
        byte[] iso = "Zażółć".getBytes(java.nio.charset.Charset.forName("ISO-8859-2"));
        wm.stubFor(get("/iso").willReturn(aResponse().withStatus(200)
                .withHeader("Content-Type", "text/plain; charset=ISO-8859-2")
                .withBody(iso)));

        String out = tool.execute(Map.of("url", url("/iso")));
        assertThat(out).isEqualTo("Zażółć");
    }

    @Test
    void rejectsNonHttpScheme() {
        var ex = catchThrowable(() -> tool.execute(Map.of("url", "ftp://example.com/x")));
        assertThat(ex).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Only http and https");
    }

    @Test
    void rejectsMissingUrl() {
        var ex = catchThrowable(() -> tool.execute(Map.of()));
        assertThat(ex).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Missing required argument: url");
    }

    @Test
    void rejectsMalformedUrl() {
        var ex = catchThrowable(() -> tool.execute(Map.of("url", "not a url")));
        assertThat(ex).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid URL");
    }

    @Test
    void rejectsUnresolvableHost() {
        var ex = catchThrowable(() ->
                tool.execute(Map.of("url", "http://definitely-not-a-real-host.invalid/")));
        assertThat(ex).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot resolve host");
    }

    private static ResponseDefinitionBuilder html(String body) {
        return aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "text/html; charset=utf-8")
                .withBody(body);
    }
}