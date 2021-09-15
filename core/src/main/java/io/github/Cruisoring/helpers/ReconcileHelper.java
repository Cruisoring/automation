package io.github.Cruisoring.helpers;

import io.github.Cruisoring.interfaces.RowDataSupplier;
import io.github.cruisoring.Functions;
import io.github.cruisoring.Lazy;
import io.github.cruisoring.throwables.FunctionThrowable;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.InvalidArgumentException;

import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ReconcileHelper {


    //region Parameters related keys
    public static final String ParametersSheetName = "Parameters";
    public static final String StoryNumbersLabel = "StoryNumbers";
    public static final String DefualtParametersKey = "Default";

    //Special key to indicate the row shall not be found with the given keys
    public static final String NoRecord = "No Record";

    //Keys for Schedule Calculation Nav data
    private static final String FromDateLabel = "FromDate";
    private static final String ToDateLabel = "ToDate";
    private static final String CalculationNetworkLabel = "CalculationNetwork";
    private static final String CalculationJobLabel = "CalculationJob";
    public static final String BusinessUnitLabel = "BusinessUnit";
    public static final String PipelineLabel = "PipelineSegment";

    private static final String PipelinesLabel = "Pipelines";
    private static final String EnvironmentLabel = "Environment";
    private static final String DataStrategyLabel = "DataStrategy";
    private static final String InExcelFileLabel = "InExcelFile";
    private static final String OutExcelFileLabel = "OutExcelFile";
    //endregion


    /*Story number shall be in form of project code followed by digital number:
        The project code can be case-incensitive like GSF, ta or Ctna, new project code shall be added to the below pattern seperated by '|'
        The digital number shall be 2 to 4 digits
        They are connected with an optional '-'
     */
    public static final Pattern StoryNumberPattern = Pattern.compile("^((GSF|TA|CTNA)\\-?\\d{2,4})$");

    public static final String NULL = "null";
    public static final String CommentTemplate = "expect: %s";
    public static final String DefaultParametersSheetName = "Parameters";
    public static final String[] StoriesColumnKeys = new String[]{ "#Stories", "_Stories", "#StoryNumbers", "_StoryNumbers" };
    public static final String[] DayTimeColumnKeys = new String[]{ "DAYTIME*" };

    private static final Predicate<Object[]> alwaysTrue = values -> true;
    private static final Predicate<Object[]> alwaysFalse = values -> false;

    /**
     * Load all data from the given RowDataSupplier and update/insert corresponding DB table in batches.
     * @param sqlHelper SQLHelper instance to perform SQL update/insert operations.
     * @param supplier  Data Supplier from a Excel sheet.
     * @return total count of rows that have been updated or inserted if all success, otherwise -1 when something failed.
     */
    public static int batchUpdateOrInsert(SQLHelper sqlHelper, RowDataSupplier supplier) {
        int rowCount = supplier.getRowCount();
        int[] rowIndexes = IntStream.range(0, rowCount).toArray();
        return batchUpdateOrInsert(sqlHelper, supplier, rowIndexes);
    }

    /**
     * Load all data from the given RowDataSupplier, delete corresponding rows first before inserting them in batches.
     * @param sqlHelper SQLHelper instance to perform SQL update/insert operations.
     * @param supplier  Data Supplier from a Excel sheet.
     * @return total count of rows that have been inserted if all success, otherwise -1 when something failed.
     */
    public static int batchDeleteThenInsert(SQLHelper sqlHelper, RowDataSupplier supplier) {
        int rowCount = supplier.getRowCount();
        int[] rowIndexes = IntStream.range(0, rowCount).toArray();
        return batchDeleteThenInsert(sqlHelper, supplier, rowIndexes);
    }

    /**
     * Load data from the given Excel Sheet and update/insert corresponding DB table in batches.
     * @param sqlHelper SQLHelper instance to perform SQL update/insert operations.
     * @param supplier  Data Supplier from a Excel sheet.
     * @param rowIndexes Indexes of the rows of the supplier to be injected.
     * @return total count of rows that have been updated or inserted if all success, otherwise -1 when something failed.
     */
    public static int batchUpdateOrInsert(SQLHelper sqlHelper, RowDataSupplier supplier, int[] rowIndexes) {
        Instant start = Instant.now();
        int updatedCount=0, insertCount=0, failedCount=0;
        try {
            List<String> orderedColumns = supplier.getOrderedColumns();
            String updateTemplate = supplier.getUpdateTemplate(orderedColumns);

            final int[] updateResults = sqlHelper.runBatch(updateTemplate, supplier::getRowValues, rowIndexes);
            int rowCount = rowIndexes.length;
            final int[] failedUpdates = IntStream.range(0, rowCount)
                    .filter(i -> updateResults[i] != 1)
                    .map(i -> rowIndexes[i])
                    .toArray();
            updatedCount = rowCount - failedUpdates.length;
            if (failedUpdates.length == 0) {
                ReportHelper.reportAsStepLog("All %d rows updated successfully in %s.", updatedCount, Duration.between(start, Instant.now()));
                return rowCount;
            }

            String failedRowNumbers = "'" + Arrays.stream(failedUpdates)
                    .mapToObj(i -> String.valueOf(i + 2))
                    .collect(Collectors.joining(", ")) + "'";

            ReportHelper.reportAsStepLog("%s rows updated, failed with rows of %s", updatedCount, failedRowNumbers);
            String insertTemplate = supplier.getInsertTemplate(orderedColumns);
            final int[] insertResults = sqlHelper.runBatch(insertTemplate, supplier::getRowValues, failedUpdates);
            insertCount = insertResults.length;
            final int[] failedInserts = IntStream.range(0, failedUpdates.length)
                    .filter(i -> insertResults[i] == 0)
                    .map(i -> failedUpdates[i])
                    .toArray();
            failedCount = failedInserts.length;
            if (failedCount == 0) {
                return insertCount + updatedCount;
            } else {
                failedRowNumbers = Arrays.stream(failedInserts)
                        .mapToObj(i -> String.valueOf(i + 2)).collect(Collectors.joining(", "));
                ReportHelper.reportAsStepLog("Faild to insert rows: ", failedRowNumbers);

                return -1;
            }
        }catch(Exception ex){
            ReportHelper.reportAsStepLog("Exception: %s", ex.getMessage());
            return  -1;
        }finally {
            if (failedCount == 0) {
                ReportHelper.reportAsStepLog("%d rows updated, %d rows inserted successfully in %s.",
                        updatedCount, insertCount, Duration.between(start, Instant.now()));
            } else {
                ReportHelper.reportAsStepLog("%d rows failed to be inserted/updated in %s.",
                        failedCount, Duration.between(start, Instant.now()));
            }
        }
    }

    /**
     * Load data from the given Excel Sheet and update/insert corresponding DB table in batches.
     * @param sqlHelper SQLHelper instance to perform SQL update/insert operations.
     * @param supplier  Data Supplier from a Excel sheet.
     * @param rowIndexes Indexes of the rows of the supplier to be injected.
     * @return total count of rows that have been updated or inserted if all success, otherwise -1 when something failed.
     */
    public static int batchDeleteThenInsert(SQLHelper sqlHelper, RowDataSupplier supplier, int[] rowIndexes) {
        Instant start = Instant.now();
        int deleteCount=0, insertCount=0, failedCount=0;
        try {
            List<String> orderedColumns = supplier.getOrderedColumns();
            String deleteTemplate = supplier.getDeleteTemplate();

            final int[] updateResults = sqlHelper.runBatch(deleteTemplate, supplier::getRowKeys, rowIndexes);
            int rowCount = rowIndexes.length;
            final int[] failedUpdates = IntStream.range(0, rowCount)
                    .filter(i -> updateResults[i] != 1)
                    .map(i -> rowIndexes[i])
                    .toArray();
            deleteCount = rowCount - failedUpdates.length;
            if (failedUpdates.length == 0) {
                ReportHelper.log("All %d rows updated successfully in %s.", deleteCount, Duration.between(start, Instant.now()));
            } else {
                String failedRowNumbers = "'" + Arrays.stream(failedUpdates)
                        .mapToObj(i -> String.valueOf(i))
                        .collect(Collectors.joining(", ")) + "'";
                ReportHelper.log("%s rows deleted, failed with rows of %s", deleteCount, failedRowNumbers);
            }

            String insertTemplate = supplier.getInsertTemplate(orderedColumns);
            final int[] insertResults = sqlHelper.runBatch(insertTemplate, supplier::getRowValues, rowIndexes);
            insertCount = insertResults.length;
            final int[] failedInserts = IntStream.range(0, failedUpdates.length)
                    .filter(i -> insertResults[i] == 0)
                    .map(i -> failedUpdates[i])
                    .toArray();
            failedCount = failedInserts.length;

            if (failedCount == 0) {
                return insertCount;
            } else {
                String failedRowNumbers = Arrays.stream(failedInserts)
                        .mapToObj(i -> String.valueOf(i)).collect(Collectors.joining(", "));
                ReportHelper.log("Faild to insert rows: ", failedRowNumbers);

                return -1;
            }
        }catch(Exception ex){
            ReportHelper.reportAsStepLog("Exception: %s", ex.getMessage());
            return  -1;
        }finally {
            if (failedCount == 0) {
                ReportHelper.log("%d rows deleted and %d rows inserted successfully in %s.",
                        deleteCount, insertCount, Duration.between(start, Instant.now()));
            } else {
                ReportHelper.reportAsStepLog("%d rows failed to be deleted then inserted in %s.",
                        failedCount, Duration.between(start, Instant.now()));
            }
        }
    }

    private static int getColumnIndex(RowDataSupplier rowDataSupplier, String... columnKeys){
        Objects.requireNonNull(rowDataSupplier);
        Objects.requireNonNull(columnKeys);

        final List<String> columns = rowDataSupplier.getColumns();
        String matched = Arrays.stream(columnKeys)
                .filter(key -> MapHelper.bestMatchedKey(columns, key) != null)
                .findFirst().orElse(null);
        return matched == null ? -1 : columns.indexOf(MapHelper.bestMatchedKey(columns, matched));
    }

    private static Predicate<Object[]> getDateInRangeRowPredicate(RowDataSupplier rowDataSupplier, LocalDate fromDateInclusive, LocalDate toDateInclusive){
        Objects.requireNonNull(rowDataSupplier);
        Objects.requireNonNull(fromDateInclusive);
        Objects.requireNonNull(toDateInclusive);

        int matched = getColumnIndex(rowDataSupplier, DayTimeColumnKeys);
        if(matched == -1)
            return alwaysTrue;

        if(fromDateInclusive.equals(toDateInclusive)){
            return values -> fromDateInclusive.equals(asLocalDate(values[0]));
        } else if (fromDateInclusive.compareTo(toDateInclusive) < 0){
            return values -> {
                LocalDate date = asLocalDate(values[0]);
                return fromDateInclusive.compareTo(date) <= 0 && toDateInclusive.compareTo(date) >=0;
            };
        } else
            return alwaysFalse;
    }

    private static Predicate<Object[]> getStoryRowPredicate(RowDataSupplier rowDataSupplier, String... stories) {
        Objects.requireNonNull(rowDataSupplier);
        Objects.requireNonNull(stories);

        if(stories.length == 0)
            return alwaysTrue;

        int matched = getColumnIndex(rowDataSupplier, StoriesColumnKeys);
        if(matched == -1)
            return alwaysFalse;

        return values -> StringHelper.containsAnyIgnoreCase(values[0].toString(), stories);
    }

    private static Predicate<Object[]> getBothPredicate(RowDataSupplier rowDataSupplier, LocalDate fromDateInclusive, LocalDate toDateInclusive, String... stories){
        Objects.requireNonNull(rowDataSupplier);
        Objects.requireNonNull(fromDateInclusive);
        Objects.requireNonNull(toDateInclusive);
        Objects.requireNonNull(stories);

        int dateIndex = getColumnIndex(rowDataSupplier, DayTimeColumnKeys);
        int storyIndex = getColumnIndex(rowDataSupplier, StoriesColumnKeys);
        if(storyIndex == -1 && dateIndex == -1)
            return alwaysTrue;
        else if(storyIndex == -1)
            return getDateInRangeRowPredicate(rowDataSupplier, fromDateInclusive, toDateInclusive);
        else if(dateIndex == -1)
            return getStoryRowPredicate(rowDataSupplier, stories);

        Predicate<Object[]> datePredicate = getDateInRangeRowPredicate(rowDataSupplier, fromDateInclusive, toDateInclusive);
        return values -> datePredicate.test(values) && StringHelper.containsAny(values[1].toString(), stories);
    }

    private static Predicate<Object[]> getAnyPredicate(RowDataSupplier rowDataSupplier, LocalDate fromDateInclusive, LocalDate toDateInclusive, String... stories){
        Objects.requireNonNull(rowDataSupplier);
        Objects.requireNonNull(fromDateInclusive);
        Objects.requireNonNull(toDateInclusive);
        Objects.requireNonNull(stories);

        int dateIndex = getColumnIndex(rowDataSupplier, DayTimeColumnKeys);
        int storyIndex = getColumnIndex(rowDataSupplier, StoriesColumnKeys);
        if(storyIndex == -1 && dateIndex == -1)
            return alwaysTrue;
        else if(storyIndex == -1)
            return getDateInRangeRowPredicate(rowDataSupplier, fromDateInclusive, toDateInclusive);
        else if(dateIndex == -1)
            return getStoryRowPredicate(rowDataSupplier, stories);

        Predicate<Object[]> datePredicate = getDateInRangeRowPredicate(rowDataSupplier, fromDateInclusive, toDateInclusive);
        return values -> datePredicate.test(values) || StringHelper.containsAny(values[1].toString(), stories);
    }

    private static LocalDate asLocalDate(Object value){
        Objects.requireNonNull(value);

        if(value instanceof LocalDate)
            return (LocalDate)value;
        else if (value instanceof Date)
            return DateTimeHelper.fromDate((Date) value);
        else if (value instanceof String)
            return DateTimeHelper.dateFromString((String) value);

        throw new IllegalArgumentException();
    }


    public static File getFromDataFolder(String filename){
        File file= new File(filename);
        if (file.isAbsolute())
            return file;
        String classPath = ReconcileHelper.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        String xlsFilePath = classPath.substring(0, classPath.indexOf("ectest-ecpa")) + "Data/" + filename;
        return new File(xlsFilePath);
    }

    public static ReconcileHelper getReconcileHelper(String envName, String filepath){
        return getReconcileHelper(envName, filepath, null);
    }

    public static ReconcileHelper getReconcileHelper(String envName, String inFilepath, String outFilePath){
        SQLHelper helper = SQLHelper.of(envName);
        File file = new File(inFilepath);
        if(!file.exists()){
            file = getFromDataFolder(inFilepath);
        }
        if(!file.exists()){
            throw new InvalidArgumentException("Cannot locate the input excel file: " + inFilepath);
        }
        return new ReconcileHelper(helper, file, outFilePath);
    }

    public static int compareAndSave(SQLHelper sqlHelper, RowDataSupplier expections, ExcelSheetHelper resultSheet){
        int rowCount = expections.getRowCount();
        if(rowCount == 0)
            return 0;

        int deltaCount = 0;
        String script = expections.getSingleQueryTemplate();

        List<String> columnNames = expections.getColumns();
        int columnCount = columnNames.size();
        int[] keyIndexes = expections.getKeyIndexes();
        List<Integer> orderIndexes = expections.getOrderedColumns().stream()
                .map(s -> columnNames.indexOf(s))
                .collect(Collectors.toList());

        List<String> deltas = new ArrayList<>();
        int rowIndex = 0;
        try(Connection connection = sqlHelper.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(script)
        ){
            List<Integer> ignorableColumnIndexes = IntStream.range(0, columnCount).boxed()
                    .filter(i -> expections.isIgnorableColumn(columnNames.get(i)))
                    .collect(Collectors.toList());

            final List<String> normalized = expections.normalize(expections.getOrderedColumns());
            final int keyCount = expections.getKeyCount();
            final int nonKeyCount = normalized.size() - keyCount;
            final String[] keyColumnNames = normalized.stream().skip(nonKeyCount)
                    .toArray(size -> new String[size]);
            final String[] nonKeyColumnNames = normalized.stream().limit(nonKeyCount)
                    .toArray(size -> new String[size]);

            for(rowIndex=0; rowIndex < rowCount; rowIndex++){
                Object[] allValues = expections.getAllRowValues(rowIndex);
                Object[] expectedValues = expections.getRowValues(rowIndex);
                Object[] keyValues = Arrays.copyOfRange(expectedValues, nonKeyCount, nonKeyCount + keyCount);
                Object[] actualValues = sqlHelper.queryRowToArray(preparedStatement, script, keyValues);

                if(actualValues == null){
                    if(StringHelper.containsIgnoreCase(expectedValues, NoRecord)){
                        String description = expections.getRowDescription(keyColumnNames, keyValues);
                        ReportHelper.reportAsStepLog("As expected, the row identified by [%s] is not found.", description);
                    } else {
                        String keys = expections.getRowDescription(keyColumnNames, keyValues);
                        Object[] otherValues = Arrays.copyOfRange(expectedValues, 0, nonKeyCount);
                        String expects = expections.getRowDescription(nonKeyColumnNames, otherValues);
                        String description  = String.format("{[%s] --->\n [%s]}", keys, expects);
                        resultSheet.highlightMissingRow(rowIndex, keyIndexes, "Missing: " + description);
                        deltaCount++;
                        ReportHelper.reportAsStepLog("Failed to get row: %s", description);
                    }
                    continue;
                } else if (actualValues.length != expectedValues.length) {
                    throw new RuntimeException("Something wrong to run single query!");
                }

                deltas.clear();
                Object expected, actual;
                for (int j = 0; j < columnCount; j++) {
                    expected = allValues[j];
                    if(ignorableColumnIndexes.contains(j) || expections.isNegligibleCell(expected)) {
                        //Ignorable cell, make it gray
                        resultSheet.deEmphasizeCell(rowIndex, j);
                        continue;
                    }

                    actual = actualValues[orderIndexes.indexOf(j)];
                    //Actual value of NULL to be accepted only when the expected is of String value 'null' ignore-cased
                    if(actual == null && NULL.equalsIgnoreCase(expected.toString())){
                        continue;
                    }

                    if (!Objects.equals(expected, actual)) {
                        resultSheet.updateCell(rowIndex, j, actual, CommentTemplate);
                        deltaCount++;
                        deltas.add(String.format("[col%d]%s != %s", j+1, toString(expected), toString(actual)));
                    }
                }
                if (deltas.size() > 0) {
                    ReportHelper.log("----Row %d identified by keys %s has %d deltas: %s", rowIndex+1,
                            getArrayDescription(keyValues), deltas.size(), String.join(", ", deltas));
                }
            }
        }catch (Exception ex){
            throw new RuntimeException("At row " + rowIndex + ": " + ex.getMessage());
        }
        if (deltaCount > 0) {
            ReportHelper.reportAsStepLog("Error: %d Differences found in %s data view.",
                    deltaCount, expections.getTablename());
        }
        return deltaCount;

    }

    public static final Map<Class<?>, FunctionThrowable<Object, String>> objectToStrings = new HashMap<Class<?>, FunctionThrowable<Object, String>>(){{
        put(LocalDateTime.class, obj -> DateTimeHelper.dateTimeString((LocalDateTime)obj));
        put(LocalDate.class, obj -> DateTimeHelper.dateString((LocalDate)obj));
        put(Date.class, obj -> DateTimeHelper.dateString((Date)obj));
        put(LocalTime.class, obj -> DateTimeHelper.timeString((LocalTime)obj));
        put(String.class, obj -> (String)obj);
        put(Boolean.class, obj -> ((Boolean)obj).toString());
        put(Byte.class, obj -> ((Byte)obj).toString());
        put(Integer.class, obj -> ((Integer)obj).toString());
        put(Long.class, obj -> ((Long)obj).toString());
        put(Double.class, obj -> ((Double)obj).toString());
        put(Float.class, obj -> ((Float)obj).toString());
        put(URL.class, obj -> ((URL)obj).toString());
        put(byte[].class, obj -> ((byte[])obj).toString());
    }
    };

    public static String toString(Object obj){
        if(obj == null) return NULL;
        Class clazz = obj.getClass();
        if(objectToStrings.containsKey(clazz))
            return objectToStrings.get(clazz).tryApply(obj);
        else
            throw new RuntimeException(clazz + " is not supported!?");
    }

    public static String getArrayDescription(Object[] values){
        if(values == null)
            return NULL;

        return String.format("[%s]", Arrays.stream(values).map(o -> toString(o)).collect(Collectors.joining(", ")));
    }

    public static Integer[] getIndexesOfDelta(Object[] expectedValues, Object[] actualValues){
        if(expectedValues == null || actualValues == null)
            return null;
        int length = expectedValues.length;
        if(length != actualValues.length)
            return null;

        List<Integer> difIndexes = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            Object expected = expectedValues[i];
            Object actual = actualValues[i];
            if (expected == null){
                if(actual == null) continue;
            } else if(expected.equals(actual))
                continue;

            difIndexes.add(i);
        }
        return difIndexes.stream().toArray(Integer[]::new);
    }

    public static Map<String, Map<String, Object>> getAllParameters(String inputFilename, String... path) throws  Exception{
        File inputFile = ResourceHelper.getInputFile(inputFilename, path);
        if(inputFile.exists()){
            return getAllParameters(inputFile);
        } else {
            ReportHelper.reportAsStepLog("Failed to load input file: %s", inputFile.getAbsolutePath());
            return null;
        }
    }

    public static Map<String, Map<String, Object>> getAllParameters(File inputFile) throws  Exception{
        Map<String, Map<String, Object>> storyParameters = new HashMap<>();

        try(
                ExcelBookHelper excelBookHelper = new ExcelBookHelper(inputFile);
        ){
            ExcelSheetHelper parameters = new ExcelSheetHelper(excelBookHelper, ParametersSheetName);
            Map<String, Object> arguments = parameters.getKeyValues(ExcelBookHelper.DefaultKeyColumnName, ExcelBookHelper.DefaultValueColumnName);
            arguments.put(InExcelFileLabel, excelBookHelper.getFile().getAbsolutePath());
            storyParameters.put(DefualtParametersKey, arguments);

            List<String> columns = parameters.getColumns();
            List<String> stories = columns.stream()
                    .filter(name -> StringUtils.isNotEmpty(name) && ReconcileHelper.StoryNumberPattern.matcher(name).matches())
                    .map(name -> name.trim())
                    .collect(Collectors.toList());

            if(stories.size() > 0) {
                for (String story : stories) {
                    Map<String, Object> storyParams = parameters.getKeyValues(ExcelBookHelper.DefaultKeyColumnName, story, true);
                    Map<String, Object> mergedParams = Stream.of(arguments, storyParams)
                            .flatMap(m -> m.entrySet().stream())
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> v2 ));
                    storyParameters.put(story, mergedParams);
                }
            }
            return storyParameters;
        }
    }



    private final File inFile;
    private final SQLHelper sqlHelper;
    private final Lazy<ExcelBookHelper> inBookHelperLazy;
    private final Lazy<List<String>> outSheetNamesLazy;
    private final Lazy<Map<String, Map<String, Object>>> allParameters;
    public final Lazy<ExcelBookHelper> resultBookLazy;

    public ReconcileHelper(SQLHelper sqlHelper, File inputFile, String resultFilename){
        Objects.requireNonNull(sqlHelper);
        Objects.requireNonNull(inputFile);
        if(!inputFile.exists())
            throw new IllegalArgumentException("Invalid input file: " + inputFile);
        this.sqlHelper = sqlHelper;
        this.inFile = inputFile;
        this.inBookHelperLazy  = new Lazy<ExcelBookHelper>(() -> new ExcelBookHelper(inFile));
        this.outSheetNamesLazy = new Lazy<List<String>>(() -> inBookHelperLazy.getValue().getOutSheetNames().stream()
                .map(name -> ExcelBookHelper.escapeSheetName(name))
                .collect(Collectors.toList()));
        this.allParameters = this.inBookHelperLazy.create(book -> book.getAllNameValues(inputFile));
        this.resultBookLazy = new Lazy<ExcelBookHelper>(() -> inBookHelperLazy.getValue().getResultBookCopy(resultFilename));
    }

    public ReconcileHelper(SQLHelper sqlHelper, File inputFile){
        this(sqlHelper, inputFile, null);
    }

    public int callProcedure(String procedure){
        return sqlHelper.call(procedure);
    }

    private Map<String, Object> getDefaultTestParameters(){
        return getStoryParameters(DefualtParametersKey);
    }

    public Map<String, Object> getStoryParameters(String storyNumber){
        Objects.requireNonNull(storyNumber);

        return allParameters.getValue().get(storyNumber);
    }

    public List<String> getOutSheetNames(){
        return outSheetNamesLazy.getValue();
    }

    /**
     * Using the input sheets to inject their data to corresponding DB tables.
     * @return  Total number of rows changed
     */
    public int injectData(){
        Instant end;
        Instant start = Instant.now();
        String tableName = null;
        try(ExcelBookHelper inBook = new ExcelBookHelper(inFile)){
            List<String> inSheetNames = inBook.getInSheetNames();
            final List<ExcelSheetHelper> inDataSuppliers = inSheetNames.stream().map(name -> inBook.getSheetHelper(name))
                    .collect(Collectors.toList());

            int total = 0;
            for (int i = 0; i < inDataSuppliers.size(); i++) {
                ExcelSheetHelper sheet = inDataSuppliers.get(i);
                tableName = sheet.getTablename();
                int changes = batchUpdateOrInsert(sheet);
                total += changes;
                end = Instant.now();
                Duration duration = Duration.between(start, end);
                ReportHelper.reportAsStepLog("%s: %d rows changed in %dms", tableName, changes, duration.toMillis());
                start = end;
            }
            return total;
        }catch (Exception ex){
            end = Instant.now();
            ReportHelper.reportAsStepLog("%dms to process %s: %s", Duration.between(start, end).toMillis(), tableName, ex.getMessage());
            return -1;
        }
    }

