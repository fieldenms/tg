package ua.com.fielden.platform.migration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import ua.com.fielden.platform.dao.DomainMetadataAnalyser;
import ua.com.fielden.platform.dao.DynamicEntityDao;
import ua.com.fielden.platform.dao.EntityMetadata;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.persistence.HibernateUtil;
import ua.com.fielden.platform.reflection.Finder;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

public class IdCache {
    private final Map<Class<?>, Map<Object, Integer>> cache = new HashMap<Class<?>, Map<Object, Integer>>();
    private final HibernateUtil hiberUtil;
    private final DomainMetadataAnalyser dma;
    private DynamicEntityDao dynamicDao;


    public IdCache(final HibernateUtil hiberUtil, final DynamicEntityDao dynamicDao, final DomainMetadataAnalyser dma) {
	this.hiberUtil = hiberUtil;
	this.dynamicDao = dynamicDao;
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

    private SortedSet<String> getKeyFields(final Class<? extends AbstractEntity<?>> entityType) {
	final List<String> keyMembersFirstLevelProps = Finder.getFieldNames(Finder.getKeyMembers(entityType));
	return new TreeSet<String>(dma.getLeafPropsFromFirstLevelProps(null, entityType, new HashSet<String>(keyMembersFirstLevelProps)));
    }

    private Object prepareValueForCache(final AbstractEntity<?> entity, final SortedSet<String> fields) {
	if (fields.size() == 1) {
	    return entity.getKey();
	} else {
	    final List<Object> result = new ArrayList<>();
	    for (final String field : fields) {
		result.add(entity.get(field));
	    }
	    return result;
	}
    }

    private Map<Object, Integer> retrieveData(final Class<? extends AbstractEntity<?>> entityType) throws Exception {
	System.out.println("___ RETRIEVING DATA FOR CACHE FOR " + entityType.getSimpleName());
	final Map<Object, Integer> result = new HashMap<>();
	dynamicDao.setEntityType(entityType);
	final List<AbstractEntity> entities = dynamicDao.getAllEntities(from(select(entityType).model()).model());



	final SortedSet<String> keyFields = getKeyFields(entityType);
	for (final AbstractEntity abstractEntity : entities) {
	    //result.put(abstractEntity.getKey(), abstractEntity.getId().intValue());
	    result.put(prepareValueForCache(abstractEntity, keyFields), abstractEntity.getId().intValue());
	}

//	hiberUtil.getSessionFactory().getCurrentSession().beginTransaction();
//	final Connection targetConn = hiberUtil.getSessionFactory().getCurrentSession().connection();
//
//	final Statement st = targetConn.createStatement();
//	final ResultSet rs = st.executeQuery(prepareRetrievingSql(entityType));
//	int index = 0;
//	while (rs.next()) {
//	    index = index + 1;
//	    result.put(rs.getObject(2), rs.getInt(1));
//	}
//	rs.close();
//	st.close();
//	targetConn.close();
	return result;
    }
}