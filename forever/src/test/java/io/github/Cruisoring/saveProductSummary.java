package io.github.Cruisoring;

import io.github.Cruisoring.components.PIContainer;
import io.github.Cruisoring.helpers.*;
import io.github.Cruisoring.screens.HomeScreen;
import io.github.Cruisoring.screens.ListScreen;
import io.github.Cruisoring.screens.ProductScreen;
import io.github.Cruisoring.wrappers.UIImage;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

@Test
public class saveProductSummary {
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
    ListScreen listScreen;
    ProductScreen productScreen;

    @BeforeClass
    public static void beforeClass(){
        worker = Worker.getAvailable();
    }

    @BeforeMethod
    public void beforeMethod(){
        worker.invalidate();
        homeScreen = worker.getScreen(HomeScreen.class);
        listScreen = worker.getScreen(ListScreen.class);
        productScreen = worker.getScreen(ProductScreen.class);
    }

    @AfterClass
    public static void afterClass() throws Exception {
        if(worker != null){
            worker.close();
            worker = null;
        }
    }

    @Test
    public void openRandomProducts() {
        worker.gotoUrl(startUrl);
        homeScreen.openMenu("Women", "All");
        int totalProducts = listScreen.getProductCount();
        int totalPages = listScreen.getPageCount();
        int currentPage = listScreen.getCurrentPage();
        Logger.I("There are %d Products in %d pages, current in page %d.", totalProducts, totalPages, currentPage);

        Assert.assertFalse(listScreen.gotoPrevPage());
//        Assert.assertTrue(listScreen.gotoNextPage());
//        Assert.assertEquals(2, listScreen.getCurrentPage());

        Assert.assertTrue(listScreen.gotoPage(10));
        Assert.assertEquals(10, listScreen.getCurrentPage());
    }

    @Test
    public void openSecondImage(){
        worker.gotoUrl(startUrl);
        homeScreen.openMenu("Women", "SWIM");

        listScreen.gotoPage(2);
//        Assert.assertEquals(2, listScreen.getCurrentPage());

        int displayedCount = listScreen.getDisplayedCount();
        Logger.I("There are %d products displayed.", displayedCount);
        listScreen.getProduct(displayedCount-2).click();

        URL picUrl = productScreen.getPictureUrl();
        String productName = productScreen.getProductName();
        String productDescription = productScreen.getDescription();
        float price = productScreen.getPrice();
        URL thumbnailUrl = productScreen.getImage(2).getURL();

        Logger.I("Name: %s, Price: $%f\nDescription: %s\nPic: %s\nThumbnail: %s",
                productName, price, productDescription, picUrl, thumbnailUrl);
    }

    @Test
    public void saveOneProduct(){
        worker.gotoUrl(startUrl);
        homeScreen.openMenu("Women");

        PIContainer product = listScreen.getProduct(10);
        product.click(-1);

        URL picUrl = productScreen.getPictureUrl();
        String productName = productScreen.getProductName();
        String productDescription = productScreen.getDescription();
        float price = productScreen.getPrice();
        UIImage defaultImage = productScreen.getImage(0);
        URL thumbnailUrl = defaultImage.getURL();

        Logger.I("Name: %s, Price: $%f\nDescription: %s\nPic: %s\nThumbnail: %s",
                productName, price, productDescription, picUrl, thumbnailUrl);

        File excelTemplate = ResourceHelper.getResourceFile("Products.xlsx");
        ExcelBookHelper templateBook = new ExcelBookHelper(excelTemplate);
        Path copyPath = Paths.get(booksDirectory.toString(), excelTemplate.getName());
        ExcelBookHelper copyBook = templateBook.getResultBookCopy(copyPath.toString());

        ExcelSheetHelper sheetHelper = copyBook.getSheetHelper("Products");

    }
}
