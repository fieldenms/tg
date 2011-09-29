package ua.com.fielden.platform.domaintree.impl;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.domaintree.ICalculatedProperty;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.utils.Pair;

/**
 * An {@link ICalculatedProperty} default implementation.
 *
 * @author TG Team
 *
 */
public class CalculatedProperty implements ICalculatedProperty {
    private static final long serialVersionUID = -8413970385471726648L;

    private final String path, name, pathAndName, originationPropertyName;
    private final Class<?> parentType, rootType;
    private final CalculatedPropertyCategory category;

    private String expression, title, desc;
    private Class<?> resultType;

    /**
     * Mainly used for serialisation.
     */
    protected CalculatedProperty() {
	this.rootType = null;
	this.pathAndName = null;
	this.category = null;
	this.path = null;
	this.name = null;
	this.originationPropertyName = null;
	this.parentType = null;
    }

    public CalculatedProperty(final Class<?> rootType, final String pathAndName, final ICalculatedProperty.CalculatedPropertyCategory category, final String originationPropertyName, final Class<?> resultType, final String expression, final String title, final String desc) {
	this.rootType = rootType;
	this.pathAndName = pathAndName;
	this.category = category;
	this.path = PropertyTypeDeterminator.isDotNotation(pathAndName) ? PropertyTypeDeterminator.penultAndLast(pathAndName).getKey() : "";
	DomainTreeEnhancer.validatePlace(new Pair<Class<?>, String>(this.rootType, this.path));
	this.name = PropertyTypeDeterminator.isDotNotation(pathAndName) ? PropertyTypeDeterminator.penultAndLast(pathAndName).getValue() : pathAndName;
	this.originationPropertyName = originationPropertyName;
	this.parentType = StringUtils.isEmpty(path) ? this.rootType : PropertyTypeDeterminator.determinePropertyType(this.rootType, path);
	this.expression = expression;
	this.title = title;
	this.desc = desc;
	this.resultType = resultType;
    }

    @Override
    public String getPath() {
	return path;
    }

    @Override
    public Class<?> getRootType() {
	return rootType;
    }

    @Override
    public String getName() {
	return name;
    }

    @Override
    public Class<?> getParentType() {
	return parentType;
    }

    @Override
    public String getExpression() {
	return expression;
    }

    @Override
    public ICalculatedProperty setExpression(final String expression) {
	this.expression = expression;
	return this;
    }

    @Override
    public Class<?> getResultType() {
	return resultType;
    }

    @Override
    public ICalculatedProperty setResultType(final Class<?> resultType) {
	this.resultType = resultType;
	return this;
    }

    @Override
    public String getTitle() {
	return title;
    }

    @Override
    public ICalculatedProperty setTitle(final String title) {
	this.title = title;
	return this;
    }

    @Override
    public String getDesc() {
	return desc;
    }

    @Override
    public ICalculatedProperty setDesc(final String desc) {
	this.desc = desc;
	return this;
    }

    @Override
    public String getOriginationPropertyName() {
	return originationPropertyName;
    }

    @Override
    public String getPathAndName() {
	return pathAndName;
    }

    @Override
    public ICalculatedProperty.CalculatedPropertyCategory getCategory() {
	return category;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((category == null) ? 0 : category.hashCode());
	result = prime * result + ((desc == null) ? 0 : desc.hashCode());
	result = prime * result + ((expression == null) ? 0 : expression.hashCode());
	result = prime * result + ((name == null) ? 0 : name.hashCode());
	result = prime * result + ((originationPropertyName == null) ? 0 : originationPropertyName.hashCode());
	result = prime * result + ((parentType == null) ? 0 : parentType.hashCode());
	result = prime * result + ((path == null) ? 0 : path.hashCode());
	result = prime * result + ((pathAndName == null) ? 0 : pathAndName.hashCode());
	result = prime * result + ((resultType == null) ? 0 : resultType.hashCode());
	result = prime * result + ((rootType == null) ? 0 : rootType.hashCode());
	result = prime * result + ((title == null) ? 0 : title.hashCode());
	return result;
    }

    @Override
    public boolean equals(final Object obj) {
	if (this == obj) {
	    return true;
	}
	if (obj == null) {
	    return false;
	}
	if (getClass() != obj.getClass()) {
	    return false;
	}
	final CalculatedProperty other = (CalculatedProperty) obj;
	if (category != other.category) {
	    return false;
	}
	if (desc == null) {
	    if (other.desc != null) {
		return false;
	    }
	} else if (!desc.equals(other.desc)) {
	    return false;
	}
	if (expression == null) {
	    if (other.expression != null) {
		return false;
	    }
	} else if (!expression.equals(other.expression)) {
	    return false;
	}
	if (name == null) {
	    if (other.name != null) {
		return false;
	    }
	} else if (!name.equals(other.name)) {
	    return false;
	}
	if (originationPropertyName == null) {
	    if (other.originationPropertyName != null) {
		return false;
	    }
	} else if (!originationPropertyName.equals(other.originationPropertyName)) {
	    return false;
	}
	if (parentType == null) {
	    if (other.parentType != null) {
		return false;
	    }
	} else if (!parentType.equals(other.parentType)) {
	    return false;
	}
	if (path == null) {
	    if (other.path != null) {
		return false;
	    }
	} else if (!path.equals(other.path)) {
	    return false;
	}
	if (pathAndName == null) {
	    if (other.pathAndName != null) {
		return false;
	    }
	} else if (!pathAndName.equals(other.pathAndName)) {
	    return false;
	}
	if (resultType == null) {
	    if (other.resultType != null) {
		return false;
	    }
	} else if (!resultType.equals(other.resultType)) {
	    return false;
	}
	if (rootType == null) {
	    if (other.rootType != null) {
		return false;
	    }
	} else if (!rootType.equals(other.rootType)) {
	    return false;
	}
	if (title == null) {
	    if (other.title != null) {
		return false;
	    }
	} else if (!title.equals(other.title)) {
	    return false;
	}
	return true;
    }
}