# Selenide vs Selenium — What This Project Does Differently

This document explains the concrete advantages Selenide provides over raw Selenium in this project. For each point it shows what the equivalent Selenium code would look like, then what was done here with Selenide, and which file in the project contains that implementation.

---

## 1. No explicit waits anywhere

In Selenium, every interaction with a dynamic element requires a manual wait. Forgetting one causes flaky tests.

**Selenium approach:**
```java
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
WebElement element = wait.until(
    ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".inventory_list"))
);
```

**Selenide in this project:**
```java
// src/test/java/org/example/tests/LoginTest.java
$(".inventory_list").shouldBe(Condition.visible);

// src/main/java/org/example/pages/CheckoutPage.java
$(ERROR_MESSAGE).shouldBe(Condition.visible).getText();
```

Selenide polls automatically up to the configured `selenide.timeout` (8 seconds, set in `src/test/resources/selenide.properties`). Every `$()` call, every `.click()`, every `.setValue()` waits for the element to be in the right state before acting. There is not a single `Thread.sleep()` or `WebDriverWait` in this codebase.

---

## 2. Concise selectors with `$()` and `$$()`

Selenium requires verbose `driver.findElement(By.cssSelector(...))` calls everywhere.

**Selenium approach:**
```java
driver.findElement(By.cssSelector("#user-name")).sendKeys("standard_user");
driver.findElement(By.cssSelector("#password")).sendKeys("secret_sauce");
driver.findElement(By.cssSelector("#login-button")).click();
```

**Selenide in this project:**
```java
// src/main/java/org/example/pages/LoginPage.java
public LoginPage enterUsername(String username) {
    $(USERNAME_INPUT).setValue(username);   // USERNAME_INPUT = "#user-name"
    return this;
}

public void submit() {
    $(LOGIN_BUTTON).click();               // LOGIN_BUTTON = "#login-button"
}
```

`$()` is a static import from `Selenide`. It accepts any CSS selector and returns a `SelenideElement` — a smart wrapper that adds auto-waiting, fluent assertions, and scoped child lookups. `$$()` returns an `ElementsCollection` for multi-element operations and is used throughout `ProductsPage`, `CartPage`, and `CheckoutPage`.

---

## 3. Scoped child element lookups

In Selenium, finding a child element inside a parent requires chaining `findElement` calls or writing complex XPath.

**Selenium approach:**
```java
List<WebElement> rows = driver.findElements(By.cssSelector(".cart_item"));
for (WebElement row : rows) {
    if (row.findElement(By.cssSelector(".inventory_item_name")).getText().equals(name)) {
        row.findElement(By.cssSelector("[data-test^='remove']")).click();
        break;
    }
}
```

**Selenide in this project:**
```java
// src/main/java/org/example/pages/CartPage.java
public void removeItem(String name) {
    $$(CART_ITEM_ROW).findBy(Condition.text(name)).$(REMOVE_BUTTON).click();
}
```

`$$(selector).findBy(condition)` finds the first matching element in a collection. The `.$(childSelector)` call then scopes the child lookup to that specific row. One readable line instead of a loop. The same pattern is used in `ProductsPage.addItemToCartByName()`.

---

## 4. Built-in text collection from element lists

In Selenium, collecting text from a list of elements requires iterating and building a list manually.

**Selenium approach:**
```java
List<WebElement> elements = driver.findElements(By.cssSelector(".inventory_item_name"));
List<String> names = new ArrayList<>();
for (WebElement el : elements) {
    names.add(el.getText());
}
```

**Selenide in this project:**
```java
// src/main/java/org/example/pages/ProductsPage.java
public List<String> getProductNames() {
    return $$(PRODUCT_NAME).texts();
}

// src/main/java/org/example/pages/CartPage.java
public List<String> getCartItemNames() {
    return $$(CART_ITEM_NAME).texts();
}

// src/main/java/org/example/pages/CheckoutPage.java
public List<String> getItemNames() {
    return $$(SUMMARY_ITEM_NAME).texts();
}
```

`ElementsCollection.texts()` returns a `List<String>` of the visible text of every matching element in one call. Used in three page objects across the project.

---

## 5. Fluent assertions on elements

In Selenium, asserting element state requires reading a property and asserting separately, with no built-in waiting.

**Selenium approach:**
```java
// No waiting — can fail if the element hasn't appeared yet
WebElement badge = driver.findElement(By.cssSelector(".shopping_cart_badge"));
assertTrue(badge.isDisplayed());
```

**Selenide in this project:**
```java
// src/main/java/org/example/pages/CheckoutPage.java
public String getErrorMessage() {
    // Waits up to 8s for the error to appear, then reads its text
    return $(ERROR_MESSAGE).shouldBe(Condition.visible).getText();
}

// src/test/java/org/example/tests/LoginTest.java
$(".inventory_list").shouldBe(Condition.visible);
```

