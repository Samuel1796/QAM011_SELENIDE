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
 * Smoke tests for the Swag Labs login page.
 * Covers Requirements 3.1–3.5 and Requirement 7.1.
 */
@Tag("smoke")
public class LoginTest extends BaseTest {

    // Happy paths

    @Test
    @Story("Login") @Severity(SeverityLevel.CRITICAL)
    @Description("Valid credentials navigate to the products page")
    void validLoginNavigatesToProductsPage() {
        loginAs(TestUser.STANDARD);
        $(".inventory_list").shouldBe(Condition.visible);
    }

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

    // Parameterised invalid credentials — wrong user/pass combinations

    @ParameterizedTest(name = "user=''{0}'' pass=''{1}'' -> error contains ''{2}''")
    @MethodSource("org.example.util.TestDataProvider#invalidCredentialRows")
    @Story("Login") @Severity(SeverityLevel.NORMAL)
    @Description("Invalid credential combinations show the mismatch error")
    void invalidCredentialCombinationsShowMismatchError(String username, String password, String expectedMsg) {
        loginPage.enterUsername(username).enterPassword(password).submit();
        assertThat(loginPage.getErrorMessage()).contains(expectedMsg);
    }

    // Empty field boundary tests

    @Test
    @Story("Login") @Severity(SeverityLevel.NORMAL)
    @Description("Submitting with empty username shows a validation error")
    void emptyUsernameShowsError() {
        loginPage.enterUsername("").enterPassword(TestDataProvider.VALID_PASSWORD).submit();
        assertThat(loginPage.getErrorMessage()).contains("Username is required");
    }

    @Test
    @Story("Login") @Severity(SeverityLevel.NORMAL)
    @Description("Submitting with empty password shows a validation error")
    void emptyPasswordShowsError() {
        loginPage.enterUsername(TestDataProvider.VALID_USERNAME).enterPassword("").submit();
        assertThat(loginPage.getErrorMessage()).contains("Password is required");
    }

    // Boundary — whitespace-only usernames

    @ParameterizedTest(name = "username=''{0}''")
    @MethodSource("org.example.util.TestDataProvider#whitespaceUsernames")
    @Story("Login") @Severity(SeverityLevel.NORMAL)
    @Description("Whitespace-only username is rejected with an error")
    void whitespaceOnlyUsernameIsRejected(String username) {
        loginPage.enterUsername(username).enterPassword(TestDataProvider.VALID_PASSWORD).submit();
        assertThat(loginPage.getErrorMessage()).isNotEmpty();
    }

    // Error-guessing — injection / special characters

    @ParameterizedTest(name = "username=''{0}''")
    @MethodSource("org.example.util.TestDataProvider#maliciousUsernames")
    @Story("Login") @Severity(SeverityLevel.NORMAL)
    @Description("Special-character usernames are rejected gracefully without crashing")
    void specialCharacterUsernamesAreRejectedGracefully(String username) {
        loginPage.enterUsername(username).enterPassword(TestDataProvider.VALID_PASSWORD).submit();
        assertThat(loginPage.getErrorMessage()).isNotEmpty();
        assertThat(WebDriverRunner.url()).doesNotContain("/inventory.html");
    }

    // Locked-out user

    @Test
    @Story("Login") @Severity(SeverityLevel.NORMAL)
    @Description("Locked out user sees an appropriate error message")
    void lockedOutUserShowsError() {
        loginPage.enterUsername(TestUser.LOCKED_OUT.username)
                 .enterPassword(TestUser.LOCKED_OUT.password)
                 .submit();
        assertThat(loginPage.getErrorMessage()).contains("Sorry, this user has been locked out");
    }

    // Error dismissal

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
