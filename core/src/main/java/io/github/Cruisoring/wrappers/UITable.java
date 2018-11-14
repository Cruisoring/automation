package io.github.Cruisoring.wrappers;

import com.google.common.base.Supplier;
import io.github.Cruisoring.helpers.StringExtensions;
import io.github.Cruisoring.interfaces.RowDataSupplier;
import io.github.Cruisoring.interfaces.WorkingContext;
import io.github.cruisoring.Lazy;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.openqa.selenium.By;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class UITable extends UICollection<UITable.UIRow> implements RowDataSupplier {

    public static final int selectorPosition = By.cssSelector("*").toString().indexOf('*');
    public static final String defaultRowSeperator = "\r\n";
    public static final String leafTablePatternString = "<(table)\\b[^>]*>(?:(?>[^<]+)|<(?!t\\1\\b[^>]*>))*?</\\1>";
    public static final Pattern simpleTablePattern = Pattern.compile(leafTablePatternString, Pattern.MULTILINE);
    public static final Pattern tableHeadPattern = Pattern.compile(leafTablePatternString.replace("table", "thead"), Pattern.MULTILINE);
    public static final Pattern tableHeadCellPattern = Pattern.compile(leafTablePatternString.replace("table", "th"), Pattern.MULTILINE);
    public static final Pattern tableBodyPattern = Pattern.compile(leafTablePatternString.replace("table", "tbody"), Pattern.MULTILINE);
    public static final Pattern tableRowPattern = Pattern.compile(leafTablePatternString.replace("table", "tr"), Pattern.MULTILINE);
    public static final Pattern tableCellPattern = Pattern.compile(leafTablePatternString.replace("table", "td"), Pattern.MULTILINE);
    public static final Pattern anyTableCellPattern = Pattern.compile(leafTablePatternString.replace("table", "td|th"), Pattern.MULTILINE);
    public static final Pattern linkPattern = Pattern.compile(leafTablePatternString.replace("table", "a"), Pattern.MULTILINE);


    public static UIRow createRow(WorkingContext context, Integer rowIndex) {
        if (! (context instanceof UITable)){
            throw new IllegalArgumentException("Context must be a UITable instance");
        }

        UITable table = (UITable)context;
        return table.new UIRow(table, rowIndex);
    }

    public final String tableCssSelector;
    public final UICollection<UIObject> headers;
    private final Lazy<List<String>> headerTextsLazy;

    public UITable(WorkingContext context, String tableCssSelector, Integer index) {
        super(context, By.cssSelector(tableCssSelector), index, (c, i) -> createRow(c, i));

        this.tableCssSelector = tableCssSelector ==null ? "table" : tableCssSelector;
        headers = new UIObject.Collection(this,
                By.cssSelector(tableCssSelector+">thead>tr"), 0,
                By.cssSelector(tableCssSelector+">thead>tr>th"));
        headerTextsLazy = new Lazy<List<String>>(() -> {
            String outerHtml = this.getOuterHTML();
            String headerHtml = StringExtensions.getFirstSegment(outerHtml, tableHeadPattern);
            return StringExtensions.getSegments(headerHtml, tableHeadCellPattern);
        });
    }

    public UITable(WorkingContext context, String tableCssSelector) {
        this(context, tableCssSelector, null);
    }

    public List<String> getHeaders() {
        return headerTextsLazy.getValue();
    }

    @Override
    public void invalidate(){
        super.invalidate();
        headerTextsLazy.closing();
    }

    public UIObject getHeader(int colIndex){
        return headers.get(colIndex);
    }

    public UIObject getHeader(String headerKey){
        return headers.get(getColumnIndex(headerKey));
    }

    public List<String> getRowTexts() {
        String bodyHtml = getFreshInnerHTML();
        bodyHtml = bodyHtml.substring(bodyHtml.indexOf("<tbody"));

        Pattern p = Pattern.compile("(<tr.*?>.*?</tr>)");
        Matcher matcher = p.matcher(bodyHtml);

        List<String> childrenTexts = new ArrayList<>();
        while(matcher.find()){
            String rowText = StringExtensions.extractHtmlText(matcher.group(1));
            childrenTexts.add(rowText);
        }
        //childrenTexts.add(0, headersText);
        return childrenTexts;
    }

    public UIObject getCell(int colIndex, Object... rowKeys) {
        UIObject cell = null;
//        try(Logger.Timer timer = Logger.M()){
            if(colIndex < 0 || colIndex >= headers.size())
                return null;
            UIRow row = get(getRowTexts(), rowKeys);
            if(row == null)
                return null;

            cell = row.get(colIndex);
//        }catch (Exception e){
//
//        }
        return cell;
    }

    public UIObject getCell(String header, Object... rowKeys) {
        return getCell(getColumnIndex(header), rowKeys);
    }

    @Override
    public UIRow get(String rowKey) {
        return get(()-> getRowTexts(), rowKey);
    }

    public UIRow get(Supplier<List<String>> rowTextsSupplier, Object... keys) {
        List<String> rowSources = rowTextsSupplier.get();
        String bestMatched = rowSources.stream()
                .filter(m->StringExtensions.containsAllIgnoreCase(m, keys))
                .sorted((l, r) -> l.length()- r.length())   //Order by length of the menu values
                .findFirst().orElse(null);

        int index = rowSources.indexOf(bestMatched);
        return get(index);
    }

    public Boolean containsAll(Object... keys) {
        String allText = getFreshAllText();
        return StringExtensions.containsAllIgnoreCase(allText, keys);
    }

    /**
     * Get the name of SQL table to be processed.
     *
     * @return Name of the table.
     */
    @Override
    public String getTablename() {
        return null;
    }

    /**
     * Given a solid column name, find its ordinal index.
     *
     * @param columnName Name of the concerned column.
     * @return Index of the column concerned (0 based)
     */
    @Override
    public Integer getColumnIndex(String columnName) {
        List<String> headers = getHeaders();
        int colIndex = headers.indexOf(columnName);
        if (colIndex != -1)
            return colIndex;
        colIndex = IntStream.range(0, headers.size())
                .filter(i -> StringUtils.containsIgnoreCase(headers.get(i), columnName))
                .findFirst().orElse(-1);
        return colIndex;
    }


    /**
     * Get all column names defined in the RowDataSupplier.
     *
     * @return List of column names.
     */
    @Override
    public List<String> getColumns() {
        return headerTextsLazy.getValue();
    }

    /**
     * Get a sorted name list of key or non-key column, ignorable columns not included.
     *
     * @return a sorted name list of non-key first and followed by key columns, ignorable columns not included.
     */
    @Override
    public List<String> getOrderedColumns() {
        return getColumns();
    }

    public List<String[]> getTableCells(){
        String tableHtml = this.getOuterHTML();
        String head = StringExtensions.getFirstSegment(tableHtml, tableHeadPattern);
        String body = StringExtensions.getFirstSegment(tableHtml, tableBodyPattern);

        List<String> rowElements = StringExtensions.getSegments(head, tableRowPattern);
        rowElements.addAll(StringExtensions.getSegments(body, tableRowPattern));
        List<String[]> cellsOfRows = rowElements.stream()
                .map(row -> StringExtensions.getTexts(row, anyTableCellPattern, true))
                .map(list -> list.toArray(new String[list.size()]))
                .collect(Collectors.toList());
        return cellsOfRows;
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
        List<String[]> allCells = getTableCells();
        if(rowNumber < 0 || rowNumber >= allCells.size()-1){
            return null;
        }

        String[] rowCells = allCells.get(rowNumber+1);

        String[] values = Arrays.stream(columns)
                .mapToObj(i -> rowCells[i]).toArray(String[]::new);
        return values;
    }

    /**
     * Get the total row number assuming the first row as headers, and remaining as data rows.
     *
     * @return total row number
     */
    @Override
    public int getRowCount() {
        List<String[]> allCells = getTableCells();
        return allCells.size()-1;
    }

    /**
     * Get total column number that identified as non-empty cells of the first row.
     *
     * @return Number of columns identified in the first row.
     */
    @Override
    public int getColumnCount() {
        return headerTextsLazy.getValue().size();
    }

    /**
     * Closes this resource, relinquishing any underlying resources.
     * This method is invoked automatically on objects managed by the
     * {@code try}-with-resources statement.
     *
     * <p>While this interface method is declared to throw {@code
     * Exception}, implementers are <em>strongly</em> encouraged to
     * declare concrete implementations of the {@code close} method to
     * throw more specific exceptions, or to throw no exception at all
     * if the close operation cannot fail.
     *
     * <p> Cases where the close operation may fail require careful
     * attention by implementers. It is strongly advised to relinquish
     * the underlying resources and to internally <em>mark</em> the
     * resource as closed, prior to throwing the exception. The {@code
     * close} method is unlikely to be invoked more than once and so
     * this ensures that the resources are released in a timely manner.
     * Furthermore it reduces problems that could arise when the resource
     * wraps, or is wrapped, by another resource.
     *
     * <p><em>Implementers of this interface are also strongly advised
     * to not have the {@code close} method throw {@link
     * InterruptedException}.</em>
     * <p>
     * This exception interacts with a thread's interrupted status,
     * and runtime misbehavior is likely to occur if an {@code
     * InterruptedException} is {@linkplain Throwable#addSuppressed
     * suppressed}.
     * <p>
     * More generally, if it would cause problems for an
     * exception to be suppressed, the {@code AutoCloseable.close}
     * method should not throw it.
     *
     * <p>Note that unlike the {@link Closeable#close close}
     * method of {@link Closeable}, this {@code close} method
     * is <em>not</em> required to be idempotent.  In other words,
     * calling this {@code close} method more than once may have some
     * visible side effect, unlike {@code Closeable.close} which is
     * required to have no effect if called more than once.
     * <p>
     * However, implementers of this interface are strongly encouraged
     * to make their {@code close} methods idempotent.
     *
     * @throws Exception if this resource cannot be closed
     */
    @Override
    public void close() throws Exception {

    }

    public class UIRow extends UIObject.Collection {
        public static final String cellTextSplitter = "  |  ";
        public static final String cellTextConnector = "\t";

        public UITable getTable() {
            return (UITable) parent;
        }

        public UIRow(UITable table, Integer rowIndex) {
            super(table,
                    By.cssSelector(table.tableCssSelector + ">tbody>tr"), rowIndex,
                    By.cssSelector(table.tableCssSelector + ">tbody>tr>td"));
        }

        public UIRow(UITable table) {
            this(table, null);
        }

        @Override
        public UIObject get(String key) {
            int colIndex = getTable().getColumnIndex(key);
            return colIndex==-1 ? super.get(key) : get(colIndex);
        }

        @Override
        public String toString() {
            String text = getAllText();
            return index == null ? text : "[" + index + "]: " + text;
        }
    }
}