`shouldBe(Condition.visible)` waits for the element to become visible (up to the timeout) and throws a descriptive `ElementNotFound` exception with a screenshot if it does not. The assertion and the wait are the same operation.

---

## 6. Automatic screenshot on failure — no custom code in tests

In Selenium, capturing a screenshot on test failure requires a custom `TestRule` or `@AfterEach` that calls `((TakesScreenshot) driver).getScreenshotAs(...)` and handles file I/O manually in every test class.

**Selenium approach:**
```java
// Repeated in every test class
@AfterEach
void tearDown(TestInfo info) {
    if (testFailed) {
        File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        Files.copy(src.toPath(), Paths.get("screenshots/" + info.getDisplayName() + ".png"));
    }
    driver.quit();
}
```

**Selenide in this project:**

`src/test/java/org/example/ScreenshotWatcher.java` implements JUnit 5's `TestWatcher`. It is registered once on `BaseTest` via `@ExtendWith(ScreenshotWatcher.class)` and fires automatically for every test class that extends `BaseTest`. On failure it:
1. Captures the PNG via the WebDriver API
2. Attaches it inline to the Allure report
3. Writes it to `target/screenshots/{ClassName}_{method}_{timestamp}.png` using `ScreenshotNamer.buildName()` from `src/main/java/org/example/util/ScreenshotNamer.java`

Additionally, `AllureSelenide` (registered in `src/test/java/org/example/BaseTest.java` `setUp()`) intercepts Selenide assertion failures and attaches screenshots automatically — no extra code in any test class.

---

## 7. `selenide.properties` replaces boilerplate driver setup

In Selenium, every test suite needs explicit driver initialisation code.

**Selenium approach:**
```java
@BeforeEach
void setUp() {
    ChromeOptions options = new ChromeOptions();
    options.addArguments("--headless=new");
    driver = new ChromeDriver(options);
    driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(8));
    driver.get("https://www.saucedemo.com/");
}
```

**Selenide in this project:**

`src/test/resources/selenide.properties`:
```properties
selenide.baseUrl=https://www.saucedemo.com
selenide.browser=chrome
selenide.headless=true
selenide.timeout=8000
```

Selenide reads this file automatically at startup. `src/test/java/org/example/BaseTest.java` `setUp()` only needs:
```java
BrowserConfig.applyBrowserOptions(); // adds --no-sandbox etc. for CI (BrowserConfig.java)
open("/");                            // opens baseUrl + "/"
```

`src/main/java/org/example/config/BrowserConfig.java` reads `Configuration.headless` (already set by Selenide from the properties file) and conditionally adds the CI-specific Chrome flags. No `new ChromeDriver()`, no `driver.manage().timeouts()`, no URL string in test code.

---

## 8. `setValue()` vs `sendKeys()` — clears before typing

In Selenium, `sendKeys()` appends to whatever is already in the field. Clearing first is a separate step.

**Selenium approach:**
```java
WebElement field = driver.findElement(By.cssSelector("#user-name"));
field.clear();
field.sendKeys("standard_user");
```

**Selenide in this project:**
```java
// src/main/java/org/example/pages/LoginPage.java
public LoginPage enterUsername(String username) {
    $(USERNAME_INPUT).setValue(username);  // clears and types atomically
    return this;
}

// src/main/java/org/example/pages/CheckoutPage.java
public void enterShippingInfo(ShippingInfo info) {
    $(FIRST_NAME).setValue(info.firstName());
    $(LAST_NAME).setValue(info.lastName());
    $(POSTAL_CODE).setValue(info.postalCode());
}
```

`setValue()` clears the field and types the new value atomically. Used in `LoginPage` and `CheckoutPage`. No `.clear()` calls anywhere in the codebase.

---

## Summary

| Concern | Selenium | Selenide — project file |
|---|---|---|
| Waiting for elements | Manual `WebDriverWait` everywhere | Auto — built into every `$()` · `selenide.properties` |
| Selector syntax | `driver.findElement(By.cssSelector(...))` | `$(selector)` / `$$(selector)` · all `pages/*.java` |
| Scoped child lookup | Loop + nested `findElement` | `$$(rows).findBy(condition).$(child)` · `CartPage.java`, `ProductsPage.java` |
| Collecting element text | Manual loop | `$$(selector).texts()` · `ProductsPage`, `CartPage`, `CheckoutPage` |
| Asserting element state | Read property + assert separately | `shouldBe(Condition.visible)` · `CheckoutPage.java`, `LoginTest.java` |
| Screenshot on failure | Custom `@AfterEach` in every class | `ScreenshotWatcher.java` + `AllureSelenide` in `BaseTest.java` |
| Driver setup | `new ChromeDriver()` + timeouts in code | `selenide.properties` + `BrowserConfig.java` |
| Typing into fields | `.clear()` + `.sendKeys()` | `.setValue()` · `LoginPage.java`, `CheckoutPage.java` |
