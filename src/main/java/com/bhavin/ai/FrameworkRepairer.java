package com.bhavin.ai;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;

public class FrameworkRepairer {

    /*
     * Keep the model in one place so it is easy to change later.
     *
     * The previous failure showed that gemini-2.5-flash was no longer
     * available to the current user. The current API response explicitly
     * recommended gemini-3.6-flash.
     */
    private static final String GEMINI_MODEL =
            "gemini-3.5-flash-lite";

    private final Client client;

    public FrameworkRepairer(Client client) {
        this.client = client;
    }

    /**
     * Repairs Maven-identified files inside the existing generated
     * Selenium framework.
     *
     * IMPORTANT:
     *
     * - This method does NOT clean the project.
     * - This method does NOT create a new framework.
     * - Gemini can only return files that Maven identified as affected.
     * - Existing framework files may be repaired if Maven identifies
     *   them as affected.
     * - Other source files are supplied to Gemini only as READ-ONLY
     *   context so that cross-file contracts can be understood.
     */
    public List<GeneratedFile> repair(
            String projectDirectory,
            FrameworkGenerationPlan frameworkPlan,
            BuildFailureAnalysis failureAnalysis)
            throws Exception {

        List<GeneratedFile> repairedFiles =
                new ArrayList<GeneratedFile>();

        if (failureAnalysis == null) {

            System.out.println(
                    "WARNING: Build failure analysis is null.");

            return repairedFiles;
        }

        if (failureAnalysis.getAffectedFiles() == null
                || failureAnalysis.getAffectedFiles().isEmpty()) {

            System.out.println(
                    "WARNING: AI failure analyzer returned "
                    + "no affected files.");

            return repairedFiles;
        }

        File projectDir =
                new File(projectDirectory);

        if (!projectDir.exists()
                || !projectDir.isDirectory()) {

            System.out.println(
                    "WARNING: Generated project directory "
                    + "does not exist:");

            System.out.println(
                    projectDir.getAbsolutePath());

            return repairedFiles;
        }

        System.out.println(
                "\n========== AI FRAMEWORK REPAIR ==========");

        System.out.println(
                "Generated project: "
                + projectDir.getAbsolutePath());

        /*
         * ---------------------------------------------------------
         * STEP 1
         *
         * Convert Maven-reported absolute paths into project-
         * relative paths and verify that they actually exist.
         * ---------------------------------------------------------
         */

        String projectPrefix =
                projectDir
                        .getAbsolutePath()
                        .replace("\\", "/");

        List<String> affectedRelativePaths =
                new ArrayList<String>();

        for (String reportedPath :
                failureAnalysis.getAffectedFiles()) {

            if (reportedPath == null
                    || reportedPath.trim().isEmpty()) {

                continue;
            }

            String relativePath =
                    normalizeRelativePath(
                            reportedPath,
                            projectPrefix);

            if (relativePath.isEmpty()) {

                continue;
            }

            /*
             * Only source/resource files inside the generated
             * framework are allowed.
             */
            if (!isAllowedProjectPath(relativePath)) {

                System.out.println(
                        "WARNING: Ignoring Maven path outside "
                        + "the generated framework: "
                        + relativePath);

                continue;
            }

            File affectedFile =
                    new File(
                            projectDir,
                            relativePath);

            /*
             * Canonical path protection against ../ traversal.
             */
            if (!isInsideProject(
                    projectDir,
                    affectedFile)) {

                System.out.println(
                        "WARNING: Rejecting path outside "
                        + "generated project: "
                        + relativePath);

                continue;
            }

            if (!affectedFile.exists()
                    || !affectedFile.isFile()) {

                System.out.println(
                        "WARNING: Maven/AI identified a file "
                        + "that does not exist:");

                System.out.println(
                        affectedFile.getAbsolutePath());

                continue;
            }

            if (!containsIgnoreCase(
                    affectedRelativePaths,
                    relativePath)) {

                affectedRelativePaths.add(
                        relativePath);
            }
        }

        if (affectedRelativePaths.isEmpty()) {

            System.out.println(
                    "WARNING: No affected Maven files could "
                    + "be resolved inside the generated project.");

            return repairedFiles;
        }

        /*
         * ---------------------------------------------------------
         * STEP 2
         *
         * Display exactly what Gemini is allowed to modify.
         * ---------------------------------------------------------
         */

        System.out.println(
                "\nAffected files:");

        for (String path :
                affectedRelativePaths) {

            System.out.println(
                    " - " + path);
        }

        /*
         * ---------------------------------------------------------
         * STEP 3
         *
         * Read affected files.
         * ---------------------------------------------------------
         */

        StringBuilder affectedFileContents =
                new StringBuilder();

        for (String relativePath :
                affectedRelativePaths) {

            appendFileContent(
                    projectDir,
                    relativePath,
                    affectedFileContents,
                    "AFFECTED FILE");
        }

        /*
         * ---------------------------------------------------------
         * STEP 4
         *
         * Read the rest of the Java framework as READ-ONLY context.
         *
         * THIS IS IMPORTANT FOR YOUR CURRENT ERROR.
         *
         * LoginPage.java needs to understand the actual WaitHelper
         * constructor and methods.
         *
         * LoginTest.java needs to understand the actual LoginPage
         * and DashboardPage APIs.
         *
         * BaseTest.java needs to understand DriverFactory.
         *
         * Gemini must see those contracts before repairing.
         * ---------------------------------------------------------
         */

        StringBuilder frameworkContext =
                new StringBuilder();

        appendJavaFrameworkContext(
                projectDir,
                affectedRelativePaths,
                frameworkContext);

        /*
         * ---------------------------------------------------------
         * STEP 5
         *
         * Build repair prompt.
         * ---------------------------------------------------------
         */

        String prompt =
                buildRepairPrompt(
                        frameworkPlan,
                        failureAnalysis,
                        affectedRelativePaths,
                        affectedFileContents.toString(),
                        frameworkContext.toString());

        /*
         * ---------------------------------------------------------
         * STEP 6
         *
         * Call Gemini.
         * ---------------------------------------------------------
         */

        System.out.println(
                "\nSending affected files and Maven failure "
                + "to Gemini for repair...");

        System.out.println(
                "Gemini model: "
                + GEMINI_MODEL);

        String responseText;

        try {

            GenerateContentResponse response =
                    client.models.generateContent(
                            GEMINI_MODEL,
                            prompt,
                            null);

            responseText =
                    response.text();

        } catch (Exception e) {

            System.out.println(
                    "\nERROR: Gemini repair request failed.");

            System.out.println(
                    e.getMessage());

            return repairedFiles;
        }

        if (responseText == null
                || responseText.trim().isEmpty()) {

            System.out.println(
                    "WARNING: Gemini returned an empty "
                    + "repair response.");

            return repairedFiles;
        }

        System.out.println(
                "Gemini repair response received.");

        /*
         * ---------------------------------------------------------
         * STEP 7
         *
         * Parse Gemini response.
         * ---------------------------------------------------------
         */

        List<GeneratedFile> aiRepairs =
                parseRepairResponse(
                        responseText);

        if (aiRepairs.isEmpty()) {

            System.out.println(
                    "WARNING: Gemini returned no valid "
                    + "repair files.");

            return repairedFiles;
        }

        /*
         * ---------------------------------------------------------
         * STEP 8
         *
         * Validate every returned repair.
         * ---------------------------------------------------------
         */

        Set<String> allowedPaths =
                new HashSet<String>();

        for (String path :
                affectedRelativePaths) {

            allowedPaths.add(
                    path.toLowerCase());
        }

        Set<String> alreadyAccepted =
                new HashSet<String>();

        for (GeneratedFile repairedFile :
                aiRepairs) {

            if (repairedFile == null) {

                continue;
            }

            String returnedPath =
                    repairedFile.getPath();

            if (returnedPath == null
                    || returnedPath.trim().isEmpty()) {

                System.out.println(
                        "WARNING: Gemini returned a repair "
                        + "without a file path.");

                continue;
            }

            String relativePath =
                    normalizeRelativePath(
                            returnedPath,
                            projectPrefix);

            /*
             * Gemini is NOT allowed to modify a file that was not
             * identified by Maven/Failure Analyzer.
             */
            if (!allowedPaths.contains(
                    relativePath.toLowerCase())) {

                System.out.println(
                        "WARNING: Rejecting Gemini repair for "
                        + "unauthorized file:");

                System.out.println(
                        relativePath);

                continue;
            }

            /*
             * Verify the file still exists.
             */
            File targetFile =
                    new File(
                            projectDir,
                            relativePath);

            if (!targetFile.exists()
                    || !targetFile.isFile()) {

                System.out.println(
                        "WARNING: Gemini returned a file that "
                        + "does not exist:");

                System.out.println(
                        relativePath);

                continue;
            }

            /*
             * Verify canonical location.
             */
            if (!isInsideProject(
                    projectDir,
                    targetFile)) {

                System.out.println(
                        "WARNING: Rejecting Gemini file "
                        + "outside generated project:");

                System.out.println(
                        relativePath);

                continue;
            }

            String content =
                    repairedFile.getContent();

            if (content == null
                    || content.trim().isEmpty()) {

                System.out.println(
                        "WARNING: Gemini returned empty "
                        + "content for:");

                System.out.println(
                        relativePath);

                continue;
            }

            /*
             * Prevent duplicate repair entries.
             */
            if (alreadyAccepted.contains(
                    relativePath.toLowerCase())) {

                System.out.println(
                        "WARNING: Duplicate Gemini repair "
                        + "ignored:");

                System.out.println(
                        relativePath);

                continue;
            }

            alreadyAccepted.add(
                    relativePath.toLowerCase());

            /*
             * Normalize the path before returning it.
             */
            repairedFile.setPath(
                    relativePath);

            repairedFiles.add(
                    repairedFile);

            System.out.println(
                    "Accepted AI repair: "
                    + relativePath);
        }

        System.out.println(
                "\nAI repairs accepted: "
                + repairedFiles.size());

        return repairedFiles;
    }

