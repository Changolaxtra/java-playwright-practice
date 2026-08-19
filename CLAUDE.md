# CLAUDE.md

This file guides Claude Code (and anyone else touching this repo) on how to work in this project.

## Project overview

A practice project for testing **[automationexercise.com](https://automationexercise.com/)** (UI) and its API with **Java 21**, **Maven**, and **Playwright** (`com.microsoft.playwright`). Playwright drives both the browser (UI tests) and raw HTTP calls (API tests via `APIRequestContext`), so there is a single toolchain for both kinds of testing.

Test targets, configured in `config.properties`:
- UI: https://automationexercise.com — test cases enumerated at https://automationexercise.com/test_cases
- API: https://automationexercise.com/api — test cases enumerated at https://automationexercise.com/api_list

This is a learning project: test cases are documented one-by-one before they're implemented (see "Test case workflow" below). Don't implement a test case's logic unless asked — the point is for the project owner to practice writing it.

## Commands

```bash
mvn test                                   # run all tests
mvn test -Dtest=Tc01RegisterUserTest       # run a single test class, once it exists
mvn test -Dheadless=false                  # override config.properties at runtime
mvn compile                                 # compile main sources only

# one-time: install Playwright's browser binaries
mvn exec:java -Dexec.mainClass=com.practice.util.PlaywrightBrowserInstaller -Dexec.args=install
```

## Project structure

```
src/main/java/com/practice/
  config/     ConfigReader - reads config.properties, overridable via -Dkey=value
  ui/pages/   Page objects
  api/        BaseApiClient + API clients (api/clients) + response/request models (api/model)
  util/       PlaywrightBrowserInstaller

src/test/java/com/practice/
  base/               BaseUiTest / BaseApiTest (JUnit 5 lifecycle)
  ui/testcases/        tcNN_<slug>/README.md per UI test case (test_cases page)
  api/testcases/       apiNN_<slug>/README.md per API test case (api_list page)
```

Keep this split: **main** holds reusable framework code (page objects, API clients, config), **test** holds only test classes and test-specific fixtures. This lets page objects/API clients be reused from multiple test classes without living in test scope.

## Test case workflow

Each `testcases/tcNN_<slug>/` (UI) or `testcases/apiNN_<slug>/` (API) folder documents exactly one test case from the site (`README.md`: description, preconditions, steps, expected result, and a `- [ ] Not implemented` status line).

When implementing a test case:
1. Read its `README.md` for the steps and expected result — don't re-derive them from the live site.
2. Add the test class in the **same folder** as the README (e.g. `ui/testcases/tc01_register_user/RegisterUserTest.java`, `api/testcases/api11_create_user_account/CreateUserAccountTest.java`), extending `BaseUiTest` or `BaseApiTest`.
3. Reuse or add page objects (`ui/pages`) / API clients (`api/clients`) under `src/main/java` rather than duplicating locators or endpoint calls across test cases — several UI test cases share flows (e.g. signup, add-to-cart, checkout) and several API test cases share the same account fixture (test case 11, Create User Account).
4. Flip the README's status line to `- [x] Implemented` once the test class is done and passing.

Don't write the test class yourself unless explicitly asked — flag which page object/API client would be needed and let the project owner implement the test body as practice.

## Java conventions (Java 21)

- Prefer `record` for immutable data (API payloads, DTOs) under `api/model`.
- Use `var` for obvious local types; use explicit types when it aids readability (e.g. Playwright's `Locator`, `APIResponse`).
- Use switch expressions (`->` form) over classic `switch` statements, e.g. `BaseUiTest.resolveBrowserType()`.
- Favor composition over inheritance beyond the single base-class level already in use (`BasePage`, `BaseApiClient`, `BaseUiTest`, `BaseApiTest`). Don't build deep class hierarchies.
- No Lombok — the codebase is small enough that plain constructors/accessors (or records) are clearer than annotation-generated code.
- Package by feature/layer (`ui.pages`, `api.clients`, `api.model`), not by type-only buckets.
- Final fields, constructor injection, no field injection frameworks — this project has no DI container.

## Playwright / testing conventions

**Locators**
- Prefer user-facing locators: `getByRole`, `getByLabel`, `getByText`, `getByTestId`. Avoid brittle CSS/XPath selectors tied to implementation details (classes, DOM depth).
- Never use `Thread.sleep` for waits. Playwright's actions and web-first assertions auto-wait; if you truly need to wait for a condition, use `page.waitForCondition(...)` or an explicit `Locator` assertion instead.
- Declare locators once (typically as fields in a page object, built from the `Page` passed to the constructor) rather than re-querying strings inline in tests.

**Page Object Model**
- One page object per logical page/component. It exposes user intentions (`login(user, pass)`, `isErrorVisible()`), never raw `Locator`s, to test classes.
- Page objects extend `BasePage` and take a `Page` in their constructor. They must not read `ConfigReader` for anything except the base URL to navigate to.
- Test classes orchestrate page objects and assert; they should read like a script of user actions, not contain Playwright API calls directly.

**Test isolation & lifecycle**
- One `Browser` per test class (`@BeforeAll`/`@AfterAll` in `BaseUiTest`), but a fresh `BrowserContext` per test method (`@BeforeEach`/`@AfterEach`) — contexts are cheap and give each test isolated cookies/storage/cache, so tests never leak state into each other.
- API tests share one `APIRequestContext` per test class scoped to `api.base.url`; if a test needs isolated auth/session state, create a dedicated context for that test instead of mutating the shared one.
- Don't rely on test execution order. Each test must set up its own preconditions (via API calls or UI actions), not depend on a previous test having run.

**API testing**
- `api.base.url` is `https://automationexercise.com/api/` (note the trailing slash) and endpoint path constants in API clients must NOT start with `/` (e.g. `"productsList"`, not `"/productsList"`). Playwright's `APIRequestContext` resolves URLs per the WHATWG URL spec: a path starting with `/` is treated as absolute from the domain root and silently drops the `/api` segment of the base URL, redirecting requests to the plain website instead of the API (confirmed while building the `api00_options_request` smoke test — the resulting URL was `https://automationexercise.com/productsList`, which 302-redirected instead of hitting the API). Always check `response.url()` if a request behaves unexpectedly.
- Add one client class per resource/endpoint group under `api/clients`, extending `BaseApiClient`. Expose intention-revealing methods (`createAccount(account)`, `verifyLogin(email, password)`) instead of leaking raw `post("createAccount", ...)` calls into tests.
- Model request/response bodies as records under `api/model`. Deserialize with `BaseApiClient.parse(response, Type.class)` (Gson) rather than manual string parsing.
- automationexercise.com's API always returns HTTP 200 and encodes the real result in the body's `responseCode`/`message` fields (see each `apiNN_.../README.md` under `api/testcases`) — assert on the body, not just the transport status code.
- Several API test cases depend on the account created in API test case 11 (`createAccount`); reuse a shared setup instead of duplicating account-creation logic per test class.

**Assertions**
- Use AssertJ (`assertThat(...)`) for all assertions — fluent, readable failure messages.
- For UI tests, prefer Playwright's own web-first assertions (`PlaywrightAssertions.assertThat(locator).isVisible()`, etc.) when asserting on live page state, since they retry/auto-wait; use AssertJ for everything else (URLs, titles, plain values, API responses).

**Configuration**
- All environment-specific values (base URLs, headless flag, browser choice) live in `src/main/resources/config.properties`, read through `ConfigReader`. Never hardcode a URL or credential in a test or page object.
- Any property can be overridden per-run with `-Dkey=value` without editing the file — use this for local debugging (`-Dheadless=false`) or pointing at a different environment.
- Never commit real secrets/credentials to `config.properties`; use `-D` overrides or environment-specific files that are gitignored if a real target requires auth.

**Naming & structure**
- Test classes: `<Subject>Test` (e.g. `RegisterUserTest`, `VerifyLoginValidTest`), placed inside their `tcNN_<slug>` folder.
- Test methods: descriptive camelCase names stating the scenario and expectation (`registerUserCreatesAndDeletesAccount`), plus a `@DisplayName` for the human-readable version when the method name alone isn't self-explanatory.
- Structure test bodies as given/when/then via blank lines or comments only when it isn't already obvious from short, sequential statements.

## What to avoid

- Don't add hard waits or sleeps to work around flakiness — fix the locator/assertion instead.
- Don't reach for CSS/XPath selectors when a role/label/test-id locator is available.
- Don't put Playwright calls directly in test classes — route them through a page object or API client.
- Don't add new dependencies (assertion libraries, HTTP clients, JSON libraries) casually — Playwright + JUnit 5 + AssertJ + Gson already cover UI, API, assertions and JSON for this project's scope.
