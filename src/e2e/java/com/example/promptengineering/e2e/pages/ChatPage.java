package com.example.promptengineering.e2e.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

public class ChatPage {
    private WebDriver driver;
    private WebDriverWait wait;

    @FindBy(id = "input")
    private WebElement messageInput;

    @FindBy(id = "send-button")
    private WebElement sendButton;

    @FindBy(id = "chatMessages")
    private WebElement chatMessages;

    @FindBy(css = ".button-panel button[onclick*='showSettings']")
    private WebElement settingsButton;

    @FindBy(id = "modelSelectorBtn")
    private WebElement modelSelectorButton;

    @FindBy(id = "useSharedKeys")
    private WebElement useSharedKeysCheckbox;

    public ChatPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        PageFactory.initElements(driver, this);
    }

    public void openSettings() {
        wait.until(ExpectedConditions.elementToBeClickable(settingsButton)).click();
    }

    public void selectModel(String modelName) {
        wait.until(ExpectedConditions.elementToBeClickable(modelSelectorButton)).click();
        By modelOption = By.xpath(
                "//div[contains(@class,'model-item-selectable') and .//span[contains(text(),'" + modelName + "')]]");
        wait.until(ExpectedConditions.elementToBeClickable(modelOption)).click();
    }

    public void enableSharedKeys() {
        if (!useSharedKeysCheckbox.isSelected()) {
            useSharedKeysCheckbox.click();
        }
    }

    public void sendMessage(String message) {
        wait.until(ExpectedConditions.visibilityOf(messageInput)).sendKeys(message);
        sendButton.click();
    }

    public String getLastMessageText() {
        wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(By.cssSelector(".message"), 0));
        WebElement firstMessage = chatMessages.findElement(By.cssSelector(".message:first-child .code-wrap"));
        return firstMessage.getText();
    }
}