    /**
     * Builds the complete Gemini repair prompt.
     */
    private String buildRepairPrompt(
            FrameworkGenerationPlan frameworkPlan,
            BuildFailureAnalysis failureAnalysis,
            List<String> affectedFiles,
            String affectedFileContents,
            String frameworkContext) {

        StringBuilder prompt =
                new StringBuilder();
        prompt.append(
                "====================================================\n"
                + "PROJECT STRUCTURE VALIDATION\n"
                + "====================================================\n");

        prompt.append(
                "Before repairing Java source code, verify that the generated "
                + "project is a valid Maven project.\n\n");

        prompt.append(
                "The project root MUST contain:\n"
                + "pom.xml\n"
                + "src/main/java\n"
                + "src/main/resources\n"
                + "src/test/java\n"
                + "src/test/resources\n\n");

        prompt.append(
                "If pom.xml is missing, DO NOT repair Java source files.\n"
                + "Classify the failure as PROJECT_STRUCTURE_FAILURE.\n"
                + "Do not invent Java changes to solve it.\n");

        prompt.append(
                "If the project structure is valid, continue with normal "
                + "Java compilation analysis.\n\n");
        prompt.append(
                "PACKAGE/PATH CONTRACT:\n"
                + "For every Java class, derive the package from its actual project-relative path.\n"
                + "Never guess a package from a class name.\n"
                + "Before changing an import, locate the referenced class in the supplied "
                + "framework context.\n"
                + "The following are equivalent and MUST agree:\n"
                + "  filesystem path\n"
                + "  package declaration\n"
                + "  import statement\n"
                + "  class name\n"
                + "Never move a class merely to satisfy an incorrect import.\n\n");
        prompt.append(
                "ANTI-REGRESSION RULES:\n"
                + "1. A repair must not create a new compiler error.\n"
                + "2. Do not modify files unrelated to the reported failure.\n"
                + "3. Do not change a correct package declaration to solve an incorrect import.\n"
                + "4. Do not create duplicate classes.\n"
                + "5. Do not change a working API when correcting its caller.\n"
                + "6. Preserve existing Maven project structure.\n"
                + "7. Preserve pom.xml unless the failure explicitly proves that pom.xml is wrong.\n"
                + "8. If the reported failure is caused by an incorrect caller, repair the caller.\n"
                + "9. If the reported failure is caused by a missing class, first verify whether "
                + "the class should already exist before creating anything.\n"
                + "10. Never repair based only on the error text when source context is available.\n\n");
        
        prompt.append(
                "You are an expert Java Selenium automation "
                + "framework repair engineer.\n\n");

        prompt.append(
                "An EXISTING Selenium automation framework "
                + "was generated and then compiled using Maven.\n\n");

        prompt.append(
                "Maven compilation failed.\n\n");

        prompt.append(
                "Your job is to repair the existing framework "
                + "implementation while preserving its existing "
                + "architecture and contracts.\n\n");

        prompt.append(
                "====================================================\n");
        prompt.append(
                "FRAMEWORK INFORMATION\n");
        prompt.append(
                "====================================================\n");

        if (frameworkPlan != null) {

            prompt.append(
                    "Framework Name: ")
                    .append(
                            safe(frameworkPlan.getFrameworkName()))
                    .append("\n");

            prompt.append(
                    "Automation Tool: ")
                    .append(
                            safe(frameworkPlan.getAutomationTool()))
                    .append("\n");

            prompt.append(
                    "Language: ")
                    .append(
                            safe(frameworkPlan.getLanguage()))
                    .append("\n");

            prompt.append(
                    "Test Framework: ")
                    .append(
                            safe(frameworkPlan.getTestFramework()))
                    .append("\n");

            prompt.append(
                    "Design Pattern: ")
                    .append(
                            safe(frameworkPlan.getDesignPattern()))
                    .append("\n");
        }

        prompt.append("\n");

        prompt.append(
                "====================================================\n");
        prompt.append(
                "MAVEN FAILURE ANALYSIS\n");
        prompt.append(
                "====================================================\n");

        prompt.append(
                "Summary:\n")
                .append(
                        safe(failureAnalysis.getSummary()))
                .append("\n\n");

        prompt.append(
                "Root Cause:\n")
                .append(
                        safe(failureAnalysis.getRootCause()))
                .append("\n\n");

        prompt.append(
                "Recommended Action:\n")
                .append(
                        safe(failureAnalysis.getRecommendedAction()))
                .append("\n\n");

        prompt.append(
                "====================================================\n");
        prompt.append(
                "FILES YOU ARE ALLOWED TO MODIFY\n");
        prompt.append(
                "====================================================\n");

        for (String path :
                affectedFiles) {

            prompt.append(
                    path)
                    .append("\n");
        }

        prompt.append("\n");

        prompt.append(
                "CRITICAL:\n"
                + "You may ONLY return files from the list above.\n"
                + "Do NOT return any other file.\n"
                + "Do NOT create a new file.\n"
                + "Do NOT rename a file.\n"
                + "Do NOT move a file.\n\n");

        prompt.append(
                "====================================================\n");
        prompt.append(
                "ACTUAL AFFECTED FILE CONTENTS\n");
        prompt.append(
                "====================================================\n");

        prompt.append(
                affectedFileContents);

        prompt.append("\n\n");

        prompt.append(
                "====================================================\n");
        prompt.append(
                "READ-ONLY FRAMEWORK CONTEXT\n");
        prompt.append(
                "====================================================\n");

        prompt.append(
                "The following source files are supplied ONLY so "
                + "that you can understand existing APIs and "
                + "cross-file contracts.\n\n");

        prompt.append(
                "You must NOT return these files unless they are "
                + "also explicitly listed under FILES YOU ARE "
                + "ALLOWED TO MODIFY.\n\n");

        prompt.append(
                frameworkContext);

        prompt.append("\n\n");

        prompt.append(
                "====================================================\n");
        prompt.append(
                "REPAIR RULES\n");
        prompt.append(
                "====================================================\n");

        prompt.append(
                "1. Preserve the existing Selenium framework "
                + "architecture.\n");

        prompt.append(
                "2. Preserve the existing package structure.\n");

        prompt.append(
                "3. Preserve the existing Page Object Model.\n");

        prompt.append(
                "4. Do not create a new framework architecture.\n");

        prompt.append(
                "5. Do not create new packages.\n");

        prompt.append(
                "6. Do not create new framework components.\n");

        prompt.append(
                "7. Do not rename or move classes.\n");

        prompt.append(
                "8. Inspect the actual source code supplied in "
                + "this prompt before making a repair decision.\n");

        prompt.append(
                "9. Existing source code is the authoritative "
                + "API contract.\n");

        prompt.append(
                "10. Pay particular attention to constructors, "
                + "method names, parameters, return types, "
                + "imports, static versus instance methods, "
                + "and package names.\n");

        prompt.append(
                "11. If a caller uses a method incorrectly, "
                + "repair the caller instead of inventing a "
                + "duplicate API.\n");

        prompt.append(
                "12. Do not add a method simply because another "
                + "file expects a different method name.\n");

        prompt.append(
                "13. Reuse existing methods whenever possible.\n");

        prompt.append(
                "14. Prefer the smallest change that restores "
                + "the compile-time contract.\n");

        prompt.append(
                "15. Do not modify unrelated code.\n");

        prompt.append(
                "16. Do not change Selenium, Java, or TestNG "
                + "versions to hide a compilation problem.\n");

        prompt.append(
                "17. Do not change Maven dependencies unless "
                + "the compiler output proves a dependency is "
                + "actually missing.\n");

        prompt.append(
                "18. Do not change the user's requirement.\n");

        prompt.append(
                "19. Return complete source files, not patches.\n");

        prompt.append(
                "20. Return ONLY files that actually need "
                + "modification.\n");

        prompt.append(
                "21. Every returned path must exactly match one "
                + "of the allowed paths.\n");

        prompt.append(
                "22. Never return an absolute Windows path.\n");

        prompt.append(
                "23. Never return Markdown or code fences.\n");

        prompt.append(
                "24. Before returning the repair, mentally "
                + "verify that the modified file compiles "
                + "against the READ-ONLY framework APIs.\n");

        prompt.append("\n");

        prompt.append(
                "====================================================\n");
        prompt.append(
                "CURRENT CONTRACT-REPAIR PRINCIPLE\n");
        prompt.append(
                "====================================================\n");

        prompt.append(
                "If an existing utility exposes:\n"
                + "    WaitHelper(WebDriver, int)\n"
                + "and exposes:\n"
                + "    waitForElementToBeVisible(WebElement)\n"
                + "then a page object calling:\n"
                + "    new WaitHelper(driver)\n"
                + "or:\n"
                + "    waitForElementToBeVisible(element, timeout)\n"
                + "must be corrected to use the EXISTING API.\n\n");

        prompt.append(
                "Do NOT add overloaded methods to WaitHelper merely "
                + "to make an incorrectly generated page object "
                + "compile.\n\n");

        prompt.append(
                "If an existing page object exposes "
                + "isDashboardLoaded(), but a test calls "
                + "isDashboardDisplayed(), repair the test "
                + "caller rather than inventing a new page-object "
                + "method.\n\n");

        prompt.append(
                "If an existing DriverFactory exposes an instance "
                + "initDriver(String) method and an instance "
                + "closeDriver() method, do not redesign "
                + "DriverFactory into a static utility merely "
                + "because BaseTest calls it statically.\n\n");

        prompt.append(
                "====================================================\n");
        prompt.append(
                "REQUIRED OUTPUT\n");
        prompt.append(
                "====================================================\n");

        prompt.append(
                "Return ONLY valid JSON in this exact format:\n\n");

        prompt.append(
                "{\n"
                + "  \"files\": [\n"
                + "    {\n"
                + "      \"path\": "
                + "\"src/main/java/com/qa/pages/LoginPage.java\",\n"
                + "      \"content\": "
                + "\"complete source code\"\n"
                + "    }\n"
                + "  ]\n"
                + "}\n\n");

        prompt.append(
                "The content must contain the COMPLETE repaired "
                + "source file.\n");

        prompt.append(
                "Do not return a diff.\n");

        prompt.append(
                "Do not return an explanation.\n");

        prompt.append(
                "Do not return Markdown.\n");

        return prompt.toString();
    }

