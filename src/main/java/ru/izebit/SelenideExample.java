package ru.izebit;


import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.WebDriverRunner;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.UnexpectedAlertBehaviour;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.URL;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;

/**
 * It's an simple example of using Selenide framework.
 * This program checks friend requests on LinkedIn.com and accepts them if they exist.
 * @see <a href="https://www.izebit.ru/2019/07/getting-started-with-selenide.html">read more</a>
 * @author Artem Konovalov
 */

@Slf4j
public class SelenideExample {
    @Parameter(names = {"--username", "-u"}, required = true)
    private String username;
    @Parameter(names = {"--password", "-p"}, required = true)
    private String password;
    @Parameter(names = {"--remote-selenium-url", "-url"})
    private String remoteSeleniumUrl = "http://127.0.0.1:4444/wd/hub";


    public static void main(String[] args) {
        SelenideExample app = new SelenideExample();
        JCommander.newBuilder()
                .addObject(app)
                .build()
                .parse(args);
        app.run();
    }

    private void run() {
        WebDriver webDriver = initWebDriver(remoteSeleniumUrl);
        login(username, password);
        int acceptedRequests = acceptRequests();
        System.out.println(acceptedRequests);
        webDriver.close();
    }


    @SneakyThrows
    private static WebDriver initWebDriver(String seleniumUrl) {
        Configuration.timeout = TimeUnit.SECONDS.toMillis(10);
        Configuration.reportsFolder = Files.createTempDirectory("selenide-build").toAbsolutePath().toString();

        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.setExperimentalOption("useAutomationExtension", false);
        chromeOptions.addArguments(
                "disable-infobars",
                "--disable-extensions",
                "--disable-dev-shm-usage",
                "--no-sandbox",
                "--incognito",
                "--window-size=1920,1080",
                "--headless",
                "--disable-gpu",
                "--ignore-certificate-errors");

        DesiredCapabilities capabilities = DesiredCapabilities.chrome();
        capabilities.setCapability(ChromeOptions.CAPABILITY, chromeOptions);
        capabilities.setCapability(CapabilityType.UNEXPECTED_ALERT_BEHAVIOUR, UnexpectedAlertBehaviour.ACCEPT);
        WebDriver driver = new RemoteWebDriver(new URL(seleniumUrl), capabilities);
        WebDriverRunner.setWebDriver(driver);
        return driver;
    }

    private static void login(String username, String password) {
        open("http://www.linkedin.com");
        $$(By.tagName("a"))
                .filter(attribute("data-tracking-control-name", "guest_homepage-basic_nav-header-signin"))
                .first()
                .waitUntil(appear, TimeUnit.SECONDS.toMillis(30))
                .click();

        $(By.id("username")).setValue(username);
        $(By.id("password")).setValue(password);

        $$(By.tagName("button"))
                .filter(text("Sign in"))
                .first()
                .click();
    }

    private static int acceptRequests() {
        $(By.id("mynetwork-tab-icon")).click();

        $(By.className("mn-community-summary")).waitUntil(appear, TimeUnit.SECONDS.toMillis(10));

        int acceptedRequests = 0;
        while (!Thread.currentThread().isInterrupted()) {
            ElementsCollection elements = $$(By.tagName("button"))
                    .filter(attribute("data-control-name", "accept"));
            if (elements.isEmpty())
                break;

            elements.first().click();
            screenshot("page-" + acceptedRequests);
            refresh();
            acceptedRequests++;
        }

        return acceptedRequests;
    }
}