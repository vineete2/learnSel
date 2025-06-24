package src.day03_Selenium_Options_Locators;

import io.qameta.allure.Description;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.testng.annotations.Test;

public class TestSelenium01 {


    @Description("Options Class")
    @Test
    public void TestSelenium01() throws Exception {

        // EdgeOptions, ChromeOptions, FirefoxOptions, SafariOptions
        ChromeOptions browserOptions = new ChromeOptions();
        browserOptions.addArguments("--start-maximized"); // Start the browser maximized
 //       edgeOptions.addArguments("--disable-notifications"); // Disable notifications
//        edgeOptions.addArguments("--disable-popup-blocking"); // Disable popup blocking
        browserOptions.addArguments("--disable-infobars"); // Disable infobars
        browserOptions.addArguments("--disable-extensions"); // Disable extensions
        browserOptions.addArguments("--incognito"); // Start in incognito mode
 //       browserOptions.addArguments("--headless=new"); // Run in headless mode (no GUI)
//        edgeOptions.addArguments("--remote-allow-origins=*"); // Allow all origins for remote connections
//        edgeOptions.setCapability("platformName", "Windows 10"); // Set platform name

        // Initialize the Edge driver with the options
        WebDriver driver = new ChromeDriver(browserOptions);
        driver.get("https://google.com");
       Thread.sleep(5000); // Sleep for 5 seconds to allow the page to load
        driver.close(); // Close the current tab, not the session




    }
}
