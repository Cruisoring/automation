package io.github.Cruisoring.helpers;

import io.github.cruisoring.throwables.FunctionThrowable;
import io.github.cruisoring.throwables.RunnableThrowable;
import io.github.cruisoring.throwables.SupplierThrowable;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.InvalidArgumentException;

import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * SQL Statement Helper to facilitate invoking of either CallableStatement or PreparedStatement from buffered DB helpers.
 */
public class SQLHelper {
    public final static String PercentageAscii = "&#37";

    public final static String SQL_STATEMENT_PACKAGE = "sqlStatements";

    @FunctionalInterface
    public interface SetStatementValue {
        void set(PreparedStatement statement, Integer index, Object value) throws Exception;

        default RunnableThrowable asRunnable(PreparedStatement statement, Integer index, Object value){
            return () -> set(statement, index, value);
        }

        default void execute(PreparedStatement statement, Integer index, Object value) {
            asRunnable(statement, index, value).tryRun();
        }
    }

    @FunctionalInterface
    public interface GetStatementValue {
        Object get(ResultSet resultSet, Integer index) throws Exception;

        default SupplierThrowable asRunnable(ResultSet resultSet, Integer index){
            return () -> get(resultSet, index);
        }

        default Object execute(ResultSet resultSet, Integer index) {
            return asRunnable(resultSet, index).tryGet();
        }
    }

    /**
     * Assume the given scriptOrFilename as SQL filename first, before treating it as raw SQL script, to get the final
     * SQL statement to build a preparedStatement.
     * @param scriptOrFilename  Script or filename of the script stored in the \src\resources\sqlStatements folder.
     *                          When used as the filename, it is CASE-SENSITIVE.
     * @return  SQL statement with placeholders of '?' for arguments.
     */
    public static String tryGetAsResource(String scriptOrFilename){
        try {
            String script = ResourceHelper.getTextFromResourceFile(Paths.get(SQL_STATEMENT_PACKAGE, scriptOrFilename).toString());
            return script == null ? scriptOrFilename : script;
        }catch (Exception ex){
            return scriptOrFilename;
        }
    }

    /**
     * Operators of setting specific types of arguments with right Oracle PreparedStatement methods.
     * You can add new operators, or update/remove existing ones to change the behavior of
     * Object setArgument(PreparedStatement ps, int position, Object argument).
     */
    public static final Map<Class<?>, SetStatementValue> argSetters = new HashMap<Class<?>, SetStatementValue>(){{
        put(LocalDateTime.class, ((statement, index, value) -> statement.setTimestamp(index, Timestamp.valueOf((LocalDateTime)value))));
        put(LocalDate.class, ((statement, index, value) -> statement.setDate(index, java.sql.Date.valueOf((LocalDate)value))));
        put(LocalTime.class, ((statement, index, value) -> statement.setTime(index, Time.valueOf((LocalTime)value))));
        put(java.util.Date.class, ((statement, index, value) -> statement.setDate(index, new java.sql.Date((((java.util.Date) value)).getTime()))));
        put(java.sql.Time.class, ((statement, index, value) -> statement.setTime(index, (java.sql.Time)value)));
//        put(Instant.class, ((statement, index, value) -> statement.setTime(index, (java.sql.Time)value))); //To be investigated
        put(String.class, ((statement, index, value) -> statement.setString(index, (String)value)));
        put(URL.class, ((statement, index, value) -> statement.setURL(index, (URL)value)));
        put(InputStream.class, ((statement, index, value) -> statement.setBlob(index, (InputStream) value)));

        put(Boolean.class, ((statement, index, value) -> statement.setBoolean(index, (Boolean) value)));
        put(boolean.class, ((statement, index, value) -> statement.setBoolean(index, (Boolean) value)));
        put(Byte.class, ((statement, index, value) -> statement.setByte(index, (Byte)value)));
        put(byte.class, ((statement, index, value) -> statement.setByte(index, (Byte)value)));
        put(Short.class, ((statement, index, value) -> statement.setShort(index, (Short)value)));
        put(short.class, ((statement, index, value) -> statement.setShort(index, (Short)value)));
        put(Integer.class, ((statement, index, value) -> statement.setInt(index, (Integer)value)));
        put(int.class, ((statement, index, value) -> statement.setInt(index, (Integer)value)));
        put(Long.class, ((statement, index, value) -> statement.setLong(index, (Long)value)));
        put(long.class, ((statement, index, value) -> statement.setLong(index, (Long)value)));
        put(Float.class, ((statement, index, value) -> statement.setFloat(index, (Float) value)));
        put(float.class, ((statement, index, value) -> statement.setFloat(index, (Float) value)));
        put(Double.class, ((statement, index, value) -> statement.setDouble(index, (Double) value)));
        put(double.class, ((statement, index, value) -> statement.setDouble(index, (Double) value)));

        put(Byte[].class, ((statement, index, value) -> statement.setBytes(index, (byte[])value)));
        put(byte[].class, ((statement, index, value) -> statement.setBytes(index, (byte[])value)));

        put(StringBuffer.class, ((statement, index, value) -> statement.setCharacterStream(index, new StringReader(value.toString()))));
    }
    };

