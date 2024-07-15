package ua.com.fielden.platform.processors.metamodel.elements;

import com.squareup.javapoet.ClassName;
import ua.com.fielden.platform.processors.metamodel.concepts.MetaModelConcept;

import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

/**
 * Represents a source code of a meta-model for a corresponding domain entity.
 *
 * @author TG Team 
 */
public final class MetaModelElement extends AbstractForwardingTypeElement {
    private final String packageName;
    
    public MetaModelElement(final TypeElement typeElement, final String packageName) {
        super(typeElement);
        this.packageName = packageName;
    }
    
    public String getPackageName() {
        return packageName;
    }
    
    public ClassName getMetaModelClassName() {
        return ClassName.get(packageName, getSimpleName().toString());
    }

    @Override
    public int hashCode() {
        // if this changes, change asEqual methods
        return 31 + Objects.hash(getQualifiedName());
    }

    @Override
    public boolean equals(final Object obj) {
        // if this changes, change asEqual methods
        return this == obj
                || obj instanceof final MetaModelElement mme
                && Objects.equals(this.getQualifiedName(), mme.getQualifiedName());
    }

    /**
     * Wraps a {@link MetaModelConcept} so that it can be treated <i>as equal</i> to a {@link MetaModelElement}.
     * Equivalence tests are those that use {@link #hashCode()} or {@link #equals(Object)} methods to compare objects.
     * Since both {@link MetaModelConcept} and {@link MetaModelElement} are uniquely identified by their qualified names,
     * it can be said that their instances are equal if they have the same qualified name.
     * <p>
     * Here are a couple of examples where {@code asEqual} can be conveniently used:
     * <ul>
     *   <li>Testing that a {@link Set} of {@link MetaModelElement}s contains a {@link MetaModelConcept}</li>
     *   <li>Testing that a {@link Map} of {@link MetaModelElement}-typed keys contains a {@link MetaModelConcept} key</li>
     * </ul>
     * 
     * @param mmc   instance to be wrapped
     * @param namer mapping function from the return type of {@link MetaModelConcept#getQualifiedName()} to that of 
     * {@link MetaModelElement#getQualifiedName()}; typically this would be {@link Elements#getName(CharSequence)}
     * @return
     */
    public static Object asEqual(final MetaModelConcept mmc, final Function<String, Name> namer) {
        final Name qualName = namer.apply(mmc.getQualifiedName());

        return new Object() {
            @Override
            public int hashCode() {
                return 31 + Objects.hash(qualName);
            }

            @Override
            public boolean equals(final Object obj) {
                return this == obj
                        || obj instanceof final MetaModelElement mme
                        && Objects.equals(qualName, mme.getQualifiedName());
            }
        };
    }

    /**
     * The same as {@link #asEqual(MetaModelConcept, Function)}, but uses {@link MetaModelConcept#getAliasedQualifiedName()},
     * thus should be used whenever {@code mmc} represents an aliased meta-model.
     */
    public static Object asEqualAliased(final MetaModelConcept mmc, final Function<String, Name> namer) {
        final Name qualName = namer.apply(mmc.getAliasedQualifiedName());

        return new Object() {
            @Override
            public int hashCode() {
                return 31 + Objects.hash(qualName);
            }

            @Override
            public boolean equals(final Object obj) {
                return this == obj
                        || obj instanceof final MetaModelElement mme
                        && Objects.equals(qualName, mme.getQualifiedName());
            }
        };
    }

}
