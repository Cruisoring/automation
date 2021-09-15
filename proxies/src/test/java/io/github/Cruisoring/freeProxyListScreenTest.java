package io.github.Cruisoring;

import io.github.Cruisoring.enums.DriverType;
import io.github.Cruisoring.helpers.Logger;
import io.github.Cruisoring.helpers.Randomizer;
import io.github.Cruisoring.helpers.URLHelper;
import io.github.Cruisoring.workers.Worker;
import io.github.cruisoring.Functions;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.Rectangle;
import org.testng.Assert;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.util.List;

public class freeProxyListScreenTest {
    public static final URL testIP = (URL) Functions.Default.apply(() -> new URL("https://httpbin.org/ip"));

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
    public void getHttpProxies() {
        Worker reference=null;
        try (Worker worker = Worker.getAny(DriverType.Chrome);
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
            Logger.I("%d proxies are loaded into %s", proxies.size(), proxyFile);
            writer.flush();
        }catch (Exception ex){
            if(reference != null){
                Functions.Default.run(reference::close);
            }
        }
    }

    @Test
    public void getSocksProxies() {
        Worker reference=null;
        try (Worker worker = Worker.create(DriverType.Chrome);
             Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(proxyFile, true), "UTF-8"));

        ) {
            Worker.arrangeWindow(worker, new Rectangle(0, 0, Worker.DefaultMonitorDimension.height*2/3, Worker.DefaultMonitorDimension.width));
            reference = worker;

            worker.gotoUrl("https://www.socks-proxy.net/");
            SocksProxyListScreen mainScreen = worker.getScreen(SocksProxyListScreen.class);
            List<Proxy> proxies = mainScreen.getAllProxies();
            for (Proxy proxy : proxies) {
                writer.append(proxy.toString()+"\r\n");
            }
            Logger.I("%d proxies are loaded int %s", proxies.size(), proxyFile);
            writer.flush();
        }catch (Exception ex){
            if(reference != null){
                Functions.Default.run(reference::close);
            }
        }
    }

    @Test
    public void getAnonymousProxies() {
        Worker reference=null;
        try (Worker worker = Worker.getAny();
             Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(proxyFile, true), "UTF-8"));

        ) {
            reference = worker;

            worker.gotoUrl("https://free-proxy-list.net/anonymous-proxy.html");
            SocksProxyListScreen mainScreen = worker.getScreen(SocksProxyListScreen.class);
            List<Proxy> proxies = mainScreen.getAllProxies();
            for (Proxy proxy : proxies) {
                writer.append(proxy.toString()+"\r\n");
            }
            Logger.I("%d proxies are loaded int %s", proxies.size(), proxyFile);
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
        try (Worker worker = Worker.getAny();) {
            reference = worker;

            worker.gotoUrl("https://free-proxy-list.net/");
            HttpProxyListScreen mainScreen = worker.getScreen(HttpProxyListScreen.class);
            mainScreen.navigator.gotoPageOrLast();
            Assert.assertTrue(mainScreen.navigator.isLastPage());
        }catch (Exception ex){
            if(reference != null){
                Functions.Default.run(reference::close);
            }
        }
    }

    @Test
    public void removeTransparent() throws MalformedURLException {
        int proxyCount = Randomizer.proxies.size();
        String myIp = URLHelper.readHtml(testIP, null);
        Logger.I("%s", myIp);

        URL yourIP = new URL("https://httpbin.org/ip");
        for (int i = proxyCount-1; i >= 0 ; i--) {
            Proxy proxy = Randomizer.proxies.get(i);
            String html = URLHelper.readHtml(yourIP, proxy);
            Logger.D("Html = %s", html);
            if(myIp == null || myIp.equals(html)){
                Logger.D("Remove transparent proxy: %s", proxy);
                Randomizer.proxies.remove(i);
            } else {
                Logger.D("Verify %s to be anonymous.", proxy);
            }
        }
    }

    @Test
    public void testProxy() throws MalformedURLException {
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("61.247.58.103", 8090));
        String html = URLHelper.readHtml(new URL("http://www.google.com"), proxy);
        Logger.I("length = %d", html.length());
    }
}