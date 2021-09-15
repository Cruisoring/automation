package io.github.Cruisoring.jt2345;

import io.github.Cruisoring.helpers.StringExtensions;
import io.github.Cruisoring.helpers.Worker;
import io.github.Cruisoring.wrappers.Screen;
import io.github.Cruisoring.wrappers.UICollection;
import io.github.Cruisoring.wrappers.UIObject;
import org.openqa.selenium.By;

import java.util.ArrayList;
import java.util.List;

public class DetailScreen extends Screen {

    public static List<Object[]> getTimetable(String trainNo, String text){
        List<Object[]> result = new ArrayList<>();
        List<String> fields = StringExtensions.getInnerText(text, "td", true);
        int rows = fields.size()/6;
        for (int i = 1; i < rows; i ++){
            Object[] rowData = new Object[]{
                    trainNo,
                    fields.get(i*6),
                    fields.get(i*6+1),
                    fields.get(i*6+2),
                    fields.get(i*6+3),
                    fields.get(i*6+4).replace("分",""),
                    fields.get(i*6+5)
            };
            result.add(rowData);
        }
        return result;
    }


    public final UICollection details;
    public final UIObject secondTable;
//    public final UICollection times;

    protected DetailScreen(Worker worker) {
        super(worker);
        details = new UICollection(this, By.cssSelector("table[width='970']"), By.cssSelector("td[bgcolor=\"#F1F5FC\"]"));
        secondTable = new UIObject(this, By.cssSelector("table"), 1);
//        times = new UICollection(this, By.cssSelector(""))
    }

    public String getTrainNo(){
        return details.get(0).getTextContent();
    }

    public String getTrainType(){
        return details.get(1).getTextContent();
    }

    public String getStartStation(){
        return details.get(2).getTextContent();
    }

    public String getStartTime(){
        return details.get(3).getTextContent();
    }

    public String getEndTime(){
        return details.get(4).getTextContent();
    }

    public String getDuration(){
        String duration = details.get(5).getTextContent();
        duration = duration.replace("小时"," hours ");
        duration = duration.replace("分"," minutes");
        return duration;
    }

    public String getEndStation(){
        return details.get(6).getTextContent();
    }

    public String getDistance(){
        return details.get(7).getTextContent();
    }

    public String getUpdated(){
        String date = details.get(8).getTextContent().trim();
        date = date.replaceAll("年", "/");
        date = date.replaceAll("月", "/");
        date = date.replaceAll("日", "");
        return date;
    }

    public List<Object[]> getTimetable(){
        return getTimetable(getTrainNo(), secondTable.getOuterHTML());
    }

}
