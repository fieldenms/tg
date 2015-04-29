package ua.com.fielden.platform.domaintree.impl;

import ua.com.fielden.platform.domaintree.IProperty;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;

/**
 * The holder of 'custom property' information.
 *
 * @author TG Team
 *
 */
public class CustomProperty implements IProperty {
    private final Class<?> root;
    private final String contextPath;
    private final String name;
    private final String title;
    private final String desc;
    private final Class<?> type;

    public CustomProperty() {
        this.root = null;
        this.contextPath = null;
        this.name = null;
        this.title = null;
        this.desc = null;
        this.type = null;
    }

    public CustomProperty(final Class<?> root, final Class<?> managedType, final String contextPath, final String name, final String title, final String desc, final Class<?> type) {
        this.root = root;

        final Result namesValidated = validateNames(contextPath, name, managedType);
        if (!namesValidated.isSuccessful()) {
            throw namesValidated;
        }

        this.contextPath = contextPath;
        this.name = name;
        this.title = title;
        this.desc = desc;
        this.type = type;
    }

    private Result validateNames(final String contextPath, final String name, final Class<?> managedType) {
        final String dotNotatedName = "".equals(contextPath) ? name : (contextPath + "." + name);

        if (!"".equals(contextPath)) {
            try {
                PropertyTypeDeterminator.determinePropertyType(managedType, contextPath);
            } catch (final IllegalArgumentException e) {
                return Result.failure(e);
            }
        }

        if (!name.equals(CalculatedProperty.generateNameFrom(name))) {
            return Result.failure(String.format("The name of the property [%s] does not conform to Java naming standarts (cropped name will be [%s]).", name, CalculatedProperty.generateNameFrom(name)));
        }

        try {
            PropertyTypeDeterminator.determinePropertyType(managedType, dotNotatedName);
            return Result.failure(String.format("There is already the property with such a name [%s] in managed entity type.", name));
        } catch (final IllegalArgumentException e) {
            return Result.successful("Ok");
        }
    }

    @Override
    public Class<?> getRoot() {
        return root;
    }

    @Override
    public String getContextPath() {
        return contextPath;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getDesc() {
        return desc;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Class<?> resultType() {
        return type;
    }

    @Override
    public String path() {
        return getContextPath();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((contextPath == null) ? 0 : contextPath.hashCode());
        result = prime * result + ((desc == null) ? 0 : desc.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((root == null) ? 0 : root.hashCode());
        result = prime * result + ((title == null) ? 0 : title.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
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
        final CustomProperty other = (CustomProperty) obj;
        if (contextPath == null) {
            if (other.contextPath != null) {
                return false;
            }
        } else if (!contextPath.equals(other.contextPath)) {
            return false;
        }
        if (desc == null) {
            if (other.desc != null) {
                return false;
            }
        } else if (!desc.equals(other.desc)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
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
        if (type == null) {
            if (other.type != null) {
                return false;
            }
        } else if (!type.equals(other.type)) {
            return false;
        }
        return true;
    }
}
