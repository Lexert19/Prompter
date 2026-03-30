package com.example.promptengineering.e2e.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import java.util.List;

public class AdminBlogListPage {
    private WebDriver driver;
    private WebDriverWait wait;

    @FindBy(linkText = "+ Nowy wpis")
    private WebElement newPostLink;

    @FindBy(id = "posts-table-body")
    private WebElement tableBody;

    public AdminBlogListPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        PageFactory.initElements(driver, this);
    }

    public void clickNewPost() {
        newPostLink.click();
    }

    public boolean isPostPresent(String title) {
        wait.until(ExpectedConditions.visibilityOf(tableBody));
        List<WebElement> rows = tableBody.findElements(By.tagName("tr"));
        for (WebElement row : rows) {
            if (row.getText().contains(title)) {
                return true;
            }
        }
        return false;
    }

    public void clickEditForPost(String title) {
        WebElement editButton = findEditButton(title);
        editButton.click();
    }

    public void clickDeleteForPost(String title) {
        WebElement deleteButton = findDeleteButton(title);
        deleteButton.click();
        driver.switchTo().alert().accept();
    }

    private WebElement findEditButton(String title) {
        WebElement row = tableBody.findElement(By.xpath(".//tr[td[contains(text(),'" + title + "')]]"));
        return row.findElement(By.linkText("Edytuj"));
    }

    private WebElement findDeleteButton(String title) {
        WebElement row = tableBody.findElement(By.xpath(".//tr[td[contains(text(),'" + title + "')]]"));
        return row.findElement(By.cssSelector(".delete-post-btn"));
    }
}
