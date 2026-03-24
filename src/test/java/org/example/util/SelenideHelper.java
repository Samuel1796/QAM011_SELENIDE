package org.example.util;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$;

/**
 * Utility class providing reusable Selenide helper methods shared across page objects.
 *
 * <p>Centralises common UI interaction patterns to avoid duplication (DRY principle)
 * and keep page objects focused on their own page's concerns (SRP).</p>
 */
public final class SelenideHelper {

    /** Prevent instantiation of this utility class. */
    private SelenideHelper() {}

    /**
     * Returns the visible text of an element identified by the given CSS selector,
     * or an empty string if the element does not exist or is not visible.
     *
     * <p>Used by any page that needs to safely read an optional error message
     * without throwing an {@code ElementNotFound} exception.</p>
     *
     * @param cssSelector the CSS selector of the target element
     * @return the element's text, or {@code ""} if absent/hidden
     */
    public static String getOptionalText(String cssSelector) {
        SelenideElement el = $(cssSelector);
        if (el.exists() && el.is(Condition.visible)) {
            return el.getText();
        }
        return "";
    }

    /**
     * Returns {@code true} if the element identified by the given CSS selector
     * exists in the DOM and is currently visible.
     *
     * @param cssSelector the CSS selector of the target element
     * @return {@code true} if visible, {@code false} otherwise
     */
    public static boolean isVisible(String cssSelector) {
        SelenideElement el = $(cssSelector);
        return el.exists() && el.is(Condition.visible);
    }
}
