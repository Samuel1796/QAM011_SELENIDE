# Project Structure

This is a Java/Maven UI test automation suite for [Swag Labs](https://www.saucedemo.com), built with Selenide, JUnit 5, and Allure reporting. It follows the Page Object Model (POM) pattern with a clean separation between production-like automation code (`src/main`) and test-only code (`src/test`).

---

## Directory Layout

```
QAM011_SELENIDE/
├── pom.xml                                          # Maven build, dependencies, plugins
├── Dockerfile                                       # Container image for CI execution
├── docker-compose.yml                               # Optional local Docker run
├── .github/
│   └── workflows/
│       └── test.yml                                 # GitHub Actions CI pipeline
└── src/
    ├── main/
    │   ├── resources/                               # (empty — config lives in test/resources)
    │   └── java/org/example/
    │       ├── config/
    │       │   └── BrowserConfig.java               # Chrome options + shared header selectors
    │       ├── model/
    │       │   └── ShippingInfo.java                # Value record for checkout form data
    │       ├── pages/
    │       │   ├── LoginPage.java                   # Page Object: login screen
    │       │   ├── ProductsPage.java                # Page Object: product listing
    │       │   ├── ProductDetailPage.java           # Page Object: product detail view
    │       │   ├── CartPage.java                    # Page Object: shopping cart
    │       │   └── CheckoutPage.java                # Page Object: checkout flow (steps 1 & 2)
    │       └── util/
    │           ├── SelenideHelper.java              # getOptionalText() shared helper
    │           └── ScreenshotNamer.java             # Builds screenshot filenames
    └── test/
        ├── resources/
        │   └── selenide.properties                  # Selenide runtime configuration
        └── java/org/example/
            ├── BaseTest.java                        # Shared test lifecycle (setup/teardown)
            ├── ScreenshotWatcher.java               # JUnit 5 TestWatcher: screenshot on failure
            ├── model/
            │   └── TestUser.java                    # Enum of test user accounts
            ├── util/
            │   └── TestDataProvider.java            # All test data constants + @MethodSource providers
            └── tests/
                ├── LoginTest.java                   # @Tag("smoke")
                ├── ProductsTest.java                # @Tag("smoke")
                ├── CartTest.java                    # @Tag("regression")
                └── CheckoutTest.java                # @Tag("regression")
```

---

## Why This Split?

| `src/main` | `src/test` |
|---|---|
| Page objects, config, model, utilities | Test classes, test lifecycle, test data |
| Compiled with `compile` scope deps (Selenide, SLF4J) | Compiled with `test` scope deps (JUnit, Allure, AssertJ) |
| Reusable automation framework layer | Test execution layer — depends on main |

This mirrors real-world POM practice: the automation framework (pages, config, helpers) lives in `main` and could theoretically be packaged as a library. Tests that consume it live in `test`.

---

## Layer Responsibilities

### Configuration — `src/main`

**`selenide.properties`** (in `src/test/resources`) is read automatically by Selenide at startup. It sets:
- `baseUrl` → `https://www.saucedemo.com`
- `browser` → `chrome`
- `headless` → `true`
- `timeout` → `8000ms`

**`BrowserConfig`** reads `Configuration.headless` (already set by Selenide from the properties file) and conditionally applies Chrome JVM flags (`--headless=new`, `--no-sandbox`, `--disable-dev-shm-usage`). It also exposes two shared CSS selectors — `CART_BADGE_SELECTOR` and `CART_LINK_SELECTOR` — used by both `ProductsPage` and `CartPage`.

---

### Model — `src/main`

**`ShippingInfo`** is a Java record (`firstName`, `lastName`, `postalCode`). Passed to `CheckoutPage.enterShippingInfo()` to keep the method signature clean. Also used as the type for checkout test data in `TestDataProvider`.

---

### Page Objects — `src/main`

Each class encapsulates the CSS/data-test selectors and Selenide interactions for one application page. Tests never use raw `$()` / `$$()` calls directly — they go through these classes.

| Class | URL | Key methods |
|---|---|---|
| `LoginPage` | `/` | `enterUsername`, `enterPassword`, `submit`, `getErrorMessage`, `isLoginButtonVisible`, `dismissError`, `isErrorVisible` |
| `ProductsPage` | `/inventory.html` | `getProductNames`, `getProductPrices`, `selectProduct`, `sortBy`, `addFirstItemToCart`, `addItemToCartByIndex`, `addItemToCartByName`, `removeItemFromCartByIndex`, `getCartBadgeCount`, `goToCart`, `openBurgerMenu`, `logoutFromSideMenu` |
| `ProductDetailPage` | `/inventory-item.html` | `getProductName`, `getPrice`, `getDescription`, `addToCart`, `backToProducts` |
| `CartPage` | `/cart.html` | `getCartItemNames`, `getCartItemCount`, `removeItem`, `isCartBadgeVisible`, `proceedToCheckout`, `continueShopping` |
| `CheckoutPage` | `/checkout-step-one.html` `/checkout-step-two.html` | `enterShippingInfo`, `continueCheckout`, `cancelCheckout`, `getItemNames`, `getTotalPrice`, `finishOrder`, `getConfirmationMessage`, `getErrorMessage` |

---

### Utilities — `src/main`

**`SelenideHelper.getOptionalText(cssSelector)`** returns element text or `""` if the element is absent or hidden. Used by `LoginPage.getErrorMessage()` to avoid throwing when no error banner is present.

**`ScreenshotNamer.buildName(className, methodName)`** generates the filename pattern `{ClassName}_{methodName}_{timestamp}.png` used by `ScreenshotWatcher`.

---

### Test Infrastructure — `src/test`

**`BaseTest`** is the parent class for every test class. Before each test it:
1. Calls `BrowserConfig.applyBrowserOptions()` to apply Chrome flags
2. Registers the `AllureSelenide` listener (attaches screenshots/page source to Allure on Selenide assertion failures)
3. Opens the base URL via `open("/")`
4. Instantiates all five page objects as `protected` fields (`loginPage`, `productsPage`, `productDetailPage`, `cartPage`, `checkoutPage`)

After each test it removes the Allure listener. It also provides the shared `loginAs(TestUser)` helper so no test class duplicates the login sequence.

**`ScreenshotWatcher`** is a JUnit 5 `TestWatcher` extension registered on `BaseTest` via `@ExtendWith`. It owns the WebDriver lifecycle — `BaseTest.tearDown()` deliberately does NOT call `closeWebDriver()`. Instead:
- `testFailed` → captures a PNG via the WebDriver API (catches both Selenide and AssertJ failures), attaches it to the Allure report, writes it to `target/screenshots/{ClassName}_{method}_{timestamp}.png`, then closes the driver
- `testSuccessful` / `testAborted` → just closes the driver

---

### Test Data — `src/test`

**`TestUser`** is an enum with three constants — `STANDARD`, `LOCKED_OUT`, `INVALID` — each carrying a `username` and `password` field. Used by `BaseTest.loginAs()` and directly in test classes.

**`TestDataProvider`** is the single source of truth for all test data. It has two kinds of members:

- **Constants** — `VALID_USERNAME`, `VALID_PASSWORD`, `INVALID_USERNAME`, `INVALID_PASSWORD`, `WRONG_PASSWORD`, `VALID_SHIPPING`, `MISSING_FIRST_NAME`, `MISSING_LAST_NAME`, `MISSING_POSTAL_CODE`
- **`@MethodSource` providers** — static methods returning `Stream<Arguments>` or `Stream<T>` wired to `@ParameterizedTest`

| Provider method | Used by | Cases |
|---|---|---|
| `invalidCredentialRows()` | `LoginTest` | 3 wrong-credential combos |
| `whitespaceUsernames()` | `LoginTest` | 3 whitespace boundary inputs |
| `maliciousUsernames()` | `LoginTest` | 5 injection/special-char inputs |
| `invalidShippingRows()` | `CheckoutTest` | 3 empty-field boundary cases |
| `nameSortOptions()` | `ProductsTest` | A-Z and Z-A sort |
| `priceSortOptions()` | `ProductsTest` | low-high and high-low sort |
| `cartItemCounts()` | `CartTest` | boundary counts 1, 2, 3, 6 |

---

### Tests — `src/test`

| Class | Tag | Covers |
|---|---|---|
| `LoginTest` | `smoke` | Valid login, logout, invalid credentials (parameterised), empty fields, whitespace boundary, injection edge cases, locked-out user, error dismissal |
| `ProductsTest` | `smoke` | Product count, detail page name/price/description, add-to-cart badge, all four sort options (parameterised), badge boundary (1 and 6 items), remove clears badge |
| `CartTest` | `regression` | Single/multiple add, remove item, badge visibility, continue shopping, item count boundary (parameterised: 1/2/3/6), middle-item removal isolation |
| `CheckoutTest` | `regression` | Form fields visible, order summary with price, confirmation message, empty-field validation (parameterised), cancel navigation, multi-item summary |

---

## How the Layers Connect

```
selenide.properties  ──auto-read by Selenide──►  Configuration fields (baseUrl, headless, timeout)
                                                          │
                                                          ▼
                                               BrowserConfig.applyBrowserOptions()
                                                          │ called by BaseTest.setUp()
                                                          ▼
                                               BaseTest  (@BeforeEach / @AfterEach)
                                                          │ instantiates page objects, opens browser
                                                          │ extended by all test classes
                                                          ▼
                              Test classes  (LoginTest / ProductsTest / CartTest / CheckoutTest)
                                                          │ call page object methods
                                                          │ use TestDataProvider for all data
                                                          ▼
                              Page Objects  (src/main — LoginPage / ProductsPage / etc.)
                                                          │ use $() / $$() to drive the browser
                                                          │ LoginPage uses SelenideHelper.getOptionalText()
                                                          │ CartPage / ProductsPage use BrowserConfig selectors
                                                          │ CheckoutPage accepts ShippingInfo
                                                          ▼
                                               Selenide → ChromeDriver → Headless Chrome
                                                          │
                                                          ▼
                                               https://www.saucedemo.com

Failure path:
ScreenshotWatcher ──► captures PNG ──► Allure attachment + target/screenshots/
ScreenshotNamer   ──► builds filename for ScreenshotWatcher
```

---

## Running the Tests

```bash
# All tests
mvn verify

# Smoke suite only
mvn verify -Dgroups=smoke

# Regression suite only
mvn verify -Dgroups=regression

# Generate and open Allure report
mvn allure:serve

# Run in Docker
docker build -t saucedemo-tests .
docker run --rm saucedemo-tests
```
