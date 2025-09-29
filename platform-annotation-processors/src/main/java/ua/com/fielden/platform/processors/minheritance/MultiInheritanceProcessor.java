package ua.com.fielden.platform.processors.minheritance;

import com.squareup.javapoet.*;
import ua.com.fielden.platform.annotations.metamodel.WithMetaModel;
import ua.com.fielden.platform.entity.Accessor;
import ua.com.fielden.platform.entity.Mutator;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.exceptions.AbstractPlatformCheckedException;
import ua.com.fielden.platform.exceptions.AbstractPlatformRuntimeException;
import ua.com.fielden.platform.processors.AbstractPlatformAnnotationProcessor;
import ua.com.fielden.platform.processors.DateTimeUtils;
import ua.com.fielden.platform.processors.metamodel.elements.AbstractForwardingElement;
import ua.com.fielden.platform.processors.metamodel.elements.EntityElement;
import ua.com.fielden.platform.processors.metamodel.elements.PropertyElement;
import ua.com.fielden.platform.processors.metamodel.utils.ElementFinder;
import ua.com.fielden.platform.processors.metamodel.utils.EntityFinder;
import ua.com.fielden.platform.processors.verify.annotation.SkipVerification;
import ua.com.fielden.platform.utils.ArrayUtils;
import ua.com.fielden.platform.utils.StreamUtils;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.*;
import java.util.Optional;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.lang.String.format;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static javax.lang.model.element.Modifier.*;
import static ua.com.fielden.platform.entity.AbstractEntity.DESC;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.minheritance.MultiInheritanceCommon.EXCLUDED_PROPERTIES;
import static ua.com.fielden.platform.processors.metamodel.utils.ElementFinder.*;
import static ua.com.fielden.platform.utils.MessageUtils.singleOrPlural;

/// An annotation processor for the [Extends] annotation.
///
/// #### Verification of generated entities
///
/// Generated entities are annotated with [ua.com.fielden.platform.processors.verify.annotation.SkipVerification],
/// which disables their verification.
///
/// Proper support for verification of generated entities requires non-trivial effort to implement.
/// Here is one scenario that currently produces undesired effects if verification is enabled for generated entities:
/// 1. `ReVehicle` with entity-typed property `model` is generated in the 1st round.
/// 2. `ReVehicle` is verified in the 2nd round.
/// 3. `ApplicationDomain` is regenerated in the 2nd round.
///
/// The outcome of step 2 depends on the prior existence of `ApplicationDomain`:
/// * If `ApplicationDomain` exists, the verification will use its old version (not the regenerated one, as it won't be seen until the 3rd round),
///   which does not yet contain `ReVehicle`.
///   The verification will report an "unregistered entity" error if `ReVehicle` has a property whose type is `ReVehicle`.
/// * If `ApplicationDomain` does not exist (most likely due to a clean build occurring),
///   verification of `ReVehicle` will report an "unregistered entity" error for each entity-typed property.
///
/// One solution is to delay verification until the 3rd round, while gathering all inputs in the first two rounds.
/// The challenging part is the handling of different scenarios -- sometimes, the 2nd round may be the last one.
///
/// #### Support for extending synthetic entity types
///
/// Currently, synthetic entity types cannot be specified in [Extends].
/// This limitation stems from the objective of simplicity for the initial versions of the processor.
///
/// If synthetic entity types are to be supported, it is important to consider the role of crit-only properties and the rules for their inheritance.
///
@SupportedAnnotationTypes("*")
public class MultiInheritanceProcessor extends AbstractPlatformAnnotationProcessor {

    private static final String WARN_PROPERTY_WILL_BE_HIDDEN =
            "Property [%s] will be hidden by inherited %s [%s]. Either the inherited %s should be excluded, or this property removed.";

    private ElementFinder elementFinder;
    private EntityFinder entityFinder;

    @Override
    public synchronized void init(final ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.elementFinder = new ElementFinder(processingEnv);
        this.entityFinder = new EntityFinder(processingEnv);
    }