    /**
     * Reads all Java source files in the generated project and
     * supplies non-affected files to Gemini as read-only context.
     */
    private void appendJavaFrameworkContext(
            File projectDir,
            List<String> affectedFiles,
            StringBuilder frameworkContext) {

        Path root =
                projectDir.toPath();

        try (Stream<Path> paths =
                Files.walk(root)) {

            paths
                    .filter(Files::isRegularFile)
                    .filter(path ->
                            path.toString()
                                    .toLowerCase()
                                    .endsWith(".java"))
                    .forEach(path -> {

                        String relativePath =
                                root
                                        .relativize(path)
                                        .toString()
                                        .replace("\\", "/");

                        /*
                         * Affected files have already been supplied
                         * in their own section.
                         */
                        if (containsIgnoreCase(
                                affectedFiles,
                                relativePath)) {

                            return;
                        }

                        appendFileContent(
                                projectDir,
                                relativePath,
                                frameworkContext,
                                "READ-ONLY FILE");
                    });

        } catch (Exception e) {

            System.out.println(
                    "WARNING: Could not scan generated "
                    + "framework source files.");

            System.out.println(
                    e.getMessage());
        }
    }

    /**
     * Reads one source file and appends it to the supplied context.
     */
    private void appendFileContent(
            File projectDir,
            String relativePath,
            StringBuilder output,
            String sectionName) {

        File file =
                new File(
                        projectDir,
                        relativePath);

        if (!file.exists()
                || !file.isFile()) {

            return;
        }

        try {

            String content =
                    new String(
                            Files.readAllBytes(
                                    file.toPath()),
                            StandardCharsets.UTF_8);

            output
                    .append("\n\n==============================\n")
                    .append(sectionName)
                    .append(": ")
                    .append(relativePath)
                    .append("\n")
                    .append("==============================\n")
                    .append(content);

        } catch (Exception e) {

            System.out.println(
                    "WARNING: Could not read: "
                    + relativePath);

            System.out.println(
                    e.getMessage());
        }
    }

