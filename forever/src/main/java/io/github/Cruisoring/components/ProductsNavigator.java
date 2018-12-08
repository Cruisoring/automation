package io.github.Cruisoring.components;

import io.github.Cruisoring.helpers.Logger;
import io.github.Cruisoring.helpers.StringExtensions;
import io.github.Cruisoring.interfaces.WorkingContext;
import io.github.Cruisoring.wrappers.UICollection;
import io.github.Cruisoring.wrappers.UIObject;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ProductsNavigator extends UIObject {
    private static final int defualtWaitPageLoadingMills = 20*1000;
    public static final By containerBy = By.cssSelector("div.view_grid");

    private final UIObject productCount;
    private final UIObject prevPage;
    private final UICollection pages;
    private final UIObject currentPage;
    private final UIObject nextPage;

    public ProductsNavigator(WorkingContext context, By by, Integer index) {
        super(context, by, 0);
        productCount = new UIObject(this, By.cssSelector("div#products-count"));
        prevPage = new UIObject(this, By.cssSelector("div#top-pager>ul>li>span.p_prev"));
        nextPage = new UIObject(this, By.cssSelector("div#top-pager>ul>li>span.p_next"));
        currentPage = new UIObject(this, By.cssSelector("div#top-pager>ul>li.underline"));
        pages = new UICollection(this, By.cssSelector("div#top-pager>ul"), By.cssSelector("li.pageno"));
    }

    public ProductsNavigator(WorkingContext context, By by) {
        this(context, by, null);
    }

    public ProductsNavigator(WorkingContext context) {
        this(context, containerBy, null);
    }

    public int getProductCount(){
        String text = productCount.getTextContent();
        text = text.substring(0, text.indexOf(" "));
        return Integer.valueOf(text);
    }

    public int getPageCount(){
        List<String> pageTexts = pages.asTexts();
        for(int i = pageTexts.size()-1; i>=0; i--) {
            String text = pageTexts.get(i);
            if(StringUtils.isNotEmpty(text))
                return Integer.valueOf(text);
        }
        return -1;
    }

    public int getCurrentPage(){
        invalidate();
        String currentPageNo = currentPage.getTextContent();
        return StringExtensions.asInt(currentPageNo, -1);
    }

    public boolean gotoPrevPage(){
        waitPageReady();
        if(getCurrentPage() <= 1)
            return false;
        return prevPage.click(defualtWaitPageLoadingMills);
    }

    public boolean gotoNextPage(){
        waitPageReady();
        if(getCurrentPage() == getPageCount())
            return false;
        boolean result = nextPage.click(defualtWaitPageLoadingMills);
        return result;
    }

    private boolean gotoPage(int pageNo, int pageCount){
        Logger.V("pageNo=%d, pageCount=%d", pageNo, pageCount);
        if(pageCount < 0)
            pageCount = getPageCount();

        if(pageNo < 1 || pageNo > pageCount){
            return false;
        } else if(pageNo == getCurrentPage()) {
            return true;
        }

        waitPageReady();
        List<String> pageNumbers = pages.asTexts();
        List<Integer> numbers = pageNumbers.stream()
                .map(text -> StringExtensions.asInt(text, -1))
                .collect(Collectors.toList());

        int index = numbers.indexOf(pageNo);
        if(index != -1){
            pages.get(index).click(defualtWaitPageLoadingMills);
            waitPageReady();
            return getCurrentPage() == pageNo;
        }

        int currentPageNo = getCurrentPage();
        if(currentPageNo < pageNo){
            Integer max = numbers.stream()
                    .filter(n -> n < pageNo)
                    .max(Comparator.naturalOrder()).orElse(-1);
            if(max != -1){
                index = numbers.indexOf(max);
                pages.get(index).click(defualtWaitPageLoadingMills);
                return gotoPage(pageNo, pageCount);
            }
        } else {
            Integer min = numbers.stream()
                    .filter(n -> n > pageNo)
                    .min(Comparator.naturalOrder()).orElse(-1);
            if (min != -1) {
                index = numbers.indexOf(min);
                pages.get(index).click(defualtWaitPageLoadingMills);
                return gotoPage(pageNo, pageCount);
            }
        }
        return false;
    }

    public boolean gotoPage(int pageNo){
        return gotoPage(pageNo, -1);
    }
}
