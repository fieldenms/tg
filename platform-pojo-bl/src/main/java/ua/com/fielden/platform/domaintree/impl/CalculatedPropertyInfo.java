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

    public CalculatedPropertyInfo() {
        root = null;
        contextPath = null;
        contextualExpression = null;
        title = null;
        attribute = null;
        originationProperty = null;
        desc = null;
        customPropertyName = null;
    }

    public CalculatedPropertyInfo(final Class<?> root, final String contextPath, final String customPropertyName, final String contextualExpression, final String title, final CalculatedPropertyAttribute attribute, final String originationProperty, final String desc) {
        this.root = root;
        this.contextPath = contextPath;
        this.customPropertyName = customPropertyName;
        this.contextualExpression = contextualExpression;
        this.title = title;
        this.attribute = attribute;
        this.originationProperty = originationProperty;
        this.desc = desc;
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

    //    /**
    //     * A specific Kryo serialiser for {@link CalculatedPropertyInfo}.
    //     *
    //     * @author TG Team
    //     *
    //     */
    //    public static class CalculatedPropertyInfoSerialiser extends AbstractDomainTreeSerialiser<CalculatedPropertyInfo> {
    //	public CalculatedPropertyInfoSerialiser(final TgKryo kryo) {
    //	    super(kryo);
    //	}
    //
    //	@Override
    //	public CalculatedPropertyInfo read(final ByteBuffer buffer) {
    //	    final Class<?> root = readValue(buffer, Class.class);
    //	    final String contextPath = readValue(buffer, String.class);
    //	    final String contextualExpression = readValue(buffer, String.class);
    //	    final String title = readValue(buffer, String.class);
    //	    final CalculatedPropertyAttribute attribute = readValue(buffer, CalculatedPropertyAttribute.class);
    //	    final String originationProperty = readValue(buffer, String.class);
    //	    final String desc = readValue(buffer, String.class);
    //	    return new CalculatedPropertyInfo(root, contextPath, contextualExpression, title, attribute, originationProperty, desc);
    //	}
    //
    //	@Override
    //	public void write(final ByteBuffer buffer, final CalculatedPropertyInfo calculatedPropertyInfo) {
    //	    writeValue(buffer, calculatedPropertyInfo.root);
    //	    writeValue(buffer, calculatedPropertyInfo.contextPath);
    //	    writeValue(buffer, calculatedPropertyInfo.contextualExpression);
    //	    writeValue(buffer, calculatedPropertyInfo.title);
    //	    writeValue(buffer, calculatedPropertyInfo.attribute);
    //	    writeValue(buffer, calculatedPropertyInfo.originationProperty);
    //	    writeValue(buffer, calculatedPropertyInfo.getDesc());
    //	}
    //    }

}
