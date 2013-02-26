package ua.com.fielden.platform.migration;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.Callable;

import org.apache.commons.lang.StringUtils;
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
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.utils.Pair;

import com.google.inject.Injector;

public class DataMigrator {
    private final Logger logger = Logger.getLogger(this.getClass());

    static class Task implements Callable<String> {
	private final String msg;

	private final Injector injector;
	private final EntityFactory factory;
	private final SessionFactory sessionFactory;
	private final String[] subsetItems;
	private final Connection conn;
	private final Class<? extends IRetriever<?>> retrieverClass;
	private final MigrationRun migrationRun;

	public Task(final String msg, final Injector injector, final SessionFactory sessionFactory, final Connection conn, final EntityFactory factory, final MigrationRun migrationRun, final Class<? extends IRetriever<?>> retrieverClass, final String... subsetItems) {
	    this.msg = msg;
	    this.injector = injector;
	    this.factory = factory;
	    this.sessionFactory = sessionFactory;
	    this.retrieverClass = retrieverClass;
	    this.conn = conn;
	    this.migrationRun = migrationRun;
	    this.subsetItems = subsetItems == null ? new String[] {} : subsetItems;
	}

	@Override
	public String call() throws Exception {
	    final IRetriever<? extends AbstractEntity<?>> ret = injector.getInstance(retrieverClass);
	    try {
		ret.populateData(sessionFactory, conn, factory, injector.getInstance(MigrationErrorDao.class), injector.getInstance(MigrationHistoryDao.class), migrationRun, getSubsetCondition(subsetItems));
		conn.close();
	    } catch (final Exception e) {
		return "Interrupted with exception: " + Arrays.asList(e.getStackTrace());
	    }
	    return msg;
	}

	private String getSubsetCondition(final String[] subsetItems) {
	    if (subsetItems == null || subsetItems.length == 0) {
		return null;
	    } else {
		final StringBuffer sb = new StringBuffer();
		sb.append(" (");
		for (final Iterator<String> iterator = Arrays.asList(subsetItems).iterator(); iterator.hasNext();) {
		    final String subsetItem = iterator.next();
		    sb.append("'" + subsetItem + "'");
		    if (iterator.hasNext()) {
			sb.append(",");
		    }
		}
		sb.append(")");
		return sb.toString();
	    }
	}
    }

    public static PriorityQueue<Pair<Set<String>, Long>> splitIntoBatches(final List<Pair<String, Long>> groupedItems, final int numberOfBatches) {
	final PriorityQueue<Pair<Set<String>, Long>> pq = new PriorityQueue<Pair<Set<String>, Long>>(numberOfBatches, new Comparator<Pair<Set<String>, Long>>() {
	    @Override
	    public int compare(final Pair<Set<String>, Long> o1, final Pair<Set<String>, Long> o2) {
		return o1.getValue().compareTo(o2.getValue());
	    }
	});

	for (int i = 0; i < numberOfBatches; i++) {
	    pq.add(new Pair<Set<String>, Long>(new HashSet<String>(), 0l));
	}

	for (final Pair<String, Long> pair : groupedItems) {
	    final Pair<Set<String>, Long> bin = pq.poll();
	    bin.getKey().add(pair.getKey());
	    bin.setValue(bin.getValue() + pair.getValue());
	    pq.add(bin);
	}

	return pq;
    }

    private final HibernateUtil hiberUtil;
    private final EntityFactory factory;
    private final List<IRetriever<? extends AbstractEntity<?>>> retrievers = new ArrayList<IRetriever<? extends AbstractEntity<?>>>();
    private final MigrationHistoryDao histDao;
    private final MigrationErrorDao errorDao;
    private final MigrationRunDao runDao;
    private final MigrationRun migrationRun;
    private final Injector injector;
    private final int threadCount;