    /**
     * Parses Gemini response.
     *
     * Supports:
     *
     * {
     *   "files": [...]
     * }
     *
     * and:
     *
     * [...]
     */
    private List<GeneratedFile> parseRepairResponse(
            String responseText) {

        List<GeneratedFile> files =
                new ArrayList<GeneratedFile>();

        try {

            String json =
                    extractJson(responseText);

            ObjectMapper objectMapper =
                    new ObjectMapper();

            JsonNode root =
                    objectMapper.readTree(json);

            JsonNode filesNode;

            if (root.isArray()) {

                filesNode = root;

            } else if (root.isObject()
                    && root.has("files")
                    && root.get("files").isArray()) {

                filesNode =
                        root.get("files");

            } else {

                System.out.println(
                        "WARNING: Gemini response does not "
                        + "contain a valid files array.");

                return files;
            }

            files =
                    objectMapper.readValue(
                            filesNode.toString(),
                            new TypeReference<List<GeneratedFile>>() {
                            });

        } catch (Exception e) {

            System.out.println(
                    "ERROR: Could not parse Gemini repair "
                    + "response as JSON.");

            System.out.println(
                    e.getMessage());

            /*
             * Print a small diagnostic portion only.
             * This makes malformed responses easier to diagnose
             * without flooding the Eclipse console.
             */
            String diagnostic =
                    responseText == null
                            ? ""
                            : responseText.trim();

            if (diagnostic.length() > 1000) {

                diagnostic =
                        diagnostic.substring(
                                0,
                                1000)
                        + "...";
            }

            System.out.println(
                    "Gemini response preview:");

            System.out.println(
                    diagnostic);
        }

        return files;
    }

