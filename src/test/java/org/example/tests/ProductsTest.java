package org.example.tests;

import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.example.BaseTest;
import org.example.model.TestUser;
import org.example.util.TestDataProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Smoke test suite for the Swag Labs product listing and detail pages.
 *
 * <p>Covers Requirements 4.1–4.4 (product browsing) and Requirement 7.2
 * (smoke suite must verify the products page). All tests are tagged
 * {@code @Tag("smoke")} and can be run independently via
 * {@code mvn verify -Dgroups=smoke}.</p>
 *
 * <p>A {@code @BeforeEach} logs in as {@link TestUser#STANDARD} so every test
 * starts on the inventory page. Sort and badge tests rely on the clean state
 * provided by {@link BaseTest#setUp()} opening a fresh browser session.</p>
 */
@Tag("smoke")
public class ProductsTest extends BaseTest {

    /**
     * Logs in as the standard user before each test so the inventory page is accessible.
     * Called after {@link BaseTest#setUp()} which opens the base URL (login page).
     */
    @BeforeEach
    void loginAsStandardUser() {
        loginAs(TestUser.STANDARD);
    }

    // -------------------------------------------------------------------------
    // Happy paths
    // -------------------------------------------------------------------------

    /**
     * Verifies that the inventory page displays at least six products after login.
     *
     * <p>SauceDemo has exactly six products in its catalogue. This test uses
     * {@code hasSizeGreaterThanOrEqualTo(6)} rather than {@code isEqualTo(6)} to
     * remain valid if the catalogue is ever expanded.</p>
     */
    @Test
    @Story("Product Browsing") @Severity(SeverityLevel.CRITICAL)
    @Description("Products page displays at least six products")
    void productsPageDisplaysAtLeastSixProducts() {
        assertThat(productsPage.getProductNames()).hasSizeGreaterThanOrEqualTo(6);
    }

    /**
     * Verifies that clicking a product name navigates to its detail page and
     * the detail page shows the same name that was clicked.
     *
     * <p>Reads the first product name from the listing, selects it, then asserts
     * the detail page heading matches. This confirms both navigation and data
     * consistency between the listing and detail views.</p>
     */
    @Test
    @Story("Product Browsing") @Severity(SeverityLevel.CRITICAL)
    @Description("Selecting a product navigates to the detail page with a matching name")
    void selectingProductNavigatesToDetailPageWithMatchingName() {
        String selectedName = productsPage.getProductNames().get(0);
        productsPage.selectProduct(selectedName);
        assertThat(productDetailPage.getProductName()).isEqualTo(selectedName);
    }

    /**
     * Verifies that the product detail page shows a correctly formatted price and
     * a non-blank description.
     *
     * <p>The price regex {@code \$\d+\.\d{2}} matches values like {@code $9.99}
     * or {@code $49.99} — a dollar sign, one or more digits, a decimal point,
     * and exactly two decimal digits.</p>
     */
    @Test
    @Story("Product Browsing") @Severity(SeverityLevel.NORMAL)
    @Description("Product detail page shows price in correct $X.XX format and non-blank description")
    void productDetailPageShowsPriceAndDescription() {
        productsPage.selectProduct(productsPage.getProductNames().get(0));
        assertThat(productDetailPage.getPrice()).matches("\\$\\d+\\.\\d{2}");
        assertThat(productDetailPage.getDescription()).isNotBlank();
    }

    /**
     * Verifies that adding a product to the cart from the detail page increments
     * the cart badge count to exactly 1 after navigating back to the listing.
     *
     * <p>The badge count is read from the inventory page (not the detail page)
     * because the badge is part of the shared header and is visible on both pages.
     * Navigating back first ensures the count is read in a stable state.</p>
     */
    @Test
    @Story("Product Browsing") @Severity(SeverityLevel.NORMAL)
    @Description("Adding a product to cart from detail page shows badge count of one")
    void addToCartFromDetailPageShowsBadgeCountOne() {
        productsPage.selectProduct(productsPage.getProductNames().get(0));
        productDetailPage.addToCart();
        productDetailPage.backToProducts();
        assertThat(productsPage.getCartBadgeCount()).isEqualTo(1);
    }

    // -------------------------------------------------------------------------
    // Sorting — parameterised over all four sort options
    // -------------------------------------------------------------------------

    /**
     * Verifies that the A-Z and Z-A name sort options reorder the product list correctly.
     *
     * <p>This is a {@code @ParameterizedTest} driven by
     * {@link TestDataProvider#nameSortOptions()}, which returns a
     * {@code Stream<Arguments>} with two rows: {@code ("az", true)} and
     * {@code ("za", false)}. JUnit 5 runs this method once per row.</p>
     *
     * <p>The assertion strategy: after applying the sort, read the actual list from
     * the UI, create a copy, sort the copy programmatically using the same comparator
     * the UI should use, then assert the actual list equals the expected sorted copy.
     * This avoids hardcoding product names in the test.</p>
     *
     * @param sortValue the dropdown option value to select ({@code "az"} or {@code "za"})
     * @param ascending {@code true} if the expected order is A→Z, {@code false} for Z→A
     */
    @ParameterizedTest(name = "sort=''{0}'' -> ascending={1}")
    @MethodSource("org.example.util.TestDataProvider#nameSortOptions")
    @Story("Product Browsing") @Severity(SeverityLevel.NORMAL)
    @Description("Name sort options produce correct alphabetical ordering")
    void nameSortOptionsProduceCorrectOrder(String sortValue, boolean ascending) {
        productsPage.sortBy(sortValue);
        List<String> actual = productsPage.getProductNames();
        List<String> expected = new ArrayList<>(actual);
        if (ascending) {
            expected.sort(String.CASE_INSENSITIVE_ORDER);
        } else {
            expected.sort((a, b) -> String.CASE_INSENSITIVE_ORDER.compare(b, a));
        }
        assertThat(actual).containsExactlyElementsOf(expected);
    }

    /**
     * Verifies that the low-to-high and high-to-low price sort options reorder
     * the product list correctly.
     *
     * <p>This is a {@code @ParameterizedTest} driven by
     * {@link TestDataProvider#priceSortOptions()}, which returns a
     * {@code Stream<Arguments>} with two rows: {@code ("lohi", true)} and
     * {@code ("hilo", false)}.</p>
     *
     * <p>Prices are read as {@code double} values (the {@code $} prefix is stripped
     * by {@link org.example.pages.ProductsPage#getProductPrices()}), then compared
     * numerically rather than lexicographically to avoid ordering issues like
     * {@code $9.99} appearing after {@code $49.99} in a string sort.</p>
     *
     * @param sortValue the dropdown option value to select ({@code "lohi"} or {@code "hilo"})
     * @param ascending {@code true} if the expected order is low→high, {@code false} for high→low
     */
    @ParameterizedTest(name = "sort=''{0}'' -> ascending={1}")
    @MethodSource("org.example.util.TestDataProvider#priceSortOptions")
    @Story("Product Browsing") @Severity(SeverityLevel.NORMAL)
    @Description("Price sort options produce correct numeric ordering")
    void priceSortOptionsProduceCorrectOrder(String sortValue, boolean ascending) {
        productsPage.sortBy(sortValue);
        List<Double> actual = productsPage.getProductPrices();
        List<Double> expected = new ArrayList<>(actual);
        if (ascending) {
            expected.sort(Double::compareTo);
        } else {
            expected.sort((a, b) -> Double.compare(b, a));
        }
        assertThat(actual).containsExactlyElementsOf(expected);
    }

    // -------------------------------------------------------------------------
    // Boundary — cart badge count
    // -------------------------------------------------------------------------

    /**
     * Verifies that the cart badge count matches the number of items added,
     * testing the lower boundary (1 item) and upper boundary (6 items = full catalogue).
     *
     * <p>This is a {@code @ParameterizedTest} using {@code @ValueSource(ints = {1, 6})}
     * — an inline data source used here because the values are simple integers that
     * do not need to live in {@link TestDataProvider}.</p>
     *
     * <p>The {@code Math.min(itemCount, names.size())} guard ensures the test does
     * not fail if the catalogue ever has fewer than 6 products.</p>
     *
     * @param itemCount the number of products to add to the cart
     */
    @ParameterizedTest(name = "add {0} item(s) -> badge={0}")
    @ValueSource(ints = {1, 6})
    @Story("Product Browsing") @Severity(SeverityLevel.NORMAL)
    @Description("Cart badge count matches items added (boundary: 1 and 6)")
    void cartBadgeCountMatchesItemsAdded(int itemCount) {
        List<String> names = productsPage.getProductNames();
        for (int i = 0; i < itemCount && i < names.size(); i++) {
            productsPage.addItemToCartByIndex(i);
        }
        assertThat(productsPage.getCartBadgeCount()).isEqualTo(Math.min(itemCount, names.size()));
    }

    // -------------------------------------------------------------------------
    // Edge case
    // -------------------------------------------------------------------------

    /**
     * Verifies that removing an item directly from the inventory page (via the
     * "Remove" button that replaces "Add to cart") clears the cart badge.
     *
     * <p>This tests the remove-from-listing flow, which is distinct from removing
     * an item from the cart page. The badge should return to 0 (hidden) after
     * the only item is removed.</p>
     */
    @Test
    @Story("Product Browsing") @Severity(SeverityLevel.NORMAL)
    @Description("Removing an item from products page clears badge when cart becomes empty")
    void removingItemFromProductsPageClearsBadge() {
        productsPage.addFirstItemToCart();
        productsPage.removeItemFromCartByIndex(0);
        assertThat(productsPage.getCartBadgeCount()).isEqualTo(0);
    }
}
