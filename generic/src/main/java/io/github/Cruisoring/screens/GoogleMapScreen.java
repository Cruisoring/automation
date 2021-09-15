package io.github.Cruisoring.screens;

import io.github.Cruisoring.helpers.StringExtensions;
import io.github.Cruisoring.workers.Worker;
import io.github.Cruisoring.wrappers.Screen;
import io.github.Cruisoring.wrappers.UIObject;
import org.openqa.selenium.By;

import java.util.ArrayList;
import java.util.List;

public class GoogleMapScreen extends Screen {

    public static final By LocationsBy = By.cssSelector("div.geolocation-common-map-locations");

    public final UIObject locations;
    protected GoogleMapScreen(Worker worker) {
        super(worker);
        locations = new UIObject(this, LocationsBy);
    }

    public List<Object[]> getPOIs(){
        getWorker().waitPageReady(-1);
        String outerHtml = locations.getOuterHTML();

        List<String> locationSegments = StringExtensions.getSegments(outerHtml, "<div class=\"geolocation\" ");
        List<Object[]> result = new ArrayList<>();
        String element, name, location, type;
        Float lat, lng;
        for (String locationSeg : locationSegments) {
            element = StringExtensions.getFirstSegmentByLeadingTag(locationSeg, "<h2");
            name = StringExtensions.getText(element, "<h2");
            lat = Float.valueOf(StringExtensions.valueOfAttribute(locationSeg, "data-lat"));
            lng = Float.valueOf(StringExtensions.valueOfAttribute(locationSeg, "data-lng"));
            element = StringExtensions.getFirstSegmentByLeadingTag(locationSeg, "<div class=\"data-row location");
            location = StringExtensions.getText(element, "<div class=\"data-row location");
            element = StringExtensions.getFirstSegmentByLeadingTag(locationSeg, "<a href=\"/how-cameras-work");
            type = StringExtensions.getText(element, "<a href=\"/how-cameras-work");
            result.add(new Object[]{name, lat, lng, location, type});
        }
        return result;
    }
}
