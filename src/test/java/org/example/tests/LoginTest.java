package org.example.tests;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.WebDriverRunner;
import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.example.BaseTest;
import org.example.model.TestUser;
import org.example.util.TestDataProvider;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static com.codeborne.selenide.Selenide.$;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Smoke test suite for the Swag Labs login page.
 *
 * <p>Covers Requirements 3.1–3.5 (login functionality) and Requirement 7.1
 * (smoke suite must include a valid-login test). All tests are tagged
 * {@code @Tag("smoke")} and can be run independently via
 * {@code mvn verify -Dgroups=smoke}.</p>
 *
 * <p>No {@code @BeforeEach} is needed — {@link BaseTest#setUp()} opens the base URL
 * ({@code /}) before each test, which is the login page.</p>
 *
 * <p>All test data (credentials, edge-case inputs) is sourced from
 * {@link TestDataProvider} to keep this class free of magic strings.</p>
 */
@Tag("smoke")
public class LoginTest extends BaseTest {

    // -------------------------------------------------------------------------
    // Happy paths
    // -------------------------------------------------------------------------

    /**
     * Verifies that submitting valid credentials navigates to the products page.
     *
     * <p>Uses {@link BaseTest#loginAs(TestUser)} with {@link TestUser#STANDARD} and
     * asserts that the inventory list element is visible, confirming a successful
     * login and page transition.</p>
     */
    @Test
    @Story("Login") @Severity(SeverityLevel.CRITICAL)
    @Description("Valid credentials navigate to the products page")
    void validLoginNavigatesToProductsPage() {
        loginAs(TestUser.STANDARD);
        $(".inventory_list").shouldBe(Condition.visible);
    }

    /**
     * Verifies the full logout flow via the side navigation menu.
     *
     * <p>Logs in, opens the burger menu, clicks logout, then asserts the browser
     * is no longer on the inventory page and the login button is visible again.</p>
     */
    @Test
    @Story("Login") @Severity(SeverityLevel.CRITICAL)
    @Description("User can log out via burger menu and returns to login page")
    void logoutViaBurgerMenuReturnsToLoginPage() {
        loginAs(TestUser.STANDARD);
        productsPage.openBurgerMenu();
        productsPage.logoutFromSideMenu();

        assertThat(WebDriverRunner.url()).doesNotContain("/inventory.html");
        assertThat(loginPage.isLoginButtonVisible()).isTrue();
    }

    // -------------------------------------------------------------------------
    // Parameterised invalid credentials
    // -------------------------------------------------------------------------

    /**
     * Verifies that three different invalid credential combinations all produce
     * the "Username and password do not match" error.
     *
     * <p>This is a {@code @ParameterizedTest} driven by
     * {@link TestDataProvider#invalidCredentialRows()}, which returns a
     * {@code Stream<Arguments>} with three rows:
     * <ol>
     *   <li>Wrong username + wrong password</li>
     *   <li>Wrong username + correct password</li>
     *   <li>Correct username + wrong password</li>
     * </ol>
     * JUnit 5 runs this method once per row, injecting the three arguments.</p>
     *
     * @param username    the username to submit
     * @param password    the password to submit
     * @param expectedMsg the error message fragment expected in the response
     */
    @ParameterizedTest(name = "user=''{0}'' pass=''{1}'' -> error contains ''{2}''")
    @MethodSource("org.example.util.TestDataProvider#invalidCredentialRows")
    @Story("Login") @Severity(SeverityLevel.NORMAL)
    @Description("Invalid credential combinations show the mismatch error")
    void invalidCredentialCombinationsShowMismatchError(String username, String password, String expectedMsg) {
        loginPage.enterUsername(username).enterPassword(password).submit();
        assertThat(loginPage.getErrorMessage()).contains(expectedMsg);
    }

    // -------------------------------------------------------------------------
    // Empty field boundary tests
    // -------------------------------------------------------------------------

    /**
     * Verifies that submitting with an empty username field shows the
     * "Username is required" validation error.
     *
     * <p>Uses {@link TestDataProvider#VALID_PASSWORD} as the password so the only
     * variable is the empty username, isolating the boundary condition.</p>
     */
    @Test
    @Story("Login") @Severity(SeverityLevel.NORMAL)
    @Description("Submitting with empty username shows a validation error")
    void emptyUsernameShowsError() {
        loginPage.enterUsername("").enterPassword(TestDataProvider.VALID_PASSWORD).submit();
        assertThat(loginPage.getErrorMessage()).contains("Username is required");
    }

    /**
     * Verifies that submitting with an empty password field shows the
     * "Password is required" validation error.
     *
     * <p>Uses {@link TestDataProvider#VALID_USERNAME} as the username so the only
     * variable is the empty password, isolating the boundary condition.</p>
     */
    @Test
    @Story("Login") @Severity(SeverityLevel.NORMAL)
    @Description("Submitting with empty password shows a validation error")
    void emptyPasswordShowsError() {
        loginPage.enterUsername(TestDataProvider.VALID_USERNAME).enterPassword("").submit();
        assertThat(loginPage.getErrorMessage()).contains("Password is required");
    }

    // -------------------------------------------------------------------------
    // Boundary — whitespace-only usernames
    // -------------------------------------------------------------------------

    /**
     * Verifies that whitespace-only usernames (space, double-space, tab) are
     * rejected with a non-empty error message.
     *
     * <p>This is a {@code @ParameterizedTest} driven by
     * {@link TestDataProvider#whitespaceUsernames()}, which returns a
     * {@code Stream<String>} with three values. JUnit 5 runs this method once per
     * value, injecting the string as the {@code username} parameter.</p>
     *
     * @param username a whitespace-only string to test as the username input
     */
    @ParameterizedTest(name = "username=''{0}''")
    @MethodSource("org.example.util.TestDataProvider#whitespaceUsernames")
    @Story("Login") @Severity(SeverityLevel.NORMAL)
    @Description("Whitespace-only username is rejected with an error")
    void whitespaceOnlyUsernameIsRejected(String username) {
        loginPage.enterUsername(username).enterPassword(TestDataProvider.VALID_PASSWORD).submit();
        assertThat(loginPage.getErrorMessage()).isNotEmpty();
    }

    // -------------------------------------------------------------------------
    // Error-guessing — injection / special characters
    // -------------------------------------------------------------------------

    /**
     * Verifies that injection and special-character usernames are rejected gracefully.
     *
     * <p>This is a {@code @ParameterizedTest} driven by
     * {@link TestDataProvider#maliciousUsernames()}, which returns a
     * {@code Stream<String>} with five values (SQL injection, XSS, comment injection,
     * newline injection, and a 255-character string). The test asserts two things:
     * <ol>
     *   <li>An error message is shown (the input was rejected)</li>
     *   <li>The URL does not contain {@code /inventory.html} (no auth bypass)</li>
     * </ol>
     * </p>
     *
     * @param username a malicious or edge-case string to test as the username input
     */
    @ParameterizedTest(name = "username=''{0}''")
    @MethodSource("org.example.util.TestDataProvider#maliciousUsernames")
    @Story("Login") @Severity(SeverityLevel.NORMAL)
    @Description("Special-character usernames are rejected gracefully without crashing")
    void specialCharacterUsernamesAreRejectedGracefully(String username) {
        loginPage.enterUsername(username).enterPassword(TestDataProvider.VALID_PASSWORD).submit();
        assertThat(loginPage.getErrorMessage()).isNotEmpty();
        assertThat(WebDriverRunner.url()).doesNotContain("/inventory.html");
    }

    // -------------------------------------------------------------------------
    // Locked-out user
    // -------------------------------------------------------------------------

    /**
     * Verifies that a locked-out user account shows the appropriate error message
     * even when the correct password is supplied.
     *
     * <p>Uses {@link TestUser#LOCKED_OUT} directly rather than
     * {@link BaseTest#loginAs(TestUser)} because this test needs to assert the
     * error message rather than proceed past the login page.</p>
     */
    @Test
    @Story("Login") @Severity(SeverityLevel.NORMAL)
    @Description("Locked out user sees an appropriate error message")
    void lockedOutUserShowsError() {
        loginPage.enterUsername(TestUser.LOCKED_OUT.username)
                 .enterPassword(TestUser.LOCKED_OUT.password)
                 .submit();
        assertThat(loginPage.getErrorMessage()).contains("Sorry, this user has been locked out");
    }

    // -------------------------------------------------------------------------
    // Error dismissal
    // -------------------------------------------------------------------------

    /**
     * Verifies that clicking the dismiss (×) button on the error banner hides it.
     *
     * <p>Submits an empty form to trigger the error, asserts it is visible, then
     * calls {@link org.example.pages.LoginPage#dismissError()} and asserts the
     * banner is no longer visible.</p>
     */
    @Test
    @Story("Login") @Severity(SeverityLevel.NORMAL)
    @Description("Dismissing the error message hides it")
    void dismissingErrorMessageHidesIt() {
        loginPage.enterUsername("").enterPassword("").submit();
        assertThat(loginPage.isErrorVisible()).isTrue();

        loginPage.dismissError();
        assertThat(loginPage.isErrorVisible()).isFalse();
    }
}