//    /**
//     * Using the input sheets to inject their data to corresponding DB tables.
//     * @return  Total number of rows changed
//     */
//    public int injectData(DataStrategy dataStrategy, LocalDate fromDateInclusive, LocalDate toDateInclusive, String... stories){
//        Instant end;
//        Instant start = Instant.now();
//        String tableName = null;
//        try(ExcelBookHelper inBook = new ExcelBookHelper(inFile)){
//            List<String> inSheetNames = inBook.getInSheetNames();
//            final List<ExcelSheetHelper> inDataSuppliers = inSheetNames.stream().map(name -> inBook.getSheetHelper(name))
//                    .collect(Collectors.toList());
//
//            int total = 0;
//            for (int i = 0; i < inDataSuppliers.size(); i++) {
//                ExcelSheetHelper sheet = inDataSuppliers.get(i);
//                tableName = sheet.getTablename();
//                int[] rowIndexes = ReconcileHelper.getStoryRowIndexes(sheet, dataStrategy, fromDateInclusive, toDateInclusive, stories);
//                if(rowIndexes.length == 0)
//                    continue;
//
//                int changes = batchUpdateOrInsert(sheet, rowIndexes);
//                total += changes;
//                end = Instant.now();
//                Duration duration = Duration.between(start, end);
//                ReportHelper.reportAsStepLog("%s: %d rows changed in %s", tableName, changes, duration);
//                start = end;
//            }
//            return total;
//        }catch (Exception ex){
//            end = Instant.now();
//            ReportHelper.reportAsStepLog("%dms to process %s: %s", Duration.between(start, end).toMillis(), tableName, ex.getMessage());
//            return -1;
//        }
//    }
//
//    /**
//     * Using the input sheets to inject their data to corresponding DB tables.
//     * @return  Total number of rows changed
//     */
//    public int injectData(Story... stories){
//        if(stories == null || stories.length == 0)
//            return 0;
//
//        Instant end;
//        Instant start = Instant.now();
//        String tableName = null;
//        try(ExcelBookHelper inBook = new ExcelBookHelper(inFile)){
//            List<String> inSheetNames = inBook.getInSheetNames();
//            final List<ExcelSheetHelper> inDataSuppliers = inSheetNames.stream().map(name -> inBook.getSheetHelper(name))
//                    .collect(Collectors.toList());
//
//            int total = 0;
//            for (int i = 0; i < inDataSuppliers.size(); i++) {
//                ExcelSheetHelper sheet = inDataSuppliers.get(i);
//                tableName = sheet.getTablename();
//                int[] rowIndexes = ReconcileHelper.getDisctinctStoryRowIndexes(sheet, stories);
//                if(rowIndexes.length == 0)
//                    continue;
//
//                int changes = batchUpdateOrInsert(sheet, rowIndexes);
//                total += changes;
//                end = Instant.now();
//                Duration duration = Duration.between(start, end);
//                ReportHelper.reportAsStepLog("%s: %d rows changed in %s", tableName, changes, duration);
//                start = end;
//            }
//            return total;
//        }catch (Exception ex){
//            end = Instant.now();
//            ReportHelper.reportAsStepLog("%dms to process %s: %s", Duration.between(start, end).toMillis(), tableName, ex.getMessage());
//            return -1;
//        }
//    }

    /**
     * Get the expected results of any output sheets, compare them with the actual values in DB and keep the differences in the result file.
     * @param resultFilePath Identify the result file as either:
     *                       - Absolute path to the target file with folder name.
     *                       - Null or empty, then the input filename would be used to deduct the result file in result folder like "target/cucumber".
     * @return number of deltas.
     */
    public int reconcileToResultFile(String resultFilePath){
        try(
                ExcelBookHelper inBook = inBookHelperLazy.getValue();
                ExcelBookHelper resultBook = resultBookLazy.getValue();
        ){
            long start = System.currentTimeMillis();
            List<String> outSheetNames = inBook.getOutSheetNames();

            int totalDifferences = 0;

            for (int i = 0; i < outSheetNames.size(); i++) {
                ExcelSheetHelper sheet = inBook.getSheetHelper(outSheetNames.get(i));
                ExcelSheetHelper resultSheet = resultBook.getSheetHelper(sheet.getTablename());
                int deltas = compareAndSave(sqlHelper, resultSheet, resultSheet);
                totalDifferences += deltas;
            }
            resultBook.save();
            ReportHelper.reportAsStepLog("Delta data is saved, click <a href='%s'>here</a> to see detailed results", resultFilePath);
            ReportHelper.reportAsStepLog("Result Path : %s", resultFilePath);
            ReportHelper.reportAsStepLog("There are %d differences in %d sheets after %dms",
                    totalDifferences, outSheetNames.size(), System.currentTimeMillis()-start);
            return totalDifferences;
        }catch (Exception ex){
            ReportHelper.reportAsStepLog("Exception occurred while processing the result file %s", ex.getMessage().toString());
            return -1;
        }
    }

    public int reconcile(){
        try(
                ExcelBookHelper resultBook = resultBookLazy.getValue();
        ){
            long start = System.currentTimeMillis();
            List<String> outSheetNames = getOutSheetNames();

            int totalDifferences = 0;

            for (int i = 0; i < outSheetNames.size(); i++) {
                ExcelSheetHelper resultSheet = resultBook.getSheetHelper(outSheetNames.get(i));
                int deltas = compareAndSave(sqlHelper, resultSheet, resultSheet);
                totalDifferences += deltas;
            }
            resultBook.save();
            ReportHelper.reportAsStepLog("Delta data is saved, click <a href='%s'>here</a> to see detailed results", resultBook.getFile().getAbsolutePath());
            ReportHelper.reportAsStepLog("There are %d differences in %d sheets after %dms",
                    totalDifferences, outSheetNames.size(), System.currentTimeMillis()-start);
            return totalDifferences;
        }catch (Exception ex){
            ReportHelper.reportAsStepLog("Exception while processing the result file %s", ex.getMessage());
            return -1;
        }
    }

    /**
     * Load data from the given RowDataSupplier and update/insert corresponding DB table in batches.
     * @param supplier  Data Supplier from a Excel sheet.
     * @return total count of rows that have been updated or inserted if all success, otherwise -1 when something failed.
     */
    public int batchUpdateOrInsert(RowDataSupplier supplier) {
        int rowCount = supplier.getRowCount();
        int[] rowIndexes = IntStream.range(0, rowCount).toArray();
        return batchUpdateOrInsert(sqlHelper, supplier);
    }

    /**
     * Load data from the given RowDataSupplier and update/insert corresponding DB table in batches.
     * @param supplier  Data Supplier from a Excel sheet.
     * @param rowIndexes Indexes of the rows of the supplier to be injected.
     * @return total count of rows that have been updated or inserted if all success, otherwise -1 when something failed.
     */
    public int batchUpdateOrInsert(RowDataSupplier supplier, int[] rowIndexes){
        return batchUpdateOrInsert(sqlHelper, supplier, rowIndexes);
    }

    public Map<String, Map<String, Object>> getAllParameters() throws Exception{
        return getAllParameters(this.inFile);
    }