    /**
     * Mapping common Java types to Oracle SQL Types.
     */
    public static final Map<Class<?>, Integer> Java2SQLTypes = new HashMap<Class<?>, Integer>(){{
        put(LocalDateTime.class, Types.TIMESTAMP);
        put(LocalDate.class, Types.DATE);
        put(java.util.Date.class, Types.DATE);
        put(java.sql.Time.class, Types.TIME);
        put(String.class, Types.VARCHAR);
        put(InputStream.class, Types.BLOB);

        put(Boolean.class, Types.BOOLEAN);
        put(boolean.class, Types.BOOLEAN);
        put(Byte.class, Types.SMALLINT);
        put(byte.class, Types.SMALLINT);
        put(Short.class, Types.SMALLINT);
        put(short.class, Types.SMALLINT);
        put(Integer.class, Types.INTEGER);
        put(int.class, Types.INTEGER);
        put(Long.class, Types.BIGINT);
        put(long.class, Types.BIGINT);
        put(Float.class, Types.FLOAT);
        put(float.class, Types.FLOAT);
        put(Double.class, Types.DOUBLE);
        put(double.class, Types.DOUBLE);

        put(Byte[].class, Types.VARBINARY);
        put(byte[].class, Types.VARBINARY);

        put(Object.class, Types.JAVA_OBJECT);
    }
    };

    /**
     * Mapping common Java types to Oracle SQL Types.
     */
    public static final Map<Integer, GetStatementValue> SQL2JavaTypes = new HashMap<Integer, GetStatementValue>(){{
        put(Types.TIMESTAMP, (resultSet, index) -> {
            Timestamp timestamp = resultSet.getTimestamp(index);
            return timestamp == null ? null : new java.util.Date(timestamp.getTime());
        });
        put(Types.DATE, (resultSet, index) -> resultSet.getDate(index));
        put(Types.TIME, (resultSet, index) -> {
            Time time = resultSet.getTime(index);
            return time == null ? null : time.toLocalTime();
        });
        put(Types.TIMESTAMP, (resultSet, index) -> {
            Timestamp timestamp = resultSet.getTimestamp(index);
            return timestamp == null ? null : new java.util.Date(timestamp.getTime());
        });
        put(Types.VARCHAR, (resultSet, index) -> resultSet.getString(index));
        put(Types.CHAR, (resultSet, index) -> resultSet.getString(index));
        put(Types.LONGNVARCHAR, (resultSet, index) -> resultSet.getString(index));

        put(Types.BOOLEAN, SQLHelper::getNullOrBoolean);
        put(Types.SMALLINT, SQLHelper::getNullOrShort);
        put(Types.INTEGER, SQLHelper::getNullOrInteger);
        put(Types.BIGINT, SQLHelper::getNullOrLong);
        put(Types.FLOAT, SQLHelper::getNullOrFloat);
        put(Types.DOUBLE, SQLHelper::getNullOrFloat);
        put(Types.NUMERIC, SQLHelper::getNullOrFloat);

        put(Types.VARBINARY, (resultSet, index) -> resultSet.getBytes(index));
    }
    };