    /**
     * Removes Markdown code fences and isolates the outer JSON.
     */
    private String extractJson(
            String responseText) {

        String text =
                responseText == null
                        ? ""
                        : responseText.trim();

        /*
         * Remove ```json ... ``` or ``` ... ```
         */
        if (text.startsWith("```")) {

            int firstNewLine =
                    text.indexOf('\n');

            int lastFence =
                    text.lastIndexOf("```");

            if (firstNewLine >= 0
                    && lastFence > firstNewLine) {

                text =
                        text.substring(
                                firstNewLine + 1,
                                lastFence)
                        .trim();
            }
        }

        /*
         * Handle an object response.
         */
        int firstObject =
                text.indexOf('{');

        int lastObject =
                text.lastIndexOf('}');

        /*
         * Handle an array response.
         */
        int firstArray =
                text.indexOf('[');

        int lastArray =
                text.lastIndexOf(']');

        /*
         * Determine whether the response is more likely an
         * object or an array.
         */
        if (firstObject >= 0
                && lastObject > firstObject
                && (firstArray < 0
                    || firstObject < firstArray)) {

            return text.substring(
                    firstObject,
                    lastObject + 1);
        }

        if (firstArray >= 0
                && lastArray > firstArray) {

            return text.substring(
                    firstArray,
                    lastArray + 1);
        }

        return text;
    }

