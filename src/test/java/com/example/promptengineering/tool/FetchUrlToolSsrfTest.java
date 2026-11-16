package com.example.promptengineering.tool;

import com.example.promptengineering.BaseIntegrationTest;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.*;

class FetchUrlToolSsrfTest extends BaseIntegrationTest {

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

    @ParameterizedTest
    @ValueSource(strings = {
            "http://127.0.0.1:%d/",
            "http://localhost:%d/",
            "http://[::1]:%d/",
            "http://0.0.0.0:%d/"
    })
    void blocksLoopbackAndAnyLocal(String template) {
        String url = template.formatted(wm.port());

        var ex = catchThrowable(() -> tool.execute(Map.of("url", url)));
        assertThat(ex).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("blocked address");

        wm.verify(0, anyRequestedFor(anyUrl()));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "http://169.254.169.254/latest/meta-data/",
            "http://10.0.0.1/",
            "http://192.168.1.1/",
            "http://172.16.0.1/",
            "http://[fe80::1]/",
            "http://[fc00::1]/"
    })
    void blocksCloudMetadataAndPrivateRanges(String url) {
        var ex = catchThrowable(() -> tool.execute(Map.of("url", url)));
        assertThat(ex).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("blocked address");

        wm.verify(0, anyRequestedFor(anyUrl()));
    }

    @Test
    void dnsNameThatResolvesToLoopbackIsAlsoBlocked() {
        var ex = catchThrowable(() -> tool.execute(
                Map.of("url", "http://127.0.0.1.nip.io:" + wm.port() + "/")));

        assertThat(ex).isInstanceOf(IllegalArgumentException.class)
                .satisfiesAnyOf(
                        e -> assertThat(e).hasMessageContaining("blocked address"),
                        e -> assertThat(e).hasMessageContaining("Cannot resolve host"));

        wm.verify(0, anyRequestedFor(anyUrl()));
    }
}