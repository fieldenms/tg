package ua.com.fielden.platform.equery.tokens.properties;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.equery.QueryModel;
import ua.com.fielden.platform.equery.QueryParameter;
import ua.com.fielden.platform.equery.RootEntityMapper;
import ua.com.fielden.platform.equery.interfaces.IQueryModel;
import ua.com.fielden.platform.equery.interfaces.IQueryToken;

public abstract class AbstractQueryProperty implements IQueryToken {
    private final ArrayList<Object> expression = new ArrayList<Object>();

    /**
     * This constructor is required mainly for serialisation.
     * @param expression
     */
    protected AbstractQueryProperty() {
    }

    public List<Object> getExpression() {
	return expression;
    }

    abstract PropertyOrigin getPropertyOrigin();

    public List<QueryParameter> getParams() {
	final List<QueryParameter> result = new ArrayList<QueryParameter>();
	for (final Object token : expression) {
	    if (token instanceof QueryParameter) {
		result.add((QueryParameter) token);
	    }
	}
	return result;
    }

    private List<Object> clonExpression() {
	final List<Object> clones = new ArrayList<Object>();
	for (final Object token : expression) {
	    if (token instanceof QueryParameter) {
		clones.add(((QueryParameter) token).clon());
	    } else if (token instanceof QueryModel) {
		clones.add(((QueryModel) token).clon());
	    } else {
		clones.add(token);
	    }
	}

	return clones;
    }

    public List<IQueryModel<? extends AbstractEntity>> getModels() {
	final List<IQueryModel<? extends AbstractEntity>> result = new ArrayList<IQueryModel<? extends AbstractEntity>>();
	for (final Object token : expression) {
	    if (token instanceof IQueryModel) {
		result.add((IQueryModel<? extends AbstractEntity>) token);
	    }
	}
	return result;

    }

    public String getProp() {
	if (isProp()) {
	    return expression.get(0).toString();
	} else {
	    throw new RuntimeException("Is not a property");
	}
    }

    public boolean isExp() {
	return expression.size() > 1;
    }

    public boolean isProp() {
	return expression.size() == 1 && expression.get(0) instanceof EntityProperty;
    }

    public boolean isParam() {
	return expression.size() == 1 && expression.get(0) instanceof QueryParameter;
    }

    public boolean isModel() {
	return expression.size() == 1 && expression.get(0) instanceof QueryModel;
    }

    protected AbstractQueryProperty(final AbstractQueryProperty original) {
	getExpression().addAll(original.clonExpression());
    }

    public AbstractQueryProperty(final String rawValue, final Object... values) {
	final String propertyExpression = rawValue;
	int lastFinishIndex = 0;
	int lastStartIndex = propertyExpression.indexOf("[", lastFinishIndex);

	while (lastStartIndex != -1) {
	    final String expStr = propertyExpression.substring(lastFinishIndex == 0 ? 0 : lastFinishIndex + 1, lastStartIndex);
	    if (StringUtils.isNotBlank(expStr)) {
		expression.add(new ExpressionString(expStr));
	    }
	    lastFinishIndex = propertyExpression.indexOf("]", lastStartIndex);
	    final String expressionValue = propertyExpression.substring(lastStartIndex + 1, lastFinishIndex);
	    if (StringUtils.isNotEmpty(expressionValue)) {
		if (expressionValue.startsWith(":")) {
		    final QueryParameter param = new QueryParameter(expressionValue.substring(1), null);
		    expression.add(param);
		} else if (StringUtils.isNumeric(expressionValue)) {
		    final Integer index = Integer.parseInt(expressionValue);
		    if (values != null && values.length >= index) {
			final Object value = values[index - 1];
			if (value instanceof IQueryModel) {
			    //models.add((IQueryModel) value);
			    expression.add(value);
			} else {
			    final QueryParameter param = new QueryParameter(null, value);
			    expression.add(param);
			}
		    } else {
			throw new RuntimeException("Mismatch between index in expression and arguments provided: index is " + index + " but arguments count is " + values.length);
		    }
		} else {
		    // there is no need to specify ''id'' explicitly. But if specified - should be removed in order to be processed correctly.
		    final EntityProperty prop = new EntityProperty(getPropertyOrigin() != PropertyOrigin.SELECT ? expressionValue.replace(".id", "") : expressionValue);
		    expression.add(prop);
		}
	    } else {
		throw new RuntimeException("Incorrect expression: " + rawValue);
	    }

	    lastStartIndex = propertyExpression.indexOf("[", lastFinishIndex);

	}
	if (lastFinishIndex != propertyExpression.length()) {
	    final String expStr = propertyExpression.substring(lastFinishIndex == 0 ? 0 : lastFinishIndex + 1);
	    if (StringUtils.isNotBlank(expStr)) {
		expression.add(new ExpressionString(expStr));
	    }
	}

	//System.out.println("              !!!!!!!!!!!!!!!!!!!!!!             " + rawValue + "                 == " + expression);
    }

    public static final class ExpressionString {
	private String value;

	/** Used for serialisation. */
	protected ExpressionString() {
	}

	private ExpressionString(final String value) {
	    this.value = value;
	}

	@Override
	public String toString() {
	    return value;
	}
    }

    public static final class EntityProperty {
	private String value;

	/** Used for serialisation. */
	protected EntityProperty() {
	}

	private EntityProperty(final String value) {
	    this.value = value;
	}

	@Override
	public String toString() {
	    return value;
	}
    }

    public String getSql(final RootEntityMapper entityMapper) {
	final StringBuffer sb = new StringBuffer();
	for (final Object expressionPart : expression) {
	    if (expressionPart instanceof EntityProperty) {
		sb.append(entityMapper.getAliasedProperty(getPropertyOrigin(), expressionPart.toString()));
	    } else if (expressionPart instanceof QueryParameter) {
		sb.append(((QueryParameter) expressionPart).getValue());
	    } else if (expressionPart instanceof IQueryModel) {
		sb.append("(");
		sb.append(((QueryModel) expressionPart).getModelResult(entityMapper.getMappingExtractor()).getSql());
		sb.append(")");
	    } else if (expressionPart instanceof ExpressionString) {
		sb.append(expressionPart.toString());
	    }
	}

	return sb.toString();
    }

    @Override
    public String toString() {
	return expression + " [" + getPropertyOrigin() + "] ";
    }
}
