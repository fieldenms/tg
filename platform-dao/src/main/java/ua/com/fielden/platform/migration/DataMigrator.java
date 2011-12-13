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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.error.Result;
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
	private final Class<? extends IRetriever> retrieverClass;
	private final MigrationRun migrationRun;

	public Task(final String msg, final Injector injector, final SessionFactory sessionFactory, final Connection conn, final EntityFactory factory, final MigrationRun migrationRun, final Class<? extends IRetriever> retrieverClass, final String... subsetItems) {
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
	    final IRetriever<? extends AbstractEntity> ret = injector.getInstance(retrieverClass);
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

    public static String getSplitSql(final IRetriever ret) {
	final String baseSql = ret.selectSql().toUpperCase();
	final int orderByStart = baseSql.indexOf("ORDER BY");
	final StringBuffer sb = new StringBuffer();
	final String splitPropertyColumn = AbstractRetriever.encodePropertyName(ret.splitProperty());
	sb.append("SELECT A." + splitPropertyColumn + ", COUNT(*) FROM (" + (orderByStart != -1 ? baseSql.substring(0, orderByStart) : baseSql) + ") A GROUP BY A."
		+ splitPropertyColumn + " ORDER BY 2 DESC");

	return sb.toString();
    }

    public static String getKeyUniquenessCheckSql(final IRetriever ret) {
	final int orderByStart = ret.selectSql().toUpperCase().indexOf("ORDER BY");
	final String baseSql = orderByStart != -1 ? ret.selectSql().toUpperCase().substring(0, orderByStart) : ret.selectSql().toUpperCase();

	final List<String> keyPropColAliases = new ArrayList<String>();
	for (final String keyPropName : Finder.getFieldNames(Finder.getKeyMembers(ret.type()))) {
	    keyPropColAliases.add(AbstractRetriever.encodePropertyName(keyPropName));
	}

	final StringBuffer sb = new StringBuffer();
	final StringBuffer sbOrderBy = new StringBuffer();

	sb.append("SELECT * FROM (" + baseSql + ") MT WHERE 1 < (SELECT COUNT(*) FROM (" + baseSql + ") AA WHERE ");
	for (final Iterator<String> iterator = keyPropColAliases.iterator(); iterator.hasNext();) {
	    final String keyPropName = iterator.next();
	    sb.append("AA." + keyPropName + " = MT." + keyPropName + (iterator.hasNext() ? " AND " : ""));
	    sbOrderBy.append("MT." + keyPropName + (iterator.hasNext() ? ", " : ""));
	}
	sb.append(") ORDER BY " + sbOrderBy.toString());

	return sb.toString();
    }

    public List<Pair<String, Long>> populateData(final IRetriever ret) throws Exception {
	final List<Pair<String, Long>> result = new ArrayList<Pair<String, Long>>();
	final String legacyDataSql = getSplitSql(ret);
	final Connection conn = injector.getInstance(Connection.class);
	final Statement st = conn.createStatement();
	final ResultSet rs = st.executeQuery(legacyDataSql);

	while (rs.next()) {
	    result.add(new Pair<String, Long>(rs.getString(1), rs.getLong(2)));
	}
	rs.close();
	st.close();
	conn.close();

	return result;
    }

    private final HibernateUtil hiberUtil;
    private final EntityFactory factory;
    private final List<IRetriever<? extends AbstractEntity>> retrievers = new ArrayList<IRetriever<? extends AbstractEntity>>();
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

	for (final Class<? extends IRetriever<? extends AbstractEntity>> retrieverClass : retrieversClasses) {
	    retrievers.add(injector.getInstance(retrieverClass));
	}

	validateRetrievalSql();

	final Date now = new Date();
	migrationRun = factory.newByKey(MigrationRun.class, migratorName + "_" + now.getTime());
	migrationRun.setStarted(now);
	runDao.save(migrationRun);
    }

    /**
     * Checks the correctness of the legacy data retrieval sql syntax and column aliases.
     *
     * @return
     * @throws Exception
     */
    private boolean validateRetrievalSql(final IRetriever retriever, final Connection conn) throws Exception {
	System.out.print("Validating " + retriever.getClass().getSimpleName() + " ... ");
	boolean foundErrors = false;
	final String legacyDataSql = retriever.selectSql();
	if (!StringUtils.isEmpty(legacyDataSql)) {

	    final Statement st = conn.createStatement();
	    try {
		final ResultSet rs = st.executeQuery(legacyDataSql);

		final ResultSetMetaData md = rs.getMetaData();

		final List<String> keyPropNames = Finder.getFieldNames(Finder.getKeyMembers(retriever.type()));
		final Set<String> propNames = new HashSet<String>();

		for (int index = 1; index <= md.getColumnCount(); index++) {
		    final String propName = AbstractRetriever.decodePropertyName(md.getColumnLabel(index));
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


		final ResultSet rs2 = st.executeQuery(getKeyUniquenessCheckSql(retriever));

		if (rs2.next()) {
		    foundErrors = true;
		    logger.error("\n\tRetriever [" + retriever.getClass().getName() + "] for type [" + retriever.type().getName() + "] contains duplicates in terms of entity key " + keyPropNames + ". More details here:\n" + getKeyUniquenessCheckSql(retriever) + "\n");
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

    private void validateRetrievalSql() throws Exception {
	boolean foundErrors = false;
	final Connection conn = injector.getInstance(Connection.class);
	for (final IRetriever<?> ret : retrievers) {
	    if (validateRetrievalSql(ret, conn)) {
		foundErrors = true;
	    }
    	}
	conn.close();
	final int index = 0;
	if (foundErrors) {
	    throw new RuntimeException("Validation detected errors. Pls consult the log file for details");
	}
    }

    public void populateData() throws Exception {
	final List<Result> results1 = new ArrayList<Result>();

	final SessionFactory sFactory = hiberUtil.getSessionFactory();

	for (final IRetriever<? extends AbstractEntity> ret : retrievers) {
	    if (ret.splitProperty() == null) {
		final Connection conn = injector.getInstance(Connection.class);
		final Result result = ret.populateData(sFactory, conn, factory, errorDao, histDao, migrationRun, null);
		conn.close();
		results1.add(result);
	    } else {
		final List<Task> tasks = new ArrayList<Task>();

		for (final Iterator<Pair<Set<String>, Long>> iterator = DataMigrator.splitIntoBatches(populateData(ret), threadCount).iterator(); iterator.hasNext();) {
		    final Connection conn = injector.getInstance(Connection.class);
		    tasks.add(new Task("done.", injector, sFactory, conn, factory, migrationRun, ret.getClass(), iterator.next().getKey().toArray(new String[] {})));
		}

		final ExecutorService exec = Executors.newFixedThreadPool(tasks.size());
		final List<Future<String>> results = new ArrayList<Future<String>>(tasks.size());

		for (final Task task : tasks) {
		    results.add(exec.submit(task));
		}

		for (int index = 0; index < results.size(); index++) {
		    System.out.println(ret.getClass().getSimpleName() + " ... waiting for task with index " + index + " ... " + results.get(index).get());
		}

		exec.shutdown(); // do not forget to shutdown the threads

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
