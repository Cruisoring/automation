package io.github.Cruisoring.helpers;

import com.google.common.base.Charsets;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Helper class for resource retrieval.
 */
public class ResourceHelper {
    public static final String PROJECT_FOLDER_NAME = "automation";
    public static final String TARGET_FOLDER_NAME = "target";
    public static final String RESULTS_FOLDER_NAME = "target/cucumber";

    public final static String[] resourcePaths;
    public final static File resultFileFolder;
    static {
        resourcePaths = getResourcePaths();

        String firstPath = resourcePaths[0];
        firstPath = firstPath.substring(0, firstPath.indexOf("src"));
        resultFileFolder = new File(firstPath, RESULTS_FOLDER_NAME);
    }

    public static<T> T createInstance(Class<? extends T> klass){
        Objects.requireNonNull(klass);
        try {
            Constructor[] ctors = klass.getConstructors();
            for (int i = 0; i < ctors.length; i++) {
                Constructor ctor = ctors[i];
                if (ctor.getGenericParameterTypes().length == 0) {
                    ctor.setAccessible(true);
                    return (T)ctor.newInstance();
                }
            }
            return null;
        }catch (Exception ex){
            return null;
        }
    }

    /**
     * Retrive the ORIGINAL resources folders of all modules involved with the call
     * @return  String array identifying the absolute paths of related resource folders
     */
    private static String[] getResourcePaths(){
        List<String> classPaths = new ArrayList<>();
        List<String> classNames = getCallerClassNames();
        for(int i = 0; i < classNames.size(); i++){
            try{
                String className = classNames.get(i);
                Class clazz = Class.forName(className);
                String classPath = clazz.getProtectionDomain().getCodeSource().getLocation().getPath();
                if (className.contains("custom")) {
                    Logger.I("[%d]: %s -> %s", i, className, classPath);
                }

                if(classPath.endsWith("target/classes/")){
//                    log.info(String.format("[%d]: %s", i, classPath));
                    classPath = classPath.replace("target/classes/", "src/main/resources/");
                } else if (classPath.endsWith("target/test-classes/")) {
//                    log.info(String.format("[%d]: %s", i, classPath));
                    classPath = classPath.replace("target/test-classes/", "src/test/resources/");
//                } else if (classPath.contains("target/classes") || classPath.contains("target/test-classes")) {
//                    log.info(String.format("Unhandled classpath: [%d] %s", i, classPath));
                } else {
                    continue;
                }

                if(!classPaths.contains(classPath)){
                    classPaths.add(classPath);
                }
            }catch (Exception ex){
                continue;
            }
        }
        Collections.reverse(classPaths);
        String lastPath = classPaths.get(classPaths.size()-1);
//        if(!lastPath.contains(CUSTOMCORE_MODULE_NAME)){
//            String customCoreModuleResourcePath = lastPath.substring(0, lastPath.indexOf(PROJECT_FOLDER_NAME))
//                    + "/" + PROJECT_FOLDER_NAME + "/" + CUSTOMCORE_MODULE_NAME + "/src/main/resources/";
//            classPaths.add(customCoreModuleResourcePath);
//        }
        String[] result = classPaths.toArray(new String[0]);
        ReportHelper.reportAsStepLog("Resource Paths detected:\n" + StringUtils.join(result, '\n'));
        return result;
    }


    /**
     * Get the caller class who calls any methods of the baseTestRunner
     * @return Class of the Caller.
     */
    public static List<String> getCallerClassNames(){
        StackTraceElement[] stacks = new Throwable().getStackTrace();
        List<String> classNames = Arrays.stream(stacks).skip(1).map(stack -> stack.getClassName()).collect(Collectors.toList());

        return classNames;
    }

    public static List<String> getCallerModuleNames(){
        List<String> callerClassNames = getCallerClassNames();
        List<String> moduleNames = new ArrayList<>();
        for (String className: callerClassNames) {
            try {
                Class clazz = Class.forName(className);
                String moduleName = getModuleName(clazz);
                moduleNames.add(moduleName);
            }catch (Exception ex){
                break;
            }
        }

        return moduleNames;
    }

