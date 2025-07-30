package src.day11_rss_excel;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.testng.annotations.Test;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

public class CsvFeedToExcelExporter {

    private static final String EXCEL_FILE = "rss_export.xlsx";
    private static final List<String> EXPORT_FIELDS = List.of("Title", "Link", "Plain Description", "Date");
    private static final String CSV_URL = "https://rss.app/feeds/r2vDqX1gFRa4OpLi.csv";

    @Test
    public void fetchCsvAndExportToExcel() {
        try {
            // Step 1: Download CSV from RSS feed
            List<Map<String, String>> csvData = downloadCsvData(CSV_URL);
            if (csvData.isEmpty()) {
                System.out.println("⚠️ No data found in the CSV feed.");
                return;
            }

            // Step 2: Load or create Excel workbook
            File file = new File(EXCEL_FILE);
            Workbook workbook;
            Sheet sheet;
            Set<String> existingLinks = new HashSet<>();

            if (file.exists()) {
                try (InputStream fis = new FileInputStream(file)) {
                    workbook = new XSSFWorkbook(fis);
                    sheet = workbook.getSheetAt(0);
                    for (Row row : sheet) {
                        if (row.getRowNum() == 0) continue;
                        Cell linkCell = row.getCell(1); // "Link" is second column
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

            // Step 3: Append only new entries
            int rowCount = sheet.getLastRowNum();
            int inserted = 0;
            for (Map<String, String> entry : csvData) {
                String link = entry.get("Link");
                if (link == null || link.isBlank() || existingLinks.contains(link)) {
                    if (link != null) System.out.println("⏭️ Skipped duplicate: " + link);
                    continue;
                }

                Row row = sheet.createRow(++rowCount);
                for (int i = 0; i < EXPORT_FIELDS.size(); i++) {
                    String field = EXPORT_FIELDS.get(i);
                    String value = entry.getOrDefault(field, "");
                    row.createCell(i).setCellValue(value);
                }

                existingLinks.add(link);
                inserted++;
                System.out.println("✅ Exported: " + link);
            }

            // Step 4: Save workbook
            try (OutputStream fos = new FileOutputStream(EXCEL_FILE)) {
                workbook.write(fos);
            }
            workbook.close();

            System.out.printf("✅ Finished. %d new entries written to %s%n", inserted, EXCEL_FILE);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<Map<String, String>> downloadCsvData(String csvUrl) throws IOException {
        List<Map<String, String>> csvData = new ArrayList<>();

        URL url = new URL(csvUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        try (CSVReader reader = new CSVReader(new InputStreamReader(connection.getInputStream()))) {
            String[] headers = reader.readNext(); // First row = headers
            if (headers != null) {
                String[] row;
                while (true) {
                    try {
                        row = reader.readNext();
                        if (row == null) break;
                    } catch (com.opencsv.exceptions.CsvValidationException e) {
                        System.err.println("⚠️ Skipping malformed CSV row: " + e.getMessage());
                        continue;
                    }

                    Map<String, String> rowData = new LinkedHashMap<>();
                    for (int i = 0; i < headers.length && i < row.length; i++) {
                        rowData.put(headers[i], row[i]);
                    }
                    csvData.add(rowData);
                }
            }
        } catch (CsvValidationException e) {
            throw new RuntimeException(e);
        }

        return csvData;
    }

}
