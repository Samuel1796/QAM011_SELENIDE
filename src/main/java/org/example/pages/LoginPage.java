package org.example.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.SelenideElement;
import org.example.util.SelenideHelper;

import static com.codeborne.selenide.Selenide.$;

/**
 * Page Object representing the Swag Labs login screen at {@code /}.
 *
 * <p>Encapsulates all selectors and interactions for the login page, keeping raw
 * Selenide calls out of test logic (Page Object Model pattern). Methods that return
 * {@code this} follow the fluent builder pattern, allowing chained calls:
 * {@code loginPage.enterUsername(u).enterPassword(p).submit()}.</p>
 *
 * <p>No {@code Thread.sleep()} calls are used — Selenide's built-in auto-waiting
 * handles element readiness throughout.</p>
 */
public class LoginPage {

    /** CSS selector for the username text input field. */
    private static final String USERNAME_INPUT = "#user-name";

    /** CSS selector for the password text input field. */
    private static final String PASSWORD_INPUT = "#password";

    /** CSS selector for the login submit button. */
    private static final String LOGIN_BUTTON = "#login-button";

    /**
     * CSS selector for the error message banner shown after a failed login attempt.
     * Uses the {@code data-test} attribute for resilience against class name changes.
     */
    private static final String ERROR_MESSAGE = "[data-test='error']";

    /**
     * Types the given value into the username field, clearing any existing content first.
     *
     * <p>Passing an empty string simulates the "empty username" boundary condition,
     * which should trigger a "Username is required" validation error.</p>
     *
     * @param username the username to enter; may be empty to test validation
     * @return this {@code LoginPage} instance for fluent method chaining
     */
    public LoginPage enterUsername(String username) {
        $(USERNAME_INPUT).shouldBe(Condition.visible, Condition.enabled).setValue(username);
        return this;
    }

    /**
     * Types the given value into the password field, clearing any existing content first.
     *
     * <p>Passing an empty string simulates the "empty password" boundary condition,
     * which should trigger a "Password is required" validation error.</p>
     *
     * @param password the password to enter; may be empty to test validation
     * @return this {@code LoginPage} instance for fluent method chaining
     */
    public LoginPage enterPassword(String password) {
        $(PASSWORD_INPUT).shouldBe(Condition.visible, Condition.enabled).setValue(password);
        return this;
    }

    /**
     * Clicks the login submit button to attempt authentication.
     *
     * <p>Selenide's auto-wait ensures the button is clickable before the click is
     * dispatched. After a successful login the browser navigates to
     * {@code /inventory.html}; on failure the page stays on {@code /} and shows
     * an error banner.</p>
     */
    public void submit() {
        $(LOGIN_BUTTON).shouldBe(Condition.visible, Condition.enabled).click();
    }

    /** Returns true when the login form controls are visible and ready for use. */
    public boolean isLoaded() {
        $(USERNAME_INPUT).shouldBe(Condition.visible);
        $(PASSWORD_INPUT).shouldBe(Condition.visible);
        $(LOGIN_BUTTON).shouldBe(Condition.visible);
        return true;
    }

    /**
     * Returns the text of the error message banner displayed after a failed login.
     *
     * <p>Delegates to {@link SelenideHelper#getOptionalText(String)} which safely
     * returns {@code ""} when the banner is absent or hidden, avoiding an
     * {@code ElementNotFound} exception in tests that assert no error is shown.</p>
     *
     * @return the visible error message text, or {@code ""} if no error is displayed
     */
    public String getErrorMessage() {
        return SelenideHelper.getOptionalText(ERROR_MESSAGE);
    }

    /**
     * Returns whether the login button is currently present and visible in the DOM.
     *
     * <p>Used after a logout flow to confirm the browser has returned to the login
     * page and the form is ready for interaction.</p>
     *
     * @return {@code true} if the login button is visible, {@code false} otherwise
     */
    public boolean isLoginButtonVisible() {
        return $(LOGIN_BUTTON).exists() && $(LOGIN_BUTTON).is(Condition.visible);
    }

    /**
     * Clicks the dismiss (×) button on the error banner to hide it.
     *
     * <p>This is a no-op if no error banner is currently visible, making it safe
     * to call unconditionally in test cleanup.</p>
     */
    public void dismissError() {
        SelenideElement dismiss = $(".error-button");
        if (dismiss.exists() && dismiss.is(Condition.visible)) {
            dismiss.click();
        }
    }

    /**
     * Returns whether the error message banner is currently visible on the page.
     *
     * <p>Used in tests that verify the banner appears after a failed submission,
     * and again after dismissal to confirm it has been hidden.</p>
     *
     * @return {@code true} if the error banner is visible, {@code false} otherwise
     */
    public boolean isErrorVisible() {
        SelenideElement el = $(ERROR_MESSAGE);
        return el.exists() && el.is(Condition.visible);
    }
}
