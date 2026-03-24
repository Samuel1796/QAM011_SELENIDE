# Implementation Plan: Selenide UI Test Automation Suite

## Overview

Incremental build-out of a Java/Maven Selenide test suite targeting https://www.saucedemo.com/. Each task produces working, integrated code. Page objects are built before tests, infrastructure before CI.

## Tasks

- [x] 1. Configure Maven project (pom.xml and properties)
  - Add dependencies: `selenide`, `junit-jupiter`, `allure-junit5`, `allure-selenide`, `jqwik`, `slf4j-simple`
  - Configure `maven-surefire-plugin` (≥ 3.x) with JUnit 5 provider and `groups` property support
  - Configure `allure-maven` plugin for report generation
  - Create `src/test/resources/selenide.properties` with `baseUrl`, `browser=chrome`, `headless=true`, `timeout=8000`
  - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5_

- [x] 2. Create data models and shared constants
  - [x] 2.1 Implement `TestUser` enum with `STANDARD`, `LOCKED_OUT`, and `INVALID` entries (username/password fields)
    - Place in `src/test/java/org/example/model/TestUser.java`
    - _Requirements: 3.1, 3.2, 3.5_
  - [x] 2.2 Implement `ProductInfo` record (`name`, `price`, `description`)
    - Place in `src/test/java/org/example/model/ProductInfo.java`
    - _Requirements: 4.2, 4.3_
  - [x] 2.3 Implement `ShippingInfo` record (`firstName`, `lastName`, `postalCode`)
    - Place in `src/test/java/org/example/model/ShippingInfo.java`
    - _Requirements: 6.1_

- [x] 3. Implement `BaseTest` and screenshot infrastructure
  - Create `src/test/java/org/example/BaseTest.java` extending nothing, annotated with `@ExtendWith`
  - `@BeforeEach`: configure `ChromeOptions` (`--headless=new`, `--no-sandbox`, `--disable-dev-shm-usage`), set `Configuration.browserCapabilities`, call `open("/")`
  - `@AfterEach`: call `closeWebDriver()`
  - Register `AllureSelenide` listener for automatic screenshot attachment
  - Implement `ScreenshotWatcher` JUnit 5 `TestWatcher` extension: on failure, call `Selenide.screenshot(...)`, save to `target/screenshots/{ClassName}_{methodName}_{timestamp}.png`, create directory if absent
  - _Requirements: 1.5, 9.1, 9.2, 9.3_

- [ ] 4. Implement `ScreenshotNamer` utility and property test for naming pattern
  - [x] 4.1 Create `src/test/java/org/example/util/ScreenshotNamer.java` with static `buildName(String className, String methodName): String`
    - Returns `{className}_{methodName}_{System.currentTimeMillis()}.png`
    - _Requirements: 9.2_
  - [ ]* 4.2 Write property test for screenshot naming pattern (Property 7)
    - `// Feature: selenide-ui-test-automation, Property 7: screenshot file name matches pattern`
    - `@Property(tries = 100)` — generate alpha class/method names, assert result matches `className_methodName_\d+\.png`
    - Place in `src/test/java/org/example/properties/ScreenshotNamingPropertyTest.java`
    - _Requirements: 9.2_

- [x] 5. Implement Page Object: `LoginPage`
  - Create `src/test/java/org/example/pages/LoginPage.java`
  - Methods: `enterUsername(String)`, `enterPassword(String)`, `submit()`, `getErrorMessage(): String`
  - Use Selenide `$()` selectors; no `Thread.sleep()`
  - _Requirements: 2.1, 2.6, 2.7_

- [x] 6. Implement Page Object: `ProductsPage`
  - Create `src/test/java/org/example/pages/ProductsPage.java`
  - Methods: `getProductNames(): List<String>`, `selectProduct(String)`, `getCartBadgeCount(): int`
  - _Requirements: 2.2, 2.7_

- [x] 7. Implement Page Object: `ProductDetailPage`
  - Create `src/test/java/org/example/pages/ProductDetailPage.java`
  - Methods: `getProductName(): String`, `getPrice(): String`, `getDescription(): String`, `addToCart()`, `backToProducts()`
  - _Requirements: 2.3, 2.7_

