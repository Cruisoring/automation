package io.github.Cruisoring.helpers;

import io.github.Cruisoring.helpers.ImageHelper;
import io.github.Cruisoring.helpers.Logger;
import io.github.cruisoring.tuple.Tuple2;
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.*;

public class ImageHelperTest {

    @Test
    public void getTypedImage() throws IOException {
        URL imageUrl = new URL("https://www.forever21.com/images/1_front_58/00307554-03.jpg");
        Tuple2<BufferedImage, String> formatImage = ImageHelper.getTypedImage(imageUrl);
        Logger.I(formatImage.toString());

        byte[] bytes = ImageHelper.toBytes(formatImage.getFirst(), formatImage.getSecond());
        Logger.I("Size of bytes: %d", bytes.length);

        ImageIO.write(formatImage.getFirst(), formatImage.getSecond(), new File("C:/temp/forever_sample.jpg"));
    }
}