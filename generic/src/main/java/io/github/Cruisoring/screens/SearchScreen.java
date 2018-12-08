package io.github.Cruisoring.screens;


import io.github.Cruisoring.helpers.DateTimeHelper;
import io.github.Cruisoring.interfaces.WorkingContext;
import io.github.Cruisoring.workers.Worker;
import io.github.Cruisoring.wrappers.*;
import io.github.cruisoring.Lazy;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;

import java.net.URL;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SearchScreen extends Screen {
    public static final String InputBoxKey = "InputBox";
    public static final String SearchButtonKey = "SearchButton";
    public static final String ResultListKey = "ResultList";
    public static final String ResultItemKey = "ResultItem";
    public static final String ResultTitleKey = "ResultTitle";
    public static final String ResultLinkKey = "ResultLink";
    public static final String ResultDescriptionKey = "ResultDescription";
    public static final String ResultTimeKey = "ResultTime";

    public static final Pattern dateInSearchResultPattern = Pattern.compile(
            "^((\\w{3} \\d{1,2}, \\d{4})" + "|" +       //Google & Ask
                    "(\\d{4}年\\d{1,2}月\\d{1,2}日)" + "|" +     //Baidu
                    "(\\d{4}-\\d{1,2}-\\d{1,2}))");             //Bing

    public static final Map<String, List<String>> commonLocators = new HashMap<String, List<String>>(){
        {
            //Search input box
            put(InputBoxKey, Arrays.asList(
                    "input[type='text'][title='Search']",   //Google
                    "input.navigation-search-box, input#yschsp",          //AOL
                    "input#kw",                             //Baidu
                    "input.search-box-input",               //Ask
                    "input[type='search']"));               //Bing
            //Search button
            put(SearchButtonKey, Arrays.asList(
                    "[aria-label='Google Search']",         //Google
                    "button[type='submit']",                //Yahoo & Ask
                    "button.navigation-search-btn",         //AOL
                    "input[type='submit']"));               //Bing & Baidu
            //Search result list
            put(ResultListKey, Arrays.asList(
                    "div#rso",                              //Google
                    "ol.searchCenterMiddle",                //AOL
                    "div#content_left",                     //Baidu
                    "div#PartialSearchResults-body",        //Ask
                    "ol#b_results"));                       //Bing
            //Result items
            put(ResultItemKey, Arrays.asList(
                    "div.g",                                //Google
                    "li>div.algo",                          //AOL
                    "div.result",                           //Baidu
                    "div.PartialSearchResults-item",        //Ask
                    "li.b_algo"));                          //Bing
            //Result Title
            put(ResultTitleKey, Arrays.asList(
                    "h3",                                   //Google & Baidu & AOL
                    "input#yschsp",                       //Yahoo
                    "div.PartialSearchResults-item-title",  //Ask
                    "h2"));                                 //Bing
           //Result link
            put(ResultLinkKey, Arrays.asList(
                    "div.r>a",                                //Google
//                    "input#yschsp",                         //Yahoo
                    "h3>a",                                 //Baidu & AOL
                    "div.PartialSearchResults-item-title>a",//Ask
                    "h2>a"));                               //Bing
            //Result Time
            put(ResultTimeKey, Arrays.asList(
                    "span.st>span.f",                       //Google
                    "span[class*='TimeFactor']",            //Baidu
//                    "input#yschsp",                       //Yahoo
                    "p.PartialSearchResults-item-abstract",  //Ask
                    "div.b_caption>p"));                     //Bing
           //Result Description
            put(ResultDescriptionKey, Arrays.asList(
                    "span.st",                               //Google
                    "div.compText",                          //AOL
                    "h3>div.c-abstract",                     //Baidu
                    "p.PartialSearchResults-item-abstract",  //Ask
                    "div.b_caption>p"));                     //Bing
        }
    };

    private static By getBy(String key){
        if(!commonLocators.containsKey(key)) {
            throw new IllegalArgumentException();
        }

        String combinedLocators = commonLocators.get(key).stream()
                .collect(Collectors.joining(", "));
        return By.cssSelector(combinedLocators);
    }

    public class ResultItem extends UIObject {

        final UIObject title;
        final UILink link;
        final UIObject time;
        final UIObject description;

        public ResultItem(WorkingContext context) {
            super(context, getBy(ResultItemKey));
            this.title = new UIObject(this, getBy(ResultTitleKey));
            this.link = new UILink(this, getBy(ResultLinkKey));
            this.time = new UIObject(this, getBy(ResultTimeKey));
            this.description = new UIObject(this, getBy(ResultDescriptionKey));
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
            Matcher matcher = dateInSearchResultPattern.matcher(text);
            if(matcher.matches()){
                return DateTimeHelper.dateFromString(matcher.group());
            }
            return null;
        }

        public String getDescription(){
            return description.getAllText();
        }
    }

    final UIEdit inputBox;
    final UIObject searchButton;
    public final UICollection<ResultItem> resultItems;
    public final Lazy<UINavigator> navigatorLazy;

    protected SearchScreen(Worker worker) {
        super(worker);
        inputBox = new UIEdit(this, getBy(InputBoxKey));
        searchButton = new UIObject(this, getBy(SearchButtonKey));
        resultItems = new UICollection<ResultItem>(this, getBy(ResultListKey), getBy(ResultItemKey));
        navigatorLazy = new Lazy<UINavigator>(this::getNavigatorByUrl);
    }

    private UINavigator getNavigatorByUrl(){
        String url = getWorker().getUrl().toString();
        if(StringUtils.containsIgnoreCase(url, "google.com")){
            return new UINavigator(this, By.cssSelector("div#navcnt tbody"), By.cssSelector("a"));
        } else if(StringUtils.containsIgnoreCase(url, "aol.com")){
            return new UINavigator(this, By.cssSelector("div.compPagination"), By.cssSelector("div.compPagination>strong, div.compPagination>a"));
        } else if(StringUtils.containsIgnoreCase(url, "baidu.com")){
            return new UINavigator(this, By.cssSelector("div#page"), By.cssSelector("div#page>a, div#page>strong"));
        } else if(StringUtils.containsIgnoreCase(url, "ask.com")){
            return new UINavigator(this, By.cssSelector("ul.PartialWebPagination"));
        } else if(StringUtils.containsIgnoreCase(url, "bing.com")){
            return new UINavigator(this, By.cssSelector("nav[role='navigation'] h4+ul"), By.cssSelector("li>a"));
        }

        throw new IllegalStateException("The UINavigator must be defined with at least one locator!");
    }

    public void gotoLastPage(String keywords){
        inputBox.enterByScript(keywords);
        searchButton.click(3000);
        int pageNumber = 0;
        while (!navigatorLazy.getValue().isLastPage()){
            UINavigator navigator = navigatorLazy.getValue();
            navigator.scrollIntoViewByScript(true);
            navigator.highlight(UIObject.DefaultHighlightScript, 1000);
            navigator.goNext(String.valueOf(pageNumber));
        }
    }
}
