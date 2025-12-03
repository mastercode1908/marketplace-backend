//package com.group7.marketplacesystem.identity.frontend;
//
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.openqa.selenium.By;
//import org.openqa.selenium.WebDriver;
//import org.openqa.selenium.WebElement;
//import org.openqa.selenium.chrome.ChromeDriver;
//import org.openqa.selenium.chrome.ChromeOptions;
//import org.openqa.selenium.support.ui.WebDriverWait;
//
//import java.time.Duration;
//
//import static org.junit.jupiter.api.Assertions.assertTrue;
//
//public class LoginTest {
//
//    private WebDriver driver;
//    private WebDriverWait wait;
//
//    @BeforeEach
//    public void setUp() {
//        System.setProperty("webdriver.chrome.driver",
//                "E:/chromedriver-win64/chromedriver-win64/chromedriver.exe");
//
//        ChromeOptions options = new ChromeOptions();
//        options.addArguments("--start-maximized");
//
//        driver = new ChromeDriver(options);
//        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
//
//        driver.get("http://localhost:5173/login");
//    }
//
//
//    private WebElement waitForAnyError() {
//        return wait.until(d -> {
//
//            // Toastify
//            var toast = d.findElements(By.cssSelector(".Toastify__toast-body"));
//            if (!toast.isEmpty()) return toast.get(0);
//
//            // Snackbar (Material UI)
//            var snackbar = d.findElements(By.cssSelector(".MuiAlert-message"));
//            if (!snackbar.isEmpty()) return snackbar.get(0);
//
//            // Inline errors
//            var inline = d.findElements(By.cssSelector("span, p, div"));
//            for (WebElement e : inline) {
//                String text = e.getText().toLowerCase();
//                if (
//                        text.contains("lỗi")
//                                || text.contains("không")
//                                || text.contains("thất")
//                                || text.contains("hợp lệ")
//                ) {
//                    return e;
//                }
//            }
//
//            return null; // tiếp tục chờ
//        });
//    }
//
//    private void login(String email, String password) {
//
//        WebElement emailInput = driver.findElement(By.cssSelector("input[type='email']"));
//        WebElement passwordInput = driver.findElement(By.cssSelector("input[type='password']"));
//        WebElement loginButton = driver.findElement(By.cssSelector("button[type='submit']"));
//
//        emailInput.clear();
//        passwordInput.clear();
//
//        emailInput.sendKeys(email);
//        passwordInput.sendKeys(password);
//
//        // Trigger blur() để FE validate realtime
//        driver.findElement(By.tagName("body")).click();
//
//        loginButton.click();
//    }
//
//    @Test
//    public void TC01_emailEmpty_shouldShowError() {
//        login("", "Ninh@123");
//        WebElement error = waitForAnyError();
//        assertTrue(error.isDisplayed());
//    }
//
//    @Test
//    public void TC02_emailSpace_shouldShowError() {
//        login("   ", "Ninh@123");
//        WebElement error = waitForAnyError();
//        assertTrue(error.isDisplayed());
//    }
//
//    @Test
//    public void TC03_emailInvalid_shouldShowError() {
//        login("abc.com", "Ninh@123");
//        WebElement error = waitForAnyError();
//        assertTrue(error.isDisplayed());
//    }
//
//    @Test
//    public void TC04_passwordEmpty_shouldShowError() {
//        login("buyer_a@example.com", "");
//        WebElement error = waitForAnyError();
//        assertTrue(error.isDisplayed());
//    }
//
//    @Test
//    public void TC05_passwordTooShort_shouldShowError() {
//        login("buyer_a@example.com", "123");
//        WebElement error = waitForAnyError();
//        assertTrue(error.isDisplayed());
//    }
//
//    @Test
//    public void TC06_validLogin_shouldSucceed() throws InterruptedException {
//        login("buyer_a@example.com", "12345678");
//        Thread.sleep(2000);
//
//        String currentUrl = driver.getCurrentUrl();
//        assertTrue(
//                currentUrl.contains("/home")
//                        || currentUrl.contains("/seller")
//                        || currentUrl.contains("/admin")
//        );
//    }
//
//    @AfterEach
//    public void tearDown() {
//        if (driver != null) driver.quit();
//    }
//
//}
