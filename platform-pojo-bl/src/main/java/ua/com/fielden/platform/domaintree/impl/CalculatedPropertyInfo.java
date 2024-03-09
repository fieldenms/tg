package ua.com.fielden.platform.domaintree.impl;

import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyAttribute;

public class CalculatedPropertyInfo {
    private final Class<?> root;
    private final String contextPath;
    private final String contextualExpression;
    private final String title;
    private final CalculatedPropertyAttribute attribute;
    private final String originationProperty;
    private final String desc;
    private final String customPropertyName;
    private final transient Integer precision;
    private final transient Integer scale;
    private final transient String path;
    private final transient String name;

    public CalculatedPropertyInfo() {
        root = null;
        contextPath = null;
        contextualExpression = null;
        title = null;
        attribute = null;
        originationProperty = null;
        desc = null;
        customPropertyName = null;
        precision = null;
        scale = null;
        path = null;
        name = null;
    }

    public CalculatedPropertyInfo(
            final Class<?> root,
            final String contextPath,
            final String customPropertyName,
            final String contextualExpression,
            final String title,
            final CalculatedPropertyAttribute attribute,
            final String originationProperty,
            final String desc,
            final Integer precision,
            final Integer scale,
            final String path,
            final String name) {
        this.root = root;
        this.contextPath = contextPath;
        this.customPropertyName = customPropertyName;
        this.contextualExpression = contextualExpression;
        this.title = title;
        this.attribute = attribute;
        this.originationProperty = originationProperty;
        this.desc = desc;
        this.precision = precision;
        this.scale = scale;
        this.path = path;
        this.name = name;
    }

    public Class<?> getRoot() {
        return root;
    }

    public String getContextPath() {
        return contextPath;
    }

    public String getCustomPropertyName() {
        return customPropertyName;
    }

    public String getContextualExpression() {
        return contextualExpression;
    }

    public String getTitle() {
        return title;
    }

    public CalculatedPropertyAttribute getAttribute() {
        return attribute;
    }

    public String getOriginationProperty() {
        return originationProperty;
    }

    public String getDesc() {
        return desc;
    }

    public Integer getPrecision() {
        return precision;
    }

    public Integer getScale() {
        return scale;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((attribute == null) ? 0 : attribute.hashCode());
        result = prime * result + ((contextPath == null) ? 0 : contextPath.hashCode());
        result = prime * result + ((customPropertyName == null) ? 0 : customPropertyName.hashCode());
        result = prime * result + ((contextualExpression == null) ? 0 : contextualExpression.hashCode());
        result = prime * result + ((desc == null) ? 0 : desc.hashCode());
        result = prime * result + ((originationProperty == null) ? 0 : originationProperty.hashCode());
        result = prime * result + ((root == null) ? 0 : root.hashCode());
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
        final CalculatedPropertyInfo other = (CalculatedPropertyInfo) obj;
        if (attribute != other.attribute) {
            return false;
        }
        if (contextPath == null) {
            if (other.contextPath != null) {
                return false;
            }
        } else if (!contextPath.equals(other.contextPath)) {
            return false;
        }
        if (customPropertyName == null) {
            if (other.customPropertyName != null) {
                return false;
            }
        } else if (!customPropertyName.equals(other.customPropertyName)) {
            return false;
        }
        if (contextualExpression == null) {
            if (other.contextualExpression != null) {
                return false;
            }
        } else if (!contextualExpression.equals(other.contextualExpression)) {
            return false;
        }
        if (desc == null) {
            if (other.desc != null) {
                return false;
            }
        } else if (!desc.equals(other.desc)) {
            return false;
        }
        if (originationProperty == null) {
            if (other.originationProperty != null) {
                return false;
            }
        } else if (!originationProperty.equals(other.originationProperty)) {
            return false;
        }
        if (root == null) {
            if (other.root != null) {
                return false;
            }
        } else if (!root.equals(other.root)) {
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

    public String path() {
        return path;
    }

    public String name() {
        return name;
    }

}