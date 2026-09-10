package com.bhavin.ai;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

public class ArtifactWriter {

    /**
     * Cleans the generated framework directory.
     *
     * This method must be called ONLY when starting a completely
     * new framework generation.
     */
    public void cleanOutputDirectory(
            String outputDirectory) throws Exception {

        File outputDir =
                new File(outputDirectory);

        /*
         * Create the directory if it does not exist.
         */
        if (!outputDir.exists()) {

            if (!outputDir.mkdirs()
                    && !outputDir.exists()) {

                throw new RuntimeException(
                        "Unable to create generated framework directory: "
                                + outputDirectory);
            }

            return;
        }

        /*
         * IMPORTANT:
         *
         * Keep the generated-project root directory itself.
         * Delete only its contents.
         */
        System.out.println(
                "Cleaning previous generated framework contents...");

        File[] existingFiles =
                outputDir.listFiles();

        if (existingFiles != null) {

            for (File file :
                    existingFiles) {

                deleteRecursively(file);
            }
        }
    }

    /**
     * Writes generated files into the output directory.
     *
     * IMPORTANT:
     * This method NEVER cleans the directory.
     *
     * This is required because the same method is also used to
     * write AI repairs into an already existing generated framework.
     */
    public void writeFiles(
            List<GeneratedFile> generatedFiles,
            String outputDirectory) throws Exception {

        File outputDir =
                new File(outputDirectory);

        /*
         * Create the output directory if necessary.
         */
        if (!outputDir.exists()) {

            if (!outputDir.mkdirs()
                    && !outputDir.exists()) {

                throw new RuntimeException(
                        "Unable to create generated framework directory: "
                                + outputDirectory);
            }
        }

        if (generatedFiles == null
                || generatedFiles.isEmpty()) {

            System.out.println(
                    "No generated files to write.");

            return;
        }

        /*
         * Write files without deleting anything already present.
         */
        for (GeneratedFile generatedFile :
                generatedFiles) {

            if (generatedFile == null) {
                continue;
            }

            String relativePath =
                    generatedFile.getPath();

            String content =
                    generatedFile.getContent();

            if (relativePath == null
                    || relativePath.trim().isEmpty()) {

                System.out.println(
                        "WARNING: Skipping generated file with empty path.");

                continue;
            }

            if (content == null) {

                System.out.println(
                        "WARNING: Skipping generated file with null content: "
                                + relativePath);

                continue;
            }

            File targetFile =
                    new File(
                            outputDir,
                            relativePath);

            File parentDirectory =
                    targetFile.getParentFile();

            if (parentDirectory != null
                    && !parentDirectory.exists()) {

                parentDirectory.mkdirs();
            }

            Files.write(
                    targetFile.toPath(),
                    content.getBytes(
                            StandardCharsets.UTF_8));

            System.out.println(
                    "Written: "
                            + targetFile.getAbsolutePath());
        }
    }

    /**
     * Recursively deletes a file or directory.
     */
    private void deleteRecursively(
            File file) throws Exception {

        if (file == null
                || !file.exists()) {

            return;
        }

        if (file.isDirectory()) {

            File[] children =
                    file.listFiles();

            if (children != null) {

                for (File child :
                        children) {

                    deleteRecursively(child);
                }
            }
        }

        Files.delete(
                file.toPath());
    }
}