---
name: check
description: Review an already-implemented test case (UI or API) against this project's Playwright/Java best practices and suggest improvements as text only — never edits the code. Use when the user asks to check, review, or critique a test case implementation, given a TCXX + UI/API identifier.
user-invocable: true
allowed-tools:
  - Read
  - Grep
  - Glob
  - Bash(find *)
  - Bash(ls *)
---

# /check — Review a test case implementation

This project is a **learning project** (see `CLAUDE.md`). The whole point of
this skill is to help the project owner practice: report findings as text,
never touch the code yourself. Do not use `Edit` or `Write` in this skill
under any circumstances, even if the fix looks trivial.

Arguments passed: `$ARGUMENTS` — expected to contain a test case number
(`TC01`, `tc1`, `01`, ...) and a suite identifier (`UI` or `API`), in any
order/casing.

## 1. Parse arguments

- Extract the suite: `ui` or `api` (case-insensitive). If neither/both appear
  ambiguously, ask the user to clarify and stop.
- Extract the test case number and zero-pad to two digits (`1` → `01`,
  `tc5` → `05`).
- If either piece is missing or unparseable, ask the user for the missing
  part and stop — don't guess.

## 2. Locate the test case folder

Base directory: `src/test/java/com/practice/<suite>/testcases/`

Find the folder matching `tc<NN>_*` under that directory (there is exactly
one per number, e.g. `tc01_register_user`, `tc07_verify_login_valid`).

- No match → tell the user this TC number doesn't exist for that suite, list
  the available `tcNN_*` folders in that suite so they can pick, and stop.
- Multiple matches → this shouldn't happen; report it and stop.

## 3. Check implementation status

List files in the folder. Besides `README.md`, is there a `*.java` test
class?

- **No test class present** → this test case isn't implemented yet. Tell the
  user there's nothing to review, and suggest running `/hint` instead to get
  a starting point. Stop here — do not invent a review of non-existent code.
- **Test class present** → continue.

## 4. Gather context

- Read the folder's `README.md` for the documented steps, preconditions, and
  expected result — the implementation should match this, not some
  re-derived version of the live site.
- Read the test class.
- Follow references to page objects (`src/main/java/com/practice/ui/pages`)
  or API clients/models (`src/main/java/com/practice/api/clients`,
  `src/main/java/com/practice/api/model`) used by the test, and read those
  too. A review of the test class alone, ignoring the page object/API client
  it drives, misses half the picture.
- Read the relevant base class (`BaseUiTest`/`BaseApiTest`, `BasePage`,
  `BaseApiClient`) only if something about lifecycle/inheritance looks off —
  don't re-read it every time for no reason.

## 5. Review against project conventions

Check the code against `CLAUDE.md` in the repo root (Java conventions,
Playwright/testing conventions, naming & structure, what-to-avoid). Focus
especially on:

- **Locators**: user-facing (`getByRole`/`getByLabel`/`getByText`/
  `getByTestId`) vs brittle CSS/XPath; locators declared once as page-object
  fields, not re-queried inline in the test.
- **No hard waits**: no `Thread.sleep`; reliance on auto-waiting/web-first
  assertions.
- **Page Object Model boundary**: test class contains no direct Playwright
  API calls or raw HTTP calls — everything routed through a page object or
  API client exposing intention-revealing methods.
- **Assertions**: Playwright web-first assertions (`PlaywrightAssertions.
  assertThat(locator)...`) for live UI state; AssertJ (`assertThat(...)`) for
  everything else (URLs, values, API responses).
- **API-specific**: endpoint path constants don't start with `/`; assertions
  check the response body's `responseCode`/`message`, not just HTTP 200;
  shared account fixture (TC11 `createAccount`) reused rather than
  duplicated if this test depends on a logged-in/created user.
- **Test isolation**: doesn't depend on other tests having run; sets up its
  own preconditions.
- **Naming**: class `<Subject>Test`, descriptive camelCase method names,
  `@DisplayName` where the method name alone isn't self-explanatory.
- **Config**: no hardcoded URLs/credentials — goes through `ConfigReader`.
- **README status**: does the `- [ ]`/`- [x]` status line in `README.md`
  match reality? Flag a mismatch, but don't fix it yourself.

## 6. Report

Present findings as a plain-text review, grouped by convention area. For
each finding give the `file:line` and a one-line explanation of what's off
and what the convention says instead. Call out genuine strengths too, not
just problems — this is a review, not just a nitpick list.

End with an explicit reminder: **these are suggestions only** — nothing was
changed, and applying (or rejecting) them is left to the project owner as
practice.
