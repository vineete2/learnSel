package src.day09_RSS_feed;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndEntryImpl; // Add this import
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

public class RssToExcel {

    @Test
    public void testGetJobPostsFromLast24Hours() throws Exception {
        // Get the RSS feed URL
        String feedUrl = "https://rss.app/feeds/uSmgOVTSWYT5SRxT.xml";  // Replace with your RSS feed URL
        List<SyndEntry> filteredEntries = getJobPostsFromLast24Hours(feedUrl);

        // Assert that the filtered list is not empty
        Assert.assertNotNull(filteredEntries, "Filtered entries should not be null.");
        Assert.assertTrue(filteredEntries.size() > 0, "There should be job posts in the last 24 hours.");
    }

    @Test
    public void testWriteToExcel() throws Exception {
        // Example job posts (using test data here)
        List<SyndEntry> mockJobPosts = new ArrayList<>();
        SyndEntry entry = new SyndEntryImpl(); // Use SyndEntryImpl instead of SyndEntry
        entry.setTitle("Test Job");
        entry.setLink("https://example.com/job1");
        entry.setPublishedDate(new Date());
        mockJobPosts.add(entry);

        // Call the writeToExcel method
        writeToExcel(mockJobPosts);

        // Assert that the Excel file has been created
        File outputFile = new File("Job_Posts_Last_24_Hours.xlsx");
        Assert.assertTrue(outputFile.exists(), "Excel file should be created.");
    }

    // Function to get job posts from the last 24 hours
    private static List<SyndEntry> getJobPostsFromLast24Hours(String feedUrl) throws Exception {
        List<SyndEntry> filteredEntries = new ArrayList<>();
        SyndFeed feed = new SyndFeedInput().build(new java.io.InputStreamReader(new URL(feedUrl).openStream()));
        long currentTime = System.currentTimeMillis();
        long oneDayAgo = currentTime - (24 * 60 * 60 * 1000); // 24 hours ago

        for (SyndEntry entry : feed.getEntries()) {
            long postTime = entry.getPublishedDate().getTime();
            if (postTime >= oneDayAgo) {
                filteredEntries.add(entry);
            }
        }
        return filteredEntries;
    }

    // Function to write job posts to an Excel file
    private static void writeToExcel(List<SyndEntry> jobPosts) throws Exception {
        // Create a workbook and a sheet
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Job Posts");

        // Create header row
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Title");
        header.createCell(1).setCellValue("Link");
        header.createCell(2).setCellValue("Published Date");

        // Write job posts data into the sheet
        int rowNum = 1;
        for (SyndEntry post : jobPosts) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(post.getTitle());
            row.createCell(1).setCellValue(post.getLink());
            row.createCell(2).setCellValue(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(post.getPublishedDate()));
        }

        // Write the Excel file to disk
        File outputFile = new File("Job_Posts_Last_24_Hours.xlsx");
        try (FileOutputStream fileOut = new FileOutputStream(outputFile)) {
            workbook.write(fileOut);
        }

        workbook.close();
    }
}