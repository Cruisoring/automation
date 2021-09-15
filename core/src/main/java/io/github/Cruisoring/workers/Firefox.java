package io.github.Cruisoring.workers;

import io.github.Cruisoring.enums.DriverType;
import io.github.Cruisoring.helpers.MapHelper;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

public class Firefox extends Worker {

    protected Firefox(Proxy proxy, Capabilities capabilities){
        super(DriverType.Firefox);

        FirefoxOptions options = new FirefoxOptions();
        options.setCapability("dom.webnotifications.enabled", false);
        options.setCapability("dom.push.enabled", false);

//        ProfilesIni profile = new ProfilesIni();
//        FirefoxProfile testprofile = profile.getProfile("default");
//        testprofile.setPreference("dom.webnotifications.enabled", false);
//        testprofile.setPreference("dom.push.enabled", false);
//        DesiredCapabilities dc = DesiredCapabilities.firefox();
//        dc.setCapability(FirefoxDriver.PROFILE, testprofile);
        if(proxy != null){
            String proxyJson = MapHelper.asJson(proxy.toJson()).replaceAll("\\s{2,}", "");
            com.google.gson.JsonObject json = new com.google.gson.JsonObject();
            json.addProperty("proxyType", "MANUAL");
            json.addProperty("httpProxy", "91.200.115.35");
            json.addProperty("httpProxyPort", "41088");
            json.addProperty("sslProxy", "91.200.115.35");
            json.addProperty("sslProxyPort", "41088");

            options.setCapability("proxy", json);
        }
//        FirefoxOptions options = new FirefoxOptions(dc);
        if(capabilities != null){
            options.merge(capabilities);
        }

        this.driver = new FirefoxDriver(options);
    }

    protected Firefox(Capabilities capabilities) {
        this(null, capabilities);
    }

    protected Firefox(){
        this(null, null);
    }
}
