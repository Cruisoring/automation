package io.github.Cruisoring;

import io.github.Cruisoring.helpers.*;
import io.github.cruisoring.Functions;
import io.github.cruisoring.tuple.Tuple;
import io.github.cruisoring.tuple.Tuple2;
import io.github.cruisoring.tuple.Tuple4;
import org.testng.annotations.Test;

import java.io.File;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class ExcelTest {

    private List<String> orderedItems = new ArrayList<>();
    private Map<String, Tuple4<String, String, Float, Float>> settings = null;

    private void loadSettings(ExcelBookHelper book){
        Map<String, Tuple4<String, String, Float, Float>> map = new HashMap<>();
        ExcelSheetHelper sheet  = book.getSheetHelper("Settings");
        List<Object[]> all = sheet.getAllRows();
        all.forEach(row -> {
            orderedItems.add(row[0].toString().trim());
            map.put(row[0].toString().trim(),
                    Tuple.create(row[1].toString(), row[2].toString(), (Float)row[3], (Float)row[4]));});
        settings = map;
    }

    private List<Object[]> getItemData(String item){
        LocalDate date = Randomizer.getRandomDate(LocalDate.of(2018, 1, 1), 365);
        Tuple4<String, String, Float, Float> conditions = settings.get(item);
        Integer count = Randomizer.getRandom(Randomizer.playerNumbers);
        String competition = "Local - Club Zoom";
        List<Object[]> rows = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Float delta = Randomizer.getRandomFloat(-1.0f, 1.5f);
            Integer age = Randomizer.getRandomAge();
            Object[] row = new Object[]{
                    date,
                    item,
                    Randomizer.getRandomName(conditions.getSecond()),
                    age,
                    String.format("team %d", count-Randomizer.random.nextInt(5)),
                    Randomizer.getRandomFloat(conditions.getThird(), conditions.getFourth()),
                    Randomizer.getRandomFloat(0.0f, 1.0f) < 0.2f ? "-" :Randomizer.getRandomFloat(-1.0f, 1.5f),
                    competition,
                    2018 - age,
                    Randomizer.getRandomFloat(0.0f, 5.0f) > 0.2f ? "-" : "comments"
            };
            rows.add(row);
        }

        return rows;
    }

    @Test
    public void populateFakeData(){
        File file = ResourceHelper.getResourceFile("Sports.xlsx");
        ExcelBookHelper reference = null;
        try(
                ExcelBookHelper book = new ExcelBookHelper(file);
                ExcelBookHelper copy = book.getResultBookCopy("C:/free/Scores.xlsx");
                ){
            reference = copy;
            Map<String, Tuple4<String, String, Float, Float>> map = new HashMap<>();
            ExcelSheetHelper data  = copy.getSheetHelper("Fakes");
            int rowCount = 0;
            int minRows = 50000;

            for (int i = 0; i < minRows/2 && rowCount < minRows; i++) {
                String nextEvent = orderedItems.get(i % orderedItems.size());
                List<Object[]> rows = getItemData(nextEvent);
                rowCount += rows.size();
                int actual = data.appendRows(rows);
                if(actual != rows.size()){
                    Logger.D("Inserted %d rows with %d data");
                }
            }
            copy.save();
        }catch (Exception ex){
            Logger.I(ex);
        }finally {
            if(reference != null){
                Functions.Default.run(reference::close);
            }
        }

    }

    final int resultIndex = 5;
    //The bigger the result, the better
    int compareFieldResults(Object[] row1, Object[] row2){
        Float result1 = (Float)row1[5];
        Float result2 = (Float)row2[5];
        if(result1 > result2)
            return 1;
        else if (result1 < result2)
            return -1;
        else
            return 0;
    }

    final Comparator<Object[]> fieldResultComparator = this::compareFieldResults;
    final Comparator<Object[]> trackResultComparator = fieldResultComparator.reversed();

    private String getCategory(Object[] row){
        Tuple4<String, String, Float, Float> attribute = settings.get(row[0].toString().trim());
        String gender = attribute.getSecond();
        Double age = (Double) row[3];
        if(gender.equalsIgnoreCase("F")){
            if(age < 16){
                return "U-16 Girls";
            } else if (age < 18) {
                return "U-18 Girls";
            } else if (age < 20) {
                return "U-20 Girls";
            } else {
                return "Senior Team Women";
            }
        }else {
            if(age < 16){
                return "U-16 Boys";
            } else if (age < 18) {
                return "U-18 Boys";
            } else if (age < 20) {
                return "U-20 Boys";
            } else {
                return "Senior Team Men";
            }
        }
    }

    @Test
    public void sort(){
        File file = new File("C:/free/Scores.xlsx");
        try(
                ExcelBookHelper book = new ExcelBookHelper(file, true);){
            loadSettings(book);

            ExcelSheetHelper rawData = book.getSheetHelper("Raw");
            int rowCount = rawData.getRowCount();

            Map<String, List<Tuple2<Integer, Float>>> groups = new HashMap<String, List<Tuple2<Integer, Float>>>();
            for (int i = 0; i < rowCount; i++) {
                Object[] rowValues = rawData.getAllRowValues(i);
                String event = rowValues[1].toString().trim();
                if(!groups.containsKey(event)){
                    groups.put(event, new ArrayList<Tuple2<Integer, Float>>());
                }
                Float result = (Float)rowValues[5];
                if(settings.get(event).getFirst().equalsIgnoreCase("Track")){
                    result = -result;
                }
                groups.get(event).add(Tuple.create(i, result));
            }

            ExcelSheetHelper sorted = book.getSheetHelper("Top6");
            int count = 0;
            for (String event : orderedItems) {
                List<Tuple2<Integer, Float>> records = groups.get(event).stream()
                        .sorted(Comparator.comparing(tuple -> tuple.getSecond()))
                        .limit(6)
                        .collect(Collectors.toList());
                for (int i = 0; i < records.size(); i++) {
                    Tuple2<Integer, Float> tuple = records.get(i);
                    Object[] row = rawData.getAllRowValues(tuple.getFirst());
                    if(sorted.appendRow(row)){
                        count++;
                    }
                }
            }
            Logger.I("%d rows inserted", count);
            book.save();
        }catch (Exception ex){
            Logger.I(ex);
        }
    }

    @Test
    public void convert(){
        File file = new File("C:/working/New Zealand Wine Producers.xlsx");
        try(
                ExcelBookHelper book = new ExcelBookHelper(file, true);){

            ExcelSheetHelper rawData = book.getSheetHelper("Raw");
            int rowCount = rawData.getRowCount();

            Map<String, List<Tuple2<Integer, Float>>> groups = new HashMap<String, List<Tuple2<Integer, Float>>>();
            for (int i = 0; i < rowCount; i++) {
                Object[] rowValues = rawData.getAllRowValues(i);
                String event = rowValues[1].toString().trim();
                if(!groups.containsKey(event)){
                    groups.put(event, new ArrayList<Tuple2<Integer, Float>>());
                }
                Float result = (Float)rowValues[5];
                if(settings.get(event).getFirst().equalsIgnoreCase("Track")){
                    result = -result;
                }
                groups.get(event).add(Tuple.create(i, result));
            }

            ExcelSheetHelper sorted = book.getSheetHelper("Top6");
            int count = 0;
            for (String event : orderedItems) {
                List<Tuple2<Integer, Float>> records = groups.get(event).stream()
                        .sorted(Comparator.comparing(tuple -> tuple.getSecond()))
                        .limit(6)
                        .collect(Collectors.toList());
                for (int i = 0; i < records.size(); i++) {
                    Tuple2<Integer, Float> tuple = records.get(i);
                    Object[] row = rawData.getAllRowValues(tuple.getFirst());
                    if(sorted.appendRow(row)){
                        count++;
                    }
                }
            }
            Logger.I("%d rows inserted", count);
            book.save();
        }catch (Exception ex){
            Logger.I(ex);
        }
    }
}
