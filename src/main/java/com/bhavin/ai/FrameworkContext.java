package com.bhavin.ai;

public class FrameworkContext {

    private String automationTool;
    private String language;
    private String testFramework;
    private String designPattern;
    private String browserManagement;
    private String assertionLibrary;

    public FrameworkContext(
            String automationTool,
            String language,
            String testFramework,
            String designPattern,
            String browserManagement,
            String assertionLibrary) {

        this.automationTool = automationTool;
        this.language = language;
        this.testFramework = testFramework;
        this.designPattern = designPattern;
        this.browserManagement = browserManagement;
        this.assertionLibrary = assertionLibrary;
    }

    public String getAutomationTool() {
        return automationTool;
    }

    public String getLanguage() {
        return language;
    }

    public String getTestFramework() {
        return testFramework;
    }

    public String getDesignPattern() {
        return designPattern;
    }

    public String getBrowserManagement() {
        return browserManagement;
    }

    public String getAssertionLibrary() {
        return assertionLibrary;
    }
}