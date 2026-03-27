package org.example.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import org.example.util.SelenideHelper;

import static com.codeborne.selenide.Selenide.$;

/**
 * Page Object representing the Swag Labs login screen at {@code /}.
 *
 * <p>Encapsulates all selectors and interactions for the login page (SRP).
 * Test classes interact with this page exclusively through its public API,
 * keeping raw Selenide calls out of test logic.</p>
 *
 * <p>Methods that return {@code this} follow the fluent builder pattern,
 * allowing chained calls: {@code loginPage.enterUsername(u).enterPassword(p).submit()}.</p>
 */
public class LoginPage {

    /** CSS selector for the username input field. */
    private static final String USERNAME_INPUT = "#user-name";

    /** CSS selector for the password input field. */
    private static final String PASSWORD_INPUT = "#password";

    /** CSS selector for the login submit button. */
    private static final String LOGIN_BUTTON = "#login-button";

    /** CSS selector for the error message container. */
    private static final String ERROR_MESSAGE = "[data-test='error']";

    /**
     * Types the given username into the username field.
     *
     * @param username the username to enter
     * @return this {@code LoginPage} instance for method chaining
     */
    public LoginPage enterUsername(String username) {
        $(USERNAME_INPUT).setValue(username);
        return this;
    }

    /**
     * Types the given password into the password field.
     *
     * @param password the password to enter
     * @return this {@code LoginPage} instance for method chaining
     */
    public LoginPage enterPassword(String password) {
        $(PASSWORD_INPUT).setValue(password);
        return this;
    }

    /**
     * Clicks the login button to submit the form.
     *
     * <p>Selenide's built-in auto-wait ensures the button is clickable before
     * the click is dispatched — no explicit waits needed.</p>
     */
    public void submit() {
        $(LOGIN_BUTTON).click();
    }

    /**
     * Returns the text of the error message displayed after a failed login attempt.
     *
     * <p>Delegates to {@link SelenideHelper#getOptionalText(String)} to safely handle
     * the case where no error element is present (DRY — avoids duplicating the
     * exists-and-visible guard in every page object).</p>
     *
     * @return the error message text, or {@code ""} if no error is displayed
     */
    public String getErrorMessage() {
        return SelenideHelper.getOptionalText(ERROR_MESSAGE);
    }

    /**
     * Returns whether the login button is visible on the login page.
     *
     * @return {@code true} if login button is visible, {@code false} otherwise
     */
    public boolean isLoginButtonVisible() {
        return $(LOGIN_BUTTON).exists() && $(LOGIN_BUTTON).is(Condition.visible);
    }

    /**
     * Clears the error message by clicking the dismiss (×) button on the error container.
     * No-op if no error is currently displayed.
     */
    public void dismissError() {
        SelenideElement dismiss = $(".error-button");
        if (dismiss.exists() && dismiss.is(Condition.visible)) {
            dismiss.click();
        }
    }

    /**
     * Returns {@code true} if the error message container is currently visible.
     */
    public boolean isErrorVisible() {
        SelenideElement el = $(ERROR_MESSAGE);
        return el.exists() && el.is(Condition.visible);
    }
}
