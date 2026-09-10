package com.bhavin.ai;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TestCase {

    private String testCaseId;
    private String title;
    private List<String> preconditions;
    private List<String> testSteps;
    private String expectedResult;
    private String priority;
    private String testType;
    private TestData testData;
    
    public String getTestCaseId() {
        return testCaseId;
    }

    public String getTitle() {
        return title;
    }

    public List<String> getPreconditions() {
        return preconditions;
    }

    public List<String> getTestSteps() {
        return testSteps;
    }

    public String getExpectedResult() {
        return expectedResult;
    }

    public String getPriority() {
        return priority;
    }

    public String getTestType() {
        return testType;
    }
    public TestData getTestData() {
        return testData;
    }
}