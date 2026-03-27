package org.example.util;

import org.example.model.ShippingInfo;

import java.util.stream.Stream;

/**
 * Centralises all static test data used across the test suite (DRY).
 *
 * <p>Keeps test classes free of inline magic strings and data construction.
 * Each method either returns a constant value or a {@link Stream} suitable
 * for use as a JUnit 5 {@code @MethodSource} argument provider.</p>
 *
 * <p>Data is grouped by domain: credentials, shipping, sort options, and
 * edge-case inputs (boundary / error-guessing).</p>
 */
public final class TestDataProvider {

    // -------------------------------------------------------------------------
    // Credentials — valid
    // -------------------------------------------------------------------------

    public static final String VALID_USERNAME = "standard_user";
    public static final String VALID_PASSWORD = "secret_sauce";

    // -------------------------------------------------------------------------
    // Credentials — invalid / boundary
    // -------------------------------------------------------------------------

    public static final String LOCKED_OUT_USERNAME = "locked_out_user";
    public static final String INVALID_USERNAME    = "bad_user";
    public static final String INVALID_PASSWORD    = "bad_pass";
    public static final String WRONG_PASSWORD      = "wrong_pass";

    // -------------------------------------------------------------------------
    // Shipping info — valid
    // -------------------------------------------------------------------------

    /** Standard valid shipping info used in happy-path checkout tests. */
    public static final ShippingInfo VALID_SHIPPING = new ShippingInfo("John", "Doe", "12345");

    /** Alternative valid shipping info for multi-scenario tests. */
    public static final ShippingInfo ALT_SHIPPING = new ShippingInfo("Jane", "Smith", "90210");

    // -------------------------------------------------------------------------
    // Shipping info — invalid (boundary: each required field empty)
    // -------------------------------------------------------------------------

    public static final ShippingInfo MISSING_FIRST_NAME  = new ShippingInfo("",     "Doe", "12345");
    public static final ShippingInfo MISSING_LAST_NAME   = new ShippingInfo("John", "",    "12345");
    public static final ShippingInfo MISSING_POSTAL_CODE = new ShippingInfo("John", "Doe", "");
    public static final ShippingInfo ALL_FIELDS_EMPTY    = new ShippingInfo("",     "",    "");

    // -------------------------------------------------------------------------
    // @MethodSource providers — invalid credentials (login tests)
    // -------------------------------------------------------------------------

    /**
     * Provides rows of {@code [username, password, expectedErrorFragment]} for
     * parameterised invalid-credential tests.
     *
     * <p>Covers: wrong user + wrong pass, wrong user + correct pass,
     * correct user + wrong pass.</p>
     */
    public static Stream<org.junit.jupiter.params.provider.Arguments> invalidCredentialRows() {
        return Stream.of(
            org.junit.jupiter.params.provider.Arguments.of(
                INVALID_USERNAME, INVALID_PASSWORD, "Username and password do not match"),
            org.junit.jupiter.params.provider.Arguments.of(
                INVALID_USERNAME, VALID_PASSWORD,   "Username and password do not match"),
            org.junit.jupiter.params.provider.Arguments.of(
                VALID_USERNAME,   WRONG_PASSWORD,   "Username and password do not match")
        );
    }

    /**
     * Provides whitespace-only username strings for boundary tests.
     */
    public static Stream<String> whitespaceUsernames() {
        return Stream.of(" ", "  ", "\t");
    }

    /**
     * Provides whitespace-only password strings for boundary tests.
     */
    public static Stream<String> whitespacePasswords() {
        return Stream.of(" ", "  ");
    }

    /**
     * Provides injection / special-character username strings for error-guessing tests.
     */
    public static Stream<String> maliciousUsernames() {
        return Stream.of(
            "' OR '1'='1",
            "<script>alert(1)</script>",
            "admin'--",
            "standard_user\n",
            "a".repeat(255)
        );
    }

    // -------------------------------------------------------------------------
    // @MethodSource providers — checkout validation (checkout tests)
    // -------------------------------------------------------------------------

    /**
     * Provides rows of {@code [ShippingInfo, expectedErrorFragment]} for
     * parameterised empty-field validation tests on the checkout form.
     */
    public static Stream<org.junit.jupiter.params.provider.Arguments> invalidShippingRows() {
        return Stream.of(
            org.junit.jupiter.params.provider.Arguments.of(MISSING_FIRST_NAME,  "First Name is required"),
            org.junit.jupiter.params.provider.Arguments.of(MISSING_LAST_NAME,   "Last Name is required"),
            org.junit.jupiter.params.provider.Arguments.of(MISSING_POSTAL_CODE, "Postal Code is required")
        );
    }

    /**
     * Provides whitespace-only first-name values for boundary tests on the checkout form.
     */
    public static Stream<ShippingInfo> whitespaceFirstNameRows() {
        return Stream.of(
            new ShippingInfo(" ",  "Doe", "12345"),
            new ShippingInfo("  ", "Doe", "12345")
        );
    }

    /**
     * Provides special-character first-name values for error-guessing tests.
     */
    public static Stream<ShippingInfo> maliciousFirstNameRows() {
        return Stream.of(
            new ShippingInfo("<script>alert(1)</script>", "Doe", "12345"),
            new ShippingInfo("' OR '1'='1",               "Doe", "12345"),
            new ShippingInfo("A".repeat(100),              "Doe", "12345")
        );
    }

    // -------------------------------------------------------------------------
    // @MethodSource providers — sort options (products tests)
    // -------------------------------------------------------------------------

    /**
     * Provides rows of {@code [sortValue, ascending]} for name-sort tests.
     */
    public static Stream<org.junit.jupiter.params.provider.Arguments> nameSortOptions() {
        return Stream.of(
            org.junit.jupiter.params.provider.Arguments.of("az", true),
            org.junit.jupiter.params.provider.Arguments.of("za", false)
        );
    }

    /**
     * Provides rows of {@code [sortValue, ascending]} for price-sort tests.
     */
    public static Stream<org.junit.jupiter.params.provider.Arguments> priceSortOptions() {
        return Stream.of(
            org.junit.jupiter.params.provider.Arguments.of("lohi", true),
            org.junit.jupiter.params.provider.Arguments.of("hilo", false)
        );
    }

    // -------------------------------------------------------------------------
    // @MethodSource providers — cart item counts (cart / products tests)
    // -------------------------------------------------------------------------

    /**
     * Boundary values for "add N items" tests: lower bound (1), mid (2, 3),
     * and upper bound (6 = full catalogue).
     */
    public static Stream<Integer> cartItemCounts() {
        return Stream.of(1, 2, 3, 6);
    }

    /** Prevent instantiation — this is a static utility class. */
    private TestDataProvider() {}
}
