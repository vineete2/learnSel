package src.day06;

import com.mongodb.client.*;
import org.bson.Document;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class MongoDuplicateCleanerTest {

    @Test
    public void removeRepostsAndDeadLinks() throws Exception {
        String uri = new String(Files.readAllBytes(Paths.get("C:/Users/vineet/IdeaProjects/learnSel/mongodb_uri.txt"))).trim();

        try (MongoClient mongoClient = MongoClients.create(uri)) {
            MongoDatabase db = mongoClient.getDatabase("jobs");
            MongoCollection<Document> collection = db.getCollection("store_jobs");

            System.out.println("🔍 Scanning for reposts and dead job links...\n");

            int total = 0, deleted = 0;

            for (Document doc : collection.find()) {
                total++;

                String id = doc.getObjectId("_id").toHexString();
                String link = doc.getString("link");
                String posted = doc.getString("posted");

                // Delete if marked as reposted
                if (posted != null && posted.toLowerCase().contains("reposted")) {
                    collection.deleteOne(new Document("_id", doc.getObjectId("_id")));
                    deleted++;
                    System.out.println("🗑️ Removed reposted job: " + link);
                    continue;
                }

                // Verify if job link is still valid (optional, but useful)
                if (link != null && !isUrlLive(link)) {
                    collection.deleteOne(new Document("_id", doc.getObjectId("_id")));
                    deleted++;
                    System.out.println("🗑️ Removed dead link: " + link);
                }
            }

            System.out.println("\n✅ Cleanup complete.");
            System.out.println("🔎 Total checked: " + total);
            System.out.println("🧹 Total removed: " + deleted);
        }
    }

    private boolean isUrlLive(String urlStr) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(urlStr).openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            int responseCode = connection.getResponseCode();
            return responseCode >= 200 && responseCode < 400;
        } catch (IOException e) {
            return false;
        }
    }
}
