package src.day02;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.annotations.Test;

public class chromeTest {

    @Test
    public void Test()
    {
        WebDriver driver = new ChromeDriver();
        driver.get("https://qa-practice.netlify.app/");
        System.out.println(driver.getCurrentUrl());
        System.out.println(driver.getTitle());
        driver.close(); //close current browser page opened
        driver.quit(); //close all browser pages opened


    }

}
