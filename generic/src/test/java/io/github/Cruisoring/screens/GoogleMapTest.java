package io.github.Cruisoring.screens;

import io.github.Cruisoring.enums.DriverType;
import io.github.Cruisoring.helpers.ExcelBookHelper;
import io.github.Cruisoring.helpers.ExcelSheetHelper;
import io.github.Cruisoring.helpers.Logger;
import io.github.Cruisoring.workers.Worker;
import io.github.Cruisoring.wrappers.UIObject;
import io.github.Cruisoring.wrappers.UITable;
import org.junit.Test;
import org.openqa.selenium.By;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GoogleMapTest {

    @Test
    public void getVicCameras(){
        try (
                Worker worker = Worker.create(DriverType.Chrome);
                ExcelBookHelper book = new ExcelBookHelper(new File("C:/working/Cameras.xlsx"));
        ){
            worker.gotoUrl("https://www.camerassavelives.vic.gov.au/camera-locations");
            GoogleMapScreen mapScreen = worker.getScreen(GoogleMapScreen.class);

            List<Object[]> locations = mapScreen.getPOIs();
            Logger.I("There are %d locations", locations.size());

            ExcelSheetHelper vic = book.getSheetHelper("VIC");
            vic.appendRows(locations);
            book.save();
        } catch (Exception e) {
            Logger.W(e);
        }
    }

    @Test
    public void getACTCameras(){
        List<Object[]> cameras = new ArrayList<>();
        try (
                Worker worker = Worker.create(DriverType.Chrome);
                ExcelBookHelper book = new ExcelBookHelper(new File("C:/working/Cameras.xlsx"));
        ){
            worker.gotoUrl("https://www.data.act.gov.au/d/h534-v2x9/visualization");
            UITable table = new UITable(worker, "table", -1);
            UIObject next = new UIObject(worker, By.cssSelector("button.pager-button-next"), -1);

            next.waitDisplayed();
            while (!next.isDisabled()){
                table.waitPageReady();
                List<String[]> cellStrings = table.getValuesOfColumns(1, 0, 3, 4, 5);
                List<Object[]> cellValues = cellStrings.stream().skip(1)
                        .filter(line -> !line[2].isEmpty())
                    .map(strings -> new Object[]{strings[0], strings[1], Float.valueOf(strings[2]), Float.valueOf(strings[3]), strings[4]})
                    .collect(Collectors.toList());

                cameras.addAll(cellValues);
                next.click(-1);
            }

            ExcelSheetHelper act = book.getSheetHelper("ACT");
            act.appendRows(cameras);
            book.save();
        } catch (Exception e) {
            Logger.W(e);
        }
    }

}
