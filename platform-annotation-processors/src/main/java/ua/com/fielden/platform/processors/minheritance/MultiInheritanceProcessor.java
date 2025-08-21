package ua.com.fielden.platform.processors.minheritance;

import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.*;
import ua.com.fielden.platform.annotations.metamodel.WithMetaModel;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.Accessor;
import ua.com.fielden.platform.entity.Mutator;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.exceptions.AbstractPlatformCheckedException;
import ua.com.fielden.platform.processors.AbstractPlatformAnnotationProcessor;
import ua.com.fielden.platform.processors.DateTimeUtils;
import ua.com.fielden.platform.processors.metamodel.elements.EntityElement;
import ua.com.fielden.platform.processors.metamodel.elements.PropertyElement;
import ua.com.fielden.platform.processors.metamodel.utils.ElementFinder;
import ua.com.fielden.platform.processors.metamodel.utils.EntityFinder;
import ua.com.fielden.platform.processors.verify.annotation.SkipVerification;
import ua.com.fielden.platform.utils.StreamUtils;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static javax.lang.model.element.Modifier.*;
import static ua.com.fielden.platform.entity.AbstractEntity.DESC;
import static ua.com.fielden.platform.entity.AbstractEntity.ID;
import static ua.com.fielden.platform.minheritance.MultiInheritanceCommon.EXCLUDED_PROPERTIES;
import static ua.com.fielden.platform.processors.metamodel.utils.ElementFinder.TYPE_ELEMENT_FILTER;
import static ua.com.fielden.platform.processors.metamodel.utils.ElementFinder.asTypeElementOfTypeMirror;
import static ua.com.fielden.platform.utils.MessageUtils.singleOrPlural;

/// An annotation processor for the [Extends] annotation.
///
/// #### Verification of generated entities
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
                    } catch (final PropertyConflictException e) {
                        e.groups.forEach(g -> printConflictMessage(g, specEntity));
                    }
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
        // TODO Prohibit duplicate entity types in `@Extends`.
        // TODO Prohibit non-existent properties in `@Extends.Entity.exclude`.

        final var atExtendsMirror = ExtendsMirror.fromAnnotation(atExtends, elementFinder);

        final var inheritedProperties = atExtendsMirror.value()
                .stream()
                .flatMap(atEntityMirror  -> {
                    final var entity = entityFinder.newEntityElement(asTypeElementOfTypeMirror(atEntityMirror.value()));
                    return inheritedPropertiesFrom(entity, ImmutableSet.copyOf(atEntityMirror.exclude()));
                })
                .filter(prop -> !EXCLUDED_PROPERTIES.contains(prop.getSimpleName().toString()))
                // `desc` is a special property that should not be declared.
                // If it is present in the spec entity type, it will be inherited.
                .filter(prop -> !prop.getSimpleName().contentEquals(DESC))
                // `id` is present in `AbstractEntity`, hence need not be declared.
                .filter(prop -> !prop.getSimpleName().contentEquals(ID))
                .toList();

        // Use `distinct` to enable property hiding in the spec-entity.
        final var specProperties = StreamUtils.distinct(
                        entityFinder.streamProperties(specEntity).filter(prop -> !EXCLUDED_PROPERTIES.contains(prop.getSimpleName().toString())),
                        PropertyElement::getSimpleName)
                .toList();

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
        final var propertySpecs = Stream.concat(
                        StreamUtils.distinct(inheritedProperties.stream(), PropertyElement::getSimpleName).map(this::makePropertySpec),
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

    private void reportHiddenProperties(
            final EntityElement specEntity,
            final List<PropertyElement> specProperties,
            final Collection<PropertyElement> inheritedProperties)
    {
        specProperties.stream()
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

    private boolean isConflicting(final List<PropertyElement> props) {
        if (props.size() <= 1) {
            return false;
        }
        return props.stream()
                       .map(PropertyElement::getType)
                       // TODO Support all kinds of types.
                       .filter(typeMirror -> typeMirror.getKind() == TypeKind.DECLARED)
                       .map(ElementFinder::asTypeElementOfTypeMirror)
                       .map(TypeElement::getQualifiedName)
                       .distinct()
                       .limit(2)
                       .count() > 1;
    }

    private MethodSpec makeSetterSpec(final FieldSpec propSpec, final CharSequence enclosingEntitySimpleName) {
        return setterSpecBuilder(propSpec.type, propSpec.name, enclosingEntitySimpleName).build();
    }

    private MethodSpec.Builder setterSpecBuilder(final TypeName propTypeName, final CharSequence propName, final CharSequence enclosingEntitySimpleName) {
        return MethodSpec.methodBuilder(Mutator.SETTER.getName(propName))
                .addModifiers(PUBLIC)
                .addAnnotation(Observable.class)
                .addParameter(propTypeName, propName.toString(), FINAL)
                // The unnamed package works because this setter will be declared in the entity itself and the entity's simple name should suffice.
                .returns(ClassName.get("", enclosingEntitySimpleName.toString()))
                .addStatement("this.$L = $L", propName, propName)
                .addStatement("return this", propName);
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

    private Stream<PropertyElement> inheritedPropertiesFrom(final EntityElement entity, final Set<String> excludedProps) {
        // Use `distinct` to enable property hiding.
        return StreamUtils.distinct(
                entityFinder.streamProperties(entity).filter(prop -> !excludedProps.contains(prop.getSimpleName().toString())),
                PropertyElement::getSimpleName);
    }

    // TODO Decide on a naming convention for generated entity types.
    //      We could also provide a way for application developers to specify the name.
    private String simpleNameForGenEntity(final CharSequence specSimpleName) {
        return specSimpleName + "_Gen";
    }

    private static String qualifiedNameForGenEntity(final CharSequence pkgName, final CharSequence simpleName) {
        return format("%s.%s_Syn", pkgName, simpleName);
    }

    private static String qualifiedNameForGenEntity(final Class<? extends AbstractEntity<?>> type) {
        return qualifiedNameForGenEntity(type.getPackageName(), type.getSimpleName());
    }

    // TODO Stack trace does not need to be captured, because this exception functions as an effect and never has a cause.
    private static final class PropertyConflictException extends AbstractPlatformCheckedException {

        private final List<List<PropertyElement>> groups;

        public PropertyConflictException(final List<List<PropertyElement>> groups) {
            super(null);
            this.groups = groups;
        }

    }

}
