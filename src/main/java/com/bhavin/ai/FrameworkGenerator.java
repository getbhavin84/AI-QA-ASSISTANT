package com.bhavin.ai;

import java.util.ArrayList;
import java.util.List;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;

public class FrameworkGenerator {

    private final Client client;

    public FrameworkGenerator(Client client) {
        this.client = client;
    }

    public List<GeneratedFile> generateFramework(
            String requirement,
            FrameworkGenerationPlan frameworkPlan,
            boolean allowProtectedFiles) throws Exception {

        List<GeneratedFile> generatedFiles =
                new ArrayList<GeneratedFile>();

        for (FileSpecification specification :
                frameworkPlan.getFileSpecifications()) {

            String path =
                    FrameworkBlueprint.normalize(
                            specification.getPath());

            System.out.println("\nGenerating Framework file:");
            System.out.println(path);

            /*
             * APPLICATION-LEVEL PATH VALIDATION
             */
            if (!FrameworkBlueprint.isAllowedPath(path)) {

                throw new IllegalArgumentException(
                        "AI attempted to generate file outside "
                        + "the fixed framework architecture: "
                        + path);
            }

            /*
             * PROTECTED FRAMEWORK FILE CHECK
             */
            if (FrameworkBlueprint.isProtectedFile(path)
                    && !allowProtectedFiles) {

                throw new IllegalArgumentException(
                        "AI attempted to modify protected framework file: "
                        + path);
            }

            String prompt =
                    "You are a Senior Java Selenium Automation Architect.\n\n"

                    + "Your task is to generate ONE COMPLETE FILE for an automatically "
                    + "generated Selenium automation framework.\n\n"

                    + "The framework MUST follow the fixed architectural contract supplied below.\n\n"

                    + "IMPORTANT:\n"
                    + "Use ONLY the architecture, directory structure, package structure, "
                    + "technical conventions and responsibilities of the reference framework.\n\n"

                    + "DO NOT copy application-specific materials from the reference framework.\n"
                    + "DO NOT copy business data.\n"
                    + "DO NOT copy credentials.\n"
                    + "DO NOT copy real URLs.\n"
                    + "DO NOT copy real locators.\n"
                    + "DO NOT copy test data.\n"
                    + "DO NOT copy screenshots or reports.\n"
                    + "DO NOT copy business-specific test cases.\n\n"

                    + FrameworkBlueprint.getArchitectureRules()
                    + "\n\n"

                    + "====================================================\n"
                    + "FRAMEWORK CONTRACT\n"
                    + "====================================================\n"

                    + "Framework Name: " + FrameworkBlueprint.getFrameworkName() + "\n"
                    + "Automation Tool: " + FrameworkBlueprint.getAutomationTool() + "\n"
                    + "Language: " + FrameworkBlueprint.getLanguage() + "\n"
                    + "Test Framework: " + FrameworkBlueprint.getTestFramework() + "\n"
                    + "Design Pattern: " + FrameworkBlueprint.getDesignPattern() + "\n\n"

                    + "====================================================\n"
                    + "MANDATORY FRAMEWORK FILES\n"
                    + "====================================================\n";

            for (String file : FrameworkBlueprint.getFixedFiles()) {
                prompt += "- " + file + "\n";
            }

            prompt +=
                    "\n====================================================\n"
                    + "TARGET FILE\n"
                    + "====================================================\n"
                    + path
                    + "\n\n"

                    + "TARGET RESPONSIBILITY\n"
                    + specification.getResponsibility()
                    + "\n\n"

                    + "APPLICATION REQUIREMENT\n"
                    + requirement
                    + "\n\n"

                    + "====================================================\n"
                    + "GENERATION RULES\n"
                    + "====================================================\n"

                    + "1. Generate the COMPLETE target file.\n"
                    + "2. The file MUST compile with the other generated framework files.\n"
                    + "3. The package declaration MUST exactly match the target path.\n"
                    + "4. Imports MUST reference only real classes in the fixed architecture "
                    + "or declared Maven dependencies.\n"
                    + "5. Do not invent classes.\n"
                    + "6. Do not invent methods.\n"
                    + "7. Do not invent constructors.\n"
                    + "8. Do not invent configuration keys.\n"
                    + "9. Use the exact framework APIs defined by FrameworkBlueprint.\n"
                    + "10. Keep WebDriver lifecycle management inside DriverFactory/BaseTest.\n"
                    + "11. Tests must not create their own WebDriver.\n"
                    + "12. Page Objects must not manage the global WebDriver lifecycle.\n"
                    + "13. Use Page Object Model.\n"
                    + "14. Keep Page Objects under com.qa.pages.\n"
                    + "15. Keep tests under com.qa.tests.\n"
                    + "16. Keep utilities under com.qa.utils.\n"
                    + "17. Keep framework base classes under com.qa.base.\n"
                    + "18. Keep configuration under src/main/resources.\n"
                    + "19. Keep TestNG configuration under src/test/resources.\n"
                    + "20. Do not introduce a second framework architecture.\n"
                    + "21. Do not use application-specific locators unless explicitly provided.\n"
                    + "22. If application details are unavailable, use clearly marked safe placeholders.\n"
                    + "23. Do not return Markdown.\n"
                    + "24. Do not return code fences.\n"
                    + "25. Return ONLY the complete file content.\n";
            GenerateContentResponse response =
                    client.models.generateContent(
                            "gemini-3.5-flash-lite",
                            prompt,
                            null);

            String content = response.text();

            generatedFiles.add(
                    new GeneratedFile(path, content));

            System.out.println(
                    "Generated successfully: " + path);

            Thread.sleep(2000);
        }

        return generatedFiles;
    }
}