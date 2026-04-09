package org.example.tests;

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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Regression test suite for the Swag Labs checkout flow.
 *
 * <p>Covers Requirements 6.1–6.4 (checkout functionality) and Requirement 8.2
 * (regression suite must cover all checkout acceptance criteria). All tests are
 * tagged {@code @Tag("regression")} and can be run independently via
 * {@code mvn verify -Dgroups=regression}.</p>
 *
 * <p>The {@code @BeforeEach} sets up a consistent starting state: logged in,
 * one item in the cart, and the checkout step-one form open. This means every
 * test in this class begins on the shipping information form.</p>
 *
 * <p>All shipping data is sourced from {@link TestDataProvider} to keep this
 * class free of inline magic strings.</p>
 */
@Tag("regression")
public class CheckoutTest extends BaseTest {

    /**
     * Sets up the precondition for every test in this class:
     * <ol>
     *   <li>Logs in as {@link TestUser#STANDARD}</li>
     *   <li>Adds the first product to the cart</li>
     *   <li>Navigates to the cart page</li>
     *   <li>Clicks "Checkout" to land on the step-one form</li>
     * </ol>
     * Called after the shared BaseTest setup opens the base URL.
     */
    @BeforeEach
    void setUpCartWithOneItem() {
        loginAs(TestUser.STANDARD);
        productsPage.addFirstItemToCart();
        productsPage.goToCart();
        cartPage.proceedToCheckout();
    }

    // -------------------------------------------------------------------------
    // Happy paths
    // -------------------------------------------------------------------------

    /**
     * Verifies that the checkout step-one page displays all three required
     * shipping information fields.
     *
     * <p>Uses Selenide's {@code shouldBe(Condition.visible)} directly on the
     * elements rather than going through the page object, because this test is
     * specifically about the presence of the form fields — not about filling them.</p>
     */
    @Test
    @Story("Checkout") @Severity(SeverityLevel.CRITICAL)
    @Description("Checkout form requests first name, last name, and postal code fields")
    void checkoutFormRequestsShippingInfo() {
        assertThat(checkoutPage.isShippingFormVisible()).isTrue();
    }

    /**
     * Verifies that submitting valid shipping information advances to the order
     * summary page, which shows item names and a correctly formatted total price.
     *
     * <p>Uses {@link TestDataProvider#VALID_SHIPPING} ({@code "John", "Doe", "12345"}).
     * The total price regex {@code .*\$\d+\.\d{2}.*} matches the full label text
     * (e.g. {@code "Total: $32.39"}) without hardcoding the exact amount.</p>
     */
    @Test
    @Story("Checkout") @Severity(SeverityLevel.CRITICAL)
    @Description("Valid shipping info shows order summary with item names and total price")
    void validShippingInfoShowsOrderSummary() {
        checkoutPage.enterShippingInfo(TestDataProvider.VALID_SHIPPING);
        checkoutPage.continueCheckout();

        assertThat(checkoutPage.getItemNames()).isNotEmpty();
        assertThat(checkoutPage.getTotalPrice()).matches(".*\\$\\d+\\.\\d{2}.*");
    }

    /**
     * Verifies the full happy-path checkout: fill form → continue → finish →
     * confirmation message.
     *
     * <p>Uses {@code contains("Thank you for your order")} rather than an exact
     * match to be resilient to minor punctuation differences (e.g. trailing
     * exclamation mark).</p>
     */
    @Test
    @Story("Checkout") @Severity(SeverityLevel.CRITICAL)
    @Description("Completing order shows confirmation message")
    void completingOrderShowsConfirmationMessage() {
        checkoutPage.enterShippingInfo(TestDataProvider.VALID_SHIPPING);
        checkoutPage.continueCheckout();
        checkoutPage.finishOrder();

        assertThat(checkoutPage.getConfirmationMessage()).contains("Thank you for your order");
    }

    // -------------------------------------------------------------------------
    // Parameterised empty-field validation (boundary)
    // -------------------------------------------------------------------------

    /**
     * Verifies that leaving each required shipping field empty in isolation
     * produces the correct field-specific validation error.
     *
     * <p>This is a {@code @ParameterizedTest} driven by
     * {@link TestDataProvider#invalidShippingRows()}, which returns a
     * {@code Stream<Arguments>} with three rows — one per required field:
     * <ol>
     *   <li>{@link TestDataProvider#MISSING_FIRST_NAME} → "First Name is required"</li>
     *   <li>{@link TestDataProvider#MISSING_LAST_NAME} → "Last Name is required"</li>
     *   <li>{@link TestDataProvider#MISSING_POSTAL_CODE} → "Postal Code is required"</li>
     * </ol>
     * Each row is a {@link ShippingInfo} with exactly one field empty, isolating
     * the boundary condition for that field.</p>
     *
     * @param info          a {@link ShippingInfo} with one field intentionally empty
     * @param expectedError the validation error message fragment expected in the UI
     */
    @ParameterizedTest(name = "shipping={0} -> error contains ''{1}''")
    @MethodSource("org.example.util.TestDataProvider#invalidShippingRows")
    @Story("Checkout") @Severity(SeverityLevel.NORMAL)
    @Description("Each empty required field shows its specific validation error")
    void emptyRequiredFieldShowsValidationError(ShippingInfo info, String expectedError) {
        checkoutPage.enterShippingInfo(info);
        checkoutPage.continueCheckout();
        assertThat(checkoutPage.getErrorMessage()).contains(expectedError);
    }

    @Test
    @Story("Checkout") @Severity(SeverityLevel.NORMAL)
    @Description("Submitting checkout with empty first name shows first-name validation error")
    void emptyFirstNameShowsError() {
        checkoutPage.enterShippingInfo(TestDataProvider.MISSING_FIRST_NAME);
        checkoutPage.continueCheckout();
        assertThat(checkoutPage.getErrorMessage()).contains("First Name is required");
    }

    // -------------------------------------------------------------------------
    // Navigation
    // -------------------------------------------------------------------------

    /**
     * Verifies that clicking "Cancel" on checkout step one returns the user to
     * the cart page without losing cart contents.
     *
     * <p>Asserts on the URL ({@code /cart.html}) as the definitive signal of
     * navigation. Cart contents are not re-asserted here — that is covered by
     * the cart tests.</p>
     */
    @Test
    @Story("Checkout") @Severity(SeverityLevel.NORMAL)
    @Description("Cancel from checkout step one returns to cart")
    void cancelFromCheckoutStepOneReturnsToCart() {
        checkoutPage.cancelCheckout();
        assertThat(WebDriverRunner.url()).contains("/cart.html");
    }

    // -------------------------------------------------------------------------
    // Multi-item checkout
    // -------------------------------------------------------------------------

    /**
     * Verifies that the order summary on step two lists all items that were in
     * the cart, regardless of how many items were added.
     *
     * <p>The {@code @BeforeEach} adds one item. This test cancels that checkout,
     * goes back to the inventory, adds two more items, then proceeds through
     * checkout again. The summary must contain all three item names.</p>
     *
     * <p>This test exercises the multi-item path through the checkout flow,
     * which is distinct from the single-item path covered by the happy-path tests.</p>
     */
    @Test
    @Story("Checkout") @Severity(SeverityLevel.NORMAL)
    @Description("Order summary lists all items added to cart before checkout")
    void orderSummaryListsAllCartItems() {
        // Cancel the single-item checkout from @BeforeEach and go back to add more
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

        // Summary must contain the original item (from @BeforeEach) plus the two added here
        assertThat(checkoutPage.getItemNames()).contains(names.get(0), second, third);
    }
}
