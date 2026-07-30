package com.practice.ui.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.practice.config.ConfigReader;

public class HomePage extends BasePage {

    private final Locator homeNavLink;
    private final Locator slogan;

    public HomePage(Page page) {
        super(page);
        this.homeNavLink = page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Home"));
        this.slogan = page.getByText("Full-Fledged practice website for Automation Engineers").first();
    }

    public HomePage open() {
        page.navigate(ConfigReader.baseUrl());
        return this;
    }

    public boolean isLoaded() {
        return homeNavLink.isVisible() && slogan.isVisible();
    }
}
