package org.example.pages;

import com.codeborne.selenide.Condition;
import org.example.model.ShippingInfo;

import java.util.List;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

/**
 * Page Object representing the Swag Labs multi-step checkout flow.
 *
 * <p>Covers two distinct pages:
 * <ul>
 *   <li><b>Step one</b> ({@code /checkout-step-one.html}) — the shipping information
 *       form where the user enters first name, last name, and postal code.</li>
 *   <li><b>Step two</b> ({@code /checkout-step-two.html}) — the order summary page
 *       showing item names, subtotal, tax, and total price before final confirmation.</li>
 * </ul>
 * </p>
 *
 * <p>Both pages are represented in a single class because they form a single linear
 * flow and share the same test setup context.</p>
 */
public class CheckoutPage {

    /** Attribute selector for the first name input on step one. */
    private static final String FIRST_NAME = "[data-test='firstName']";

    /** Attribute selector for the last name input on step one. */
    private static final String LAST_NAME = "[data-test='lastName']";

    /** Attribute selector for the postal code input on step one. */
    private static final String POSTAL_CODE = "[data-test='postalCode']";

    /** Attribute selector for the "Continue" button that advances from step one to step two. */
    private static final String CONTINUE_BUTTON = "[data-test='continue']";

    /**
     * Attribute selector for the "Cancel" button present on both step one and step two.
     * On step one it returns to the cart; on step two it returns to the inventory page.
     */
    private static final String CANCEL_BUTTON = "[data-test='cancel']";

    /** Attribute selector for the "Finish" button on step two that completes the order. */
    private static final String FINISH_BUTTON = "[data-test='finish']";

    /**
     * CSS selector for item name labels in the order summary on step two.
     * Scoped inside {@code .cart_item} to avoid matching unrelated text.
     */
    private static final String SUMMARY_ITEM_NAME = ".cart_item .inventory_item_name";

    /** CSS selector for the total price label (including tax) on step two. */
    private static final String TOTAL_PRICE = ".summary_total_label";

    /** CSS selector for the confirmation heading shown after a successful order. */
    private static final String CONFIRMATION_HEADER = ".complete-header";

    /**
     * Attribute selector for the validation error message on step one.
     * Shown when a required field is left empty and "Continue" is clicked.
     */
    private static final String ERROR_MESSAGE = "[data-test='error']";

    /**
     * Fills in all three fields of the shipping information form on step one.
     *
     * <p>Accepts a {@link ShippingInfo} value object to keep the method signature
     * clean. Any field may be an empty string to intentionally trigger a validation
     * error — this is used in negative-path tests that verify each required field.</p>
     *
     * @param info the shipping details to enter into the form
     */
    public void enterShippingInfo(ShippingInfo info) {
        $(FIRST_NAME).shouldBe(Condition.visible, Condition.enabled).setValue(info.firstName());
        $(LAST_NAME).shouldBe(Condition.visible, Condition.enabled).setValue(info.lastName());
        $(POSTAL_CODE).shouldBe(Condition.visible, Condition.enabled).setValue(info.postalCode());
    }

    /**
     * Clicks the "Continue" button to advance from step one to the order summary.
     *
     * <p>If any required field is empty, the page stays on step one and displays
     * a validation error. Tests call {@link #getErrorMessage()} after this to
     * assert the correct error text.</p>
     */
    public void continueCheckout() {
        $(CONTINUE_BUTTON).shouldBe(Condition.visible, Condition.enabled).click();
    }

    /**
     * Clicks the "Cancel" button to navigate away from the current checkout step.
     *
     * <p>Behaviour depends on the current step:
     * <ul>
     *   <li>Step one → navigates back to {@code /cart.html}</li>
     *   <li>Step two → navigates back to {@code /inventory.html}</li>
     * </ul>
     * </p>
     */
    public void cancelCheckout() {
        $(CANCEL_BUTTON).shouldBe(Condition.visible, Condition.enabled).click();
    }

    /**
     * Returns the names of all items listed in the order summary on step two.
     *
     * <p>Used in tests to verify that every product added to the cart before
     * checkout appears in the summary, regardless of how many items were added.</p>
     *
     * @return an ordered list of item name strings from the order summary
     */
    public List<String> getItemNames() {
        $(TOTAL_PRICE).shouldBe(Condition.visible);
        return $$(SUMMARY_ITEM_NAME).texts();
    }

    /**
     * Returns the total price label text from the order summary on step two.
     *
     * <p>The label includes the prefix, e.g. {@code "Total: $32.39"}.
     * Tests assert this matches the regex {@code .*\$\d+\.\d{2}.*} to verify
     * the price format is correct.</p>
     *
     * @return the total price label string as displayed in the UI
     */
    public String getTotalPrice() {
        return $(TOTAL_PRICE).shouldBe(Condition.visible).getText();
    }

    /**
     * Clicks the "Finish" button on step two to complete the order.
     *
     * <p>Navigates to the order confirmation page where
     * {@link #getConfirmationMessage()} can be used to assert the success message.</p>
     */
    public void finishOrder() {
        $(FINISH_BUTTON).shouldBe(Condition.visible, Condition.enabled).click();
    }

    /**
     * Returns the confirmation heading text displayed after a successful order.
     *
     * <p>The expected value is {@code "Thank you for your order!"}.
     * Tests use {@code contains("Thank you for your order")} to be resilient
     * to minor punctuation differences.</p>
     *
     * @return the confirmation heading string
     */
    public String getConfirmationMessage() {
        return $(CONFIRMATION_HEADER).shouldBe(Condition.visible).getText();
    }

    /**
     * Returns the validation error message displayed when step-one form submission fails.
     *
     * <p>Waits for the error element to be visible before reading its text, which
     * ensures the assertion does not race against the DOM update after clicking
     * "Continue" with an empty field.</p>
     *
     * @return the visible validation error message text
     * @throws com.codeborne.selenide.ex.ElementNotFound if no error appears within the configured timeout
     */
    public String getErrorMessage() {
        return $(ERROR_MESSAGE).shouldBe(Condition.visible).getText();
    }

    public boolean isShippingFormVisible() {
        $(FIRST_NAME).shouldBe(Condition.visible);
        $(LAST_NAME).shouldBe(Condition.visible);
        $(POSTAL_CODE).shouldBe(Condition.visible);
        return true;
    }
}
