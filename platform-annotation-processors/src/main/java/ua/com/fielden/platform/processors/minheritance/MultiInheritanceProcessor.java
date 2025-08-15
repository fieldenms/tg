package ua.com.fielden.platform.processors.minheritance;

import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.*;
import ua.com.fielden.platform.annotations.metamodel.WithMetaModel;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.Accessor;
import ua.com.fielden.platform.entity.Mutator;
import ua.com.fielden.platform.entity.annotation.Extends;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.exceptions.AbstractPlatformCheckedException;
import ua.com.fielden.platform.processors.AbstractPlatformAnnotationProcessor;
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
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static javax.lang.model.element.Modifier.*;
import static ua.com.fielden.platform.entity.AbstractEntity.DESC;
import static ua.com.fielden.platform.entity.AbstractEntity.KEY;
import static ua.com.fielden.platform.entity.AbstractPersistentEntity.*;
import static ua.com.fielden.platform.entity.ActivatableAbstractEntity.ACTIVE;
import static ua.com.fielden.platform.processors.metamodel.utils.ElementFinder.TYPE_ELEMENT_FILTER;
import static ua.com.fielden.platform.processors.metamodel.utils.ElementFinder.asTypeElementOfTypeMirror;

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

    private static final Set<String> EXCLUDED_PROPERTIES = ImmutableSet.of(
            KEY, DESC,
            ACTIVE,
            CREATED_DATE,
            CREATED_BY,
            CREATED_TRANSACTION_GUID,
            LAST_UPDATED_DATE,
            LAST_UPDATED_BY,
            LAST_UPDATED_TRANSACTION_GUID);

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

        return false;
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
                .toList();

        final var specProperties = entityFinder.streamProperties(specEntity)
                .filter(prop -> !EXCLUDED_PROPERTIES.contains(prop.getSimpleName().toString()))
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

        // There are no conflicts, so pick the first property for each name.
        // Generate only properties from entities in `@Extends`, since properties of the spec entity will be inherited by virtue of extending it.
        final var generatedProperties = StreamUtils.distinct(inheritedProperties.stream(), PropertyElement::getSimpleName).toList();

        final var genEntitySimpleName = simpleNameForGenEntity(specEntity.getSimpleName());

        return TypeSpec.classBuilder(genEntitySimpleName)
                .addModifiers(PUBLIC)
                .superclass(specEntity.getEntityClassName())
                .addAnnotation(WithMetaModel.class)
                // Skip verification of generated entities, which are unlikely to violate any rules.
                // For more details, see the documentation of this class.
                .addAnnotation(SkipVerification.class)
                .addFields(generatedProperties.stream().map(this::makePropertySpec).toList())
                .addMethods(generatedProperties.stream()
                                    .flatMap(prop -> Stream.of(makeGetterSpec(prop), makeSetterSpec(prop, genEntitySimpleName)))
                                    .toList())
                // Static field for the EQL model.
                // Not `final` -- the EQL model will be generated at runtime.
                .addField(FieldSpec.builder(EntityResultQueryModel.class, "model_", PRIVATE, STATIC).build())
                .build();
    }

    private void printConflictMessage(final List<PropertyElement> group, final EntityElement specEntity) {
        // If a property of the spec entity is among conflicting ones, attach the message to it.
        // Otherwise, attach the message to the spec entity.
        final var maybeSpecProperty = group.stream()
                .filter(prop -> prop.getEnclosingElement().equals(specEntity.element()))
                .findFirst();
        final var msg = format("Cannot inherit property [%s] with different types from %s.",
                                  group.getFirst().getSimpleName(),
                                  group.stream()
                                          .map(prop -> prop.getEnclosingElement().getSimpleName())
                                          .map("[%s]"::formatted)
                                          .collect(joining(", ")));
        maybeSpecProperty.ifPresentOrElse(it -> messager.printMessage(Diagnostic.Kind.ERROR, msg, it.element()),
                                          () -> messager.printMessage(Diagnostic.Kind.ERROR, msg, specEntity.element()));
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

    private MethodSpec makeSetterSpec(final PropertyElement prop, final CharSequence enclosingEntitySimpleName) {
        return MethodSpec.methodBuilder(Mutator.SETTER.getName(prop.getSimpleName()))
                .addModifiers(PUBLIC)
                .addAnnotation(Observable.class)
                .addParameter(makeTypeName(prop.getType()), prop.getSimpleName().toString(), FINAL)
                // The unnamed package works because this setter will be declared in the entity itself and the entity's simple name should suffice.
                .returns(ClassName.get("", enclosingEntitySimpleName.toString()))
                .addStatement("this.$L = $L", prop.getSimpleName(), prop.getSimpleName())
                .addStatement("return this", prop.getSimpleName())
                .build();
    }

    private MethodSpec makeGetterSpec(final PropertyElement prop) {
        final var name = prop.getType().getKind().equals(TypeKind.BOOLEAN)
                ? Accessor.IS.getName(prop.getSimpleName())
                : Accessor.GET.getName(prop.getSimpleName());
        return MethodSpec.methodBuilder(name)
                .addModifiers(PUBLIC)
                .returns(makeTypeName(prop.getType()))
                .addStatement("return this.$L", prop.getSimpleName())
                .build();
    }

    private FieldSpec makePropertySpec(final PropertyElement prop) {
        final var builder = FieldSpec.builder(makeTypeName(prop.getType()), prop.getSimpleName().toString(), PRIVATE)
                .addAnnotation(IsProperty.class);
        final Title atTitle = prop.getAnnotation(Title.class);
        if (atTitle != null) {
            builder.addAnnotation(AnnotationSpec.get(atTitle));
        }
        return builder.build();
    }

    private TypeName makeTypeName(final TypeMirror typeMirror) {
        return TypeName.get(typeMirror);
    }

    private Stream<PropertyElement> inheritedPropertiesFrom(final EntityElement entity, final Set<String> excludedProps) {
        return entityFinder.streamProperties(entity)
                .filter(prop -> !excludedProps.contains(prop.getSimpleName().toString()));
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
