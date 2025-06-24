package src.day04;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class LinkedInJobTracker {

    public static void main(String[] args) throws InterruptedException {
        // Setup Chrome
//        System.setProperty("webdriver.chrome.driver", "PATH_TO_CHROMEDRIVER"); // <-- Set your path
//
//        ChromeOptions options = new ChromeOptions();
//        options.addArguments("user-data-dir=/path/to/your/Chrome/Profile"); // <-- reuse your logged-in session
//        WebDriver driver = new ChromeDriver(options);
        WebDriver driver= new FirefoxDriver();
        // LinkedIn search URL
     //   String url = "https://www.linkedin.com/jobs/search/?keywords=Test%20Automation%20Selenium&f_TPR=r8600";
// LinkedIn search URL for Germany and Test Automation Selenium jobs posted in the past 24 hours
        String url = "https://www.linkedin.com/jobs/search/?keywords=Test%20Automation%20Selenium&f_TPR=r86400&location=Germany&geoId=101282230";

        while (true) {
            driver.get(url);
            TimeUnit.SECONDS.sleep(5); // Wait for page to load

            List<WebElement> jobCards = driver.findElements(By.cssSelector("ul.jobs-search__results-list li"));

            System.out.println("Checking for jobs posted in past hour...\n");

            for (WebElement job : jobCards) {
                try {
                    String timeText = job.findElement(By.cssSelector("time")).getText();
                    if (timeText.contains("Just now") || timeText.contains("1 hour")) {
                        String title = job.findElement(By.cssSelector("h3")).getText();
                        String company = job.findElement(By.cssSelector("h4")).getText();
                        String link = job.findElement(By.tagName("a")).getAttribute("href");

                        System.out.println("🔔 New Job Found:");
                        System.out.println("Title: " + title);
                        System.out.println("Company: " + company);
                        System.out.println("Link: " + link);
                        System.out.println("Posted: " + timeText);
                        System.out.println("-----------------------------------");
                    }
                } catch (Exception e) {
                    // Handle elements missing expected fields
                    continue;
                }
            }

            System.out.println("Waiting 10 minutes before checking again...");
            TimeUnit.MINUTES.sleep(10);
        }
    }
}