- [x] 8. Implement Page Objects: `CartPage` and `CheckoutPage`
  - [x] 8.1 Create `src/test/java/org/example/pages/CartPage.java`
    - Methods: `getCartItemNames(): List<String>`, `removeItem(String)`, `isCartBadgeVisible(): boolean`, `proceedToCheckout()`
    - _Requirements: 2.4, 2.7_
  - [x] 8.2 Create `src/test/java/org/example/pages/CheckoutPage.java`
    - Methods: `enterShippingInfo(ShippingInfo)`, `continueCheckout()`, `getItemNames(): List<String>`, `getTotalPrice(): String`, `finishOrder()`, `getConfirmationMessage(): String`, `getErrorMessage(): String`
    - _Requirements: 2.5, 2.7_

- [x] 9. Implement `LoginTest` (smoke)
  - Create `src/test/java/org/example/tests/LoginTest.java` extending `BaseTest`, `@Tag("smoke")`
  - Annotate each method with `@Story`, `@Severity`, `@Description`
  - Test: valid login navigates to products page — _Requirements: 3.1_
  - Test: invalid credentials show error "Username and password do not match" — _Requirements: 3.2_
  - Test: empty username shows "Username is required" — _Requirements: 3.3_
  - Test: empty password shows "Password is required" — _Requirements: 3.4_
  - Test: locked-out user shows "Sorry, this user has been locked out" — _Requirements: 3.5_

- [ ] 10. Write property test for invalid credentials (Property 1)
  - [ ]* 10.1 Add `InvalidCredentialsPropertyTest` in `src/test/java/org/example/properties/`
    - `// Feature: selenide-ui-test-automation, Property 1: invalid credentials always produce an error message`
    - `@Property(tries = 100)` — provide arbitrary username/password strings excluding the valid pair; assert `getErrorMessage()` is not empty
    - Extends `BaseTest`
    - _Requirements: 3.2, 3.3, 3.4, 3.5_

- [x] 11. Implement `ProductsTest` (smoke)
  - Create `src/test/java/org/example/tests/ProductsTest.java` extending `BaseTest`, `@Tag("smoke")`
  - Login with `TestUser.STANDARD` in `@BeforeEach`
  - Test: products page displays ≥ 6 products — _Requirements: 4.1_
  - Test: selecting a product navigates to detail page with matching name — _Requirements: 4.2_
  - Test: product detail page shows price matching `$X.XX` — _Requirements: 4.3_
  - Test: add to cart from detail page shows badge count `1` on back-navigation — _Requirements: 4.4_

- [ ] 12. Write property test for product detail page (Property 2)
  - [ ]* 12.1 Add `ProductDetailPropertyTest` in `src/test/java/org/example/properties/`
    - `// Feature: selenide-ui-test-automation, Property 2: product detail page reflects selected product`
    - `@Property(tries = 6)` — provide each of the 6 product names; assert detail name equals selected name and price matches `\$\d+\.\d{2}`
    - Extends `BaseTest`, logs in before each try
    - _Requirements: 4.2, 4.3_

- [ ] 13. Checkpoint — smoke suite passes
  - Ensure all tests pass, ask the user if questions arise.

- [x] 14. Implement `CartTest` (regression)
  - Create `src/test/java/org/example/tests/CartTest.java` extending `BaseTest`, `@Tag("regression")`
  - Login and add products in `@BeforeEach`
  - Test: single added product appears in cart — _Requirements: 5.1_
  - Test: multiple added products all appear in cart — _Requirements: 5.2_
  - Test: removed item no longer in cart list — _Requirements: 5.3_
  - Test: removing all items hides cart badge — _Requirements: 5.4_