    @Override
    protected boolean processRound(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        roundEnv.getElementsAnnotatedWith(Extends.class)
                .stream()
                .mapMulti(TYPE_ELEMENT_FILTER)
                .map(entityFinder::newEntityElement)
                .forEach(specEntity -> {
                    printNote("Generating an entity from the multi-inheritance specification in [%s].", specEntity.getQualifiedName());
                    try {
                        final var javaFile = writeEntity(buildEntity(specEntity, specEntity.getAnnotation(Extends.class)), specEntity);
                        printNote("Generated [%s.%s] from the multi-inheritance specification in [%s].",
                                  javaFile.packageName, javaFile.typeSpec.name, specEntity.getQualifiedName());
                    }
                    catch (final PropertyConflictException e) {
                        e.groups.forEach(g -> printConflictMessage(g, specEntity));
                    }
                    // An error message must have had been reported before this exception was thrown.
                    // We catch it to avoid interrupting other processors.
                    catch (final SpecEntityDefinitionException $) {}
                });

        // Detect and report orphaned entity types.
        // Ideally, orphaned entity types should be regenerated into "stubs", to prevent their further use.
        // However, that is not currently possible because:
        // * Sources that are among APT inputs cannot be regenerated.
        //   Currently, this is the only way they can be discovered at all.
        // * Alternative discovery mechanisms, such as a registry file, are possible but will add complexity.
        if (getRoundNumber() == 1) {
            roundEnv.getElementsAnnotatedWith(SpecifiedBy.class)
                    .stream()
                    .mapMulti(TYPE_ELEMENT_FILTER)
                    .filter(this::isOrphaned)
                    .forEach(elt -> messager.printError(
                            format("[%s] is orphaned: its specification type could not be resolved. The orphaned entity type should be deleted manually.",
                                   elt.getQualifiedName()),
                            elt));
        }

        return false;
    }

    private boolean isOrphaned(final TypeElement element) {
        final var annot = element.getAnnotation(SpecifiedBy.class);
        if (annot == null) {
            messager.printWarning("Generated entity type [%s] is missing @%s.".formatted(element.getQualifiedName(), SpecifiedBy.class.getCanonicalName()),
                                  element);
            return true;
        }
        final var specTypeMirror = elementFinder.getAnnotationElementValueOfClassType(annot, SpecifiedBy::value);
        // Missing types have TypeKind.ERROR.
        return specTypeMirror.getKind() == TypeKind.ERROR;
    }

    private JavaFile writeEntity(final TypeSpec genEntity, final EntityElement specEntity) {
        final var javaFile = JavaFile.builder(specEntity.getPackageName(), genEntity).indent("    ").build();
        try {
            javaFile.writeTo(filer);
        } catch (final IOException ex) {
            printWarning("Failed to generate [%s] from [%s]. %s", genEntity.name, specEntity.getQualifiedName(), ex.getMessage());
        }
        return javaFile;
    }

