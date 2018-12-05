package io.github.Cruisoring.workers;

import io.github.Cruisoring.enums.DriverType;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.UnexpectedAlertBehaviour;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerOptions;

public class IE extends Worker {

    protected IE(Proxy proxy, Capabilities capabilities){
        super(DriverType.IE);

        InternetExplorerOptions options = new InternetExplorerOptions();
        options.setCapability("unexpectedAlertBehaviour", "accept");
        if(capabilities != null){
            options.merge(capabilities);
        }
        if(proxy != null){
            options.setProxy(proxy);
        }
        this.driver = new InternetExplorerDriver(options);
    }

    protected IE(Capabilities capabilities) {
        this(null, capabilities);
    }

    protected IE(){
        this(null);
    }

}
