package ua.com.fielden.platform.basic.autocompleter;

import static ua.com.fielden.platform.equery.equery.select;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.basic.IValueMatcher;
import ua.com.fielden.platform.dao.IEntityAggregatesDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.SyntheticEntity;
import ua.com.fielden.platform.equery.EntityAggregates;
import ua.com.fielden.platform.equery.fetch;
import ua.com.fielden.platform.equery.interfaces.IQueryModel;
import ua.com.fielden.platform.equery.interfaces.IQueryOrderedModel;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;

/**
 *
 * @author oleh
 *
 */
public class SyntheticEntityValueMatcher implements IValueMatcher<EntityAggregates> {

    private final IEntityAggregatesDao entityAggregatesDao;
    private final IQueryOrderedModel<EntityAggregates> defaultModel;
    private final String propertyParamName = "propertyNameFor_key";

    private int pageSize = 10;

    public SyntheticEntityValueMatcher(final IEntityAggregatesDao entityAggregatesDao, final Class<? extends SyntheticEntity> syntheticEntityClass) {
	this.entityAggregatesDao = entityAggregatesDao;

	final List<Field> properties = Finder.findProperties(syntheticEntityClass);
	properties.remove(Finder.getFieldByName(syntheticEntityClass, AbstractEntity.DESC));
	final List<Field> keys = Finder.getKeyMembers(syntheticEntityClass);
	properties.removeAll(keys);
	final List<IQueryModel<EntityAggregates>> propertiesModels = new ArrayList<IQueryModel<EntityAggregates>>();
	for (final Field propertyField : properties) {
	    if (AbstractEntity.class.isAssignableFrom(propertyField.getType())) {
		propertiesModels.add(createQueryModelFor(propertyField));
	    }
	}
	if (propertiesModels.size() > 0) {
	    defaultModel = select(propertiesModels.toArray(new IQueryModel[propertiesModels.size()])).yieldProp("key").yieldProp("desc").model(EntityAggregates.class);
	} else {
	    defaultModel = null;
	}
    }

    private IQueryModel<EntityAggregates> createQueryModelFor(final Field propertyField) {
	if (!AbstractEntity.class.isAssignableFrom(propertyField.getType())) {
	    return null;
	}
	String propertyName = "key";
	Class<?> propertyType = PropertyTypeDeterminator.determinePropertyType(propertyField.getType(), propertyName);
	while (AbstractEntity.class.isAssignableFrom(propertyType)) {
	    propertyName += ".key";
	    propertyType = PropertyTypeDeterminator.determinePropertyType(propertyType, "key");
	}
	return select((Class<AbstractEntity>) propertyField.getType()).where().prop(propertyName).like().param(propertyParamName).yieldProp(propertyName, "key").yieldProp("desc").model(EntityAggregates.class);
    }

    private fetch createJoinModel(final Class<? extends AbstractEntity> clazz) {
	if (AbstractEntity.class.isAssignableFrom(AnnotationReflector.getKeyType(clazz))) {
	    return new fetch(clazz).with("key", createJoinModel((Class<AbstractEntity>) AnnotationReflector.getKeyType(clazz)));
	} else {
	    return new fetch(clazz);
	}
    }

    @Override
    public List<EntityAggregates> findMatches(final String value) {
	defaultModel.setParamValue(propertyParamName, value);
	return entityAggregatesDao.getPage(defaultModel, null, 0, pageSize).data();
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