    private TypeSpec buildEntity(final EntityElement specEntity, final Extends atExtends)
            throws PropertyConflictException
    {
        final var atExtendsMirror = ExtendsMirror.fromAnnotation(atExtends, elementFinder);

        verifyExtends(specEntity, atExtendsMirror);

        final var specHasDesc = entityFinder.maybePropDesc(specEntity).isPresent();

        final var inheritedProperties = atExtendsMirror.value()
                .stream()
                .flatMap(atEntityMirror  -> {
                    final var entity = entityFinder.newEntityElement(asTypeElementOfTypeMirror(atEntityMirror.value()));
                    return inheritedPropertiesFrom(entity, atEntityMirror);
                })
                .filter(prop -> !EXCLUDED_PROPERTIES.contains(prop.getSimpleName().toString()))
                // Do not inherit `desc` if the spec-entity type does not have `desc`.
                .filter(prop -> !prop.getSimpleName().contentEquals(DESC) || specHasDesc)
                .toList();

        // Use `distinct` to enable property hiding in the spec-entity.
        final var specProperties = StreamUtils.distinct(
                        entityFinder.streamProperties(specEntity)
                                .filter(prop -> !EXCLUDED_PROPERTIES.contains(prop.getSimpleName().toString()))
                                .filter(prop -> !prop.getSimpleName().contentEquals(DESC) || specHasDesc),
                        PropertyElement::getSimpleName)
                .toList();

        final var autoYieldPropertyNames = specProperties.stream()
                .filter(prop -> hasAnnotation(prop, AutoYield.class))
                .peek(prop -> verifyAutoYieldProp(prop, specEntity, atExtendsMirror))
                .map(AbstractForwardingElement::getSimpleName)
                .collect(toImmutableSet());

        // Group all properties by name to detect conflicts.
        final var propertyGroups = Stream.concat(specProperties.stream(), inheritedProperties.stream())
                .collect(groupingBy(PropertyElement::getSimpleName));

        final var conflictingGroups = propertyGroups.values()
                .stream()
                .filter(group -> group.size() > 1)
                .filter(this::isConflicting)
                .toList();
        if (!conflictingGroups.isEmpty()) {
            throw new PropertyConflictException(conflictingGroups);
        }

        reportHiddenProperties(specEntity, specProperties, inheritedProperties);

        // There are no conflicts, so pick the first property for each name.
        // Generate only properties from entities in `@Extends`, since properties of the spec entity will be inherited by virtue of extending it.
        // Do not generate @AutoYield properties.
        final var propertySpecs = Stream.concat(
                        StreamUtils.distinct(inheritedProperties.stream(), PropertyElement::getSimpleName)
                                .filter(prop -> !autoYieldPropertyNames.contains(prop.getSimpleName()))
                                // `desc`, if present, will be inherited from the spec-entity, hence does not need to be declared.
                                .filter(prop -> !DESC.contentEquals(prop.getSimpleName()))
                                .map(this::makePropertySpec),
                        Stream.of(propertySpecBuilder(TypeName.get(String.class), atExtends.entityTypeCarrierProperty())
                                          .addAnnotation(EntityTypeCarrier.class)
                                          .build()))
                .toList();

        final var genEntitySimpleName = atExtendsMirror.name();

        return TypeSpec.classBuilder(genEntitySimpleName)
                .addModifiers(PUBLIC)
                .superclass(specEntity.getEntityClassName())
                .addAnnotation(WithMetaModel.class)
                // Skip verification of generated entities, which are unlikely to violate any rules.
                // For more details, see the documentation of this class.
                .addAnnotation(SkipVerification.class)
                .addAnnotation(AnnotationSpec.builder(SpecifiedBy.class).addMember("value", "$T.class", specEntity.element()).build())
                .addAnnotation(CompanionIsGenerated.class)
                .addAnnotation(buildAtGenerated(DateTimeUtils.toIsoFormat(DateTimeUtils.zonedNow())))
                .addFields(propertySpecs)
                .addMethods(propertySpecs.stream()
                                    .flatMap(prop -> Stream.of(makeGetterSpec(prop), makeSetterSpec(prop, genEntitySimpleName)))
                                    .toList())
                // Static field for the EQL model.
                // Not `final` -- the EQL model will be generated at runtime.
                .addField(FieldSpec.builder(ParameterizedTypeName.get(List.class, EntityResultQueryModel.class), "models_", PRIVATE, STATIC).build())
                .build();
    }

