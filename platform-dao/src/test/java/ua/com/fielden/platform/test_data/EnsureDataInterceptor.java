package ua.com.fielden.platform.test_data;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.utils.DbUtils.batchExecSql;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.logging.log4j.Logger;

import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.test.DbCreator;
import ua.com.fielden.platform.test.exceptions.DomainDriventTestException;
import ua.com.fielden.platform.utils.StreamUtils;

/// An interceptor used to ensure correct data population based on `populate*` methods in `IDomainData` and their annotation [EnsureData],
/// which specifies the dependency between the methods.
///
/// In addition, it is also responsible for scripting the data populated by those methods.
/// The cached INSERT/UPDATE scripts provide a fast alternative for reusable data population.
///
public class EnsureDataInterceptor implements MethodInterceptor {
    private static final Logger LOGGER = getLogger(EnsureDataInterceptor.class);

    private static final String REGEX_FOR_CSV_LINE_WITH_SINGLE_QUOTES = ",(?=(?:[^']*'[^']*')*[^']*$)";

    private final Map<String, List<String>> scripts = new HashMap<>();

    private String runningTestCase = "";
    private final Set<String> executedScripts = new HashSet<>();

    private final boolean saveScriptsToFile;
    private final boolean loadDataScriptFromFile;

    public EnsureDataInterceptor() {

        if (isEmpty(System.getProperty("saveScriptsToFile"))) {
            saveScriptsToFile = false;
        } else {
            saveScriptsToFile = Boolean.parseBoolean(System.getProperty("saveScriptsToFile"));
        }

        if (isEmpty(System.getProperty("loadDataScriptFromFile"))) {
            loadDataScriptFromFile = false;
        } else {
            loadDataScriptFromFile = Boolean.parseBoolean(System.getProperty("loadDataScriptFromFile"));
        }
    }

    @Override
    public Object invoke(final MethodInvocation invocation) throws Throwable {
        final ITestCaseWithEnsureData testCase = (ITestCaseWithEnsureData) invocation.getThis();
        final String testCaseName = PropertyTypeDeterminator.stripIfNeeded(testCase.getClass()).getSimpleName();
        final String methodName = invocation.getMethod().getName();

        LOGGER.debug(() -> format("Intercepted [%s] in test case [%s].", methodName, testCaseName));

        if (!testCaseName.equals(runningTestCase)) {
            testCase.setCleanUpAfterPrepopulation(() -> { runningTestCase = ""; executedScripts.clear();});
            runningTestCase = testCaseName;
            executedScripts.clear();
        }

        // First, let's run methods to populate the data that the invoked methods depends on.
        final String[] ensureDataMethods = invocation.getStaticPart().getAnnotation(EnsureData.class).value();
        for (final String depMethodName: ensureDataMethods) {
            LOGGER.debug(() -> format("Processing dependencies for [%s] in test case [%s].", depMethodName, testCaseName));
            final Method method = testCase.getClass().getDeclaredMethod(depMethodName);
            method.invoke(testCase);
        }

        // And now we can invoke the intercepted method... but with some smarts...
        LOGGER.debug(() -> format("Checking if there are applicable data scripts in cache for [%s] in test case [%s].", methodName, testCaseName));
        // check if we have a script then run it before calling the method
        // we really need to know if we ought to call it... but for now let's simply call and ignore the errors
        final DbCreator dbCreator = testCase.getDbCreator();

        // try loading the script first from file if applicable
        if (loadDataScriptFromFile && !scripts.containsKey(methodName)) {
            final List<String> potentiallyLoadedScript = DbCreator.loadScriptFromFile(mkScriptFileName(methodName));
            if (!potentiallyLoadedScript.isEmpty()) {
                scripts.put(methodName, potentiallyLoadedScript);
                LOGGER.debug(() -> format("Loaded [%s] scripts from file for [%s]", potentiallyLoadedScript.size(), methodName));
            }
        }

        // Let's make a decision whether we can execute the script instead of invoking the method.
        boolean useScript = executedScripts.contains(methodName);
        if (!testCase.skipCaching() && !executedScripts.contains(methodName) && scripts.containsKey(methodName)) {
            // we're in business -- there is already a script prepared for us
            // so. let's just run it
            LOGGER.debug(() -> format("There are applicable data scripts in cache for [%s] in test case [%s].", methodName, testCaseName));
            final Connection conn = dbCreator.connection();
            try {
                final List<String> script = scripts.get(methodName);
                LOGGER.debug(() -> format("Executing [%s] data scripts from cache for [%s] in test case [%s].", script.size(), methodName, testCaseName));
                batchExecSql(script, conn, 100);
                conn.commit();
                executedScripts.add(methodName);
                LOGGER.debug(() -> format("Completed executing [%s] data scripts from cache for [%s] in test case [%s].", script.size(), methodName, testCaseName));
                useScript = true;
            } catch (final Exception ex) {
                LOGGER.warn(() -> format("Failed to execute data scripts from cache for [%s] in test case [%s] due to: %s", methodName, testCaseName, ex.getMessage()));
                conn.rollback();
            }
        }

        // if the cached script was used to populate the data then there is no need to actually call the method
        if (!useScript) {
            LOGGER.debug(() -> format("Invoking [%s] in test case [%s].", methodName, testCaseName));
            final Object result = invocation.proceed();
            LOGGER.debug(() -> format("Completed invocation of [%s] in test case [%s].", methodName, testCaseName));

            // if there was no script to the intercepted method yet then surely we can now generate and cache it
            LOGGER.debug(() -> format("Making a decision about generating data scripts for [%s] in test case [%s].", methodName, testCaseName));
            if (!testCase.skipCaching() && !scripts.containsKey(methodName)) {
                LOGGER.debug(() -> format("Generating data scripts for [%s] in test case [%s].", methodName, testCaseName));
                // let's collect only those records that belong to the current transaction
                final String transactionGuid = testCase.getTransactionGuid();
                final Connection conn = testCase.getDbCreator().connection();
                final List<String> script = dbCreator.genInsertStmt(dbCreator.persistentEntitiesMetadata(), conn).stream()
                                            //.peek(stmt -> System.out.println(stmt))
                                            .filter(stmt -> stmt.contains(transactionGuid))
                                            .map(stmt -> transformToUpdateIfAppropriate(stmt, transactionGuid))
                                            .collect(toList());
                LOGGER.debug(() -> format("Caching [%s] data scripts for [%s] in test case [%s].", script.size(), methodName, testCaseName));
                scripts.put(methodName, script);
                conn.commit();
                executedScripts.add(methodName);


                // and let's also save the script to a file if applicable
                if (saveScriptsToFile) {
                    LOGGER.debug(() -> format("Saving [%s] script [%s] to file...", script.size(), methodName));
                    DbCreator.saveScriptToFile(script, mkScriptFileName(methodName));
                }

            } else {
                LOGGER.debug(() -> format("Avoid generation of data scripts for [%s] in test case [%s] due to [skipping: %s, already present: %s].", methodName, testCaseName, testCase.skipCaching(), scripts.containsKey(methodName)));
            }

            return result;
        } else {
            LOGGER.debug(() -> format("Data scripts were used for [%s] in test case [%s].", methodName, testCaseName));
            return null;
        }
    }

