package io.github.Cruisoring.interfaces;

import com.google.common.base.Strings;
import io.github.Cruisoring.helpers.StringHelper;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public interface RowDataSupplier extends AutoCloseable {
    //Following Strings cannot be used in SQL Column Name, could add more but be mind of the predication happens with their orders
    final static String[] KeyColumnSuffixes = new String[]{ "%KEY", "*KEY", "*", "%"};

    //Prefixes to indicate the column as ignorable
    final static String[] IgnorableColumnPrefixes = new String[]{ "#", "_"};

    //Indicators to show the cell can be negligible
    final static String[] NegligibleCellIndicators = new String[] { "N/A" };

    /**
     * Get the name of SQL table to be processed.
     * @return Name of the table.
     */
    String getTablename();

    /**
     * Given a solid column name, find its ordinal index.
     * @param columnName Name of the concerned column.
     * @return  Index of the column concerned (0 based)
     */
    Integer getColumnIndex(String columnName);

    /**
     * Get all column names defined in the RowDataSupplier.
     * @return  List of column names.
     */
    List<String> getColumns();

    /**
     * Get a sorted name list of key or non-key column, ignorable columns not included.
     * @return  a sorted name list of non-key first and followed by key columns, ignorable columns not included.
     */
    List<String> getOrderedColumns();

    /**
     * Retrieve the values of the concerned row specified with their column indexes as an array of Object.
     * @param rowNumber Row number starting from 0.
     * @param columns   indexes of the concerned columns starting from 0.
     * @return  An Object arrays of the concerned row and columns if the row number is between 0 and getRowCount() exclusively;
     *          otherwise null.
     */
    Object[] getRowValues(int rowNumber, int[] columns);

    /**
     * Get the total row number assuming the first row as headers, and remaining as data rows.
     * @return total row number
     */
    int getRowCount();

    /**
     * Get total column number that identified as non-empty cells of the first row.
     * @return Number of columns identified in the first row.
     */
    int getColumnCount();

    /**
     * Retrieve ALL column names of a RowDataSupplier, return them as a list with non-key columns before all key columns.
     * Thus both Update and Insert SQL statements could use data in this order.
     * @return
     */
    default Map<String, Integer> getColumnIndexes(){
        final List<String> columns =  this.getColumns();
        List<String> validColumns = columns.stream().filter(c -> StringUtils.isNotEmpty(c)).collect(Collectors.toList());
        Map<String, Integer> indexes = IntStream.range(0, validColumns.size()).boxed()
                .collect(Collectors.toMap(
                        index -> validColumns.get(index),
                        index -> columns.indexOf(validColumns.get(index))
                ));
        return indexes;
    }

    /**
     * Get the prefix that indicate the given columnName is an ignorable column.
     * @param columnName   the column name to be evaluated
     * @return  prefix string indicating the columnName to be ignorable, null if it is not matched
     */
    default String getKeySuffix(String columnName){
        Objects.requireNonNull(columnName);
        return Arrays.stream(KeyColumnSuffixes)
                .filter(prefix -> StringUtils.endsWithIgnoreCase(columnName.trim(), prefix))
                .findFirst()
                .orElse(null);
    }

    /**
     * Get the suffix that indicate the given columnName is a key column.
     * @param columnName   the column name to be evaluated
     * @return  suffix string indicating the columnName to be key, null if it is not a key column
     */
    default String getIgnorablePrefix(String columnName){
        Objects.requireNonNull(columnName);
        return Arrays.stream(IgnorableColumnPrefixes)
                .filter(suffix -> StringUtils.startsWithIgnoreCase(columnName.trim(), suffix))
                .findFirst()
                .orElse(null);
    }

    /**
     * Get the indicator showing the specific cell shall be negligible.
     * @param cellValueString   the displayed value of the cell
     * @return  case-insensitive indicator if the cell is negligible, null if it is not
     */
    default String getNegligibleCellIndicator(String cellValueString){
        Objects.requireNonNull(cellValueString);
        return Arrays.stream(NegligibleCellIndicators)
                .filter(indicator -> StringUtils.containsIgnoreCase(cellValueString.trim(), indicator))
                .findFirst()
                .orElse(null);
    }

    /**
     * Check if the columnName indicate it is a key column
     * @param columnName    the column name to be evaluated
     * @return  <tt>true</tt> if it is Key column, otherwise <tt>false</tt>
     */
    default boolean isKeyColumn(String columnName){
        return getKeySuffix(columnName) != null;
    }

    /**
     * Check if the columnName indicate it is an ignorable column
     * @param columnName    the column name to be evaluated
     * @return  <tt>true</tt> if it is ignorable column, otherwise <tt>false</tt>
     */
    default boolean isIgnorableColumn(String columnName) {
        return Strings.isNullOrEmpty(columnName) || getIgnorablePrefix(columnName) != null;
    }

    /**
     * Check if the cell content indicate it is an negligible one with any predefined case-insensitive indicators
     * @param cellValue    the value of the cell
     * @return  <tt>true</tt> if it is negligible cell, otherwise <tt>false</tt>
     */
    default boolean isNegligibleCell(Object cellValue) {
        return cellValue == null || getNegligibleCellIndicator(cellValue.toString()) != null;
    }

    /**
     * Get the number of key columns
     * @return  number of keys
     */
    default int getKeyCount() {
        return (int) getColumns().stream()
                .filter(c -> isKeyColumn(c))
                .count();
    }

    /**
     * Get all names of key columns as a list
     * @return list of the key columnNames
     */
    default List<String> getKeyColumns(){
        return getColumns().stream()
                .filter(c -> isKeyColumn(c))
                .collect(Collectors.toList());
    }

    /**
     * Normalize the given column names list to bypass ignorable ones, remove key suffixes and keep the original order.
     * @param columnNames   Names of the column to be nomalized
     * @return  list of normalized column names that can be mapped to SQL table columns.
     */
    default List<String> normalize(List<String> columnNames){
        List<String> result = columnNames.stream()
                .filter(c -> ! isIgnorableColumn(c))
                .map(name -> isKeyColumn(name) ?
                        name.trim().substring(0, name.trim().length()-getKeySuffix(name).length())
                        : name)
                .collect(Collectors.toList());
        return result;
    }

    /**
     * Get the column names of the RowDataSupplier, key columns followed by non-key columns, ignorable columns excluded.
     * @param supplier  Supplier of the Row Data.
     * @return  List of column names, non-key columns before any key columns, ignorable columns not included.
     */
    default List<String> getOrderedColumns(RowDataSupplier supplier){
        List<String> concernedColumns = getColumns().stream()
                .filter(c -> !isIgnorableColumn(c))
                .collect(Collectors.toList());

        concernedColumns.sort((c1, c2) -> {
            boolean isKey1 = isKeyColumn(c1);
            boolean isKey2 = isKeyColumn(c2);
            if(isKey1 == isKey2)
                return 0;
            return isKey1 ? 1 : -1;
        });

        return concernedColumns;
    }

    /**
     * Get the indexes of the key columns as an array.
     * @return  An array of integers denoting the key columns.
     */
    default int[] getKeyIndexes(){
        List<String> keyColumns = getKeyColumns();
        return keyColumns.stream()
                .map(this::getColumnIndex)
                .mapToInt(Integer::intValue)
                .toArray();
    }

    /**
     * Get the indexes of the name list of concerned columns.
     * @param orderedColumns names of the concered columns
     * @return
     */
    default int[] getColumnIndexes(List<String> orderedColumns){
        return orderedColumns.stream()
                .map(this::getColumnIndex)
                .mapToInt(Integer::intValue)
                .toArray();
    }

    /**
     * Get the row values that would be used to execute Update or Insert with the templates generated from the column names.
     * @param rowNumber Number of the concerned row, starting from 0.
     * @return  Object array containing the values of the concerned row.
     */
    default Object[] getRowValues(int rowNumber){
        int[] columnIndexes = getColumnIndexes(getOrderedColumns());
        return getRowValues(rowNumber, columnIndexes);
    }

    /**
     * Get the row keys that would be used to execute DELETE with the templates generated from the column names.
     * @param rowNumber Number of the concerned row, starting from 0.
     * @return  Object array containing the values of the concerned row.
     */
    default Object[] getRowKeys(int rowNumber){
        int[] keyIndexes = getKeyIndexes();
        return getRowValues(rowNumber, keyIndexes);
    }

    default String getRowDescription(String[] columnNames, Object[] values){
        Objects.requireNonNull(columnNames);
        Objects.requireNonNull(values);
        if(columnNames.length != values.length)
            throw new RuntimeException("Columns and values must be of same length.");

        String description = IntStream.range(0, columnNames.length).boxed()
                .map(i -> String.format("%s: '%s'", columnNames[i], StringHelper.asString(values[i])))
                .reduce((s1, s2) -> s1 + ", " + s2).orElse("?");
        return description;
    }

    /**
     * Get all cell values of a specific row that have non-empty column names defined
     * @param rowNumber index of the row (0 based)
     * @return  Object array containing the values of the concerned row.
     */
    default Object[] getAllRowValues(int rowNumber){
        int[] columnIndexes = IntStream.range(0, getColumnCount()).toArray();
        return getRowValues(rowNumber, columnIndexes);
    }

    /**
     * Get cell values of all rows as a list
     * @return  A list of Object array
     */
    default List<Object[]> getAllRows(){
        int rowCount = getRowCount();
        List<Object[]> allRows = IntStream.range(0, rowCount)
                .mapToObj(i -> getAllRowValues(i))
                .collect(Collectors.toList());
        return allRows;
    }


    /**
     * Get the SQL template to insert SQL table with default list of columnNames where all key columns followed by non-key columns
     * @return a string template used to create preparedStatement
     */
    default String getInsertTemplate(){
        return getInsertTemplate(getOrderedColumns());
    }

    /**
     * Get the SQL template to insert SQL table with a list of columnNames where all key columns followed by non-key columns
     * @param orderedColumns    all column names to build the update statement where all key columns followed by non-key columns
     * @return      a string template used to create preparedStatement
     */
    default String getInsertTemplate(List<String> orderedColumns){
        List<String> normalized = normalize(orderedColumns);
        String reservations = org.apache.commons.lang3.StringUtils.repeat("?", ", ", orderedColumns.size());
        String insertTemplate = String.format("INSERT INTO %s (%s) VALUES (%s)", getTablename(),
                String.join(", ", normalized), reservations);
        return insertTemplate;
    }

    /**
     * Get the SQL template to update SQL table with the default list of columnNames where all key columns followed by non-key columns
     * @return      a string template used to create preparedStatement
     */
    default String getUpdateTemplate() {
        return getUpdateTemplate(getOrderedColumns());
    }

    /**
     * Get the SQL template to update SQL table with a list of columnNames where all key columns followed by non-key columns
     * @param orderedColumns    all column names to build the update statement where all key columns followed by non-key columns
     * @return      a string template used to create preparedStatement
     */
    default String getUpdateTemplate(List<String> orderedColumns){
        int keyIndex = IntStream.range(0, orderedColumns.size())
                .filter(index -> isKeyColumn(orderedColumns.get(index)))
                .findFirst()
                .orElse(-1);

        if (keyIndex == 0)
            throw new RuntimeException("How to update when all columns are keys?!");

        List<String> keys = normalize(orderedColumns.subList(keyIndex, orderedColumns.size()));
        List<String> nonKeys = orderedColumns.subList(0, keyIndex);

        String updateTemplate = String.format("UPDATE %s SET %s=? WHERE %s=?", getTablename(),
                String.join("=?, ", nonKeys),
                String.join("=? AND ", keys)
        );

        return updateTemplate;
    }


    /**
     * Get the SQL template to query a single row from the concerned SQL table
     * @return      a string template used to create preparedStatement
     */
    default String getSingleQueryTemplate(){
        final List<String> columns = normalize(getOrderedColumns());
        final List<String> keyColumns = normalize(getKeyColumns());
        String whereSection = String.join("=? AND ", keyColumns);

        String queryTemplate = String.format("SELECT %s FROM %s WHERE %s=?",
                String.join(", ", columns), getTablename(), whereSection);
        return queryTemplate;
    }

    /**
     * Get the SQL template to delete a single row from the concerned SQL table
     * @return      a string template used to create preparedStatement
     */
    default String getDeleteTemplate(){
        final List<String> columns = normalize(getOrderedColumns());
        final List<String> keyColumns = normalize(getKeyColumns());
        String whereSection = String.join("=? AND ", keyColumns);

        String queryTemplate = String.format("DELETE FROM %s WHERE %s=?",
                getTablename(), whereSection);
        return queryTemplate;
    }

    /**
     * Filter the rows with given predicate on columns specified by columnIndexes to get matched row indexes.
     * @param columnIndexes      Indexes of the columns to be evaluated with the given predicate.
     * @param rowValuePredicate    Predicate to see if the row shall be returned.
     * @return  Index array of all matched rows (0 starting).
     */
    default int[] getRowIndexesWithPredicate(int[] columnIndexes, Predicate<Object[]> rowValuePredicate){
        Objects.requireNonNull(columnIndexes);
        Objects.requireNonNull(rowValuePredicate);

        int rowCount = getRowCount();
        List<Integer> matchedRows = new ArrayList<>();
        for (int row = 0; row < rowCount; row++) {
            Object[] rowValues = getRowValues(row, columnIndexes);
            if(rowValuePredicate.test(rowValues)){
                matchedRows.add(row);
            }
        }
        return matchedRows.stream().mapToInt(i -> i).toArray();
    }
}
