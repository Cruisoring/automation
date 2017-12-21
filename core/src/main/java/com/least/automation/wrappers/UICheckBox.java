package com.least.automation.wrappers;

import com.least.automation.helpers.Logger;
import com.least.automation.interfaces.WorkingContext;
import org.openqa.selenium.By;

/**
 * Wrapper of CheckBox or Radio Button that have state of Checked and UnChecked.
 */
public class UICheckBox extends UIObject  {
    public static final Class UICheckBoxClass = UICheckBox.class;

    public static class Collection extends UICollection<UICheckBox> {
        public Collection(WorkingContext context, By by, Integer index, By childrenBy){
            super(context, by, index, UICheckBoxClass, childrenBy);
        }

        public Collection(WorkingContext context, By by, By childrenBy){
            this(context, by, 0, childrenBy);
        }

        public Collection(UIObject context, By childrenBy) {
            super(context.parent, context.locator, null, UICheckBoxClass, childrenBy);
        }
    }

    public UICheckBox(WorkingContext context, By by, Integer index) {
        super(context, by, index);
    }

    public UICheckBox(WorkingContext context, By by) {
        super(context, by);
    }


    public Boolean isChecked() {
        Boolean result = getFreshElement().isSelected();
        return result;
    }

    public Boolean setChecked(Boolean toCheck) {
        return super.perform(()->click(), ()-> toCheck == isChecked(), 1);
    }

    @Override
    public Boolean perform(String trueOrFalse) {
        Boolean checkOrNot = Boolean.parseBoolean(trueOrFalse);

        Boolean result = super.perform(()->click(), ()-> checkOrNot == isChecked(), 1);
        if(result) {
            Logger.D("%s is %s", this, checkOrNot ? "checked" : "unchecked");
        }
        return result;
    }

}
