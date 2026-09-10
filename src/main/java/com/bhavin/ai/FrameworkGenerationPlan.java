package com.bhavin.ai;

import java.util.List;

public class FrameworkGenerationPlan {

    private String frameworkName;
    private String automationTool;
    private String language;
    private String testFramework;
    private String designPattern;
    private List<String> files;
    private List<FileSpecification> fileSpecifications;
    
    public String getFrameworkName() {
        return frameworkName;
    }

    public void setFrameworkName(String frameworkName) {
        this.frameworkName = frameworkName;
    }

    public String getAutomationTool() {
        return automationTool;
    }

    public void setAutomationTool(String automationTool) {
        this.automationTool = automationTool;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getTestFramework() {
        return testFramework;
    }

    public void setTestFramework(String testFramework) {
        this.testFramework = testFramework;
    }

    public String getDesignPattern() {
        return designPattern;
    }

    public void setDesignPattern(String designPattern) {
        this.designPattern = designPattern;
    }

    public List<String> getFiles() {
        return files;
    }

    public void setFiles(List<String> files) {
        this.files = files;
    }
    public List<FileSpecification> getFileSpecifications() {
        return fileSpecifications;
    }

    public void setFileSpecifications(
            List<FileSpecification> fileSpecifications) {
        this.fileSpecifications = fileSpecifications;
    }
}