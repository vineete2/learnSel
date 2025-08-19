package src.day13_rss_toExcel;


import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

public class JobApplicationAutomation {

    WebDriver driver;

    @BeforeClass
    public void setUp() {
        // Set path to your chromedriver
       // System.setProperty("webdriver.chrome.driver", "path/to/chromedriver");

        // Configure Chrome options to load plugins and ensure a smooth experience
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized"); // Open in maximized window
        options.addArguments("--disable-extensions"); // Disable Chrome extensions
        options.addArguments("--enable-plugins"); // Enable plugins

        // Initialize WebDriver
        driver = new ChromeDriver(options);
    }

    @Test
    public void testJobApplication() {
        // A list of job URLs that mention Selenium
        String[] seleniumJobLinks = {
                "https://www.linkedin.com/jobs/search/?f_TPR=r86400&geoId=105015875&keywords=Test%20Automation%20Selenium",
                "https://www.linkedin.com/jobs/search/?f_TPR=r86400&geoId=105646813&keywords=Test%20Automation%20Selenium",
                // Add more URLs here
        };

        // Iterate through each job link and apply
        for (String jobLink : seleniumJobLinks) {
            applyForJob(driver, jobLink);
        }

        // After browsing the job listing, wait for user verification before submitting
        System.out.println("⚠️ Please verify the application and submit manually.");
        System.out.println("Press Enter to exit after verification.");
        try {
            System.in.read(); // Wait for user input before closing the browser
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void applyForJob(WebDriver driver, String jobLink) {
        try {
            // Navigate to the job link
            driver.get(jobLink);
            driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS); // Wait for page load

            // Wait until the "Apply" button is visible (example for LinkedIn)
            WebElement applyButton = driver.findElement(By.xpath("//button[contains(text(), 'Apply')]"));
            applyButton.click(); // Click "Apply" button
            driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS); // Wait for apply page

            // Optionally fill out the application form (example: filling name field)
            WebElement nameField = driver.findElement(By.name("firstName"));
            nameField.sendKeys("John Doe"); // Fill out name field (customize for other fields)

            // You can add more fields to fill out (like email, resume, etc.)
            // Example: WebElement emailField = driver.findElement(By.name("email"));
            // emailField.sendKeys("email@example.com");

            System.out.println("🔍 Job opened for " + jobLink);

        } catch (Exception e) {
            System.out.println("⚠️ Could not open job: " + jobLink + " due to " + e.getMessage());
        }
    }

    // Quit the browser after tests
    @AfterClass
    public void tearDown() {
        if (driver != null) {
            driver.quit(); // Close the browser
        }
    }
}
