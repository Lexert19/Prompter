package com.example.promptengineering.e2e;

import com.example.promptengineering.PromptengineeringApplication;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = PromptengineeringApplication.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
public abstract class BaseE2ETest {
    @LocalServerPort
    protected int port;
    protected WebDriver driver;
    protected String baseUrl;

    @BeforeEach
    void setUp() {
        WebDriverManager.firefoxdriver().setup();

        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--start-maximized");

        driver = new FirefoxDriver(options);
        baseUrl = "http://localhost:" + port;
    }

//    @AfterEach
//    void tearDown() {
//        if (driver != null) {
//            driver.quit();
//        }
//    }
}