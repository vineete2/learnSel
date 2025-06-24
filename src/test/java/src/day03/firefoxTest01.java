package src.day03;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

public class firefoxTest01 {

    @Test
    public void firefoxtest(){
        WebDriver driver= new FirefoxDriver();
        // adding implicit wait of 12 secs
        driver.manage().timeouts().implicitlyWait(120, TimeUnit.SECONDS);

        driver.get("https://qa-practice.netlify.app/");
        driver.manage().window().maximize();
        driver.findElement(By.cssSelector("#auth-shop > b")).click();
        // get browser title after browser launch
        System.out.println("Browser title: " + driver.getTitle());

      //  WebElement e = driver.findElement(By.xpath("<value of xpath>"));
       // String text = driver.findElement(By.xpath("<value of xpath>")).getAttribute("value");
        driver.findElement(By.id("email")).click();
        driver.findElement(By.id("email")).sendKeys("testemail");

        // Locate the search box using name attribute
       // WebElement searchBox = driver.findElement(By.name("q"));
      //  WebElement username = driver.findElement(By.id("email"));

        driver.close(); //close current browser page opened
      //  driver.quit(); //close all browser pages opened

    }
}
