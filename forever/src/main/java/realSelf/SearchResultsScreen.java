package realSelf;

import io.github.Cruisoring.helpers.Worker;
import io.github.Cruisoring.wrappers.Screen;
import io.github.Cruisoring.wrappers.UICollection;
import io.github.Cruisoring.wrappers.UINavigator;
import io.github.Cruisoring.wrappers.UIObject;
import org.openqa.selenium.By;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SearchResultsScreen extends Screen {
    public static final Map<String, By> navigatorBys = new HashMap<String, By>(){{
        put("Previouses", By.cssSelector("a.Pager-back"));
        put("Nexts", By.cssSelector("a.Pager-forward"));
    }};

    public final UICollection doctorList;
    public final UINavigator navigator;

    protected SearchResultsScreen(Worker worker) {
        super(worker);
        doctorList = new UICollection(this, By.cssSelector("div.search-results"), By.cssSelector("div.js-finderResult>div[ng-transclude]"));
        navigator = new UINavigator(this, By.cssSelector("div[rs-pagination]>ul"), 0, By.cssSelector("li>a"), navigatorBys);
    }

    public List<Doctor> getDoctorList(){
        List<UIObject> doctorElements = doctorList.getChildren();
        List<Doctor> doctors = doctorElements.stream()
                .map(o -> BriefWidget.getDoctorBrief(o.getOuterHTML()))
                .collect(Collectors.toList());
        return doctors;
    }

    public List<Doctor> getAllDoctors(){
        List<Doctor> doctors = getDoctorList();
        while (!navigator.isLastPage()){
            navigator.goNext();
            doctors.addAll(getDoctorList());
        }
        return doctors;
    }
}
