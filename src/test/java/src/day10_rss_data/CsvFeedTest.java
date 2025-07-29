package src.day10_rss_data;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.InsertOneOptions;
import com.opencsv.CSVReader;
import org.bson.Document;
import org.bson.conversions.Bson;
import static com.mongodb.client.model.Filters.eq;
import org.testng.annotations.Test;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

public class CsvFeedTest {

    @Test
    public void readAndPrintCsvFeed() {

        String csvUrl = "https://rss.app/feeds/r2vDqX1gFRa4OpLi.csv";
        List<Map<String, String>> csvData = new ArrayList<>();

        try {
            // Step 1: Load RSS CSV
            URL url = new URL(csvUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Read the CSV using OpenCSV
            CSVReader reader = new CSVReader(new InputStreamReader(connection.getInputStream()));
            String[] headers = reader.readNext(); // First row: column headers

            if (headers != null) {
                String[] row;
                while ((row = reader.readNext()) != null) {
                    Map<String, String> rowData = new LinkedHashMap<>();
                    for (int i = 0; i < headers.length && i < row.length; i++) {
                        rowData.put(headers[i], row[i]);
                    }
                    csvData.add(rowData);
                }
            }

            // Print the data
//            for (Map<String, String> entry : csvData) {
//                System.out.println("---- Row ----");
//                for (Map.Entry<String, String> field : entry.entrySet()) {
//                    System.out.println(field.getKey() + ": " + field.getValue());
//                }
//            }

            // Step 2: Load MongoDB URI from config file
            Properties props = new Properties();
            props.load(Files.newBufferedReader(Paths.get("config.properties")));
            String uri = props.getProperty("mongodb_uri").trim();
//            System.out.println(">>> Mongo URI: [" + uri + "]");

            // Step 3: Build date-stamped collection name
            String dateStamp = new SimpleDateFormat("ddMMyyyy").format(new Date());
            String collectionName = "rssEntries_" + dateStamp;


            try (MongoClient mongoClient = MongoClients.create(uri)) {
                MongoDatabase db = mongoClient.getDatabase("testDB");
                MongoCollection<Document> collection = db.getCollection(collectionName);

                System.out.println("✅ Connected to MongoDB Atlas!");
                int insertedCount = 0;

                for (Map<String, String> entry : csvData) {
                    String link = entry.get("Link"); // Assuming 'Link' is the unique identifier
                    if (link == null || link.isBlank()) {
                        continue; // Skip if link is missing
                    }

                    Bson filter = eq("Link", link);
                    if (collection.countDocuments(filter) == 0) {
                        Document doc = new Document();
                        for (Map.Entry<String, String> field : entry.entrySet()) {
                            doc.append(field.getKey(), field.getValue());
                        }
                        collection.insertOne(doc);
                        System.out.println("Inserted: " + doc.toJson());
                        insertedCount++;
                    } else {
                        System.out.println("Skipped (already exists): " + link);
                    }
                }

                System.out.println("✅ Finished. Total new entries inserted: " + insertedCount);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
