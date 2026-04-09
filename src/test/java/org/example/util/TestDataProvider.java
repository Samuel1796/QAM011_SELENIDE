package org.example.util;

import org.example.model.ShippingInfo;
import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

/**
 * Centralises all static test data used across the test suite (DRY).
 *
 * <p>This class has two kinds of members:
 * <ol>
 *   <li><b>Constants</b> — used directly in test methods via
 *       {@code TestDataProvider.VALID_USERNAME} etc.</li>
 *   <li><b>{@code @MethodSource} providers</b> — static methods that return a
 *       {@link Stream} and are referenced by {@code @ParameterizedTest} methods
 *       using the annotation {@code @MethodSource("org.example.util.TestDataProvider#methodName")}.
 *       JUnit 5 calls the method at runtime and injects each stream element as
 *       test arguments.</li>
 * </ol>
 * </p>
 *
 * <p>Keeping all data here means a credential or shipping value only needs to be
 * updated in one place, regardless of how many tests use it.</p>
 */
public final class TestDataProvider {

    // -------------------------------------------------------------------------
    // Credentials — valid
    // -------------------------------------------------------------------------

    /** Username for the fully functional standard user account on SauceDemo. */
    public static final String VALID_USERNAME = "standard_user";

    /** Password shared by all valid SauceDemo test accounts. */
    public static final String VALID_PASSWORD = "secret_sauce";

    // -------------------------------------------------------------------------
    // Credentials — invalid / boundary
    // -------------------------------------------------------------------------

    /** Username for a non-existent account, used to trigger the mismatch error. */
    public static final String INVALID_USERNAME = "bad_user";

    /** Password for the non-existent account. */
    public static final String INVALID_PASSWORD = "bad_pass";

    /**
     * A wrong password for the valid username, used to verify that a correct
     * username with an incorrect password still shows the mismatch error.
     */
    public static final String WRONG_PASSWORD = "wrong_pass";

    // -------------------------------------------------------------------------
    // Shipping info — valid
    // -------------------------------------------------------------------------

    /**
     * Standard valid shipping info used in happy-path checkout tests.
     * All three required fields are populated with realistic values.
     */
    public static final ShippingInfo VALID_SHIPPING = new ShippingInfo("John", "Doe", "12345");

    // -------------------------------------------------------------------------
    // Shipping info — invalid (boundary: each required field empty in isolation)
    // -------------------------------------------------------------------------

    /**
     * Shipping info with an empty first name.
     * Used to verify the "First Name is required" validation error.
     */
    public static final ShippingInfo MISSING_FIRST_NAME = new ShippingInfo("", "Doe", "12345");

    /**
     * Shipping info with an empty last name.
     * Used to verify the "Last Name is required" validation error.
     */
    public static final ShippingInfo MISSING_LAST_NAME = new ShippingInfo("John", "", "12345");

    /**
     * Shipping info with an empty postal code.
     * Used to verify the "Postal Code is required" validation error.
     */
    public static final ShippingInfo MISSING_POSTAL_CODE = new ShippingInfo("John", "Doe", "");

    // -------------------------------------------------------------------------
    // @MethodSource providers — LoginTest
    // -------------------------------------------------------------------------

    /**
     * Provides three rows of invalid credential combinations for
     * {@code LoginTest#invalidCredentialCombinationsShowMismatchError}.
     *
     * <p>Each {@link Arguments} row contains:
     * {@code (String username, String password, String expectedErrorFragment)}.
     * The three rows cover:
     * <ol>
     *   <li>Wrong username + wrong password</li>
     *   <li>Wrong username + correct password</li>
     *   <li>Correct username + wrong password</li>
     * </ol>
     * All three should produce the same "Username and password do not match" error.</p>
     *
     * @return a stream of three argument rows
     */
    public static Stream<Arguments> invalidCredentialRows() {
        return Stream.of(
            Arguments.of(INVALID_USERNAME, INVALID_PASSWORD, "Username and password do not match"),
            Arguments.of(INVALID_USERNAME, VALID_PASSWORD,   "Username and password do not match"),
            Arguments.of(VALID_USERNAME,   WRONG_PASSWORD,   "Username and password do not match")
        );
    }

