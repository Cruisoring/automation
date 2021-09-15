package io.github.Cruisoring.helpers;

import io.github.Cruisoring.interfaces.RowDataSupplier;
import io.github.cruisoring.Lazy;
import io.github.cruisoring.tuple.Tuple2;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.poi.ss.usermodel.*;

import java.awt.image.BufferedImage;
import java.net.URL;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ExcelSheetHelper implements RowDataSupplier {
    private static DataFormatter formatter = new DataFormatter();

    protected static final Map<CellType, Function<Cell, Object>> cellValueGetters = new HashMap<CellType, Function<Cell, Object>>() {{
        put(CellType.BOOLEAN, cell -> cell.getBooleanCellValue());
        put(CellType.STRING, cell -> cell.getStringCellValue());
        put(CellType.BLANK, cell -> null);
        //put(CellType.FORMULA, (Function<Cell, Object>)cell -> cell.getCellFormula());   //TODO: how to handle CellFormula?
        put(CellType.NUMERIC, cell -> (DateUtil.isCellDateFormatted(cell))
                ? cell.getDateCellValue()
                : (float) cell.getNumericCellValue());
    }};

    protected static final Map<Class, BiConsumer<Cell, Object>> cellValueSetters = new HashMap<Class, BiConsumer<Cell, Object>>() {{
        put(Boolean.class, (cell, value) -> cell.setCellValue((boolean) value));
        put(boolean.class, (cell, value) -> cell.setCellValue((boolean) value));
        put(String.class, (cell, value) -> cell.setCellValue((String) value));
        put(Float.class, (cell, value) -> cell.setCellValue((float) value));
        put(Integer.class, (cell, value) -> cell.setCellValue((Integer)value));
        put(int.class, (cell, value) -> cell.setCellValue((int) value));
        put(float.class, (cell, value) -> cell.setCellValue((float) value));
        put(Double.class, (cell, value) -> cell.setCellValue((float) value));
        put(double.class, (cell, value) -> cell.setCellValue((float) value));
        put(Date.class, (cell, value) -> cell.setCellValue(DateTimeHelper.dateString((Date) value)));
        put(LocalDate.class, (cell, value) -> cell.setCellValue(DateTimeHelper.dateString((LocalDate) value)));
        put(java.sql.Date.class, (cell, value) -> cell.setCellValue((Date) value));
    }};

    /**
     * Get the value of a specific cell as matched JAVA object
     *
     * @param cell Cell to extract value from.
     * @return Java object to represent the value of the cell.
     */
    public static Object getCellValue(Cell cell) {
        if (cell == null)
            return null;
        CellType cellType = cell.getCellTypeEnum();
        if (cellType.equals(CellType.FORMULA)) {
            cellType = cell.getCachedFormulaResultTypeEnum();
        }
        //Let following codes to throw Exception if the handler of CellType has not been defined
        Object value = cellValueGetters.get(cellType).apply(cell);
        //Try to convert the string to NULL or LocalDate
        if (value instanceof String){
            LocalDate localDate = DateTimeHelper.dateFromString((String)value);
            if(localDate != null){
                value = DateTimeHelper.toDate(localDate);
            }
        }
        return value;
    }

    /**
     * Set the cell with correct Cell type of the given value
     *
     * @param cell  Cell to be write
     * @param value The value to be written to the cell
     */
    public static void setCellValue(Cell cell, Object value) {
        Objects.requireNonNull(cell);
        if (value == null) {
            cell.setCellType(CellType.BLANK);
            return;
        }
        Class clazz = value.getClass();
        if (!cellValueSetters.containsKey(clazz))
            throw new RuntimeException("Setting not supported for " + clazz);
        try {
            BiConsumer<Cell, Object> setter = cellValueSetters.get(clazz);
            setter.accept(cell, value);
        } catch (Exception ex) {
            ReportHelper.reportAsStepLog("Failed to save value '%s' to cell", value);
        }
    }

    /**
     * Save an array of values to specific sheetLazy of specific excel file (.xls or .xlsx)
     *
     * @param rows      Values to be written to the target sheetLazy. first row is treated as the table headers.
     * @param filePath  Path to open/create the excel file that ended with either .xls or .xlsx
     * @param sheetName Name of the sheetLazy to be written
     * @return Absolute path of the operated excel file.
     */
    public static String saveToExcelSheet(List<Object[]> rows, Path filePath, String sheetName) throws Exception {
        Objects.requireNonNull(rows);
        Objects.requireNonNull(filePath);
        Objects.requireNonNull(sheetName);

        ExcelBookHelper bookHelper = ExcelBookHelper.asWritable(filePath.toFile());
        ExcelSheetHelper sheetHelper = bookHelper.createSheet(sheetName);
        sheetHelper.saveRows(rows);
        bookHelper.save();
        return bookHelper.getFile().getAbsolutePath();
    }

    private final ExcelBookHelper excelBook;
    private final String name;
    public final Lazy<Sheet> sheetLazy;
    private final Lazy<List<String>> columns;
    private final Lazy<Drawing> drawingLazy;
    private Lazy<Map<String, Integer>> columnIndexes;
    private Lazy<List<String>> orderedColumnNames = new Lazy(() -> getOrderedColumns(this));
    private Lazy<int[]> orderedColumnIndexes = new Lazy(() -> getColumnIndexes(orderedColumnNames.getValue()));
    private Lazy<String> singleQueryTemplate = new Lazy(() -> getSingleQueryTemplate());
    private Drawing drawing = null;

    /**
     * Constructor to create an ExcelSheetHelper of the specific sheetLazy of the given workbook.
     *
     * @param sheets    ExcelBookHelper to wrap the concerned workbook that ended with either .xls or .xlsx
     * @param sheetName Name of the concerned sheetLazy
     */
    public ExcelSheetHelper(ExcelBookHelper sheets, String sheetName) {
        this.excelBook = sheets;
        name = sheetName.contains(ExcelBookHelper.SHEET_ROLE_INDICATOR) ?
                sheetName.substring(0, sheetName.indexOf(ExcelBookHelper.SHEET_ROLE_INDICATOR)) : sheetName;
        sheetLazy = new Lazy<>(() -> this.excelBook.workbook.getValue().getSheet(sheetName));
        drawingLazy = new Lazy<Drawing>(() -> sheetLazy.getValue().createDrawingPatriarch());
        columns = new Lazy<List<String>>(() -> {
            Row row0 = sheetLazy.getValue().getRow(0);
            return IntStream.range(0, row0.getLastCellNum()).boxed()
                    .map(index -> row0.getCell(index).getStringCellValue().trim())
                    .collect(Collectors.toList());
        });
        columnIndexes = new Lazy<Map<String, Integer>>(() -> getColumnIndexes());

    }

    /**
     * Get the name of SQL table to be processed.
     *
     * @return Name of the table.
     */
    @Override
    public String getTablename() {
        return name;
    }

    /**
     * Save the given values to a newly created sheetLazy
     *
     * @param rows row values to be written, the first row is assumed as the headers
     */
    public void saveRows(List<Object[]> rows) {
        Objects.requireNonNull(rows);

        Sheet _sheet = sheetLazy.getValue();
        for (int i = 0; i < rows.size(); i++) {
            Object[] values = rows.get(i);
            Row row = _sheet.createRow(i);
            CellStyle style = i == 0 ? excelBook.getHeaderStyle() : null;
            for (int j = 0; j < values.length; j++) {
                Cell cell = row.createCell(j);
                if (style != null)
                    cell.setCellStyle(style);
                setCellValue(cell, values[j]);
            }
        }
    }

    /**
     * Get all column names defined in the RowDataSupplier.
     *
     * @return List of column names.
     */
    @Override
    public List<String> getColumns() {
        return columns.getValue().stream()
                .collect(Collectors.toList());
    }

    /**
     * Given a solid column name, find its ordinal index.
     *
     * @param columnName Name of the concerned column.
     * @return Index of the column concerned (0 based)
     */
    @Override
    public Integer getColumnIndex(String columnName) {
        return MapHelper.getIgnoreCase(columnIndexes.getValue(), columnName);
    }

    /**
     * Get a sorted name list of key or non-key column, ignorable columns not included.
     *
     * @return a sorted name list of non-key first and followed by key columns, ignorable columns not included.
     */
    @Override
    public List<String> getOrderedColumns() {
        return orderedColumnNames.getValue();
    }

    /**
     * Get the row values that would be used to execute Update or Insert with the templates generated from the column names.
     *
     * @param rowNumber Number of the concerned row, starting from 0.
     * @return Object array containing the values of the concerned row.
     */
    @Override
    public Object[] getRowValues(int rowNumber) {
        return getRowValues(rowNumber, orderedColumnIndexes.getValue());
    }

    /**
     * Retrieve the values of the concerned row specified with their column indexes as an array of Object.
     *
     * @param rowNumber Row number starting from 0.
     * @param columns   indexes of the concerned columns starting from 0.
     * @return An Object arrays of the concerned row and columns if the row number is between 0 and getRowCount() exclusively;
     * otherwise null.
     */
    @Override
    public Object[] getRowValues(int rowNumber, int[] columns) {
        if (rowNumber < 0 || rowNumber >= getRowCount())
            return null;

        //Suppose the first row list Column names, thus shall be bypassed
        Row row = sheetLazy.getValue().getRow(rowNumber + 1);

        if(row == null)
            return null;

        Object[] values = Arrays.stream(columns)
                .mapToObj(i -> getCellValue(row.getCell(i))).toArray(Object[]::new);
        return values;
    }

    /**
     * Get the style of the concerned cell
     * @param rowIndex Index of the concerned row (0 based)
     * @param colIndex Index of the concerned cells (0 based)
     * @return CellStyle of the concerned cell
     */
    public CellStyle getCellStyle(int rowIndex, int colIndex){
        Sheet theSheet = sheetLazy.getValue();
        Row row = theSheet.getRow(rowIndex + 1);
        Cell cell = row.getCell(colIndex);
        if (cell == null) {
            return null;
        }
        return cell.getCellStyle();
    }


    /**
     * Get the value of concerned cell and return it as an JAVA Object.
     * @param rowIndex Index of the concerned row (0 based)
     * @param colIndex Index of the concerned cells (0 based)
     * @return  Object to represent the original cell value naturally
     */
    public Object getCellValue(int rowIndex, int colIndex){
        if (rowIndex < 0 || rowIndex >= getRowCount())
            return null;

        //Suppose the first row list Column names, thus shall be bypassed
        Row row = sheetLazy.getValue().getRow(rowIndex + 1);

        if(row == null)
            return null;

        Object cellValue = getCellValue(row.getCell(colIndex));
        return cellValue;
    }

    /**
     * Get the total row number assuming the first row as headers, and remaining as data rows.
     *
     * @return total row number
     */
    @Override
    public int getRowCount() {
        Sheet workSheet = sheetLazy.getValue();
        int lastRowNumber = workSheet.getLastRowNum();
        return lastRowNumber;
    }

    /**
     * Get total column number that identified as non-empty cells of the first row.
     *
     * @return Number of columns identified in the first row.
     */
    @Override
    public int getColumnCount() {
        return getColumns().size();
    }

    /**
     * Ensure the workbook save the changs automatically when this sheetLazy is closed
     */
    @Override
    public void close() throws Exception {
        excelBook.close();
    }

    /**
     * Add comment to keep text as a comment
     *
     * @param cell Cell to add comment to
     * @param text Comment text to be added
     */
    public void addComment(Cell cell, String text, int colSpan, int rowSpan) {
        Drawing drawing = drawingLazy.getValue();

        CreationHelper factory = excelBook.workbook.getValue().getCreationHelper();
        ClientAnchor anchor = factory.createClientAnchor();
        anchor.setCol1(cell.getColumnIndex());
        anchor.setCol2(cell.getColumnIndex() + colSpan);
        anchor.setRow1(cell.getRowIndex());
        anchor.setRow2(cell.getRowIndex() + rowSpan);
        Comment comment = drawing.createCellComment(anchor);
        RichTextString commentText = factory.createRichTextString(text);
        comment.setString(commentText);

        cell.setCellComment(comment);
    }

    private void addComment(Cell cell, String text) {
        addComment(cell, text, 3, 2);
    }

    /**
     * Change the style of a row to highlight its cells
     *
     * @param rowIndex   Index of the concerned row (0 based)
     * @param keyIndexes Index of the concerned cells (0 based)
     */
    public void highlightMissingRow(int rowIndex, int[] keyIndexes, String comment) {
        Sheet theSheet = sheetLazy.getValue();
        Row row = theSheet.getRow(rowIndex + 1);
        int columnCount = row.getLastCellNum();
        int cellIndex = 0;

        try {
            CellStyle highlightStyle = excelBook.getHighlightStyle();
            Object[] keyValues = getRowValues(rowIndex, keyIndexes);
            if (comment == null)
                comment = String.format("Missing: %s", ReconcileHelper.getArrayDescription(keyValues));
            for (; cellIndex < columnCount; cellIndex++) {
                Cell cell = row.getCell(cellIndex);
                int keyIndex = ArrayUtils.indexOf(keyIndexes, cellIndex);

                if (keyIndex != -1) {
                    //For key columns, the cell must contains something
                    if (keyIndex == 0) {
                        addComment(cell, comment, 6, 3);
                    }
                    if (keyValues[keyIndex] instanceof Date) {
                        cell.setCellStyle(excelBook.getHighlightDateStyle());
                    } else {
                        cell.setCellStyle(highlightStyle);
                    }
                } else {
                    if (cell == null) {
                        cell = row.createCell(cellIndex, CellType.BLANK);
                    } else {
                        cell.setCellType(CellType.BLANK);
                    }
                    cell.setCellStyle(highlightStyle);
                }
            }
        } catch (Exception ex) {
            //Capture the exceptions to keep processing following rows
            ReportHelper.log("Exception with row %d cell %d: %s", rowIndex + 1, cellIndex + 1, ex.getMessage());
        }
    }

    /**
     * Update the specific cells of concerned row with new values
     * @param rowIndex  Index of the row to be updated
     * @param values    Values to be updated
     * @param columns   Column numbers to identify the cells
     */
    public void updateRow(int rowIndex, Object[] values, int[] columns){
        Objects.requireNonNull(values);
        Objects.requireNonNull(columns);
        int size = columns.length;
        if(size != values.length){
            throw new RuntimeException();
        }

        Sheet theSheet = sheetLazy.getValue();
        Row row = theSheet.getRow(rowIndex + 1);
        int cellIndex = 0;

        try {
            for (int i = 0; i < size; i ++) {
                cellIndex = columns[i];
                Cell cell = row.getCell(cellIndex);
                if(cell == null){
                    cell = row.createCell(cellIndex);
                }
                setCellValue(cell, values[i]);
            }
        } catch (Exception ex) {
            //Capture the exceptions to keep processing following rows
            ReportHelper.log("Exception with row %d cell %d: %s", rowIndex + 1, cellIndex + 1, ex.getMessage());
        }

    }

    /**
     * Change the style of a row to de-emphasize its cells
     *
     * @param rowIndex Index of the concerned row (0 based)
     * @param colIndex Index of the concerned cells (0 based)
     */
    public void deEmphasizeCell(int rowIndex, int colIndex) {
        Sheet theSheet = sheetLazy.getValue();
        Row row = theSheet.getRow(rowIndex + 1);
        Cell cell = row.getCell(colIndex);
        if (cell == null) {
            cell = row.createCell(colIndex, CellType.BLANK);
        }

        CellStyle deEmphasizeStyle = excelBook.getDeEmphasizeStyle();
        cell.setCellStyle(deEmphasizeStyle);
    }

    /**
     * Update/add a cell with any given value, add a comment if the old value is different
     *
     * @param rowIndex        index of the concerned row (0 based)
     * @param columnIndex     index of the concerned column (0 based)
     * @param newValue        value to be added/updated
     * @param commentTemplate template to be applied if comment is to be added
     */
    public void updateCell(int rowIndex, int columnIndex, Object newValue, String commentTemplate) {
        Sheet theSheet = sheetLazy.getValue();
        Row row = theSheet.getRow(rowIndex + 1);
        Cell cell = row.getCell(columnIndex);
        if (cell == null) {
            cell = row.createCell(columnIndex, CellType.BLANK);
        }
        Object oldValue = getCellValue(cell);
        setCellValue(cell, newValue);
        cell.setCellStyle(excelBook.getHighlightStyle());
        String text = String.format(commentTemplate, oldValue);
        addComment(cell, text);
    }

    /**
     * Retrieve all KeyValue pairs of the sheetLazy as a Map&lt;String, Object&gt;.
     *
     * @param keyName        Case insensitive column name of the keys
     * @param valueName      Case insensitive column name of the values
     * @param skipNullValues <tt>true</tt> to skip keyValuePair when the value is null, <tt>false</tt> to save the null value as empty string
     * @return A map of all values referred by the keys
     */
    public Map<String, Object> getKeyValues(String keyName, String valueName, boolean skipNullValues) {
        Objects.requireNonNull(keyName);
        Objects.requireNonNull(valueName);

        int keyColumnIndex = getColumnIndex(keyName);
        int valueColumnIndex = getColumnIndex(valueName);
        if (keyColumnIndex == -1 || valueColumnIndex == -1)
            return null;

        int[] indexes = new int[]{keyColumnIndex, valueColumnIndex};
        Map<String, Object> keyValues = new HashMap<>();
        int rowCount = getRowCount();
        for (int i = 0; i < rowCount; i++) {
            Object[] keyAndValue = getRowValues(i, indexes);
            if(keyAndValue == null)
                continue;
            if (keyAndValue[0] != null) {
                Object value = keyAndValue[1];
                if (value != null || !skipNullValues) {
                    keyValues.put(keyAndValue[0].toString(), value == null ? "" : value);
                }
            }
        }
        return keyValues;
    }

    /**
     * Retrieve all KeyValue pairs of the sheetLazy as a Map&lt;String, Object&gt;.
     *
     * @param keyName        Case insensitive column name of the keys
     * @param valueName      Case insensitive column name of the values
     * @return A map of all values referred by the keys
     */
    public Map<String, Object> getKeyValues(String keyName, String valueName) {
        return getKeyValues(keyName, valueName, false);
    }

    /**
     * Remove a specific row identified by the index.
     * @param dataRowIndex  Index of the row to be removed.
     * @return  <tt>true</tt> if removed successfully, otherwise <tt>false</tt>
     */
    public boolean removeDataRow(int dataRowIndex){
        int rowCount = getRowCount();
        if(dataRowIndex < 0 || dataRowIndex >= rowCount)
            return false;

        Sheet theSheet = sheetLazy.getValue();
        if(dataRowIndex == rowCount-1){
            Row row = theSheet.getRow(dataRowIndex+1);
            if(row != null){
                theSheet.removeRow(row);
                return true;
            } else {
                return false;
            }
        } else {
            theSheet.shiftRows(dataRowIndex+2, rowCount+1, -1);
            return true;
        }
    }

    /**
     * Remove a range of rows identified by the start and end row number.
     * @param startRowIndex Start of the rows to be removed (0 based, inclusive).
     * @param endRowIndex   End of the rows to be removed (0 base, exclusive).
     * @return
     */
    public int removeDataRows(int startRowIndex, int endRowIndex){
        int rowCount = getRowCount();
        if(rowCount == 0 || startRowIndex >= rowCount || endRowIndex < 0 || startRowIndex > endRowIndex)
            return 0;

        startRowIndex = startRowIndex < 0 ? 0 : startRowIndex;
        endRowIndex = endRowIndex >= rowCount ? rowCount-1 : endRowIndex;

        Sheet theSheet = sheetLazy.getValue();
        int count = 0;
        if (endRowIndex == rowCount -1){
            for (int i = endRowIndex; i >= startRowIndex; i--) {
                Row row = theSheet.getRow(i+1);
                if(row != null) {
                    theSheet.removeRow(row);
                }
                count++;
            }
            return count;
        } else {
            int nextRowIndex = endRowIndex+2;
            int lasRowIndex = rowCount+1;
            int shift = startRowIndex-endRowIndex-1;
            theSheet.shiftRows(nextRowIndex, lasRowIndex, shift);
            return -shift;
        }
    }

    /**
     * Remove rows that are not relevant when not included in the given index array.
     * @param relevantRowIndexes Array of all test related rows (0 starting)
     * @return Number of rows removed.
     */
    public int trimDataRows(int[] relevantRowIndexes){
        if(relevantRowIndexes == null)
            return 0;

        Sheet theSheet = sheetLazy.getValue();
        int rowCount = getRowCount();
        if(relevantRowIndexes.length == 0) {
            return removeDataRows(0, rowCount-1);
        }

        //Get a sorted unique index set
        List<Integer> rowIndexesToKeep = Arrays.stream(relevantRowIndexes)
                .distinct()
                .filter(i -> i >= 0 && i < rowCount)
                .sorted()
                .mapToObj(i -> Integer.valueOf(i))
                .collect(Collectors.toList());

        int removedCount = 0;
        for (int i = rowCount-1; i >= 0; i--) {
            if (rowIndexesToKeep.contains(i))
                continue;
            Row row = theSheet.getRow(i + 1);
            if(row != null){
                theSheet.removeRow(row);
                removedCount++;
            }
        }

        int removeStart, removeEnd, currentIndex, previousIndex;
        final int keepCount = rowIndexesToKeep.size();
        int shiftCount = 0;
        for (int i = keepCount-1; i >= 0; i--) {
            currentIndex = rowIndexesToKeep.get(i);
            if(i == keepCount-1 && currentIndex != rowCount-1){
                removeStart = currentIndex+1;
                removeEnd = rowCount-1;
                shiftCount += removeDataRows(removeStart, removeEnd);
            }

            if (i == 0){
                if(currentIndex == 0)
                    continue;
                removeStart = 0;
                removeEnd = currentIndex-1;
            } else {
                previousIndex = rowIndexesToKeep.get(i-1);
                if(previousIndex == currentIndex-1)
                    continue;
                removeStart = previousIndex+1;
                removeEnd = currentIndex-1;
            }
            shiftCount += removeDataRows(removeStart, removeEnd);
        }
        return removedCount;
    }

//    public int trimDataRows(DataStrategy dataStrategy, LocalDate fromDateInclusive, LocalDate toDateInclusive, String... stories)
//    {
//        Sheet theSheet = sheetLazy.getValue();
//
//        int[] rowToKeep = ReconcileHelper.getStoryRowIndexes(this, dataStrategy, fromDateInclusive, toDateInclusive, stories);
//        return trimDataRows(rowToKeep);
//    }

//    public int[] getIndexesOfRanges(DateRange... dateRanges){
//        List<int[]> rowIndexesToKeep = Arrays.stream(dateRanges)
//                .map(range -> ReconcileHelper.getStoryRowIndexes(this, DataStrategy.DateRange, range.fromDate, range.toDate))
//                .collect(Collectors.toList());
//
//        Set<Integer> matchedRows = rowIndexesToKeep.stream()
//                .flatMap(array -> Arrays.stream(array).boxed())
//                .collect(Collectors.toSet());
//
//        Sheet theSheet = sheetLazy.getValue();
//
//        int[] rowsToKeep = matchedRows.stream()
//                .mapToInt(i -> i).toArray();
//        return rowsToKeep;
//    }

    /**
     * Insert a concerned image to a cell and span multiple columns or rows.
     * @param cell      Top left cell to insert the image.
     * @param imageUrl  URL of the concerned image.
     * @param colSpan   Span of columns.
     * @param rowSpan   Span of rows.
     * @return          <tt>true</tt> if image inserted successfully, otherwise <tt>false</tt>
     */
    public boolean insertImage(Cell cell, URL imageUrl, int colSpan, int rowSpan) {
        Objects.requireNonNull(cell);
        Objects.requireNonNull(imageUrl);

        Tuple2<BufferedImage, String> typedImage = ImageHelper.getTypedImage(imageUrl);
        BufferedImage image = typedImage.getFirst();
        int pictureIndex = excelBook.addImage(image, typedImage.getSecond());
//        int imageHeight = image.getHeight();
//        int cellHeight = cell.getRow().getHeight();
        if(pictureIndex == -1) {
            //Failed to add the picture for some reason
            return false;
        }

        try {
            // Create the drawing patriarch.  This is the top level container for all shapes.
            Drawing drawing = drawingLazy.getValue();

            CreationHelper factory = excelBook.workbook.getValue().getCreationHelper();
            //add a picture shape
            //subsequent call of Picture#resize() will operate relative to it
            ClientAnchor anchor = factory.createClientAnchor();

            anchor.setCol1(cell.getColumnIndex());
            anchor.setCol2(cell.getColumnIndex() + colSpan);
            anchor.setRow1(cell.getRowIndex());
            anchor.setRow2(cell.getRowIndex() + rowSpan);

            Picture pict = drawing.createPicture(anchor, pictureIndex);

            //auto-size picture relative to its top-left corner
//            pict.resize();

            return true;
        }catch (Exception e){
            Logger.W(e);
            return false;
        }
    }

    /**
     * Insert a concerned image to a single cell.
     * @param cell      The cell to hold the inserted image.
     * @param imageUrl  URL of the concerned image.
     * @return          <tt>true</tt> if image inserted successfully, otherwise <tt>false</tt>
     */
    public boolean insertImage(Cell cell, URL imageUrl){
        return insertImage(cell, imageUrl, 1, 1);
    }

    /**
     * Download an image specified by the URL and insert it into a specific cell identified by rowIndex and colIndex.
     * @param rowIndex  Index of the row to be inserted (0 started)
     * @param colIndex  Index of the column to be inserted (0 started)
     * @param imageUrl  URL of the image to be inserted
     * @return          <tt>true</tt> if inserted, otherwise <tt>false</tt>
     */
    public boolean insertImage(int rowIndex, int colIndex, URL imageUrl){
        Sheet sheet = sheetLazy.getValue();
        Row row = sheet.getRow(rowIndex);
        if(row == null) {
            row = sheet.createRow(rowIndex);
        }
        Cell cell = row.getCell(colIndex);
        if(cell == null){
            cell = row.createCell(colIndex);
        }

        return insertImage(cell, imageUrl);
    }

    /**
     * Insert a value array to the row specified by the rowIndex
     * @param rowIndex  Index of the row to be inserted (0 started)
     * @param values    Values to be inserted
     * @return          Number of changed/created cells.
     */
    public int insertRowAt(int rowIndex, Object... values){
        Sheet sheet = sheetLazy.getValue();
        int count = 0;
        Row row = sheet.getRow(rowIndex);
        if(row == null) {
            row = sheet.createRow(rowIndex);
        }

        CellStyle autowrap = excelBook.getAutoWrap();
        for (int i = 0; i < values.length; i++) {
            Object value = values[i];
            if(value == null && row.getCell(i)!=null){
                row.getCell(i).setCellType(CellType.BLANK);
                count++;
            }

            Class valueClass = value.getClass();
            Cell cell = row.getCell(i);
            if(cell == null){
                cell = row.createCell(i);
            }
            if(cellValueSetters.containsKey(valueClass)){
                setCellValue(cell, value);
                cell.setCellStyle(autowrap);
                count++;
            } else if(valueClass.equals(URL.class)){
                URL url = (URL)value;
                String file = url.getFile();
                if(StringExtensions.containsAnyIgnoreCase(file, "JPG", "PNG", "JPEG")){
                    if(insertImage(cell, url))
                        count++;
                }
            }
        }
        return count;
    }

    public boolean appendRow(Object... values){
        if(values == null)
            return false;

        Sheet theSheet = sheetLazy.getValue();
        int lastRow = theSheet.getLastRowNum();
        Row newRow = theSheet.createRow(lastRow+1);
        for (int i = 0; i < values.length; i++) {
            Object value = values[i];
            if (value == null)
                continue;

            Cell newCell = newRow.createCell(i);
            setCellValue(newCell, value);
        }
        return true;
    }

    public int appendRows(List<Object[]> rows){
        int count = 0;
        for (int i = 0; i < rows.size(); i++) {
            if(appendRow(rows.get(i)))
                count++;
        }

        return count;
    }

}