    private static Boolean getNullOrBoolean(ResultSet resultSet, int index) throws SQLException {
        Boolean value = resultSet.getBoolean(index);
        return resultSet.wasNull() ? null : value;
    }

    private static Float getNullOrFloat(ResultSet resultSet, int index) throws SQLException {
        Float value = resultSet.getFloat(index);
        return resultSet.wasNull() ? null : value;
    }

    private static Short getNullOrShort(ResultSet resultSet, int index) throws SQLException {
        Short value = resultSet.getShort(index);
        return resultSet.wasNull() ? null : value;
    }

    private static Integer getNullOrInteger(ResultSet resultSet, int index) throws SQLException {
        Integer value = resultSet.getInt(index);
        return resultSet.wasNull() ? null : value;
    }

    private static Long getNullOrLong(ResultSet resultSet, int index) throws SQLException {
        Long value = resultSet.getLong(index);
        return resultSet.wasNull() ? null : value;
    }

    public static final Map<String, FunctionThrowable<ResultSet, Map<String, Object>>> rowToMapExtractors = new HashMap<>();
    public static final Map<String, FunctionThrowable<ResultSet, Object[]>> rowToArrayExtractors = new HashMap<>();

    //Named SQLHelper buffer.
    public static final Map<String, SQLHelper> dbHelpers = new HashMap<>();

    /**
     * Given the name of an environment, retrieve the existing instance if there is, or create/cache/return a new one automatically.
     * @param environmentName   The name of the environment like "AGEA", "SIT", or "UAT" that are managed by the Environment.
     * @return The SQLHelper build with the corresponding SQL URL/Username/Password for the given environment.
     */
    public static SQLHelper of(String environmentName){
        SQLHelper helper = MapHelper.getIgnoreCase(dbHelpers, environmentName);
        if (helper == null) {
            String dbUrl = "";
            String dbUsername = "";
            helper = new SQLHelper(environmentName, dbUrl, dbUsername, "");
            dbHelpers.put(environmentName, helper);
        }
        return helper;
    }

    /**
     * Extract rows of a ResultSet to solid Java Class instances.
     * @param resultSet ResultSet that could be mapped to Java class instances.
     * @param converter Converter of the ResultSet row to solid class instance.
     * @param <T>   Type of the Java Class instances.
     * @return  A list of the solid Java Class instances.
     */
    public static <T> List<T> fromResultSet(ResultSet resultSet, FunctionThrowable<ResultSet, T> converter){
        if(resultSet == null)
            return null;
        List<T> list = new ArrayList<T>();
        try {
            while(resultSet.next()){
                T t = converter.tryApply(resultSet);
                list.add(t);
            }
        } catch (SQLException e){
            Logger.E(e);
        }
        return list;
    }

    private static String[] asPseudoArguments(Object... args){
        Objects.requireNonNull(args);

        int argsLength = args.length;
        if (argsLength == 0) {
            return new String[0];
        }

        String[] result = new String[argsLength];
        for (int i = 0; i < argsLength; i++) {
            Object arg = args[i];
            if(arg == null)
                result[i] = "null";
            else if(arg instanceof String)
                result[i] = String.format("'%s'", arg);
            else if (arg instanceof LocalDate)
                result[i] = String.format("to_date('%s', 'YYYY-MM-DD')", DateTimeHelper.dateString((LocalDate)arg));
            else if (arg instanceof java.util.Date)
                result[i] = String.format("to_date('%s', 'YYYY-MM-DD')", DateTimeHelper.dateString((java.util.Date)arg));
            else
                result[i] = arg.toString();
        }
        return result;
    }

