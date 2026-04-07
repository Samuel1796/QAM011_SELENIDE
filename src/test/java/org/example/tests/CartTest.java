package org.example.tests;

import com.codeborne.selenide.WebDriverRunner;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Regression test suite for the Swag Labs shopping cart functionality.
 *
 * <p>Covers Requirements 5.1–5.4 (cart operations) and Requirement 8.1
 * (regression suite must cover all cart acceptance criteria). All tests are
 * tagged {@code @Tag("regression")} and can be run independently via
 * {@code mvn verify -Dgroups=regression}.</p>
 *
 * <p>A {@code @BeforeEach} logs in as {@link TestUser#STANDARD} so every test
 * starts on the inventory page with an empty cart. Each test is responsible for
 * adding the items it needs.</p>
 */
@Tag("regression")
public class CartTest extends BaseTest {

    /**
     * Logs in as the standard user before each test.
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
     * Verifies that a single product added from the inventory page appears in the cart.
     *
     * <p>Reads the first product name before adding it so the assertion can compare
     * against the exact string shown in the UI, rather than a hardcoded value.</p>
     */
    @Test
    @Story("Cart") @Severity(SeverityLevel.CRITICAL)
    @Description("Single added product appears in cart")
    void singleAddedProductAppearsInCart() {
        String productName = productsPage.getProductNames().get(0);
        productsPage.addFirstItemToCart();
        productsPage.goToCart();
        assertThat(cartPage.getCartItemNames()).contains(productName);
    }

    /**
     * Verifies that all products added from the inventory page appear in the cart.
     *
     * <p>Adds two products by name (using {@link org.example.pages.ProductsPage#addItemToCartByName(String)})
     * to ensure the correct items are targeted, then asserts both names are present
     * in the cart item list.</p>
     */
    @Test
    @Story("Cart") @Severity(SeverityLevel.CRITICAL)
    @Description("Multiple added products all appear in cart")
    void multipleAddedProductsAllAppearInCart() {
        String first  = productsPage.getProductNames().get(0);
        String second = productsPage.getProductNames().get(1);
        productsPage.addItemToCartByName(first);
        productsPage.addItemToCartByName(second);
        productsPage.goToCart();
        assertThat(cartPage.getCartItemNames()).contains(first, second);
    }

    /**
     * Verifies that removing an item from the cart causes it to disappear from
     * the cart item list.
     *
     * <p>Uses {@link org.example.pages.CartPage#removeItem(String)} which scopes
     * the remove button click to the specific cart row, preventing accidental
     * removal of the wrong item when multiple items are present.</p>
     */
    @Test
    @Story("Cart") @Severity(SeverityLevel.NORMAL)
    @Description("Removed item no longer in cart list")
    void removedItemNoLongerInCartList() {
        String productName = productsPage.getProductNames().get(0);
        productsPage.addFirstItemToCart();
        productsPage.goToCart();
        cartPage.removeItem(productName);
        assertThat(cartPage.getCartItemNames()).doesNotContain(productName);
    }

    /**
     * Verifies that removing the last item from the cart hides the cart badge.
     *
     * <p>The badge element is only present in the DOM when the cart has items.
     * After removing the only item, {@link org.example.pages.CartPage#isCartBadgeVisible()}
     * should return {@code false}.</p>
     */
    @Test
    @Story("Cart") @Severity(SeverityLevel.NORMAL)
    @Description("Removing all items hides cart badge")
    void removingAllItemsHidesCartBadge() {
        String productName = productsPage.getProductNames().get(0);
        productsPage.addFirstItemToCart();
        productsPage.goToCart();
        cartPage.removeItem(productName);
        assertThat(cartPage.isCartBadgeVisible()).isFalse();
    }

    /**
     * Verifies that clicking "Continue Shopping" from the cart page navigates
     * back to the inventory listing without clearing the cart.
     *
     * <p>Asserts on the URL rather than a page element to keep the assertion
     * lightweight — the URL change is the definitive signal of navigation.</p>
     */
    @Test
    @Story("Cart") @Severity(SeverityLevel.NORMAL)
    @Description("Continue shopping navigates back to products page")
    void continueShoppingNavigatesBackToProductsPage() {
        productsPage.addFirstItemToCart();
        productsPage.goToCart();
        cartPage.continueShopping();
        assertThat(WebDriverRunner.url()).contains("/inventory.html");
    }

    // -------------------------------------------------------------------------
    // Boundary — parameterised item counts
    // -------------------------------------------------------------------------

    /**
     * Verifies that the cart item count matches the number of products added,
     * across four boundary values: 1 (lower), 2 and 3 (mid-range), 6 (upper/full catalogue).
     *
     * <p>This is a {@code @ParameterizedTest} driven by
     * {@link TestDataProvider#cartItemCounts()}, which returns a
     * {@code Stream<Integer>} with four values. JUnit 5 runs this method once per value.</p>
     *
     * <p>The {@code Math.min(count, names.size())} guard caps the add loop at the
     * actual catalogue size, making the test resilient to catalogue changes.</p>
     *
     * @param count the number of products to add to the cart
     */
    @ParameterizedTest(name = "add {0} item(s) -> cart has {0} item(s)")
    @MethodSource("org.example.util.TestDataProvider#cartItemCounts")
    @Story("Cart") @Severity(SeverityLevel.NORMAL)
    @Description("Cart item count matches the number of products added (boundary: 1, 2, 3, 6)")
    void cartItemCountMatchesNumberOfProductsAdded(int count) {
        List<String> names = productsPage.getProductNames();
        int toAdd = Math.min(count, names.size());
        for (int i = 0; i < toAdd; i++) {
            productsPage.addItemToCartByIndex(i);
        }
        productsPage.goToCart();
        assertThat(cartPage.getCartItemCount()).isEqualTo(toAdd);
    }

    // -------------------------------------------------------------------------
    // Edge case
    // -------------------------------------------------------------------------

    /**
     * Verifies that removing a middle item from a multi-item cart leaves the
     * surrounding items intact.
     *
     * <p>Adds three products (indices 0, 1, 2), removes the middle one (index 1),
     * then asserts the remaining list contains the first and third but not the second.
     * This tests that the remove operation is correctly scoped to the target item
     * and does not affect adjacent items.</p>
     */
    @Test
    @Story("Cart") @Severity(SeverityLevel.NORMAL)
    @Description("Removing a middle item from a multi-item cart leaves the others intact")
    void removingMiddleItemLeavesOthersIntact() {
        List<String> names = productsPage.getProductNames();
        String first  = names.get(0);
        String second = names.get(1);
        String third  = names.get(2);

        productsPage.addItemToCartByName(first);
        productsPage.addItemToCartByName(second);
        productsPage.addItemToCartByName(third);
        productsPage.goToCart();

        cartPage.removeItem(second);

        List<String> remaining = cartPage.getCartItemNames();
        assertThat(remaining).contains(first, third);
        assertThat(remaining).doesNotContain(second);
    }
}
