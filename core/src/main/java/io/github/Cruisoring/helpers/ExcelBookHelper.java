package io.github.Cruisoring.helpers;

import io.github.cruisoring.Functions;
import io.github.cruisoring.Lazy;
import io.github.cruisoring.tuple.Tuple2;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.InvalidArgumentException;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ExcelBookHelper implements AutoCloseable{

    public static final String DEFAULT_DATE_CELL_FORMAT = "d/mm/yyyy";
    public static final java.awt.Color NEGLECTED_CELL_COLOR = new java.awt.Color(0x70, 0x70, 0x70);

    public static final String SHEET_ROLE_INDICATOR = "|";
    public static final String INPUT_INDICATOR = SHEET_ROLE_INDICATOR + "IN";
    public static final String OUTPUT_INDICATOR = SHEET_ROLE_INDICATOR + "OUT";

    public static String DefaultKeyColumnName = "Name";
    public static String DefaultValueColumnName = "Value";

    public static final Predicate<String> IN_Predicate = name -> StringUtils.endsWithIgnoreCase(name, INPUT_INDICATOR);
    public static final Predicate<String> OUT_Predicate =  name -> StringUtils.endsWithIgnoreCase(name, OUTPUT_INDICATOR);

    public static String escapeSheetName(String originalName){
        Objects.requireNonNull(originalName);

        int roleIndicatorIndex = originalName.indexOf(SHEET_ROLE_INDICATOR);
        return roleIndicatorIndex == -1 ? originalName : originalName.substring(0, roleIndicatorIndex);
    }

    public static int asPictureType(String imageFormat) {
        Objects.requireNonNull(imageFormat);

        int pictureType = -1;
        switch (imageFormat.toUpperCase()){
            case "DIB" : return Workbook.PICTURE_TYPE_DIB;
            case "EMF" : return Workbook.PICTURE_TYPE_EMF;
            case "WMF" : return Workbook.PICTURE_TYPE_WMF;
            case "PICT" : return Workbook.PICTURE_TYPE_PICT;
            case "PNG" : return Workbook.PICTURE_TYPE_PNG;
            case "JPEG" : return Workbook.PICTURE_TYPE_JPEG;
            default: throw new IllegalArgumentException("Unkown format: " + imageFormat);
        }
    }

    public static ExcelBookHelper asWritable(File excelFile) {
        Objects.requireNonNull(excelFile);
        Workbook workbook = null;
        String filename = excelFile.getName();
        String extension = filename.substring(filename.lastIndexOf("."));
        try {
            if (".xlsx".equalsIgnoreCase(extension)) {
                workbook = new XSSFWorkbook();
            } else if (".xls".equalsIgnoreCase(extension)) {
                workbook = new HSSFWorkbook();
            } else
                throw new RuntimeException("Extension not supported: " + filename);
            if (!excelFile.exists()) {
                File directory = excelFile.getParentFile();
                if (!directory.exists()) {
                    directory.mkdirs();
                }

                FileOutputStream fileOut = new FileOutputStream(excelFile.getAbsolutePath());
                workbook.write(fileOut);
                fileOut.close();

                // Closing the workbook
                workbook.close();
            }

            return new ExcelBookHelper(excelFile, true);
        }catch (Exception ex){
            Logger.W(ex);
            return null;
        } finally{
            if(workbook != null){
                try {
                    workbook.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static Map<String, Map<String, Object>> getAllNameValues(File excelFile) throws Exception {
        return getAllNameValues(excelFile, DefaultKeyColumnName, DefaultValueColumnName);
    }

    public static Map<String, Map<String, Object>> getAllNameValues(File excelFile, String keyColumnName, String valueColumnName) throws Exception {
        Objects.requireNonNull(excelFile);
        Objects.requireNonNull(keyColumnName);
        Objects.requireNonNull(valueColumnName);

        try(ExcelBookHelper book = new ExcelBookHelper(excelFile)) {
            Map<String, Map<String, Object>> maps = book.sheets.getValue()
                    .entrySet().stream()
                    .collect(Collectors.toMap(
                            entry -> entry.getKey(),
                            entry -> entry.getValue().getKeyValues(keyColumnName, valueColumnName)
                            )
                    );

            return maps;
        }
    }

    private final File file;
    private final Boolean isXSSFWorkbook;
    private final Lazy<InputStream> inStream;
    public final Lazy<Workbook> workbook;
    protected final Lazy<CreationHelper> creationHelper;
    public final Lazy<Map<String, Integer>> sheetNameIndexes;
    public final Lazy<Map<String, ExcelSheetHelper>> sheets;

    private CellStyle headerStyle = null;
    private CellStyle autoWrap = null;
    private CellStyle hyperLink = null;
    private CellStyle highlightStyle = null;
    private CellStyle highlightDateStyle = null;
    private CellStyle deEmphasizeStyle = null;

    /**
     * Construct an ExcelBookHelper to wrap an either .xls or .xlsx file
     * @param file      File of the excel.
     * @param writable  'true' to make it writable, 'false' to make it read-only
     */
    public ExcelBookHelper(File file, Boolean writable){
        if(file == null || !file.exists()){
            throw new InvalidArgumentException("Invalid file: " + file.getAbsolutePath());
        }

        if(writable) {
            file.setWritable(writable);
        }

        this.file = file;
        inStream = new Lazy<>(()->new FileInputStream(this.file));
        String name = this.file.getName();
        String extension = name.substring(name.lastIndexOf("."));
        isXSSFWorkbook = StringUtils.equalsIgnoreCase(".xlsx", extension);
        workbook = inStream.create(stream -> (Workbook) Functions.tryGet(
                () -> isXSSFWorkbook ? new XSSFWorkbook(stream) : new HSSFWorkbook(stream))
        );
        creationHelper  = workbook.create(book -> book.getCreationHelper());
        sheetNameIndexes = workbook.create(book -> {
            int sheetCount = book.getNumberOfSheets();

            Map<String, Integer> indexes = IntStream.range(0, sheetCount).boxed()
                    .collect(Collectors.toMap(
                            i -> book.getSheetName(i),
                            i -> i
                    ));
            return indexes;
        });
        sheets = sheetNameIndexes.create(map -> map.keySet().stream()
                .collect(Collectors.toMap(
                        sheetName -> sheetName,
                        sheetName -> new ExcelSheetHelper(this, sheetName)
                ))
        );
    }

    /**
     * Construct an ExcelBookHelper to perform readonly operations
     * @param file  File of the target excel
     */
    public ExcelBookHelper(File file){
        this(file, false);
    }

    public ExcelSheetHelper getSheetHelperByName(String name){
        return new ExcelSheetHelper(this, name);
    }

    public File getFile(){
        return file;
    }

    /**
     * Retrieve all names of the contained sheets
     * @return
     */
    private Set<String> getAllSheetNames(){
        return sheetNameIndexes.getValue().keySet();
    }

    /**
     * Retrieve all names of sheets with a filter
     * @param namePredicate filter to get concerned sheet names
     * @return  a list of matched sheet names
     */
    public List<String> getSheetNames(Predicate<String> namePredicate){
        List<String> allNames = sheets.getValue().entrySet().stream()
                .map(entry -> entry.getKey())
                .collect(Collectors.toList());
        if(namePredicate == null)
            return allNames;

        List<String> matchedNames = allNames.stream()
                .filter(name -> namePredicate.test(name)).collect(Collectors.toList());
        return matchedNames;
    }

    /**
     * Get all input sheet names
     * @return  a list of input sheet names
     */
    public List<String> getInSheetNames(){
        return getSheetNames(IN_Predicate);
    }

    /**
     * Get all output sheet names
     * @return  a list of all output sheet names
     */
    public List<String> getOutSheetNames(){
        return getSheetNames(OUT_Predicate);
    }

    /**
     * Rename a specific sheet to a given new name
     * @param originalName  name of the sheet to be renamed
     * @param newName   the new name to be applied
     * @return  'true' indicate the sheet is renamed successfully, 'false" when exception thrown
     */
    public boolean changeSheetname(String originalName, String newName){
        Workbook book = workbook.getValue();
        try(OutputStream outputStream = new FileOutputStream(file)){
            int index = book.getSheetIndex(originalName);
            if(index == -1) return false;
            book.setSheetName(index, newName);
            book.write(outputStream);
            return true;
        }catch (Exception ex){
            return false;
        }
    }

    /**
     * Helper method to delete a specific sheet identified by name
     * @param sheetName name of the sheet to be deleted
     * @return  weather deletion happen successfully or not
     */
    public boolean removeSheetByName(String sheetName){
        Workbook book = workbook.getValue();
        try(OutputStream outputStream = new FileOutputStream(file)){
            int index = book.getSheetIndex(sheetName);
            if(index != -1){
                book.removeSheetAt(index);
            }
            return true;
        }catch (Exception ex){
            return false;
        }
    }

    /**
     * Clean up the bounded Excel file to rename output sheets and removed input sheets
     * @return
     */
    protected ExcelBookHelper convertToResultBook() {
        this.close();
        Workbook book = workbook.getValue();
        List<String> outSheetNames = getOutSheetNames();
        List<String> inSheetNames = getInSheetNames();
        if(outSheetNames.size() == 0 && inSheetNames.size() == 0)
            return this;
        try(OutputStream outputStream = new FileOutputStream(file)){
            int index;
            for (int i = 0; i < outSheetNames.size(); i++) {
                String oldName = outSheetNames.get(i);
                String newName = escapeSheetName(oldName);
                if(oldName.equals(newName))
                    continue;

                index = book.getSheetIndex(oldName);
                if(index != -1)
                    book.setSheetName(index, newName);
            }
            for (int i = 0; i < inSheetNames.size(); i++) {
                index = book.getSheetIndex(inSheetNames.get(i));
                if(index != -1) {
                    book.removeSheetAt(index);
                }
            }
            book.write(outputStream);
        }catch (Exception ex){
            Logger.W(ex);
        } finally {
            //Reset the sheets map since renaming and deletion might happened
            try {
                sheetNameIndexes.close();
                sheets.close();
            }catch (Exception e){}
            return this;
        }
    }

    /**
     * Retrieve the ExcelSheetHelper with it corresponding name
     * @param sheetName Name of the sheet to be processed
     * @return  ExcelSheetHelper of the concerned sheet
     */
    public ExcelSheetHelper getSheetHelper(String sheetName){
        Map<String, ExcelSheetHelper> map = sheets.getValue();
        return map.get(sheetName);
    }

    /**
     * Helper method to create a new blank sheet
     * @param sheetName Name of the sheet to be added
     * @return ExcelSheetHelper instance to process the newly created sheet
     */
    public ExcelSheetHelper createSheet(String sheetName){
        Workbook wb = workbook.getValue();

        Map<String, Integer> sheetIndexes = sheetNameIndexes.getValue();
        if(sheetIndexes.containsKey(sheetName)){
            wb.removeSheetAt(sheetIndexes.get(sheetName));
            wb.createSheet(sheetName);
            wb.setSheetOrder(sheetName, sheetIndexes.get(sheetName));
        } else {
            wb.createSheet(sheetName);
            sheetIndexes.put(sheetName, wb.getSheetIndex(sheetName));
            if(sheets.isValueInitialized()){
                sheets.getValue().put(sheetName, new ExcelSheetHelper(this, sheetName));
            }
        }

        return sheets.getValue().get(sheetName);
    }

    /**
     * Helper method to clone a sheet from given name and then renamed
     * @param fromName Name of the original sheet to be cloned
     * @param toName    New name of the newly cloned sheet
     * @return ExcelSheetHelper instance to process the newly created sheet
     */
    public ExcelSheetHelper cloneSheet(String fromName, String toName){
        Workbook wb = workbook.getValue();

        Map<String, Integer> sheetIndexes = sheetNameIndexes.getValue();
        //If toName exists already, return false
        if(sheetIndexes.containsKey(toName)){
            //Clone is bypassed
            return null;
        }

        if(sheetIndexes.containsKey(fromName)){
            Sheet newSheet = wb.cloneSheet(sheetIndexes.get(fromName));
            wb.setSheetName(wb.getSheetIndex(newSheet), toName);
        } else {
            wb.createSheet(toName);
        }
        sheetIndexes.put(toName, wb.getSheetIndex(toName));
        if(sheets.isValueInitialized()){
            sheets.getValue().put(toName, new ExcelSheetHelper(this, toName));
        }

        return sheets.getValue().get(toName);
    }

    /**
     * Get the default header style
     * @return  CellStyle to be applied to headers
     */
    public CellStyle getHeaderStyle(){
        if(headerStyle == null){
            Workbook wb = workbook.getValue();
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.index);

            headerStyle = wb.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.index);
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setLeftBorderColor(IndexedColors.WHITE.index);
            headerStyle.setBorderLeft(BorderStyle.DASH_DOT);
            headerStyle.setWrapText(true);
            headerStyle.setFont(headerFont);
        }
        return headerStyle;
    }

    /**
     * Get the AutoWrap style
     * @return  CellStyle of auto wrapping text and put it in center
     */
    public CellStyle getAutoWrap(){
        if(autoWrap == null){
            Workbook wb = workbook.getValue();
            autoWrap = wb.createCellStyle();
            autoWrap.setWrapText(true); //Set wordwrap
            autoWrap.setAlignment(HorizontalAlignment.CENTER);
            autoWrap.setVerticalAlignment(VerticalAlignment.CENTER);
        }
        return autoWrap;
    }

    /**
     * Get the HyperLink style.
     * @return  CellStyle of the HyperLink.
     */
    public CellStyle getHyperLinkStyle() {
        if(hyperLink == null){
            Workbook wb = workbook.getValue();
            hyperLink = wb.createCellStyle();
            Font font = wb.createFont();
            font.setUnderline(Font.U_SINGLE);
            font.setColor(Font.COLOR_NORMAL);
            hyperLink.setFont(font);
        }
        return hyperLink;
    }

    /**
     * Get the default hightlight style
     * @return  CellStyle to highlight a cell
     */
    public CellStyle getHighlightStyle(){
        if(highlightStyle != null)
            return highlightStyle;

        CellStyle style = workbook.getValue().createCellStyle();
        style.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setLeftBorderColor(IndexedColors.RED.getIndex());
        style.setRightBorderColor(IndexedColors.RED.getIndex());
        style.setTopBorderColor(IndexedColors.RED.getIndex());
        style.setBottomBorderColor(IndexedColors.RED.getIndex());
        highlightStyle = style;
        return style;
    }

    /**
     * Get the default hightlight style for DATE cells
     * @return  CellStyle to highlight a DATE cell
     */
    public CellStyle getHighlightDateStyle(){
        if(highlightDateStyle != null){
            return highlightDateStyle;
        }

        CreationHelper helper = creationHelper.getValue();
        CellStyle style = workbook.getValue().createCellStyle();
        short dateFormatIndex = helper.createDataFormat().getFormat(DEFAULT_DATE_CELL_FORMAT);
        style.setDataFormat(dateFormatIndex);
        style.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setLeftBorderColor(IndexedColors.RED.getIndex());
        style.setRightBorderColor(IndexedColors.RED.getIndex());
        style.setTopBorderColor(IndexedColors.RED.getIndex());
        style.setBottomBorderColor(IndexedColors.RED.getIndex());

        highlightDateStyle = style;
        return style;
    }

    /**
     * Get the default de-emphasize style
     * @return  CellStyle to de-emphasize a cell
     */
    public CellStyle getDeEmphasizeStyle(){
        if (deEmphasizeStyle != null)
            return deEmphasizeStyle;

        if(isXSSFWorkbook){
            XSSFWorkbook book = (XSSFWorkbook)workbook.getValue();
            XSSFCellStyle style = book.createCellStyle();
            style.setFillForegroundColor(IndexedColors.BLUE_GREY.getIndex());
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            deEmphasizeStyle = style;
        } else {
            //Return style for HSSFWorkbook
            //*/// Cannot set the background to custom color due to a bug of POI, use LIGHT_TURQUOISE instead
            CellStyle style = workbook.getValue().createCellStyle();
            style.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            deEmphasizeStyle = style;
            /*/// A bug of POI of palette.getColor(int index) would always return HSSFPalette.CustomColor instance
            //  without calling constructor of its base HSSFColor, thus the default BLACK is used actually
            HSSFWorkbook book = (HSSFWorkbook)workbook.getValue();
            HSSFCellStyle style = book.createCellStyle();
            HSSFColor hssfColor = null;
            try {
                final HSSFPalette palette = book.getCustomPalette();
                byte red = (byte) NEGLECTED_CELL_COLOR.getRed();
                byte green = (byte) NEGLECTED_CELL_COLOR.getGreen();
                byte blue = (byte) NEGLECTED_CELL_COLOR.getBlue();
                short index = HSSFColor.HSSFColorPredefined.BROWN.getIndex();
                hssfColor= palette.findColor(red, green, blue);
                if (hssfColor == null ){
                    palette.setColorAtIndex(index, red, green, blue);
                    hssfColor = palette.getColor(index);
                }
                if(Objects.deepEquals(hssfColor.getTriplet(), new int[]{0, 0, 0})){
                    style.setFillBackgroundColor(HSSFColor.HSSFColorPredefined.LIGHT_TURQUOISE.getIndex());
                } else {
                }
                style.setFillBackgroundColor(index);
                style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                deEmphasizeStyle = style;
            } catch (Exception e) {
                ReportHelper.log(e.getMessage());
            }
            //*///
        }

        return deEmphasizeStyle;
    }

    /**
     * Get a copy of concerned Excel file, after removing Input sheets and renaming the Output ones, return a wrapper ExelBookHelper instance.
     * @param filenameOrPath    Filename of the result result Excel file, or absolute path, or null to use the same name but in result folder.
     * @return  A new ExcelBookHelper instance based on this ExcelBookHelper, but get input sheets removed and output sheets renamed.
     */
    public ExcelBookHelper getResultBookCopy(String filenameOrPath){
        File resultFile = null;
        if(StringUtils.isEmpty(filenameOrPath)){
            resultFile = ResourceHelper.getResultFile(file.getName());
        } else if(Paths.get(filenameOrPath).isAbsolute()){
            resultFile = new File(filenameOrPath);
        } else {
            resultFile = ResourceHelper.getResultFile(filenameOrPath);
        }

        File directory = resultFile.getParentFile();
        if (!directory.exists()) {
            directory.mkdirs();
        }
        try {
            Files.copy(file.toPath(), resultFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ExcelBookHelper resultBook = new ExcelBookHelper(resultFile, true)
                .convertToResultBook();
        return resultBook;
    }

//    public int trimDataRows(List<String> outSheetNames,
//                            DataStrategy dataStrategy, LocalDate fromDateInclusive, LocalDate toDateInclusive, String... stories)
//    {
//        Instant end;
//        Instant start = Instant.now();
//        String tableName = null;
//        final List<ExcelSheetHelper> outDataSuppliers = outSheetNames.stream().map(name -> getSheetHelper(name))
//                .collect(Collectors.toList());
//
//        int total = 0;
//        for (int i = 0; i < outDataSuppliers.size(); i++) {
//            ExcelSheetHelper sheet = outDataSuppliers.get(i);
//            tableName = sheet.getTablename();
//            int rowsRemoved = sheet.trimDataRows(dataStrategy, fromDateInclusive, toDateInclusive, stories);
//            total += rowsRemoved;
//            end = Instant.now();
//            ReportHelper.log("%s get %d rows removed %s", tableName, rowsRemoved, Duration.between(start, end));
//            start = end;
//        }
//        return total;
//    }

    /**
     * Save the changes to the Excel file
     */
    public void save(){
        try(Workbook book = workbook.getValue();

            OutputStream outputStream = new FileOutputStream(file);){
            book.write(outputStream);
            Logger.I("Excel file saved as %s", file);
        } catch (Exception ex){
            throw new RuntimeException(ex);
        } finally {
            Functions.tryRun(workbook::close);
            Functions.tryRun(sheets::close);
        }
    }

    @Override
    public void close(){
        if( inStream.isValueInitialized()) {
            Functions.tryRun(inStream::close);
        }
        Functions.tryRun(workbook::close);
    }

    /**
     * Add an image from given URL to the ExcelBook.
     * @param imageUrl  URL of the concerned Image.
     * @return          ID of the added picture.
     */
    public int addImage(URL imageUrl){
        Tuple2<BufferedImage, String> imageWithFormat = ImageHelper.getTypedImage(imageUrl);
        return addImage(imageWithFormat.getFirst(), imageWithFormat.getSecond());
    }

    /**
     * Add an image with format specified to the workbook.
     * @param image     Image to be added.
     * @param format    Format string of the image.
     * @return          ID of the added picture.
     */
    public int addImage(BufferedImage image, String format){
        try {
            int pictureType = asPictureType(format);
            byte[] bytes = ImageHelper.toBytes(image, format);
            int pictureIdx = workbook.getValue().addPicture(bytes, pictureType);
            return pictureIdx;
        }catch (Exception e){
            return -1;
        }
    }
}
