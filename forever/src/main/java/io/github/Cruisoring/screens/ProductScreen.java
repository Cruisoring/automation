package io.github.Cruisoring.screens;

import io.github.Cruisoring.helpers.StringExtensions;
import io.github.Cruisoring.helpers.Worker;
import io.github.Cruisoring.wrappers.UIImage;
import io.github.Cruisoring.wrappers.UIObject;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;

import java.awt.image.BufferedImage;
import java.net.URL;

public class ProductScreen extends BaseScreen {
    private final UIObject productTitle;
    private final UIObject itemPrice;
    private final UIObject description;
    private final UIImage picture;
    private final UIImage.Collection thumbnail;

    public ProductScreen(Worker worker) {
        super(worker);
        productTitle = new UIObject(this, By.cssSelector("h1#h1Title"));
        itemPrice = new UIObject(this, By.cssSelector("div#ItemPrice>div"));
        description = new UIObject(this, By.cssSelector("div#divDescription>section>div.d_content"));
        picture = new UIImage(this, By.cssSelector("div#pic_container img"));
        thumbnail = new UIImage.Collection(this, By.cssSelector("div.thumbnail"), By.cssSelector("ul>li img"));
    }

    public String getProductName(){
        return productTitle.getTextContent();
    }

    public float getPrice(){
        String priceText = itemPrice.getTextContent();
        priceText = priceText.substring(priceText.indexOf("$")+1);
        float price = StringExtensions.asFloat(priceText, -1f);
        return price;
    }

    public String getDescription(){
        String text = description.getTextContent();
        text = text.replaceAll("^\"*|\"*$", "");
        return text;
    }

    public URL getPictureUrl(){
        return picture.getURL();
    }

    public UIImage getImage(int index){
        return thumbnail.get(index);
    }

}
