package io.github.Cruisoring.wrappers;

import io.github.Cruisoring.interfaces.WorkingContext;
import io.github.cruisoring.Functions;
import io.github.cruisoring.tuple.Tuple;
import io.github.cruisoring.tuple.Tuple2;
import org.openqa.selenium.By;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.Objects;

public class UIImage extends UIObject {
    public static final Class UIImageClass = UIImage.class;
    public static final By defaultImgLocator = By.cssSelector("img");

    public static class Collection extends UICollection<UIImage> {
        public Collection(WorkingContext context, By by, Integer index, By childrenBy){
            super(context, by, index, UIImageClass, childrenBy);
        }

        public Collection(WorkingContext context, By by, By childrenBy){
            this(context, by, 0, childrenBy);
        }

        public Collection(UIObject context, By childrenBy) {
            super(context.parent, context.locator, null, UIImageClass, childrenBy);
        }

        public Collection(UIObject context) {
            super(context.parent, context.locator, null, UIImageClass, defaultImgLocator);
        }
    }

    public UIImage(WorkingContext context, By by, Integer index) {
        super(context, by, index);
    }

    public UIImage(WorkingContext context, By by) {
        super(context, by);
    }

    public UIImage(WorkingContext context) {
        super(context, defaultImgLocator);
    }

    @Override
    public String getTextContent() {
        return getAttribute("alt");
    }

    public URL getURL() {
        String src = getAttribute("src");
        URL contextUrl = worker.getUrl();
        try {
            URL imgUrl = new URL(contextUrl, src);
            return imgUrl;
        }catch (Exception ex){
            try {
                URL asAbolute = new URL(src);
                return asAbolute;
            }catch (Exception e){
                return contextUrl;
            }
        }
    }

    public BufferedImage getImage(){
        try {
            return ImageIO.read(getURL());
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public String asBase64(){
        return super.asBase64();
    }
}
