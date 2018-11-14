package io.github.Cruisoring;

import io.github.Cruisoring.helpers.Logger;
import io.github.Cruisoring.helpers.Worker;
import io.github.cruisoring.Functions;
import org.junit.BeforeClass;
import org.junit.Test;
import org.testng.Assert;

import java.io.*;
import java.net.Proxy;
import java.util.List;
import java.util.stream.Collectors;

public class freeProxyListScreenTest {

    static File proxyFile = new File("c:/working/proxies.txt");

    @BeforeClass
    public static void createFile(){
        if(!proxyFile.exists()){
            try {
                proxyFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void getAllProxies() {
        Worker reference=null;
        try (Worker worker = Worker.getAvailable();
             Writer writer = new BufferedWriter(new OutputStreamWriter(
                     new FileOutputStream(proxyFile, true), "UTF-8"));
        ) {
            reference = worker;

            worker.gotoUrl("https://free-proxy-list.net/");
            HttpProxyListScreen mainScreen = worker.getScreen(HttpProxyListScreen.class);
            List<Proxy> proxies = mainScreen.getAllProxies();
            for (Proxy proxy : proxies) {
                writer.append(proxy.toString()+"\r\n");
            }
            Logger.I("%d proxies are loaded", proxies.size());
            writer.flush();
        }catch (Exception ex){
            if(reference != null){
                Functions.Default.run(reference::close);
            }
        }
    }

    @Test
    public void getAllSocksProxies() {
        Worker reference=null;
        try (Worker worker = Worker.getAvailable();
             Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(proxyFile, true), "UTF-8"));

        ) {
            reference = worker;

            worker.gotoUrl("https://www.socks-proxy.net/");
            SocksProxyListScreen mainScreen = worker.getScreen(SocksProxyListScreen.class);
            List<Proxy> proxies = mainScreen.getAllProxies();
            for (Proxy proxy : proxies) {
                writer.append(proxy.toString()+"\r\n");
            }
            Logger.I("%d proxies are loaded", proxies.size());
            writer.flush();
        }catch (Exception ex){
            if(reference != null){
                Functions.Default.run(reference::close);
            }
        }
    }

    @Test
    public void isLast() {
        Worker reference=null;
        try (Worker worker = Worker.getAvailable();) {
            reference = worker;

            worker.gotoUrl("https://free-proxy-list.net/");
            HttpProxyListScreen mainScreen = worker.getScreen(HttpProxyListScreen.class);
            mainScreen.navigator.goLast();
            Assert.assertTrue(mainScreen.navigator.isLastPage());
        }catch (Exception ex){
            if(reference != null){
                Functions.Default.run(reference::close);
            }
        }
    }
}