package org.example.pages;

import com.codeborne.selenide.Condition;
import org.example.config.BrowserConfig;

import java.util.List;
import java.util.stream.Collectors;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

/**
 * Page Object representing the Swag Labs product listing page ({@code /inventory.html}).
 *
 * <p>Encapsulates all selectors and interactions for the products page (SRP).
 * Navigation helpers such as {@link #goToCart()} and {@link #addFirstItemToCart()}
 * are provided here so test classes never use raw Selenide selectors directly,
 * keeping tests readable and resilient to selector changes (OCP / DRY).</p>
 */
public class ProductsPage {

    /** CSS selector for each product name link in the inventory list. */
    private static final String PRODUCT_NAME = ".inventory_item_name";

    /** CSS selector for add-to-cart buttons (data-test attribute starts with "add-to-cart"). */
    private static final String ADD_TO_CART_BUTTONS = "[data-test^='add-to-cart']";

    /** CSS selector for remove-from-cart buttons (data-test attribute starts with "remove"). */
    private static final String REMOVE_FROM_CART_BUTTONS = "[data-test^='remove']";

    /** Data-test selector for the product sorting dropdown. */
    private static final String SORT_DROPDOWN = "[data-test='product-sort-container']";

    /** CSS selector for product prices in the inventory list. */
    private static final String PRODUCT_PRICE = ".inventory_item_price";

    /** CSS selector for each product row on the inventory page. */
    private static final String PRODUCT_ROW = ".inventory_item";

    /** Data-test selector for the burger menu button in the header. */
    private static final String BURGER_MENU_BUTTON = "#react-burger-menu-btn";

    /** Data-test selector for the logout action in the side menu. */
    private static final String LOGOUT_LINK = "[data-test='logout-sidebar-link']";

    /**
     * Returns the display names of all products currently visible on the page.
     *
     * <p>Uses Selenide's {@code $$().texts()} which waits for at least one element
     * to be present before collecting text values.</p>
     *
     * @return an ordered list of product name strings
     */
    public List<String> getProductNames() {
        return $$(PRODUCT_NAME).texts();
    }

    /**
     * Selects a sort option from the product sort dropdown.
     *
     * <p>Supported values on SauceDemo are {@code az}, {@code za}, {@code lohi}, and {@code hilo}.</p>
     *
     * @param sortValue the option value attribute to select
     */
    public void sortBy(String sortValue) {
        $(SORT_DROPDOWN).selectOptionByValue(sortValue);
    }

    /**
     * Returns all product prices as numeric values.
     *
     * @return ordered list of product prices as doubles
     */
    public List<Double> getProductPrices() {
        return $$(PRODUCT_PRICE).texts().stream()
                .map(text -> text.replace("$", "").trim())
                .map(Double::parseDouble)
                .collect(Collectors.toList());
    }

    /**
     * Clicks the product name link that exactly matches the given name,
     * navigating to that product's detail page.
     *
     * <p>{@link Condition#exactText} is used to avoid partial-match false positives
     * when product names share common substrings.</p>
     *
     * @param name the exact product name to select
     */
    public void selectProduct(String name) {
        $$(PRODUCT_NAME).findBy(Condition.exactText(name)).click();
    }

    /**
     * Returns the current cart item count shown in the header badge.
     *
     * <p>Parses the badge text as an integer. Returns {@code 0} if the badge
     * is absent or hidden (i.e. the cart is empty).</p>
     *
     * @return the number of items in the cart, or {@code 0} if the badge is not visible
     */
    public int getCartBadgeCount() {
        if (BrowserConfig.isCartBadgeVisible()) {
            return Integer.parseInt($(BrowserConfig.CART_BADGE_SELECTOR).getText().trim());
        }
        return 0;
    }

    /**
     * Clicks the shopping cart icon in the header to navigate to the cart page.
     *
     * <p>Centralised here so no test class needs to know the cart link selector (DRY).</p>
     */
    public void goToCart() {
        $(BrowserConfig.CART_LINK_SELECTOR).click();
    }

    /**
     * Clicks the first "Add to cart" button on the products page.
     *
     * <p>Convenience method used in test setup to quickly add one item without
     * needing to know the selector (DRY).</p>
     */
    public void addFirstItemToCart() {
        $$(ADD_TO_CART_BUTTONS).get(0).click();
    }

    /**
     * Clicks the "Add to cart" button at the given zero-based index.
     *
     * <p>Used when tests need to add a specific product by its position in the list.</p>
     *
     * @param index zero-based index of the add-to-cart button to click
     */
    public void addItemToCartByIndex(int index) {
        $$(ADD_TO_CART_BUTTONS).get(index).click();
    }

    /**
     * Adds a product to the cart using its exact display name.
     *
     * @param name exact product name to add
     */
    public void addItemToCartByName(String name) {
        $$(PRODUCT_ROW)
                .findBy(Condition.text(name))
                .$(ADD_TO_CART_BUTTONS)
                .click();
    }

    /**
     * Clicks the remove button at the given zero-based index.
     *
     * @param index zero-based index of the remove button to click
     */
    public void removeItemFromCartByIndex(int index) {
        $$(REMOVE_FROM_CART_BUTTONS).get(index).click();
    }

    /**
     * Removes a product from the cart using its exact display name.
     *
     * @param name exact product name to remove
     */
    public void removeItemFromCartByName(String name) {
        $$(PRODUCT_ROW)
                .findBy(Condition.text(name))
                .$(REMOVE_FROM_CART_BUTTONS)
                .click();
    }

    /**
     * Opens the side navigation menu from the products page.
     */
    public void openBurgerMenu() {
        $(BURGER_MENU_BUTTON).shouldBe(Condition.visible, Condition.enabled).click();

        // SauceDemo side menu can occasionally miss the first click in CI/headless runs.
        if (!$(LOGOUT_LINK).is(Condition.visible)) {
            $(BURGER_MENU_BUTTON).shouldBe(Condition.visible, Condition.enabled).click();
        }

        $(LOGOUT_LINK).shouldBe(Condition.visible);
    }

    /**
     * Clicks the logout action in the side menu.
     */
    public void logoutFromSideMenu() {
        if (!$(LOGOUT_LINK).is(Condition.visible)) {
            openBurgerMenu();
        }

        $(LOGOUT_LINK).shouldBe(Condition.visible, Condition.enabled).click();
    }
}
