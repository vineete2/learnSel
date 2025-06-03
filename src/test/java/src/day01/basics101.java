package src.day01;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.testng.annotations.Test;

public class basics101 {

@Test
    public void test_loginToWebsite(){

    WebDriver driver= new FirefoxDriver();
    driver.get("https://www.google.com");
    System.out.println(driver.getTitle());
    System.out.println(driver.getCurrentUrl());
}

}
