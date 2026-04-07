package org.example.pages;

import com.codeborne.selenide.Condition;
import org.example.config.BrowserConfig;

import java.util.List;
import java.util.stream.Collectors;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

/**
 * Page Object representing the Swag Labs product listing page at {@code /inventory.html}.
 *
 * <p>Encapsulates all selectors and interactions for the inventory page. Navigation
 * helpers such as {@link #goToCart()} and add-to-cart methods are provided here so
 * test classes never reference raw CSS selectors directly, keeping tests resilient
 * to UI changes (Page Object Model pattern).</p>
 *
 * <p>Cart badge visibility and the cart link selector are delegated to
 * {@link BrowserConfig} so the same selectors are shared with {@link CartPage}
 * without duplication (DRY).</p>
 */
public class ProductsPage {

    /** CSS selector matching every product name link in the inventory grid. */
    private static final String PRODUCT_NAME = ".inventory_item_name";

    /**
     * Attribute selector matching all "Add to cart" buttons.
     * Uses {@code data-test^='add-to-cart'} (starts-with) to match all variants
     * regardless of the product-specific suffix in the attribute value.
     */
    private static final String ADD_TO_CART_BUTTONS = "[data-test^='add-to-cart']";

    /**
     * Attribute selector matching all "Remove" buttons on the inventory page.
     * These appear after a product has been added to the cart.
     */
    private static final String REMOVE_FROM_CART_BUTTONS = "[data-test^='remove']";

    /** Attribute selector for the product sort dropdown control. */
    private static final String SORT_DROPDOWN = "[data-test='product-sort-container']";

    /** CSS selector matching the price label for each product in the inventory grid. */
    private static final String PRODUCT_PRICE = ".inventory_item_price";

    /** CSS selector matching the entire row container for each product card. */
    private static final String PRODUCT_ROW = ".inventory_item";

    /** CSS selector for the hamburger (side-nav) menu button in the page header. */
    private static final String BURGER_MENU_BUTTON = "#react-burger-menu-btn";

    /** Attribute selector for the logout link inside the side navigation menu. */
    private static final String LOGOUT_LINK = "[data-test='logout-sidebar-link']";

    /**
     * Returns the display names of all products currently visible on the inventory page.
     *
     * <p>Uses Selenide's {@code $$().texts()} which collects the text of every
     * matching element in DOM order, waiting for at least one element to be present.</p>
     *
     * @return an ordered list of product name strings as shown in the UI
     */
    public List<String> getProductNames() {
        return $$(PRODUCT_NAME).texts();
    }

    /**
     * Returns all product prices as parsed numeric values.
     *
     * <p>Strips the leading {@code $} character from each price label and parses
     * the remainder as a {@code double}. The order matches the current display order,
     * which changes when a sort option is applied.</p>
     *
     * @return an ordered list of product prices as {@code double} values
     */
    public List<Double> getProductPrices() {
        return $$(PRODUCT_PRICE).texts().stream()
                .map(t -> t.replace("$", "").trim())
                .map(Double::parseDouble)
                .collect(Collectors.toList());
    }

    /**
     * Selects a sort option from the product sort dropdown.
     *
     * <p>Supported option values on SauceDemo:
     * <ul>
     *   <li>{@code "az"} — Name (A to Z)</li>
     *   <li>{@code "za"} — Name (Z to A)</li>
     *   <li>{@code "lohi"} — Price (low to high)</li>
     *   <li>{@code "hilo"} — Price (high to low)</li>
     * </ul>
     * </p>
     *
     * @param sortValue the {@code value} attribute of the {@code <option>} to select
     */
    public void sortBy(String sortValue) {
        $(SORT_DROPDOWN).selectOptionByValue(sortValue);
    }

    /**
     * Clicks the product name link that exactly matches the given name, navigating
     * to that product's detail page.
     *
     * <p>{@link Condition#exactText} is used rather than a partial match to avoid
     * false positives when product names share common substrings (e.g.
     * "Sauce Labs Backpack" vs "Sauce Labs Fleece Jacket").</p>
     *
     * @param name the exact product name as displayed in the inventory grid
     */
    public void selectProduct(String name) {
        $$(PRODUCT_NAME).findBy(Condition.exactText(name)).click();
    }

    /**
     * Returns the current cart item count shown in the header badge.
     *
     * <p>Parses the badge text as an integer. Returns {@code 0} when the badge is
     * absent or hidden, which is the correct state for an empty cart.</p>
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
     * Clicks the shopping cart icon in the page header to navigate to the cart page.
     *
     * <p>The cart link selector is sourced from {@link BrowserConfig} so it is
     * shared with other page objects without duplication.</p>
     */
    public void goToCart() {
        $(BrowserConfig.CART_LINK_SELECTOR).click();
    }

    /**
     * Clicks the first "Add to cart" button in the inventory grid.
     *
     * <p>Convenience method for test setup steps that need to add one item quickly
     * without caring which specific product is added.</p>
     */
    public void addFirstItemToCart() {
        $$(ADD_TO_CART_BUTTONS).first().click();
    }

    /**
     * Clicks the "Add to cart" button at the given zero-based position in the grid.
     *
     * <p>Used in parameterised tests that add a specific number of products by
     * iterating over indices 0 through N-1.</p>
     *
     * @param index zero-based index of the add-to-cart button to click
     */
    public void addItemToCartByIndex(int index) {
        $$(ADD_TO_CART_BUTTONS).get(index).click();
    }

    /**
     * Adds a product to the cart by locating its row using the product's display name.
     *
     * <p>Scopes the add-to-cart button click to the specific product row to avoid
     * clicking the wrong button when multiple products are visible.</p>
     *
     * @param name the exact display name of the product to add
     */
    public void addItemToCartByName(String name) {
        $$(PRODUCT_ROW).findBy(Condition.text(name)).$(ADD_TO_CART_BUTTONS).click();
    }

    /**
     * Clicks the "Remove" button at the given zero-based position in the grid.
     *
     * <p>The "Remove" button replaces the "Add to cart" button after a product has
     * been added. Used in tests that verify the cart badge clears when an item is
     * removed directly from the inventory page.</p>
     *
     * @param index zero-based index of the remove button to click
     */
    public void removeItemFromCartByIndex(int index) {
        $$(REMOVE_FROM_CART_BUTTONS).get(index).click();
    }

    /**
     * Opens the side navigation menu by clicking the hamburger button.
     *
     * <p>Includes a retry for CI/headless environments where the first click can
     * occasionally be missed before the menu animation completes. Waits for the
     * logout link to be visible before returning, ensuring the menu is fully open.</p>
     */
    public void openBurgerMenu() {
        $(BURGER_MENU_BUTTON).shouldBe(Condition.visible, Condition.enabled).click();
        // Retry once — the side menu animation can cause the first click to be missed
        // in headless/CI environments where rendering is slower.
        if (!$(LOGOUT_LINK).is(Condition.visible)) {
            $(BURGER_MENU_BUTTON).shouldBe(Condition.visible, Condition.enabled).click();
        }
        $(LOGOUT_LINK).shouldBe(Condition.visible);
    }

    /**
     * Clicks the logout link in the side navigation menu.
     *
     * <p>Calls {@link #openBurgerMenu()} first if the menu is not already open,
     * making this method safe to call without a prior {@link #openBurgerMenu()} call.</p>
     */
    public void logoutFromSideMenu() {
        if (!$(LOGOUT_LINK).is(Condition.visible)) {
            openBurgerMenu();
        }
        $(LOGOUT_LINK).shouldBe(Condition.visible, Condition.enabled).click();
    }
}
