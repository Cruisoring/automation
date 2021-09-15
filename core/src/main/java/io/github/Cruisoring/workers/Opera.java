package io.github.Cruisoring.workers;

import io.github.Cruisoring.enums.DriverType;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.opera.OperaDriver;
import org.openqa.selenium.opera.OperaOptions;

public class Opera extends Worker {
    protected static final String OperaBinKey = "opera.bin";

    protected Opera(Proxy proxy, Capabilities capabilities){
        super(DriverType.Opera);

        OperaOptions options = new OperaOptions()
                .setBinary(System.getProperty(OperaBinKey));
        if(capabilities != null){
            options.merge(capabilities);
        }
        if(proxy != null){
            options.setProxy(proxy);
        }
        this.driver = new OperaDriver(options);
    }

    protected Opera(Capabilities capabilities) {
        this(null, capabilities);
    }

    protected Opera(){
        this(null);
    }
}
