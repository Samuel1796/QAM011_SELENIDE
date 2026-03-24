package org.example.model;

/**
 * Immutable value object representing the shipping information entered during checkout.
 *
 * <p>Passed to {@link org.example.pages.CheckoutPage#enterShippingInfo(ShippingInfo)}
 * to avoid a long parameter list and make test data self-documenting (clean API / ISP).</p>
 *
 * <p>Any field may be set to an empty string to intentionally trigger a validation
 * error on the checkout form (used in negative-path tests).</p>
 *
 * @param firstName  the buyer's first name
 * @param lastName   the buyer's last name
 * @param postalCode the buyer's postal/ZIP code
 */
public record ShippingInfo(String firstName, String lastName, String postalCode) {}