    /**
     * Converts an absolute Maven path into a project-relative path.
     *
     * Handles the Windows Maven form:
     *
     * /F:/SELQEDGE/ai-qa-assistance/generated-project/src/...
     *
     * and:
     *
     * F:/SELQEDGE/ai-qa-assistance/generated-project/src/...
     *
     * into:
     *
     * src/...
     */
    private String normalizeRelativePath(
            String reportedPath,
            String projectPrefix) {

        if (reportedPath == null) {

            return "";
        }

        String normalized =
                reportedPath
                        .trim()
                        .replace("\\", "/");

        String normalizedProjectPrefix =
                projectPrefix
                        .trim()
                        .replace("\\", "/");

        /*
         * Maven/Javac on Windows can report:
         *
         * /F:/...
         *
         * while File.getAbsolutePath() returns:
         *
         * F:/...
         */
        if (normalized.matches(
                "^/[A-Za-z]:/.*")) {

            normalized =
                    normalized.substring(1);
        }

        /*
         * Normalize project prefix similarly.
         */
        if (normalizedProjectPrefix.matches(
                "^/[A-Za-z]:/.*")) {

            normalizedProjectPrefix =
                    normalizedProjectPrefix.substring(1);
        }

        /*
         * Remove trailing slash from prefix.
         */
        while (normalizedProjectPrefix.endsWith("/")) {

            normalizedProjectPrefix =
                    normalizedProjectPrefix.substring(
                            0,
                            normalizedProjectPrefix.length() - 1);
        }

        /*
         * Windows drive-letter paths are case-insensitive.
         */
        String lowerPath =
                normalized.toLowerCase();

        String lowerPrefix =
                normalizedProjectPrefix.toLowerCase();

        if (lowerPath.equals(
                lowerPrefix)) {

            return "";
        }

        if (lowerPath.startsWith(
                lowerPrefix + "/")) {

            normalized =
                    normalized.substring(
                            normalizedProjectPrefix.length());
        }

        /*
         * If Maven supplied a relative path already,
         * simply normalize it.
         */
        while (normalized.startsWith("/")) {

            normalized =
                    normalized.substring(1);
        }

        return normalized;
    }

