package src.day03;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.interactions.Actions;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

public class loginTest {

    @Test
    public void firefoxtest(){
        WebDriver driver= new FirefoxDriver();
        // adding implicit wait of 12 secs
        //driver.manage().timeouts().implicitlyWait(120, TimeUnit.SECONDS);
        driver.manage().timeouts().implicitlyWait(java.time.Duration.ofSeconds(120));

        driver.get("https://qa-practice.netlify.app/");
        driver.manage().window().maximize();
        driver.findElement(By.cssSelector("#auth-shop > b")).click();
        driver.findElement(By.id("email")).click();
        driver.findElement(By.id("email")).sendKeys("admin@admin.com");
        driver.findElement(By.id("password")).sendKeys("admin123");
        driver.findElement(By.id("submitLoginBtn")).click();
        driver.findElement(By.cssSelector(".shop-item:nth-child(1) .btn")).click();
        {
            WebElement element = driver.findElement(By.cssSelector(".shop-item:nth-child(1) .btn"));
            Actions builder = new Actions(driver);
            builder.moveToElement(element).perform();
        }
        {
            WebElement element = driver.findElement(By.tagName("body"));
            Actions builder = new Actions(driver);
            builder.moveToElement(element, 0, 0).perform();
        }
        driver.findElement(By.id("prooood")).click();

        driver.close(); //close current browser page opened
      //  driver.quit(); //close all browser pages opened

    }
}
