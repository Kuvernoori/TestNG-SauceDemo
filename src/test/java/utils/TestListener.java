package utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TestListener implements ITestListener {
    private static final Logger logger = LogManager.getLogger(TestListener.class);
    public static ExtentReports extent;
    public static ExtentTest test;
    public static WebDriver driver;

    @Override
    public void onStart(ITestContext context) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        ExtentSparkReporter spark = new ExtentSparkReporter("test-output/ExtentReport_" + timestamp + ".html");
        extent = new ExtentReports();
        extent.attachReporter(spark);
        logger.info("Test suite started");
    }

    @Override
    public void onTestStart(ITestResult result) {
        test = extent.createTest(result.getMethod().getMethodName());
        logger.info("Test started: {}", result.getName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        test.log(Status.PASS, "Test passed");
        logger.info("Test passed: {}", result.getName());
    }

    @Override
    public void onTestFailure(ITestResult result) {
        test.log(Status.FAIL, result.getThrowable());
        if (driver != null) {
            String path = captureScreenshot(result.getName());
            if (!path.isEmpty()) {
                try {
                    test.fail("Screenshot on failure",
                            com.aventstack.extentreports.MediaEntityBuilder.createScreenCaptureFromPath(path).build());
                } catch (Exception e) {
                    logger.error("Failed to attach screenshot", e);
                }
            }
        }
        logger.error("Test failed: {}", result.getName(), result.getThrowable());
    }

    @Override
    public void onFinish(ITestContext context) {
        if (extent != null) {
            extent.flush();
        }
        logger.info("Test suite finished. Report generated in test-output/");
    }

    private String captureScreenshot(String testName) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String path = "screenshots/" + testName + "_" + timestamp + ".png";
        new File("screenshots").mkdirs();
        try {
            File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Files.copy(src.toPath(), new File(path).toPath());
            logger.info("Screenshot saved: {}", path);
            return path;
        } catch (IOException e) {
            logger.error("Failed to save screenshot", e);
            return "";
        }
    }
}