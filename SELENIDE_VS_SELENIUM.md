# Selenide vs Selenium — What This Project Does Differently

This document explains the concrete advantages Selenide provides over raw Selenium in this project. For each point, it shows what the equivalent Selenium code would look like, then what was done here with Selenide instead.

---

## 1. No explicit waits anywhere

In Selenium, every interaction with a dynamic element requires a manual wait. Forgetting one causes flaky tests.

**Selenium approach:**
```java
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".inventory_list")));
element.click();
```

**What this project does with Selenide:**
```java
$(".inventory_list").shouldBe(Condition.visible);
$("[data-test='add-to-cart']").click();
```

Selenide polls automatically up to the configured `selenide.timeout` (8 seconds here). Every `$()` call, every `.click()`, every `.setValue()` waits for the element to be in the right state before acting. There is not a single `Thread.sleep()` or `WebDriverWait` in this codebase.

---

## 2. Concise selectors with `$()` and `$$()`

Selenium requires verbose `driver.findElement(By.cssSelector(...))` calls everywhere.

**Selenium approach:**
```java
driver.findElement(By.cssSelector("#user-name")).sendKeys("standard_user");
driver.findElement(By.cssSelector("#password")).sendKeys("secret_sauce");
driver.findElement(By.cssSelector("#login-button")).click();
```

**What this project does with Selenide (`LoginPage.java`):**
```java
$(USERNAME_INPUT).setValue(username);
$(PASSWORD_INPUT).setValue(password);
$(LOGIN_BUTTON).click();
```

`$()` is a static import from `Selenide`. It accepts any CSS selector and returns a `SelenideElement` — a smart wrapper that adds auto-waiting, fluent assertions, and scoped child lookups. `$$()` returns an `ElementsCollection` for multi-element operations.

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

**What this project does with Selenide (`CartPage.java`):**
```java
$$(CART_ITEM_ROW).findBy(Condition.text(name)).$(REMOVE_BUTTON).click();
```

`$$(selector).findBy(condition)` finds the first matching element in a collection. The `.$(childSelector)` call then scopes the child lookup to that specific row. This is one readable line instead of a loop.

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

**What this project does with Selenide (`ProductsPage.java`):**
```java
public List<String> getProductNames() {
    return $$(PRODUCT_NAME).texts();
}
```

`ElementsCollection.texts()` returns a `List<String>` of the visible text of every matching element in one call. Used throughout the page objects for product names, cart item names, and order summary items.

---

## 5. Fluent assertions on elements

In Selenium, asserting element state requires reading a property and asserting separately, with no built-in waiting.

**Selenium approach:**
```java
WebElement badge = driver.findElement(By.cssSelector(".shopping_cart_badge"));
assertTrue(badge.isDisplayed());
assertEquals("2", badge.getText());
```

**What this project does with Selenide (`CheckoutPage.java`):**
```java
public String getErrorMessage() {
    return $(ERROR_MESSAGE).shouldBe(Condition.visible).getText();
}
```

`shouldBe(Condition.visible)` waits for the element to become visible (up to the timeout) and throws a descriptive `ElementNotFound` exception with a screenshot if it does not. The assertion and the wait are the same operation — no separate `WebDriverWait` needed.

---

## 6. Automatic screenshot on failure — no custom code needed

In Selenium, capturing a screenshot on test failure requires a custom `TestRule` or `@AfterEach` that calls `((TakesScreenshot) driver).getScreenshotAs(...)` and handles file I/O manually.

**Selenium approach:**
```java
@AfterEach
void tearDown(TestInfo info) {
    if (testFailed) { // need to track this manually
        File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        Files.copy(screenshot.toPath(), Paths.get("target/screenshots/" + info.getDisplayName() + ".png"));
    }
    driver.quit();
}
```

**What this project does:**

`ScreenshotWatcher.java` implements JUnit 5's `TestWatcher`. It is registered once on `BaseTest` via `@ExtendWith(ScreenshotWatcher.class)` and fires automatically for every test class that extends `BaseTest`. On failure it:
1. Captures the PNG via the WebDriver API
2. Attaches it inline to the Allure report
3. Writes it to `target/screenshots/{ClassName}_{method}_{timestamp}.png`

Additionally, `AllureSelenide` (registered in `BaseTest.setUp()`) intercepts Selenide assertion failures and attaches screenshots automatically to the Allure report — no extra code in any test class.

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

**What this project does:**

`src/test/resources/selenide.properties`:
```properties
selenide.baseUrl=https://www.saucedemo.com
selenide.browser=chrome
selenide.headless=true
selenide.timeout=8000
```

Selenide reads this file automatically at startup. `BaseTest.setUp()` only needs:
```java
BrowserConfig.applyBrowserOptions(); // adds --no-sandbox etc. for CI
open("/");                            // opens baseUrl + "/"
```

No `new ChromeDriver()`, no `driver.manage().timeouts()`, no URL string in test code.

---

## 8. `setValue()` vs `sendKeys()` — clears before typing

In Selenium, `sendKeys()` appends to whatever is already in the field. Clearing first is a separate step.

**Selenium approach:**
```java
WebElement field = driver.findElement(By.cssSelector("#user-name"));
field.clear();
field.sendKeys("standard_user");
```

**What this project does with Selenide:**
```java
$(USERNAME_INPUT).setValue(username);
```

`setValue()` clears the field and types the new value atomically. This is used in `LoginPage`, `CheckoutPage`, and anywhere a form field is filled. No `.clear()` calls anywhere in the codebase.

---

## Summary

| Concern | Selenium | This project (Selenide) |
|---|---|---|
| Waiting for elements | Manual `WebDriverWait` everywhere | Automatic — built into every `$()` call |
| Selector syntax | `driver.findElement(By.cssSelector(...))` | `$(selector)` / `$$(selector)` |
| Scoped child lookup | Loop + nested `findElement` | `$$(rows).findBy(condition).$(child)` |
| Collecting element text | Manual loop | `$$(selector).texts()` |
| Asserting element state | Read property + assert separately | `shouldBe(Condition.visible)` — waits and asserts |
| Screenshot on failure | Custom `@AfterEach` + file I/O | `ScreenshotWatcher` + `AllureSelenide` listener |
| Driver setup | `new ChromeDriver()` + timeouts in code | `selenide.properties` file |
| Typing into fields | `.clear()` + `.sendKeys()` | `.setValue()` |
