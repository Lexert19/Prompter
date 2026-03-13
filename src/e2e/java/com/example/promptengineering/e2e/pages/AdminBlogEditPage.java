package com.example.promptengineering.e2e.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

public class AdminBlogEditPage {
    private WebDriver driver;
    private WebDriverWait wait;

    @FindBy(id = "title")
    private WebElement titleInput;

    @FindBy(id = "content")
    private WebElement contentTextarea;

    @FindBy(id = "slug")
    private WebElement slugInput;

    @FindBy(id = "shortDescription")
    private WebElement shortDescriptionTextarea;

    @FindBy(css = "button[type='submit']")
    private WebElement saveButton;

    public AdminBlogEditPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        PageFactory.initElements(driver, this);
    }

    public void fillForm(String title, String slug, String shortDesc, String content) {
        wait.until(ExpectedConditions.visibilityOf(titleInput)).clear();
        titleInput.sendKeys(title);

        slugInput.clear();
        slugInput.sendKeys(slug);

        shortDescriptionTextarea.clear();
        shortDescriptionTextarea.sendKeys(shortDesc);

        contentTextarea.clear();
        contentTextarea.sendKeys(content);
    }

    public void submit() {
        saveButton.click();
    }
}