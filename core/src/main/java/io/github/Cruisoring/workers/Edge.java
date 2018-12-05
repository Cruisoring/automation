package io.github.Cruisoring.workers;

import io.github.Cruisoring.enums.DriverType;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;

public class Edge extends Worker {

    protected Edge(Proxy proxy, Capabilities capabilities){
        super(DriverType.Edge);

        EdgeOptions options = new EdgeOptions();
        if(capabilities != null){
            options.merge(capabilities);
        }

        if(proxy != null){
            options.setProxy(proxy);
        }

        this.driver = new EdgeDriver(options);
    }

    protected Edge(Capabilities capabilities){
        this(null, capabilities);
    }

    protected Edge(){
        this(null, null);
    }
}
