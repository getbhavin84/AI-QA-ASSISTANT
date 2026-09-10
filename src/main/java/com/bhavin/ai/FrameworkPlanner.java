package com.bhavin.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;

public class FrameworkPlanner {

    private final Client client;

    public FrameworkPlanner(Client client) {
        this.client = client;
    }

    public FrameworkGenerationPlan createPlan(String requirement) throws Exception {

        String prompt =
                "You are a senior QA Automation Architect.\n\n"

                + "You are NOT designing a new automation framework.\n"
                + "A fixed Selenium automation framework already exists.\n"
                + "Your task is to determine how the supplied application requirement "
                + "should be implemented INSIDE that existing framework.\n\n"

                + FrameworkBlueprint.getArchitectureRules()
                + "\n"

                + "EXISTING FRAMEWORK:\n"
                + "Framework Name: " + FrameworkBlueprint.getFrameworkName() + "\n"
                + "Automation Tool: " + FrameworkBlueprint.getAutomationTool() + "\n"
                + "Language: " + FrameworkBlueprint.getLanguage() + "\n"
                + "Test Framework: " + FrameworkBlueprint.getTestFramework() + "\n"
                + "Design Pattern: " + FrameworkBlueprint.getDesignPattern() + "\n\n"

                + "FIXED FRAMEWORK FILES:\n";

        for (String file : FrameworkBlueprint.getFixedFiles()) {
            prompt += "- " + file + "\n";
        }

        prompt +=
                "\nFEATURE PLANNING RULES:\n"
                + "1. Analyze the requirement.\n"
                + "2. Determine which existing framework files are relevant.\n"
                + "3. Determine which NEW feature files are required, if any.\n"
                + "4. New files may only be created inside the allowed framework directories.\n"
                + "5. Prefer reusing existing Page Objects and utilities.\n"
                + "6. Do not create duplicate Page Objects.\n"
                + "7. Do not create duplicate utilities.\n"
                + "8. Do not create DriverFactory.\n"
                + "9. Do not create BaseTest.\n"
                + "10. Do not create ConfigReader.\n"
                + "11. Do not create WaitHelper.\n"
                + "12. Do not modify protected framework infrastructure merely to implement a feature.\n"
                + "13. The requirement must be implemented within the existing architecture.\n\n"

                + "RETURN FORMAT:\n"
                + "Return ONLY valid JSON.\n"
                + "Do NOT return Markdown.\n"
                + "Do NOT use code fences.\n\n"

                + "Use this JSON structure:\n"
                + "{\n"
                + "  \"frameworkName\": \"Selenium-TestNG-POM-Framework\",\n"
                + "  \"automationTool\": \"Selenium WebDriver\",\n"
                + "  \"language\": \"Java\",\n"
                + "  \"testFramework\": \"TestNG\",\n"
                + "  \"designPattern\": \"Page Object Model\",\n"
                + "  \"files\": [\n"
                + "    \"src/main/java/com/qa/pages/ExamplePage.java\",\n"
                + "    \"src/test/java/com/qa/tests/ExampleTest.java\"\n"
                + "  ],\n"
                + "  \"fileSpecifications\": [\n"
                + "    {\n"
                + "      \"path\": \"src/main/java/com/qa/pages/ExamplePage.java\",\n"
                + "      \"responsibility\": \"Represent the required application page using the existing framework architecture.\"\n"
                + "    },\n"
                + "    {\n"
                + "      \"path\": \"src/test/java/com/qa/tests/ExampleTest.java\",\n"
                + "      \"responsibility\": \"Implement the requirement as a TestNG test using existing framework components.\"\n"
                + "    }\n"
                + "  ]\n"
                + "}\n\n"

                + "IMPORTANT:\n"
                + "The framework metadata must match the fixed blueprint exactly.\n"
                + "Do not invent framework architecture.\n"
                + "Do not generate source code at this stage.\n\n"

                + "APPLICATION REQUIREMENT:\n"
                + requirement;

        GenerateContentResponse response =
                client.models.generateContent(
                        "gemini-3.5-flash-lite",
                        prompt,
                        null
                );

        String json = response.text();

        ObjectMapper objectMapper = new ObjectMapper();

        return objectMapper.readValue(
                json,
                FrameworkGenerationPlan.class
        );
    }
}