package com.example.promptengineering.service;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HexFormat;

@Slf4j
@Service
public class JsBundleService {

    @Getter
    private final byte[] bundle;

    @Getter
    private final String version;

    public JsBundleService(ResourcePatternResolver resourceLoader)
            throws IOException, NoSuchAlgorithmException {

        Resource[] resources = resourceLoader.getResources("classpath:static/js/**/*.js");
        Arrays.sort(resources, Comparator.comparing(r -> {
            try { return r.getURL().toString(); } catch (IOException e) { return ""; }
        }));

        var out = new ByteArrayOutputStream();
        for (Resource r : resources) {
            try (InputStream in = r.getInputStream()) {
                in.transferTo(out);
            }
            out.write("\n;\n".getBytes(StandardCharsets.UTF_8));
        }
        this.bundle = out.toByteArray();

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        this.version = HexFormat.of().formatHex(md.digest(bundle)).substring(0, 12);

        log.info("JS bundle: {} files, {} bytes, version={}",
                resources.length, bundle.length, version);
    }

    public String url() {
        return "/js/bundle-" + version + ".js";
    }
}