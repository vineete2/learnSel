package src.day10_rss_data;

import com.mongodb.client.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.bson.Document;
import org.testng.annotations.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static org.testng.Assert.*;

public class MongoToExcel {

    private static final String EXCEL_FILE = "rss_export.xlsx";
    private static final String CONFIG_FILE = "config.properties";
    private static final String DB_NAME = "testDB";
    private static final List<String> EXPORT_FIELDS = List.of("Title", "Link", "Plain Description", "Date");

    @Test
    public void exportSelectedFieldsToExcel() {
        try {
            // Step 1: Load MongoDB URI from config
            Properties props = new Properties();
            props.load(Files.newBufferedReader(Paths.get(CONFIG_FILE)));
            String uri = props.getProperty("mongodb_uri").trim();
            assertNotNull(uri, "MongoDB URI not found in config file");

            try (MongoClient mongoClient = MongoClients.create(uri)) {
                MongoDatabase db = mongoClient.getDatabase(DB_NAME);

                for (String collectionName : db.listCollectionNames()) {
                    if (collectionName.startsWith("rssEntries_")) {
                        exportCollection(db.getCollection(collectionName));
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            fail("Export failed: " + e.getMessage());
        }
    }

    private void exportCollection(MongoCollection<Document> collection) throws IOException {
        File file = new File(EXCEL_FILE);
        Workbook workbook;
        Sheet sheet;
        Set<String> existingLinks = new HashSet<>();

        // Load or create workbook/sheet
        if (file.exists()) {
            try (InputStream fis = new FileInputStream(file)) {
                workbook = new XSSFWorkbook(fis);
                sheet = workbook.getSheetAt(0);
                for (Row row : sheet) {
                    if (row.getRowNum() == 0) continue;
                    Cell linkCell = row.getCell(1); // Assuming "Link" is 2nd column
                    if (linkCell != null) {
                        existingLinks.add(linkCell.getStringCellValue());
                    }
                }
            }
        } else {
            workbook = new XSSFWorkbook();
            sheet = workbook.createSheet("RSS Feed");
            Row header = sheet.createRow(0);
            for (int i = 0; i < EXPORT_FIELDS.size(); i++) {
                header.createCell(i).setCellValue(EXPORT_FIELDS.get(i));
            }
        }

        int rowCount = sheet.getLastRowNum();
        int inserted = 0;

        for (Document doc : collection.find()) {
            String link = doc.getString("Link");
            if (link != null && existingLinks.contains(link)) {
                System.out.println("⏭️ Skipped duplicate: " + link);
                continue;
            }

            Row row = sheet.createRow(++rowCount);
            for (int i = 0; i < EXPORT_FIELDS.size(); i++) {
                String key = EXPORT_FIELDS.get(i);
                Object value = doc.get(key);
                if (value != null) {
                    row.createCell(i).setCellValue(value.toString());
                }
            }

            if (link != null) existingLinks.add(link);
            System.out.println("✅ Exported: " + link);
            inserted++;
        }

        try (OutputStream fos = new FileOutputStream(EXCEL_FILE)) {
            workbook.write(fos);
        }
        workbook.close();

        System.out.printf("✅ %s: %d new rows written to %s%n", collection.getNamespace().getCollectionName(), inserted, EXCEL_FILE);
    }
}
