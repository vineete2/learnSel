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
import java.util.*;
import java.util.stream.Collectors;

public class CsvFeedToExcelExporter {

    private static final String EXCEL_FILE = "rss_export.xlsx";
    private static final String SHEET_NAME = "RSS Feed";

    private static final List<String> EXPORT_FIELDS = List.of(
            "Date", "Automation Tools", "Title", "Link", "Plain Description"
    );

    private static final String CSV_URL = "https://rss.app/feeds/eJ0TEDn69t2LYgAK.csv"; // SQA Germany 2

    private static final List<String> DATE_FIELDS = List.of("Date", "Published", "PubDate", "Published Date", "PublishedAt");

    private static final List<DateTimeFormatter> DATE_FORMATTERS = List.of(
            DateTimeFormatter.ISO_INSTANT,
            DateTimeFormatter.ISO_OFFSET_DATE_TIME,
            DateTimeFormatter.RFC_1123_DATE_TIME,
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss z"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd")
    );

    private static final Duration LOOKBACK_PERIOD = Duration.ofHours(25); // little buffer

    @Test
    public void fetchCsvAndExportToExcel() throws Exception {
        Instant now = Instant.now();
        Instant since = now.minus(LOOKBACK_PERIOD);

        System.out.printf("Run at %s — looking for entries since %s%n", now, since);

        // 1. Read already known links + find last row
        Workbook workbook;
        Sheet sheet;
        Set<String> knownLinks = new HashSet<>();
        int nextRowNum;

        File file = new File(EXCEL_FILE);
        if (file.exists()) {
            workbook = new XSSFWorkbook(new FileInputStream(file));
            sheet = workbook.getSheet(SHEET_NAME);
            if (sheet == null) {
                sheet = workbook.createSheet(SHEET_NAME);
            }

            // Read existing links (safer column index lookup)
            Row header = sheet.getRow(0);
            int linkColIdx = findColumnIndex(header, "Link");
            if (linkColIdx < 0) throw new IllegalStateException("Cannot find 'Link' column");

            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;
                Cell c = row.getCell(linkColIdx);
                if (c != null && c.getCellType() == CellType.STRING) {
                    String link = c.getStringCellValue().trim();
                    if (!link.isEmpty()) knownLinks.add(link);
                }
            }

            nextRowNum = sheet.getLastRowNum() + 1;
        } else {
            workbook = new XSSFWorkbook();
            sheet = workbook.createSheet(SHEET_NAME);
            Row header = sheet.createRow(0);
            for (int i = 0; i < EXPORT_FIELDS.size(); i++) {
                header.createCell(i).setCellValue(EXPORT_FIELDS.get(i));
            }
            nextRowNum = 1;
        }

        // 2. Download fresh feed
        List<Map<String, String>> feedEntries = downloadCsvData(CSV_URL);
        if (feedEntries.isEmpty()) {
            System.out.println("No entries in feed.");
            workbook.close();
            return;
        }

        // 3. Add marker row before new entries
        int originalRowCount = nextRowNum;
        if (!feedEntries.isEmpty()) {
            Row marker = sheet.createRow(nextRowNum++);
            marker.createCell(0).setCellValue("─── New entries from feed ────────");
            marker.createCell(1).setCellValue("Source: " + CSV_URL);
            marker.createCell(2).setCellValue("Fetched: " + now.toString());
            // optional: make it bold / background color
            CellStyle style = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            style.setFont(font);
            for (int i = 0; i < 3; i++) {
                marker.getCell(i).setCellStyle(style);
            }
        }

        // 4. Process & append new entries
        int added = 0;
        int skippedDuplicate = 0;
        int skippedOld = 0;

        int dateColIdx    = findColumnIndex(sheet.getRow(0), "Date");
        int titleColIdx   = findColumnIndex(sheet.getRow(0), "Title");
        int linkColIdx    = findColumnIndex(sheet.getRow(0), "Link");
        int descColIdx    = findColumnIndex(sheet.getRow(0), "Plain Description");
        int toolsColIdx   = findColumnIndex(sheet.getRow(0), "Key");

        for (Map<String, String> entry : feedEntries) {
            String link = safe(entry.get("Link")).trim();
            if (link.isEmpty()) continue;

            if (knownLinks.contains(link)) {
                skippedDuplicate++;
                // System.out.println("Duplicate: " + link);
                continue;
            }

            Instant pubDate = parsePublishedDate(entry);
            if (pubDate == null || pubDate.isBefore(since)) {
                skippedOld++;
                continue;
            }

            String description = safe(entry.get("Plain Description")).toLowerCase();

            Set<String> tools = extractAutomationTools(description);
            String toolsStr = tools.isEmpty() ? "" : String.join(", ", tools);

            // Print links
            System.out.printf("→ ADDING   %-35s   %s%n",
                    toolsStr.isEmpty() ? "—" : toolsStr,
                    link.length() > 90 ? link.substring(0, 87) + "…" : link
            );
            Row row = sheet.createRow(nextRowNum++);
            row.createCell(dateColIdx).setCellValue(pubDate.toString());
            row.createCell(toolsColIdx).setCellValue(toolsStr);
            row.createCell(titleColIdx).setCellValue(safe(entry.get("Title")));
            row.createCell(linkColIdx).setCellValue(link);
            row.createCell(descColIdx).setCellValue(safe(entry.get("Plain Description")));

            knownLinks.add(link);
            added++;
        }

        // Auto-size
        for (int i = 0; i < EXPORT_FIELDS.size(); i++) {
            sheet.autoSizeColumn(i);
        }

        // Save
        try (FileOutputStream out = new FileOutputStream(EXCEL_FILE)) {
            workbook.write(out);
        }
        workbook.close();

        System.out.printf("Done.%n  Added: %d%n  Skipped (duplicate): %d%n  Skipped (old): %d%n  Total rows now: %d%n",
                added, skippedDuplicate, skippedOld, sheet.getLastRowNum() + 1);
    }

    // ────────────────────────────────────────────────────────────────
    //  Helpers
    // ────────────────────────────────────────────────────────────────

    private static Instant parsePublishedDate(Map<String, String> entry) {
        for (String field : DATE_FIELDS) {
            String val = safe(entry.get(field)).trim();
            if (val.isEmpty()) continue;

            for (DateTimeFormatter fmt : DATE_FORMATTERS) {
                try {
                    return Instant.from(fmt.parse(val));
                } catch (Exception ignore) {
                    try {
                        ZonedDateTime zdt = ZonedDateTime.parse(val, fmt);
                        return zdt.toInstant();
                    } catch (Exception ignore2) {}
                }
            }
        }
        return null; // or Instant.now() as fallback — your choice
    }

    private static Set<String> extractAutomationTools(String textLower) {
        Map<String, String> patterns = Map.of(
                "selenium",     "Selenium",
                "cypress",      "Cypress",
                "playwright",   "Playwright",
                "webdriver.io", "WebdriverIO",
                "wdio",         "WebdriverIO",
                "appium",       "Appium"
        );

        Set<String> found = new LinkedHashSet<>();
        for (var e : patterns.entrySet()) {
            if (textLower.contains(e.getKey())) {
                found.add(e.getValue());
            }
        }
        return found;
    }

    private static int findColumnIndex(Row header, String name) {
        if (header == null) return -1;
        for (Cell cell : header) {
            if (cell != null && name.equalsIgnoreCase(cell.getStringCellValue().trim())) {
                return cell.getColumnIndex();
            }
        }
        return -1;
    }

    private List<Map<String, String>> downloadCsvData(String urlStr) throws Exception {
        List<Map<String, String>> data = new ArrayList<>();
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(15000);

        try (var reader = new CSVReader(new InputStreamReader(conn.getInputStream()))) {
            String[] headers = reader.readNext();
            if (headers == null) return data;

            String[] line;
            while ((line = reader.readNext()) != null) {
                Map<String, String> map = new LinkedHashMap<>();
                for (int i = 0; i < headers.length && i < line.length; i++) {
                    map.put(headers[i].trim(), line[i]);
                }
                data.add(map);
            }
        }
        return data;
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }
}