    /**
     * Get the caller class who calls any methods of the baseTestRunner
     * @return Class of the Caller.
     */
    public static Class getCallerClass(){
        List<String> callers = getCallerClassNames();
        String thisClassName = ResourceHelper.class.getName();
        try{
            String externalCaller = callers.stream().filter(caller -> !caller.contains(thisClassName)).findFirst().orElse(null);
            Class callerClass = Class.forName(externalCaller);
            return callerClass;
        } catch (Exception ex){
            return null;
        }
    }

    public static String getModuleName(Class clazz){
        Objects.requireNonNull(clazz);

        String classPath = clazz.getProtectionDomain().getCodeSource().getLocation().getPath();
        int index = classPath.indexOf(PROJECT_FOLDER_NAME);
        int start = classPath.indexOf("/", index);
        int end = classPath.indexOf("/", start+1);
        return classPath.substring(start+1, end);
    }

    /**
     * Retrieve the solic file from any possible module.
     * @param filename Name of the file to be handled.
     * @param folderNames Directory names of the file.
     * @return File instance if it exist, otherwise null.
     */
    public static File getResourceFile(String filename, String... folderNames){
        Objects.requireNonNull(filename);

        String folderPath = folderNames == null ? "" : String.join("/", folderNames);

        //Try to load properties if it is not loaded by previous resourcePaths.
        for (String path : resourcePaths) {
            File resourceFolder = new File(path, folderPath);
            File file = new File(resourceFolder, filename);
            //No such resources defined, continue
            if (!file.exists()){
                continue;
            }

            return file;
        }

        return null;
    }

    /**
     * Locate the resource identified with filename and its folder names from any possible module.
     * @param filename Name of the file to be handled.
     * @param folderNames Directory names of the file.
     * @return the absolute file path if it is found, or null when there is no such resource.
     */
    public static Path getResourcePath(String filename, String... folderNames){
        Objects.requireNonNull(filename);

        String folderPath = folderNames == null ? "" : String.join("/", folderNames);

        //Try to load properties if it is not loaded by previous resourcePaths.
        for (String path : resourcePaths) {
            File resourceFolder = new File(path, folderPath);
            File file = new File(resourceFolder, filename);
            //No such resources defined, continue
            if (!file.exists()){
                continue;
            }

            return file.toPath();
        }

        String error = String.format("Failed to locate %s in folder of %s from %s", filename, folderPath, StringUtils.join(resourcePaths, ','));
        throw new RuntimeException(error);
    }

    /**
     * Retrieve the absolute file path from any possible module.
     * @param filename Name of the file to be handled.
     * @param folderNames Directory names of the file.
     * @return Path of the expected file.
     */
    public static Path getAbsoluteFilePath(String filename, String... folderNames){
        Objects.requireNonNull(filename);

        String folderPath = folderNames == null ? "" : String.join("/", folderNames);

        //Output Folder in the original caller module target directory
        File folder = new File(resourcePaths[0], folderPath);

        File file = new File(folder, filename);
        return file.toPath();
    }

    /**
     * With given input filename, and optional result filename, get the final File instance to keep the result.
     * @param filename    Optional result filename to be used.
     * @param folderNames Folder segments to locate the input file.
     * @return
     */
    public static File getResultFile(String filename, String... folderNames){
        Objects.requireNonNull(filename);

        String folderPath = folderNames == null ? "" : String.join("/", folderNames);
        File outputFolder = new File(resultFileFolder, folderPath);

        File resultFile = new File(outputFolder, filename);
        return resultFile;
    }

    /**
     * Retrieve the absolute file path in the test result folder.
     * @param filename Name of the file to be handled.
     * @return Path of the expected file.
     */
    public static Path getResultFilePath(String filename, String... folderNames){
        return getResultFile(filename, folderNames).toPath();
    }