    private void verifyExtends(final EntityElement specEntity, final ExtendsMirror atExtends) {
        // Extends.value must not be empty.
        if (atExtends.value().isEmpty()) {
            final var msg = "At least one entity type must be specified in @%s.".formatted(Extends.class.getSimpleName());
            printMessageOn(Diagnostic.Kind.ERROR, msg, specEntity.element(), Extends.class);
            throw new SpecEntityDefinitionException(specEntity, msg);
        }

        // Entity types in @Extends must be unique.
        atExtends.value()
                .stream()
                .map(ExtendsMirror.EntityMirror::value)
                .filter(tm -> tm.getKind() != TypeKind.ERROR)
                .collect(groupingBy(ElementFinder::asTypeElementOfTypeMirror))
                .forEach((typeElt, mirrors) -> {
                    if (mirrors.size() > 1) {
                        final var msg = "Entity types in @%s must be unique. [%s] occurs %s times.".formatted(
                                Extends.class.getSimpleName(),
                                typeElt.getSimpleName(),
                                mirrors.size());
                        printMessageOn(Diagnostic.Kind.ERROR, msg, specEntity.element(), Extends.class);
                        throw new SpecEntityDefinitionException(specEntity, msg);
                    }
                });

        // Each entity type must be a persistent type.
        atExtends.value()
                .stream()
                .map(ExtendsMirror.EntityMirror::value)
                .filter(tm -> tm.getKind() == TypeKind.DECLARED)
                .map(ElementFinder::asTypeElementOfTypeMirror)
                .map(entityFinder::newEntityElement)
                .filter(entity -> !entityFinder.isPersistentEntityType(entity))
                .forEach(entity -> {
                    final var msg = "[%s] cannot be specified in @%s. Only persistent entity types are allowed.".formatted(
                            entity.getSimpleName(), Extends.class.getSimpleName());
                    printMessageOn(Diagnostic.Kind.ERROR, msg, specEntity.element(), Extends.class);
                    throw new SpecEntityDefinitionException(specEntity, msg);
                });

        // Excluded properties must exist in the entity type they are excluded from.
        atExtends.value()
                .stream()
                .filter(atEntity -> atEntity.value().getKind() != TypeKind.ERROR)
                .forEach(atEntity -> {
                    final var entity = entityFinder.newEntityElement(ElementFinder.asTypeElementOfTypeMirror(atEntity.value()));
                    final var invalidProps = Arrays.stream(atEntity.exclude())
                            .filter(prop -> !canPropertyBeInheritedFrom(prop, entity))
                            .toList();
                    if (!invalidProps.isEmpty()) {
                        final var msg = "%s [%s] %s not inherited and cannot be excluded.".formatted(
                                singleOrPlural(invalidProps.size(), "property", "properties"),
                                invalidProps.stream().map(prop -> "%s.%s".formatted(entity.getSimpleName(), prop)).collect(joining(", ")),
                                singleOrPlural(invalidProps.size(), "is", "are"));
                        printMessageOn(Diagnostic.Kind.ERROR, msg, specEntity.element(), Extends.class);
                        throw new SpecEntityDefinitionException(specEntity, msg);
                    }
                });


        // TODO Uncomment if support for synthetic entity types in `@Extends` is enabled.
        /*
        // Warn about extended synthetic entity types without `id`.
        atExtends.value()
                .stream()
                .filter(atEntity -> atEntity.value().getKind() != TypeKind.ERROR)
                .forEach(atEntity -> {
                    final var entity = entityFinder.newEntityElement(ElementFinder.asTypeElementOfTypeMirror(atEntity.value()));
                    verifyIdInSynType(specEntity, entity);
                });
        */

        // Warn if none of the extended types has `desc` but the spec-entity type does.
        // Such a definition will result in `desc` recognised as a property of the generated entity type, but no yields for `desc` will be generated.
        // Therefore, the developer should take action.
        if (entityFinder.maybePropDesc(specEntity).isPresent()) {
            final var noInheritedDesc = atExtends.value()
                    .stream()
                    .allMatch(atEntity -> {
                        if (ArrayUtils.contains(atEntity.exclude(), DESC)) {
                            return true;
                        }
                        else {
                            final var entity = entityFinder.newEntityElement(ElementFinder.asTypeElementOfTypeMirror(atEntity.value()));
                            return entityFinder.maybePropDesc(entity).isEmpty();
                        }
                    });
            if (noInheritedDesc) {
                final var msg = """
                    [%s] is not inherited from any of the types in @%s. \
                    Either [%s] should be removed from [%s], or [%s] should be yielded explicitly in the EQL model."""
                    .formatted(DESC, Extends.class.getSimpleName(), DESC, specEntity.getSimpleName(), DESC);
                printMessageOn(Diagnostic.Kind.WARNING, msg, specEntity.element(), Extends.class);
            }
        }
    }

    private void verifyAutoYieldProp(final PropertyElement prop, final EntityElement specEntity, final ExtendsMirror atExtendsMirror) {
        final var inheritedFromNone = atExtendsMirror.value()
                .stream()
                .noneMatch(atEntityMirror -> isPropertyInheritedFrom(prop.getSimpleName(), atEntityMirror));
        if (inheritedFromNone) {
            final var msg = format("@%s is not applicable to [%s], which is not inherited from any of the types in @%s.",
                                   AutoYield.class.getSimpleName(), prop.getSimpleName(), Extends.class.getSimpleName());
            printMessageOnProperty(Diagnostic.Kind.ERROR, msg, specEntity, prop);
            throw new SpecEntityDefinitionException(specEntity, msg);
        }
    }