    /// Transforms generated INSERT statements to UPDATE statements if the current transaction was not creating the corresponding data, but modifying it.
    ///
    private static String transformToUpdateIfAppropriate(final String insertStmt, final String transGuid) {
        // INSERT INTO [PERSON_]
        // ([_ID],[KEY_],[_VERSION],[USER_],[TIMESHEETTYPE_],[SUPERABLE_],[NORMALDAILYHOURS_],[NORMALWEEKLYHOURS_],[MONDAYREQUIRED_],[TUESDAYREQUIRED_],[WEDNESDAYREQUIRED_],[THURSDAYREQUIRED_],[FRIDAYREQUIRED_],[SATURDAYREQUIRED_],[SUNDAYREQUIRED_],[TIMESHEETALLOWED_],[COPYTIMESHEETALLOWED_],[REMOVEINITIALSONDEACTIVATE_],[PERSONTYPE_],[TITLE_],[EMPLOYEENO_],[CONTRACTOR_],[MISMANAGER_],[MISTEAMLEADER_],[COSTCENTRE_],[LOCATION_],[TEAM_],[CORERATE_],[CAPEXRATE_],[OSRATE_],[AGSRATE_],[AUTHORISER_],[BUYER_],[ISSUER_],[SUPPLYPERSON_],[SUPPLYSUPERVISOR_],[ORIGINATOR_],[PLANNER_],[TECHNICIAN_],[ENGINEER_],[REALMANAGER_],[REALTEAMLEADER_],[TECHNICALCOORDINATOR_],[RESTRICTEDWACREATOR_],[PHONE_],[FAX_],[MOBILE_],[EMAIL_],[WANOTIFICATIONEMAIL_],[DELIVERYLOCATION_],[ACTIVE_],[REFCOUNT_],[CREATEDBY_],[CREATEDDATE_],[CREATEDTRANSACTIONGUID_],[LASTUPDATEDBY_],[LASTUPDATEDDATE_],[LASTUPDATEDTRANSACTIONGUID_],[DESC_])
        // VALUES
        //(25,'UNIT_TEST_USER',0,0,NULL,NULL,NULL,NULL,'N','N','N','N','N','N','N','Y','Y','N',NULL,NULL,NULL,'N',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'N','N','N','N','N','N','N','N','N','N','N','N','N',NULL,NULL,NULL,'UNIT_TEST_USER@ttgams.tg.test','N',NULL,'Y',0,0,'Jan 22 2018  3:48:40.1410000PM','aceaa545-77b9-4f1f-8a1c-e646e9398ef4',NULL,NULL,NULL,'Person who is a user')
        final String[] parts = insertStmt.split("VALUES");
        final Pattern pTuple = Pattern.compile("\\((.*)\\)", Pattern.CASE_INSENSITIVE | Pattern.DOTALL | Pattern.MULTILINE);

        final String updateColName = "LASTUPDATEDTRANSACTIONGUID_";
        final int updateGuidColIndex = findUpdateGuildColumnIndex(updateColName, parts[0], pTuple);
        final String upadateGuidColValue = getValueByColumnIndex(updateGuidColIndex, parts[1], pTuple);

        final String createColName = "CREATEDTRANSACTIONGUID_";
        final int createGuidColIndex = findUpdateGuildColumnIndex(createColName, parts[0], pTuple);
        final String createGuidColValue = getValueByColumnIndex(createGuidColIndex, parts[1], pTuple);

        // check is INSERT -> UPDATE transformation is required
        if (upadateGuidColValue.contains(transGuid) && !createGuidColValue.contains(transGuid)) {
            final Pattern pTableName = Pattern.compile("INTO\\s*(.*)\\s*\\(");
            final Matcher m = pTableName.matcher(parts[0]);
            final String tableName;
            if (m.find()) {
                tableName = m.group(1).trim();
            } else {
                throw new DomainDriventTestException(format("Could not identify the table name from [%s].", parts[0]));
            }

            final int idColIndex = findUpdateGuildColumnIndex("_ID", parts[0], pTuple);
            final String idColValue = getValueByColumnIndex(idColIndex, parts[1], pTuple);

            final StringBuilder updateStmt = new StringBuilder();
            updateStmt.append("UPDATE "+ tableName + " ");
            updateStmt.append("SET ");
            final String assignments = StreamUtils.zip(streamFromText(parts[0], pTuple), streamFromText(parts[1], pTuple), (col, val) -> col + " = " + val).collect(Collectors.joining(", "));
            updateStmt.append(assignments + " ");
            updateStmt.append("WHERE _ID = " + idColValue + ";");
            return updateStmt.toString();
        }
        return insertStmt;
    }

