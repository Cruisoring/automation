package io.github.Cruisoring.screens;

import io.github.Cruisoring.helpers.ExcelBookHelper;
import io.github.Cruisoring.helpers.ExcelSheetHelper;
import io.github.Cruisoring.helpers.Executor;
import io.github.Cruisoring.helpers.Logger;
import io.github.cruisoring.tuple.Tuple;
import org.junit.Test;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ExcelTest {

    @Test
    public void trimDuplicated(){
        String fileName = "c://working/201910_DataSet.xlsx";

        //read file into stream, try-with-resources
        try (
                ExcelBookHelper book = new ExcelBookHelper(new File(fileName))
        ) {
            ExcelSheetHelper sheetHelper = book.getSheetHelper("201910_DataSet");

            Map<String, Executor.FunctionThrows<Object[], Tuple>> criterias = new LinkedHashMap<String, Executor.FunctionThrows<Object[], Tuple>>(){{
               put("B,D,H,I,J,K,O,R", row -> Tuple.create(row[1], row[3], row[7], row[8], row[9], row[10], row[14], row[17]));
               put("B,K,O,R", row -> Tuple.create(row[1], row[10], row[14], row[17]));
                put("B,K,O", row -> Tuple.create(row[1], row[10], row[14]));
                put("K,O", row -> Tuple.create(row[10], row[14]));
            }};

            List<Object[]> rows = sheetHelper.getAllRows();

            for (String criteria : criterias.keySet()) {
                Executor.FunctionThrows<Object[], Tuple> functionThrows = criterias.get(criteria);
                ExcelSheetHelper copy = book.cloneSheet("Cleaned", criteria);
                List<Tuple> keys = new ArrayList<>();
                for (Object[] row :
                        rows) {
                    Tuple key = functionThrows.apply(row);
                    keys.add(key);
                }

                Map<Tuple, List<Integer>> map = IntStream.range(0, keys.size()).boxed()
                        .collect(Collectors.groupingBy(i -> keys.get(i)));

                Logger.I("There are %d unique rows for criteria of %s", map.size(), criteria);
                List<Integer> indexes = map.values().stream()
                        .map(list -> list.get(0)).collect(Collectors.toList());
                indexes.sort(Comparator.naturalOrder());

                for (Integer index : indexes) {
                    copy.appendRow(rows.get(index));
                }
            }

            book.save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
