package org.example.model;

/**
 * Immutable value object representing the display data of a single product
 * as shown on the product detail page.
 *
 * <p>Used by {@link org.example.pages.ProductDetailPage} to return a snapshot
 * of product data for assertion in tests, keeping method signatures clean (ISP).</p>
 *
 * @param name        the product name as displayed on the detail page
 * @param price       the product price string including the {@code $} prefix (e.g. {@code "$29.99"})
 * @param description the product description text
 */
public record ProductInfo(String name, String price, String description) {}
