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
 * Regression tests for the Swag Labs shopping cart.
 * Covers Requirements 5.1–5.4 and Requirement 8.1.
 */
@Tag("regression")
public class CartTest extends BaseTest {

    @BeforeEach
    void loginAsStandardUser() {
        loginAs(TestUser.STANDARD);
    }

    // Happy paths

    @Test
    @Story("Cart") @Severity(SeverityLevel.CRITICAL)
    @Description("Single added product appears in cart")
    void singleAddedProductAppearsInCart() {
        String productName = productsPage.getProductNames().get(0);
        productsPage.addFirstItemToCart();
        productsPage.goToCart();
        assertThat(cartPage.getCartItemNames()).contains(productName);
    }

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

    @Test
    @Story("Cart") @Severity(SeverityLevel.NORMAL)
    @Description("Continue shopping navigates back to products page")
    void continueShoppingNavigatesBackToProductsPage() {
        productsPage.addFirstItemToCart();
        productsPage.goToCart();
        cartPage.continueShopping();
        assertThat(WebDriverRunner.url()).contains("/inventory.html");
    }

    // Boundary — parameterised item counts (lower and upper boundary)

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

    // Edge cases

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
