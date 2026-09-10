package com.qa.pages;

import com.qa.utils.WaitHelper;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class DashboardPage {

    private WebDriver driver;
    private WaitHelper waitHelper;

    @FindBy(id = "safe-placeholder-dashboard-element")
    private WebElement dashboardElement;

    public DashboardPage(WebDriver driver) {
        this.driver = driver;
        this.waitHelper = new WaitHelper(driver);
        PageFactory.initElements(driver, this);
    }

    public boolean isDashboardElementDisplayed() {
        waitHelper.waitForVisibility(dashboardElement);
        return dashboardElement.isDisplayed();
    }
}