    private static Stream<String> streamFromText(final String text, final Pattern p) {
        final Matcher m = p.matcher(text);
        if (m.find()) {
            final String group1 = m.group(1);

            final String[] values = group1.split(REGEX_FOR_CSV_LINE_WITH_SINGLE_QUOTES, -1);
            return Stream.of(values);
        }

        return Stream.empty();
    }

    private static String getValueByColumnIndex(final int updateGuidColumnIndex, final String valuesPart, final Pattern p) {
        final Matcher m = p.matcher(valuesPart);
        if (m.find()) {
            final String group1 = m.group(1);
            final String[] values = group1.split(REGEX_FOR_CSV_LINE_WITH_SINGLE_QUOTES, -1);
            return values[updateGuidColumnIndex];
        }

        throw new DomainDriventTestException(format("Could not find values substring in [%s].", valuesPart));
    }

    private static int findUpdateGuildColumnIndex(final String colName, final String insertIntoPart, final Pattern p) {
        final AtomicInteger updateGuidColumnIndex = new AtomicInteger(-1);
        final Matcher m = p.matcher(insertIntoPart);
        if (m.find()) {
            final String tupleOfColumnNames = m.group(1);
            final String[] columns = tupleOfColumnNames.split(",");

            Stream.of(columns).filter(v -> {
                updateGuidColumnIndex.incrementAndGet();
                return v.toLowerCase().contains(colName.toLowerCase());
            })
            .findFirst()
            .orElseThrow(() -> new DomainDriventTestException(format("Could not find column [%s] in [%s].", colName, insertIntoPart)));
        }

        return updateGuidColumnIndex.get();
    }

    private static String mkScriptFileName(final String methodName) {
        return format("%s/%s.script", DbCreator.baseDir, methodName);
    }

}
