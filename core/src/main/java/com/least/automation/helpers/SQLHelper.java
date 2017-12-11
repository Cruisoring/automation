package com.least.automation.helpers;

import com.least.automation.interfaces.TriConsumer;
import com.least.automation.interfaces.TriFunction;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.nio.file.Paths;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.*;

/**
 * SQL Statement Helper to facilitate invoking of either CallableStatement or PreparedStatement from buffered DB helpers.
 */
public class SQLHelper {

    public static final TriConsumer<PreparedStatement, Integer, java.lang.Object> defaultSetter = (statement, position, argument) -> statement.setObject(position, argument);

    public static final Map<Class<?>, TriConsumer<PreparedStatement, Integer, Object>> setters = new HashMap<>();
    public static final Map<Class<?>, BiFunction<CallableStatement, Integer, Object>> getters = new HashMap<>();
    
    public static final Map<Class<?>, Integer> types = new HashMap<>();
    public static final int JavaObjectType = Types.JAVA_OBJECT;

    private static Object retrieveObject(CallableStatement statement, Integer position){
        try {
            return statement.getObject(position);
        }catch (Exception e){
            return null;
        }
    }

    private static Object retrieveLocalDateTime(CallableStatement statement, Integer position){
        try {
            return statement.getTimestamp(position).toLocalDateTime();
        }catch (Exception e){
            return null;
        }
    }

    private static Object retrieveLocalDate(CallableStatement statement, Integer position){
        try {
            return statement.getDate(position).toLocalDate();
        }catch (Exception e){
            return null;
        }
    }

    private static Object retrieveDate(CallableStatement statement, Integer position){
        try {
            return statement.getDate(position);
        }catch (Exception e){
            return null;
        }
    }

    private static Object retrieveBoolean(CallableStatement statement, Integer position){
        try {
            return statement.getBoolean(position);
        }catch (Exception e){
            return null;
        }
    }

    private static Object retrieveByte(CallableStatement statement, Integer position){
        try {
            return statement.getByte(position);
        }catch (Exception e){
            return null;
        }
    }

    private static Object retrieveShort(CallableStatement statement, Integer position){
        try {
            return statement.getShort(position);
        }catch (Exception e){
            return null;
        }
    }

    private static Object retrieveLong(CallableStatement statement, Integer position){
        try {
            return statement.getLong(position);
        }catch (Exception e){
            return null;
        }
    }

    private static Object retrieveInt(CallableStatement statement, Integer position){
        try {
            return statement.getInt(position);
        }catch (Exception e){
            return null;
        }
    }

    private static Object retrieveDouble(CallableStatement statement, Integer position){
        try {
            return statement.getDouble(position);
        }catch (Exception e){
            return null;
        }
    }

    private static Object retrieveFloat(CallableStatement statement, Integer position){
        try {
            return statement.getFloat(position);
        }catch (Exception e){
            return null;
        }
    }

    private static Object retrieveBigDecimal(CallableStatement statement, Integer position){
        try {
            return statement.getBigDecimal(position);
        }catch (Exception e){
            return null;
        }
    }

    private static Object retrieveString(CallableStatement statement, Integer position){
        try {
            return statement.getString(position);
        }catch (Exception e){
            return null;
        }
    }

    private static Object retrieveTimestamp(CallableStatement statement, Integer position){
        try {
            return statement.getTimestamp(position);
        }catch (Exception e){
            return null;
        }
    }

    private static Object retrieveBytes(CallableStatement statement, Integer position){
        try {
            return statement.getBytes(position);
        }catch (Exception e){
            return null;
        }
    }

    private static Object retrieveURL(CallableStatement statement, Integer position){
        try {
            return statement.getURL(position);
        }catch (Exception e){
            return null;
        }
    }

