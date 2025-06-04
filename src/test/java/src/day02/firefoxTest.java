package src.day02;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.testng.annotations.Test;

public class firefoxTest {

    @Test
    public void firefoxtest(){
        WebDriver driver= new FirefoxDriver();
        driver.get("https://qa-practice.netlify.app/");


    }
}
