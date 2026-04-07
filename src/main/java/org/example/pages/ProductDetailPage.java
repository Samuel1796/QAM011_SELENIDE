package org.example.pages;

import static com.codeborne.selenide.Selenide.$;

/**
 * Page Object representing the Swag Labs product detail view at
 * {@code /inventory-item.html}.
 *
 * <p>Encapsulates all selectors and interactions for a single product's detail page.
 * Tests navigate here by calling {@link ProductsPage#selectProduct(String)} and
 * return to the listing via {@link #backToProducts()}.</p>
 */
public class ProductDetailPage {

    /**
     * Returns the product name as displayed in the detail view heading.
     *
     * <p>Used in tests to verify that the detail page shows the same name that was
     * clicked on the inventory listing page.</p>
     *
     * @return the product name string
     */
    public String getProductName() {
        return $(".inventory_details_name").getText();
    }

    /**
     * Returns the product price string as displayed on the detail page.
     *
     * <p>The value includes the {@code $} prefix, e.g. {@code "$29.99"}.
     * Tests assert this matches the regex {@code \$\d+\.\d{2}} to verify
     * the price format is correct.</p>
     *
     * @return the price string including the currency symbol
     */
    public String getPrice() {
        return $(".inventory_details_price").getText();
    }

    /**
     * Returns the product description text shown below the product name.
     *
     * <p>Tests assert this is non-blank to verify the description is populated
     * for every product in the catalogue.</p>
     *
     * @return the product description string
     */
    public String getDescription() {
        return $(".inventory_details_desc").getText();
    }

    /**
     * Clicks the "Add to cart" button on the product detail page.
     *
     * <p>After clicking, the button label changes to "Remove" and the cart badge
     * count in the header increments by one. Tests navigate back to the inventory
     * page via {@link #backToProducts()} to assert the badge count.</p>
     */
    public void addToCart() {
        $("[data-test='add-to-cart']").click();
    }

    /**
     * Clicks the "Back to products" link to return to the inventory listing page.
     *
     * <p>Navigates back to {@code /inventory.html} without using the browser's
     * back button, keeping the test flow explicit and predictable.</p>
     */
    public void backToProducts() {
        $("#back-to-products").click();
    }
}
