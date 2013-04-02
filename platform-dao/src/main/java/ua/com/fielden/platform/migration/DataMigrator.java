package ua.com.fielden.platform.migration;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import ua.com.fielden.platform.dao.DomainMetadata;
import ua.com.fielden.platform.dao.DomainMetadataAnalyser;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.migration.RetrieverPropsValidator.RetrievedPropValidationError;
import ua.com.fielden.platform.migration.dao.MigrationErrorDao;
import ua.com.fielden.platform.migration.dao.MigrationHistoryDao;
import ua.com.fielden.platform.migration.dao.MigrationRunDao;
import ua.com.fielden.platform.persistence.HibernateUtil;

import com.google.inject.Injector;

public class DataMigrator {
    private final Logger logger = Logger.getLogger(this.getClass());

    private final HibernateUtil hiberUtil;
    private final EntityFactory factory;
    private final List<IRetriever<? extends AbstractEntity<?>>> retrievers = new ArrayList<IRetriever<? extends AbstractEntity<?>>>();
    private final MigrationHistoryDao histDao;
    private final MigrationErrorDao errorDao;
    private final MigrationRunDao runDao;
    private final MigrationRun migrationRun;
    private final Injector injector;
    private final DomainMetadataAnalyser dma;

    private static List<IRetriever<? extends AbstractEntity<?>>> instantiateRetrievers(final Injector injector, final Class... retrieversClasses) {
	final List<IRetriever<? extends AbstractEntity<?>>> result = new ArrayList<IRetriever<? extends AbstractEntity<?>>>();
	for (final Class<? extends IRetriever<? extends AbstractEntity<?>>> retrieverClass : retrieversClasses) {
	    result.add(injector.getInstance(retrieverClass));
	}
	return result;
    }

    public DataMigrator(final Injector injector, final HibernateUtil hiberUtil, final EntityFactory factory, final String migratorName, final Class... retrieversClasses)
	    throws Exception {
	this.injector = injector;
	this.factory = factory;
	this.hiberUtil = hiberUtil;
	this.histDao = injector.getInstance(MigrationHistoryDao.class);
	this.errorDao = injector.getInstance(MigrationErrorDao.class);
	this.runDao = injector.getInstance(MigrationRunDao.class);
	this.dma = new DomainMetadataAnalyser(injector.getInstance(DomainMetadata.class));
	this.retrievers.addAll(instantiateRetrievers(injector, retrieversClasses));

	new RetrieverDeadReferencesSeeker(dma).determineUsers(retrievers);

	for (final IRetriever<? extends AbstractEntity<?>> ret : retrievers) {
	    logger.debug("Checking props for [" + ret.getClass().getSimpleName() + "]");
	    final SortedMap<String,RetrievedPropValidationError> checkResult = new RetrieverPropsValidator(dma, ret.type(), ret.resultFields().keySet()).validate();
	    if (checkResult.size() > 0) {
		logger.error("The following issues have been revealed for props in [" + ret.getClass().getSimpleName() + "]:\n " + checkResult);
	    }
	}
	final Connection conn = injector.getInstance(Connection.class);

	checkEmptyStrings(dma, conn);
	checkDataIntegrity(dma, conn);
	//validateRetrievalSql(dma);
	final Date now = new Date();
	migrationRun = null;
	throw new RuntimeException();

//	migrationRun = factory.newByKey(MigrationRun.class, migratorName + "_" + now.getTime());
//	migrationRun.setStarted(now);
//	runDao.save(migrationRun);
    }

    /**
     * Checks the correctness of the legacy data retrieval sql syntax and column aliases.
     *
     * @return
     * @throws Exception
     */
    private boolean validateRetrievalSqlForKeyFieldsUniqueness(final DomainMetadataAnalyser dma, final IRetriever<? extends AbstractEntity<?>> retriever, final Connection conn) throws Exception {
	final Statement st = conn.createStatement();
	final String sql = new RetrieverSqlProducer(dma).getKeyUniquenessViolationSql(retriever);
	boolean result = false;
	try {
	    logger.debug("Checking uniqueness of key data for [" + retriever.getClass().getSimpleName() + "]");
	    final ResultSet rs = st.executeQuery(sql);
	    if (rs.next()) {
		logger.error("There are duplicates in data of [" + retriever.getClass().getSimpleName() + "]");
		result = true;
	    }
	    rs.close();
	} catch (final Exception ex) {
	    logger.error("Exception while checking [" + retriever.getClass().getSimpleName() + "]" + ex + " SQL:\n" + sql);
	    result = true;
	} finally {
	    st.close();
	}

	return result;
    }

