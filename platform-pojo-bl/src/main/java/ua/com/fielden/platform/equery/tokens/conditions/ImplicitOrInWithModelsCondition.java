package ua.com.fielden.platform.equery.tokens.conditions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.equery.LogicalOperator;
import ua.com.fielden.platform.equery.QueryModel;
import ua.com.fielden.platform.equery.RootEntityMapper;
import ua.com.fielden.platform.equery.interfaces.IQueryModel;
import ua.com.fielden.platform.equery.tokens.properties.SearchProperty;

public final class ImplicitOrInWithModelsCondition extends ConditionOnProperty<ImplicitOrInWithModelsCondition> {
    private final ArrayList<IQueryModel<? extends AbstractEntity>> models = new ArrayList<IQueryModel<? extends AbstractEntity>>();
    private final boolean negated;

    /**
     * Mainly used for serialisation.
     */
    protected ImplicitOrInWithModelsCondition() {
	negated = false;
    }

    public ImplicitOrInWithModelsCondition(final LogicalOperator logicalOperator, final boolean negated, final SearchProperty property, final IQueryModel<? extends AbstractEntity>... otherValues) {
	setLogicalOperator(logicalOperator);
	setProperty(property);
	this.negated = negated;

	for (final IQueryModel<? extends AbstractEntity> queryModel : otherValues) {
	    if (queryModel != null) {
		models.add(((QueryModel) queryModel).clon());
	    }
	}
    }

    private ImplicitOrInWithModelsCondition(final LogicalOperator logicalOperator, final boolean negated, final SearchProperty property, final List<IQueryModel<? extends AbstractEntity>> models) {
	setLogicalOperator(logicalOperator);
	setProperty(property);
	this.negated = negated;
	for (final IQueryModel<? extends AbstractEntity> queryModel : models) {
	    this.models.add(((QueryModel) queryModel).clon());
	}
    }

    public List<IQueryModel<? extends AbstractEntity>> getModels() {
	return models;
    }

    @Override
    public String getSql(final RootEntityMapper alias) {
	final StringBuffer sb = new StringBuffer();
	sb.append(models.size() > 1 ? "(" : "");
	for (final Iterator<IQueryModel<? extends AbstractEntity>> iterator = models.iterator(); iterator.hasNext();) {
	    final IQueryModel<? extends AbstractEntity> model = iterator.next();
	    sb.append(getProperty().getSql(alias));
	    sb.append(negated ? " NOT" : "");
	    sb.append(" IN (");
	    sb.append(model.getModelResult(alias.getMappingExtractor()).getSql());
	    sb.append(")");

	    if (iterator.hasNext()) {
		sb.append(" OR ");
	    }
	}

	sb.append(models.size() > 1 ? ")" : "");
	return sb.toString();
    }

    public ImplicitOrInWithModelsCondition clon() {
	final ImplicitOrInWithModelsCondition clon = new ImplicitOrInWithModelsCondition(getLogicalOperator(), negated, getProperty().clon(), models);
	return clon;
    }
}
