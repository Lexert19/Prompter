package com.example.promptengineering.e2e;

import com.example.promptengineering.e2e.pages.AdminBlogEditPage;
import com.example.promptengineering.e2e.pages.AdminBlogListPage;
import com.example.promptengineering.e2e.pages.LoginPage;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
public class BlogE2ETest extends BaseE2ETest {
    private LoginPage loginPage;
    private AdminBlogListPage blogListPage;
    private AdminBlogEditPage blogEditPage;

    @BeforeEach
    void initPages() {
        loginPage = new LoginPage(driver);
        blogListPage = new AdminBlogListPage(driver);
        blogEditPage = new AdminBlogEditPage(driver);
    }

    @Test
    void fullPostLifecycle() {
        driver.get(baseUrl + "/auth/login");
        loginPage.login("admin@example.com", "admin123");
        driver.get(baseUrl + "/admin/blog");
        blogListPage.clickNewPost();
        String uniqueTitle = "Test E2E " + System.currentTimeMillis();
        blogEditPage.fillForm(uniqueTitle, System.currentTimeMillis()+"", "Short description", "Post content");
        blogEditPage.submit();
        driver.get(baseUrl + "/admin/blog");
        assertTrue(blogListPage.isPostPresent(uniqueTitle));
        blogListPage.clickEditForPost(uniqueTitle);
        String editedTitle = "(edited)";
        blogEditPage.fillForm(editedTitle, System.currentTimeMillis()+"", "Update short description", "New content");
        blogEditPage.submit();
        driver.get(baseUrl + "/admin/blog");
        assertTrue(blogListPage.isPostPresent(editedTitle));
        assertFalse(blogListPage.isPostPresent(uniqueTitle));
        blogListPage.clickDeleteForPost(editedTitle);
        driver.get(baseUrl + "/admin/blog");
        assertFalse(blogListPage.isPostPresent(editedTitle));
    }
}