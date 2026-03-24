package org.example.tests;

import com.codeborne.selenide.Condition;
import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.example.BaseTest;
import org.example.model.ShippingInfo;
import org.example.model.TestUser;
import org.example.pages.CartPage;
import org.example.pages.CheckoutPage;
import org.example.pages.ProductsPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static com.codeborne.selenide.Selenide.$;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Regression test suite for the Swag Labs checkout flow.
 *
 * <p>Covers Requirement 6 (Checkout Flow Tests) and Requirement 8.2 (Regression Suite).
 * All tests are tagged {@code @Tag("regression")} and can be run via
 * {@code mvn verify -Dgroups=regression}.</p>
 *
 * <p>Login and cart setup are performed in {@link #setUpCartWithOneItem()} using
 * {@link BaseTest#loginAs(TestUser)} and {@link ProductsPage} helpers — no raw
 * Selenide selectors in test methods (DRY / OCP).</p>
 *
 * <p>A reusable {@link ShippingInfo} constant {@link #VALID_SHIPPING} is defined
 * once and shared across tests that need valid shipping data (DRY).</p>
 */
@Tag("regression")
public class CheckoutTest extends BaseTest {

    /**
     * Reusable valid shipping info used across multiple tests.
     * Defined as a constant to avoid repeating the same literal values (DRY).
     */
    private static final ShippingInfo VALID_SHIPPING = new ShippingInfo("John", "Doe", "12345");

    /**
     * Logs in as the standard user, adds one item to the cart, and proceeds to checkout
     * so each test starts on the checkout step-one page.
     *
     * <p>Uses {@link BaseTest#loginAs(TestUser)}, {@link ProductsPage#addFirstItemToCart()},
     * and {@link ProductsPage#goToCart()} to avoid raw selectors in setup (DRY / OCP).</p>
     */
    @BeforeEach
    void setUpCartWithOneItem() {
        loginAs(TestUser.STANDARD);

        ProductsPage productsPage = new ProductsPage();
        productsPage.addFirstItemToCart();
        productsPage.goToCart();

        new CartPage().proceedToCheckout();
    }

    /**
     * Verifies that the checkout step-one page displays the first name, last name,
     * and postal code input fields.
     */
    @Test
    @Story("Checkout")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Checkout form requests shipping info fields")
    void checkoutFormRequestsShippingInfo() {
        $("[data-test='firstName']").shouldBe(Condition.visible);
        $("[data-test='lastName']").shouldBe(Condition.visible);
        $("[data-test='postalCode']").shouldBe(Condition.visible);
    }

    /**
     * Verifies that submitting valid shipping info advances to the order summary page
     * which shows item names and a total price.
     */
    @Test
    @Story("Checkout")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Valid shipping info shows order summary")
    void validShippingInfoShowsOrderSummary() {
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.enterShippingInfo(VALID_SHIPPING);
        checkoutPage.continueCheckout();

        assertThat(checkoutPage.getItemNames()).isNotEmpty();
        assertThat(checkoutPage.getTotalPrice()).isNotBlank();
    }

    /**
     * Verifies that completing the order displays the "Thank you for your order" confirmation.
     */
    @Test
    @Story("Checkout")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Completing order shows confirmation message")
    void completingOrderShowsConfirmationMessage() {
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.enterShippingInfo(VALID_SHIPPING);
        checkoutPage.continueCheckout();
        checkoutPage.finishOrder();

        assertThat(checkoutPage.getConfirmationMessage()).contains("Thank you for your order");
    }

    /**
     * Verifies that submitting the checkout form with an empty first name field
     * displays the "First Name is required" validation error.
     */
    @Test
    @Story("Checkout")
    @Severity(SeverityLevel.NORMAL)
    @Description("Empty first name shows validation error")
    void emptyFirstNameShowsError() {
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.enterShippingInfo(new ShippingInfo("", "Doe", "12345"));
        checkoutPage.continueCheckout();

        assertThat(checkoutPage.getErrorMessage()).contains("First Name is required");
    }
}
