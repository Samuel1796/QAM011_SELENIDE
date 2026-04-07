# Test Guide

This document explains every test class, each individual test case, and how parameterised tests are wired to their data sources.

---

## Test Architecture Overview

All test classes extend `BaseTest`, which handles the browser lifecycle and provides pre-instantiated page objects. Test data is centralised in `TestDataProvider`. The two are connected through JUnit 5's `@MethodSource` mechanism.

```
TestUser (enum)          TestDataProvider (constants + Stream providers)
      │                            │
      │  loginAs(TestUser)         │  @MethodSource("...#providerMethod")
      ▼                            ▼
   BaseTest  ◄──── extended by ────  LoginTest / ProductsTest / CartTest / CheckoutTest
      │
      ├── loginPage        (LoginPage)
      ├── productsPage     (ProductsPage)
      ├── productDetailPage (ProductDetailPage)
      ├── cartPage         (CartPage)
      └── checkoutPage     (CheckoutPage)
```

---

## How Parameterised Tests Work

JUnit 5 parameterised tests replace writing the same test body multiple times with different inputs. The key annotations are:

- `@ParameterizedTest` — marks the method as parameterised; JUnit runs it once per data row
- `@MethodSource("fully.qualified.ClassName#methodName")` — points to a static method that returns a `Stream` of inputs
- `@ValueSource(ints = {...})` — inline integer values, used when only one parameter is needed

The `@MethodSource` string format is always:
```
"org.example.util.TestDataProvider#providerMethodName"
```

JUnit resolves this at runtime, calls the static method, and injects each element as test arguments. For `Stream<Arguments>`, each `Arguments.of(...)` maps to one test invocation with multiple parameters. For `Stream<String>` or `Stream<Integer>`, each element maps to a single-parameter invocation.

---

## BaseTest

**File:** `src/test/java/org/example/BaseTest.java`

Every test class extends this. It runs before and after each individual test method (including each parameterised iteration).

**`@BeforeEach setUp()`**
1. Calls `BrowserConfig.applyBrowserOptions()` — applies `--headless=new` etc. if headless mode is on
2. Registers the `AllureSelenide` listener — attaches screenshots/page source to Allure on Selenide assertion failures
3. Opens `https://www.saucedemo.com/` via `open("/")`
4. Instantiates all five page objects as `protected` fields

**`@AfterEach tearDown()`**
Removes the Allure listener. WebDriver is closed by `ScreenshotWatcher`, not here.

**`loginAs(TestUser user)`**
Shared helper that calls `loginPage.enterUsername(...).enterPassword(...).submit()`. Every test class that needs to be logged in calls this rather than duplicating the three lines.

---

## LoginTest

**File:** `src/test/java/org/example/tests/LoginTest.java`
**Tag:** `@Tag("smoke")` — runs with `mvn verify -Dgroups=smoke`
**Requirements covered:** 3.1–3.5, 7.1

### Setup
No `@BeforeEach` — each test starts on the login page (opened by `BaseTest.setUp()`).

### Test Cases

#### `validLoginNavigatesToProductsPage`
Type: `@Test` (single run)
Uses: `TestUser.STANDARD` via `loginAs()`
Asserts: `.inventory_list` is visible after login.

#### `logoutViaBurgerMenuReturnsToLoginPage`
Type: `@Test`
Uses: `TestUser.STANDARD`
Steps: logs in → opens burger menu → clicks logout
Asserts: URL does not contain `/inventory.html` and login button is visible again.

#### `invalidCredentialCombinationsShowMismatchError`
Type: `@ParameterizedTest`
Provider: `TestDataProvider#invalidCredentialRows` → `Stream<Arguments>`

Each `Arguments.of(username, password, expectedMsg)` produces one test run:

| Run | username | password | expectedMsg |
|---|---|---|---|
| 1 | `bad_user` | `bad_pass` | `Username and password do not match` |
| 2 | `bad_user` | `secret_sauce` | `Username and password do not match` |
| 3 | `standard_user` | `wrong_pass` | `Username and password do not match` |

