package io.github.Cruisoring.screens;

import io.github.Cruisoring.helpers.DateTimeHelper;
import io.github.Cruisoring.interfaces.WorkingContext;
import io.github.Cruisoring.wrappers.UILink;
import io.github.Cruisoring.wrappers.UIObject;
import org.openqa.selenium.By;

import java.time.LocalDate;
import java.util.regex.Matcher;

public class ResultItem extends UIObject {

    final UIObject title;
    final UILink link;
    final UIObject time;
    final UIObject description;

    public ResultItem(WorkingContext context, By by, Integer index) {
        super(context, by, index);
        this.title = new UIObject(this, SearchScreen.getBy(SearchScreen.ResultTitleKey));
        this.link = new UILink(title);
        this.time = new UIObject(this, SearchScreen.getBy(SearchScreen.ResultTimeKey));
        this.description = new UIObject(this, SearchScreen.getBy(SearchScreen.ResultDescriptionKey));
    }

    public String getTitle(){
        return title.getTextContent().trim();
    }

    public String getUrl(){
        String url = link.getURL();
        return url;
    }

    public LocalDate getPublishTime(){
        if(!time.isVisible())
            return null;

        String text = time.getAllText().trim();
        Matcher matcher = SearchScreen.dateInSearchResultPattern.matcher(text);
        if(matcher.matches()){
            return DateTimeHelper.dateFromString(matcher.group());
        }
        return null;
    }

    public String getDescription(){
        return description.getAllText();
    }
}
