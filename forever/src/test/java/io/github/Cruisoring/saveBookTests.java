package io.github.Cruisoring;

import io.github.Cruisoring.helpers.*;
import io.github.Cruisoring.screens.HomeScreen;
import io.github.Cruisoring.wrappers.UIObject;
import org.openqa.selenium.By;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

@Test
public class saveBookTests {
    public static final String defaultPropertyFilename = "default.properties";
    public static final String startUrl;
    public static final String saveLocation;
    protected static Path booksDirectory = Paths.get("C:/test/books/");


    static {
        Properties properties = ResourceHelper.getProperties(defaultPropertyFilename);

        startUrl = properties.getProperty("startUrl");
        saveLocation = properties.getProperty("saveLocation");

        if (!Files.exists(booksDirectory)){
            try {
                booksDirectory = Files.createDirectories(booksDirectory);
            } catch (IOException e) {
                Logger.I(e);
            }
        }
    }

    static Worker worker;

    HomeScreen homeScreen;

    @BeforeClass
    public static void beforeClass(){
        worker = Worker.getAvailable();
    }

    @BeforeMethod
    public void beforeMethod(){
        worker.invalidate();
        homeScreen = worker.getScreen(HomeScreen.class);
    }

    @AfterClass
    public static void afterClass() throws Exception {
        if(worker != null){
            worker.close();
            worker = null;
        }
    }

    @Test
    public void getCountries(){
        try {
            URL url = new URL("https://www.enfsolar.com/directory/installer");
            worker.gotoUrl(url);

            UIObject body = new UIObject(worker, By.cssSelector("div.mk-body"));
            String html = body.getOuterHTML();
            List<String> elements = StringExtensions.getSegments(html, StringExtensions.LinkPattern);
            List<String> hrefs = elements.stream().map(e -> StringExtensions.valueOfAttribute(e, "href"))
                    .map(href -> URLHelper.getLink(url, href))
                    .collect(Collectors.toList());

            for (String link : hrefs ) {
                System.out.println(link);
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
}
