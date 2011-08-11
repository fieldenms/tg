package ua.com.fielden.platform.equery.tokens.main;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.equery.EntityAggregates;
import ua.com.fielden.platform.equery.QueryModel;
import ua.com.fielden.platform.equery.RootEntityMapper;
import ua.com.fielden.platform.equery.interfaces.IClon;
import ua.com.fielden.platform.equery.interfaces.IFilter;
import ua.com.fielden.platform.equery.interfaces.IMain.ICompletedAndYielded;
import ua.com.fielden.platform.equery.interfaces.IQueryModel;
import ua.com.fielden.platform.equery.interfaces.IQueryToken;
import ua.com.fielden.platform.reflection.Finder;

import static ua.com.fielden.platform.equery.equery.select;

public final class QuerySource implements IQueryToken, IClon<QuerySource> {
    private Class entityType;
    private final ArrayList<IQueryModel> models = new ArrayList<IQueryModel>();

    /**
     * This constructor is required mainly for serialisation.
     * @param expression
     */
    public QuerySource() {
    }

    public QuerySource(final Class entityType) {
	this.entityType = entityType;

	if (AbstractUnionEntity.class.isAssignableFrom(entityType)) {
	    models.addAll(generateModelResult(entityType));
	}
    }

    private List<IQueryModel> generateModelResult(final Class<? extends AbstractUnionEntity> propType) {
	final List<String> commons = AbstractUnionEntity.commonProperties(propType);
	final List<Field> unions = AbstractUnionEntity.unionProperties(propType);
	final List<IQueryModel> models = new ArrayList<IQueryModel>();

	for (final Field field : unions) {
	    System.out.println("\n");
	    ICompletedAndYielded qry = select((Class<? extends AbstractEntity>) field.getType()).yieldValue(field.getType().getName(), "_class");
	    final List<Field> keyMembers = Finder.getKeyMembers(field.getType());
	    if (keyMembers.size() == 1) {
		qry = qry.yieldProp(keyMembers.get(0).getName(), "_key");
	    } else {
		throw new RuntimeException("Handling of entities with composite key is not yet implemented");
	    }

	    for (final String prop : commons) {
		// TODO skip collections
		qry = qry.yieldProp(prop, "_" + prop);
		//System.out.println(" yielding: " + prop + " as _" + prop);
	    }
	    for (final Field field2 : unions) {
		final List<Field> props = Finder.findProperties(field2.getType());
		for (final Field field3 : props) {
		    if (!Collection.class.isAssignableFrom(field3.getType())) {
			if (field2 == field) {
			    qry = qry.yieldProp(field3.getName(), field2.getName() + "_" + field3.getName());
			    //System.out.println(" yielding: " + field3.getName() + " as " + field2.getName() + "_" + field3.getName());
			} else {
			    qry = qry.yieldValue(null, field2.getName() + "_" + field3.getName());
			    //System.out.println(" yielding: null as " + field2.getName() + "_" + field3.getName());
			}
		    } else {
			//System.out.println(" skipping collection  here: " + field3.getName());
		    }
		}
	    }
	    models.add(qry.model(EntityAggregates.class));
	}

	return models;
    }

    public QuerySource(final IQueryModel... sourceModels) {
	for (final IQueryModel queryModel : sourceModels) {
	    this.models.add(queryModel);
	}
	entityType = null;
    }

    public void applyFilter(final IFilter filter, final String userName) {
	if (isEntityTypeBased()) {
	    final IQueryModel model = filter.enhance(entityType, userName);
	    if (model != null) {
		models.add(model);
		entityType = null;
	    }
	}
	else {
	    final List<IQueryModel> filtered = new ArrayList<IQueryModel>();

	    for (final IQueryModel iQueryModel : models) {
		final QueryModel model = ((QueryModel) iQueryModel).clon();
		filtered.add((QueryModel)model.enhanceWith(filter, userName));
	    }

	    models.clear();
	    models.addAll(filtered);
	}
    }

    public boolean isEntityTypeBased() {
	return entityType != null;
    }

    public boolean isUnionEntityTypeBased() {
	return entityType != null && models.size() > 0;
    }

    @Override
    public String getSql(final RootEntityMapper alias) {
	final StringBuffer sb = new StringBuffer();
	//	if (properties.size() > 0) {
	//	    sb.append("\n   ORDER BY ");
	//
	//	    for (final Iterator<OrderByProperty> iterator = properties.iterator(); iterator.hasNext();) {
	//		final OrderByProperty property = iterator.next();
	//		sb.append(property.getSql(alias));
	//		if (iterator.hasNext()) {
	//		    sb.append(", ");
	//		}
	//	    }
	//	}
	return sb.toString();
    }

    @Override
    public QuerySource clon() {
	if (isEntityTypeBased()) {
	    return new QuerySource(entityType);
	} else {
	    final List<IQueryModel> cloned = new ArrayList<IQueryModel>();
	    for (final IQueryModel model : models) {
		cloned.add(((QueryModel) model).clon());
	    }
	    return new QuerySource(cloned.toArray(new IQueryModel[] {}));
	}
    }

    public Class getEntityType() {
	return entityType != null ? entityType : models.get(0).getType();// EntityAggregates.class;
    }

    public List<IQueryModel> getModels() {
	return models;
    }
}
