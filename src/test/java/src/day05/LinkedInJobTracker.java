package src.day05;

import com.google.firebase.FirebaseApp;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.testng.annotations.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
 public class LinkedInJobTracker {
     @Test
     public void Test() throws Exception {

         WebDriver driver = new FirefoxDriver();

         // MongoDB Atlas connection string
         //String uri = "mongodb+srv://vineete2:yybB3lJ4UCCYNl3v@cluster007.7ipfcej.mongodb.net/?retryWrites=true&w=majority&appName=Cluster007";

         String uri = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(System.getProperty("user.home") + "/.secure/mongodb_uri.txt"))).trim();

         try (MongoClient mongoClient = MongoClients.create(uri)) {
             MongoDatabase db = mongoClient.getDatabase("testDB");
             System.out.println("✅ Connected to MongoDB Atlas!");


             // Initialize Firebase
//         FileInputStream serviceAccount = new FileInputStream("path/to/your/serviceAccountKey.json");
//         FirebaseApp.initializeApp(FirebaseCredentials.fromCertificate(serviceAccount));


// LinkedIn search URL for Germany and Test Automation Selenium jobs posted in the past 24 hours
             String url = "https://www.linkedin.com/jobs/search/?keywords=Test%20Automation%20Selenium&f_TPR=r8600&location=Germany&geoId=101282230";

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
 }