    public DataMigrator(final Injector injector, final HibernateUtil hiberUtil, final EntityFactory factory, final String migratorName, final int threadCount, final Class... retrieversClasses)
	    throws Exception {
	this.threadCount = threadCount;
	this.injector = injector;
	this.factory = factory;
	this.hiberUtil = hiberUtil;
	this.histDao = injector.getInstance(MigrationHistoryDao.class);
	this.errorDao = injector.getInstance(MigrationErrorDao.class);
	this.runDao = injector.getInstance(MigrationRunDao.class);
	final DomainMetadataAnalyser dma = new DomainMetadataAnalyser(injector.getInstance(DomainMetadata.class));

	for (final Class<? extends IRetriever<? extends AbstractEntity<?>>> retrieverClass : retrieversClasses) {
	    final IRetriever<? extends AbstractEntity<?>> ret = injector.getInstance(retrieverClass);
	    logger.debug("Checking props for [" + ret.getClass().getSimpleName() + "]");
	    final SortedMap<String,RetrievedPropValidationError> checkResult = new RetrieverPropsValidator(dma, ret.type(), ret.resultFields().keySet()).validate();
	    if (checkResult.size() > 0) {
		logger.error("The following issues have been revealed for props in [" + ret.getClass().getSimpleName() + "]:\n " + checkResult);
	    }
	    retrievers.add(ret);
	}

	validateRetrievalSql(dma);
	throw new IllegalStateException();

//	final Date now = new Date();
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
	final String sql = new RetrieverSqlProducer(dma, retriever).getKeyUniquenessViolationSql();
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
	final String sql = new RetrieverSqlProducer(dma, retriever).getSql();
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

    /**
     * Checks the correctness of the legacy data retrieval sql syntax and column aliases.
     *
     * @return
     * @throws Exception
     */
    private boolean validateRetrievalSql(final IRetriever<? extends AbstractEntity<?>> retriever, final Connection conn) throws Exception {
	System.out.print("Validating " + retriever.getClass().getSimpleName() + " ... ");
	boolean foundErrors = false;
	final String legacyDataSql = null; //retriever.selectSql();
	if (!StringUtils.isEmpty(legacyDataSql)) {

	    final Statement st = conn.createStatement();
	    try {
		final ResultSet rs = st.executeQuery(legacyDataSql);

		final ResultSetMetaData md = rs.getMetaData();

		final List<String> keyPropNames = Finder.getFieldNames(Finder.getKeyMembers(retriever.type()));
		final Set<String> propNames = new HashSet<String>();

		for (int index = 1; index <= md.getColumnCount(); index++) {
		    final String propName = /*AbstractRetriever.decodePropertyName*/(md.getColumnLabel(index));
		    propNames.add(propName);

		    try {
			PropertyTypeDeterminator.determinePropertyType(retriever.type(), propName);
		    } catch (final Exception ex) {
			foundErrors = true;
			logger.error("\n\tEntity of type [" + retriever.type().getName() + "] doesn't have property [" + propName + "].\n\tPlease correct respective column alias in " + retriever.getClass().getName() + " selectSql() method.");
		    }
		}

		rs.close();

		for (final String keyPropName : keyPropNames) {
		    if (!propNames.contains(keyPropName)) {
			foundErrors = true;
			logger.error("\n\tRetriever for type [" + retriever.type().getName() + "] doesn't have all key properties specified. These should be " + keyPropNames + ".\n\tPlease add missing properties in " + retriever.getClass().getName() + " selectSql() method.");
		    }
		}


		final ResultSet rs2 = null; //st.executeQuery(getKeyUniquenessCheckSql(retriever));

		if (rs2.next()) {
		    foundErrors = true;
		    //logger.error("\n\tRetriever [" + retriever.getClass().getName() + "] for type [" + retriever.type().getName() + "] contains duplicates in terms of entity key " + keyPropNames + ". More details here:\n" + getKeyUniquenessCheckSql(retriever) + "\n");
		}

		rs2.close();
	    } catch (final Exception ex) {
		foundErrors = true;
		logger.error(retriever.getClass(), ex);
	    } finally {
		st.close();
	    }
	}
	System.out.println("done.");
	return foundErrors;
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
	final List<Result> results1 = new ArrayList<Result>();

	final SessionFactory sFactory = hiberUtil.getSessionFactory();

	for (final IRetriever<? extends AbstractEntity<?>> ret : retrievers) {
	    if (ret.splitProperty() == null) {
		final Connection conn = injector.getInstance(Connection.class);
		final Result result = ret.populateData(sFactory, conn, factory, errorDao, histDao, migrationRun, null);
		conn.close();
		results1.add(result);
	    } else {
//		final List<Task> tasks = new ArrayList<Task>();
//
//		for (final Iterator<Pair<Set<String>, Long>> iterator = DataMigrator.splitIntoBatches(populateData(ret), threadCount).iterator(); iterator.hasNext();) {
//		    final Connection conn = injector.getInstance(Connection.class);
//		    tasks.add(new Task("done.", injector, sFactory, conn, factory, migrationRun, (Class<? extends IRetriever<?>>) ret.getClass(), iterator.next().getKey().toArray(new String[] {})));
//		}
//
//		final ExecutorService exec = Executors.newFixedThreadPool(tasks.size());
//		final List<Future<String>> results = new ArrayList<Future<String>>(tasks.size());
//
//		for (final Task task : tasks) {
//		    results.add(exec.submit(task));
//		}
//
//		for (int index = 0; index < results.size(); index++) {
//		    System.out.println(ret.getClass().getSimpleName() + " ... waiting for task with index " + index + " ... " + results.get(index).get());
//		}
//
//		exec.shutdown(); // do not forget to shutdown the threads

	    }
	}

	for (final Result result : results1) {
	    System.out.println("\n" + result.getMessage());
	}

	runSql(dataPostPopulateSql());
	migrationRun.setFinished(new Date());
	runDao.save(migrationRun);
    }
}
