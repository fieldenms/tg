package ua.com.fielden.platform.migration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;

import org.apache.log4j.Logger;
import org.hibernate.Transaction;
import org.joda.time.DateTime;
import org.joda.time.Period;

import ua.com.fielden.platform.dao.DomainMetadata;
import ua.com.fielden.platform.dao.DomainMetadataAnalyser;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.migration.RetrieverPropsValidator.RetrievedPropValidationError;
import ua.com.fielden.platform.persistence.HibernateUtil;
import ua.com.fielden.platform.security.user.User;

import com.google.inject.Injector;

public class DataMigrator {
    private final Logger logger = Logger.getLogger(this.getClass());

    private final HibernateUtil hiberUtil;
    private final EntityFactory factory;
    private final List<IRetriever<? extends AbstractEntity<?>>> retrievers = new ArrayList<IRetriever<? extends AbstractEntity<?>>>();
    private final Injector injector;
    private final DomainMetadataAnalyser dma;
    private final boolean includeDetails;
    private final Map<Class<?>, Map<Object, Integer>> cache = new HashMap<Class<?>, Map<Object, Integer>>();

    private static List<IRetriever<? extends AbstractEntity<?>>> instantiateRetrievers(final Injector injector, final Class... retrieversClasses) {
	final List<IRetriever<? extends AbstractEntity<?>>> result = new ArrayList<IRetriever<? extends AbstractEntity<?>>>();
	for (final Class<? extends IRetriever<? extends AbstractEntity<?>>> retrieverClass : retrieversClasses) {
	    result.add(injector.getInstance(retrieverClass));
	}
	return result;
    }

    public DataMigrator(final Injector injector, final HibernateUtil hiberUtil, final EntityFactory factory, final boolean includeDetails, final Class<? extends AbstractEntity<?>> personClass, final Class... retrieversClasses)
	    throws Exception {
	this.injector = injector;
	this.factory = factory;
	this.hiberUtil = hiberUtil;
	this.dma = new DomainMetadataAnalyser(injector.getInstance(DomainMetadata.class));
	this.retrievers.addAll(instantiateRetrievers(injector, retrieversClasses));
	this.includeDetails = includeDetails;

	for (final IRetriever<? extends AbstractEntity<?>> ret : retrievers) {
	    logger.debug("Checking props for [" + ret.getClass().getSimpleName() + "]");
	    final SortedMap<String, RetrievedPropValidationError> checkResult = new RetrieverPropsValidator(dma, ret).validate();
	    if (checkResult.size() > 0) {
		logger.error("The following issues have been revealed for props in [" + ret.getClass().getSimpleName() + "]:\n " + checkResult);
	    }
	}

	//	for (final EntityMetadata<? extends AbstractEntity<?>> emd : dma.getDomainMetadata().getEntityMetadataMap().values()) {
	//	    for (final PropertyMetadata pmd : emd.getProps().values()) {
	//		if (pmd.getJavaType() == boolean.class) {
	//		    System.out.println(emd.getType().getSimpleName() + "." + pmd.getName());
	//		}
	//	    }
	//	}

	final Connection conn = injector.getInstance(Connection.class);
	checkEmptyStrings(dma, conn);
	checkRequiredness(dma, conn);
	checkDataIntegrity(dma, conn);
	validateRetrievalSql(dma);

	final DateTime start = new DateTime();
	final Integer finalId = batchInsert(dma, conn, 0, personClass);
	final Period pd = new Period(start, new DateTime());

	runSql(new ArrayList<String>() {
	    {
		add("UPDATE UNIQUE_ID SET NEXT_VALUE = " + finalId + " WHERE _ID = 1");
		add("UPDATE NUMBERS SET WONOINC = (SELECT CAST(MAX(KEY_) AS INTEGER) + 1 FROM WODET) WHERE _ID = 0");
	    }
	});

	System.out.println("Migration duration: " + pd.getMinutes() + " m " + pd.getSeconds() + " s " + pd.getMillis() + " ms");
    }

