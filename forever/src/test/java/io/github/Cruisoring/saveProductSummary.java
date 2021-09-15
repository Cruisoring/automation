package io.github.Cruisoring;

import io.github.Cruisoring.components.PIContainer;
import io.github.Cruisoring.helpers.ExcelBookHelper;
import io.github.Cruisoring.helpers.ExcelSheetHelper;
import io.github.Cruisoring.helpers.Logger;
import io.github.Cruisoring.helpers.ResourceHelper;
import io.github.Cruisoring.screens.HomeScreen;
import io.github.Cruisoring.screens.ListScreen;
import io.github.Cruisoring.screens.ProductScreen;
import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;

@Test
public class saveProductSummary {
    public static final String defaultPropertyFilename = "default.properties";
    public static final String startUrl;
    public static final String saveLocation;
    protected static Path booksDirectory = Paths.get("C:/temp/");

    static final Worker master;
//    static final Map<Worker, String> slaves = new HashMap<>();


    static {
        Properties properties = ResourceHelper.getProperties(defaultPropertyFilename);
        master = Worker.getAvailable();

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

    HomeScreen homeScreen;
    ListScreen listScreen;
    ProductScreen productScreen;

    @BeforeMethod
    public void beforeMethod(){
        master.invalidate();
        homeScreen = master.getScreen(HomeScreen.class);
        listScreen = master.getScreen(ListScreen.class);
        productScreen = master.getScreen(ProductScreen.class);
    }

    @AfterClass
    public static void afterClass() throws Exception {
        if(master != null){
            master.close();
        }
//        for (Worker slave : slaves.keySet()) {
//            if(slave != null){
//                slave.close();
//            }
//        }
//        slaves.clear();
    }

    @Test
    public void openRandomProducts() {
        master.gotoUrl(startUrl);
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
    public void saveOneProduct(){
        master.gotoUrl(startUrl);
        homeScreen.openMenu("Women", "SWIM");

//        listScreen.gotoPage(2);
//        Assert.assertEquals(2, listScreen.getCurrentPage());

        int displayedCount = listScreen.getDisplayedCount();
        Logger.I("There are %d products displayed.", displayedCount);
        listScreen.getProduct(2).click(-1);

        URL picUrl = productScreen.getPictureUrl();
        String productName = productScreen.getProductName();
        String productDescription = productScreen.getDescription();
        String address = productScreen.getWorker().driver.getCurrentUrl();
        String price = productScreen.getPrice();
        URL imageUrl = productScreen.getImage(0).getURL();

        File excelTemplate = ResourceHelper.getResourceFile("Products.xlsx");
        try (
                ExcelBookHelper templateBook = new ExcelBookHelper(excelTemplate);) {

            Path copyPath = Paths.get(booksDirectory.toString(), excelTemplate.getName());
            ExcelBookHelper copyBook = templateBook.getResultBookCopy(copyPath.toString());

            ExcelSheetHelper sheet = copyBook.getSheetHelper("Products");
            sheet.sheetLazy.getValue().setDefaultRowHeightInPoints(100);

            Object[] values = new Object[]{productName, productDescription, address, price, imageUrl};
            sheet.appendRow(values);
            copyBook.save();
        }catch (Exception e){}
    }

    @Test
    public void getURLs(){
        master.gotoUrl(startUrl);
        homeScreen.openMenu("women", "swim");

        int displayedCount = listScreen.getDisplayedCount();
        Logger.I("There are %d products displayed.", displayedCount);

        List<PIContainer> products = listScreen.getProducts();
        List<String> productURLs = products.stream().parallel()
                .map(p -> p.getLink())
                .collect(Collectors.toList());

        int size = productURLs.size();
        Logger.I("%d Product URLs.", size);

        File excelTemplate = ResourceHelper.getResourceFile("Products.xlsx");
        try (
                ExcelBookHelper templateBook = new ExcelBookHelper(excelTemplate);) {

            Path copyPath = Paths.get(booksDirectory.toString(), excelTemplate.getName());
            ExcelBookHelper copyBook = templateBook.getResultBookCopy(copyPath.toString());

            ExcelSheetHelper sheet = copyBook.getSheetHelper("Products");
            sheet.sheetLazy.getValue().setDefaultRowHeightInPoints(100);

            for (int i = 0; i < size; i++) {
                Object[] details = extractProductDetails(master, productURLs.get(i));
                sheet.appendRow(details);
            }
            copyBook.save();
        }catch (Exception e){

        }

    }

    private Object[] extractProductDetails(Worker worker, String url){
        Objects.requireNonNull(worker);
        Objects.requireNonNull(url);

        worker.gotoUrl(url);
        ProductScreen prod = worker.getScreen(ProductScreen.class);

        String productName = prod.getProductName();
        String productDescription = prod.getDescription();
        String price = prod.getPrice();
        URL firstImage = prod.getImage(0).getURL();
        Object[] details = new Object[] { productName, productDescription, url, price, firstImage};
        Logger.D(StringUtils.join(details, ", "));
        return details;
    }
}