The test method receives three parameters: `String username, String password, String expectedMsg`.
Asserts: `loginPage.getErrorMessage()` contains `expectedMsg`.

#### `emptyUsernameShowsError`
Type: `@Test`
Uses: `TestDataProvider.VALID_PASSWORD` (constant)
Asserts: error contains `"Username is required"`.

#### `emptyPasswordShowsError`
Type: `@Test`
Uses: `TestDataProvider.VALID_USERNAME` (constant)
Asserts: error contains `"Password is required"`.

#### `whitespaceOnlyUsernameIsRejected`
Type: `@ParameterizedTest`
Provider: `TestDataProvider#whitespaceUsernames` → `Stream<String>`

Each string produces one run:

| Run | username |
|---|---|
| 1 | `" "` (single space) |
| 2 | `"  "` (two spaces) |
| 3 | `"\t"` (tab) |

The test method receives one parameter: `String username`.
Uses: `TestDataProvider.VALID_PASSWORD` as the password constant.
Asserts: error message is not empty (whitespace is not a valid username).

#### `specialCharacterUsernamesAreRejectedGracefully`
Type: `@ParameterizedTest`
Provider: `TestDataProvider#maliciousUsernames` → `Stream<String>`

| Run | username |
|---|---|
| 1 | `' OR '1'='1` |
| 2 | `<script>alert(1)</script>` |
| 3 | `admin'--` |
| 4 | `standard_user\n` |
| 5 | 255 × `a` |

Asserts: error is not empty AND URL does not contain `/inventory.html` (app did not crash or bypass auth).

#### `lockedOutUserShowsError`
Type: `@Test`
Uses: `TestUser.LOCKED_OUT` directly (not via `loginAs()` — needs to assert the error, not proceed)
Asserts: error contains `"Sorry, this user has been locked out"`.

#### `dismissingErrorMessageHidesIt`
Type: `@Test`
Steps: submits empty form → asserts error is visible → clicks dismiss → asserts error is gone.

---

## ProductsTest

**File:** `src/test/java/org/example/tests/ProductsTest.java`
**Tag:** `@Tag("smoke")`
**Requirements covered:** 4.1–4.4, 7.2

### Setup
`@BeforeEach loginAsStandardUser()` — calls `loginAs(TestUser.STANDARD)` so every test starts on the products page.

### Test Cases

#### `productsPageDisplaysAtLeastSixProducts`
Type: `@Test`
Asserts: `productsPage.getProductNames()` has size ≥ 6.

#### `selectingProductNavigatesToDetailPageWithMatchingName`
Type: `@Test`
Steps: gets first product name → selects it → reads name from detail page
Asserts: detail page name equals the name that was clicked.

#### `productDetailPageShowsPriceAndDescription`
Type: `@Test`
Steps: selects first product
Asserts: price matches regex `\$\d+\.\d{2}` and description is not blank.

#### `addToCartFromDetailPageShowsBadgeCountOne`
Type: `@Test`
Steps: selects product → adds to cart → navigates back
Asserts: cart badge count equals 1.

#### `nameSortOptionsProduceCorrectOrder`
Type: `@ParameterizedTest`
Provider: `TestDataProvider#nameSortOptions` → `Stream<Arguments>`

| Run | sortValue | ascending |
|---|---|---|
| 1 | `"az"` | `true` |
| 2 | `"za"` | `false` |

The test method receives `String sortValue, boolean ascending`.
Steps: calls `productsPage.sortBy(sortValue)` → reads actual names → builds expected sorted copy
Asserts: actual list equals expected sorted list (case-insensitive).

#### `priceSortOptionsProduceCorrectOrder`
Type: `@ParameterizedTest`
Provider: `TestDataProvider#priceSortOptions` → `Stream<Arguments>`

| Run | sortValue | ascending |
|---|---|---|
| 1 | `"lohi"` | `true` |
| 2 | `"hilo"` | `false` |

