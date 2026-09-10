package com.qa.pages;

import com.qa.utils.WaitHelper;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class LoginPage {
    private WebDriver driver;
    private WaitHelper waitHelper;

    @FindBy(id = "safe-login-button-placeholder")
    private WebElement loginButton;

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        this.waitHelper = new WaitHelper(driver);
        PageFactory.initElements(driver, this);
    }

    public boolean isLoginButtonDisplayed() {
        waitHelper.waitForVisibility(loginButton);
        return loginButton.isDisplayed();
    }
}
