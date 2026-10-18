package com.acme.wo;

import com.microsoft.playwright.*;
import com.microsoft.playwright.junit.UsePlaywright;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.RequestOptions;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

@UsePlaywright
public class UITests {

    @Test
    void basicTest(Page page) {
        page.navigate("https://playwright.dev/");

        assertThat(page).hasTitle(Pattern.compile("Playwright"));
    }

    @Test
    void basicTest2(Page page, APIRequestContext context) {
        var options = RequestOptions.create().setIgnoreHTTPSErrors(true);

        var response = context.get("https://192.168.1.173:8443/", options);
        //page.navigate("https://192.168.1.173:8443/");

        assertThat(response).isOK();
    }

    @Test
    void basicTest3() {
        Playwright.CreateOptions options = new Playwright.CreateOptions();
        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions();

        try (Browser browser = Playwright.create(options).chromium().launch(launchOptions)) {
            Browser.NewContextOptions newContextOptions = new Browser.NewContextOptions();
            newContextOptions.ignoreHTTPSErrors = true;
            BrowserContext context = browser.newContext(newContextOptions);

            Page page = context.newPage();
            page.navigate("https://192.168.1.173:8443/");
            assertThat(page).hasTitle(Pattern.compile("Training"));

            var locator = page.getByRole(AriaRole.ARTICLE);

            assertThat(page
                    .getByRole(AriaRole.ARTICLE))
                    .hasCount(2);

            page.getByRole(AriaRole.ARTICLE).filter(new Locator.FilterOptions().setHasText("Force & Reactive"))
                    .getByRole(AriaRole.BUTTON,
                            new Locator.GetByRoleOptions().setName("START WORKOUT"))
                    .click();

            assertThat(page.getByRole(AriaRole.BUTTON).filter(new Locator.FilterOptions().setHasText("Start"))).hasCount(1);
            assertThat(page.getByRole(AriaRole.BUTTON).filter(new Locator.FilterOptions().setHasText("Start"))).isVisible();
        }
    }
    //192.168.1.173
}
