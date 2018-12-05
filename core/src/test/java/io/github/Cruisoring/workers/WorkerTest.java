package io.github.Cruisoring.workers;

import io.github.Cruisoring.enums.DriverType;
import io.github.Cruisoring.helpers.Executor;
import io.github.Cruisoring.helpers.Logger;
import io.github.Cruisoring.helpers.MapHelper;
import io.github.Cruisoring.helpers.ReportHelper;
import io.github.cruisoring.Functions;
import org.junit.Test;

import java.awt.*;

public class WorkerTest {

    @Test
    public void getScreenSize(){
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        int width = gd.getDisplayMode().getWidth();
        int height = gd.getDisplayMode().getHeight();
        int resolution = Toolkit.getDefaultToolkit().getScreenResolution();
        ReportHelper.log("width=%d, height=%d, resolution=%d", width, height, resolution);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        ReportHelper.log("size= %s", screenSize);
    }

//    @Test
//    public void getChrome() throws Exception{
//        try (
//        Worker worker = Worker.getChrome("--start-maximized");) {
//            worker.gotoUrl("https://httpbin.org/ip");
//            ChromeDriver driver = (ChromeDriver)worker.driver;
//            Executor.sleep(3000);
//            org.openqa.selenium.Dimension size = driver.manage().window().getSize();
//            Logger.I("Maximised size=%s", size);
//            org.openqa.selenium.Rectangle rect = worker.relocate(new org.openqa.selenium.Rectangle(size.width/2, size.height/2, size.height/2, size.width/2));
//            Logger.I("Rect = (x=%d, y=%d, width=%d, height=%d)", rect.x, rect.y, rect.width, rect.height);
//
//            Capabilities capabilities = Chrome.setRect(null, Worker.DefaultBrowserRectangls.get(3)[2]);
//            Worker worker2 = new Chrome(capabilities);
//            worker2.gotoUrl("https://httpbin.org/ip");
//            org.openqa.selenium.Rectangle rect2 = worker2.relocate(new org.openqa.selenium.Rectangle(0, 0, size.height/2, size.width/2));
//            Logger.I("Rect2 = (x=%d, y=%d, width=%d, height=%d)", rect2.x, rect2.y, rect2.width, rect2.height);
//
//            Logger.V(MapHelper.asJson(worker2.driver.getCapabilities().asMap()));
//            Executor.sleep(10000);
//            worker2.close();
//        }
//    }

    @Test
    public void getChrome2(){
        Worker chrome = Worker.create(DriverType.Chrome, null);
        chrome.gotoUrl("https://httpbin.org/ip");
        chrome.arrangeWindow(Worker.DefaultBrowserRectangls.get(6)[2]);

        Logger.V(MapHelper.asJson(chrome.driver.getCapabilities().asMap()));

        Executor.sleep(3000);
        Functions.Default.run(chrome::close);
    }

    @Test
    public void getIE(){
        Worker ie = Worker.getIE();
        ie.gotoUrl("https://google.com");
        ie.arrangeWindow(Worker.DefaultBrowserRectangls.get(6)[2]);
        Executor.sleep(3000);
        ie.arrangeWindow(Worker.DefaultBrowserRectangls.get(6)[1]);
        Executor.sleep(3000);
        ie.arrangeWindow(Worker.DefaultBrowserRectangls.get(6)[1]);
        Executor.sleep(3000);
        ie.arrangeWindow(Worker.DefaultBrowserRectangls.get(6)[0]);
        Executor.sleep(3000);

        Logger.V(MapHelper.asJson(ie.driver.getCapabilities().asMap()));
        Functions.Default.run(ie::close);

    }

    @Test
    public void getEdge() {
        Worker edge = Worker.getEdge();
        edge.gotoUrl("https://httpbin.org/ip");
        Logger.V(MapHelper.asJson(edge.driver.getCapabilities().asMap()));
        Executor.sleep(3000);
        edge.arrangeWindow(Worker.DefaultBrowserRectangls.get(6)[0]);
        Executor.sleep(3000);
        edge.arrangeWindow(Worker.DefaultBrowserRectangls.get(6)[3]);
        Executor.sleep(3000);
        Functions.Default.run(edge::close);
    }

    @Test
    public void getOpera() {
        Worker worker = new Opera(null);
        worker.gotoUrl("https://httpbin.org/ip");
        Executor.sleep(3000);
        Logger.I(MapHelper.asJson(worker.driver.getCapabilities().asMap()));
        worker.arrangeWindow(Worker.DefaultBrowserRectangls.get(3)[1]);
        Executor.sleep(3000);
        worker.arrangeWindow(Worker.DefaultBrowserRectangls.get(5)[0]);
        Executor.sleep(3000);
        worker.arrangeWindow(Worker.DefaultBrowserRectangls.get(5)[4]);
        Executor.sleep(3000);
        Functions.Default.run(worker::close);
    }

    @Test
    public void getFirefox() {
        Worker worker = Worker.create(DriverType.Firefox, null);
        worker.gotoUrl("https://whatismyipaddress.com/");
        Logger.I(MapHelper.asJson(worker.driver.getCapabilities().asMap()));
        worker.arrangeWindow(Worker.DefaultBrowserRectangls.get(3)[1]);
        Executor.sleep(3000);
        worker.arrangeWindow(Worker.DefaultBrowserRectangls.get(5)[0]);
        Executor.sleep(3000);
        worker.arrangeWindow(Worker.DefaultBrowserRectangls.get(5)[4]);
        Executor.sleep(3000);
        Functions.Default.run(worker::close);
    }

    @Test
    public void getAllBrowsers(){
        Worker chrome = Worker.create(DriverType.Chrome, null);
        chrome.gotoUrl("https://whatismyipaddress.com/");
        Executor.sleep(3000);
        Worker edge = Worker.create(DriverType.Edge, null);
        edge.gotoUrl("https://whatismyipaddress.com/");
        Executor.sleep(3000);
        Worker firefox = Worker.create(DriverType.Firefox, null);
        firefox.gotoUrl("https://whatismyipaddress.com/");
        Executor.sleep(3000);
        Worker opera = Worker.create(DriverType.Opera, null);
        opera.gotoUrl("https://whatismyipaddress.com/");
        Executor.sleep(3000);
        Worker ie = Worker.create(DriverType.IE, null);
        ie.gotoUrl("https://whatismyipaddress.com/");
        Executor.sleep(3000);
        Worker.closeAll();
    }
}