package com.bhavin.ai;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class MavenBuildExecutor {

    public BuildResult compile(String projectDirectory) throws Exception {

        // ============================================================
        // 1. VALIDATE PROJECT DIRECTORY
        // ============================================================

        if (projectDirectory == null || projectDirectory.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Generated project directory is null or empty.");
        }

        File projectDir = new File(projectDirectory).getCanonicalFile();
        File pomFile = new File(projectDir, "pom.xml").getCanonicalFile();

        System.out.println();
        System.out.println("========== MAVEN PROJECT VALIDATION ==========");
        System.out.println("Maven working directory : "
                + projectDir.getAbsolutePath());
        System.out.println("Expected POM            : "
                + pomFile.getAbsolutePath());

        if (!projectDir.exists()) {
            throw new IllegalStateException(
                    "Generated project directory does not exist: "
                            + projectDir.getAbsolutePath());
        }

        if (!projectDir.isDirectory()) {
            throw new IllegalStateException(
                    "Generated project path is not a directory: "
                            + projectDir.getAbsolutePath());
        }

        if (!pomFile.exists()) {
            throw new IllegalStateException(
                    "Cannot run Maven because pom.xml does not exist: "
                            + pomFile.getAbsolutePath());
        }

        if (!pomFile.isFile()) {
            throw new IllegalStateException(
                    "pom.xml exists but is not a file: "
                            + pomFile.getAbsolutePath());
        }

        System.out.println("pom.xml FOUND");
        System.out.println("==============================================");


        // ============================================================
        // 2. RESOLVE MAVEN COMMAND
        // ============================================================

        String mavenCommand = System.getenv("MAVEN_HOME");

        if (mavenCommand != null && !mavenCommand.trim().isEmpty()) {

            File mavenCmdFile = new File(
                    mavenCommand,
                    "bin\\mvn.cmd"
            ).getCanonicalFile();

            if (!mavenCmdFile.exists()) {
                throw new IllegalStateException(
                        "MAVEN_HOME is set but mvn.cmd was not found at: "
                                + mavenCmdFile.getAbsolutePath());
            }

            mavenCommand = mavenCmdFile.getAbsolutePath();

        } else {

            // Use Maven available through PATH
            mavenCommand = "mvn.cmd";
        }


        // ============================================================
        // 3. CREATE MAVEN PROCESS
        // ============================================================

        ProcessBuilder processBuilder =
                new ProcessBuilder(
                        mavenCommand,
                        "clean",
                        "test",
                        "-DskipTests"
                );

        processBuilder.directory(projectDir);

        // Combine stderr + stdout so AI receives one complete build log.
        processBuilder.redirectErrorStream(true);


        // ============================================================
        // 4. PRINT EXECUTION INFORMATION
        // ============================================================

        System.out.println();
        System.out.println("========== MAVEN EXECUTION ==========");
        System.out.println("Command        : "
                + mavenCommand + " clean test -DskipTests");
        System.out.println("Working Dir    : "
                + projectDir.getAbsolutePath());
        System.out.println("POM            : "
                + pomFile.getAbsolutePath());
        System.out.println("=====================================");


        // ============================================================
        // 5. START MAVEN
        // ============================================================

        Process process;

        try {

            process = processBuilder.start();

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Unable to start Maven.\n"
                            + "Command: " + mavenCommand + "\n"
                            + "Working directory: "
                            + projectDir.getAbsolutePath()
                            + "\n"
                            + "Reason: " + e.getMessage(),
                    e
            );
        }


        // ============================================================
        // 6. CAPTURE COMPLETE MAVEN OUTPUT
        // ============================================================

        StringBuilder output = new StringBuilder();

        try (BufferedReader reader =
                     new BufferedReader(
                             new InputStreamReader(
                                     process.getInputStream()))) {

            String line;

            while ((line = reader.readLine()) != null) {

                System.out.println(line);

                output.append(line)
                        .append(System.lineSeparator());
            }
        }


        // ============================================================
        // 7. WAIT FOR MAVEN
        // ============================================================

        int exitCode = process.waitFor();


        // ============================================================
        // 8. FINAL RESULT
        // ============================================================

        boolean success = exitCode == 0;

        System.out.println();
        System.out.println("========== MAVEN RESULT ==========");
        System.out.println("Exit Code : " + exitCode);
        System.out.println("Status    : "
                + (success ? "SUCCESS" : "FAILURE"));
        System.out.println("==================================");


        return new BuildResult(
                success,
                output.toString()
        );
    }
}