Same pattern as name sort but operates on `List<Double>` prices.
Asserts: actual price list equals expected numerically sorted copy.

#### `cartBadgeCountMatchesItemsAdded`
Type: `@ParameterizedTest`
Provider: `@ValueSource(ints = {1, 6})` — inline, no `TestDataProvider` needed

| Run | itemCount |
|---|---|
| 1 | 1 |
| 2 | 6 |

Steps: adds `itemCount` products by index → reads badge count
Asserts: badge count equals `min(itemCount, catalogue size)`.
Boundary rationale: 1 = lower bound, 6 = upper bound (full catalogue).

#### `removingItemFromProductsPageClearsBadge`
Type: `@Test`
Steps: adds first item → removes it by index
Asserts: badge count equals 0.

---

## CartTest

**File:** `src/test/java/org/example/tests/CartTest.java`
**Tag:** `@Tag("regression")`
**Requirements covered:** 5.1–5.4, 8.1

### Setup
`@BeforeEach loginAsStandardUser()` — logs in as `TestUser.STANDARD`. Each test starts on the products page.

### Test Cases

#### `singleAddedProductAppearsInCart`
Type: `@Test`
Steps: reads first product name → adds it → navigates to cart
Asserts: cart item names contain that product name.

#### `multipleAddedProductsAllAppearInCart`
Type: `@Test`
Steps: adds first and second products by name → navigates to cart
Asserts: cart contains both names.

#### `removedItemNoLongerInCartList`
Type: `@Test`
Steps: adds first product → goes to cart → removes it
Asserts: cart item names do not contain the removed product.

#### `removingAllItemsHidesCartBadge`
Type: `@Test`
Steps: adds one product → goes to cart → removes it
Asserts: `cartPage.isCartBadgeVisible()` is false.

#### `continueShoppingNavigatesBackToProductsPage`
Type: `@Test`
Steps: adds one product → goes to cart → clicks "Continue Shopping"
Asserts: URL contains `/inventory.html`.

#### `cartItemCountMatchesNumberOfProductsAdded`
Type: `@ParameterizedTest`
Provider: `TestDataProvider#cartItemCounts` → `Stream<Integer>`

| Run | count |
|---|---|
| 1 | 1 |
| 2 | 2 |
| 3 | 3 |
| 4 | 6 |

The test method receives `int count`.
Steps: adds `count` products by index → navigates to cart
Asserts: `cartPage.getCartItemCount()` equals `toAdd` (capped at catalogue size).
Boundary rationale: 1 = lower bound, 2 and 3 = mid-range, 6 = upper bound (full catalogue).

#### `removingMiddleItemLeavesOthersIntact`
Type: `@Test`
Steps: adds products at index 0, 1, 2 → goes to cart → removes the middle one (index 1)
Asserts: remaining list contains first and third, does not contain second.
Purpose: verifies that remove targets the correct item when multiple items are present.

---

## CheckoutTest

**File:** `src/test/java/org/example/tests/CheckoutTest.java`
**Tag:** `@Tag("regression")`
**Requirements covered:** 6.1–6.4, 8.2

### Setup
`@BeforeEach setUpCartWithOneItem()`:
1. Logs in as `TestUser.STANDARD`
2. Adds the first product to the cart
3. Navigates to the cart
4. Clicks "Checkout" — every test starts on the checkout step-one form

### Test Cases

#### `checkoutFormRequestsShippingInfo`
Type: `@Test`
Asserts: first name, last name, and postal code fields are all visible on step one.

#### `validShippingInfoShowsOrderSummary`
Type: `@Test`
Uses: `TestDataProvider.VALID_SHIPPING` constant (`ShippingInfo("John", "Doe", "12345")`)
Steps: fills form → clicks Continue
Asserts: item names list is not empty and total price matches `.*\$\d+\.\d{2}.*`.

