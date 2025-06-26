package src.day06;

import com.mongodb.client.*;
import com.mongodb.client.model.IndexOptions;
import org.bson.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.Test;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class LinkedInJobTrackerDB {
    @Test
    public void Test() throws Exception {



//        WebDriver driver = new FirefoxDriver();


        //String uri = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(System.getProperty("user.home") + "/.secure/mongodb_uri.txt"))).trim();
//        List<String> config = Files.readAllLines(Paths.get("C:/Users/vineet/IdeaProjects/learnSel/mongodb_uri.txt"));
//        String uri = config.get(0).trim();
//        String email = config.get(1).trim();     // Optional if needed
//        String password = config.get(2).trim();  // Optional if needed
//        String firefoxProfilePath = config.get(3).trim();

        Properties props = new Properties();
        props.load(new FileInputStream("C:/Users/vineet/IdeaProjects/learnSel/config.properties"));

        String uri = props.getProperty("mongodb_uri").trim();
      //  System.out.println("Loaded Mongo URI: [" + uri + "]");
        String email = props.getProperty("email").trim();        // Optional if used
        String password = props.getProperty("password").trim();  // Optional if used
        String firefoxProfilePath = props.getProperty("firefox_profile_path").trim();

        if (!uri.startsWith("mongodb://") && !uri.startsWith("mongodb+srv://")) {
            throw new IllegalArgumentException("Mongo URI is invalid or missing. Check config.properties.");
        }
        if (firefoxProfilePath.isEmpty()) {
            throw new IllegalArgumentException("Firefox profile path is missing in config.properties.");
        }

        FirefoxOptions options = new FirefoxOptions();
        FirefoxProfile profile = new FirefoxProfile(new java.io.File(firefoxProfilePath));
        options.setProfile(profile);

        WebDriver driver = new FirefoxDriver(options);

        // MongoDB Atlas connection string
        try (MongoClient mongoClient = MongoClients.create(uri)) {
            MongoDatabase db = mongoClient.getDatabase("jobs"); // Changed DB name
            System.out.println("✅ Connected to MongoDB Atlas!");
            MongoCollection<Document> collection = db.getCollection("store_jobs_new");  // Changed collection name
            // ✅ Create unique index on "link" field (only runs if not already created)
            collection.createIndex(new Document("link", 1), new IndexOptions().unique(true));
            FindIterable<Document> docs = collection.find();

            System.out.println("Documents in 'jobs' collection:");
            for (Document doc : docs) {
                System.out.println(doc.toJson());// Converts each document to JSON and prints it
            }

            // Initialize Firebase
//         FileInputStream serviceAccount = new FileInputStream("path/to/your/serviceAccountKey.json");
//         FirebaseApp.initializeApp(FirebaseCredentials.fromCertificate(serviceAccount));


// LinkedIn search URL for Germany and Test Automation Selenium jobs posted in the past 24 hours
            String url = "https://www.linkedin.com/jobs/search/?keywords=Test%20Automation%20Selenium&f_TPR=r86400&location=Germany&geoId=101282230";

            while (true) {
                driver.get(url);
                //TimeUnit.SECONDS.sleep(5); // Wait for page to load
                new WebDriverWait(driver, Duration.ofSeconds(15))
                        .until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.WTXqiwRsDNAWuAcyqLmgDiHMaCIebAQmtEzdQ")));

                List<WebElement> jobCards = driver.findElements(By.cssSelector("ul.jobs-search__results-list li"));

                System.out.println("Checking for jobs posted in past 24 hours...\n");
                System.out.println("Found " + jobCards.size() + " job cards\n");

                for (WebElement job : jobCards) {
                    try {
                        String title = "", company = "", timeText = "";

                        // Title: fallback to span.sr-only if h3 is empty
                        try {
                            title = job.findElement(By.cssSelector("h3.base-search-card__title")).getText().trim();
                            if (title.isEmpty()) {
                                title = job.findElement(By.cssSelector("a.base-card__full-link span.sr-only")).getText().trim();
                            }
                        } catch (Exception e) {
                            System.out.println("❌ Title not found");
                        }

                        // Company
                        try {
                            WebElement companyElement = job.findElement(By.cssSelector("h4.base-search-card__subtitle a"));
                            company = companyElement.getText().trim();
                        } catch (Exception e) {
                            System.out.println("❌ Company not found");
                        }

                        // Posted time
                        try {
                            timeText = job.findElement(By.tagName("time")).getText().trim();
                        } catch (Exception e) {
                            System.out.println("❌ Time not found");
                        }

                        // Link
                        String link = job.findElement(By.tagName("a")).getAttribute("href").trim();

                        // Skip if title or company is missing
                        if (title.isEmpty() || company.isEmpty()) {
                            System.out.println("⚠️ Skipping incomplete job card\n");
                            continue;
                        }

                        // Check for duplicates by link
                        Document existing = collection.find(new Document("link", link)).first();
                        if (existing == null) {
                            Document jobDoc = new Document("title", title)
                                    .append("company", company)
                                    .append("link", link)
                                    .append("posted", timeText)
                                    .append("timestamp", new Date())
                                    .append("applied", false);

                            collection.insertOne(jobDoc);
                            System.out.println("✅ Inserted into DB\n");
                        } else {
                            System.out.println("⚠️ Job already exists in DB\n");
                        }
                      //  }
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

