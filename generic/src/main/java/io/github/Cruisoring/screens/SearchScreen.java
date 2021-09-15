package io.github.Cruisoring.screens;


import io.github.Cruisoring.helpers.Executor;
import io.github.Cruisoring.helpers.Logger;
import io.github.Cruisoring.helpers.Randomizer;
import io.github.Cruisoring.workers.Worker;
import io.github.Cruisoring.wrappers.*;
import io.github.cruisoring.Lazy;
import io.github.cruisoring.throwables.RunnableThrowable;
import io.github.cruisoring.throwables.SupplierThrowable;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SearchScreen extends Screen {
    public static final String InputBoxKey = "InputBox";
    public static final String SearchButtonKey = "SearchButton";
    public static final String ResultListKey = "ResultList";
    public static final String ResultItemKey = "ResultItem";
    public static final String ResultTitleKey = "ResultTitle";
    public static final String ResultLinkKey = "ResultLink";
    public static final String ResultDescriptionKey = "ResultDescription";
    public static final String ResultTimeKey = "ResultTime";

    public static final Map<String, List<String>> commonLocators = new HashMap<String, List<String>>() {
        {
            //Search input box
            put(InputBoxKey, Arrays.asList(
                    "input[type='text'][title='Search']",   //Google
                    "input.navigation-search-box, input#yschsp, input[name='q']",          //AOL
                    "input#kw",                             //Baidu
                    "input.search-box-input, input.PartialSearchBox-input",               //Ask
                    "input[type='search'], input.b_searchbox"));               //Bing
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
                    "div.content_left", "div#content_left",  //Baidu
                    "div.PartialSearchResults-body",        //Ask
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

    public static By getBy(String key) {
        if (!commonLocators.containsKey(key)) {
            throw new IllegalArgumentException();
        }

        String combinedLocators = commonLocators.get(key).stream()
                .collect(Collectors.joining(", "));
        return By.cssSelector(combinedLocators);
    }

    final UIEdit inputBox;
    final UIObject searchButton;
    public final UICollection<ResultItem> resultItems;
    public final Lazy<UINavigator> navigatorLazy;

    protected SearchScreen(Worker worker) {
        super(worker);
        inputBox = new UIEdit(this, getBy(InputBoxKey));
        searchButton = new UIObject(this, getBy(SearchButtonKey));
        resultItems = new UICollection<ResultItem>(this, getBy(ResultListKey), 0, ResultItem.class, getBy(ResultItemKey));
        navigatorLazy = new Lazy<UINavigator>(this::getNavigatorByUrl);
    }

    public Map<String, Boolean> searchToLast(int maxPageToSearch, Function<String, String> titlePredicate, Predicate<String> linkPredicate, String... keywords) {
        List<Integer> randomIndexes = Randomizer.getRandomIndex(keywords.length);

        Map<String, Boolean> result = new HashMap<>();

        try {
            for (int i = 0; i < keywords.length; i++) {
                String nextKey = keywords[randomIndexes.get(i)];

                if (!inputBox.isVisible()) {
                    getWorker().goBack();
                }
                inputBox.enterText(nextKey);
                Executor.sleep(1000);
                searchButton.click(-1);

                int pageNumber = 0;
                UINavigator navigator = navigatorLazy.getValue();
                navigator.waitDisplayed();
                SupplierThrowable<Set<ResultItem>> getResultItems = () ->
                        getMatchedItems(titlePredicate, linkPredicate);
                SupplierThrowable<Set<ResultItem>> scrollOnly = () -> {
                    worker.scrollSmoothly(0, 10, 20);
                    return null;
                };

                while (navigator.isVisible() && !navigator.isLastPage() && ++pageNumber < maxPageToSearch) {

                    Set<ResultItem> resultItems = Executor.getParallel(Arrays.asList(getResultItems, scrollOnly))
                            .stream().filter(items -> items != null).findFirst().orElse(null);

                    worker.scrollSmoothly(0, -20, 10);
                    if (resultItems != null && !resultItems.isEmpty()) {
                        for (ResultItem item : resultItems) {
                            result.putIfAbsent(nextKey, true);
                            item.scrollIntoView();
                            item.title.executeScript("var e =arguments[0]; e.style.color='yellow';e.style.backgroundColor='red';");
                            Executor.sleep(500);

                            item.link.click(-1);
                            getWorker().scrollSmoothly(0, 100, 20);
                            getWorker().goBack();
                        }
                    }

                    if (navigator == null || !navigator.isVisible()) {
                        result.putIfAbsent(nextKey, false);
                        continue;
                    }

                    navigator.scrollIntoView();
                    Executor.sleep(1000);
                    navigator.gotoPageOrNext(String.valueOf(pageNumber + 1));
                }
                result.putIfAbsent(nextKey, false);
            }
        } catch (Exception e) {
            Logger.E(e);
        }
        return result;
    }

    private void updateTitleColor(Integer index, String color) {
        if (color != null) {
            resultItems.get(index).title.executeScript(String.format("var e=arguments[0]; e.style.color='white';e.style.backgroundColor='%s';", color));
        }
    }

    private Set<ResultItem> getMatchedItems(Function<String, String> asColor, Predicate<String> linkPredicate) {
        try {
            final List<String> titles = resultItems.valuesOf(item -> item.getTitle());
            List<String> links = resultItems.valuesOf(item -> item.link.getURL());

            List<RunnableThrowable> applyColors = new ArrayList<>();
            for (int i = 0; i < titles.size(); i++) {
                final Integer index = i;
                String color = asColor.apply(titles.get(i));
                applyColors.add(() -> updateTitleColor(index, color));
            }

            Executor.runParallel(applyColors);

            Set<ResultItem> matchedResults = IntStream.range(0, links.size()).boxed()
                    .filter(j -> linkPredicate.test(links.get(j)))
                    .map(j -> resultItems.get(j))
                    .collect(Collectors.toSet());
            return matchedResults;
        } catch (Exception e) {
            Logger.I(e);
            return new TreeSet<ResultItem>();
        }

    }

    private UINavigator getNavigatorByUrl() {
        String url = getWorker().getUrl().toString();
        if (StringUtils.containsIgnoreCase(url, "google.com")) {
            return new UINavigator(this, By.cssSelector("div#navcnt tbody"), By.cssSelector("td span"));
        } else if (StringUtils.containsIgnoreCase(url, "aol.com")) {
            return new UINavigator(this, By.cssSelector("div.compPagination"), By.cssSelector("div.compPagination>strong, div.compPagination>a"));
        } else if (StringUtils.containsIgnoreCase(url, "baidu.com")) {
            return new UINavigator(this, By.cssSelector("div#page"), By.cssSelector("div#page>a, div#page>strong"));
        } else if (StringUtils.containsIgnoreCase(url, "ask.com")) {
            return new UINavigator(this, By.cssSelector("ul.PartialWebPagination"), By.cssSelector("a"));
        } else if (StringUtils.containsIgnoreCase(url, "bing.com")) {
            return new UINavigator(this, By.cssSelector("nav[role='navigation'] h4+ul"), By.cssSelector("li>a"));
        }

        throw new IllegalStateException("The UINavigator must be defined with at least one locator!");
    }

    public static final Pattern dateInSearchResultPattern = Pattern.compile(
            "^((\\w{3} \\d{1,2}, \\d{4})" + "|" +       //Google & Ask
                    "(\\d{4}年\\d{1,2}月\\d{1,2}日)" + "|" +     //Baidu
                    "(\\d{4}-\\d{1,2}-\\d{1,2}))");             //Bing

}
