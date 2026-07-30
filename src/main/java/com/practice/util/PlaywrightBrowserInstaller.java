package com.practice.util;

import com.microsoft.playwright.CLI;

import java.io.IOException;

/**
 * Entry point used to run Playwright's own CLI (e.g. to install browser
 * binaries) through Maven, without installing the Node.js version of
 * Playwright.
 *
 * <p>Usage:
 * <pre>
 * mvn exec:java -Dexec.mainClass="com.practice.util.PlaywrightBrowserInstaller" -Dexec.args="install --with-deps"
 * </pre>
 */
public final class PlaywrightBrowserInstaller {

    private PlaywrightBrowserInstaller() {
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        CLI.main(args);
    }
}
