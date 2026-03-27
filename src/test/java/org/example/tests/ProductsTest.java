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
 * Smoke tests for the Swag Labs product listing and detail pages.
 * Covers Requirements 4.1–4.4 and Requirement 7.2.
 */
@Tag("smoke")
public class ProductsTest extends BaseTest {

    @BeforeEach
    void loginAsStandardUser() {
        loginAs(TestUser.STANDARD);
    }

    // Happy paths

    @Test
    @Story("Product Browsing") @Severity(SeverityLevel.CRITICAL)
    @Description("Products page displays at least six products")
    void productsPageDisplaysAtLeastSixProducts() {
        assertThat(productsPage.getProductNames()).hasSizeGreaterThanOrEqualTo(6);
    }

    @Test
    @Story("Product Browsing") @Severity(SeverityLevel.CRITICAL)
    @Description("Selecting a product navigates to the detail page with a matching name")
    void selectingProductNavigatesToDetailPageWithMatchingName() {
        String selectedName = productsPage.getProductNames().get(0);
        productsPage.selectProduct(selectedName);
        assertThat(productDetailPage.getProductName()).isEqualTo(selectedName);
    }

    @Test
    @Story("Product Browsing") @Severity(SeverityLevel.NORMAL)
    @Description("Product detail page shows price in correct $X.XX format and non-blank description")
    void productDetailPageShowsPriceAndDescription() {
        productsPage.selectProduct(productsPage.getProductNames().get(0));
        assertThat(productDetailPage.getPrice()).matches("\\$\\d+\\.\\d{2}");
        assertThat(productDetailPage.getDescription()).isNotBlank();
    }

    @Test
    @Story("Product Browsing") @Severity(SeverityLevel.NORMAL)
    @Description("Adding a product to cart from detail page shows badge count of one")
    void addToCartFromDetailPageShowsBadgeCountOne() {
        productsPage.selectProduct(productsPage.getProductNames().get(0));
        productDetailPage.addToCart();
        productDetailPage.backToProducts();
        assertThat(productsPage.getCartBadgeCount()).isEqualTo(1);
    }

    // Sorting — parameterised over all four sort options

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

    // Boundary — cart badge count: first item, last item, all items

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

    // Edge case — remove from products page clears badge

    @Test
    @Story("Product Browsing") @Severity(SeverityLevel.NORMAL)
    @Description("Removing an item from products page clears badge when cart becomes empty")
    void removingItemFromProductsPageClearsBadge() {
        productsPage.addFirstItemToCart();
        productsPage.removeItemFromCartByIndex(0);
        assertThat(productsPage.getCartBadgeCount()).isEqualTo(0);
    }
}
