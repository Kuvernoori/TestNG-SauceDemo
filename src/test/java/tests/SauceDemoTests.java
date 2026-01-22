package tests;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import pages.InventoryPage;
import pages.LoginPage;
import utils.TestListener;

public class SauceDemoTests {
    private static final Logger logger = LogManager.getLogger(SauceDemoTests.class);
    private WebDriver driver;
    private LoginPage loginPage;

    @BeforeClass
    public void setUp() {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();

        options.addArguments("--disable-features=PasswordLeakDetection");

        java.util.Map<String, Object> prefs = new java.util.HashMap<>();
        prefs.put("profile.password_manager_leak_detection", false);
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);

        options.setExperimentalOption("prefs", prefs);

        options.addArguments("--disable-notifications");
        options.addArguments("--no-sandbox");

        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        TestListener.driver = driver;
        logger.info("Browser started with leak detection disabled");
    }

    @BeforeMethod
    public void openSite() {
        driver.get("https://www.saucedemo.com/");
        loginPage = new LoginPage(driver);
        logger.info("Opened saucedemo.com");
    }

    @Test(description = "TC-01 Successful login with standard user")
    public void testSuccessfulLogin() {
        logger.info("Entering credentials");
        loginPage.login("standard_user", "secret_sauce");

        InventoryPage inventory = new InventoryPage(driver);
        logger.info("Verifying inventory page");
        Assert.assertTrue(inventory.isInventoryPage(), "Products title not found");
        /* Assert.assertEquals(
                inventory.getText(By.className("title")),
                "Wrong Title - demo",
                "Bla bla bla"
        ); */
    }

    @Test(description = "TC-02 Login with invalid password")
    public void testInvalidPassword() {
        logger.info("Entering invalid credentials");
        loginPage.login("standard_user", "wrongpass123");

        logger.info("Checking error message");
        String error = loginPage.getErrorText();
        Assert.assertTrue(error.contains("Epic sadface"), "Expected login error message");
    }

    @Test(description = "TC-03 Add product to cart after login")
    public void testAddToCart() {
        logger.info("Logging in");
        loginPage.login("standard_user", "secret_sauce");

        InventoryPage inventory = new InventoryPage(driver);
        logger.info("Adding first product");
        inventory.addFirstProductToCart();

        logger.info("Verifying cart badge");
        Assert.assertEquals(inventory.getCartCount(), "1", "Cart should contain 1 item");
    }

    @AfterClass
    public void tearDown() {
        if (driver != null) {
            driver.quit();
            logger.info("Browser closed");
        }
        if (TestListener.extent != null) {
            TestListener.extent.flush();
            logger.info("Extent report flushed");
        }
    }
}