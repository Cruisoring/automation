package io.github.Cruisoring.workers;

import io.github.Cruisoring.enums.DriverType;
import io.github.Cruisoring.helpers.Logger;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.HashMap;
import java.util.Map;

public class Chrome extends Worker {

    protected Chrome(Proxy proxy, Capabilities capabilities){
        super(DriverType.Chrome);

        Map<String, Object> prefs = new HashMap<String, Object>();
        prefs.put("profile.default_content_setting_values.notifications", 2);
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("prefs", prefs);
        //options.addArguments("start-maximized");
        options.addArguments("disable-infobars");
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-notifications");

        if(capabilities != null){
            options.merge(capabilities);
        }
        if(proxy != null){
            Logger.I("Proxy used: %s", proxy);
//            if(proxy.getSslProxy() != null) {
//                String ssl = proxy.getSslProxy();
//                String proxySettings = String.format("--proxy-server=https://%s", ssl);
//                options.addArguments(proxySettings);
//            } else {
//                options.addArguments("--proxy-server=socks5://" + proxy.getSocksProxy());
//            }
            options.setProxy(proxy);
        }
        this.driver = new ChromeDriver(options);
    }

    protected Chrome(Capabilities capabilities) {
        this(null, capabilities);
    }

    protected Chrome(Capabilities capabilities, Rectangle rect){
        super(DriverType.Chrome);

        ChromeOptions options = new ChromeOptions().merge(capabilities);
        if(rect != null){
            options.addArguments(String.format("--window-size=%d,%d", rect.width, rect.height));
            options.addArguments(String.format("--window-position=%d,%d", rect.x, rect.y));
            Logger.V("x:%d, y=%d, width=%d, height=%d", rect.x, rect.y, rect.width, rect.height);
        }
        this.driver = new ChromeDriver(options);
    }

    protected Chrome(boolean withProxy, boolean inHeadless, String... arguments) {
        super(DriverType.Chrome);

        ChromeOptions options  = new ChromeOptions();
        //options.addArguments("--start-maximized");
        options.addArguments("--disable-web-security");
        if (inHeadless) {
            options.setHeadless(true);
        }
        if (withProxy) {
            Proxy proxy = getNextProxy();
            Logger.I("Proxy used: %s", proxy);
            if(proxy.getSslProxy() != null) {
                String ssl = proxy.getSslProxy();
                String proxySettings = String.format("--proxy-server=https://%s", ssl);
                options.addArguments(proxySettings);
            } else {
                options.addArguments("--proxy-server=socks5://" + proxy.getSocksProxy());
            }
            options.setProxy(proxy);
        } else {
            options.addArguments("--no-proxy-server");
        }
        options.addArguments(arguments);

        Map<String, Object> prefs = new HashMap<String, Object>();
        prefs.put("credentials_enable_service", true);
        prefs.put("profile.password_manager_enabled", true);

        options.setExperimentalOption("prefs", prefs);
        this.driver = new ChromeDriver(options);
    }
}
