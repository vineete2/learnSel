package src.day14_feed_enhance;


import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.testng.annotations.Test;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

public class CsvFeedToExcelExporter {

    private static final String EXCEL_FILE = "rss_export.xlsx";
    private static final List<String> EXPORT_FIELDS = List.of("Date", "Selenium Mentioned","Title", "Link", "Plain Description" ); // , "Geography"

    // Example Poland feed (as in your code)
    private static final String CSV_URL =
          //  "https://rss.app/feeds/Bo2079SUtLZYsUkz.csv"; //Italy Testing
           // "https://rss.app/feeds/gDv3NbcYeKVJxUZR.csv"; //Italy
            "https://rss.app/feeds/DyxSZgo4qf8pS9zb.csv"; //Playwright Germany
    //  "https://rss.app/feeds/TfrgfHu6qgXhpecJ.csv"; //Belgium
           //       "https://rss.app/feeds/DnUENi9PYodPnehA.csv"; //Germany
       //   "https://rss.app/feeds/o498pCwRG99n9WZ0.csv"; //Poland


    // Candidate header names that may contain the publication date
    private static final List<String> DATE_FIELDS = List.of("Date", "Published", "PubDate", "Published Date", "PublishedAt");

    // Common RSS/CSV date patterns
    private static final List<DateTimeFormatter> DATE_FORMATTERS = List.of(
            // ISO-8601 variants
            DateTimeFormatter.ISO_INSTANT,                                      // 2025-08-18T07:23:45Z
            DateTimeFormatter.ISO_OFFSET_DATE_TIME,                             // 2025-08-18T07:23:45+00:00
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(ZoneOffset.UTC),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX").withZone(ZoneOffset.UTC), // 2025-08-18T07:23:45.123Z or +00:00
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault()),
            // RFC-1123 (typical HTTP/RSS date)
            DateTimeFormatter.RFC_1123_DATE_TIME,                               // Mon, 18 Aug 2025 07:23:45 GMT
            // Fallback simple date
            DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault())
    );

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
                        Cell linkCell = row.getCell(2); // "Link" is third column
                        if (linkCell != null && linkCell.getCellType() == CellType.STRING) {
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

            // Step 3: Append only new entries from last 24h
            int rowCount = sheet.getLastRowNum();
            long nowMs = System.currentTimeMillis();
            long oneDayAgoMs = nowMs - 186_400_000L; // 24 hours 186_400_000L 21_600_000L

            int inserted = 0;
            for (Map<String, String> entry : csvData) {
                String link = safe(entry.get("Link"));
                if (link.isBlank() || existingLinks.contains(link)) {
                    if (!link.isBlank()) System.out.println("⏭️ Skipped duplicate: " + link);
                    continue;
                }

                // Parse date robustly; skip if missing/unparseable
                Long pubTimeMs = parsePublishedMillis(entry);
                if (pubTimeMs == null) {
                    System.out.println("⚠️ Skipping (no/invalid date): " + link);
                    continue;
                }
                if (pubTimeMs < oneDayAgoMs) {
                    System.out.println("⏭️ Skipped old entry: " + link);
                    continue;
                }

                // Check if Selenium is mentioned in the description
                String description = safe(entry.get("Plain Description"));
                String seleniumMentioned = description.toLowerCase().contains("selenium") ? "Yes" : "No";
                System.out.println("🔍 Selenium Mentioned: " + seleniumMentioned + " for Job: " + link);

                // Write row
                Row row = sheet.createRow(++rowCount);
                for (int i = 0; i < EXPORT_FIELDS.size(); i++) {
                    String field = EXPORT_FIELDS.get(i);
                    String value = switch (field) {
                        case "Selenium Mentioned" -> seleniumMentioned;
                        // Normalize the "Date" field to ISO_INSTANT for consistency
                        case "Date" -> Instant.ofEpochMilli(pubTimeMs).toString();
                        default -> safe(entry.get(field));
                    };
                    row.createCell(i).setCellValue(value);
                }

                existingLinks.add(link);
                inserted++;
            }

            // Auto-size columns for readability
            for (int i = 0; i < EXPORT_FIELDS.size(); i++) {
                sheet.autoSizeColumn(i);
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

    /** Try to parse a published date from any of the DATE_FIELDS. Returns epoch millis, or null if not parseable. */
    private Long parsePublishedMillis(Map<String, String> entry) {
        String raw = null;
        for (String key : DATE_FIELDS) {
            raw = entry.get(key);
            if (raw != null && !raw.isBlank()) break;
        }
        if (raw == null || raw.isBlank()) return null;

        String s = raw.trim();
        // Quick normalization: some feeds use "UTC" literal
        s = s.replace(" UTC", " GMT");

        // Try all known formatters
        for (DateTimeFormatter fmt : DATE_FORMATTERS) {
            try {
                // First try as Instant (works for ISO_INSTANT/RFC_1123 with zone)
                Instant inst;
                try {
                    inst = Instant.from(fmt.parse(s));
                    return inst.toEpochMilli();
                } catch (DateTimeParseException ignored) {
                    // Try as LocalDateTime with system default if pattern lacks zone
                    try {
                        LocalDateTime ldt = LocalDateTime.parse(s, fmt);
                        return ldt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                    } catch (DateTimeParseException ignored2) {
                        // Try as ZonedDateTime
                        ZonedDateTime zdt = ZonedDateTime.parse(s, fmt);
                        return zdt.toInstant().toEpochMilli();
                    }
                }
            } catch (DateTimeParseException ignored3) {
                // try next formatter
            }
        }
        // Last-ditch: some feeds provide epoch seconds/millis as strings
        try {
            long n = Long.parseLong(s);
            if (s.length() <= 10) { // seconds
                return n * 1000L;
            } else { // millis
                return n;
            }
        } catch (NumberFormatException ignored) {}

        return null;
    }

    private static String safe(String v) {
        return v == null ? "" : v;
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
                    } catch (CsvValidationException e) {
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
