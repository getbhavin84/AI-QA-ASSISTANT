package com.bhavin.ai;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;

public class SeleniumCodeGenerator {

	private Client client;

	public SeleniumCodeGenerator(Client client) {
		this.client = client;
	}

	public String generateCode(TestCase testCase, FrameworkContext frameworkContext, PageObjectContext loginPageContext,
			PageObjectContext homePageContext) throws Exception { // Added throws Exception for Thread.sleep

		String prompt =

				"You are a senior SDET and Selenium automation architect.\n\n"

						+ "Generate Selenium WebDriver Java automation code for the following test case.\n\n"

						+ "TEST CASE\n" + "Test Case ID: " + testCase.getTestCaseId() + "\n" + "Title: "
						+ testCase.getTitle() + "\n" + "Preconditions: " + testCase.getPreconditions() + "\n"
						+ "Test Steps: " + testCase.getTestSteps() + "\n" + "Expected Result: "
						+ testCase.getExpectedResult() + "\n" + "Test Data:\n" + "Username: "
						+ (testCase.getTestData() != null ? testCase.getTestData().getUsername() : "Not provided")
						+ "\n" + "Password: "
						+ (testCase.getTestData() != null ? testCase.getTestData().getPassword() : "Not provided")
						+ "\n\n"

						+ "FRAMEWORK CONTEXT\n" + "Automation Tool: " + frameworkContext.getAutomationTool() + "\n"
						+ "Language: " + frameworkContext.getLanguage() + "\n" + "Test Framework: "
						+ frameworkContext.getTestFramework() + "\n" + "Design Pattern: "
						+ frameworkContext.getDesignPattern() + "\n" + "Browser Management: "
						+ frameworkContext.getBrowserManagement() + "\n" + "Assertion Library: "
						+ frameworkContext.getAssertionLibrary() + "\n\n" + "TEST CLASS CONTEXT\n"
						+ "Test Package: tests\n" + "Base Test Class: base.BaseTest\n"
						+ "The generated TestNG test class must belong to the tests package.\n"
						+ "The BaseTest class must be imported from the base package.\n\n"
						+ "AVAILABLE PAGE OBJECTS\n\n"

						+ "Page Object: " + loginPageContext.getPageName() + "\n" + "Package: "
						+ loginPageContext.getPackageName() + "\n" + "Available Methods: "
						+ loginPageContext.getAvailableMethods() + "\n\n"

						+ "Page Object: " + homePageContext.getPageName() + "\n" + "Package: "
						+ homePageContext.getPackageName() + "\n" + "Available Methods: "
						+ homePageContext.getAvailableMethods() + "\n\n"

						+ "STRICT AUTOMATION RULES\n"

						+ "1. Generate a TestNG test class containing exactly one @Test method.\n"

						+ "2. Do NOT create a main() method.\n"

						+ "3. Do NOT create, initialize, configure, or quit WebDriver.\n"

						+ "4. WebDriver lifecycle is completely managed by the existing framework.\n"

						+ "5. Do NOT create new Page Object classes.\n"

						+ "6. Do NOT create new Page Object methods.\n"

						+ "7. Use ONLY the Page Objects and methods explicitly provided above.\n"

						+ "8. Do NOT use driver.findElement(), By.id(), By.name(), XPath, CSS selectors, "
						+ "or any other direct Selenium locator.\n"

						+ "9. Perform UI actions through Page Object methods.\n"

						+ "10. Assertions must be written in the TestNG test class using Assert.\n"

						+ "11. Verify the expected final application state, not merely that a button was clicked.\n"

						+ "12. For a successful login, prefer verification through HomePage.isUserLoggedIn().\n"

						+ "13. Do not duplicate assertions that verify the same outcome.\n"

						+ "14. Do not invent URLs, locators, Page Objects, methods, or framework infrastructure.\n"

						+ "15. Do not assume unavailable framework details.\n"

						+ "16. If required information is unavailable, use a clearly marked TODO placeholder.\n"

						+ "17. Use the supplied test data exactly as provided.\n"

						+ "18. Do not generate additional test cases.\n"

						+ "19. Generate clean, readable, maintainable Java code.\n"

						+ "20. Return ONLY the Java source code. Do not use Markdown code fences or explanations.\n\n"
						+ "21. The generated TestNG test class MUST extend base.BaseTest.\n"
						+ "22. Use the exact package names and Page Object classes provided by the framework.\n"
						+ "23. Do NOT invent package names such as com.example.tests or com.example.pages.\n"
						+ "24. The available Page Objects belong to the existing framework and must be imported from their actual packages.\n"
						+ "25. If a Page Object method declares throws InterruptedException, the test method may declare throws InterruptedException.\n"
						+ "26. The generated test class must use package tests.\n"
						+ "27. Import BaseTest from base.BaseTest.\n"
						+ "28. Import Page Objects from the exact packages provided in the Page Object Context.\n"
						+ "29. Do not place the test class in the base package just because BaseTest is in that package.\n"
						
						+ "IMPORTANT:\n"
						+ "The generated code will be reviewed by a QA automation architect before execution.\n"
						+ "Follow the supplied framework contract exactly.";
		
		String prompttemp =
				"You are a senior SDET and Selenium automation architect.\n\n"
						+ "Generate Selenium WebDriver Java automation code for the following test case.\n\n"
						
						// Keep your existing TEST CASE and FRAMEWORK CONTEXT prompt blocks intact...

						// ADD THIS BLOCK: Gives context to utilities so it doesn't fail the constructor match
						+ "AVAILABLE UTILITIES & CONSTRUCTORS\n"
						+ "- com.qa.utils.WaitHelper: Constructor requires EXACTLY two arguments -> new WaitHelper(WebDriver driver, int timeoutInSeconds);\n\n"

						+ "AVAILABLE PAGE OBJECTS\n\n"
						+ "Page Object: " + loginPageContext.getPageName() + "\n"
						+ "Package: " + loginPageContext.getPackageName() + "\n"
						+ "Available Methods: " + loginPageContext.getAvailableMethods() + "\n\n"

						+ "Page Object: " + homePageContext.getPageName() + "\n"
						+ "Package: " + homePageContext.getPackageName() + "\n"
						+ "Available Methods: " + homePageContext.getAvailableMethods() + "\n\n"

						+ "STRICT AUTOMATION RULES\n"
						// Keep rules 1-5 intact...
						// CRITICAL FIX FOR RULE 6 & 7: Prevent method hallucinations (like getLoginButton)
						+ "6. Do NOT create or call new Page Object methods. CRITICAL: If a method (such as a getter like getLoginButton()) is NOT explicitly listed in the 'Available Methods' section above, you are strictly FORBIDDEN from calling it.\n"
						+ "7. Use ONLY the Page Objects and methods explicitly provided above. When instantiating WaitHelper, you must pass both arguments: new WaitHelper(driver, 10);\n"
						// Keep rules 8-20 intact...
						+ "21. The generated TestNG test class MUST extend base.BaseTest.\n"
						+ "22. Use the exact package names and Page Object classes provided by the framework.\n"
						+ "23. Do NOT invent package names such as com.example.tests or com.example.pages.\n"
						+ "24. The available Page Objects belong to the existing framework and must be imported from their actual packages.\n"
						+ "25. If a Page Object method declares throws InterruptedException, the test method may declare throws InterruptedException.\n"
						+ "26. The generated test class must use package tests.\n"
						+ "27. Import BaseTest from base.BaseTest.\n"
						+ "28. Import Page Objects from the exact packages provided in the Page Object Context.\n"
						+ "29. Do not place the test class in the base package just because BaseTest is in that package.\n\n"
						
						+ "IMPORTANT:\n"
						+ "The generated code will be reviewed by a QA automation architect before execution.\n"
						+ "Follow the supplied framework contract exactly. Do not invent methods or change constructor signatures.";

		GenerateContentResponse response = client.models.generateContent("gemini-3.5-flash-lite", prompttemp, null);
		return response.text();
	}
}
