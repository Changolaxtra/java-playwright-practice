package com.practice.ui.pages;

import com.microsoft.playwright.Page;

/**
 * Common behaviour shared by every page object. Page objects should expose
 * user-facing actions and state (e.g. {@code login(user, pass)},
 * {@code isErrorVisible()}) and hide Playwright locators from test classes.
 */
public abstract class BasePage {

    protected final Page page;

    protected BasePage(Page page) {
        this.page = page;
    }

    public String title() {
        return page.title();
    }

    public String url() {
        return page.url();
    }
}
