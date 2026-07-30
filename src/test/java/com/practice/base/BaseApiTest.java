package com.practice.base;

import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.Playwright;
import com.practice.config.ConfigReader;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

/**
 * Base class for API tests. A single {@link APIRequestContext}, scoped to
 * {@code api.base.url}, is shared across all tests in a class since it is
 * cheap to reuse and keeps requests (and cookies, if any) isolated from the
 * browser contexts used by UI tests.
 */
public abstract class BaseApiTest {

    protected static Playwright playwright;
    protected static APIRequestContext apiRequestContext;

    @BeforeAll
    static void createApiContext() {
        playwright = Playwright.create();
        apiRequestContext = playwright.request().newContext(
                new APIRequest.NewContextOptions().setBaseURL(ConfigReader.apiBaseUrl()));
    }

    @AfterAll
    static void disposeApiContext() {
        apiRequestContext.dispose();
        playwright.close();
    }
}
