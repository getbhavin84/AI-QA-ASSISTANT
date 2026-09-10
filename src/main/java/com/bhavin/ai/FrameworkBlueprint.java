package com.bhavin.ai;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class FrameworkBlueprint {

    private FrameworkBlueprint() {
    }

    public static String getFrameworkName() {
        return "Selenium-TestNG-POM-Framework";
    }

    public static String getAutomationTool() {
        return "Selenium WebDriver";
    }

    public static String getLanguage() {
        return "Java";
    }

    public static String getTestFramework() {
        return "TestNG";
    }

    public static String getDesignPattern() {
        return "Page Object Model";
    }

    public static List<String> getAllowedDirectories() {

        return Arrays.asList(
                "src/main/java/com/qa/base",
                "src/main/java/com/qa/pages",
                "src/main/java/com/qa/utils",
                "src/main/resources",
                "src/test/java/com/qa/base",
                "src/test/java/com/qa/tests",
                "src/test/resources"
        );
    }

    public static Set<String> getFixedFiles() {

        return new HashSet<String>(Arrays.asList(
                "pom.xml",
                "src/main/java/com/qa/base/DriverFactory.java",
                "src/main/java/com/qa/pages/LoginPage.java",
                "src/main/java/com/qa/pages/DashboardPage.java",
                "src/main/java/com/qa/utils/ConfigReader.java",
                "src/main/java/com/qa/utils/WaitHelper.java",
                "src/main/resources/config.properties",
                "src/test/java/com/qa/base/BaseTest.java",
                "src/test/java/com/qa/tests/LoginTest.java",
                "src/test/resources/testng.xml"
        ));
    }

    public static Set<String> getProtectedFiles() {

        return new HashSet<String>(Arrays.asList(
                "pom.xml",
                "src/main/java/com/qa/base/DriverFactory.java",
                "src/main/java/com/qa/utils/ConfigReader.java",
                "src/main/java/com/qa/utils/WaitHelper.java",
                "src/test/java/com/qa/base/BaseTest.java",
                "src/test/resources/testng.xml"
        ));
    }
    
    public static boolean isFrameworkFile(String path) { if (path == null) { return false; } return getFixedFiles().contains(normalize(path)); }

    public static boolean isAllowedPath(String path) {

        if (path == null || path.trim().isEmpty()) {
            return false;
        }

        String normalized = normalize(path);

        if (getFixedFiles().contains(normalized)) {
            return true;
        }

        for (String directory : getAllowedDirectories()) {

            if (normalized.startsWith(directory + "/")) {
                return true;
            }
        }

        return false;
    }

    public static boolean isProtectedFile(String path) {

        if (path == null) {
            return false;
        }

        return getProtectedFiles().contains(normalize(path));
    }

    public static String normalize(String path) {

        return path.replace("\\", "/");
    }

    /*
    public static String getArchitectureRules() {

        return
                "FIXED FRAMEWORK ARCHITECTURE:\n"
                + "1. Framework architecture is controlled by the application, not by AI.\n"
                + "2. Do NOT design a new framework.\n"
                + "3. Do NOT create new top-level directories.\n"
                + "4. Use Selenium WebDriver.\n"
                + "5. Use Java.\n"
                + "6. Use TestNG.\n"
                + "7. Use Page Object Model.\n"
                + "8. Page Objects belong under com.qa.pages.\n"
                + "9. Test classes belong under com.qa.tests.\n"
                + "10. Base classes belong under com.qa.base.\n"
                + "11. Utilities belong under com.qa.utils.\n"
                + "12. Configuration belongs under src/main/resources.\n"
                + "13. TestNG configuration belongs under src/test/resources.\n"
                + "14. Reuse existing DriverFactory, BaseTest, ConfigReader and WaitHelper.\n"
                + "15. Do NOT create duplicate framework infrastructure.\n"
                + "16. Do NOT move existing classes.\n"
                + "17. Do NOT rename existing classes.\n"
                + "18. Do NOT introduce another WebDriver lifecycle architecture.\n"
                + "19. Do NOT introduce another test framework.\n"
                + "20. Feature implementation must integrate with the existing framework.\n";
    } */
    
    public static String getArchitectureRules() {

        return
                "FIXED SELENIUM FRAMEWORK ARCHITECTURE:\n"
                + "1. The framework must use Selenium WebDriver.\n"
                + "2. The framework must use Java.\n"
                + "3. The framework must use TestNG.\n"
                + "4. The framework must use Page Object Model.\n"
                + "5. Page Objects belong under com.qa.pages.\n"
                + "6. Tests belong under com.qa.tests.\n"
                + "7. Framework base classes belong under com.qa.base.\n"
                + "8. Utilities belong under com.qa.utils.\n"
                + "9. Application configuration belongs under src/main/resources.\n"
                + "10. TestNG configuration belongs under src/test/resources.\n"
                + "11. WebDriver lifecycle must be controlled by DriverFactory/BaseTest.\n"
                + "12. Test classes must not create or destroy WebDriver directly.\n"
                + "13. Page Objects must not create an independent WebDriver.\n"
                + "14. Configuration access must use one consistent ConfigReader API.\n"
                + "15. Wait handling must use one consistent WaitHelper API.\n"
                + "16. Package names must exactly match directory paths.\n"
                + "17. Every referenced class must exist in the generated project.\n"
                + "18. Every referenced method must exist with the exact expected signature.\n"
                + "19. Every referenced configuration key must exist in config.properties.\n"
                + "20. Do not invent duplicate framework infrastructure.\n"
                + "21. Do not create a second WebDriver lifecycle architecture.\n"
                + "22. Do not introduce another test framework.\n"
                + "23. Do not copy application-specific data from the reference framework.\n"
                + "24. The generated framework must be Maven-buildable.\n";
    }
    
}