    static {
        setters.put(LocalDateTime.class, (statement, position, argument) -> statement.setTimestamp(position, Timestamp.valueOf((LocalDateTime) argument)));
        setters.put(LocalDate.class, (statement, position, argument) -> statement.setDate(position, Date.valueOf((LocalDate) argument)));
        setters.put(java.util.Date.class, defaultSetter);
        setters.put(boolean.class, (statement, position, argument) -> statement.setBoolean(position, (boolean) argument));
        setters.put(int.class, (statement, position, argument) -> statement.setInt(position, (int) argument));
        setters.put(byte.class, (statement, position, argument) -> statement.setByte(position, (byte) argument));
        setters.put(short.class, (statement, position, argument) -> statement.setShort(position, (short) argument));
        setters.put(double.class, (statement, position, argument) -> statement.setDouble(position, (double) argument));
        setters.put(float.class, (statement, position, argument) -> statement.setFloat(position, (float) argument));
        setters.put(long.class, (statement, position, argument) -> statement.setLong(position, (long) argument));
        setters.put(BigDecimal.class, (statement, position, argument) -> statement.setBigDecimal(position, (BigDecimal) argument));
        setters.put(String.class, (statement, position, argument) -> statement.setString(position, (String) argument));
        setters.put(URL.class, (statement, position, argument) -> statement.setURL(position, (URL) argument));
        setters.put(InputStream.class, (statement, position, argument) -> statement.setBinaryStream(position, (InputStream) argument));
        setters.put(byte[].class, (statement, position, argument) -> statement.setBytes(position, (byte[]) argument));

        types.put(short.class, Types.SMALLINT);
        types.put(boolean.class, Types.SMALLINT);
        types.put(int.class, Types.INTEGER);
        types.put(long.class, Types.BIGINT);
        types.put(float.class, Types.REAL);
        types.put(double.class, Types.DOUBLE);
        types.put(BigDecimal.class, Types.DECIMAL);
        types.put(String.class, Types.VARCHAR);
        types.put(Timestamp.class, Types.TIMESTAMP);
        types.put(LocalDateTime.class, Types.TIMESTAMP);
        types.put(java.util.Date.class, Types.DATE);
        types.put(LocalDate.class, Types.DATE);

        getters.put(LocalDateTime.class, SQLHelper::retrieveLocalDateTime);
        getters.put(LocalDate.class, SQLHelper::retrieveLocalDate);
        getters.put(java.util.Date.class, SQLHelper::retrieveDate);
        getters.put(boolean.class, SQLHelper::retrieveBoolean);
        getters.put(int.class, SQLHelper::retrieveInt);
        getters.put(byte.class, SQLHelper::retrieveByte);
        getters.put(short.class, SQLHelper::retrieveShort);
        getters.put(double.class, SQLHelper::retrieveDouble);
        getters.put(float.class, SQLHelper::retrieveFloat);
        getters.put(long.class, SQLHelper::retrieveLong);
        getters.put(BigDecimal.class, SQLHelper::retrieveBigDecimal);
        getters.put(String.class, SQLHelper::retrieveString);
        getters.put(URL.class, SQLHelper::retrieveURL);
        getters.put(byte[].class, SQLHelper::retrieveBytes);

    }

    /**
     * Extract rows of a ResultSet to solid Java Class instances.
     * @param resultSet ResultSet that could be mapped to Java class instances.
     * @param converter Converter of the ResultSet row to solid class instance.
     * @param <T>   Type of the Java Class instances.
     * @return  A list of the solid Java Class instances.
     */
    public static <T> List<T> fromResultSet(ResultSet resultSet, Function<ResultSet, T> converter){
        if(resultSet == null)
            return null;
        List<T> list = new ArrayList<T>();
        try {
            while(resultSet.next()){
                T t = converter.apply(resultSet);
                list.add(t);
            }
        } catch (SQLException e){
            Logger.W(e.getMessage());
        }
        return list;
    }

    /**
     * Set a single argument to a specific position of the PreparedStatement with the right setXXX().
     * @param ps        PreparedStatement to be handled.
     * @param position  The position of the argument to be set.
     * @param argument  The actual argument to be consumed by the PreparedStatement.
     * @throws SQLException
     */
    private static void setArgument(PreparedStatement ps, int position, Object argument) throws Exception {

        Class clazz = argument.getClass();
        if(argument == null || !setters.containsKey(clazz)){
            defaultSetter.accept(ps, position, argument);
        } else {
            setters.get(clazz).accept(ps, position, argument);
        }
    }

    /**
     * Fill the PreparedStatement with right parameter types and positions.
     * @param ps        PreparedStatement to be handled.
     * @param args      Arguments used to run the PreparedStatement.
     * @throws SQLException SQLException during the process.
     */
    public static void setArguments(PreparedStatement ps, Object... args)
            throws Exception {
        AtomicInteger atomicInteger = new AtomicInteger();
        for (Object argument : args) {
           setArgument(ps, atomicInteger.incrementAndGet(), argument);
        }
    }

    /**
     * Fill the CallableStatement with right parameter types and positions.
     * @param statement     CallableStatement to be processed.
     * @param args          Actual arguments to be used by calling the CallableStatement.
     * @throws SQLException
     */
    public static void setCallableArguments(CallableStatement statement, Object... args)
            throws Exception {
        AtomicInteger atomicInteger = new AtomicInteger();
        for (Object argument : args) {
            if (argument instanceof Out){
                registerArgument(statement, atomicInteger.incrementAndGet(), (Out)argument);
            }else {
                setArgument(statement, atomicInteger.incrementAndGet(), argument);
            }
        }
    }

    private static void registerArgument(CallableStatement statement, int position, Out out) throws SQLException {
        Object argument = out.value;
        Class clazz = argument.getClass();
        int sqlType = (types.containsKey(clazz)) ? types.get(clazz) : JavaObjectType;
        statement.registerOutParameter(position, sqlType);
    }

    private static Object retrieveOutArgument(CallableStatement statement, int position, Out out) throws SQLException {
        BiFunction<CallableStatement, Integer, Object> retriever = getters.containsKey(out.valueClass)
                ? getters.get(out.valueClass) : SQLHelper:: retrieveObject;

        return retriever.apply(statement, position);
    }

