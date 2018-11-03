package io.github.Cruisoring.screens;

import io.github.Cruisoring.components.PIContainer;
import io.github.Cruisoring.components.ProductsNavigator;
import io.github.Cruisoring.helpers.Worker;
import io.github.Cruisoring.wrappers.UICollection;
import org.openqa.selenium.By;

public class ListScreen extends BaseScreen {
    protected final ProductsNavigator pages;
    protected final UICollection<PIContainer> products;

    public ListScreen(Worker worker) {
        super(worker);
        pages = new ProductsNavigator(worker);
        products = new UICollection<PIContainer>(this, By.cssSelector("div#products"), 0,
                (c, i) -> new PIContainer(c, PIContainer.piContainerBy, i));
    }

    public int getProductCount() {
        return pages.getProductCount();
    }

    public int getPageCount() {
        return pages.getPageCount();
    }

    public int getCurrentPage() {
        return pages.getCurrentPage();
    }

    public boolean gotoPrevPage() {
        return pages.gotoPrevPage();
    }

    public boolean gotoNextPage() {
        return pages.gotoNextPage();
    }

    public boolean gotoPage(int pageNo) {
        waitPageReady();
        return pages.gotoPage(pageNo);
    }

    public PIContainer getProduct(int index){
        return products.get(index);
    }

    public int getDisplayedCount(){
        return products.size();
    }
}
