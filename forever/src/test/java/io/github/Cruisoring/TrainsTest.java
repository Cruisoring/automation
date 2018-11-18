package io.github.Cruisoring;

import io.github.Cruisoring.helpers.*;
import io.github.Cruisoring.jt2345.CheciScreen;
import io.github.Cruisoring.jt2345.DetailScreen;
import io.github.Cruisoring.screens.HomeScreen;
import io.github.Cruisoring.screens.ListScreen;
import io.github.Cruisoring.wrappers.UILink;
import io.github.Cruisoring.wrappers.UITable;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.testng.annotations.AfterClass;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class TrainsTest {

    public static final String startUrl = "http://www.jt2345.com/huoche/checi/";
    protected static Path booksDirectory = Paths.get("C:/temp/trains");

    static final Worker master;
//    static final Map<Worker, String> slaves = new HashMap<>();


    static {
        master = Worker.getAvailable();

        if (!Files.exists(booksDirectory)){
            try {
                booksDirectory = Files.createDirectories(booksDirectory);
            } catch (IOException e) {
                Logger.I(e);
            }
        }
    }

    CheciScreen list = master.getScreen(CheciScreen.class);
    DetailScreen detailScreen = master.getScreen(DetailScreen.class);

    @org.junit.AfterClass
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
        //Get the file reference
        Path path = Paths.get(booksDirectory.toString(), "links.txt");
        List<String> links = null;
        int total = 0;

        if(Files.exists(path)){
            links = new ArrayList<>();
            //read file into stream, try-with-resources
            try (Stream<String> stream = Files.lines(path)) {

                stream.forEach(links::add);

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            master.gotoUrl(startUrl);
            String outerHTML = list.table.getOuterHTML();
            links = StringExtensions.getUrls(master.getUrl(), outerHTML);

            //Use try-with-resource to get auto-closeable writer instance
            String link = null;
            int size = links.size();
            try (BufferedWriter writer = Files.newBufferedWriter(path)) {
                for (int i = 0; i < size; i++) {
                    link = links.get(i);
                    writer.write(link + "\n");
                }
            } catch (Exception ex) {
                Logger.W("Something is wrong with %s", link);
            }
        }
        total = links.size();
        Logger.I("There are %d links", total);

        File excelTemplate = ResourceHelper.getResourceFile("Trains.xlsx");

        ExcelBookHelper copyBook = null;
        try (
                ExcelBookHelper templateBook = new ExcelBookHelper(excelTemplate);) {

            Path copyPath = Paths.get(booksDirectory.toString(), excelTemplate.getName());
            copyBook = templateBook.getResultBookCopy(copyPath.toString());

            ExcelSheetHelper listSheet = copyBook.getSheetHelper("List");
            ExcelSheetHelper timetable = copyBook.getSheetHelper("Timetable");

            for (int i = 0; i < total; i++) {
                Object[] details = getDetail(master, links.get(i));
                listSheet.appendRow(details);
                List<Object[]> times = detailScreen.getTimetable();
                times.forEach(row -> timetable.appendRow(row));
            }
        }catch (Exception e){

        } finally {
            copyBook.save();
        }
    }

    private Object[] getDetail(Worker worker, String url){
        Objects.requireNonNull(worker);
        Objects.requireNonNull(url);

        try {
            worker.gotoUrl(url, 1000);

            Object[] details = new Object[] {
                    detailScreen.getTrainNo(),
                    detailScreen.getTrainType(),
                    detailScreen.getStartStation(),
                    detailScreen.getStartTime(),
                    detailScreen.getEndTime(),
                    detailScreen.getDuration(),
                    detailScreen.getEndStation(),
                    detailScreen.getDistance(),
                    detailScreen.getUpdated()
            };
            Logger.D(StringUtils.join(details, ", "));
            return details;
        } catch (Exception e){
            Logger.W(e);
            return null;
        }
    }

    @Test
    public void scanUrl() throws  Exception{

        //Get the file reference
        Path path = Paths.get(booksDirectory.toString(), "links.txt");
        List<String> links = null;
        int total = 0;

        if(Files.exists(path)){
            links = new ArrayList<>();
            //read file into stream, try-with-resources
            try (Stream<String> stream = Files.lines(path)) {

                stream.forEach(links::add);

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            master.gotoUrl(startUrl);
            String outerHTML = list.table.getOuterHTML();
            links = StringExtensions.getUrls(master.getUrl(), outerHTML);

            //Use try-with-resource to get auto-closeable writer instance
            String link = null;
            int size = links.size();
            try (BufferedWriter writer = Files.newBufferedWriter(path)) {
                for (int i = 0; i < size; i++) {
                    link = links.get(i);
                    writer.write(link + "\n");
                }
            } catch (Exception ex) {
                Logger.W("Something is wrong with %s", link);
            }
        }
        total = links.size();
        Logger.I("There are %d links", total);

        Pattern detailsPattern = Pattern.compile("(?=<td bgcolor=\"#F1F5FC\"[^>]*>).*?(?<=</td>)");

        File excelTemplate = ResourceHelper.getResourceFile("Trains.xlsx");
        ExcelBookHelper copyBook = null;
        try (
                ExcelBookHelper templateBook = new ExcelBookHelper(excelTemplate);) {

            Path copyPath = Paths.get(booksDirectory.toString(), excelTemplate.getName());
            copyBook = templateBook.getResultBookCopy(copyPath.toString());

            ExcelSheetHelper listSheet = copyBook.getSheetHelper("List");
            ExcelSheetHelper timetable = copyBook.getSheetHelper("Timetable");

            for (int i = 0; i < total; i++) {
                String html = HttpClientHelper.readStringFromURL(links.get(i), "GB2312");
                List<String> tableContents = StringExtensions.getSegments(html, UITable.simpleTablePattern);
                List<String> details = StringExtensions.getTexts(tableContents.get(0), detailsPattern, true);
                listSheet.appendRow(details.toArray());

                List<Object[]> times = DetailScreen.getTimetable(details.get(0), tableContents.get(1));
                times.forEach(row -> timetable.appendRow(row));

                Logger.D("%d: %s", i+1, StringUtils.join(details, ", "));
            }
        }catch (Exception e){

        } finally {
            copyBook.save();
        }
    }

}
