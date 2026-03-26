package org.example.pages;

import com.codeborne.selenide.Condition;
import org.example.config.BrowserConfig;

import java.util.List;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

/**
 * Page Object representing the Swag Labs shopping cart page ({@code /cart.html}).
 *
 * <p>Encapsulates all selectors and interactions for the cart page (SRP).
 * Cart badge visibility is delegated to {@link BrowserConfig#isCartBadgeVisible()}
 * to avoid duplicating the exists-and-visible guard (DRY).</p>
 */
public class CartPage {

    /** CSS selector for the name of each item in the cart. */
    private static final String CART_ITEM_NAME = ".cart_item .inventory_item_name";

    /** CSS selector for each cart item row container. */
    private static final String CART_ITEM_ROW = ".cart_item";

    /** Attribute selector prefix for remove buttons (data-test starts with "remove"). */
    private static final String REMOVE_BUTTON = "[data-test^='remove']";

    /** Data-test selector for the "Checkout" button. */
    private static final String CHECKOUT_BUTTON = "[data-test='checkout']";

    /** Data-test selector for the "Continue Shopping" button. */
    private static final String CONTINUE_SHOPPING_BUTTON = "[data-test='continue-shopping']";

    /**
     * Returns the names of all items currently in the cart.
     *
     * <p>Uses Selenide's {@code $$().texts()} which collects the visible text
     * of all matching elements.</p>
     *
     * @return an ordered list of cart item name strings
     */
    public List<String> getCartItemNames() {
        return $$(CART_ITEM_NAME).texts();
    }

    /**
     * Removes the cart item whose name matches the given string.
     *
     * <p>Finds the cart row containing the product name, then clicks the remove
     * button within that row. Using {@link Condition#text} scoped to the row
     * avoids clicking the wrong remove button when multiple items are in the cart.</p>
     *
     * @param name the exact product name of the item to remove
     */
    public void removeItem(String name) {
        $$(CART_ITEM_ROW)
                .findBy(Condition.text(name))
                .$(REMOVE_BUTTON)
                .click();
    }

    /**
     * Returns whether the cart badge in the page header is currently visible.
     *
     * <p>Delegates to {@link BrowserConfig#isCartBadgeVisible()} (DRY — shared
     * with {@link ProductsPage}).</p>
     *
     * @return {@code true} if the badge is visible (cart has items), {@code false} otherwise
     */
    public boolean isCartBadgeVisible() {
        return BrowserConfig.isCartBadgeVisible();
    }

    /**
     * Clicks the "Checkout" button to begin the checkout flow.
     *
     * <p>Navigates to the checkout step-one page ({@code /checkout-step-one.html}).</p>
     */
    public void proceedToCheckout() {
        $(CHECKOUT_BUTTON).click();
    }

    /**
     * Clicks the "Continue Shopping" button to return to the products page.
     */
    public void continueShopping() {
        $(CONTINUE_SHOPPING_BUTTON).click();
    }
}