    /**
     * Checks the correctness of the legacy data retrieval sql syntax and column aliases.
     *
     * @return
     * @throws Exception
     */
    private boolean checkRetrievalSqlForSyntaxErrors(final DomainMetadataAnalyser dma, final IRetriever<? extends AbstractEntity<?>> retriever, final Connection conn) throws Exception {
	final Statement st = conn.createStatement();
	final String sql = new RetrieverSqlProducer(dma).getSql(retriever);
	boolean result = false;
	try {
	    logger.debug("Checking sql syntax for [" + retriever.getClass().getSimpleName() + "]");
	    final ResultSet rs = st.executeQuery(sql);
	    rs.close();
	} catch (final Exception ex) {
	    logger.error("Exception while checking syntax for [" + retriever.getClass().getSimpleName() + "]" + ex + " SQL:\n" + sql);
	    result = true;
	} finally {
	    st.close();
	}

	return result;
    }

    private boolean checkDataIntegrity(final DomainMetadataAnalyser dma, final Connection conn) throws Exception {
	final Map<Class<? extends AbstractEntity<?>>, String> stmts = new RetrieverDeadReferencesSeeker(dma).determineUsers(retrievers);
	final Statement st = conn.createStatement();
	boolean result = false;
	for (final Entry<Class<? extends AbstractEntity<?>>, String> entry : stmts.entrySet()) {
	    try {
		final ResultSet rs = st.executeQuery(entry.getValue());
		rs.next();
		final Integer count = rs.getInt(1);
		if (count > 0) {
		    logger.error("Dead references count for entity type [" + entry.getKey().getSimpleName() + "] is [" + count + "].\n" + entry.getValue() + "\n\n\n");
		}
		rs.close();
	    } catch (final Exception ex) {
		logger.error("Exception while counting dead references for entity type [" + entry.getKey().getSimpleName() + "]" + ex + " SQL:\n" + entry.getValue());
		result = true;
	    } finally {
	    }

	}
	st.close();

	return result;
    }

    private boolean checkEmptyStrings(final DomainMetadataAnalyser dma, final Connection conn) throws Exception {
	final Set<String> stmts = new RetrieverEmptyStringsChecker(dma).getSqls(retrievers);
	final Statement st = conn.createStatement();
	boolean result = false;
	for (final String sql : stmts) {
	    try {
		final ResultSet rs = st.executeQuery(sql);
		rs.next();
		final String retriever = rs.getString(1);
		final String prop = rs.getString(2);
		final Integer count = rs.getInt(3);
		if (count > 0) {
		    logger.error("Empty string reference count for property [" + prop +"] within retriever [" + retriever + "] is [" + count + "].\n");
		}
		rs.close();
	    } catch (final Exception ex) {
		logger.error("Exception while counting empty strings with SQL:\n" + sql);
		result = true;
	    } finally {
	    }

	}
	st.close();

	return result;
    }

    private void runSql(final List<String> ddl) throws Exception {
	final Transaction tr = hiberUtil.getSessionFactory().getCurrentSession().beginTransaction();
	final Connection conn = hiberUtil.getSessionFactory().getCurrentSession().connection();
	for (final String sql : ddl) {
	    final Statement st = conn.createStatement();
	    st.execute(sql);
	    st.close();
	}
	tr.commit();
    }

    private List<String> dataPostPopulateSql() {
	final List<String> sql = new ArrayList<String>();
	//sql.add("UPDATE NUMBERS SET WONOINC = COALESCE((SELECT MAX(KEY_) FROM WODET), 0) WHERE NUMBKEY = 'WO';");
	return sql;
    }

    private void validateRetrievalSql(final DomainMetadataAnalyser dma) throws Exception {
	boolean foundErrors = false;
	final Connection conn = injector.getInstance(Connection.class);
	for (final IRetriever<? extends AbstractEntity<?>> ret : retrievers) {
	    if (validateRetrievalSqlForKeyFieldsUniqueness(dma, ret, conn)) {
		foundErrors = true;
	    }

	    if (checkRetrievalSqlForSyntaxErrors(dma, ret, conn)) {
		foundErrors = true;
	    }
    	}
	conn.close();
	if (foundErrors) {
	    throw new RuntimeException("Validation detected errors. Pls consult the log file for details");
	}
    }

    public void populateData() throws Exception {
	final List<Result> results = new ArrayList<Result>();

	final SessionFactory sFactory = hiberUtil.getSessionFactory();

	for (final IRetriever<? extends AbstractEntity<?>> ret : retrievers) {
//	    if (ret.splitProperty() == null) {
		final Connection conn = injector.getInstance(Connection.class);
		final Result result = ret.populateData(sFactory, conn, factory, errorDao, histDao, migrationRun, null);
		conn.close();
		results.add(result);
//	    }
	}

	for (final Result result : results) {
	    System.out.println("\n" + result.getMessage());
	}

	runSql(dataPostPopulateSql());
	migrationRun.setFinished(new Date());
	runDao.save(migrationRun);
    }
}
