package io.github.Cruisoring.helpers;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ResultSetIterable implements Iterable<Object[]>, AutoCloseable {
    public static int maxRowsToHandle = 5000;

    /**
     * Convert the current row of the result to Map that keeps the values with their keys.
     * Notice: this method assume the resultSet.next() is called and checked externally.
     * @return  A map of the current row of the resultSet.
     */
    public static Map<String, Object> rowToMap(ResultSet resultSet){
        try {
            ResultSetIterable resultSetIterable = new ResultSetIterable(resultSet);
            int count = resultSetIterable.columnCount;
            Map<String, Object> map = new HashMap<>();
            for (int i = 0; i < count; i++) {
                map.put(resultSetIterable.columnNames[i],
                        resultSetIterable.valueGetters[i].get(resultSet, i+1));
            }
            return map;
        } catch (Exception e) {
            return null;
        }
    }

    private final ResultSetIterator resultSetIterator;
    public final int columnCount;
    public final String[] columnNames;
    public SQLHelper.GetStatementValue[] valueGetters;

    public ResultSetIterable(ResultSet resultSet) throws Exception {
        this.resultSetIterator = new ResultSetIterator(resultSet);

        ResultSetMetaData rsm = resultSet.getMetaData();
        columnCount = rsm.getColumnCount();
        columnNames = new String[columnCount];

        valueGetters = new SQLHelper.GetStatementValue[columnCount];
        for (int i = 0; i < columnCount; i++) {
            //System.out.println(i + " -> " + rsm.getColumnName(i));
            columnNames[i] = rsm.getColumnName(i+1);
            Integer valueType = rsm.getColumnType(i+1);
            valueGetters[i] = SQLHelper.SQL2JavaTypes.get(valueType);
        }
    }

    public int getRowCount(){
        return resultSetIterator.getRowCount();
    }

    @Override
    public Iterator<Object[]> iterator() {
        return resultSetIterator;
    }

    @Override
    public void close() throws Exception {
        resultSetIterator.close();
    }

    public List<Object[]> asArrayList(){
        List<Object[]> arrayList = new ArrayList<>();
        arrayList.add(columnNames);

        while (resultSetIterator.hasNext()){
            arrayList.add(resultSetIterator.next());
        }
        return arrayList;
    }

    private class ResultSetIterator implements Iterator<Object[]>, AutoCloseable {

        private final AtomicInteger counter = new AtomicInteger();
        private final ResultSet resultSet;

        ResultSetIterator(ResultSet resultSet){
            Objects.requireNonNull(resultSet);

            this.resultSet = resultSet;
        }

        @Override
        public boolean hasNext() {
            try {
                if(resultSet.getRow() > maxRowsToHandle)
                    throw new Exception("Too many rows to process");

                boolean hasNext= resultSet.next();
                if(hasNext){
                    counter.incrementAndGet();
                }
                return hasNext;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        public Object[] next() {
            try {
                Object[] nextRow = new Object[columnCount];
                for (int i = 0; i < columnCount; i++) {
                    nextRow[i] = valueGetters[i].get(resultSet, i+1);
                }
                return nextRow;
            } catch (Exception e){
                e.printStackTrace();
                return null;
            }
        }

        public int getRowCount(){
            try {
                while (resultSet.next()){
                    counter.incrementAndGet();
                }
                return counter.get();
            }catch (Exception e){
                return -1;
            }
        }

        @Override
        public void close() throws Exception {
            if(!resultSet.isClosed()){
                resultSet.close();
            }
        }
    }
}