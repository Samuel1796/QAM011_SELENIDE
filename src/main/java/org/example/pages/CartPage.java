package org.example.pages;

import com.codeborne.selenide.Condition;
import org.example.config.BrowserConfig;

import java.util.List;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

/**
 * Page Object representing the Swag Labs shopping cart page at {@code /cart.html}.
 *
 * <p>Encapsulates all selectors and interactions for the cart page. Cart badge
 * visibility is delegated to {@link BrowserConfig#isCartBadgeVisible()} so the
 * same guard logic is shared with {@link ProductsPage} without duplication (DRY).</p>
 */
public class CartPage {

    private static final String CART_CONTENTS = ".cart_contents_container";

    /**
     * CSS selector for the name label of each item currently in the cart.
     * Scoped inside {@code .cart_item} to avoid matching product names elsewhere.
     */
    private static final String CART_ITEM_NAME = ".cart_item .inventory_item_name";

    /** CSS selector for the entire row container of each cart item. */
    private static final String CART_ITEM_ROW = ".cart_item";

    /**
     * Attribute selector for the "Remove" button inside a cart item row.
     * Uses {@code data-test^='remove'} (starts-with) to match all product-specific
     * remove button variants.
     */
    private static final String REMOVE_BUTTON = "[data-test^='remove']";

    /** Attribute selector for the "Checkout" button at the bottom of the cart page. */
    private static final String CHECKOUT_BUTTON = "[data-test='checkout']";

    /** Attribute selector for the "Continue Shopping" button on the cart page. */
    private static final String CONTINUE_SHOPPING_BUTTON = "[data-test='continue-shopping']";

    /**
     * Returns the display names of all items currently in the cart.
     *
     * <p>Uses Selenide's {@code $$().texts()} to collect the visible text of every
     * matching element. Returns an empty list when the cart is empty.</p>
     *
     * @return an ordered list of cart item name strings
     */
    public List<String> getCartItemNames() {
        return $$(CART_ITEM_NAME).texts();
    }

    /**
     * Returns the total number of items currently in the cart.
     *
     * <p>Delegates to {@link #getCartItemNames()} and returns its size, so the
     * count is always consistent with the visible item list.</p>
     *
     * @return the number of distinct items in the cart
     */
    public int getCartItemCount() {
        return getCartItemNames().size();
    }

    /**
     * Removes the cart item whose name matches the given string.
     *
     * <p>Locates the cart row that contains the product name, then clicks the
     * "Remove" button scoped to that row. Scoping to the row prevents clicking
     * the wrong remove button when multiple items are in the cart.</p>
     *
     * @param name the exact display name of the product to remove
     */
    public void removeItem(String name) {
        $$(CART_ITEM_ROW).findBy(Condition.text(name)).$(REMOVE_BUTTON)
                .shouldBe(Condition.visible, Condition.enabled)
                .click();
    }

    /**
     * Returns whether the cart badge in the page header is currently visible.
     *
     * <p>Delegates to {@link BrowserConfig#isCartBadgeVisible()} to avoid
     * duplicating the exists-and-visible guard that is also used by
     * {@link ProductsPage#getCartBadgeCount()}.</p>
     *
     * @return {@code true} if the badge is visible (cart has items), {@code false} otherwise
     */
    public boolean isCartBadgeVisible() {
        return BrowserConfig.isCartBadgeVisible();
    }

    /**
     * Clicks the "Checkout" button to begin the checkout flow.
     *
     * <p>Navigates to the checkout step-one page at
     * {@code /checkout-step-one.html}.</p>
     */
    public void proceedToCheckout() {
        $(CHECKOUT_BUTTON).shouldBe(Condition.visible, Condition.enabled).click();
    }

    /**
     * Clicks the "Continue Shopping" button to return to the products listing page.
     *
     * <p>Navigates back to {@code /inventory.html} without clearing the cart,
     * allowing tests to add more items before proceeding to checkout.</p>
     */
    public void continueShopping() {
        $(CONTINUE_SHOPPING_BUTTON).shouldBe(Condition.visible, Condition.enabled).click();
    }

    public boolean isLoaded() {
        $(CART_CONTENTS).shouldBe(Condition.visible);
        return true;
    }
}
