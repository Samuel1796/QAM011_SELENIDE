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
import org.example.util.TestDataProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

import static com.codeborne.selenide.Selenide.$;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Regression tests for the Swag Labs checkout flow.
 * Covers Requirements 6.1–6.4 and Requirement 8.2.
 */
@Tag("regression")
public class CheckoutTest extends BaseTest {

    @BeforeEach
    void setUpCartWithOneItem() {
        loginAs(TestUser.STANDARD);
        productsPage.addFirstItemToCart();
        productsPage.goToCart();
        cartPage.proceedToCheckout();
    }

    // Happy paths

    @Test
    @Story("Checkout") @Severity(SeverityLevel.CRITICAL)
    @Description("Checkout form requests first name, last name, and postal code fields")
    void checkoutFormRequestsShippingInfo() {
        $("[data-test='firstName']").shouldBe(Condition.visible);
        $("[data-test='lastName']").shouldBe(Condition.visible);
        $("[data-test='postalCode']").shouldBe(Condition.visible);
    }

    @Test
    @Story("Checkout") @Severity(SeverityLevel.CRITICAL)
    @Description("Valid shipping info shows order summary with item names and total price")
    void validShippingInfoShowsOrderSummary() {
        checkoutPage.enterShippingInfo(TestDataProvider.VALID_SHIPPING);
        checkoutPage.continueCheckout();

        assertThat(checkoutPage.getItemNames()).isNotEmpty();
        assertThat(checkoutPage.getTotalPrice()).matches(".*\\$\\d+\\.\\d{2}.*");
    }

    @Test
    @Story("Checkout") @Severity(SeverityLevel.CRITICAL)
    @Description("Completing order shows confirmation message")
    void completingOrderShowsConfirmationMessage() {
        checkoutPage.enterShippingInfo(TestDataProvider.VALID_SHIPPING);
        checkoutPage.continueCheckout();
        checkoutPage.finishOrder();

        assertThat(checkoutPage.getConfirmationMessage()).contains("Thank you for your order");
    }

    // Parameterised empty-field validation (boundary — each required field)

    @ParameterizedTest(name = "shipping={0} -> error contains ''{1}''")
    @MethodSource("org.example.util.TestDataProvider#invalidShippingRows")
    @Story("Checkout") @Severity(SeverityLevel.NORMAL)
    @Description("Each empty required field shows its specific validation error")
    void emptyRequiredFieldShowsValidationError(ShippingInfo info, String expectedError) {
        checkoutPage.enterShippingInfo(info);
        checkoutPage.continueCheckout();
        assertThat(checkoutPage.getErrorMessage()).contains(expectedError);
    }

    // Navigation / cancel

    @Test
    @Story("Checkout") @Severity(SeverityLevel.NORMAL)
    @Description("Cancel from checkout step one returns to cart")
    void cancelFromCheckoutStepOneReturnsToCart() {
        checkoutPage.cancelCheckout();
        assertThat(WebDriverRunner.url()).contains("/cart.html");
    }

    // Multi-item checkout

    @Test
    @Story("Checkout") @Severity(SeverityLevel.NORMAL)
    @Description("Order summary lists all items added to cart before checkout")
    void orderSummaryListsAllCartItems() {
        checkoutPage.cancelCheckout();
        cartPage.continueShopping();

        List<String> names = productsPage.getProductNames();
        String second = names.get(1);
        String third  = names.get(2);
        productsPage.addItemToCartByName(second);
        productsPage.addItemToCartByName(third);
        productsPage.goToCart();
        cartPage.proceedToCheckout();

        checkoutPage.enterShippingInfo(TestDataProvider.VALID_SHIPPING);
        checkoutPage.continueCheckout();

        assertThat(checkoutPage.getItemNames()).contains(names.get(0), second, third);
    }
}