    /**
     * Allows only files that belong to the generated Maven project.
     *
     * This does NOT define the architecture.
     * It simply blocks unrelated files such as .git metadata,
     * arbitrary system files, etc.
     */
    private boolean isAllowedProjectPath(
            String relativePath) {

        if (relativePath == null
                || relativePath.trim().isEmpty()) {

            return false;
        }

        String normalized =
                relativePath
                        .replace("\\", "/");

        /*
         * Block traversal.
         */
        if (normalized.startsWith("../")
                || normalized.contains("/../")
                || normalized.equals("..")
                || normalized.contains(":/")) {

            return false;
        }

        /*
         * Repair is source/resource focused.
         */
        return normalized.startsWith(
                    "src/main/")
                || normalized.startsWith(
                    "src/test/")
                || normalized.equals(
                    "pom.xml");
    }

    /**
     * Makes sure a file remains physically inside the generated
     * project directory.
     */
    private boolean isInsideProject(
            File projectDir,
            File targetFile) {

        try {

            String projectCanonical =
                    projectDir
                            .getCanonicalPath();

            String targetCanonical =
                    targetFile
                            .getCanonicalPath();

            String projectPrefix =
                    projectCanonical
                            + File.separator;

            return targetCanonical
                    .equals(projectCanonical)
                    || targetCanonical
                            .startsWith(projectPrefix);

        } catch (Exception e) {

            return false;
        }
    }

    /**
     * Case-insensitive contains.
     */
    private boolean containsIgnoreCase(
            List<String> values,
            String target) {

        if (values == null
                || target == null) {

            return false;
        }

        for (String value :
                values) {

            if (value != null
                    && value.equalsIgnoreCase(
                            target)) {

                return true;
            }
        }

        return false;
    }

    /**
     * Null-safe string helper.
     */
    private String safe(
            String value) {

        return value == null
                ? ""
                : value;
    }
}