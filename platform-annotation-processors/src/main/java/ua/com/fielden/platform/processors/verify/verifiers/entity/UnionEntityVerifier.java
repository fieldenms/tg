package ua.com.fielden.platform.processors.verify.verifiers.entity;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static javax.tools.Diagnostic.Kind.ERROR;
import static ua.com.fielden.platform.processors.metamodel.utils.ElementFinder.asTypeElementOfTypeMirror;
import static ua.com.fielden.platform.processors.metamodel.utils.ElementFinder.getSimpleName;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.type.TypeMirror;

import ua.com.fielden.platform.processors.metamodel.elements.EntityElement;
import ua.com.fielden.platform.processors.metamodel.elements.PropertyElement;
import ua.com.fielden.platform.processors.metamodel.utils.EntityFinder;
import ua.com.fielden.platform.processors.verify.ViolatingElement;

/**
 * Composable verifier for union entities. Verification rules include:
 * <ol>
 *  <li>There should be at least one entity-typed property.</li>
 *  <li>Only entity-typed properties are permitted. However, those should not be union entities themselves
 *  (i.e., nesting of union entities are not supported).</li>
 *  <li>There should be at most one property of a particular entity type (i.e., multiple properties of the same entity type are disallowed).</li>
 * </ol>
 *
 * @author TG Team
 */
public class UnionEntityVerifier extends AbstractComposableEntityVerifier {

    private static final String ERR_NO_ENTITY_TYPED_PROPERTIES = "Union entity [%s] requires at least one entity-typed property.";
    private static final String ERR_NON_ENTITY_TYPED_PROPS = "Union entity [%s] should declare only entity-typed properties. Property [%s] is not entity-typed.";
    private static final String ERR_UNION_ENTITY_TYPED_PROPS = "Union entity [%s] should not declare properties of a union entity type. Property [%s] is of a union entity type.";
    private static final String ERR_MULTIPLE_PROPS_OF_SAME_TYPE = "Union entity [%s] should not declare multiple properties of the same entity type.";
    private static final String ERR_PROPS_WITH_SAME_TYPE = "Union entity [%s] should not declare properties of the same type. Property [%s] has the same type as some other property.";

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

        public static final String errNoEntityTypedProperties(final String entityName) {
            return ERR_NO_ENTITY_TYPED_PROPERTIES.formatted(entityName);
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
                    return empty();
                }
                return of(new ViolatingElement(entity.element(), ERROR, errNoEntityTypedProperties(getSimpleName(entity.element()))));
            }
        }
    }

    /**
     * Only entity-typed properties are permitted. However, those should not be union entities themselves
     * (i.e., nesting of union entities is not supported).
     */
    static class PropertyTypeVerifier extends AbstractEntityVerifier {

        public static final String errNonEntityTypedProperty(final String entityName, final String propName) {
            return ERR_NON_ENTITY_TYPED_PROPS.formatted(entityName, propName);
        }

        public static final String errUnionEntityTypedProperty(final String entityName, final String propName) {
            return ERR_UNION_ENTITY_TYPED_PROPS.formatted(entityName, propName);
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
                if (!entityFinder.isEntityType(propType)) {
                    return of(new ViolatingElement(property.element(), ERROR, errNonEntityTypedProperty(getSimpleName(entity.element()), getSimpleName(property.element()))));
                }
                if (entityFinder.isUnionEntityType(propType)) {
                    return of(new ViolatingElement(property.element(), ERROR, errUnionEntityTypedProperty(getSimpleName(entity.element()), getSimpleName(property.element()))));
                }

                return Optional.empty();
            }
        }
    }

    /**
     * There should be at most one property of a particular entity type (i.e., multiple properties of the same entity type are disallowed).
     */
    static class DistinctPropertyEntityTypesVerifier extends AbstractEntityVerifier {

        public static String errMultiplePropertiesOfSameType(final String entityName) {
            return ERR_MULTIPLE_PROPS_OF_SAME_TYPE.formatted(entityName);
        }

        public static String errPropertyHasNonUniqueType(final String entityName, final String propName) {
            return ERR_PROPS_WITH_SAME_TYPE.formatted(entityName, propName);
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
                // key is an entity type and value - properties having that type
                final Map<EntityElement, List<PropertyElement>> map = entityFinder.streamDeclaredProperties(entity)
                        .filter(prop -> entityFinder.isEntityType(prop.asType()))
                        .collect(Collectors.groupingBy(this::entityElementOfPropertyType));

                final List<ViolatingElement> violatingProperties = map.entrySet().stream()
                        // we are interested in those entity types that are used more than once as property types
                        .filter(entry -> entry.getValue().size() > 1)
                        .flatMap(entry -> {
                            final List<PropertyElement> properties = entry.getValue();
                            return properties.stream().map(prop -> new ViolatingElement(
                                    prop.element(), ERROR, errPropertyHasNonUniqueType(getSimpleName(entity.element()), getSimpleName(prop.element()))));
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
