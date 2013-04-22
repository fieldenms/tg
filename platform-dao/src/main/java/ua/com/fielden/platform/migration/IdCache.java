package ua.com.fielden.platform.migration;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import ua.com.fielden.platform.dao.DomainMetadataAnalyser;
import ua.com.fielden.platform.dao.EntityMetadata;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.persistence.HibernateUtil;

public class IdCache {
    private final Map<Class<?>, Map<Object, Integer>> cache = new HashMap<Class<?>, Map<Object, Integer>>();
    private final HibernateUtil hiberUtil;
    private final DomainMetadataAnalyser dma;

    public IdCache(final HibernateUtil hiberUtil, final DomainMetadataAnalyser dma) {
	this.hiberUtil = hiberUtil;
	this.dma = dma;
    }

    protected void registerCacheForType(final Class<? extends AbstractEntity<?>> entityType) {
	if (!cache.containsKey(entityType)) {
	    cache.put(entityType, new HashMap<Object, Integer>());
	}
    }

    protected Map<Object, Integer> getCacheForType(final Class<? extends AbstractEntity<?>> entityType) throws Exception {
	if (!cache.containsKey(entityType)) {
	    cache.put(entityType, retrieveData(entityType));
	}

	return cache.get(entityType);
    }

    private String prepareRetrievingSql(final Class<? extends AbstractEntity<?>> entityType) {
	final EntityMetadata<? extends AbstractEntity<?>> emd = dma.getEntityMetadata(entityType);
	return "SELECT _ID," + emd.getProps().get("key").getColumn().getName() + " FROM " + emd.getTable() + " ORDER BY 2";
    }

    private Map<Object, Integer> retrieveData(final Class<? extends AbstractEntity<?>> entityType) throws Exception {
	final Map<Object, Integer> result = new HashMap<>();
	hiberUtil.getSessionFactory().getCurrentSession().beginTransaction();
	final Connection targetConn = hiberUtil.getSessionFactory().getCurrentSession().connection();

	final Statement st = targetConn.createStatement();
	final ResultSet rs = st.executeQuery(prepareRetrievingSql(entityType));
	int index = 0;
	while (rs.next()) {
	    index = index + 1;
	    result.put(rs.getObject(2), rs.getInt(1));
	}
	rs.close();
	st.close();
	targetConn.close();
	return result;
    }
}