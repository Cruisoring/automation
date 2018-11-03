package io.github.Cruisoring.helpers;

import io.github.cruisoring.Functions;
import io.github.cruisoring.tuple.Tuple;
import io.github.cruisoring.tuple.Tuple2;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.Objects;

public class ImageHelper {

    /**
     * Get the bytes of given image with format specified.
     * @param image         Image to be converted.
     * @param formatName    Image format.
     * @return              Byte array of the given image.
     */
    public static byte[] toBytes(BufferedImage image, String formatName){
        Objects.requireNonNull(image);

        try(
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();){
            if(!ImageIO.write(image, formatName, byteArrayOutputStream))
                return null;
            byteArrayOutputStream.flush();
            byte[] bytes = byteArrayOutputStream.toByteArray();
            return bytes;
        }catch (Exception ex){
            return null;
        }
    }

    /**
     * Parse the given URL to get both BufferedImage and its format.
     * @param imageUrl  URL of the concerned image.
     * @return          Tuple of both the BufferedImage and its format.
     */
    public static Tuple2<BufferedImage, String> getTypedImage(URL imageUrl){
        Objects.requireNonNull(imageUrl);

        try (
                InputStream inputStream = (InputStream) Functions.ReturnsDefaultValue.apply(() -> imageUrl.openStream());
                ImageInputStream imageInputStream = ImageIO.createImageInputStream(inputStream);
        ){
            Iterator iter = ImageIO.getImageReaders(imageInputStream);
            if (!iter.hasNext()) {
                return null;
            }
            ImageReader reader = (ImageReader) iter.next();
            reader.setInput(imageInputStream);
            String format = reader.getFormatName();
            BufferedImage image = reader.read(0);
            return Tuple.create(image, format);
        }catch (Exception e){
            return null;
        }
    }

}
