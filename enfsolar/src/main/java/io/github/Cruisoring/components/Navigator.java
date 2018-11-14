package io.github.Cruisoring.components;

import io.github.Cruisoring.interfaces.WorkingContext;
import io.github.Cruisoring.wrappers.UICollection;
import io.github.Cruisoring.wrappers.UIObject;
import org.openqa.selenium.By;

public class Navigator extends UIObject {
    public static final By navigatorBy = By.cssSelector("div.mk-body>div>nav>ul");

    public final UICollection pages;
    public final UIObject currentPage;
    public final UIObject prevPage;
    public final UIObject nextPage;
    public Navigator(WorkingContext context) {
        super(context, navigatorBy);
        pages = new UICollection(this, By.cssSelector("li>a"));
        currentPage = new UIObject(this, By.cssSelector("li>a.active"));
        prevPage = new UIObject(this, By.cssSelector("li>a>i.fa-chevron-left"));
        nextPage = new UIObject(this, By.cssSelector("li>a>i.fa-chevron-right"));
    }

    public void openPage(int pageNo){
        UIObject page = pages.get(String.valueOf(pageNo));
        page.click();
    }

    public int getCurrentPage(){
        return Integer.valueOf(currentPage.getTextContent());
    }

    public boolean isLast() {
        return !nextPage.isVisible();
    }

    public boolean goNextPage(){
        nextPage.click(-1);
        return isLast();
    }
}
