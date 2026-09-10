package com.qa.base;

import com.qa.utils.ConfigReader;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public class BaseTest {

    protected WebDriver driver;

    // ADD THIS METHOD SO LOGIN TEST CAN ACCESS THE DRIVER
    public WebDriver getDriver() {
        return driver;
    }

    @BeforeMethod
    public void setUp() {
        String browser = ConfigReader.getProperty("browser");
        driver = DriverFactory.initDriver(browser);
        
        String url = ConfigReader.getProperty("url");
        if (url != null && !url.isEmpty()) {
            driver.get(url);
        }
    }

    @AfterMethod
    public void tearDown() {
        DriverFactory.quitDriver();
    }
}
