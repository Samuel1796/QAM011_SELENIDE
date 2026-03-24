package org.example.tests;

import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.example.BaseTest;
import org.example.model.TestUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Regression test suite for the Swag Labs shopping cart functionality.
 *
 * <p>Covers Requirement 5 (Cart Functionality Tests) and Requirement 8.1 (Regression Suite).
 * All tests are tagged {@code @Tag("regression")} and can be run via
 * {@code mvn verify -Dgroups=regression}.</p>
 *
 * <p>All page interactions use the {@code productsPage} and {@code cartPage} fields
 * inherited from {@link BaseTest} — no inline page object instantiation (DRY).</p>
 */
@Tag("regression")
public class CartTest extends BaseTest {

    /**
     * Logs in as the standard user before each test.
     */
    @BeforeEach
    void loginAsStandardUser() {
        loginAs(TestUser.STANDARD);
    }

    /**
     * Verifies that a single product added from the products page appears in the cart.
     */
    @Test
    @Story("Cart")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Single added product appears in cart")
    void singleAddedProductAppearsInCart() {
        String productName = productsPage.getProductNames().get(0);
        productsPage.addFirstItemToCart();
        productsPage.goToCart();

        assertThat(cartPage.getCartItemNames()).contains(productName);
    }

    /**
     * Verifies that all products added from the products page appear in the cart.
     */
    @Test
    @Story("Cart")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Multiple added products all appear in cart")
    void multipleAddedProductsAllAppearInCart() {
        String firstName  = productsPage.getProductNames().get(0);
        String secondName = productsPage.getProductNames().get(1);

        productsPage.addItemToCartByIndex(0);
        productsPage.addItemToCartByIndex(1);
        productsPage.goToCart();

        assertThat(cartPage.getCartItemNames()).contains(firstName, secondName);
    }

    /**
     * Verifies that removing an item from the cart causes it to no longer appear
     * in the cart item list.
     */
    @Test
    @Story("Cart")
    @Severity(SeverityLevel.NORMAL)
    @Description("Removed item no longer in cart list")
    void removedItemNoLongerInCartList() {
        String productName = productsPage.getProductNames().get(0);
        productsPage.addFirstItemToCart();
        productsPage.goToCart();

        cartPage.removeItem(productName);

        assertThat(cartPage.getCartItemNames()).doesNotContain(productName);
    }

    /**
     * Verifies that removing all items from the cart hides the cart badge in the header.
     */
    @Test
    @Story("Cart")
    @Severity(SeverityLevel.NORMAL)
    @Description("Removing all items hides cart badge")
    void removingAllItemsHidesCartBadge() {
        String productName = productsPage.getProductNames().get(0);
        productsPage.addFirstItemToCart();
        productsPage.goToCart();

        cartPage.removeItem(productName);

        assertThat(cartPage.isCartBadgeVisible()).isFalse();
    }
}