    /***
     * Get the pseudo SQL to be executed with arguments for logging purposes.
     * @param script    SQL template to build PreparedStatement.
     * @param args      Arguments to be set into the PreparedStatement.
     * @return      Psuedo SQL to show how SQL looks like when it is executed.
     */
    public static String pseudoSQL(String script, Object... args){
        String[] segments = script.split("[?]");
        StringBuilder sb = new StringBuilder();
        sb.append(segments[0]);

        try {
            String[] pseudoArguments = asPseudoArguments(args);
            for (int i = 0; i < args.length; i++) {
                sb.append(pseudoArguments[i]);
                sb.append(segments[1+i]);
            }
            sb.append("\n");
        }catch (Exception ex){

        }finally {
            return sb.toString();
        }
    }

    /**
     * Fill the PreparedStatement with right parameter types and positions.
     * @param ps        PreparedStatement to be handled.
     * @param args      Arguments used to run the PreparedStatement.
     * @throws SQLException SQLException during the process.
     */
    public static Object[] setArguments(PreparedStatement ps, Object... args)
            throws SQLException {
        AtomicInteger atomicInteger = new AtomicInteger();
        int argsLength = args.length;
        Object[] actualSet = new Object[argsLength];
        for (int i =0; i<argsLength; i++) {
            Object argument = args[i];
            if(argument == null) {
                ps.setObject(atomicInteger.incrementAndGet(), argument);
                actualSet[i] = null;
            }else {
                actualSet[i] = setArgument(ps, atomicInteger.incrementAndGet(), argument);
            }
        }
        return actualSet;
    }

    /**
     * Set a single argument that cannot be Null to a specific position of the PreparedStatement with the right setXXX().
     * @param ps        PreparedStatement to be handled.
     * @param position  The position of the argument to be set.
     * @param argument  The actual argument to be consumed by the PreparedStatement.
     * @throws SQLException
     */
    private static Object setArgument(PreparedStatement ps, int position, Object argument) throws SQLException {
        Objects.requireNonNull(argument);
        Class<?> clazz = argument.getClass();
        if(argSetters.containsKey(clazz)){
            argSetters.get(clazz).execute(ps, position, argument);
        } else {
            ps.setObject(position, argument);
        }
        return argument;
    }

    /**
     * Fill the CallableStatement with right parameter types and positions.
     * @param statement     CallableStatement to be processed.
     * @param args          Actual arguments to be used by calling the CallableStatement.
     * @throws SQLException
     */
    public static void setCallableArguments(CallableStatement statement, Object... args)
            throws SQLException {
        AtomicInteger atomicInteger = new AtomicInteger();
        for (Object argument : args) {
            if(argument == null) {
                statement.setObject(atomicInteger.incrementAndGet(), argument);
            }else if (argument instanceof Out){
                registerArgument(statement, atomicInteger.incrementAndGet(), (Out)argument);
            }else {
                setArgument(statement, atomicInteger.incrementAndGet(), argument);
            }
        }
    }

