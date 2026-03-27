package org.example.pages;

import org.example.model.ShippingInfo;
import org.example.util.SelenideHelper;

import java.util.List;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$$;

/**
 * Page Object representing the Swag Labs multi-step checkout flow.
 *
 * <p>Covers two pages:
 * <ul>
 *   <li>Step One ({@code /checkout-step-one.html}) — shipping information form</li>
 *   <li>Step Two ({@code /checkout-step-two.html}) — order summary and finish</li>
 * </ul>
 * </p>
 *
 * <p>Error message retrieval is delegated to {@link SelenideHelper#getOptionalText(String)}
 * to avoid duplicating the exists-and-visible guard that also appears in {@link LoginPage} (DRY).</p>
 */
public class CheckoutPage {

    /** Data-test selector for the first name input field. */
    private static final String FIRST_NAME = "[data-test='firstName']";

    /** Data-test selector for the last name input field. */
    private static final String LAST_NAME = "[data-test='lastName']";

    /** Data-test selector for the postal code input field. */
    private static final String POSTAL_CODE = "[data-test='postalCode']";

    /** Data-test selector for the "Continue" button on step one. */
    private static final String CONTINUE_BUTTON = "[data-test='continue']";

    /** Data-test selector for the "Cancel" button on checkout pages. */
    private static final String CANCEL_BUTTON = "[data-test='cancel']";

    /** Data-test selector for the "Finish" button on step two. */
    private static final String FINISH_BUTTON = "[data-test='finish']";

    /** CSS selector for item names in the order summary on step two. */
    private static final String SUMMARY_ITEM_NAME = ".cart_item .inventory_item_name";

    /** CSS selector for the total price label on the order summary page. */
    private static final String TOTAL_PRICE = ".summary_total_label";

    /** CSS selector for the order confirmation heading on the completion page. */
    private static final String CONFIRMATION_HEADER = ".complete-header";

    /** Data-test selector for the validation error message on step one. */
    private static final String ERROR_MESSAGE = "[data-test='error']";

    /**
     * Fills in the shipping information form on checkout step one.
     *
     * <p>Accepts a {@link ShippingInfo} value object to keep the method signature
     * clean and avoid a long parameter list (ISP / clean API design).</p>
     *
     * @param info the shipping details to enter; fields may be empty to trigger validation errors
     */
    public void enterShippingInfo(ShippingInfo info) {
        $(FIRST_NAME).setValue(info.firstName());
        $(LAST_NAME).setValue(info.lastName());
        $(POSTAL_CODE).setValue(info.postalCode());
    }

    /**
     * Clicks the "Continue" button to advance from step one to the order summary (step two).
     *
     * <p>If required fields are empty, the page stays on step one and displays an error.</p>
     */
    public void continueCheckout() {
        $(CONTINUE_BUTTON).click();
    }

    /**
     * Clicks the "Cancel" button from checkout to navigate back.
     */
    public void cancelCheckout() {
        $(CANCEL_BUTTON).click();
    }

    /**
     * Returns the names of all items listed in the order summary on step two.
     *
     * @return an ordered list of item name strings
     */
    public List<String> getItemNames() {
        return $$(SUMMARY_ITEM_NAME).texts();
    }

    /**
     * Returns the total price label text from the order summary on step two.
     *
     * <p>Example return value: {@code "Total: $32.39"}</p>
     *
     * @return the total price label string
     */
    public String getTotalPrice() {
        return $(TOTAL_PRICE).getText();
    }

    /**
     * Clicks the "Finish" button to complete the order and navigate to the confirmation page.
     */
    public void finishOrder() {
        $(FINISH_BUTTON).click();
    }

    /**
     * Returns the confirmation heading text displayed after a successful order.
     *
     * <p>Expected value: {@code "Thank you for your order!"}</p>
     *
     * @return the confirmation message string
     */
    public String getConfirmationMessage() {
        return $(CONFIRMATION_HEADER).getText();
    }

    /**
     * Returns the validation error message displayed when step-one form submission fails.
     *
     * <p>Delegates to {@link SelenideHelper#getOptionalText(String)} to safely handle
     * the case where no error element is present (DRY — same pattern as {@link LoginPage}).</p>
     *
     * @return the error message text, or {@code ""} if no error is displayed
     */
    public String getErrorMessage() {
        return SelenideHelper.getOptionalText(ERROR_MESSAGE);
    }

    /**
     * Returns the number of items listed in the order summary on step two.
     */
    public int getItemCount() {
        return $$(SUMMARY_ITEM_NAME).texts().size();
    }

    /**
     * Returns the sub-total label text (before tax) from the order summary.
     * Example: {@code "Item total: $29.99"}
     */
    public String getSubtotal() {
        return SelenideHelper.getOptionalText(".summary_subtotal_label");
    }

    /**
     * Returns the tax label text from the order summary.
     * Example: {@code "Tax: $2.40"}
     */
    public String getTax() {
        return SelenideHelper.getOptionalText(".summary_tax_label");
    }
}
