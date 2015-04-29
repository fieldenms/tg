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
}