    private void reportHiddenProperties(
            final EntityElement specEntity,
            final List<PropertyElement> specProperties,
            final Collection<PropertyElement> inheritedProperties)
    {
        specProperties.stream()
                // `desc` may be present in the spec-entity type and also in extended types, but due to its special treatment it will not be hidden.
                .filter(specProp -> !DESC.contentEquals(specProp.getSimpleName()))
                .filter(specProp -> !hasAnnotation(specProp, AutoYield.class))
                .filter(inheritedProperties::contains)
                .forEach(specProp -> {
                    final var hidingProps = inheritedProperties.stream().filter(prop -> prop.equals(specProp)).toList();
                    final var msg = WARN_PROPERTY_WILL_BE_HIDDEN.formatted(
                            specProp.getSimpleName(),
                            singleOrPlural(hidingProps.size(), "property", "properties"),
                            hidingProps.stream()
                                    .map(prop -> "%s.%s".formatted(prop.getEnclosingElement().getSimpleName(), prop.getSimpleName()))
                                    .collect(joining(", ")),
                            singleOrPlural(hidingProps.size(), "property", "properties"));
                    printMessageOnProperty(Diagnostic.Kind.MANDATORY_WARNING, msg, specEntity, specProp);
                });
    }

    // TODO Uncomment if support for synthetic entity types in `@Extends` is enabled.
    /*
    private void verifyIdInSynType(final EntityElement specEntity, final EntityElement extendedEntity) {
        if (entityFinder.isSyntheticEntityType(extendedEntity) && entityFinder.maybePropId(extendedEntity).isEmpty()) {
            final var msg = """
                The structure of [%s] indicates that it does not have [%s]. \
                The resulting EQL model will yield null. \
                If this is not intended, [%s] should be explicitly declared in [%s]."""
                .formatted(extendedEntity.getSimpleName(), ID, ID, extendedEntity.getSimpleName());
            printMessageOn(Diagnostic.Kind.WARNING, msg, specEntity.element(), Extends.class);
        }
    }
    */

    private void printConflictMessage(final List<PropertyElement> group, final EntityElement specEntity) {
        // Use the spec property, if possible, for a more precise message.
        final var property = group.stream()
                .filter(prop -> prop.getEnclosingElement().equals(specEntity.element()))
                .findFirst()
                .orElseGet(group::getFirst);
        final var msg = format("Cannot inherit property [%s] with different types from %s.",
                                  group.getFirst().getSimpleName(),
                                  group.stream()
                                          .map(prop -> prop.getEnclosingElement().getSimpleName())
                                          .map("[%s]"::formatted)
                                          .collect(joining(", ")));
        printMessageOnProperty(Diagnostic.Kind.ERROR, msg, specEntity, property);
    }

    private void printMessageOnProperty(final Diagnostic.Kind kind, final String message, final EntityElement entity, final PropertyElement property) {
        if (property.getEnclosingElement().equals(entity.element())) {
            messager.printMessage(kind, message, property.element());
        }
        else {
            messager.printMessage(kind, message, entity.element());
        }
    }

    /// This predicate is true if `props` is a group of conflicting properties, which means that all of them have the same name but not the same type.
    /// This methods assumes that all properties in `props` have the same name.
    ///
    private boolean isConflicting(final List<PropertyElement> props) {
        if (props.size() <= 1) {
            return false;
        }
        return props.stream()
                       .map(PropertyElement::getType)
                       .filter(typeMirror -> typeMirror.getKind() != TypeKind.ERROR)
                       // String representation of a type should uniquely identify it.
                       .map(TypeMirror::toString)
                       .distinct()
                       .limit(2)
                       .count() > 1;
    }

    private MethodSpec makeSetterSpec(final FieldSpec propSpec, final CharSequence enclosingEntitySimpleName) {
        return setterSpecBuilder(propSpec.type, propSpec.name, enclosingEntitySimpleName).build();
    }

    private MethodSpec.Builder setterSpecBuilder(final TypeName propTypeName, final CharSequence propName, final CharSequence enclosingEntitySimpleName) {
        // ID has an unconventional setter (AbstractEntity.setId).
        final var isId = ID.contentEquals(propName);
        final var builder = MethodSpec.methodBuilder(Mutator.SETTER.getName(propName))
                .addModifiers(PUBLIC)
                .addAnnotation(Observable.class)
                .addParameter(propTypeName, propName.toString(), FINAL)
                // The unnamed package works because this setter will be declared in the entity itself and the entity's simple name should suffice.
                .returns(isId ? TypeName.VOID : ClassName.get("", enclosingEntitySimpleName.toString()))
                .addStatement("this.$L = $L", propName, propName);
        if (!isId) {
            builder.addStatement("return this", propName);
        }
        return builder;
    }

