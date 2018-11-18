package io.github.Cruisoring.screens;

import io.github.Cruisoring.components.Country;
import io.github.Cruisoring.helpers.*;
import io.github.Cruisoring.wrappers.UILink;
import io.github.cruisoring.Functions;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
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
public class EnfsolarTest {

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
    static Pattern cellPattern = Pattern.compile("<(td|a) itemprop[^>]*>[\\s\\S]*?</\\1>");


    @AfterClass
    public static void afterClass() throws Exception {
        if(master != null){
            master.close();
        }
    }


//    @Test
//    public void saveLinks(){
//        master.gotoUrl(startUrl);
//        List<Country> countryList = homeScreen.allCountries.getChildren();
//        Map<String, String> countryLinks = countryList.stream()
//                .collect(Collectors.toMap(
//                        country -> country.getName(),
//                        country -> country.getLink()
//                ));
//
//        homeScreen.allCountries.get(0).click(-1);
//
//        List<String> installerUrls = new ArrayList<>();
//
//        UILink.Collection links = listScreen.links;
//        while (!listScreen.navigator.isLastPage()){
//            for (UILink link :
//                    links.getChildren()) {
//                installerUrls.add(link.getURL());
//            }
//
//            listScreen.navigator.goNext();
//        }
//
//        int size = installerUrls.size();
//        File excelTemplate = ResourceHelper.getResourceFile("C:/temp/enfsolar/Installers.xlsx");
//        try (
//                ExcelBookHelper templateBook = new ExcelBookHelper(excelTemplate);) {
//
//            Path copyPath = Paths.get(booksDirectory.toString(), excelTemplate.getName());
//            ExcelBookHelper copyBook = templateBook.getResultBookCopy(copyPath.toString());
//
//            ExcelSheetHelper sheet = copyBook.getSheetHelper("Italy");
////            sheet.sheetLazy.getValue().setDefaultRowHeightInPoints(100);
//
//            for (int i = 0; i < size; i++) {
//                Object[] details = getDetail(master, installerUrls.get(i));
//                sheet.insertNewRow(details);
//            }
//            copyBook.save();
//        }catch (Exception e){
//
//        }
//
//    }


    public static final Pattern nameElementPattern = Pattern.compile("(?:<(h1) [^>]*>[\\S]*)[\\s\\S]*?(?:</\\1>)");
    public static final Pattern detailElementPattern = Pattern.compile("<td>[\\s\\S]*<img class=\"enf-flag\"[^>]*>[\\s\\S]*</td>|<(td|a) itemprop=[^>]*>[\\s\\S]*?</\\1>");
    public static final Pattern businessAttributeElementPattern = Pattern.compile("<div class=\"col-xs-2 enf-section-body-title\">[^<>]*</div>");
    public static final Pattern businessValueElementPattern = Pattern.compile("<(div) class=\"col-xs-10 enf-section-body-content( blue)?\">(?:(?>[^<]+)|<(?!t\\1\\b[^>]*>))*?</\\1>");
    public static final Pattern businessPairPattern = Pattern.compile("<(div) class=\"(col-xs-2 enf-section-body-title|col-xs-10 enf-section-body-content( blue)?)\"([^>]*)?>(?:(?>[^<]+)|<(?!t\\1\\b[^>]*>))*?</\\1>");

    public static Map<String, String> getPairs(String html){
        Map<String, String> details = new HashMap<>();
        details.put("Name", StringExtensions.getTexts(html, nameElementPattern, true).get(0));
        List<String> companyDetails = StringExtensions.getSegments(html, detailElementPattern);
        for (String d : companyDetails) {
            String key = StringExtensions.valueOfAttribute(d, "itemprop");
            String value = StringExtensions.extractHtmlText(d).trim();
            details.put(key.trim(), value);
        }

        List<String> pairs = StringExtensions.getSegments(html, businessPairPattern);
        for (int i = 0; i < pairs.size(); i+=2) {
            String key = StringExtensions.extractHtmlText(pairs.get(i)).trim();
            String value = StringExtensions.extractHtmlText(pairs.get(i+1)).trim();
            details.put(key, value);
        }

        return details;
    }

    @Test
    public void peek(){

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
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("113.252.222.73", 80));

        //read file into stream, try-with-resources
        try (Stream<String> stream = Files.lines(Paths.get(fileName))) {

            stream.forEach(installerUrls::add);

            URL url = new URL("https://www.enfsolar.com/directory/installer/96283/3fg-impianti?utm_source=ENF&utm_medium=Italy&utm_content=96283&utm_campaign=profiles_installer");
//            String test = URLHelper.readHtml(new URL("https://httpbin.org/ip"), proxy);
            String html = URLHelper.readHtml(url, proxy);
            Map<String, String> pairs = getPairs(html);

            Logger.I(StringUtils.join(pairs.entrySet(), ", "));
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

    static String[] headers = new String[] { "Country", "Name", "Address", "Telephone", "Email", "Web"};
    public static Object[] getDetails(String country, String address){
        try{
            URL url = new URL(address);
            String html = URLHelper.readHtml(url, Randomizer.getProxy());
            Map<String, String> pairs = getPairs(html);

            return null;
        } catch (Exception e) {
            Logger.W("Failed with %s\n%s", e.getMessage(), address);
            return null;
        }
    }


//    @Test
//    public void recursively() {
//        master.gotoUrl(startUrl);
//        ExcelBookHelper copyBook = null;
//        File excelTemplate = ResourceHelper.getResourceFile("Installers.xlsx");
//        try (
//                ExcelBookHelper templateBook = new ExcelBookHelper(excelTemplate);) {
//
//            Path copyPath = Paths.get(booksDirectory.toString(), excelTemplate.getName());
//            copyBook = templateBook.getResultBookCopy(copyPath.toString());
//
//            List<Country> countryList = homeScreen.allCountries.getChildren();
//            for (Country country: countryList) {
//                String name = country.getName();
//                copyBook.cloneSheet("Global", name);
//                String url = country.getLink();
//
//                List<String> links = listScreen.getAllLinks(master.getUrl());
//                List<Object[]> installers = links.stream()
//                        .map(link -> getDetails(link))
//                        .collect(Collectors.toList());
//            }
//
//            country.click(-1);
//            List<String> links = listScreen.getAllLinks(master.getUrl());
//            List<Object[]> installers = links.stream()
//                    .map(link -> getDetails(link))
//                    .collect(Collectors.toList());
//        } catch (Exception e) {
//            Logger.I(e);
//        } finally {
//            copyBook.save();
//            Functions.Default.run(master::close);
//        }
//    }
}