- [ ] 15. Write property tests for cart behavior (Properties 3, 4, 5)
  - [ ]* 15.1 Add `CartContentsPropertyTest` in `src/test/java/org/example/properties/`
    - `// Feature: selenide-ui-test-automation, Property 3: cart contents match added products`
    - `@Property(tries = 100)` — generate non-empty subsets of product names; assert cart contains exactly those names
    - _Requirements: 5.1, 5.2_
  - [ ]* 15.2 Add `CartRemovalPropertyTest`
    - `// Feature: selenide-ui-test-automation, Property 4: removing item excludes it from cart`
    - `@Property(tries = 100)` — add a product, remove it, assert name absent from cart list
    - _Requirements: 5.3_
  - [ ]* 15.3 Add `EmptyCartBadgePropertyTest`
    - `// Feature: selenide-ui-test-automation, Property 5: empty cart has no visible badge`
    - `@Property(tries = 100)` — add 1–6 products, remove all, assert badge not visible
    - _Requirements: 5.4_

- [x] 16. Implement `CheckoutTest` (regression)
  - Create `src/test/java/org/example/tests/CheckoutTest.java` extending `BaseTest`, `@Tag("regression")`
  - Login and add item in `@BeforeEach`
  - Test: checkout form requests first name, last name, postal code — _Requirements: 6.1_
  - Test: valid shipping info shows order summary with item names and total price — _Requirements: 6.2_
  - Test: completing order shows "Thank you for your order" — _Requirements: 6.3_
  - Test: empty first name shows "First Name is required" — _Requirements: 6.4_

- [ ] 17. Write property test for order summary (Property 6)
  - [ ]* 17.1 Add `OrderSummaryPropertyTest` in `src/test/java/org/example/properties/`
    - `// Feature: selenide-ui-test-automation, Property 6: order summary contains all cart items`
    - `@Property(tries = 100)` — generate 1–3 product names and valid `ShippingInfo`; assert all product names appear in checkout summary
    - _Requirements: 6.2_

- [ ] 18. Checkpoint — regression suite passes
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 19. Write property tests for report data models (Properties 8, 9)
  - [ ]* 19.1 Add `ReportCountsPropertyTest` in `src/test/java/org/example/properties/`
    - `// Feature: selenide-ui-test-automation, Property 8: report counts are consistent`
    - `@Property(tries = 100)` — generate `TestRunSummary` value objects; assert `passed + failed + skipped == total`
    - _Requirements: 10.2_
  - [ ]* 19.2 Add `FailedTestEntryPropertyTest`
    - `// Feature: selenide-ui-test-automation, Property 9: failed test entries include failure details`
    - `@Property(tries = 100)` — generate `AllureTestResult` value objects with non-empty failure fields; assert both `failureMessage` and `stackTrace` are non-empty
    - _Requirements: 10.3_

- [x] 20. Create `Dockerfile`
  - Place in project root; base image `selenium/standalone-chrome:latest`
  - Install Maven (`apt-get install -y maven`)
  - `WORKDIR /app`, `COPY . .`, pre-resolve dependencies with `mvn dependency:resolve -q`
  - `CMD ["mvn", "verify"]`
  - Support `SELENIDE_BASE_URL` and `SELENIDE_BROWSER` env vars (read in `selenide.properties` or `BaseTest`)
  - _Requirements: 11.1, 11.2, 11.3, 11.4_

- [x] 21. Create GitHub Actions workflow
  - Create `.github/workflows/test.yml`
  - Trigger: `push` to any branch
  - Steps: checkout → setup JDK 17 → `mvn verify` → upload `target/allure-results/`, `target/screenshots/`, `target/site/allure-maven-plugin/` as artifacts → notify via Slack webhook (`SLACK_WEBHOOK_URL` secret) on failure (non-blocking step)
  - _Requirements: 12.1, 12.2, 12.3, 12.4, 12.5_

- [ ] 22. Final checkpoint — full suite and CI green
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional and can be skipped for a faster MVP
- Property tests use jqwik `@Property` / `@ForAll`; each runs 100 tries unless noted
- All test classes extend `BaseTest` to inherit browser setup, teardown, and Allure listener
- No `Thread.sleep()` anywhere — Selenide's built-in polling handles all waits
- Run smoke only: `mvn verify -Dgroups=smoke` | regression only: `mvn verify -Dgroups=regression`
