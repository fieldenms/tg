package ua.com.fielden.platform.processors.metamodel.concepts;

import static java.lang.String.format;

import java.util.Objects;

import com.squareup.javapoet.ClassName;

import ua.com.fielden.platform.processors.metamodel.MetaModelConstants;
import ua.com.fielden.platform.processors.metamodel.elements.EntityElement;
import ua.com.fielden.platform.processors.metamodel.elements.MetaModelElement;

/**
 * This class represents an abstraction for a meta-model that needs to be generated as opposed to a meta-model that was already generated. The latter is represented by {@link MetaModelElement}. 
 *
 * @author TG Team
 *
 */
public final class MetaModelConcept {
    private final EntityElement entityElement;
    private final String simpleName;
    private final String aliasedSimpleName;
    private final String packageName;
    private final String qualifiedName;

    public MetaModelConcept(final EntityElement entityElement) {
        this.entityElement = entityElement;
        this.simpleName = entityElement.getSimpleName() + MetaModelConstants.META_MODEL_NAME_SUFFIX;
        this.aliasedSimpleName = entityElement.getSimpleName() + MetaModelConstants.META_MODEL_ALIASED_NAME_SUFFIX;
        this.packageName = entityElement.getPackageName() + MetaModelConstants.META_MODEL_PKG_NAME_SUFFIX;
        this.qualifiedName = format("%s.%s", packageName, simpleName);
    }

    public EntityElement getEntityElement() {
        return entityElement;
    }

    public String getSimpleName() {
        return simpleName;
    }
    
    public String getAliasedSimpleName() {
        return aliasedSimpleName;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getQualifiedName() {
        return qualifiedName;
    }

    public ClassName getMetaModelClassName() {
        return ClassName.get(packageName, simpleName);
    }
    
    public ClassName getMetaModelAliasedClassName() {
        return ClassName.get(packageName, aliasedSimpleName);
    }

    @Override
    public int hashCode() {
        return 31 + Objects.hash(getQualifiedName());
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MetaModelConcept)) {
            return false;
        }
        final MetaModelConcept that = (MetaModelConcept) obj;
        return Objects.equals(this.qualifiedName, that.qualifiedName);
    }

}