    public static Object[] flatScriptArguments(String scriptTemplate, Object... args){
        StringBuilder sb = new StringBuilder();
        int before = scriptTemplate.indexOf("?");
        sb.append(scriptTemplate.substring(0, before == -1 ? scriptTemplate.length() : before));
        List<Object> flattedArgs = new ArrayList<>();
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            int next = scriptTemplate.indexOf("?", before+1);
            if(arg == null || argSetters.containsKey(arg.getClass()) || !arg.getClass().isArray()){
                flattedArgs.add(arg);
                sb.append(scriptTemplate.substring(before, next==-1 ? scriptTemplate.length(): next));
                before = next;
                continue;
            }
            int argLength = java.lang.reflect.Array.getLength(arg);
            for (int j = 0; j < argLength; j++) {
                flattedArgs.add(java.lang.reflect.Array.get(arg, j));
            }
            sb.append(StringUtils.repeat("?,", argLength-1));
            sb.append(scriptTemplate.substring(before, next==-1 ? scriptTemplate.length(): next));
            before = next;
        }
        Object[] result = new Object[]{sb.toString(), flattedArgs.toArray()};
        return result;
    }

    public static FunctionThrowable<ResultSet, Object[]> getRowValueArrayExtractor(ResultSet resultSet){
        try {
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            //Get SQL Data types of all columns
            final List<FunctionThrowable<ResultSet, Object>> rowFunctions = new ArrayList<>();
            final List<String> columnNames = new ArrayList<>();
            for (int i=0; i<columnCount; i++){
                final Integer index = i+1;
                int sqlType = metaData.getColumnType(index);
                columnNames.add(metaData.getColumnName(index));
                if(SQL2JavaTypes.containsKey(sqlType)){
                    FunctionThrowable<ResultSet, Object> columnValueExtractor = rSet ->
                            SQL2JavaTypes.get(sqlType).get(rSet, index);
                    rowFunctions.add(columnValueExtractor);
                } else {
                    throw new RuntimeException("Unsupported SQL type: " + i);
                }
            }
            FunctionThrowable<ResultSet,Object[]> row2ArrayExtractor = (rSet) -> {
                if(!rSet.next())
                    return null;    //Returns Null if there is no row returned
                Object[] values = new Object[columnCount];
                for (int i = 0; i < columnCount; i++) {
                    values[i] = rowFunctions.get(i).apply(rSet);
                }
                return values;
            };
            return row2ArrayExtractor;
        } catch (Exception ex){
            Logger.E(ex);
            throw new RuntimeException(ex);
        }
    }

    private static void registerArgument(CallableStatement statement, int position, Out out) throws SQLException {
        Object argument = out.value;
        if(out.clazz == ResultSet.class){
//            statement.registerOutParameter(position, OracleTypes.CURSOR);
        }else if (argument instanceof LocalDateTime) {
            statement.registerOutParameter(position, Types.TIMESTAMP);
        }else if (argument instanceof LocalDate) {
            statement.registerOutParameter(position, Types.DATE);
        }else if (argument instanceof String) {
            statement.registerOutParameter(position, Types.VARCHAR);
        } else if (argument instanceof Integer) {
            statement.registerOutParameter(position, Types.INTEGER);
        } else if (argument instanceof Long) {
            statement.registerOutParameter(position, Types.BIGINT);
        } else if (argument instanceof Double) {
            statement.registerOutParameter(position, Types.DOUBLE);
        } else if (argument instanceof Float) {
            statement.registerOutParameter(position, Types.FLOAT);
        } else {
            statement.registerOutParameter(position, Types.JAVA_OBJECT);
        }
    }

    private static Object retrieveOutArgument(CallableStatement statement, int position, Out out) throws SQLException {
        Object argument = out.value;
        if (argument instanceof LocalDateTime) {
            argument = statement.getTimestamp(position).toLocalDateTime();
        }else if (argument instanceof LocalDate) {
            argument = statement.getDate(position).toLocalDate();
        }else if (argument instanceof String) {
            argument = statement.getString(position);
        } else if (argument instanceof Integer) {
            argument = statement.getInt(position);
        } else if (argument instanceof Long) {
            argument = statement.getLong(position);
        } else if (argument instanceof Double) {
            argument = statement.getDouble(position);
        } else if (argument instanceof Float) {
            argument = statement.getFloat(position);
        } else {
            argument = statement.getObject(position);
        }
        return argument;
    }

    public final String name;
    public final Properties info;
    public final String url;

    /**
     * Constructor to be called by the static constructor when environment is used to deduct url/username/password.
     * @param url       URL to the target DB.
     * @param user      Username to log into the target DB.
     * @param password  Password to log into the target DB.
     */
    private SQLHelper(String name, String url, String user, String password) {
        this.name = name;
        this.url = url;
        this.info = new Properties();
        info.setProperty("user", user);
        info.setProperty("password", password);
        info.setProperty("MaxPooledStatements", "100");
    }

    @Override
    public String toString(){
        return String.format("%s (%s)", name, url);
    }

    /**
     * Get connection.
     * @return The DB connection.
     */
    public Connection getConnection(){
        try {
            return DriverManager.getConnection(url,info);
        } catch (SQLException e) {
            Logger.E(e);
            return null;
        }
    }

    /**
     * Log the executed SQL statement.
     * @param script    Template of the SQL script to be run as PreparedStatement
     * @param args      Arguments to be filled into the PreparedStatement
     */
    public void logPseudoSQL(String script, Object... args){

        String[] argStrings = asPseudoArguments(args);
        String scriptFormat = name + ": " + script.replaceAll("[%]", PercentageAscii).replaceAll("[?]", "%s");

        ReportHelper.log(scriptFormat, argStrings);
    }

    /**
     * Run the given SQL script as a PreparedStatement, and convert the ResultSet to a list of Java instances.
     * @param converter Converter of the ResetSet rows to Java class instances.
     * @param querySQL  SQL resource filename if ended with '.sql', or SQL template with placeholders ('?") for the arguments.
     * @param args      Actual arguments to be set to the PreparedStatement.
     * @param <T>       Type of the Java class to be converted to.
     * @return          A list of Java class instances converted from running the PreparedStatement.
     */
    public <T> List<T> queryToObjects(FunctionThrowable<ResultSet, T> converter, String querySQL, Object... args){
        return query(rs -> fromResultSet(rs, converter), querySQL, args);
    }

    /**
     * Run the given SQL script as a PreparedStatement, then apply the transformer method to convert the ResultSet to any object.
     * @param transformer   Method to be applied to convert the ResultSet to any object.
     * @param querySQL  SQL resource filename if ended with '.sql', or SQL template with placeholders ('?") for the arguments.
     * @param args      Actual arguments to be set to the PreparedStatement.
     * @param <T>       Type of the Java class to be converted to.
     * @return          The converted instance from the ResultSet if success, or Null if failed.
     */
    public <T> T query(Function<ResultSet, T> transformer, String querySQL, Object... args){
        String script = tryGetAsResource(querySQL);
        Object[] flatted = flatScriptArguments(script, args);
        script = (String) flatted[0];
        args = (Object[]) flatted[1];
        try(Connection connection = getConnection();
            PreparedStatement stmt = connection.prepareStatement(script)){
            Object[] actualArgs = setArguments(stmt, args);
            logPseudoSQL(script, actualArgs);
            try (ResultSet resultSet = stmt.executeQuery()){
                return transformer.apply(resultSet);
            }
        } catch (SQLException sqlException){
            ReportHelper.reportAsStepLog(sqlException.getMessage());
            return null;
        }
    }

    /**
     * Run the given SQL script as a PreparedStatement, and convert the ResultSet as a list of HashMap corresponding every rows.
     * @param querySQL  SQL resource filename if ended with '.sql', or SQL template with placeholders ('?") for the arguments.
     * @param args      Actual arguments to be set to the PreparedStatement.
     * @return          The list of maps, each of the maps use the column names as keys to corresponding values of the row.
     */
    public List<Object[]> query(String querySQL, Object... args) {
        String script = tryGetAsResource(querySQL);
        Object[] flatted = flatScriptArguments(script, args);
        script = (String) flatted[0];
        args = (Object[]) flatted[1];
        try(Connection connection = getConnection();
            PreparedStatement stmt = connection.prepareStatement(script)){
            Object[] actualArgs = setArguments(stmt, args);
            logPseudoSQL(script, actualArgs);
            try (ResultSet resultSet = stmt.executeQuery()){
                ResultSetIterable resultSetIterable =  new ResultSetIterable(resultSet);
                return resultSetIterable == null ? null : resultSetIterable.asArrayList();
            }
        } catch (Exception ex){
            ReportHelper.reportAsStepLog(ex.getMessage());
            return null;
        }
    }

    PreparedStatement lastQueryStatement = null;
    /**
     * Used the PreparedState to execute query and return the first row as an array.
     * @param stmt      The PreparedStatement instance that shall be used multiple times.
     * @param script    SQL resource filename if ended with '.sql', or SQL template with placeholders ('?") for the arguments.
     * @param args      Actual arguments to be set to the PreparedStatement.
     * @return          Values of the first row returned as an Object array.
     */
    public Object[] queryRowToArray(PreparedStatement stmt, String script, Object... args){
        try {
            Object[] actualArgs = setArguments(stmt, args);
            if(stmt != lastQueryStatement) {
                lastQueryStatement = stmt;
                logPseudoSQL(script, actualArgs);
            }
            ResultSet resultSet = stmt.executeQuery();
            if (!rowToArrayExtractors.containsKey(script)) {
                rowToArrayExtractors.put(script, getRowValueArrayExtractor(resultSet));
            }
            Object[] values = rowToArrayExtractors.get(script).tryApply(resultSet);
            if (values == null) {
                Logger.I("Failed to find row with keys: %s", ReconcileHelper.getArrayDescription(actualArgs));
            } else if (resultSet.next()) {
                throw new InvalidArgumentException("'" + script + "' returns more than one row, are all Keys set properly?");
            }
            return values;
        }catch (Exception ex){
            return null;
        }
    }

    /**
     * Execute query and return rows of returned.
     * @param querySQL
     * @param args
     * @return
     */
    public int getRowNum(String querySQL, Object... args){
        String script = tryGetAsResource(querySQL);
        Object[] flatted = flatScriptArguments(script, args);
        script = (String) flatted[0];
        args = (Object[]) flatted[1];
        try(Connection connection = getConnection();
            PreparedStatement stmt = connection.prepareStatement(script)){
            Object[] actualArgs = setArguments(stmt, args);
            logPseudoSQL(script, actualArgs);
            try (ResultSet resultSet = stmt.executeQuery()){
                ResultSetIterable resultSetIterable = new ResultSetIterable(resultSet);
                return resultSetIterable == null ? -1 : resultSetIterable.getRowCount();
            }
        } catch (Exception ex){
            ReportHelper.reportAsStepLog(ex.getMessage());
            return -1;
        }
    }

    /**
     * ExecuteUpdate the given SQL script as a PreparedStatement, without logging the pseudo SQL.
     * @param updateSQL  SQL resource filename if ended with '.sql', or SQL template with placeholders ('?") for the arguments.
     * @param args      Actual arguments to be set to the PreparedStatement.
     * @return          (1) the row count for SQL Data Manipulation Language (DML) statements
     *                  or (2) 0 for SQL statements that return nothing
     *                  or (3) -1 if there is some SQLException thrown.
     */
    public int update(String updateSQL, Object... args){
        String script = tryGetAsResource(updateSQL);
        Object[] flatted = flatScriptArguments(script, args);
        script = (String) flatted[0];
        args = (Object[]) flatted[1];
        try(Connection connection = getConnection();
            PreparedStatement stmt = connection.prepareStatement(script)){
            Object[] actualArgs = setArguments(stmt, args);
            logPseudoSQL(script, actualArgs);
            return stmt.executeUpdate();
        } catch (SQLException sqlException){
            Logger.W(sqlException);
            return -1;
        }
    }

    /**
     * Execute SQL query as PreparedStatement and save the result to a sheet of the specific Excel file.
     * @param xlsFilename   Excel file name.
     * @param sheetName     Sheet name.
     * @param querySQL      SQL template to run the query.
     * @param args          Arguments to be filled into the query.
     * @return              Absolute path of the saved Excel file.
     */
    public String queryToSpreadSheet(String xlsFilename, String sheetName, String querySQL, Object... args) throws Exception{
        List<Object[]> list = query(querySQL, args);
        Path outputFilePath = ResourceHelper.getResultFilePath(xlsFilename);
        String filename = ExcelSheetHelper.saveToExcelSheet(list, outputFilePath, sheetName);
        return filename;
    }


    /**
     * Run the given SQL script as a CallableStatement.
     * @param procSignature SQL template with placeholders ('?") for the arguments.
     * @param args          Actual arguments to be set to the CallableStatement.
     * @return              Rows updated by the CallableStatement.
     */
    public int call(String procSignature, Object... args){
        String script = tryGetAsResource(procSignature);

        int count = 0;
        if (!script.contains("{call")) {
            script = String.format("{call %s}", script);
        }
        try(Connection connection = getConnection();
            CallableStatement stmt = connection.prepareCall(script)){

            setCallableArguments(stmt, args);
            logPseudoSQL(script, args);
            count = stmt.executeUpdate();

            for (int i = 0; i < args.length; i ++) {
                Object argument = args[i];
                if(argument instanceof Out){
                    ((Out) argument).value = retrieveOutArgument(stmt, i+1, (Out) argument);
                }
            }
        } catch (SQLException sqlException){
            Logger.W(sqlException);
        }
        return count;
    }

    /**
     * Execute the given statement in batches.
     * @param statement     SQL template to be executed, or name to locate the SQL template.
     * @param dataSupplier  For each row number, return an array of parameters to be set to the SQL template.
     * @param startRow      first row number to be executed.
     * @param endRow        last row number to be executed.
     * @return              the result of the update/insert of each rows.
     */
    public int[] runBatch(String statement, Function<Integer, Object[]> dataSupplier, int startRow, int endRow){
        String script = tryGetAsResource(statement);

        int key = startRow;
        int[] result;
        try(Connection connection = getConnection();
            PreparedStatement stmt = connection.prepareStatement(script)){
            connection.setAutoCommit(false);

            while(key < endRow){
                Object[] args = dataSupplier.apply(key);
                if(args == null)
                    break;

                Object[] actualArgs = setArguments(stmt, args);
                if(key == startRow) {
                    ReportHelper.reportAsStepLog(pseudoSQL(script, actualArgs));
                }
                stmt.addBatch();
                key++;
            }
            result = stmt.executeBatch();
            connection.commit();
            return result;
        } catch (Exception ex){
            Logger.W(ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     * Execute the given statement in batches.
     * @param statement     SQL template to be executed, or name to locate the SQL template.
     * @param dataSupplier  For each row number, return an array of parameters to be set to the SQL template.
     * @param rowNumbers    distinct row numbers.
     * @return              the result of the update/insert of each rows.
     */
    public int[] runBatch(String statement, Function<Integer, Object[]> dataSupplier, int[] rowNumbers){
        String script = tryGetAsResource(statement);

        int count = rowNumbers.length;
        int[] result = new int[count];
        try(Connection connection = getConnection();
            PreparedStatement stmt = connection.prepareStatement(script)){
            connection.setAutoCommit(false);

            for (int i = 0; i < count; i++) {
                int rowNumber = rowNumbers[i];
                Object[] args = dataSupplier.apply(rowNumber);
                if(args == null)
                    break;

                Object[] actualArgs = setArguments(stmt, args);
                if(i == 0){
                    logPseudoSQL(script, actualArgs);
                }
                stmt.addBatch();
            }
            result = stmt.executeBatch();
            connection.commit();
            return result;
        } catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }

    /**
     * Wapper class to indicate the arguments as OUT.
     */
    public static class Out {
        public Object value;
        public Class<?> clazz;
        public Out(Object o){
            value = o;
        }

        public Out(Class<?> c){
            clazz = c;
        }
    }

    public List<String> getAllColumnStringValues(String columnName, String sql, Object... arguments){
        return queryToObjects(rs -> rs.getString(columnName), sql, arguments);
    }
}
