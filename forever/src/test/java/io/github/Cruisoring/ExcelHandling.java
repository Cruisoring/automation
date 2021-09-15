package io.github.Cruisoring;

import io.github.Cruisoring.helpers.ExcelBookHelper;
import io.github.Cruisoring.helpers.ExcelSheetHelper;
import io.github.Cruisoring.helpers.Logger;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ExcelHandling {

    @Test
    public void handlingExcel(){
        try (
                ExcelBookHelper book = new ExcelBookHelper(new File("C:/temp/DornbrachtAll.xlsx"), true);
                ) {

            ExcelSheetHelper sheet = book.getSheetHelper("Sheet1");

            Map<String, Object[]> rowsMap = new HashMap<>();
            //Column indexes of Columns A, C, H, I, J, K, L, M, N, O, P, Q, R, S
            int[] columns = new int[]{0, 2, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18};
            //Looping through row 2 - 780
            for (int i = 0; i < 779; i++) {
                Object[] values = sheet.getRowValues(i, columns);
                //Get the first 8 digits of Column D
                String model = sheet.getCellValue(i, 3).toString().substring(0, 8);
                //Keep the first 8 characters of column D as key, values of concerned columns as the value
                rowsMap.put(model, values);
            }

            //Looping though row 783 - 2495
            for (int i = 781; i < 2494; i++) {
                //Get the first 8 characters of column D to match with the saved keys
                String model = sheet.getCellValue(i, 3).toString().substring(0, 8);
                //Skip the row if no matching
                if(rowsMap.containsKey(model)){
                    //Here is the values to be copied from the matched row
                    Object[] values = rowsMap.get(model);
                    //Insert OR update the concerned cells
                    sheet.updateRow(i, values, columns);
                }
            }
            book.save();
        }catch (Exception e){
            Logger.E(e);
        }

    }
}
