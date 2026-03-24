# Requirements Document

## Introduction

This project automates UI testing for the Swag Labs web application (https://www.saucedemo.com/) using Selenide, a Java-based framework built on top of Selenium. The automation suite covers core user flows including login, product browsing, cart management, and checkout. It follows the Page Object Model (POM) pattern for maintainability, supports headless browser execution, runs in Docker containers, integrates with CI/CD pipelines, and generates test reports with screenshots on failure.

## Glossary

- **Test_Suite**: The collection of automated test cases organized into smoke and regression groups
- **Page_Object**: A class that encapsulates the UI elements and interactions for a single application page
- **Login_Page**: The Page Object representing the Swag Labs login screen at https://www.saucedemo.com/
- **Products_Page**: The Page Object representing the product listing page after successful login
- **Product_Detail_Page**: The Page Object representing the individual product detail view
- **Cart_Page**: The Page Object representing the shopping cart page
- **Checkout_Page**: The Page Object representing the multi-step checkout flow
- **Selenide**: The Java test automation framework built on Selenium used for browser interaction and assertions
- **POM**: Page Object Model — a design pattern that separates page UI logic from test logic
- **CI_Pipeline**: The continuous integration pipeline (GitHub Actions) that triggers test execution on code commits
- **Test_Reporter**: The component responsible for generating HTML test execution reports
- **Screenshot_Capture**: The mechanism that captures browser screenshots when a test fails
- **Docker_Container**: The isolated execution environment in which the Test_Suite runs
- **Headless_Browser**: A browser instance running without a graphical UI, used for CI execution
- **Smoke_Suite**: A subset of tests covering critical happy-path scenarios (login, product page)
- **Regression_Suite**: A broader set of tests covering cart and checkout functionality

---

## Requirements

### Requirement 1: Project Setup and Dependencies

**User Story:** As a QA engineer, I want a properly configured Maven project with Selenide and all required dependencies, so that I can write and run automated UI tests without manual environment setup.

#### Acceptance Criteria

1. THE Test_Suite SHALL declare Selenide, JUnit 5, and Allure (or Surefire) reporting dependencies in `pom.xml`
2. THE Test_Suite SHALL include a Maven Surefire or Failsafe plugin configuration that enables running tests via `mvn test` or `mvn verify`
3. WHEN the project is built for the first time, THE Test_Suite SHALL resolve all dependencies from Maven Central without requiring manual downloads
4. THE Test_Suite SHALL include a `selenide.properties` or equivalent configuration file that sets the base URL to `https://www.saucedemo.com/`
5. WHERE headless mode is enabled, THE Test_Suite SHALL configure the browser to run without a graphical interface using ChromeOptions or FirefoxOptions

---

### Requirement 2: Page Object Model Structure

**User Story:** As a QA engineer, I want Page Object classes for each key page of Swag Labs, so that test logic is separated from UI interaction code and tests remain maintainable.

#### Acceptance Criteria

1. THE Login_Page SHALL expose methods to enter a username, enter a password, and submit the login form
2. THE Products_Page SHALL expose methods to retrieve the list of displayed product names and to select a product by name
3. THE Product_Detail_Page SHALL expose methods to retrieve the product name, price, and description, and to add the product to the cart
4. THE Cart_Page SHALL expose methods to retrieve cart item names, remove an item by name, and proceed to checkout
5. THE Checkout_Page SHALL expose methods to enter shipping information, complete the order, and retrieve the order confirmation message
6. THE Login_Page SHALL expose a method to retrieve the displayed error message when login fails
7. WHEN a Page Object method interacts with a UI element, THE Page_Object SHALL use Selenide's `$()` or `$$()` selectors and built-in waiting mechanisms instead of explicit `Thread.sleep()` calls

---

### Requirement 3: Login Functionality Tests

**User Story:** As a QA engineer, I want automated tests for the login flow, so that authentication behavior is continuously validated.

#### Acceptance Criteria

1. WHEN valid credentials (`standard_user` / `secret_sauce`) are submitted, THE Login_Page SHALL navigate the browser to the Products_Page
2. WHEN invalid credentials are submitted, THE Login_Page SHALL display an error message containing the text "Username and password do not match"
3. WHEN the username field is left empty and the form is submitted, THE Login_Page SHALL display an error message containing the text "Username is required"
4. WHEN the password field is left empty and the form is submitted, THE Login_Page SHALL display an error message containing the text "Password is required"
5. WHEN a locked-out user (`locked_out_user`) submits valid credentials, THE Login_Page SHALL display an error message containing the text "Sorry, this user has been locked out"

---

### Requirement 4: Product Browsing Tests

**User Story:** As a QA engineer, I want automated tests for product listing and detail pages, so that product display functionality is continuously validated.

#### Acceptance Criteria

1. WHEN a user is logged in, THE Products_Page SHALL display at least 6 products
2. WHEN a user selects a product by name, THE Product_Detail_Page SHALL display the same product name as selected on the Products_Page
3. WHEN a user selects a product by name, THE Product_Detail_Page SHALL display a price in the format `$X.XX`
4. WHEN a user clicks "Add to cart" on the Product_Detail_Page, THE Products_Page cart icon SHALL display a badge with the count `1` after navigating back

---

### Requirement 5: Cart Functionality Tests

**User Story:** As a QA engineer, I want automated tests for cart operations, so that adding, viewing, and removing items from the cart is continuously validated.

#### Acceptance Criteria

1. WHEN a user adds a product from the Products_Page, THE Cart_Page SHALL display that product's name in the cart item list
2. WHEN a user adds multiple products, THE Cart_Page SHALL display all added product names in the cart item list
3. WHEN a user removes an item from the Cart_Page, THE Cart_Page SHALL no longer display that item's name in the cart item list
4. WHEN all items are removed from the Cart_Page, THE Cart_Page cart badge SHALL not be visible on the page header

---

### Requirement 6: Checkout Flow Tests

**User Story:** As a QA engineer, I want automated tests for the end-to-end checkout process, so that the purchase flow is continuously validated.

#### Acceptance Criteria

1. WHEN a user proceeds to checkout with at least one item in the cart, THE Checkout_Page SHALL display a form requesting first name, last name, and postal code
2. WHEN valid shipping information is submitted, THE Checkout_Page SHALL display an order summary including item names and a total price
3. WHEN the order is completed, THE Checkout_Page SHALL display a confirmation message containing the text "Thank you for your order"
4. WHEN the checkout form is submitted with an empty first name field, THE Checkout_Page SHALL display an error message containing the text "First Name is required"

---

### Requirement 7: Smoke Test Suite

**User Story:** As a QA engineer, I want a dedicated smoke test suite covering critical paths, so that I can quickly validate the application is functional after a deployment.

#### Acceptance Criteria

1. THE Smoke_Suite SHALL include a test that verifies successful login with valid credentials
2. THE Smoke_Suite SHALL include a test that verifies the Products_Page displays at least one product after login
3. WHEN the Smoke_Suite is executed, THE Test_Suite SHALL complete all smoke tests within 3 minutes
4. THE Smoke_Suite SHALL be executable independently from the Regression_Suite via a Maven test group or tag (e.g., JUnit 5 `@Tag("smoke")`)

---

### Requirement 8: Regression Test Suite

**User Story:** As a QA engineer, I want a regression test suite covering cart and checkout flows, so that I can detect regressions in core transactional functionality.

#### Acceptance Criteria

1. THE Regression_Suite SHALL include tests covering all acceptance criteria defined in Requirement 5 (Cart Functionality)
2. THE Regression_Suite SHALL include tests covering all acceptance criteria defined in Requirement 6 (Checkout Flow)
3. THE Regression_Suite SHALL be executable independently from the Smoke_Suite via a Maven test group or tag (e.g., JUnit 5 `@Tag("regression")`)
4. WHEN the Regression_Suite is executed, THE Test_Suite SHALL produce a pass/fail result for each individual test case

---

### Requirement 9: Screenshot Capture on Failure

**User Story:** As a QA engineer, I want screenshots automatically captured when a test fails, so that I can diagnose UI issues without re-running tests manually.

#### Acceptance Criteria

1. WHEN a test fails, THE Screenshot_Capture SHALL save a PNG screenshot of the browser viewport to the `target/screenshots/` directory
2. WHEN a test fails, THE Screenshot_Capture SHALL name the screenshot file using the pattern `{TestClassName}_{testMethodName}_{timestamp}.png`
3. THE Screenshot_Capture SHALL use Selenide's built-in `Screenshots.takeScreenShotAsFile()` or the Selenide listener mechanism rather than a custom WebDriver screenshot implementation

---

### Requirement 10: Test Reporting

**User Story:** As a QA engineer, I want an HTML test report generated after each test run, so that I can review results, pass/fail status, and failure details in a readable format.

#### Acceptance Criteria

1. WHEN the Test_Suite finishes execution, THE Test_Reporter SHALL generate an HTML report in the `target/` directory
2. THE Test_Reporter SHALL include the total number of tests run, the number passed, the number failed, and the number skipped
3. WHEN a test fails, THE Test_Reporter SHALL include the failure message and stack trace in the report
4. WHERE Allure reporting is configured, THE Test_Reporter SHALL embed the failure screenshot into the Allure report for the corresponding failed test

---

### Requirement 11: Docker Containerization

**User Story:** As a QA engineer, I want the test suite to run inside a Docker container, so that tests execute in a consistent, isolated environment regardless of the host machine.

#### Acceptance Criteria

1. THE Docker_Container SHALL be defined by a `Dockerfile` in the project root that uses a base image containing Java and a compatible browser (e.g., `selenium/standalone-chrome`)
2. WHEN the Docker_Container is started with `docker run`, THE Test_Suite SHALL execute all tests and exit with code `0` on full pass or a non-zero code on any failure
3. THE Docker_Container SHALL support passing environment variables to override the target base URL and browser type at runtime
4. THE Docker_Container SHALL mount or copy the `target/` directory output so that reports and screenshots are accessible on the host after the container exits

---

### Requirement 12: CI/CD Pipeline Integration

**User Story:** As a QA engineer, I want tests triggered automatically on every commit, so that regressions are detected as early as possible in the development cycle.

#### Acceptance Criteria

1. THE CI_Pipeline SHALL be defined as a GitHub Actions workflow file located in `.github/workflows/`
2. WHEN a commit is pushed to any branch, THE CI_Pipeline SHALL check out the code and execute the full Test_Suite via `mvn verify`
3. WHEN the Test_Suite execution completes, THE CI_Pipeline SHALL archive the generated HTML report and screenshots as build artifacts
4. WHEN any test fails, THE CI_Pipeline SHALL mark the pipeline build as failed and notify the team via the configured notification channel (e.g., email or Slack webhook)
5. THE CI_Pipeline SHALL run tests using a Headless_Browser to avoid requiring a display server in the CI environment
