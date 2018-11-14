package io.github.Cruisoring;


import io.github.Cruisoring.helpers.Executor;
import io.github.Cruisoring.helpers.MapHelper;
import io.github.Cruisoring.helpers.URLHelper;
import io.github.Cruisoring.helpers.Worker;
import io.github.Cruisoring.wrappers.Screen;
import io.github.Cruisoring.wrappers.UINavigator;
import io.github.Cruisoring.wrappers.UITable;
import io.github.cruisoring.Lazy;
import io.github.cruisoring.tuple.Tuple3;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.poi.util.ArrayUtil;
import org.openqa.selenium.By;

import java.net.Proxy;
import java.util.*;
import java.util.stream.Collectors;

public class HttpProxyListScreen extends Screen {
//    public final String[] ProxyIndicators = new String[]{"IP Address", "IP", "Address"};
    public final static String[] AddressIndicators = new String[]{"IP Address", "IP", "Address"};
    public final static String[] PortIndicators = new String[]{"Port"};
    public final static String[] CountryIndicators = new String[]{"Country"};
    public final static String[] AnonymityIndicators = new String[]{"Anonymity", "Anonymous"};
    private final static List<String[]> concernedIndicators = Arrays.asList(AddressIndicators, PortIndicators, CountryIndicators, AnonymityIndicators);

    public final UITable dataTable;
    public final UINavigator navigator;

    protected HttpProxyListScreen(Worker worker) {
        super(worker);
        dataTable = new UITable(this, "table");
        navigator = new UINavigator(this, By.cssSelector("ul.pagination"), 0, By.cssSelector("li"), UINavigator.defaultPagePattern);
    }

    public List<Proxy> getProxies(){
        List<Proxy> proxies = new ArrayList<>();
        List<String[]> allCells = dataTable.getTableCells();

        for (int i = 1; i < allCells.size(); i++) {
            String address = allCells.get(i)[0];
            int port = Integer.valueOf(allCells.get(i)[1]);
            Proxy proxy = URLHelper.getHttpProxy(address, port);
            proxies.add(proxy);
        }

        return proxies;
    }

    public List<Proxy> getAllProxies(){
        List<Proxy> proxies = getProxies();
        while (!navigator.isLastPage()) {
            navigator.goNext();
            proxies.addAll(getProxies());
        }
        return proxies;
    }
}
