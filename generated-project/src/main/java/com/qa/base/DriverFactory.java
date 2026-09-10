package com.qa.base;

import com.qa.utils.ConfigReader;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.time.Duration;

public class DriverFactory {

    private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();

    public static WebDriver initDriver(String browser) {
        if (browser == null || browser.isEmpty()) {
            browser = ConfigReader.getProperty("browser");
        }

        if (driverThreadLocal.get() == null) {
            WebDriver driver;
            if (browser != null && browser.equalsIgnoreCase("firefox")) {
                driver = new FirefoxDriver();
            } else {
                driver = new ChromeDriver();
            }

            String timeoutProp = ConfigReader.getProperty("implicit.wait");
         // Falls back to 10 seconds if 'implicit.wait' is missing or blank in config properties
         long implicitWait = (timeoutProp != null && !timeoutProp.trim().isEmpty()) ? Long.parseLong(timeoutProp.trim()) : 10L;

            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWait));
            driver.manage().window().maximize();
            driverThreadLocal.set(driver);
        }
        return getDriver();
    }

    public static WebDriver getDriver() {
        return driverThreadLocal.get();
    }

    public static void quitDriver() {
        if (driverThreadLocal.get() != null) {
            driverThreadLocal.get().quit();
            driverThreadLocal.remove();
        }
    }
}