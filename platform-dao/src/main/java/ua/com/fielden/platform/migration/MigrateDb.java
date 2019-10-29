package ua.com.fielden.platform.migration;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.hibernate.Transaction;

import com.google.inject.Injector;

import ua.com.fielden.platform.persistence.HibernateUtil;
import ua.com.fielden.platform.security.user.IUser;

public class MigrateDb {

    private static final Logger LOGGER = Logger.getLogger(MigrateDb.class);
    /**
     * Checks whether servicing structures exists, and creates them in case of non-existence.
     *
     * @param ddl
     * @param conn
     * @throws SQLException
     * @throws Exception
     */
    private static void checkAndCreate(final List<String> ddl, final HibernateUtil hiberUtil) throws SQLException {
        final Transaction tr = hiberUtil.getSessionFactory().getCurrentSession().beginTransaction();
        hiberUtil.getSessionFactory().getCurrentSession().doWork(conn -> {
            for (final String sql : ddl) {
                final Statement st = conn.createStatement();
                st.execute(sql);
                st.close();
            }
        });
        tr.commit();
    }

    private List<Class<?>> limitedWithFromStmt(final String fromRetriever, final Class<?>[] retrieversClassesSequence) {
        final List<Class<?>> result = new ArrayList<>();

        boolean foundFromRetriever = false;
        final Class<?> fromRetrieverClass = getRetrieverClassBySimpleName(fromRetriever.trim(), retrieversClassesSequence);

        for (final Class<?> retrieverClass : retrieversClassesSequence) {
            if (!foundFromRetriever && retrieverClass.equals(fromRetrieverClass)) {
                foundFromRetriever = true;
            }

            if (foundFromRetriever) {
                result.add(retrieverClass);
            }
        }

        return result;
    }

    private List<Class<?>> limitedWithToStmt(final String toRetriever, final Class<?>[] retrieversClassesSequence) {
        final List<Class<?>> result = new ArrayList<>();

        boolean foundToRetriever = false;
        final Class<?> toRetrieverClass = getRetrieverClassBySimpleName(toRetriever.trim(), retrieversClassesSequence);

        for (final Class<?> retrieverClass : retrieversClassesSequence) {
            if (!foundToRetriever) {
                result.add(retrieverClass);

                if (retrieverClass.equals(toRetrieverClass)) {
                    foundToRetriever = true;
                }
            }
        }

        return result;
    }

    private List<Class<?>> limitedWithList(final String[] retrieverNames, final Class<?>[] retrieversClassesSequence) {
        final List<Class<?>> result = new ArrayList<>();

        for (final String retrieverName : retrieverNames) {
            result.add(getRetrieverClassBySimpleName(retrieverName.trim(), retrieversClassesSequence));
        }

        final List<Class<?>> result2 = new ArrayList<>();

        // rearranging user limited subset of retrievers according to established retrievers sequence
        for (final Class<?> retrieverClass : retrieversClassesSequence) {
            if (result.contains(retrieverClass)) {
                result2.add(retrieverClass);
            }
        }

        return result2;
    }

    private Class<?> getRetrieverClassBySimpleName(final String retieverClassSimpleName, final Class<?>[] retrieversClassesSequence) {
        for (final Class<?> retrieverClass : retrieversClassesSequence) {
            if (retrieverClass.getSimpleName().equalsIgnoreCase(retieverClassSimpleName)) {
                return retrieverClass;
            }
        }

        throw new IllegalArgumentException("Can't find class for retriever with simple name: " + retieverClassSimpleName);
    }

    private enum CmdParams {
        LIMIT_TO("-limitTo"), RESET_PASSWORDS("-resetPasswords"), DETAILS("-details"), CREATE_DB_SCHEMA("-createSchema"), PRINT_DB_SCHEMA("-printSchema"), SKIP_VALIDATIONS(
                "-skipValidations");

        private final String value;