    /**
     * Run the given SQL script as a PreparedStatement, and convert the ResultSet to a list of Java instances.
     * @param converter Converter of the ResetSet rows to Java class instances.
     * @param querySQL  SQL resource filename if ended with '.sql', or SQL template with placeholders ('?") for the arguments.
     * @param args      Actual arguments to be set to the PreparedStatement.
     * @param <T>       Type of the Java class to be converted to.
     * @return          A list of Java class instances converted from running the PreparedStatement.
     */
    public <T> List<T> queryToObjects(Supplier<Connection> connectionSupplier, Function<ResultSet, T> converter, String querySQL, Object... args){
        return query(connectionSupplier, rs -> fromResultSet(rs, converter), querySQL, args);
    }

    /**
     * Run the given SQL script as a PreparedStatement, then apply the transformer method to convert the ResultSet to any object.
     * @param transformer   Method to be applied to convert the ResultSet to any object.
     * @param querySQL  SQL resource filename if ended with '.sql', or SQL template with placeholders ('?") for the arguments.
     * @param args      Actual arguments to be set to the PreparedStatement.
     * @param <T>       Type of the Java class to be converted to.
     * @return          The converted instance from the ResultSet if success, or Null if failed.
     */
    public <T> T query(Supplier<Connection> connectionSupplier, Function<ResultSet, T> transformer, String querySQL, Object... args){
        String script = tryGetAsResource(querySQL);
        try(Connection connection = connectionSupplier.get();
            PreparedStatement stmt = connection.prepareStatement(script)){
            setArguments(stmt, args);
            report(script, args);
            try (ResultSet resultSet = stmt.executeQuery()){
                return transformer.apply(resultSet);
            }
        } catch (Exception sqlException){
            Logger.W(sqlException.getMessage());
            return null;
        }
    }

    public String pseudoSQL(String script, Object... args){
        String[] segments = script.split("[?]");
        StringBuilder sb = new StringBuilder();
        sb.append(segments[0]);
        try {
            for (int i = 0; i < args.length; i++) {
                sb.append(args[i].toString());
                sb.append(segments[1+i]);
            }
        }catch (Exception ex){

        }finally {
            return sb.toString();
        }
    }

//    public <T> T query(String tableViewName, Function<ResultSet, T> transformer, LocalDate startDate, LocalDate endDate){
//        String sql = "Select * from " + tableViewName + " where daytime between ? and ?";
//        return query(transformer, sql, startDate, endDate);
//    }

    /**
     * ExecuteUpdate the given SQL script as a PreparedStatement.
     * @param updateSQL  SQL resource filename if ended with '.sql', or SQL template with placeholders ('?") for the arguments.
     * @param args      Actual arguments to be set to the PreparedStatement.
     * @return          (1) the row count for SQL Data Manipulation Language (DML) statements
     *                  or (2) 0 for SQL statements that return nothing
     *                  or (3) -1 if there is some SQLException thrown.
     */
    public int update(Supplier<Connection> connectionSupplier, String updateSQL, Object... args){
        String script = tryGetAsResource(updateSQL);
        try(Connection connection = connectionSupplier.get();
            PreparedStatement stmt = connection.prepareStatement(script)){
            setArguments(stmt, args);
            report(script, args);

            return stmt.executeUpdate();
        } catch (Exception sqlException){
            Logger.W(sqlException.getMessage());
            return -1;
        }
    }

    public void report(String script, Object... args){
        Logger.D("Run SQL script like: \n" + pseudoSQL(script, args));
    }


    public static String tryGetAsResource(String scriptOrFilename){
        try {
            String script = ResourceHelper.getTextFromResourceFile(Paths.get("appPackages", scriptOrFilename).toString());
            return script == null ? scriptOrFilename : script;
        }catch (Exception ex){
            return scriptOrFilename;
        }
    }

    /**
     * Run the given SQL script as a CallableStatement.
     * @param procSignature SQL template with placeholders ('?") for the arguments.
     * @param args          Actual arguments to be set to the CallableStatement.
     * @return              Rows updated by the CallableStatement.
     */
    public int call(Supplier<Connection> connectionSupplier, String procSignature, Object... args){
        String script = tryGetAsResource(procSignature);

        int count = 0;
        if (!script.contains("{call")) {
            script = String.format("{call %s}", script);
        }
        try(Connection connection = connectionSupplier.get();
            CallableStatement stmt = connection.prepareCall(script)){

            setCallableArguments(stmt, args);
            count = stmt.executeUpdate();
            report(script, args);

            for (int i = 0; i < args.length; i ++) {
                Object argument = args[i];
                if(argument instanceof Out){
                    ((Out) argument).value = retrieveOutArgument(stmt, i+1, (Out) argument);
                }
            }
        } catch (Exception sqlException){
            Logger.W(sqlException.getMessage());
        }
        return count;
    }

    /**
     * Wapper class to indicate the arguments as OUT.
     */
    public static class Out {

        public Object value;
        public final Class valueClass;
        public Out(Object o){
            valueClass = o.getClass();
            value = o;
        }
    }
}
