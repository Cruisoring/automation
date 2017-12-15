package com.least.automation.wrappers;

import com.google.common.base.Supplier;
import com.least.automation.helpers.StringExtensions;
import com.least.automation.interfaces.WorkingContext;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class UITable extends UICollection<UITable.UIRow> {

    public static final int selectorPosition = By.cssSelector("*").toString().indexOf('*');
    public static final String defaultRowSeperator = "\r\n";

    public static UIRow createRow(WorkingContext context, Integer rowIndex) {
        if (! (context instanceof UITable)){
            throw new IllegalArgumentException("Context must be a UITable instance");
        }

        UITable table = (UITable)context;
        return table.new UIRow(table, rowIndex);
    }

    public final String tableCssSelector;
    public final UICollection<UIObject> headers;
    private List<String> headerTexts;

    //TODO: the tabelCssSelector might not contains table tagname like "parent>.[id='...']"
    public UITable(WorkingContext context, String tableCssSelector, Integer index) {
        super(context, By.cssSelector(tableCssSelector), index, (c, i) -> createRow(c, i));

        this.tableCssSelector = tableCssSelector ==null ? "table" : tableCssSelector;
        headers = new UICollection<UIObject>(this,
                By.cssSelector(tableCssSelector+">thead>tr"),
                By.cssSelector(tableCssSelector+">thead>tr>th"));
    }

    public UITable(WorkingContext context, String tableCssSelector) {
        this(context, tableCssSelector, null);
    }

    public List<String> getHeaders() {
        if (headerTexts != null) {
            return headerTexts;
        }
        List<UIObject> headerCells = headers.getChildren();
        headerTexts = IntStream.range(0, headerCells.size())
                .mapToObj(i -> headerCells.get(i).getTextContent())
                .collect(Collectors.toList());
        return headerTexts;
    }

    @Override
    public void invalidate(){
        super.invalidate();
        children = null;
        headerTexts = null;
    }

    public int getIndex(String key) {
        List<String> headers = getHeaders();
        int colIndex = headers.indexOf(key);
        if (colIndex != -1)
            return colIndex;
        colIndex = IntStream.range(0, headers.size())
                .filter(i -> StringUtils.containsIgnoreCase(headers.get(i), key))
                .findFirst().orElse(-1);
        return colIndex;
    }


    public UIObject getHeader(int colIndex){
        return headers.get(colIndex);
    }

    public UIObject getHeader(String headerKey){
        return headers.get(headerKey);
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
        return getCell(getIndex(header), rowKeys);
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

    public class UIRow extends UICollection<UIObject> {
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
            int colIndex = getTable().getIndex(key);
            return colIndex==-1 ? super.get(key) : get(colIndex);
        }

        @Override
        public String toString() {
            String text = getAllText();
            return index == null ? text : "[" + index + "]: " + text;
        }
    }
}
