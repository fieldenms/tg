package ua.com.fielden.platform.processors.verify.verifiers.entity;

import static javax.tools.Diagnostic.Kind.ERROR;
import static ua.com.fielden.platform.processors.metamodel.utils.ElementFinder.getSimpleName;

import java.util.List;
import java.util.Optional;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.type.TypeMirror;

import ua.com.fielden.platform.processors.metamodel.elements.EntityElement;
import ua.com.fielden.platform.processors.metamodel.elements.PropertyElement;
import ua.com.fielden.platform.processors.metamodel.utils.EntityFinder;
import ua.com.fielden.platform.processors.verify.ViolatingElement;

/**
 * Composable verifier for union entities. Verification rules include:
 * <ol>
 *  <li>There should be at least 1 entity-typed property.</li>
 *  <li>Only entity-typed properties are permitted. However, those should not be union entities themselves
 *  (i.e., nesting of union entities is not supported).</li>
 *  <li>There should be at most one property of a particular entity type (i.e., multiple properties of the same entity type are disallowed).</li>
 * </ol>
 *
 * @author homedirectory
 */
public class UnionEntityVerifier extends AbstractComposableEntityVerifier {

    public UnionEntityVerifier(final ProcessingEnvironment processingEnv) {
        super(processingEnv);
    }

    @Override
    protected List<AbstractEntityVerifier> createComponents(final ProcessingEnvironment procEnv) {
        return List.of(new EntityTypedPropertyPresenceVerifier(procEnv));
    }

    /**
     * There should be at least 1 entity-typed property.
     */
    static class EntityTypedPropertyPresenceVerifier extends AbstractEntityVerifier {

        public static final String errNoEntityTypedProperties() {
            return "A union entity must declare at least 1 entity-typed property";
        }

        protected EntityTypedPropertyPresenceVerifier(final ProcessingEnvironment processingEnv) {
            super(processingEnv);
        }

        @Override
        protected List<ViolatingElement> verify(final EntityRoundEnvironment roundEnv) {
            return roundEnv.acceptUnionEntityVisitor(new EntityVisitor(entityFinder));
        }

        private class EntityVisitor extends AbstractEntityVerifyingVisitor {
            public EntityVisitor(final EntityFinder entityFinder) {
                super(entityFinder);
            }

            @Override
            public Optional<ViolatingElement> visitEntity(final EntityElement entity) {
                if (entityFinder.streamDeclaredProperties(entity).anyMatch(prop -> entityFinder.isEntityType(prop.getType()))) {
                    return Optional.empty();
                }
                return Optional.of(new ViolatingElement(entity.element(), ERROR, errNoEntityTypedProperties()));
            }
        }
    }

    /**
     * Only entity-typed properties are permitted. However, those should not be union entities themselves
     * (i.e., nesting of union entities is not supported).
     */
    static class PropertyTypeVerifier extends AbstractEntityVerifier {

        public static final String errNonEntityTypedProperty(final String property) {
            return "A union entity shall declare only entity-typed properties. Property [%s] is not of entity type.".formatted(property);
        }

        public static final String errUnionEntityTypedProperty(final String property) {
            return "A union entity can't be composed of other union entities. Property [%s] is of a union entity type.".formatted(property);
        }

        protected PropertyTypeVerifier(final ProcessingEnvironment processingEnv) {
            super(processingEnv);
        }

        @Override
        protected List<ViolatingElement> verify(final EntityRoundEnvironment roundEnv) {
            return roundEnv.acceptDeclaredPropertiesVisitor(new PropertyVisitor(entityFinder));
        }

        private class PropertyVisitor extends AbstractPropertyVerifyingVisitor {
            public PropertyVisitor(final EntityFinder entityFinder) {
                super(entityFinder);
            }

            @Override
            public Optional<ViolatingElement> visitProperty(final EntityElement entity, final PropertyElement property) {
                final TypeMirror propType = property.getType();
                if (!entityFinder.isEntityType(propType)) {
                    return Optional.of(new ViolatingElement(
                            property.element(), ERROR, errNonEntityTypedProperty(getSimpleName(property.element()))));
                }
                if (entityFinder.isUnionEntityType(propType)) {
                    return Optional.of(new ViolatingElement(
                            property.element(), ERROR, errUnionEntityTypedProperty(getSimpleName(property.element()))));
                }

                return Optional.empty();
            }
        }
    }

}
