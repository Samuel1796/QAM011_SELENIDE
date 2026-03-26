package org.example.tests;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.WebDriverRunner;
import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.example.BaseTest;
import org.example.model.ShippingInfo;
import org.example.model.TestUser;
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
 * <p>All page interactions use the {@code productsPage}, {@code cartPage}, and
 * {@code checkoutPage} fields inherited from {@link BaseTest} — no inline page
 * object instantiation (DRY).</p>
 *
 * <p>A reusable {@link ShippingInfo} constant {@link #VALID_SHIPPING} is defined
 * once and shared across tests that need valid shipping data (DRY).</p>
 */
@Tag("regression")
public class CheckoutTest extends BaseTest {

    /**
     * Reusable valid shipping info used across multiple tests (DRY).
     */
    private static final ShippingInfo VALID_SHIPPING = new ShippingInfo("John", "Doe", "12345");

    /**
     * Logs in, adds one item to the cart, and proceeds to checkout step one
     * so each test starts on the checkout form page.
     */
    @BeforeEach
    void setUpCartWithOneItem() {
        loginAs(TestUser.STANDARD);
        productsPage.addFirstItemToCart();
        productsPage.goToCart();
        cartPage.proceedToCheckout();
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
        checkoutPage.enterShippingInfo(new ShippingInfo("", "Doe", "12345"));
        checkoutPage.continueCheckout();

        assertThat(checkoutPage.getErrorMessage()).contains("First Name is required");
    }

    /**
     * Verifies that submitting with an empty last name shows the expected validation error.
     */
    @Test
    @Story("Checkout")
    @Severity(SeverityLevel.NORMAL)
    @Description("Empty last name shows validation error")
    void emptyLastNameShowsError() {
        checkoutPage.enterShippingInfo(new ShippingInfo("John", "", "12345"));
        checkoutPage.continueCheckout();

        assertThat(checkoutPage.getErrorMessage()).contains("Last Name is required");
    }

    /**
     * Verifies that submitting with an empty postal code shows the expected validation error.
     */
    @Test
    @Story("Checkout")
    @Severity(SeverityLevel.NORMAL)
    @Description("Empty postal code shows validation error")
    void emptyPostalCodeShowsError() {
        checkoutPage.enterShippingInfo(new ShippingInfo("John", "Doe", ""));
        checkoutPage.continueCheckout();

        assertThat(checkoutPage.getErrorMessage()).contains("Postal Code is required");
    }

    /**
     * Verifies that cancel from checkout step one returns the user to the cart page.
     */
    @Test
    @Story("Checkout")
    @Severity(SeverityLevel.NORMAL)
    @Description("Cancel from checkout step one returns to cart")
    void cancelFromCheckoutStepOneReturnsToCart() {
        checkoutPage.cancelCheckout();

        assertThat(WebDriverRunner.url()).contains("/cart.html");
    }
}
