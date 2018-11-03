package io.github.Cruisoring.components;

import io.github.Cruisoring.interfaces.WorkingContext;
import io.github.Cruisoring.wrappers.UIObject;
import org.openqa.selenium.By;

public class PIContainer extends UIObject {
    public static final By piContainerBy = By.cssSelector("div.pi_container");

    public PIContainer(WorkingContext context, By by, Integer index) {
        super(context, by, index);
    }

//    public PIContainer(WorkingContext context, Integer index) {
//        super(context, piContainerBy, index);
//    }
}
