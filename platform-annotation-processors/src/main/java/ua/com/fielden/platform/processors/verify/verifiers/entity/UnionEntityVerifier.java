package ua.com.fielden.platform.processors.verify.verifiers.entity;

import ua.com.fielden.platform.processors.metamodel.elements.EntityElement;
import ua.com.fielden.platform.processors.metamodel.elements.PropertyElement;
import ua.com.fielden.platform.processors.metamodel.utils.EntityFinder;
import ua.com.fielden.platform.processors.verify.ViolatingElement;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static javax.tools.Diagnostic.Kind.ERROR;
import static ua.com.fielden.platform.processors.metamodel.utils.ElementFinder.asTypeElementOfTypeMirror;
import static ua.com.fielden.platform.processors.metamodel.utils.ElementFinder.getSimpleName;

/**
 * Composable verifier for union entities. Verification rules include:
 * <ol>
 *  <li>There should be at least 1 entity-typed property.</li>
 *  <li>Only entity-typed properties are permitted. However, these should not be union entities themselves
 *  (i.e., nesting of union entities is not supported).</li>
 *  <li>There should be at most one property of a particular entity type (i.e., multiple properties of the same entity type are disallowed).</li>
 * </ol>
 * Most contexts where an {@linkplain ErrorType unresolved type} is encountered are not subject to verification because
 * erroneous definitions are inherently incorrect.
 *
 * @author homedirectory
 */
public class UnionEntityVerifier extends AbstractComposableEntityVerifier {

    public UnionEntityVerifier(final ProcessingEnvironment processingEnv) {
        super(processingEnv);
    }

    @Override
    protected List<AbstractEntityVerifier> createComponents(final ProcessingEnvironment procEnv) {
        return List.of(
                new EntityTypedPropertyPresenceVerifier(procEnv),
                new PropertyTypeVerifier(procEnv),
                new DistinctPropertyEntityTypesVerifier(procEnv));
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
            return roundEnv.findViolatingUnionEntities(new EntityElementVerifier(entityFinder));
        }

        private class EntityElementVerifier extends AbstractEntityElementVerifier {
            public EntityElementVerifier(final EntityFinder entityFinder) {
                super(entityFinder);
            }

            @Override
            public Optional<ViolatingElement> verify(final EntityElement entity) {
                if (entityFinder.streamDeclaredProperties(entity).anyMatch(prop -> entityFinder.isEntityType(prop.getType()))) {
                    return Optional.empty();
                }
                return Optional.of(new ViolatingElement(entity.element(), ERROR, errNoEntityTypedProperties()));
            }
        }
    }

    /**
     * Only entity-typed properties are permitted. However, these should not be union entities themselves
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
            return roundEnv.findViolatingUnionEntityDeclaredProperties(new PropertyElementVerifier(entityFinder));
        }

        private class PropertyElementVerifier extends AbstractPropertyElementVerifier {
            public PropertyElementVerifier(final EntityFinder entityFinder) {
                super(entityFinder);
            }

            @Override
            public Optional<ViolatingElement> verifyProperty(final EntityElement entity, final PropertyElement property) {
                final TypeMirror propType = property.getType();
                if (propType.getKind() == TypeKind.ERROR) {
                    return Optional.empty();
                }
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

    /**
     * There should be at most one property of a particular entity type (i.e., multiple properties of the same entity type are disallowed).
     */
    static class DistinctPropertyEntityTypesVerifier extends AbstractEntityVerifier {

        public static String errMultiplePropertiesOfSameType(final String entity) {
            return "Union entity [%s] can't have multiple properties of the same entity type.".formatted(entity);
        }

        public static String errPropertyHasNonUniqueType(final String property) {
            return "Property [%s] has the same type as some other property of this union entity.".formatted(property);
        }

        protected DistinctPropertyEntityTypesVerifier(final ProcessingEnvironment processingEnv) {
            super(processingEnv);
        }

        @Override
        protected List<ViolatingElement> verify(final EntityRoundEnvironment roundEnv) {
            return roundEnv.findViolatingUnionEntities(new EntityElementVerifier(entityFinder));
        }

        private class EntityElementVerifier extends AbstractEntityElementVerifier {
            public EntityElementVerifier(final EntityFinder entityFinder) {
                super(entityFinder);
            }

            @Override
            public Optional<ViolatingElement> verify(final EntityElement entity) {
                // entity type -> properties having that type
                final Map<EntityElement, List<PropertyElement>> map = entityFinder.streamDeclaredProperties(entity)
                        .filter(prop -> entityFinder.isEntityType(prop.asType()))
                        .collect(Collectors.groupingBy(this::entityElementOfPropertyType));

                final List<ViolatingElement> violatingProperties = map.entrySet().stream()
                        // we are interested in those entity types that are used more than once as property types
                        .filter(entry -> entry.getValue().size() > 1)
                        .flatMap(entry -> {
                            final List<PropertyElement> properties = entry.getValue();
                            return properties.stream().map(prop -> new ViolatingElement(
                                    prop.element(), ERROR, errPropertyHasNonUniqueType(getSimpleName(prop.element()))));
                        }).toList();

                if (!violatingProperties.isEmpty()) {
                    return Optional.of(
                            new ViolatingElement(entity.element(), ERROR, errMultiplePropertiesOfSameType(getSimpleName(entity.element())))
                            .addSubElements(violatingProperties));
                }

                return Optional.empty();
            }

            /** Extracts the property type as an {@link EntityElement} */
            private EntityElement entityElementOfPropertyType(final PropertyElement property) {
                return entityFinder.newEntityElement(asTypeElementOfTypeMirror(property.getType()));
            }
        }
    }

}
