package io.github.Cruisoring.helpers;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.InvalidArgumentException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Helper class for resource retrieval.
 */
public class ResourceHelper {
    public static final String className = ResourceHelper.class.getName();
    /**
     * Check to see if there is a relative resource identified by the resourceFilename.
     * @param resourceFilename  The relative path of the reourcefile to be checked.
     * @return  'True' if the relative path exists, "False" if not.
     */
//    public static Boolean isResourceAvailable(ClassLoader classLoader, String resourceFilename){
//        URL url = classLoader.getResource(resourceFilename);
//        return url != null;
//    }

    /**
     * Retrieve the content of the resource file a String.
     * @param resourceFilename The relative path of the reourcefile to be checked.
     * @return NULL if there is no such resource identified by the relative path, or content of the resource as a String.
     */
    public static String getTextFromResourceFile(String resourceFilename){
        URL url = ResourceHelper.class.getClassLoader().getResource(resourceFilename);
        if(url == null)
            return null;

        String sql = null;
        try {
            sql = Resources.toString(url, Charsets.UTF_8);
        } catch (IOException e) {
           Logger.E(e.getMessage());
        }
        return sql;
    }

    /**
     * Retrieve all properties resources as a named HashMap.
     * @param resourcePackagename   Relative package name containing the properties.
     * @param result    A map from the caller to be filled with the properties.
     * @return  A map containing all the properties within the targeted package.
     */
    public static Map<String, Properties> getAllProperties(String resourcePackagename, Map<String, Properties> result){
        if(result == null)
            result = new HashMap<>();
        ClassLoader classLoader = ResourceHelper.class.getClassLoader();
        String path = classLoader.getResource(resourcePackagename).getPath();
        File file = new File(path);
        if (!file.exists()){
           Logger.E("file not found: " + resourcePackagename);
            return null;
        }

        File[] propertiesFiles = new File(path).listFiles();
        for (File f : propertiesFiles ) {
            if(f.isFile()){
                addFile(f, result);
            } else if(f.isDirectory()){
                addDirectory(f, result);
            }
        }

        return result;
    }

    /**
     * Add the properties within a directory.
     * @param file      File of the target directory.
     * @param result    Named dictionary to keep the retrieved properties.
     */
    protected static void addDirectory(File file, Map<String, Properties> result){
        File[] files = new File(file.getPath()).listFiles();

        for (File f: files ) {
            if(f.isFile()){
                addFile(f, result);
            } else if (f.isDirectory()){
                addDirectory(f, result);
            }
        }
    }

    /**
     * Add a single file as properties to the given result dictionary.
     * @param file  The instance of a single properties file.
     * @param result    Named dictionary to keep the retrieved properties.
     */
    protected static void addFile(File file, Map<String, Properties> result){

        String propertiesName = file.getName();
        propertiesName = propertiesName.substring(0, propertiesName.indexOf("."));

        Properties properties = getProperties(file);
        result.put(propertiesName, properties); //Let it throw Exception if there is duplicated keys.
    }

    public static Properties getProperties(File file){
        if (file == null || !file.exists())
            throw new InvalidArgumentException(file.toString());

        Properties properties = new Properties();
        try {
            properties.load(new FileReader(file));
        } finally {
            return properties;
        }
    }

    public static Properties getProperties(String propertyFilename){
        Class callerClass = ClassHelper.getCallerClass(s -> !StringUtils.equalsIgnoreCase(s.getClassName(), className));
        ClassLoader classLoader = callerClass.getClassLoader();
        URL url = classLoader.getResource(propertyFilename);
        if (url == null)
            return null;

        try {
            return getProperties(new File(url.toURI()));
        } catch (URISyntaxException e) {
            return null;
        }
    }

}
