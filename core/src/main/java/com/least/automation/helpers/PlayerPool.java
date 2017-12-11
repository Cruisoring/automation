package com.least.automation.helpers;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wiljia on 3/08/2017.
 */
public class PlayerPool {
//    private static final List<DriverType> driverTypes = new ArrayList<>();
//    private static final List<WebDriver> drivers = new ArrayList<>();
//    private static int driverCount = 0;
//
//    static {
//        System.setProperty("webdriver.chrome.driver", "vendor/chromedriver.exe");
//        System.setProperty("webdriver.ie.driver", "vendor/IEDriverServer.exe");
//    }
//
//    private static Worker singleton = null;
//
//    public static Worker getPlayer(DriverType... type) {
//        if(singleton == null) {
//            singleton = getChromePlayer(null);
//        }
//        return singleton;
//    }
//
//    private static Worker getIEPlayer(DesiredCapabilities desiredCapabilities){
//        if (desiredCapabilities == null){
//            System.setProperty("webdriver.ie.driver.loglevel","TRACE");
//            System.setProperty("webdriver.ie.driver.logfile", "C:/Projects/logme.txt");
//            DesiredCapabilities caps = DesiredCapabilities.internetExplorer();
//            caps.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS,true);
//            caps.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
//            //caps.setCapability(InternetExplorerDriver.IE_ENSURE_CLEAN_SESSION, true);
//            WebDriver driver = new InternetExplorerDriver(caps);
//        }
//        WebDriver driver = new InternetExplorerDriver(desiredCapabilities);
//        return new Worker(driver);
//    }
//
//    private static Worker getChromePlayer(ChromeOptions options) {
//        if (options == null) {
//            options = new ChromeOptions();
//            options.addArguments("--start-maximized");
//            options.addArguments("--disable-web-security");
//            options.addArguments("--no-proxy-server");
//
//            Map<String, Object> prefs = new HashMap<String, Object>();
//            prefs.put("credentials_enable_service", false);
//            prefs.put("profile.password_manager_enabled", false);
//
//            options.setExperimentalOption("prefs", prefs);
//        }
//        ChromeDriver chrome = new ChromeDriver(options);
//        return new Worker(chrome);
//    }
}
