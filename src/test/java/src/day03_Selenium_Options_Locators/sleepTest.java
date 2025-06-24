package src.day03_Selenium_Options_Locators;

import io.qameta.allure.Description;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.Test;

public class sleepTest {

    @Description("Open the URL")
    @Test
    public void sleepTest()throws Exception {

        WebDriver driver = new ChromeDriver();
        driver.get("https://google.com");

        // Close - will close the current tab, not the session
        // session id != null

//        Thread.sleep(3000);
//        driver.close();


        try {
            Thread.sleep(5000); // Sleep for 5 seconds
            driver.quit();
            // It will close all the tabs. - session id == null
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
