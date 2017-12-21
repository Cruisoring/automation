package screens;

import com.least.automation.helpers.Worker;
import com.least.automation.wrappers.Screen;
import com.least.automation.wrappers.UICheckBox;
import com.least.automation.wrappers.UIEdit;
import com.least.automation.wrappers.UIObject;
import org.openqa.selenium.By;

public class LoginScreen extends Screen {
    public final UIObject signIn;
    public final UIEdit email;
    public final UIEdit password;
    public final UICheckBox rememberMe;
    public final UIObject signInButton;

    public LoginScreen(Worker worker){
        super(worker);
        signIn = new UIObject(this, By.cssSelector("a.t-sign-in"));
        email = new UIEdit(this, By.cssSelector("input#id_email"));
        password = new UIEdit(this, By.cssSelector("input#id_password1"));
        rememberMe = new UICheckBox(this, By.cssSelector("input[name='remember']"));
        signInButton = new UIObject(this, By.cssSelector("input#login"));
    }

    public boolean login(){
        if(waitPageReady() && !signIn.isVisible())
            return true;
        signIn.click();
        if(!waitScreenVisible())
            return false;

        email.enterByScript("william.jiang@apa.com.au");
        password.enterByScript("p@ssW0rd");
        rememberMe.setChecked(true);
        signInButton.click(100);

        return waitScreenGone();
    }
}