    /**
     * Checks the correctness of the legacy data retrieval sql syntax and column aliases.
     *
     * @return
     * @throws Exception
     */
    private boolean validateRetrievalSqlForKeyFieldsUniqueness(final DomainMetadataAnalyser dma, final IRetriever<? extends AbstractEntity<?>> retriever, final Connection conn)
	    throws Exception {
	final Statement st = conn.createStatement();
	final String sql = new RetrieverSqlProducer(dma).getKeyUniquenessViolationSql(retriever);
	boolean result = false;
	try {
	    logger.debug("Checking uniqueness of key data for [" + retriever.getClass().getSimpleName() + "]");
	    final ResultSet rs = st.executeQuery(sql);
	    if (rs.next()) {
		logger.error("There are duplicates in data of [" + retriever.getClass().getSimpleName() + "].\n" + (includeDetails ? sql + "\n\n\n" : ""));
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
    private boolean checkRetrievalSqlForSyntaxErrors(final DomainMetadataAnalyser dma, final IRetriever<? extends AbstractEntity<?>> retriever, final Connection conn)
	    throws Exception {
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
		    logger.error("Dead references count for entity type [" + entry.getKey().getSimpleName() + "] is [" + count + "].\n"
			    + (includeDetails ? entry.getValue() + "\n\n\n" : ""));
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
		    logger.error("Empty string reference count for property [" + prop + "] within retriever [" + retriever + "] is [" + count + "].\n"
			    + (includeDetails ? sql + "\n\n\n" : ""));
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

    private boolean checkRequiredness(final DomainMetadataAnalyser dma, final Connection conn) throws Exception {
	final Set<String> stmts = new RetrieverPropsRequirednessChecker(dma).getSqls(retrievers);
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
		    logger.error("Violated requiredness records count for property [" + prop + "] within retriever [" + retriever + "] is [" + count + "].\n"
			    + (includeDetails ? sql + "\n\n\n" : ""));
		}
		rs.close();
	    } catch (final Exception ex) {
		logger.error("Exception while counting records with violated requiredness with SQL:\n" + sql);
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
	    logger.error("\n\n\n======== Validation detected errors. Pls consult the log file for details ==========\n\n\n");
	}
    }

    private Integer batchInsert(final DomainMetadataAnalyser dma, final Connection conn, final int startingId, final Class<? extends AbstractEntity<?>> personClass)
	    throws Exception {
	final RetrieverSqlProducer rsp = new RetrieverSqlProducer(dma);
	Integer id = startingId;
	final Map<Object, Integer> userTypeCache = new HashMap<>();
	cache.put(User.class, userTypeCache);

	for (final IRetriever<? extends AbstractEntity<?>> retriever : retrievers) {
	    final RetrieverBatchStmtGenerator rbsg = new RetrieverBatchStmtGenerator(dma, retriever);
	    final String sql = rsp.getSql(retriever);
	    final Map<String, Integer> exceptions = new HashMap<>();
	    final Map<Object, Integer> existingTypeCache = cache.get(retriever.type());
	    if (existingTypeCache == null) {
		cache.put(retriever.type(), new HashMap<Object, Integer>());
	    }
	    final Map<Object, Integer> typeCache = cache.get(retriever.type());
	    final DateTime start = new DateTime();
	    final Statement st = conn.createStatement();
	    final String insertSql = rbsg.getInsertStmt();
	    final List<Integer> indexFields = rbsg.produceKeyFieldsIndices();
	    try {
		final ResultSet rs = st.executeQuery(sql);
		final Transaction tr = hiberUtil.getSessionFactory().getCurrentSession().beginTransaction();
		final Connection conn2 = hiberUtil.getSessionFactory().getCurrentSession().connection();
		final PreparedStatement st2 = conn2.prepareStatement(insertSql);
		while (rs.next()) {
		    id = id + 1;
		    final List<Object> keyValue = new ArrayList<>();
		    for (final Integer keyIndex : indexFields) {
			keyValue.add(rs.getObject(keyIndex.intValue()));
		    }
		    typeCache.put(keyValue.size() == 1 ? keyValue.get(0) : keyValue, id);
		    if (retriever.type().equals(personClass)) {
			userTypeCache.put(rs.getObject("username"), id);
		    }
		    int index = 1;
		    for (final Object value : rbsg.transformValues(rs, cache, id)) {
			st2.setObject(index, value);
			index = index + 1;
		    }
		    st2.addBatch();

		    if ((id % 100) == 0) {
			try {
			    st2.executeBatch();
			} catch (final Exception ex) {
			    final Integer prevValue = exceptions.get(ex.toString());
			    exceptions.put(ex.toString(), (prevValue != null ? prevValue : 0) + 1);
			    //logger.error("Exception while performing batch insert of [" + retriever.getClass().getSimpleName() + "] " + ex + ". SQL:\n" + insertSql);
			} finally {
			    st2.clearBatch();
			}
		    }
		}
		if ((id % 100) != 0) {
		    st2.executeBatch();
		}

		tr.commit();
		st2.close();
		rs.close();
	    } catch (final Exception ex) {
		final Integer prevValue = exceptions.get(ex.toString());
		exceptions.put(ex.toString(), (prevValue != null ? prevValue : 0) + 1);
		//logger.error("Exception while performing batch insert of [" + retriever.getClass().getSimpleName() + "] " + ex + ". SQL:\n" + insertSql);
	    } finally {
		st.close();
	    }
	    final Period pd = new Period(start, new DateTime());
	    System.out.println(retriever.getClass().getSimpleName() + " -- duration: " + pd.getMinutes() + " m " + pd.getSeconds() + " s " + pd.getMillis()
		    + " ms. Entities count: " + typeCache.size());
	    if (exceptions.size() > 0) {
		System.out.println("       " + retriever.getClass().getSimpleName() + " -- SQL: " + insertSql);
		System.out.println("       " + retriever.getClass().getSimpleName() + " -- exceptions: ");
		for (final Entry<String, Integer> entry : exceptions.entrySet()) {
		    System.out.println("                 (" + entry.getValue() + ") -- " + entry.getKey());
		}
	    }

	}
	return id;
    }
}