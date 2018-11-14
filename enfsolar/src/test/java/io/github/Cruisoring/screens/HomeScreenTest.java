package io.github.Cruisoring.screens;

import io.github.Cruisoring.components.Country;
import io.github.Cruisoring.helpers.*;
import io.github.Cruisoring.wrappers.UILink;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.Assert.*;
public class HomeScreenTest {

    public static final String defaultPropertyFilename = "default.properties";
    public static final String startUrl;
    public static final String saveLocation;
    protected static Path booksDirectory = Paths.get("C:/temp/enfsolar");

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

    HomeScreen homeScreen = master.getScreen(HomeScreen.class);
    ListScreen listScreen = master.getScreen(ListScreen.class);


    @AfterClass
    public static void afterClass() throws Exception {
        if(master != null){
            master.close();
        }
    }


    /**
     * Rigorous Test :-)
     */
    @Test
    public void openFirst(){
        master.gotoUrl(startUrl);
        List<Country> countryList = homeScreen.allCountries.getChildren();
        for (Country country:
                countryList) {
            Logger.I(country.toString());
        }

        homeScreen.allCountries.get(0).click(-1);

        List<String> installerUrls = new ArrayList<>();

        UILink.Collection links = listScreen.links;
        while (!listScreen.navigator.isLast()){
            for (UILink link :
                    links.getChildren()) {
                installerUrls.add(link.getURL());
            }

            listScreen.navigator.goNextPage();
        }

        int size = installerUrls.size();
        File excelTemplate = ResourceHelper.getResourceFile("Installers.xlsx");
        try (
                ExcelBookHelper templateBook = new ExcelBookHelper(excelTemplate);) {

            Path copyPath = Paths.get(booksDirectory.toString(), excelTemplate.getName());
            ExcelBookHelper copyBook = templateBook.getResultBookCopy(copyPath.toString());

            ExcelSheetHelper sheet = copyBook.getSheetHelper("Italy");
//            sheet.sheetLazy.getValue().setDefaultRowHeightInPoints(100);

            for (int i = 0; i < size; i++) {
                Object[] details = getDetail(master, installerUrls.get(i));
                sheet.insertNewRow(details);
            }
            copyBook.save();
        }catch (Exception e){

        }

    }
    @Test
    public void loadAll(){
        String fileName = "c://temp/links.txt";
        List<String> installerUrls = new ArrayList<>();

        //read file into stream, try-with-resources
        try (Stream<String> stream = Files.lines(Paths.get(fileName))) {

            stream.forEach(installerUrls::add);

        } catch (IOException e) {
            e.printStackTrace();
        }


        int size = installerUrls.size();
        Random random = new Random();
        List<Integer> randoms = IntStream.range(0, 150)
                .map(i -> random.nextInt(size)).boxed()
                .distinct().collect(Collectors.toList());
        ExcelBookHelper copyBook = null;
        File excelTemplate = ResourceHelper.getResourceFile("Installers.xlsx");
        try (
                ExcelBookHelper templateBook = new ExcelBookHelper(excelTemplate);) {

            Path copyPath = Paths.get(booksDirectory.toString(), excelTemplate.getName());
            copyBook = templateBook.getResultBookCopy(copyPath.toString());

            ExcelSheetHelper sheet = copyBook.getSheetHelper("Italy");

            Object[] details = null;
            int count = 0;
            for (int i = 0; i < randoms.size(); i++) {
                do {
                    Integer next = randoms.get(i);
                    details = getDetail(master, installerUrls.get(next));
                }while(details == null);

                sheet.insertNewRow(details);
                count ++;
                if(count == 100)
                    break;
            }
            Logger.I("File saved as %s", copyBook.getFile().toString());

        }catch (Exception e){
            Logger.E(e);
        } finally {
            copyBook.save();
        }

    }

    @Test
    public void loadHtml(){
        String fileName = "c://temp/links.txt";
        List<String> installerUrls = new ArrayList<>();
        Pattern cellPattern = Pattern.compile("<(td|a) itemprop[^>]*>[\\s\\S]*?</\\1>");

        //read file into stream, try-with-resources
        try (Stream<String> stream = Files.lines(Paths.get(fileName))) {

            stream.forEach(installerUrls::add);

            URL url = new URL(installerUrls.get(0));
            String html = URLHelper.readHtml(url);
            String name = StringExtensions.getInnerText(html, "h1", true).get(0);
            List<String> innerTexts = StringExtensions.getTexts(html, cellPattern, true);
            Object[] details = new String[] {
                    innerTexts.get(1),
                    name,
                    innerTexts.get(2),
                    innerTexts.get(3),
                    innerTexts.get(4),
                    innerTexts.get(5)
            };
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private Object[] getDetail(Worker worker, String url){
        Objects.requireNonNull(worker);
        Objects.requireNonNull(url);

        try {
            worker.gotoUrl(url);
            InstallerScreen installerScreen = worker.getScreen(InstallerScreen.class);

            String name = installerScreen.getName();
            String address = installerScreen.getAddress();
            String telephone = installerScreen.getTelephone();
//        String email = installerScreen.getEmail();
            String web = installerScreen.getUrl();
            Object[] details = new Object[]{name, address, telephone, web};
            Logger.D(StringUtils.join(details, ", "));
            return details;
        } catch (Exception e){
            Logger.W(e);
            return null;
        }
    }

}