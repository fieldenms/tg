package ua.com.fielden.platform.migration;

import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.hibernate.Transaction;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.persistence.HibernateUtil;
import ua.com.fielden.platform.security.provider.IUserController;

import com.google.inject.Injector;

public class MigrateDb {

    /**
     * Checks whether servicing structures exists, and creates them in case of non-existence.
     *
     * @param ddl
     * @param conn
     * @throws Exception
     */
    private static void checkAndCreate(final List<String> ddl, final HibernateUtil hiberUtil) throws Exception {
	final Transaction tr = hiberUtil.getSessionFactory().getCurrentSession().beginTransaction();
	final Connection conn = hiberUtil.getSessionFactory().getCurrentSession().connection();
	for (final String sql : ddl) {
	    final Statement st = conn.createStatement();
	    st.execute(sql);
	    st.close();
	}

	tr.commit();
    }

    private List<Class> limitedWithFromStmt(final String fromRetriever, final Class[] retrieversClassesSequence) {
	//final Options options = new Options();


	final List<Class> result = new ArrayList<Class>();

	boolean foundFromRetriever = false;
	final Class fromRetrieverClass = getRetrieverClassBySimpleName(fromRetriever.trim(), retrieversClassesSequence);

	for (int i = 0; i < retrieversClassesSequence.length; i++) {
	    final Class retrieverClass = retrieversClassesSequence[i];
	    if (!foundFromRetriever) {
		if (retrieverClass.equals(fromRetrieverClass)) {
		    foundFromRetriever = true;
		}
	    }

	    if (foundFromRetriever) {
		result.add(retrieverClass);
	    }
	}

	return result;
    }

    private List<Class> limitedWithToStmt(final String toRetriever, final Class[] retrieversClassesSequence) {
	final List<Class> result = new ArrayList<Class>();

	boolean foundToRetriever = false;
	final Class toRetrieverClass = getRetrieverClassBySimpleName(toRetriever.trim(), retrieversClassesSequence);

	for (int i = 0; i < retrieversClassesSequence.length; i++) {
	    final Class retrieverClass = retrieversClassesSequence[i];
	    if (!foundToRetriever) {
		result.add(retrieverClass);

		if (retrieverClass.equals(toRetrieverClass)) {
		    foundToRetriever = true;
		}
	    }
	}

	return result;
    }

    private List<Class> limitedWithList(final String[] retrieverNames, final Class[] retrieversClassesSequence) {
	final List<Class> result = new ArrayList<Class>();

	for (int i = 0; i < retrieverNames.length; i++) {
	    result.add(getRetrieverClassBySimpleName(retrieverNames[i].trim(), retrieversClassesSequence));
	}

	final List<Class> result2 = new ArrayList<Class>();

	// rearranging user limited subset of retrievers according to established retrievers sequence
	for (int i = 0; i < retrieversClassesSequence.length; i++) {
	    final Class retrieverClass = retrieversClassesSequence[i];
	    if (result.contains(retrieverClass)) {
		result2.add(retrieverClass);
	    }
	}

	return result2;
    }

    private Class getRetrieverClassBySimpleName(final String retieverClassSimpleName, final Class[] retrieversClassesSequence) {
	for (int i = 0; i < retrieversClassesSequence.length; i++) {
	    final Class retrieverClass = retrieversClassesSequence[i];
	    if (retrieverClass.getSimpleName().equalsIgnoreCase(retieverClassSimpleName)) {
		return retrieverClass;
	    }
	}

	throw new IllegalArgumentException("Can't find class for retriever with simple name: " + retieverClassSimpleName);
    }

    private static enum CmdParams {
	    LIMIT_TO("-limitTo"), RESET_PASSWORDS("-resetPasswords"), DETAILS("-details"), CREATE_DB_SCHEMA("-createSchema"), PRINT_DB_SCHEMA("-printSchema");

	    private final String value;

	    CmdParams(final String value) {
		this.value = value;
	    }

	    public String getValue() {
		return value;
	    }

	    @Override
	    public String toString() {
	        return value;
	    }
    }

    private Map<CmdParams, String> retrieveCommandLineParams(final String[] args) {
	final Map<CmdParams, String> result = new HashMap<CmdParams, String>();

	for (int i = 0; i < args.length; i++) {
	    if (CmdParams.LIMIT_TO.value.equals(args[i])) {
		i = i + 1;
		if (i < args.length && !args[i].startsWith("-")) {
		    result.put(CmdParams.LIMIT_TO, args[i]);
		} else {
		    throw new IllegalArgumentException("-limitTo requires one of the following argument... instead of " + args[i]);
		}
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

    private List<Class> decodeLimitToParameter(final String paramValue, final Class[] retrieversClassesSequence) {

	final String[] values = paramValue.trim().split(",");
	if (values.length > 0) {
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
			    throw new IllegalArgumentException("Invalid argument in condition to limit subset of existing retrievers. Should be either 'from' or 'to'. Was '" +  fromOrTo + "'");
			}
		    } else {
			throw new IllegalArgumentException("-limitTo parameter has more than 2 words in its string argument: " + paramValue);
		    }
		}
	} else {
	    throw new IllegalArgumentException("-limitTo parameter has incorrect argument");
	}
    }

    public void migrate(final String[] args, final Properties props, final List<String> ddl, final Class[] retrieversClassesSequence, final Injector injector, final Class<? extends AbstractEntity<?>> personClass) throws Exception {
	final Map<CmdParams, String> cmdParams = retrieveCommandLineParams(args);
	final String limitToParamArgument = cmdParams.get(CmdParams.LIMIT_TO);
	final Class[] limitToRetrievers = limitToParamArgument == null ? retrieversClassesSequence
		: decodeLimitToParameter(limitToParamArgument, retrieversClassesSequence).toArray(new Class[] {});

	final HibernateUtil hibernateUtil = injector.getInstance(HibernateUtil.class);
	final EntityFactory factory = injector.getInstance(EntityFactory.class);

	if (cmdParams.containsKey(CmdParams.CREATE_DB_SCHEMA)) {
	    System.out.println("Creating database schema...");
	    checkAndCreate(ddl, hibernateUtil);
	}

	if (cmdParams.containsKey(CmdParams.PRINT_DB_SCHEMA)) {
	    System.out.println("Printing database schema...\n");
	    for (final String ddlStmt : ddl) {
		System.out.println(ddlStmt);
	    }
	} else {
	    new DataMigrator(injector, hibernateUtil, factory, cmdParams.containsKey(CmdParams.DETAILS), personClass, limitToRetrievers);//.populateData();
	}
	// reset passwords
	if (cmdParams.containsKey(CmdParams.RESET_PASSWORDS)) {
	    System.out.println("Resetting user passwords...");
	    final ResetUserPassword passwordReset = new ResetUserPassword(injector.getInstance(IUserController.class));
	    passwordReset.resetAll(props.getProperty("private-key"));
	}

	System.out.println("\nData migration completed.");
    }
}