//
//    public int trimDataRows(List<String> stories){
//        if(stories == null || stories.isEmpty() || (stories.size() == 1 && stories.get(0).equals(DefualtParametersKey))){
//            //No needs to trim output sheets if no specific story required
//            return 0;
//        }
//
//        List<String> storiesDefined = new ArrayList<>(allParameters.getValue().keySet());
//        List<String> matchedStories = stories.stream()
//                .map(name -> MapHelper.bestMatchedKey(storiesDefined, name))
//                .filter(story -> story != null)
//                .collect(Collectors.toList());
//
//        if(matchedStories.isEmpty())
//            return 0;
//
//        ReportHelper.log("There are %d stories matched.", matchedStories.size());
//
//        int totalRowsRemoved = 0;
//        List<String> outSheetNames = getOutSheetNames();
//
//        try(
//                ExcelBookHelper resultBook = resultBookLazy.getValue();
//        ) {
//            for (String outSheetName : outSheetNames) {
//                ExcelSheetHelper sheet = resultBook.getSheetHelper(outSheetName);
//                List<int[]> rowsToKeep = new ArrayList<>();
//                for (String story : matchedStories) {
//                    Map<String, Object> parameters = getStoryParameters(story);
//                    LocalDate fromDate = DateTimeHelper.fromDate((Date)parameters.get(FromDateLabel));
//                    LocalDate toDate = DateTimeHelper.fromDate((Date)parameters.get(ToDateLabel));
//                    //Assume the DataStrategy as DateRange if not defined
//                    DataStrategy dataStrategy = parameters.containsKey(DataStrategyLabel) ?
//                            DataStrategy.fromString(parameters.get(DataStrategyLabel).toString())
//                            : DataStrategy.DateRange;
//                    int[] storyRows = ReconcileHelper.getStoryRowIndexes(sheet, dataStrategy, fromDate, toDate, story);
//                    rowsToKeep.add(storyRows);
//                }
//                List<Integer> sortedDistictIndexes = rowsToKeep.stream()
//                        .flatMap(array -> Arrays.stream(array).boxed())
//                        .distinct().sorted()
//                        .collect(Collectors.toList());
//                int[] array = sortedDistictIndexes.stream()
//                        .mapToInt(i -> i)
//                        .toArray();
//                int rowsRemovedInSheet = sheet.trimDataRows(array);
//                totalRowsRemoved += rowsRemovedInSheet;
//            }
//
//            resultBook.save();
//            return totalRowsRemoved;
//        }
//
//    }

}