    private MethodSpec makeGetterSpec(final FieldSpec propSpec) {
        return getterSpecBuilder(propSpec.type, propSpec.name).build();
    }

    private MethodSpec.Builder getterSpecBuilder(final TypeName propTypeName, final CharSequence propName) {
        final var name = propTypeName.equals(TypeName.BOOLEAN)
                ? Accessor.IS.getName(propName)
                : Accessor.GET.getName(propName);
        return MethodSpec.methodBuilder(name)
                .addModifiers(PUBLIC)
                .returns(propTypeName)
                .addStatement("return this.$L", propName);
    }

    private FieldSpec makePropertySpec(final PropertyElement prop) {
        final var builder = propertySpecBuilder(makeTypeName(prop.getType()), prop.getSimpleName());
        final Title atTitle = prop.getAnnotation(Title.class);
        if (atTitle != null) {
            builder.addAnnotation(AnnotationSpec.get(atTitle));
        }
        return builder.build();
    }

    private FieldSpec.Builder propertySpecBuilder(final TypeName typeName, final CharSequence name) {
        return FieldSpec.builder(typeName, name.toString(), PRIVATE)
                .addAnnotation(IsProperty.class);
    }

    private TypeName makeTypeName(final TypeMirror typeMirror) {
        return TypeName.get(typeMirror);
    }

    private boolean isPropertyInheritedFrom(final CharSequence prop, final ExtendsMirror.EntityMirror atEntityMirror) {
        return !ArrayUtils.contains(atEntityMirror.exclude(), prop.toString())
               && canPropertyBeInheritedFrom(prop, atEntityMirror.value());
    }

    private boolean canPropertyBeInheritedFrom(final CharSequence prop, final TypeMirror typeMirror) {
        final var entity = entityFinder.newEntityElement(asTypeElementOfTypeMirror(typeMirror));
        return canPropertyBeInheritedFrom(prop, entity);
    }

    private boolean canPropertyBeInheritedFrom(final CharSequence prop, final EntityElement entity) {
        final Optional<PropertyElement> maybeProp;
        if (DESC.contentEquals(prop)) {
            maybeProp = entityFinder.maybePropDesc(entity);
        }
        else if (ID.contentEquals(prop)) {
            maybeProp = entityFinder.maybePropId(entity);
        }
        else {
            maybeProp = entityFinder.findProperty(entity, prop);
        }
        return maybeProp
                .filter(propElt -> entityFinder.isPersistentProperty(propElt))
                .isPresent();
    }

    private Stream<PropertyElement> inheritedPropertiesFrom(final EntityElement entity, final ExtendsMirror.EntityMirror atEntityMirror) {
        // Use `distinct` to enable property hiding.
        return StreamUtils.distinct(
                Stream.concat(entityFinder.streamProperties(entity),
                              // `streamProperties` does not include `AbstractEntity.id`, hence we include it explicitly.
                              entityFinder.maybePropId(entity).stream())
                        .filter(prop -> isPropertyInheritedFrom(prop.getSimpleName(), atEntityMirror)),
                PropertyElement::getSimpleName);
    }

    // TODO Stack trace does not need to be captured, because this exception functions as an effect and never has a cause.
    private static final class PropertyConflictException extends AbstractPlatformCheckedException {

        private final List<List<PropertyElement>> groups;

        public PropertyConflictException(final List<List<PropertyElement>> groups) {
            super(null);
            this.groups = groups;
        }

    }

    // TODO Stack trace does not need to be captured, because this exception functions as an effect and never has a cause.
    private static final class SpecEntityDefinitionException extends AbstractPlatformRuntimeException {

        public SpecEntityDefinitionException(final TypeElement specEntity, final CharSequence msg) {
            super("Invalid definition of specification entity type [%s]. %s".formatted(specEntity.getQualifiedName(), msg));
        }

    }

}