#### `completingOrderShowsConfirmationMessage`
Type: `@Test`
Uses: `TestDataProvider.VALID_SHIPPING`
Steps: fills form → Continue → Finish
Asserts: confirmation message contains `"Thank you for your order"`.

#### `emptyRequiredFieldShowsValidationError`
Type: `@ParameterizedTest`
Provider: `TestDataProvider#invalidShippingRows` → `Stream<Arguments>`

Each `Arguments.of(ShippingInfo, expectedError)` produces one run:

| Run | ShippingInfo | expectedError |
|---|---|---|
| 1 | `MISSING_FIRST_NAME` → `("", "Doe", "12345")` | `"First Name is required"` |
| 2 | `MISSING_LAST_NAME` → `("John", "", "12345")` | `"Last Name is required"` |
| 3 | `MISSING_POSTAL_CODE` → `("John", "Doe", "")` | `"Postal Code is required"` |

The test method receives `ShippingInfo info, String expectedError`.
Steps: fills form with the partial `ShippingInfo` → clicks Continue
Asserts: error message contains `expectedError`.
Boundary rationale: each required field is tested empty in isolation — this is boundary analysis on the form validation.

#### `cancelFromCheckoutStepOneReturnsToCart`
Type: `@Test`
Steps: clicks Cancel on step one
Asserts: URL contains `/cart.html`.

#### `orderSummaryListsAllCartItems`
Type: `@Test`
Steps: cancels the single-item checkout from `@BeforeEach` → goes back to products → adds two more items → proceeds to checkout again → fills valid shipping → continues
Asserts: order summary contains all three item names (the original one from setup plus the two added here).

---

## TestDataProvider Connection Map

This table shows exactly which provider method feeds which test method:

| Provider method | Returns | Feeds test | Parameters injected |
|---|---|---|---|
| `invalidCredentialRows()` | `Stream<Arguments>` | `LoginTest#invalidCredentialCombinationsShowMismatchError` | `String username, String password, String expectedMsg` |
| `whitespaceUsernames()` | `Stream<String>` | `LoginTest#whitespaceOnlyUsernameIsRejected` | `String username` |
| `maliciousUsernames()` | `Stream<String>` | `LoginTest#specialCharacterUsernamesAreRejectedGracefully` | `String username` |
| `nameSortOptions()` | `Stream<Arguments>` | `ProductsTest#nameSortOptionsProduceCorrectOrder` | `String sortValue, boolean ascending` |
| `priceSortOptions()` | `Stream<Arguments>` | `ProductsTest#priceSortOptionsProduceCorrectOrder` | `String sortValue, boolean ascending` |
| `cartItemCounts()` | `Stream<Integer>` | `CartTest#cartItemCountMatchesNumberOfProductsAdded` | `int count` |
| `invalidShippingRows()` | `Stream<Arguments>` | `CheckoutTest#emptyRequiredFieldShowsValidationError` | `ShippingInfo info, String expectedError` |

`@ValueSource(ints = {1, 6})` in `ProductsTest#cartBadgeCountMatchesItemsAdded` is inline — no `TestDataProvider` method needed since it's a simple two-value integer list.

---

## Total Test Execution Count

| Class | `@Test` methods | `@ParameterizedTest` methods | Expanded runs | Total |
|---|---|---|---|---|
| `LoginTest` | 5 | 3 | 3 + 3 + 5 = 11 | 16 |
| `ProductsTest` | 4 | 3 | 2 + 2 + 2 = 6 | 10 |
| `CartTest` | 6 | 1 | 4 | 10 |
| `CheckoutTest` | 4 | 1 | 3 | 7 |
| **Total** | **19** | **8** | **24** | **43** |

---

## Running Specific Suites

```bash
# All tests (~43 executions)
mvn verify

# Smoke only: LoginTest + ProductsTest (~26 executions)
mvn verify -Dgroups=smoke

# Regression only: CartTest + CheckoutTest (~17 executions)
mvn verify -Dgroups=regression
```
