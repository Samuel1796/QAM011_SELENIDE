package org.example.model;

/**
 * Enum representing the test user accounts available on Swag Labs.
 *
 * <p>Centralises all credential constants in one place (DRY) so that any change
 * to a test account only requires updating this enum, not every test class.</p>
 *
 * <p>Each constant carries its own {@code username} and {@code password} fields,
 * making usage self-documenting: {@code TestUser.STANDARD.username}.</p>
 */
public enum TestUser {

    /**
     * A fully functional standard user account.
     * Used in the majority of positive-path tests.
     */
    STANDARD("standard_user", "secret_sauce"),

    /**
     * An account that has been locked out by the administrator.
     * Used to verify the locked-out error message on the login page.
     */
    LOCKED_OUT("locked_out_user", "secret_sauce"),

    /**
     * A non-existent account with invalid credentials.
     * Used to verify the invalid-credentials error message on the login page.
     */
    INVALID("bad_user", "bad_pass");

    /** The username string for this test account. */
    public final String username;

    /** The password string for this test account. */
    public final String password;

    /**
     * Constructs a {@code TestUser} constant with the given credentials.
     *
     * @param username the account username
     * @param password the account password
     */
    TestUser(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
