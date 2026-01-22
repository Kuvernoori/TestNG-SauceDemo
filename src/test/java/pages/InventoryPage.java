package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class InventoryPage extends BasePage {

    private final By title = By.className("title");
    private final By firstAddToCart = By.xpath("(//button[contains(text(),'Add to cart')])[1]");
    private final By cartBadge = By.className("shopping_cart_badge");

    public InventoryPage(WebDriver driver) {
        super(driver);
    }

    public boolean isInventoryPage() {
        return getText(title).equals("Products");
    }

    public void addFirstProductToCart() {
        click(firstAddToCart);
    }

    public String getCartCount() {
        return isDisplayed(cartBadge) ? getText(cartBadge) : "0";
    }
}