package io.github.Cruisoring.screens;

import io.github.Cruisoring.helpers.Worker;
import io.github.Cruisoring.wrappers.Screen;
import io.github.Cruisoring.wrappers.UILink;
import io.github.Cruisoring.wrappers.UIObject;
import org.openqa.selenium.By;

public class InstallerScreen extends Screen {
    public final UIObject name;
    public final UIObject profile;
    public final UIObject address;
    public final UIObject telephone;
    public final UIObject email;
    public final UILink url;

    protected InstallerScreen(Worker worker) {
        super(worker);
        name = new UIObject(this, By.cssSelector("h1.blue-title"));
        profile = new UIObject(this, By.cssSelector("div.enf-company-profile-info-main-spec"));
        address = new UIObject(profile, By.cssSelector("td[itemprop='address']"));
        telephone = new UIObject(profile, By.cssSelector("td[itemprop='telephone']"));
        email = new UIObject(profile, By.cssSelector("td[itemprop='email']"));
        url = new UILink(profile, By.cssSelector("a[itemprop='url']"));
    }

    public String getName(){
        return name.getTextContent().trim();
    }

    public String getAddress(){
        return address.isVisible() ? address.getTextContent().trim() : null;
    }

    public String getTelephone(){
        return telephone.isVisible() ? telephone.getTextContent().trim() : null;
    }

    public String getEmail(){
        if(email == null)
            return null;
        email.click(5000);
        return email.getTextContent().trim();
    }

    public String getUrl(){
        return url.isVisible() ? url.getTextContent().trim() : null;
    }
}
