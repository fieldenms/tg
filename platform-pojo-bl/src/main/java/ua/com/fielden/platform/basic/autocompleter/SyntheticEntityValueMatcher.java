package ua.com.fielden.platform.basic.autocompleter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.basic.IValueMatcher;
import ua.com.fielden.platform.dao.IEntityAggregatesDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.SyntheticEntity;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetch;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

/**
 *
 * @author oleh
 *
 */
public class SyntheticEntityValueMatcher implements IValueMatcher<EntityAggregates> {

    private final IEntityAggregatesDao entityAggregatesDao;
    private final AggregatedResultQueryModel defaultModel;
    private final String propertyParamName = "propertyNameFor_key";

    private int pageSize = 10;

    public SyntheticEntityValueMatcher(final IEntityAggregatesDao entityAggregatesDao, final Class<? extends SyntheticEntity> syntheticEntityClass) {
	this.entityAggregatesDao = entityAggregatesDao;

	final List<Field> properties = Finder.findProperties(syntheticEntityClass);
	properties.remove(Finder.getFieldByName(syntheticEntityClass, AbstractEntity.DESC));
	final List<Field> keys = Finder.getKeyMembers(syntheticEntityClass);
	properties.removeAll(keys);
	final List<AggregatedResultQueryModel> propertiesModels = new ArrayList<AggregatedResultQueryModel>();
	for (final Field propertyField : properties) {
	    if (AbstractEntity.class.isAssignableFrom(propertyField.getType())) {
		propertiesModels.add(createQueryModelFor(propertyField));
	    }
	}
	if (propertiesModels.size() > 0) {
	    defaultModel = select(propertiesModels.toArray(new AggregatedResultQueryModel[propertiesModels.size()])).yield().prop("key").as("key").yield().prop("desc").as("desc").modelAsAggregate();
	    } else {
	    defaultModel = null;
	}
    }

    private AggregatedResultQueryModel createQueryModelFor(final Field propertyField) {
	if (!AbstractEntity.class.isAssignableFrom(propertyField.getType())) {
	    return null;
	}
	String propertyName = "key";
	Class<?> propertyType = PropertyTypeDeterminator.determinePropertyType(propertyField.getType(), propertyName);
	while (AbstractEntity.class.isAssignableFrom(propertyType)) {
	    propertyName += ".key";
	    propertyType = PropertyTypeDeterminator.determinePropertyType(propertyType, "key");
	}
	return select((Class<AbstractEntity>) propertyField.getType()).where().prop(propertyName).like().param(propertyParamName).yield().prop(propertyName).as("key").yield().prop("desc").as("desc").modelAsAggregate();
    }

    private fetch createJoinModel(final Class<? extends AbstractEntity> clazz) {
	if (AbstractEntity.class.isAssignableFrom(AnnotationReflector.getKeyType(clazz))) {
	    return fetch(clazz).with("key", createJoinModel((Class<AbstractEntity>) AnnotationReflector.getKeyType(clazz)));
	} else {
	    return fetch(clazz);
	}
    }

    @Override
    public List<EntityAggregates> findMatches(final String value) {
	return entityAggregatesDao.getPage(from(defaultModel).with(propertyParamName, value).model(), 0, pageSize).data();
    }

    @Override
    public List<EntityAggregates> findMatchesWithModel(final String value) {
	throw new UnsupportedOperationException("findMatchesWithModel for the SyntheticEntityValueMatcher is unsupported");
    }

    @Override
    public void setFetchModel(final fetch fetchModel) {
	throw new UnsupportedOperationException("setQueryModel for the SyntheticEntityValueMatcher is unsupported");
    }

    @Override
    public fetch<?> getFetchModel() {
	throw new UnsupportedOperationException("getQueryModel for the SyntheticEntityValueMatcher is unsupported");
    }

    public void setPageSize(final int pageSize) {
	this.pageSize = pageSize;
    }

    @Override
    public Integer getPageSize() {
	return pageSize;
    }
}