        CmdParams(final String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    private Map<CmdParams, String> retrieveCommandLineParams(final String[] args) {
        final Map<CmdParams, String> result = new EnumMap<>(CmdParams.class);

        for (int i = 0; i < args.length; i++) {
            if (CmdParams.LIMIT_TO.value.equals(args[i])) {
                i = i + 1;
                if (i < args.length && !args[i].startsWith("-")) {
                    result.put(CmdParams.LIMIT_TO, args[i]);
                } else {
                    throw new IllegalArgumentException("-limitTo requires one of the following argument... instead of " + args[i]);
                }
            } else if (CmdParams.SKIP_VALIDATIONS.value.equals(args[i])) {
                result.put(CmdParams.SKIP_VALIDATIONS, null);
            } else if (CmdParams.RESET_PASSWORDS.value.equals(args[i])) {
                result.put(CmdParams.RESET_PASSWORDS, null);
            } else if (CmdParams.CREATE_DB_SCHEMA.value.equals(args[i])) {
                result.put(CmdParams.CREATE_DB_SCHEMA, null);
            } else if (CmdParams.PRINT_DB_SCHEMA.value.equals(args[i])) {
                result.put(CmdParams.PRINT_DB_SCHEMA, null);
            } else if (CmdParams.DETAILS.value.equals(args[i])) {
                result.put(CmdParams.DETAILS, null);
            } else {
                throw new IllegalArgumentException("Unrecognised command line parameter: " + args[i]);
            }
        }

        return result;
    }

    private List<Class<?>> decodeLimitToParameter(final String paramValue, final Class<?>[] retrieversClassesSequence) {
        final String[] values = paramValue.trim().split(",");
        if (values.length == 0) {
            throw new IllegalArgumentException("-limitTo parameter has incorrect argument");
        }

        if (values.length != 1 || values[0].trim().split(" ").length == 1) {
            return limitedWithList(values, retrieversClassesSequence);
        } else {
            final String[] fromOrTovalues = values[0].split(" ");
            final String fromOrTo = fromOrTovalues[0].trim();
            if (fromOrTovalues.length == 2) {
                if ("from".equalsIgnoreCase(fromOrTo)) {
                    return limitedWithFromStmt(fromOrTovalues[1], retrieversClassesSequence);
                } else if ("to".equalsIgnoreCase(fromOrTo)) {
                    return limitedWithToStmt(fromOrTovalues[1], retrieversClassesSequence);
                } else {
                    throw new IllegalArgumentException("Invalid argument in condition to limit subset of existing retrievers. Should be either 'from' or 'to'. Was '"
                            + fromOrTo + "'");
                }
            } else {
                throw new IllegalArgumentException("-limitTo parameter has more than 2 words in its string argument: " + paramValue);
            }
        }
    }

    public void migrate(final String[] args, final Properties props, final List<String> ddl, final Class[] retrieversClassesSequence, final Injector injector) throws SQLException {
        final Map<CmdParams, String> cmdParams = retrieveCommandLineParams(args);
        final String limitToParamArgument = cmdParams.get(CmdParams.LIMIT_TO);
        final Class[] limitToRetrievers = limitToParamArgument == null ? retrieversClassesSequence
                : decodeLimitToParameter(limitToParamArgument, retrieversClassesSequence).toArray(new Class[] {});

        final HibernateUtil hibernateUtil = injector.getInstance(HibernateUtil.class);

        if (cmdParams.containsKey(CmdParams.CREATE_DB_SCHEMA)) {
            LOGGER.info("Creating database schema...");
            checkAndCreate(ddl, hibernateUtil);
        }

        if (cmdParams.containsKey(CmdParams.PRINT_DB_SCHEMA)) {
            LOGGER.info("Printing database schema...\n");
            for (final String ddlStmt : ddl) {
                LOGGER.info(ddlStmt);
            }
        } else {
            new DataMigrator(injector, hibernateUtil, cmdParams.containsKey(CmdParams.SKIP_VALIDATIONS), cmdParams.containsKey(CmdParams.DETAILS), limitToRetrievers);
        }
        // reset passwords
        if (cmdParams.containsKey(CmdParams.RESET_PASSWORDS)) {
            LOGGER.info("Resetting user passwords...");
            final ResetUserPassword passwordReset = new ResetUserPassword(injector.getInstance(IUser.class));
            passwordReset.resetAll();
        }

        LOGGER.info("Data migration completed.");
    }
}

