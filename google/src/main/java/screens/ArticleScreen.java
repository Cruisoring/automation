package screens;

import io.github.Cruisoring.helpers.Worker;
import io.github.Cruisoring.wrappers.Screen;
import io.github.Cruisoring.wrappers.UICollection;
import io.github.Cruisoring.wrappers.UIObject;
import org.openqa.selenium.By;

public class ArticleScreen extends Screen {

    public final UIObject title;
    public final UIObject content;
    public final UICollection<UIObject> headers;
    public final UICollection<UIObject> posts;
    public final UIObject repository;

    public ArticleScreen(Worker worker){
        super(worker);
        title = new UIObject(worker, By.cssSelector("div.title"));
        content = new UIObject(worker, By.cssSelector("div#contentdiv"));

        headers = new UICollection<>(content, By.cssSelector("h2,h3,h4"));
        posts = new UICollection<>(content, By.cssSelector("ol>li>a[href*='echniques']"));
        repository = new UIObject(content, By.cssSelector("a[href*='Cruisoring/functionExtensions']"));
    }

    @Override
    public void invalidate(){
        super.invalidate();
        headers.invalidate();
        posts.invalidate();
    }


    public void read(){
        title.waitDisplayed();
        int headCount = headers.getElementsCount();
        for (int i = 0; i < headCount; i++ ) {
            UIObject head = headers.get(i);
            head.scrollIntoViewByScript(true);
            waitPageReady();
        }
        int postCount = posts.getChildren().size();
        for (int i = 0; i < postCount; i++) {
            UIObject post = posts.get(i);
            post.click();
            worker.waitPageReady();
            worker.goBack();
        }

        repository.waitDisplayed();
        repository.click();
    }
}
