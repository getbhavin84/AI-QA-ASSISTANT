package com.bhavin.ai;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;

public class GeminiDemo {

    public static void main(String[] args) throws Exception {

        // =========================================================
        // 1. GEMINI CLIENT
        // =========================================================

        String apiKey = System.getenv("GEMINI_API_KEY");

        if (apiKey == null || apiKey.trim().isEmpty()) {

            throw new IllegalStateException(
                    "GEMINI_API_KEY environment variable is not configured.");
        }

        Client client = Client.builder()
                .apiKey(apiKey)
                .build();


        // =========================================================
        // 2. USER REQUIREMENT
        // =========================================================

        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter your application requirement:");
        String requirement = scanner.nextLine();

        if (requirement == null || requirement.trim().isEmpty()) {

            scanner.close();

            throw new IllegalArgumentException(
                    "Application requirement cannot be empty.");
        }


        // =========================================================
        // 3. SINGLE AUTHORITATIVE GENERATED PROJECT PATH
        // =========================================================

        Path assistantRoot =
                Paths.get(System.getProperty("user.dir"))
                        .toAbsolutePath()
                        .normalize();

        Path generatedProjectPath =
                assistantRoot
                        .resolve("generated-project")
                        .toAbsolutePath()
                        .normalize();

        String outputDirectory =
                generatedProjectPath.toString();

        System.out.println();
        System.out.println("========== AI QA ASSISTANT PATHS ==========");
        System.out.println("AI Assistant root : " + assistantRoot);
        System.out.println("Generated project  : " + generatedProjectPath);
        System.out.println("===========================================");


        // =========================================================
        // 4. FRAMEWORK PLANNING
        // =========================================================

        System.out.println(
                "\nAnalyzing requirement against fixed Selenium architecture...");

        FrameworkPlanner frameworkPlanner =
                new FrameworkPlanner(client);

        FrameworkGenerationPlan frameworkPlan =
                frameworkPlanner.createPlan(requirement);


        // =========================================================
        // 5. DISPLAY FRAMEWORK PLAN
        // =========================================================

        System.out.println(
                "\n========== FRAMEWORK PLAN ==========");

        System.out.println(
                "Framework Name: "
                        + frameworkPlan.getFrameworkName());

        System.out.println(
                "Automation Tool: "
                        + frameworkPlan.getAutomationTool());

        System.out.println(
                "Language: "
                        + frameworkPlan.getLanguage());

        System.out.println(
                "Test Framework: "
                        + frameworkPlan.getTestFramework());

        System.out.println(
                "Design Pattern: "
                        + frameworkPlan.getDesignPattern());


        System.out.println("\nRequired Files:");

        for (String file : frameworkPlan.getFiles()) {

            System.out.println(" - " + file);
        }


        System.out.println(
                "\nFile Responsibilities:");

        for (FileSpecification specification :
                frameworkPlan.getFileSpecifications()) {

            System.out.println(
                    "\nFile: "
                            + specification.getPath());

            System.out.println(
                    "Responsibility: "
                            + specification.getResponsibility());
        }

        System.out.println(
                "\n====================================\n");


        // =========================================================
        // 6. GENERATE COMPLETE FRAMEWORK
        // =========================================================

        System.out.println(
                "Generating COMPLETE Selenium framework...");

        FrameworkGenerator frameworkGenerator =
                new FrameworkGenerator(client);

        List<GeneratedFile> generatedFiles =
                frameworkGenerator.generateFramework(
                        requirement,
                        frameworkPlan,true);


        // =========================================================
        // 7. VALIDATE GENERATED FILE PATHS
        // =========================================================

        System.out.println(
                "\nValidating generated file paths...");

        for (GeneratedFile generatedFile :
                generatedFiles) {

            String path =
                    FrameworkBlueprint.normalize(
                            generatedFile.getPath());

            System.out.println(
                    "Checking: " + path);

            if (!FrameworkBlueprint.isAllowedPath(path)) {

                throw new IllegalStateException(
                        "Generated file violates framework architecture: "
                                + path);
            }
        }

        System.out.println(
                "All generated paths are valid.");


        // =========================================================
        // 8. DISPLAY GENERATED FILES
        // =========================================================

        System.out.println(
                "\n========== GENERATED FILES ==========");

        for (GeneratedFile generatedFile :
                generatedFiles) {

            System.out.println(
                    "\nFile: "
                            + generatedFile.getPath());

            System.out.println(
                    "------------------------------------");

            System.out.println(
                    generatedFile.getContent());

            System.out.println(
                    "------------------------------------");
        }

        System.out.println(
                "\n====================================");


        // =========================================================
        // 9. WRITE GENERATED FRAMEWORK
        // =========================================================

        System.out.println(
                "\nWriting generated framework files to disk...");

        ArtifactWriter artifactWriter =
                new ArtifactWriter();

        artifactWriter.writeFiles(
                generatedFiles,
                outputDirectory);


        System.out.println(
                "\nFramework written to: "
                        + generatedProjectPath);


        // =========================================================
        // 10. VALIDATE GENERATED PROJECT STRUCTURE
        // =========================================================

        System.out.println(
                "\n========== GENERATED PROJECT VALIDATION ==========");

        System.out.println(
                "Generated project: "
                        + generatedProjectPath);

        if (!Files.exists(generatedProjectPath)) {

            throw new IllegalStateException(
                    "Generated project directory does not exist: "
                            + generatedProjectPath);
        }

        if (!Files.isDirectory(generatedProjectPath)) {

            throw new IllegalStateException(
                    "Generated project path is not a directory: "
                            + generatedProjectPath);
        }


        // ---------------------------------------------------------
        // MANDATORY POM CHECK
        // ---------------------------------------------------------

        Path pomPath =
                generatedProjectPath.resolve("pom.xml");

        System.out.println(
                "Checking POM: "
                        + pomPath);

        if (!Files.exists(pomPath)) {

            throw new IllegalStateException(
                    "FRAMEWORK GENERATION FAILED: "
                            + "pom.xml was not generated at: "
                            + pomPath);
        }

        if (!Files.isRegularFile(pomPath)) {

            throw new IllegalStateException(
                    "FRAMEWORK GENERATION FAILED: "
                            + "pom.xml exists but is not a file: "
                            + pomPath);
        }

        System.out.println(
                "pom.xml FOUND.");

        System.out.println(
                "==================================================");


        // =========================================================
        // 11. MAVEN BUILD
        // =========================================================

        System.out.println(
                "\nValidating generated framework with Maven...");

        MavenBuildExecutor buildExecutor =
                new MavenBuildExecutor();

        BuildResult buildResult =
                buildExecutor.compile(
                        outputDirectory);


        // =========================================================
        // 12. BUILD SUCCESS
        // =========================================================

        if (buildResult.isSuccess()) {

            System.out.println(
                    "\n========== BUILD SUCCESS ==========");

            System.out.println(
                    "Generated Selenium framework "
                            + "compiled successfully.");

        }

        // =========================================================
        // 13. BUILD FAILURE
        // =========================================================
        else {

            System.out.println(
                    "\n========== BUILD FAILURE ==========");

            System.out.println(
                    "\nCompiler output captured "
                            + "for AI analysis.");

            System.out.println(
                    buildResult.getOutput());


            // =====================================================
            // STEP 13A - AI FAILURE ANALYSIS
            // =====================================================

            System.out.println(
                    "\nAnalyzing build failure with AI...");

            BuildFailureAnalyzer failureAnalyzer =
                    new BuildFailureAnalyzer(client);

            BuildFailureAnalysis failureAnalysis =
                    failureAnalyzer.analyze(
                            buildResult,
                            frameworkPlan);


            // =====================================================
            // STEP 13B - DISPLAY AI FAILURE ANALYSIS
            // =====================================================

            System.out.println(
                    "\n========== AI FAILURE ANALYSIS ==========");

            System.out.println(
                    "\nFailure Detected: "
                            + failureAnalysis.isHasFailure());

            System.out.println(
                    "\nSummary: "
                            + failureAnalysis.getSummary());

            System.out.println(
                    "\nRoot Cause: "
                            + failureAnalysis.getRootCause());

            System.out.println(
                    "\nAffected Files:");

            for (String file :
                    failureAnalysis.getAffectedFiles()) {

                System.out.println(
                        " - " + file);
            }

            System.out.println(
                    "\nRecommended Action: "
                            + failureAnalysis.getRecommendedAction());

            System.out.println(
                    "\n==========================================");


            // =====================================================
            // IMPORTANT:
            // DO NOT BLOCK REPAIR OF GENERATED FRAMEWORK FILES
            // =====================================================

            /*
             * IMPORTANT ARCHITECTURAL RULE:
             *
             * The generated-project is owned by the AI Assistant.
             *
             * Therefore DriverFactory, BaseTest, ConfigReader,
             * WaitHelper, pom.xml etc. may be repaired by AI
             * when the generated project contains an error.
             *
             * The external Selenium reference framework is NOT
             * modified.
             *
             * Therefore we intentionally DO NOT perform:
             *
             * FrameworkBlueprint.isProtectedFile(...)
             *
             * here.
             */


            // =====================================================
            // STEP 14 - AI FRAMEWORK REPAIR
            // =====================================================

            System.out.println(
                    "\nRepairing generated framework with AI...");

            FrameworkRepairer frameworkRepairer =
                    new FrameworkRepairer(client);

            List<GeneratedFile> repairedFiles =
                    frameworkRepairer.repair(
                            outputDirectory,
                            frameworkPlan,
                            failureAnalysis);


            // =====================================================
            // STEP 14A - DISPLAY REPAIR PLAN
            // =====================================================

            System.out.println(
                    "\n========== AI REPAIR PLAN ==========");

            for (GeneratedFile repairedFile :
                    repairedFiles) {

                System.out.println(
                        "Repairing: "
                                + repairedFile.getPath());
            }

            System.out.println(
                    "====================================");


            // =====================================================
            // STEP 14B - VALIDATE REPAIR PATHS
            // =====================================================

            for (GeneratedFile repairedFile :
                    repairedFiles) {

                String normalizedPath =
                        FrameworkBlueprint.normalize(
                                repairedFile.getPath());

                if (!FrameworkBlueprint.isAllowedPath(
                        normalizedPath)) {

                    throw new IllegalStateException(
                            "AI repair generated a file outside "
                                    + "the allowed framework architecture: "
                                    + normalizedPath);
                }
            }


            // =====================================================
            // STEP 14C - WRITE AI REPAIRS
            // =====================================================

            artifactWriter.writeFiles(
                    repairedFiles,
                    outputDirectory);

            System.out.println(
                    "\nAI repairs written to generated framework.");


            // =====================================================
            // STEP 14D - VERIFY POM STILL EXISTS
            // =====================================================

            Path repairedPomPath =
                    generatedProjectPath.resolve("pom.xml");

            if (!Files.exists(repairedPomPath)) {

                throw new IllegalStateException(
                        "AI REPAIR BROKE PROJECT STRUCTURE: "
                                + "pom.xml no longer exists at: "
                                + repairedPomPath);
            }


            // =====================================================
            // STEP 14E - REVALIDATE AFTER REPAIR
            // =====================================================

            System.out.println(
                    "\nRe-validating generated framework "
                            + "after AI repair...");

            BuildResult repairedBuildResult =
                    buildExecutor.compile(
                            outputDirectory);


            // =====================================================
            // STEP 14F - REPAIR SUCCESS
            // =====================================================

            if (repairedBuildResult.isSuccess()) {

                System.out.println(
                        "\n========== AI REPAIR BUILD SUCCESS ==========");

                System.out.println(
                        "AI successfully repaired the "
                                + "generated Selenium framework.");

            }

            // =====================================================
            // STEP 14G - REPAIR FAILURE
            // =====================================================
            else {

                System.out.println(
                        "\n========== AI REPAIR BUILD FAILURE ==========");

                System.out.println(
                        repairedBuildResult.getOutput());
            }
        }


        // =========================================================
        // 15. TEST CASE GENERATOR
        // =========================================================

        String testCasePrompt =
                "You are a senior QA engineer. "
                + "Analyze the following software requirement "
                + "and generate a structured QA test suite.\n\n"

                + "IMPORTANT:\n"
                + "1. Do not assume business rules that are not stated "
                + "in the requirement.\n"
                + "2. Separate directly derived test cases from "
                + "AI-recommended scenarios.\n\n"

                + "SECTION 1: REQUIREMENT-BASED TEST CASES\n"
                + "Generate test cases that can be directly derived "
                + "from the requirement.\n\n"

                + "SECTION 2: RECOMMENDED ADDITIONAL TEST CASES\n"
                + "Suggest useful negative, edge, boundary, security, "
                + "and usability scenarios that QA should consider, "
                + "but clearly mark them as recommendations.\n\n"

                + "For every test case provide:\n"
                + "Test Case ID\n"
                + "Title\n"
                + "Preconditions\n"
                + "Test Steps\n"
                + "Test Data\n"
                + "Expected Result\n"
                + "Priority\n"
                + "Test Type\n\n"

                + "Return the final test cases as valid JSON only. "
                + "Do not include Markdown, code fences, explanations, "
                + "or headings outside the JSON.\n\n"

                + "Use this JSON structure:\n"
                + "[\n"
                + "  {\n"
                + "    \"testCaseId\": \"TC001\",\n"
                + "    \"title\": \"...\",\n"
                + "    \"preconditions\": [\"...\"],\n"
                + "    \"testSteps\": [\"...\", \"...\"],\n"
                + "    \"testData\": {\n"
                + "      \"username\": \"...\",\n"
                + "      \"password\": \"...\"\n"
                + "    },\n"
                + "    \"expectedResult\": \"...\",\n"
                + "    \"priority\": \"High\",\n"
                + "    \"testType\": \"Positive\"\n"
                + "  }\n"
                + "]\n\n"

                + "Requirement:\n"
                + requirement;


        System.out.println(
                "\nGenerating test cases... Please wait.\n");


        GenerateContentResponse testCaseResponse =
                client.models.generateContent(
                        "gemini-3.5-flash-lite",
                        testCasePrompt,
                        null);


        System.out.println(
                "\nAI Generated Test Cases:");

        System.out.println(
                testCaseResponse.text());


        ObjectMapper objectMapper =
                new ObjectMapper();

        List<TestCase> testCases =
                objectMapper.readValue(
                        testCaseResponse.text(),
                        new TypeReference<List<TestCase>>() {
                        });


        System.out.println(
                "\nNumber of test cases: "
                        + testCases.size());


        for (TestCase testCase : testCases) {

            System.out.println(
                    "\n-----------------------------");

            System.out.println(
                    "ID       : "
                            + testCase.getTestCaseId());

            System.out.println(
                    "Title    : "
                            + testCase.getTitle());

            System.out.println(
                    "Priority : "
                            + testCase.getPriority());

            System.out.println(
                    "Type     : "
                            + testCase.getTestType());

            System.out.println(
                    "Expected : "
                            + testCase.getExpectedResult());
        }


        // =========================================================
        // 16. SELECT FIRST TEST CASE
        // =========================================================

        if (testCases.isEmpty()) {

            throw new IllegalStateException(
                    "AI did not generate any test cases.");
        }

        TestCase testCase =
                testCases.get(0);


        System.out.println(
                "\nSelected Test Case:");

        System.out.println(
                "ID       : "
                        + testCase.getTestCaseId());

        System.out.println(
                "Title    : "
                        + testCase.getTitle());

        System.out.println(
                "Steps    : "
                        + testCase.getTestSteps());

        System.out.println(
                "Expected : "
                        + testCase.getExpectedResult());


        // =========================================================
        // 17. SAVE TEST CASES
        // =========================================================

        String jsonOutput =
                objectMapper
                        .writerWithDefaultPrettyPrinter()
                        .writeValueAsString(testCases);

        Files.writeString(
                assistantRoot.resolve("test-cases.json"),
                jsonOutput);

        System.out.println(
                "\nTest cases saved to: "
                        + assistantRoot.resolve("test-cases.json"));


        // =========================================================
        // 18. TEST DATA GENERATOR
        // =========================================================

        System.out.println(
                "\n[System] Pacing API requests... "
                        + "waiting 2 seconds.");

        Thread.sleep(2000);


        String testDataPrompt =
                "You are a senior QA engineer. "
                + "Generate test data for the following software requirement.\n\n"

                + "Do not invent specific business rules that are not stated. "
                + "Clearly identify any assumed or recommended data.\n\n"

                + "Generate data in these categories where applicable:\n"
                + "1. Valid test data\n"
                + "2. Invalid test data\n"
                + "3. Boundary test data\n"
                + "4. Empty/null data\n"
                + "5. Special character data\n"
                + "6. Security-related test data\n\n"

                + "For each item provide:\n"
                + "Category\n"
                + "Field\n"
                + "Test Value\n"
                + "Purpose\n"
                + "Expected Behavior\n\n"

                + "Requirement:\n"
                + requirement;


        System.out.println(
                "\nGenerating test data... Please wait.\n");


        GenerateContentResponse testDataResponse =
                client.models.generateContent(
                        "gemini-3.5-flash-lite",
                        testDataPrompt,
                        null);


        System.out.println(
                "\nAI Generated Test Data:");

        System.out.println(
                testDataResponse.text());


        // =========================================================
        // 19. CLEANUP
        // =========================================================

        scanner.close();

        System.out.println(
                "\n========== AI QA ASSISTANT COMPLETE ==========");
    }
}