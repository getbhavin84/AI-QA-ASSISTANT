package com.qa.tests;

import com.qa.base.BaseTest;
import com.qa.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class LoginTest extends BaseTest {

    @Test
    public void testLoginButtonVisibility() {
        // Redundant URL navigation removed because BaseTest handles it
        LoginPage loginPage = new LoginPage(getDriver());
        boolean isLoginButtonVisible = loginPage.isLoginButtonDisplayed();

        Assert.assertTrue(isLoginButtonVisible, "The login button should be visible on the page.");
    }
}
