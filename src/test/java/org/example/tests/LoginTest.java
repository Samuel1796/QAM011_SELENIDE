package org.example.tests;

import com.codeborne.selenide.Condition;
import io.qameta.allure.Description;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.example.BaseTest;
import org.example.model.TestUser;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static com.codeborne.selenide.Selenide.$;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Smoke test suite for the Swag Labs login page.
 *
 * <p>Covers Requirement 3 (Login Functionality Tests) and Requirement 7.1 (Smoke Suite).
 * All tests are tagged {@code @Tag("smoke")} so they can be run independently via
 * {@code mvn verify -Dgroups=smoke}.</p>
 *
 * <p>All page interactions go through the {@code loginPage} field inherited from
 * {@link BaseTest} — no {@code new LoginPage()} calls in test methods (DRY).</p>
 */
@Tag("smoke")
public class LoginTest extends BaseTest {

    /**
     * Verifies that submitting valid credentials navigates to the products inventory page.
     */
    @Test
    @Story("Login")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Valid credentials navigate to the products page")
    void validLoginNavigatesToProductsPage() {
        loginAs(TestUser.STANDARD);
        $(".inventory_list").shouldBe(Condition.visible);
    }

    /**
     * Verifies that submitting invalid credentials displays the appropriate error message.
     */
    @Test
    @Story("Login")
    @Severity(SeverityLevel.NORMAL)
    @Description("Invalid credentials show an error message")
    void invalidCredentialsShowError() {
        loginPage.enterUsername(TestUser.INVALID.username)
                 .enterPassword(TestUser.INVALID.password)
                 .submit();

        assertThat(loginPage.getErrorMessage())
                .contains("Username and password do not match");
    }

    /**
     * Verifies that submitting the form with an empty username field shows a validation error.
     */
    @Test
    @Story("Login")
    @Severity(SeverityLevel.NORMAL)
    @Description("Submitting with empty username shows a validation error")
    void emptyUsernameShowsError() {
        loginPage.enterUsername("")
                 .enterPassword("secret_sauce")
                 .submit();

        assertThat(loginPage.getErrorMessage())
                .contains("Username is required");
    }

    /**
     * Verifies that submitting the form with an empty password field shows a validation error.
     */
    @Test
    @Story("Login")
    @Severity(SeverityLevel.NORMAL)
    @Description("Submitting with empty password shows a validation error")
    void emptyPasswordShowsError() {
        loginPage.enterUsername("standard_user")
                 .enterPassword("")
                 .submit();

        assertThat(loginPage.getErrorMessage())
                .contains("Password is required");
    }

    /**
     * Verifies that a locked-out user account displays the appropriate error message
     * even when the correct password is supplied.
     */
    @Test
    @Story("Login")
    @Severity(SeverityLevel.NORMAL)
    @Description("Locked out user sees an appropriate error message")
    void lockedOutUserShowsError() {
        loginPage.enterUsername(TestUser.LOCKED_OUT.username)
                 .enterPassword(TestUser.LOCKED_OUT.password)
                 .submit();

        assertThat(loginPage.getErrorMessage())
                .contains("Sorry, this user has been locked out");
    }
}
