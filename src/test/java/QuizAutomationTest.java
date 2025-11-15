import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;

public class QuizAutomationTest {

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeClass
    public void setUp() {
        // Automatically manages the ChromeDriver
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().window().maximize();

        // Explicit wait
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // Create screenshots directory
        new File("screenshots").mkdirs();
    }

    /**
     * Helper method to take screenshots
     * @param fileName Name of the screenshot file (without .png)
     */
    private void takeScreenshot(String fileName) {
        try {
            File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            FileUtils.copyFile(srcFile, new File("screenshots/" + fileName + ".png"));
            System.out.println("LOG: Screenshot captured: " + fileName + ".png");
        } catch (IOException e) {
            System.err.println("ERROR: Failed to capture screenshot: " + e.getMessage());
        }
    }

    @Test(priority = 1)
    public void test01_VerifyLandingPage() {
        // Get the absolute path to the HTML file
        String filePath = Paths.get("quiz_app.html").toUri().toString();
        driver.get(filePath);

        String title = driver.getTitle();
        String url = driver.getCurrentUrl();

        System.out.println("LOG: Page Title: " + title);
        System.out.println("LOG: Page URL: " + url);

        Assert.assertEquals(title, "Dynamic Quiz App", "Page title is incorrect.");
        Assert.assertTrue(url.endsWith("quiz_app.html"), "Page URL is incorrect.");

        takeScreenshot("01_LandingPage");
    }

    @Test(priority = 2)
    public void test02_StartQuiz() {
        // Select Category: Science
        Select categorySelect = new Select(driver.findElement(By.id("category")));
        categorySelect.selectByValue("science");

        // Select Difficulty: Easy
        Select difficultySelect = new Select(driver.findElement(By.id("difficulty")));
        difficultySelect.selectByValue("easy");

        // Click Start
        driver.findElement(By.id("start-btn")).click();

        // Verify first question is displayed
        // This is correct: visibilityOfElementLocated returns a WebElement
        WebElement questionText = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("question-text")));
        Assert.assertTrue(questionText.isDisplayed(), "Quiz screen did not appear.");
        Assert.assertEquals(questionText.getText(), "What is the chemical symbol for water?", "First question text is incorrect.");

        takeScreenshot("02_FirstQuestionDisplayed");
    }

    @Test(priority = 3)
    public void test03_QuestionNavigationAndAnswerSelection() {
        // We are already on Question 1 from the previous test

        // --- Question 1: "What is the chemical symbol for water?" ---
        // Verify options
        List<WebElement> options1 = driver.findElements(By.cssSelector(".option-label"));
        Assert.assertEquals(options1.get(0).getText(), "H2O");

        // Select answer "H2O" (index 0)
        options1.get(0).click();
        takeScreenshot("03_Question1_Answered");
        driver.findElement(By.id("next-btn")).click();

        // --- Question 2: "What planet is known as the Red Planet?" ---

        // *** THIS IS THE FIX ***
        // We just wait for the text to be correct. We don't assign the result (which is a Boolean).
        wait.until(ExpectedConditions.textToBe(By.id("question-text"), "What planet is known as the Red Planet?"));

        // Verify options
        List<WebElement> options2 = driver.findElements(By.cssSelector(".option-label"));
        Assert.assertEquals(options2.get(1).getText(), "Mars");

        // Select answer "Mars" (index 1)
        options2.get(1).click();
        takeScreenshot("04_Question2_Answered");
        driver.findElement(By.id("next-btn")).click();

        // --- Question 3: "What is the largest mammal?" ---

        // *** THIS IS THE FIX ***
        // Wait for the text to update.
        wait.until(ExpectedConditions.textToBe(By.id("question-text"), "What is the largest mammal?"));

        // Verify options
        List<WebElement> options3 = driver.findElements(By.cssSelector(".option-label"));
        Assert.assertEquals(options3.get(2).getText(), "Blue Whale");

        // Select answer "Blue Whale" (index 2)
        options3.get(2).click();
        takeScreenshot("05_Question3_Answered");

        // Verify button text changed to "Submit"
        WebElement submitButton = driver.findElement(By.id("next-btn"));
        Assert.assertEquals(submitButton.getText(), "Submit", "Button text should be 'Submit' on last question.");

        // Click Submit
        submitButton.click();
    }

    @Test(priority = 4)
    public void test04_SubmitQuizAndVerifyScore() {
        // Wait for results screen to be visible
        WebElement scoreTextElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("score-text")));

        // Verify Score
        Assert.assertEquals(scoreTextElement.getText(), "3 / 3", "Final score is incorrect.");

        // Verify Correct/Incorrect counts
        WebElement correctCount = driver.findElement(By.id("correct-count"));
        Assert.assertEquals(correctCount.getText(), "3", "Correct count is incorrect.");

        WebElement incorrectCount = driver.findElement(By.id("incorrect-count"));
        Assert.assertEquals(incorrectCount.getText(), "0", "Incorrect count is incorrect.");

        // Verify charts are displayed
        WebElement performanceChart = driver.findElement(By.id("performance-chart"));
        Assert.assertTrue(performanceChart.isDisplayed(), "Performance chart is not displayed.");

        WebElement timeChart = driver.findElement(By.id("time-chart"));
        Assert.assertTrue(timeChart.isDisplayed(), "Time chart is not displayed.");

        System.out.println("LOG: Results Verified Successfully!");
        takeScreenshot("06_ResultAnalysisPage");
    }

    @AfterClass
    public void tearDown() {
        if (driver != null) {
            driver.quit();
            System.out.println("LOG: Browser closed.");
        }
    }
}