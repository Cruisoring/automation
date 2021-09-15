package io.github.Cruisoring;

import AMPA.DetailScreen;
import AMPA.ListScreen;
import io.github.Cruisoring.helpers.ExcelBookHelper;
import io.github.Cruisoring.helpers.ExcelSheetHelper;
import io.github.Cruisoring.helpers.Logger;
import io.github.Cruisoring.wrappers.UIObject;
import io.github.cruisoring.Functions;
import org.junit.Test;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AMPATest {

    @Test
    public void saveAllLinks(){
        File file = new File("C:/working/ampa.txt");
        if(!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Worker reference=null;
        try (Worker worker = Worker.getAvailable();
             Writer writer = new BufferedWriter(new OutputStreamWriter(
                     new FileOutputStream(file, true), "UTF-8"));
        ) {
            reference = worker;
            URL baseUrl = new URL("https://www.madrid.es/portales/munimadrid/es/Samur/Asociaciones-de-Madres-y-Padres-de-Alumnos?vgnextfmt=default&vgnextoid=3c7914b3c84ed010VgnVCM100000171f5a0aRCRD&vgnextchannel=84516c77e7d2f010VgnVCM1000000b205a0aRCRD");

            worker.gotoUrl(baseUrl);
            ListScreen listScreen = worker.getScreen(ListScreen.class);
            List<String> links = listScreen.getAllLinks(baseUrl);
            for (String link : links) {
                writer.append(link+"\r\n");
            }
            Logger.I("%d links are loaded", links.size());
            writer.flush();
        }catch (Exception ex){
            if(reference != null){
                Functions.Default.run(reference::close);
            }
        }
    }

    @Test
    public void fixAddresses(){
        ExcelBookHelper reference=null;
        File excelTemplate = new File("C:/working/AMPA.xlsx");
        try (Worker worker = Worker.getAvailable();
             ExcelBookHelper book = new ExcelBookHelper(excelTemplate);
        ) {
            reference = book;
            ExcelSheetHelper sheetHelper = book.getSheetHelper("Sheet1");
            List<Object[]> rowData = sheetHelper.getAllRows();

            URL baseUrl = new URL("https://www.madrid.es/portales/munimadrid/es/Samur/Asociaciones-de-Madres-y-Padres-de-Alumnos?vgnextfmt=default&vgnextoid=3c7914b3c84ed010VgnVCM100000171f5a0aRCRD&vgnextchannel=84516c77e7d2f010VgnVCM1000000b205a0aRCRD");

            worker.gotoUrl(baseUrl);
            ListScreen listScreen = worker.getScreen(ListScreen.class);

            worker.gotoUrl(baseUrl);
            Map<String, String> nameAddress = listScreen.getAllSchoolAddresses();

            String email, web;
            for (Object[] row : rowData){
                String address = nameAddress.get(row[0]);
                if(row.length > 5 && row[5] != null){
                    email = row[5].toString();
                    int index = email.indexOf("Web");
                    if(index != -1){
                        web = email.substring(index+3).trim();
                        email = email.substring(0, index).trim();
                    } else {
                        web = null;
                    }
                } else {
                    email = null;
                    web = null;
                }

                Object[] objects = new Object[]{row[0], address, row[2], row[3], row[4], email, web};
                sheetHelper.appendRow(objects);
            }

            book.save();
            reference = null;
        }catch (Exception ex){
            if(reference != null){
                Functions.Default.run(reference::close);
            }
        }finally {
            if(reference != null){
                reference.save();
            }
        }

    }

    @Test
    public void openAllLinks(){

        ExcelBookHelper reference=null;
        File excelTemplate = new File("C:/working/AMPA.xlsx");
        try (Worker worker = Worker.getAvailable();
             ExcelBookHelper book = new ExcelBookHelper(excelTemplate);
        ) {
            reference = book;
            ExcelSheetHelper sheetHelper = book.getSheetHelper("Sheet1");
            URL baseUrl = new URL("https://www.madrid.es/portales/munimadrid/es/Samur/Asociaciones-de-Madres-y-Padres-de-Alumnos?vgnextfmt=default&vgnextoid=3c7914b3c84ed010VgnVCM100000171f5a0aRCRD&vgnextchannel=84516c77e7d2f010VgnVCM1000000b205a0aRCRD");

            worker.gotoUrl(baseUrl);
            ListScreen listScreen = worker.getScreen(ListScreen.class);
            DetailScreen detailScreen = worker.getScreen(DetailScreen.class);
            List<Map<String, String>> allResult = new ArrayList<>();
            do {
                worker.waitPageReady();
                List<UIObject> link = listScreen.list.getChildren();

                for (int i = 0; i < link.size(); i++) {
                    link.get(i).click(-1);
                    Map<String, String> details = detailScreen.getDetails();
                    allResult.add(details);
                    worker.goBack();
                }

                if(listScreen.navigator.isLastPage()){
                    break;
                }
                listScreen.navigator.goPageOrNext();
                listScreen.invalidate();
            } while(true);

            for (Map<String, String> details: allResult){
                String[] objects = new String[]{details.get("Name"), details.get("Address"), details.get("City"), details.get("Telephone"), details.get("Fax"), details.get("Email")};
                sheetHelper.appendRow(objects);
            }

            book.save();
            reference = null;
        }catch (Exception ex){
            if(reference != null){
                Functions.Default.run(reference::close);
            }
        }finally {
            if(reference != null){
                reference.save();
            }
        }
    }
//
//    @Test
//    public void scanUrl() throws  Exception{
//        File file = new File("C:/working/ampa.txt");
//        List<String> links = Files.lines(file.toPath()).collect(Collectors.toList());
//        int total = links.size();
//        Logger.I("There are %d links", total);
//
//        Pattern titlePattern = Pattern.compile(StringExtensions.leafTablePatternString.replace("table", "h3"), Pattern.MULTILINE);
//        Pattern contactPattern = Pattern.compile("<div class=\\\"row vcard geo\\\">[\\s\\S]*</div>\\s*(?:<div class=\\\"row rs_skip)", Pattern.MULTILINE);
//
//        Worker reference=null;
//        try (Worker worker = Worker.getAvailable();
//             Writer writer = new BufferedWriter(new OutputStreamWriter(
//                     new FileOutputStream(file, true), "UTF-8"));
//        ) {
//            reference = worker;
//
//            worker.gotoUrl(links.get(0));
//            DetailScreen detailScreen = worker.getScreen(DetailScreen.class);
//            Map<String, String> details = detailScreen.getDetails();
//        }catch (Exception ex){
//            if(reference != null){
//                Functions.Default.run(reference::close);
//            }
//        }
//
//
//        File excelTemplate = ResourceHelper.getResourceFile("C:/working/AMPA.xlsx");
//        try (
//                ExcelBookHelper book = new ExcelBookHelper(excelTemplate);) {
//
//            copyBook = book;
//            ExcelSheetHelper sheet = copyBook.getSheetHelper("Sheet1");
//
//            String[] keywords = new String[] {
//                    "Dirección" //Address
//                    , "Barrio / Distrito"   //City
//                    , "Teléfono"
//                    , "Fax"
//                    , "Correo"
//            };
//
//            for (int i = 0; i < total; i++) {
//                String html = HttpClientHelper.readStringFromURL(links.get(i), null);
//                String name = StringExtensions.getFirstSegment(html, titlePattern);
//                name = StringExtensions.extractHtmlText(name);
//
//                int start = html.indexOf("<div class=\"row vcard geo\">");
//                int last = html.indexOf("<div class=\"row rs_skip\">", start);
//
//                String contractDiv = html.substring(start, last).trim();
//                String contract = StringExtensions.extractHtmlText(contractDiv);
//
//                Integer[] indexes = Arrays.stream(keywords)
//                        .map(key -> contract.indexOf(key))
//                        .toArray(size -> new Integer[size]);
//
//                String address = null, city = null, telephone = null, fax = null, email = null;
//                int end = contract.length();
//
//                if (indexes[4] != -1) {
//                    email = contract.substring(keywords[4].length() + indexes[4] + 1, end).trim();
//                    end = indexes[4];
//                }
//
//                if (indexes[3] != -1) {
//                    fax = contract.substring(keywords[3].length() + indexes[3] + 1, end).trim();
//                    end = indexes[3];
//                }
//
//                if (indexes[2] != -1) {
//                    telephone = contract.substring(keywords[2].length() + indexes[2] + 1, end).trim();
//                    end = indexes[2];
//                }
//
//                if (indexes[1] != -1) {
//                    city = contract.substring(keywords[1].length() + indexes[1] + 1, end).trim();
//                    end = indexes[1];
//                }
//
//
//                List<String> tableContents = StringExtensions.getSegments(html, UITable.simpleTablePattern);
//                List<String> details = StringExtensions.getTexts(tableContents.get(0), detailsPattern, true);
//                sheet.appendRow(details.toArray());
//
//                List<Object[]> times = DetailScreen.getTimetable(details.get(0), tableContents.get(1));
//                times.forEach(row -> timetable.appendRow(row));
//
//                Logger.D("%d: %s", i+1, StringUtils.join(details, ", "));
//            }
//            book.save();
//        }catch (Exception e){
//
//        } finally {
////            copyBook.save();
//        }
//    }
//
}