    /**
     * Provides whitespace-only username strings for boundary tests in
     * {@code LoginTest#whitespaceOnlyUsernameIsRejected}.
     *
     * <p>Whitespace inputs (single space, double space, tab) should not be treated
     * as valid credentials and must produce an error message.</p>
     *
     * @return a stream of three whitespace strings
     */
    public static Stream<String> whitespaceUsernames() {
        return Stream.of(" ", "  ", "\t");
    }

    /**
     * Provides injection and special-character username strings for error-guessing
     * tests in {@code LoginTest#specialCharacterUsernamesAreRejectedGracefully}.
     *
     * <p>These inputs simulate common attack vectors (SQL injection, XSS, comment
     * injection, and an oversized input). The app must reject
     * them gracefully — showing an error and not navigating to the inventory page.</p>
     *
     * @return a stream of four malicious input strings
     */
    public static Stream<String> maliciousUsernames() {
        return Stream.of(
            "' OR '1'='1",
            "<script>alert(1)</script>",
            "admin'--",
            "a".repeat(255)
        );
    }

    // -------------------------------------------------------------------------
    // @MethodSource providers — CheckoutTest
    // -------------------------------------------------------------------------

    /**
     * Provides three rows pairing an invalid {@link ShippingInfo} with its expected
     * validation error message, for
     * {@code CheckoutTest#emptyRequiredFieldShowsValidationError}.
     *
     * <p>Each row tests one required field being empty in isolation (boundary analysis).
     * Each {@link Arguments} row contains:
     * {@code (ShippingInfo info, String expectedErrorFragment)}.</p>
     *
     * @return a stream of three argument rows
     */
    public static Stream<Arguments> invalidShippingRows() {
        return Stream.of(
            Arguments.of(MISSING_FIRST_NAME,  "First Name is required"),
            Arguments.of(MISSING_LAST_NAME,   "Last Name is required"),
            Arguments.of(MISSING_POSTAL_CODE, "Postal Code is required")
        );
    }

    // -------------------------------------------------------------------------
    // @MethodSource providers — ProductsTest
    // -------------------------------------------------------------------------

    /**
     * Provides two rows of name-sort options for
     * {@code ProductsTest#nameSortOptionsProduceCorrectOrder}.
     *
     * <p>Each {@link Arguments} row contains:
     * {@code (String sortValue, boolean ascending)}.
     * The test applies the sort, reads the actual product names, builds an
     * expected sorted copy, and asserts they match.</p>
     *
     * @return a stream of two argument rows (A-Z and Z-A)
     */
    public static Stream<Arguments> nameSortOptions() {
        return Stream.of(
            Arguments.of("az", true),
            Arguments.of("za", false)
        );
    }

    /**
     * Provides two rows of price-sort options for
     * {@code ProductsTest#priceSortOptionsProduceCorrectOrder}.
     *
     * <p>Each {@link Arguments} row contains:
     * {@code (String sortValue, boolean ascending)}.
     * The test applies the sort, reads the actual prices as doubles, builds an
     * expected numerically sorted copy, and asserts they match.</p>
     *
     * @return a stream of two argument rows (low-to-high and high-to-low)
     */
    public static Stream<Arguments> priceSortOptions() {
        return Stream.of(
            Arguments.of("lohi", true),
            Arguments.of("hilo", false)
        );
    }

    // -------------------------------------------------------------------------
    // @MethodSource providers — CartTest
    // -------------------------------------------------------------------------

    /**
     * Provides boundary values for the "add N items and verify cart count" test in
     * {@code CartTest#cartItemCountMatchesNumberOfProductsAdded}.
     *
     * <p>The values cover:
     * <ul>
     *   <li>{@code 1} — lower boundary (minimum non-empty cart)</li>
     *   <li>{@code 2}, {@code 3} — mid-range values</li>
     *   <li>{@code 6} — upper boundary (full SauceDemo catalogue)</li>
     * </ul>
     * </p>
     *
     * @return a stream of four integer values
     */
    public static Stream<Integer> cartItemCounts() {
        return Stream.of(1, 2, 3, 6);
    }

    /** Prevent instantiation — this is a static utility class. */
    private TestDataProvider() {}
}
