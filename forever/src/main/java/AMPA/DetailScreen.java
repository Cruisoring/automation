package AMPA;

import io.github.Cruisoring.helpers.StringExtensions;
import io.github.Cruisoring.helpers.Worker;
import io.github.Cruisoring.wrappers.Screen;
import io.github.Cruisoring.wrappers.UIObject;
import org.openqa.selenium.By;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class DetailScreen extends Screen {

    public final UIObject Name;
    public final UIObject Contact;
//    public final UIObject Address;
//    public final UIObject City;
//    public final UIObject Telephone;
//    public final UIObject Fax;
//    public final UIObject Email;

    protected DetailScreen(Worker worker) {
        super(worker);
        Name = new UIObject(this, By.cssSelector("h3"), 0);
        Contact = new UIObject(this, By.cssSelector("div.col-xs-12.col-sm-8"));
//        Address = new UIObject(Contact, By.cssSelector("dl>dd"));
//        City = new UIObject(Contact, )
    }

    static String[] keywords = new String[] {
            "Dirección" //Address
            , "Barrio"   //City
            , "Teléfono"
            , "Fax"
            , "Correo"
            , "Añadir a mi libreta de direcciones"
    };

    public Map<String, String> getDetails(){
        String html = Contact.getOuterHTML();
        String contract = StringExtensions.extractHtmlText(html);

        Map<String, String> details = new HashMap<>();
        details.put("Name", Name.getTextContent());

        Integer[] indexes = Arrays.stream(keywords)
                .map(key -> contract.indexOf(key))
                .toArray(size -> new Integer[size]);

        String address = null, city = null, telephone = null, fax = null, email = null;
        int end = indexes[5] == -1 ? contract.length() : indexes[5];

        if (indexes[4] != -1) {
            email = contract.substring(keywords[4].length() + indexes[4], end).trim();
            details.put("Email", email);
            end = indexes[4];
        }

        if (indexes[3] != -1) {
            fax = contract.substring(keywords[3].length() + indexes[3], end).trim();
            fax = fax.replaceAll("Fax", "").trim();
            details.put("Fax", fax);
            end = indexes[3];
        }

        if (indexes[2] != -1) {
            telephone = contract.substring(keywords[2].length() + indexes[2], end).trim();
            telephone = telephone.replace("Work", "").trim();
            details.put("Telephone", telephone);
            end = indexes[2];
        }

        if (indexes[1] != -1) {
            city = contract.substring(keywords[1].length() + indexes[1]+2, end).trim();
            city = city.replace("Distrito", "");
            details.put("City", city);
            end = indexes[1];
        }

        return details;
    }
}
