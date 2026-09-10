package com.bhavin.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;

public class BuildFailureAnalyzer {

	private final Client client;

	public BuildFailureAnalyzer(Client client) {
		this.client = client;
	}

	public BuildFailureAnalysis analyze(BuildResult buildResult, FrameworkGenerationPlan frameworkPlan)
			throws Exception {

		String prompt = "You are a senior QA Automation Architect and Java " + "build failure analysis expert.\n\n"

				+ "A fresh Selenium automation framework was generated " + "automatically by an AI system.\n\n"

				+ "The generated framework was compiled using Maven.\n\n"

				+ "Your task is to analyze the Maven build output and "
				+ "identify the actual root cause of the failure.\n\n"

				+ "IMPORTANT RULES:\n" + "1. Analyze the compiler output carefully.\n"
				+ "2. Do not invent errors that are not present.\n"
				+ "3. Identify the files directly involved in the failure.\n"
				+ "4. Look for cross-file API, constructor, method, package, " + "or dependency mismatches.\n"
				+ "5. Distinguish the root cause from downstream errors.\n"
				+ "6. The affectedFiles list MUST use the exact file paths "
				+ "reported by Maven whenever a path is present in the compiler output.\n"
				+ "7. Do not substitute a similar or duplicate file from the " + "framework plan.\n"
				+ "8. If Maven reports a file path that is not present in the "
				+ "framework plan, still use the exact Maven-reported path.\n"
				+ "9. Do not modify or generate source code at this stage.\n"
				+ "10. Do not provide a manual fix outside the JSON response.\n\n"
				+ "IMPORTANT BUILD DIAGNOSTIC RULES:\n"
				+ "1. First determine whether Maven successfully located pom.xml.\n"
				+ "2. If Maven reports MissingProjectException or 'there is no POM in this directory', "
				+ "do NOT attempt to repair Java source files.\n"
				+ "3. A missing pom.xml is a project-generation or filesystem/lifecycle problem.\n"
				+ "4. Verify the generated project root before proposing source-code repairs.\n"
				+ "5. Never invent a Java file as a solution to a missing pom.xml.\n"
				+ "6. Never change imports or package names to solve a missing pom.xml.\n"
				+ "7. If pom.xml is missing, identify the generation/path/lifecycle failure explicitly.\n"
				+ "8. Distinguish BUILD_CONFIGURATION failures from JAVA_COMPILATION failures.\n"

				+ "FRAMEWORK INFORMATION:\n" + "Framework Name: " + frameworkPlan.getFrameworkName() + "\n"

				+ "Automation Tool: " + frameworkPlan.getAutomationTool() + "\n"

				+ "Language: " + frameworkPlan.getLanguage() + "\n"

				+ "Test Framework: " + frameworkPlan.getTestFramework() + "\n"

				+ "Design Pattern: " + frameworkPlan.getDesignPattern() + "\n\n"

				+ "PLANNED FRAMEWORK FILES:\n";

		for (String file : frameworkPlan.getFiles()) {

			prompt += "- " + file + "\n";
		}

		prompt += "\nIMPORTANT:\n" + "The planned file list is not necessarily identical to the "
				+ "files actually generated on disk.\n" + "Maven compiler output is authoritative for identifying "
				+ "the files that actually failed.\n\n";

		prompt += "\nMAVEN BUILD STATUS:\n" + buildResult.isSuccess() + "\n\n"

				+ "MAVEN BUILD OUTPUT:\n" + buildResult.getOutput() + "\n\n"

				+ "RETURN ONLY VALID JSON.\n" + "Do not return Markdown.\n" + "Do not use code fences.\n"
				+ "Do not add explanations before or after the JSON.\n\n"

				+ "Use exactly this structure:\n"

				+ "{\n" + "  \"hasFailure\": true,\n" + "  \"summary\": \"short description of the failure\",\n"
				+ "  \"rootCause\": \"actual root cause\",\n" + "  \"affectedFiles\": [\n" + "    \"file/path\"\n"
				+ "  ],\n" + "  \"recommendedAction\": \"recommended repair strategy\"\n" + "}";

		GenerateContentResponse response = client.models.generateContent("gemini-3.5-flash-lite", prompt, null);

		String json = response.text();

		ObjectMapper objectMapper = new ObjectMapper();

		return objectMapper.readValue(json, BuildFailureAnalysis.class);
	}
}