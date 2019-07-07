package ru.izebit;


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


@Slf4j
public class SelenideExample {
    public static void main(String[] args) {
        String username = args[0];
        String password = args[1];

        WebDriver webDriver = initWebDriver("http://127.0.0.1:4444/wd/hub");
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
                "disable-infobars", // disabling infobars
                "--disable-extensions", // disabling extensions
                "--disable-dev-shm-usage", // overcome limited resource problems
                "--no-sandbox", // Bypas
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