    /**
     * Locate the resource identified with filename and its folder names from any possible module or external resource.
     * @param filename Name of the file to be handled.
     * @param folderNames Directory names of the file.
     * @return the absolute file path if it is found, or null when there is no such resource.
     */

    public static File getInputFile(String filename, String... folderNames) {
        String inExcelFileSystemProperty = System.getProperty("InExcelFile");

        if (inExcelFileSystemProperty != null) {
            ReportHelper.reportAsStepLog("InExcelFile is defined as System Property %s", "'InExcelFile'");
            filename = inExcelFileSystemProperty;
        }

        String simpleFilename = filename.trim();

        if (StringUtils.startsWithIgnoreCase(filename, "file:"))
            simpleFilename = filename.substring("file:".length());

        File asAbsoluteFile = new File(simpleFilename);

        if (asAbsoluteFile != null && asAbsoluteFile.exists()) {
            ReportHelper.reportAsStepLog("InExcelFile is located at : %s", asAbsoluteFile);
            return asAbsoluteFile;
        }

        try {
            URL url = new URL(filename);
            asAbsoluteFile = new File(url.toURI());
        } catch (Exception e) {
            try {
                URI uri = URI.create(simpleFilename);
                asAbsoluteFile = new File(uri);
                if (asAbsoluteFile.exists()) {
                    ReportHelper.reportAsStepLog("InExcelFile is located at :%s", asAbsoluteFile);
                    return asAbsoluteFile;
                }
            } catch (Exception e1) {
            }
        }
        if (asAbsoluteFile != null && asAbsoluteFile.exists()) {
            ReportHelper.reportAsStepLog("InExcelFile is located at : %s", asAbsoluteFile);
            return asAbsoluteFile;
        }

        return getResourceFile(filename, folderNames);

    }

    /**
     * Check to see if there is a relative resource identified by the resourceFilename.
     * @param resourceFilename  The relative path of the reourcefile to be checked.
     * @return  'True' if the relative path exists, "False" if not.
     */
    public static Boolean isResourceAvailable(String resourceFilename){
        return getResourcePath(resourceFilename) != null;
    }

    /**
     * Retrieve the content of the resource file a String.
     * @param resourceFilename The relative path of the reourcefile to be checked.
     * @param folders Optional folder names.
     * @return NULL if there is no such resource identified by the relative path, or content of the resource as a String.
     */
    public static String getTextFromResourceFile(String resourceFilename, String... folders){
        Path path = getResourcePath(resourceFilename, folders);
        Logger.I("%s would be extracted from %s", resourceFilename, path);

        if(path == null){
            return null;
        }

        try {
            byte[] encoded = Files.readAllBytes(path);
            String text = new String(encoded, Charsets.UTF_8);
            return text;
        } catch (IOException e) {
            return null;
        }
    }

    public static Properties getProperties(String propertiesFilename) {
        for (String path : resourcePaths) {
            File file = new File(path, propertiesFilename);
            if (!file.exists())
                continue;

            Properties properties = new Properties();
            try {
                properties.load(new FileReader(file));
                return properties;
            } catch (IOException e){
                Logger.W(e);
            }
        }
        return null;
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

        //Try to load properties if it is not loaded by previous resourcePaths.
        for (String path : resourcePaths) {
            File resourceFolder = new File(path, resourcePackagename);
            //No such resources defined, continue
            if (!resourceFolder.exists()){
                continue;
            }

            File[] propertiesFiles = resourceFolder.listFiles();
            for (File f : propertiesFiles ) {
                if(f.isFile()){
                    addFile(f, result);
                } else if(f.isDirectory()){
                    addDirectory(f, result);
                }
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
        //Avoid override existing properties identified by same case-insensitive key
        if(MapHelper.containsIgnoreCase(result, propertiesName))
            return;

        Properties properties = new Properties();
        try {
            properties.load(new FileReader(file));
        } catch (IOException e){
            Logger.W(e);
            return;
        }
        result.put(propertiesName, properties); //Let it throw Exception if there is duplicated keys.
    }
}