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
 * Smoke test suite for the Swag Labs product listing and detail pages.
 *
 * <p>Covers Requirement 4 (Product Browsing Tests) and Requirement 7.2 (Smoke Suite).
 * All tests are tagged {@code @Tag("smoke")} and can be run via
 * {@code mvn verify -Dgroups=smoke}.</p>
 *
 * <p>All page interactions use the {@code productsPage} and {@code productDetailPage}
 * fields inherited from {@link BaseTest} — no inline page object instantiation (DRY).</p>
 */
@Tag("smoke")
public class ProductsTest extends BaseTest {

    /**
     * Logs in as the standard user before each test so the products page is accessible.
     */
    @BeforeEach
    void loginAsStandardUser() {
        loginAs(TestUser.STANDARD);
    }

    /**
     * Verifies that the products page displays at least six products after login.
     */
    @Test
    @Story("Product Browsing")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Products page displays at least six products")
    void productsPageDisplaysAtLeastSixProducts() {
        assertThat(productsPage.getProductNames()).hasSizeGreaterThanOrEqualTo(6);
    }

    /**
     * Verifies that clicking a product name navigates to the detail page
     * and the detail page shows the same product name.
     */
    @Test
    @Story("Product Browsing")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Selecting a product navigates to the detail page with a matching name")
    void selectingProductNavigatesToDetailPageWithMatchingName() {
        String selectedName = productsPage.getProductNames().get(0);
        productsPage.selectProduct(selectedName);

        assertThat(productDetailPage.getProductName()).isEqualTo(selectedName);
    }

    /**
     * Verifies that the product detail page shows a price in the expected {@code $X.XX} format.
     */
    @Test
    @Story("Product Browsing")
    @Severity(SeverityLevel.NORMAL)
    @Description("Product detail page shows price in correct format")
    void productDetailPageShowsPriceInCorrectFormat() {
        productsPage.selectProduct(productsPage.getProductNames().get(0));

        assertThat(productDetailPage.getPrice()).matches("\\$\\d+\\.\\d{2}");
    }

    /**
     * Verifies that adding a product to the cart from the detail page increments
     * the cart badge count to 1 after navigating back to the products page.
     */
    @Test
    @Story("Product Browsing")
    @Severity(SeverityLevel.NORMAL)
    @Description("Adding a product to cart from detail page shows badge count of one")
    void addToCartFromDetailPageShowsBadgeCountOne() {
        productsPage.selectProduct(productsPage.getProductNames().get(0));
        productDetailPage.addToCart();
        productDetailPage.backToProducts();

        assertThat(productsPage.getCartBadgeCount()).isEqualTo(1);
    }
}
