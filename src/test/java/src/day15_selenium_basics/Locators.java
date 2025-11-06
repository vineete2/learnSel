package src.day15_selenium_basics;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

public class Locators {

    public static void main(String[] args) {

        WebDriver driver = new ChromeDriver();
        driver.get("https://rahulshettyacademy.com/locatorspractice/");
        driver.findElement(By.id("inputUsername")).sendKeys("vineet");
        driver.findElement(By.name("inputPassword")).sendKeys("test124");
        driver.findElement(By.className("signInBtn")).click();

        System.out.println(driver.findElement(By.cssSelector("p.error")).getText());


    }
}
