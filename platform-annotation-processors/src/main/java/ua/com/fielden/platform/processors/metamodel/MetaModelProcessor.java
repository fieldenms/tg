package ua.com.fielden.platform.processors.metamodel;

import com.squareup.javapoet.*;
import jakarta.annotation.Nonnull;
import org.apache.commons.lang3.StringUtils;
import ua.com.fielden.platform.annotations.metamodel.MetaModelForType;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.NoKey;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.processors.AbstractPlatformAnnotationProcessor;
import ua.com.fielden.platform.processors.DateTimeUtils;
import ua.com.fielden.platform.processors.metamodel.concepts.MetaModelConcept;
import ua.com.fielden.platform.processors.metamodel.elements.EntityElement;
import ua.com.fielden.platform.processors.metamodel.elements.MetaModelElement;
import ua.com.fielden.platform.processors.metamodel.elements.MetaModelsElement;
import ua.com.fielden.platform.processors.metamodel.elements.PropertyElement;
import ua.com.fielden.platform.processors.metamodel.exceptions.EntityMetaModelAliasedException;
import ua.com.fielden.platform.processors.metamodel.exceptions.EntitySourceDefinitionException;
import ua.com.fielden.platform.processors.metamodel.exceptions.MetaModelProcessorException;
import ua.com.fielden.platform.processors.metamodel.models.EntityMetaModel;
import ua.com.fielden.platform.processors.metamodel.models.PropertyMetaModel;
import ua.com.fielden.platform.processors.metamodel.utils.ElementFinder;
import ua.com.fielden.platform.processors.metamodel.utils.EntityFinder;
import ua.com.fielden.platform.processors.metamodel.utils.MetaModelFinder;
import ua.com.fielden.platform.utils.ImmutableSetUtils;
import ua.com.fielden.platform.utils.Pair;

import javax.annotation.processing.FilerException;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic.Kind;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.lang.String.join;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableCollection;
import static java.util.Optional.empty;
import static java.util.stream.Collectors.*;
import static ua.com.fielden.platform.processors.metamodel.MetaModelConstants.*;
import static ua.com.fielden.platform.processors.metamodel.utils.ElementFinder.TYPE_ELEMENT_FILTER;
import static ua.com.fielden.platform.processors.metamodel.utils.ElementFinder.isSameType;

/// Annotation processor that generates meta-models for domain entities.
///
@SupportedAnnotationTypes("*")
public class MetaModelProcessor extends AbstractPlatformAnnotationProcessor {

    private static final String INDENT = "    ";
    private ElementFinder elementFinder;
    private EntityFinder entityFinder;
    private MetaModelFinder metaModelFinder;

    /// This collection is used to track meta-models that were generated during one of the rounds of the current compilation cycle.
    /// This tracking helps to avoid regenerating already generated meta-models during the current compilation cycle, which otherwise results in [FilerException].
    ///
    private final Set<MetaModelConcept> allGeneratedMetaModels = new HashSet<>();

    @Override
    public synchronized void init(final ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.elementFinder = new ElementFinder(processingEnv);
        this.entityFinder = new EntityFinder(processingEnv);
        this.metaModelFinder = new MetaModelFinder(processingEnv);
    }

    @Override
    public boolean processRound(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        // we need the meta-model entry point if it exists, as it provides a definitive list of meta-models that have been generated previously
        // this information is used for processing optimisation and identification of meta-models that should be deactivated
        final Optional<MetaModelsElement> maybeMetaModelsElement = metaModelFinder.findMetaModelsElement();

        // meta-models need to be generated at every processing round for the matching entities, which where included into the round by the compiler
        // except those that were already generated on one of previous rounds during the current compilation process
        // also, if the meta-models entry point already exists, we should use it to identify existing meta-models to avoid excessive meta-model generation
        final var generatedMetaModels = collectEntitiesForMetaModelGeneration(roundEnv, maybeMetaModelsElement)
                                        .filter(mmc -> writeMetaModel(mmc)).collect(Collectors.toSet());
        if (!generatedMetaModels.isEmpty()) {
            printNote("Generated %s entity meta-models.", generatedMetaModels.size());
        }
        allGeneratedMetaModels.addAll(generatedMetaModels);

        // Generation or re-generation of the meta-models entry point class should occur only during the 2nd round of processing.
        // This is because all possible meta-models will be generated only by the end of the 2nd round -- meta-models for
        // source entities are generated in the 1st round, for generated synthetic entities -- in the 2nd round.
        if (getRoundNumber() == 2) {
            if (maybeMetaModelsElement.isEmpty()) {
                // If the meta-models entry point does not yet exist, let's generate it to include all generated meta-models.
                writeMetaModelsClass(allGeneratedMetaModels);
            } else {
                final MetaModelsElement metaModelsElt = maybeMetaModelsElement.get();
                // if MetaModels class already exists, we need to analyse its content and identify meta-models that represent "inactive" entities -- those that either no longer exist or are not considered to be domain entities.
                // such meta-models need to be regenerated as empty and abstract classes and MetaModels should be re-generated without those fields
                printNote("Entry point [%s] already exists. It needs to be processed and re-generated.", metaModelsElt.getQualifiedName());

                // analyse fields in the MetaModels class to identify meta-models that do not represent domain entities any longer
                final Set<MetaModelElement> inactiveMetaModels = findInactiveMetaModels(metaModelsElt);

                printNote("Inactive meta-models: [%s]", inactiveMetaModels.stream().map(MetaModelElement::getSimpleName).collect(joining(", ")));

                // deactivate meta-models by regenerating them as abstract classes to prevent their instantiation
                final Set<MetaModelElement> deactivedMetaModels = inactiveMetaModels.stream().filter(mme -> writeInactiveMetaModel(mme)).collect(toSet());

                // regenerate the MetaModels class by adding new fields and removing those that represent deactivated meta-models
                writeMetaModelsClass(allGeneratedMetaModels, maybeMetaModelsElement, deactivedMetaModels);
            }
        }

        // must return false to avoid claiming all annotations (as defined by @SupportedAnnotationTypes("*")) to allow other processors to run
        return false;
    }

    /// Processes a round to collect entity classes for meta-model generation.
    /// Returns [MetaModelConcept]s representing entities that require a meta-model to be generated.
    ///
    /// @param maybeMetaModelsElement – the meta-models entry point class
    private Stream<MetaModelConcept> collectEntitiesForMetaModelGeneration(final RoundEnvironment roundEnv, final Optional<MetaModelsElement> maybeMetaModelsElement) {
        final Set<TypeElement> metaModeledElements = roundEnv.getRootElements().stream()
                .mapMulti(TYPE_ELEMENT_FILTER)
                .filter(entityFinder::isEntityThatNeedsMetaModel)
                .collect(toSet());

        if (metaModeledElements.isEmpty()) {
            printNote("There are no subjects for meta-modeling.");
            return Stream.empty();
        }
        printNote(formatSequence("metaModeledElements", metaModeledElements.stream().map(Element::getSimpleName).iterator()));

        // let's process each type element representing a domain entity
        // all relevant types will have a meta-model concept created for them and their properties explored for the purpose of meta-modelling
        final Set<MetaModelConcept> metaModelConcepts = new HashSet<>();
        final var existingMetaModels = maybeMetaModelsElement.map(mme -> mme.getMetaModels().stream().map(m -> m.getQualifiedName().toString()).collect(toSet())).orElse(Collections.emptySet());
        for (final TypeElement typeElement: metaModeledElements) {
            final EntityElement entityElement = entityFinder.newEntityElement(typeElement);
            final MetaModelConcept mmc = new MetaModelConcept(entityElement);
            if (!allGeneratedMetaModels.contains(mmc)) {
                // domain entities that came in the round environment should have their meta-models generated always.
                metaModelConcepts.add(mmc);

                // traverse all properties for the current entity element to ensure that any entity-typed properties get their type considered for meta-model generation
                // this is mainly important to pick up entity types that come from other project dependencies, such as the TG platform itself
                explore(entityElement, existingMetaModels, metaModelConcepts);
            }
        }

        printNote(formatSequence("metaModelConcepts", metaModelConcepts.stream().map(MetaModelConcept::getSimpleName).iterator()));

        return metaModelConcepts.stream();
    }

    /// Analyses the structure of `entityElement` to identify other referenced entities that could be subject to meta-modelling.
    /// This is required to find those entity types that are not contained in the set of root elements of the round environment (e.g., platform entity types).
    /// This process proceeds recursively breadth-first with all the relevant meta-model concepts added to the `metaModelConcepts` collection.
    ///
    private void explore(final EntityElement entityElement, final Set<String> existingMetaModels, final Set<MetaModelConcept> metaModelConcepts) {
        final var mccs = Stream.concat(entityFinder.streamParents(entityElement),
                                       entityFinder.findProperties(entityElement)
                                               .stream()
                                               .filter(pel -> entityFinder.isEntityType(pel.getType()))
                                               .map(pel -> entityFinder.newEntityElement(pel.getTypeAsTypeElementOrThrow())))
                .filter(entityFinder::isEntityThatNeedsMetaModel)
                .map(MetaModelConcept::new)
                // we should not include types that already have their meta-models generated or meta-model concepts included for processing
                .filter(mmc -> !existingMetaModels.contains(mmc.getQualifiedName()) && !metaModelConcepts.contains(mmc))
                .collect(toSet());
        // if there were some types identified, then add them as meta-model concepts for generation and recursively process properties of those types...
        if (!mccs.isEmpty()) {
            metaModelConcepts.addAll(mccs);
            // recursively process each type...
            mccs.stream()
                    .map(MetaModelConcept::getEntityElement)
                    .forEach(ee -> explore(ee, existingMetaModels, metaModelConcepts));
        }
    }

    /// Re-generates a meta-model class, represented by `mme`, as an interface with default visibility in order to make it unusable.
    /// This is necessary to invalidate meta-models for entities that have been removed or are no longer recognised as domain entities.
    ///
    private boolean writeInactiveMetaModel(final MetaModelElement mme) {
        final TypeSpec.Builder emptyMetaModelBuilder = TypeSpec.interfaceBuilder(mme.getSimpleName().toString());

        // @Generated annotation
        final String dateString = DateTimeUtils.toIsoFormat(DateTimeUtils.zonedNow());
        final AnnotationSpec annotGenerated = buildAtGenerated(dateString);

         emptyMetaModelBuilder
                .addJavadoc("INACTIVE auto-generated meta-model.\n<p>\n")
                .addJavadoc(format("Generation datetime: %s\n<p>\n", dateString))
                .addJavadoc(format("Generated by {@link %s}\n<p>\n", this.getClass().getCanonicalName()))
                .addAnnotation(annotGenerated);

        final TypeSpec emptyMetaModel = emptyMetaModelBuilder.build();

        // ######################## WRITE TO FILE #####################
        final JavaFile javaFile = JavaFile.builder(mme.getPackageName(), emptyMetaModel).indent(INDENT).build();
        try {
            javaFile.writeTo(filer);
        } catch (final IOException ex) {
            printWarning("Failed to generate empty meta-model [%s]. %s", mme.getSimpleName(), ex.getMessage());
            return false;
        }

        printNote("Generated empty meta-model [%s].", mme.getSimpleName());
        return true;
    }

    /// Check the content of `metaModelsElement` to identify fields that represent meta-models for entities, which do not exists or are not domain entities.
    ///
    private Set<MetaModelElement> findInactiveMetaModels(final MetaModelsElement metaModelsElement) {
        printNote("Verifying [%s].", metaModelsElement.getSimpleName());
        final Set<MetaModelElement> inactive = new LinkedHashSet<>();

        for (final MetaModelElement mme: metaModelsElement.getMetaModels()) {
            final Optional<EntityElement> maybeEntity = entityFinder.findEntityForMetaModel(mme);

            if (maybeEntity.isEmpty()) {
                printNote(format("Entity for [%s] does not exist anymore.", mme.getSimpleName()));
                inactive.add(mme);
            } else {
                final EntityElement entity = maybeEntity.get();
                // don't consider entities with an unresolved supertype inactive to avoid excessive regeneration
                if (entity.getSuperclass().getKind() != TypeKind.ERROR && !entityFinder.isEntityThatNeedsMetaModel(entity)) {
                    printNote(format("Entity [%s] is no longer a domain entity.", entity.getSimpleName()));
                    inactive.add(mme);
                }
            }
        }

        return inactive;
    }

    /// A convenient wrapper invoking [#writeMetaModel(MetaModelConcept,Predicate)] with predicate `EntityFinder::isPropertyOfDomainEntityType` to generate matching fields having the type of corresponding meta-model.
    ///
    private boolean writeMetaModel(final MetaModelConcept mmc) {
        return writeMetaModel(mmc, entityFinder::isPropertyOfDomainEntityType);
    }

    /// Generates and writes a meta-model source file for an entity, represented by `mmc`.
    ///
    /// A meta-model is generated in 2 forms:
    ///
    /// -  A regular meta-model which extends [EntityMetaModel], and
    /// -  An aliased meta-model which extends the first one and provides aliasing capabilities.
    ///
    /// Therefore, 2 meta-model source files are generated for every entity.
    ///
    /// Properties, which test positive for COVID... `propertyTypeMetamodeledTest` are modelled with a respective meta-model type.
    /// All other properties, unless their type cannot be resovled, are modelled with [PropertyMetaModel],
    /// which are terminal and do not support property traversing.
    ///
    /// If an entity extends another meta-modelled entity, then the meta-model for the latter will be used as a super class
    /// for the meta-model generated for the former.
    ///
    /// @return `true` if the meta-model was successfully generated, `false` otherwise
    ///
    private boolean writeMetaModel(final MetaModelConcept mmc, final Predicate<PropertyElement> propertyTypeMetamodeledTest) {
        final String thisMetaModelName = mmc.getSimpleName();
        final String thisMetaModelPkgName = mmc.getPackageName();

        final EntityElement entityElement = mmc.getEntityElement();
        final EntityElement entitySupertype = entityFinder.getParent(entityElement).orElse(null);
        if (entitySupertype == null) {
            messager.printMessage(Kind.WARNING,
                    "Entity [%s] has no parent entity type, won't be meta-modelled.".formatted(entityElement.getQualifiedName()),
                    entityElement.element());
            return false;
        }

        final Optional<EntityElement> maybeMetaModelledSupertype = Optional.ofNullable(entityFinder.isEntityThatNeedsMetaModel(entitySupertype) ? entitySupertype : null);
        final Collection<PropertyElement> properties;
        try {
            properties = collectProperties(entityElement, maybeMetaModelledSupertype);
        } catch (final EntitySourceDefinitionException e) {
            messager.printMessage(Kind.WARNING,
                    "Failed to generate meta-model for [%s]. Reason: %s".formatted(entityElement.getSimpleName(), e.getLocalizedMessage()),
                    entityElement.element());
            return false;
        }

        // ######################## FIELDS ###########################
        final SortedSet<FieldSpec> fieldSpecs = new TreeSet<>((f1, f2) -> f1.name.compareTo(f2.name));
        // first generate field TYPE for a convenient access to the type of a modelled entity
        final ClassName entityClassName = entityElement.getEntityClassName();
        final ParameterizedTypeName entityType = ParameterizedTypeName.get(ClassName.get(Class.class), entityClassName);
        fieldSpecs.add(FieldSpec.builder(entityType, "TYPE")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("$L.class", entityClassName)
                .build());
        // now let's process all properties
        for (final PropertyElement prop: properties) {
            final var propName = prop.getSimpleName().toString();
            final var fieldNameForProp = fieldNameForProp(propName);
            final var propName_ = propName + "_";
            // ### static property holding the property's name ###
            // private static final String ${PROPERTY}_ = "${PROPERTY}";
            fieldSpecs.add(FieldSpec.builder(String.class, propName_)
                                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                                    .initializer("$S", propName)
                                    .build());
            // ### instance property ###
            final FieldSpec.Builder fieldSpecBuilder;
            if (propertyTypeMetamodeledTest.test(prop)) {
                final MetaModelConcept propTypeMmc = new MetaModelConcept(entityFinder.newEntityElement(prop.getTypeAsTypeElementOrThrow()));
                final ClassName propTypeMmcClassName = propTypeMmc.getMetaModelClassName();
                // property type is target for meta-model generation
                // private Supplier<${METAMODEL}> ${PROPERTY};
                final ParameterizedTypeName propTypeName = ParameterizedTypeName.get(ClassName.get(Supplier.class), propTypeMmcClassName);
                fieldSpecBuilder = FieldSpec.builder(propTypeName, fieldNameForProp)
                                            .addModifiers(Modifier.PRIVATE);
            } else {
                // private final PropertyMetaModel ${PROPERTY};
                fieldSpecBuilder = FieldSpec.builder(ClassName.get(PropertyMetaModel.class), fieldNameForProp)
                                            .addModifiers(Modifier.PRIVATE, Modifier.FINAL);
            }
            fieldSpecs.add(fieldSpecBuilder.build());
        }

        // ######################## METHODS ###########################
        // methods to access property meta-models
        final List<MethodSpec> methodSpecs = new ArrayList<>();
        for (final PropertyElement prop: properties) {
            final var propName = prop.getSimpleName().toString();
            final var fieldNameForProp = fieldNameForProp(propName);
            final MethodSpec.Builder methodSpecBuilder = MethodSpec.methodBuilder(propName);

            final ClassName propTypeMmcClassName;
            if (propertyTypeMetamodeledTest.test(prop)) {
                final MetaModelConcept propTypeMmc = new MetaModelConcept(entityFinder.newEntityElement(prop.getTypeAsTypeElementOrThrow()));
                propTypeMmcClassName = propTypeMmc.getMetaModelClassName();
                /* property type is target for meta-model generation

                public ${METAMODEL} ${PROPERTY}() {
                    return ${PROPERTY}.get();
                }
                 */
                methodSpecBuilder.addModifiers(Modifier.PUBLIC)
                                 .returns(propTypeMmcClassName)
                                 .addStatement("return $L.get()", fieldNameForProp);
            } else {
                propTypeMmcClassName = null;
                /*
                public PropertyMetaModel ${PROPERTY}() {
                    return ${PROPERTY};
                }
                 */
                methodSpecBuilder.addModifiers(Modifier.PUBLIC)
                                 .returns(ClassName.get(PropertyMetaModel.class))
                                 .addStatement("return $L", fieldNameForProp);
            }

            buildJavadoc(prop, methodSpecBuilder, propTypeMmcClassName);
            methodSpecs.add(methodSpecBuilder.build());
        }

        // also need to override generic getEntityClass() to return TYPE
        /*
        @Override
        public static Class<${ENTITY}> getEntityClass() {
            return TYPE;
        }
         */
        final ClassName abstractEntityClassName = ClassName.get(AbstractEntity.class);
        final ParameterizedTypeName returnType = ParameterizedTypeName.get(ClassName.get(Class.class), WildcardTypeName.subtypeOf(abstractEntityClassName));
        final MethodSpec getModelMethod = MethodSpec
                .methodBuilder("getEntityClass")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(returnType)
                .addStatement("return TYPE")
                .build();
        methodSpecs.add(getModelMethod);

        // ######################## CONSTRUCTORS ######################

        /*
        public ${METAMODEL}(String path) {
            super(path);
            ...
        }
         */
        final MethodSpec.Builder constructorBuilder = MethodSpec
                .constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String.class, "path", Modifier.FINAL)
                .addStatement("super(path)");

        final CodeBlock.Builder constructorStatementsBuilder = CodeBlock.builder();
        for (final PropertyElement prop: properties) {
            final var propName = prop.getSimpleName().toString();
            final var fieldNameForProp = fieldNameForProp(propName);
            final var propName_ = propName + "_";
            if (propertyTypeMetamodeledTest.test(prop)) {
                final MetaModelConcept propTypeMmc = new MetaModelConcept(entityFinder.newEntityElement(prop.getTypeAsTypeElementOrThrow()));
                final ClassName propTypeMetaModelClassName = propTypeMmc.getMetaModelClassName();

                /* property type is target for meta-model generation

                this.${PROPERTY} = () -> {
                    ${METAMODEL} value = new ${METAMODEL} ( joinPath( ${PROPERTY}_ ) );
                    // this is an optimisation technique to avoid meta-model re-instantiation after the initial instantiation
                    ${PROPERTY} = () -> value;
                    return value;
                };
                 */
                final CodeBlock lambda = CodeBlock.builder()
                        .add("() -> {\n").indent()
                        .addStatement(
                                "$T $L = new $T(joinPath($L))",
                                propTypeMetaModelClassName, "value", propTypeMetaModelClassName, propName_)
                        .addStatement(
                                "$L = () -> $L",
                                fieldNameForProp, "value")
                        .addStatement("return $L", "value")
                        .unindent().add("}")
                        .build();
                final CodeBlock code = CodeBlock.builder()
                        .addStatement("this.$L = $L", fieldNameForProp, lambda.toString())
                        .build();
                constructorStatementsBuilder.add(code);
            } else {
                // this.${PROPERTY} = new PropertyMetaModel ( joinPath( ${PROPERTY}_ ) );
                constructorStatementsBuilder.addStatement(
                        "this.$L = new $T(joinPath($L))",
                        fieldNameForProp, ClassName.get(PropertyMetaModel.class), propName_);
            }
        }

        final MethodSpec constructor = constructorBuilder.addCode(constructorStatementsBuilder.build()).build();

        // the default constructor
        /*
        public ${METAMODEL} {
            this("");
        }
         */
        final MethodSpec defaultConstructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addStatement("this(\"\")")
                .build();

        final List<MethodSpec> constructors = List.of(constructor, defaultConstructor);

        // ######################## CLASS ######################
        /*
        @MetaModelForType<$T.class>
        public class ${METAMODEL} extends [EntityMetaModel | ${PARENT_METAMODEL}] {
            ...
        }
         */

        // sort methods alphabetically
        methodSpecs.sort((ms1, ms2) -> ms1.name.compareTo(ms2.name));

        // Let's now create a meta-model class by putting all the parts together
        final TypeSpec.Builder metaModelBuilder = TypeSpec.classBuilder(thisMetaModelName)
                .addModifiers(Modifier.PUBLIC)
                .superclass(determineMetaModelSuperClassName(entitySupertype, maybeMetaModelledSupertype.isPresent()))
                .addFields(fieldSpecs)
                .addMethods(constructors)
                .addMethods(methodSpecs)
                .build().toBuilder();

        // javadoc
        final Pair<String, String> entityTitleAndDesc = entityFinder.getEntityTitleAndDesc(entityElement);
        final String title = entityTitleAndDesc.getKey();
        if (!StringUtils.isEmpty(title)) {
            metaModelBuilder.addJavadoc(format("Title: %s\n<p>\n", title)).build();
        }

        final String desc = entityTitleAndDesc.getValue();
        if (!StringUtils.isEmpty(desc)) {
            metaModelBuilder.addJavadoc(format("Description: %s\n<p>\n", desc)).build();
        }

        // @MetaModelForType annotation
        final AnnotationSpec annotMetaModelForType = AnnotationSpec.builder(MetaModelForType.class)
            .addMember("value", "$T.class", entityClassName)
            .build();

        // @Generated annotation
        final String dateString = DateTimeUtils.toIsoFormat(DateTimeUtils.zonedNow());
        final AnnotationSpec annotGenerated = buildAtGenerated(dateString);

        final TypeSpec metaModelSpec = metaModelBuilder.addJavadoc("Auto-generated meta-model for {@link $T}.\n<p>\n", entityClassName)
                 .addJavadoc(format("Generation datetime: %s\n<p>\n", dateString))
                 .addJavadoc(format("Generated by {@link %s}\n<p>\n", this.getClass().getCanonicalName()))
                 .addAnnotation(annotGenerated)
                 .addAnnotation(annotMetaModelForType)
                 .build();

        // ######################## WRITE TO FILE #####################
        final JavaFile metaModelJavaFile = JavaFile.builder(thisMetaModelPkgName, metaModelSpec).indent(INDENT).build();
        try {
            metaModelJavaFile.writeTo(filer);
        } catch (final IOException ex) {
            printWarning("Failed to generate meta-model [%s]. %s", metaModelSpec.name, ex.getMessage());
            return false;
        }

        // ############## Generate an aliased meta-model #############
        /*
        @MetaModelForType<$T.class>
        public class ${METAMODEL_ALIASED} extends ${METAMODEL} {
            public final String alias;

            public ${METAMODEL_ALIASED}(final String alias) {
                super(alias);
                if (alias.isBlank()) {
                    throw new EntityMetaModelAliasedException("Alias can't be blank");
                }
                this.alias = alias;
            }

            @Override
            public String toPath() {
                return this.path.isEmpty() ? this.alias : this.path;
            }
        }
         */
        final String thisMetaModelAliasedName = mmc.getAliasedSimpleName();
        final TypeSpec metaModelAliasedSpec = TypeSpec.classBuilder(thisMetaModelAliasedName)
                .addAnnotation(annotGenerated)
                .addAnnotation(annotMetaModelForType)
                .addModifiers(Modifier.PUBLIC)
                .superclass(ClassName.get(thisMetaModelPkgName, thisMetaModelName))
                .addField(String.class, "alias", Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(String.class, "alias", Modifier.FINAL)
                        .addStatement("super(alias)")
                        .addCode(CodeBlock.builder()
                                .beginControlFlow("if (alias.isBlank())")
                                    .addStatement("throw new $T($S)", EntityMetaModelAliasedException.class, "An alias cannot be blank.")
                                .endControlFlow()
                                .addStatement("this.alias = alias")
                                .build())
                        .build())
                .addMethod(MethodSpec.methodBuilder("toPath")
                        .addModifiers(Modifier.PUBLIC)
                        .addAnnotation(Override.class)
                        .addAnnotation(Nonnull.class)
                        .returns(String.class)
                        .addStatement("return this.path.isEmpty() ? this.alias : this.path")
                        .build())
                .build();

        final JavaFile metaModelAliasedJavaFile = JavaFile.builder(thisMetaModelPkgName, metaModelAliasedSpec).indent(INDENT).build();
        try {
            metaModelAliasedJavaFile.writeTo(filer);
        } catch (final IOException ex) {
            printWarning("Failed to generate aliased meta-model [%s]. %s", metaModelAliasedSpec.name, ex.getMessage());
            return false;
        }

        // TODO enable for "verbose" mode
//        printNote("Generated %s for entity %s.", metaModelSpec.name, entityElement.getSimpleName());
        return true;
    }

    /// Collects entity properties for meta-modeling.
    ///
    private Collection<PropertyElement> collectProperties(
            final EntityElement entity,
            final Optional<EntityElement> maybeMetaModelledSupertype)
            throws EntitySourceDefinitionException
    {
        final Predicate<PropertyElement> erroneousPropertyFilter = prop -> {
            if (prop.getType().getKind() == TypeKind.ERROR) {
                messager.printMessage(Kind.WARNING,
                        "Property [%s] with unresolved type won't be meta-modelled.".formatted(prop.getSimpleName()),
                        prop.element());
                return false;
            }
            return true;
        };

        final Set<PropertyElement> properties;
        if (entityFinder.isUnionEntityType(entity)) {
            properties = entityFinder.findProperties(entity);
        }
        else if (maybeMetaModelledSupertype.isPresent()) {
            // declared properties + poperty key
            final var declaredProps = entityFinder.streamDeclaredProperties(entity).filter(erroneousPropertyFilter).toList();
            // "key" is guaranteed to exist
            final var keyElt = entityFinder.findProperty(maybeMetaModelledSupertype.get(), AbstractEntity.KEY)
                    .orElseThrow(() -> new EntitySourceDefinitionException("Property [%s] was not found in [%s].".formatted(AbstractEntity.KEY, entity)));
            properties = ImmutableSetUtils.insert(entityFinder.processProperties(declaredProps, entity), keyElt);
        } else {
            properties = entityFinder.processProperties(entityFinder.streamProperties(entity).filter(erroneousPropertyFilter).toList(), entity);
        }

        // { propertyName : property }
        final var propertiesMap = new LinkedHashMap<String, PropertyElement>(
                properties.stream().collect(Collectors.toMap(elt -> elt.getSimpleName().toString(), Function.identity())));

        // Need additional processing of property "key" to correctly identify its type or to remove it.
        // An exception is thrown in case of erroneous entity definition.
        processPropertyKey(propertiesMap, entity);

        return unmodifiableCollection(propertiesMap.values());
    }

    /// Processes entity key type information, possibly modifying `properties` to update or remove the entry for property `key`.
    ///
    private void processPropertyKey(final LinkedHashMap<String, PropertyElement> properties, final EntityElement entity) {
        // Child entity simply inherits "key" property meta-model from its parent, but only if that super meta-model contains a model for "key"...
        // This is why, in order to simplify processing the situations where super meta-model does not have "key", we shall simply regenerate a meta-model for "key" for sub entities.
        //
        // Abstract entity types may have annotation @KeyType absent.
        // However, it may still be necessary to generate a meta-model for abstract entities.
        // Therefore, in such cases, property "key" should simply be removed from consideration.
        final Optional<KeyType> maybeKeyType = entityFinder.findAnnotation(entity, KeyType.class);
        if (entity.isAbstract() && maybeKeyType.isEmpty()) {
            properties.remove(AbstractEntity.KEY);
        }
        // In all other cases, if annotation @KeyType is present, we shall make sure that its type is updated to match the type from @KeyType
        else if (maybeKeyType.isPresent()) {
            final KeyType atKeyType = maybeKeyType.get();

            // obtain KeyType::value()
            final TypeMirror keyType = entityFinder.getKeyType(atKeyType);
            // Property "key" of type NoKey does not need to be meta-modelled
            if (isSameType(keyType, NoKey.class)) {
                properties.remove(AbstractEntity.KEY);
            } else {
                // mapping for "key" should always exist
                properties.compute(AbstractEntity.KEY, (name, propEl) -> propEl.changeType(keyType));
            }
        }
    }

    /// Determines appropriate super class for a meta-model to extend. It can either be the default meta-model super class or another meta-model class.
    ///
    private static ClassName determineMetaModelSuperClassName(final EntityElement entityParent, final boolean isEntitySuperclassMetamodeled) {
        if (isEntitySuperclassMetamodeled) {
            final MetaModelConcept parentMmc = new MetaModelConcept(entityParent);
            return ClassName.get(parentMmc.getPackageName(), parentMmc.getSimpleName());
        } else {
            return META_MODEL_SUPERCLASS_CLASSNAME;
        }
    }

    /// Add Javadoc to `specBuilder`, which describes an entity property.
    ///
    private void buildJavadoc(final PropertyElement prop, final MethodSpec.Builder specBuilder, final ClassName propTypeMmcClassName) {
        // javadoc: property title and description
        final Pair<String, String> propTitleAndDesc = entityFinder.getPropTitleAndDesc(prop);
        if (propTitleAndDesc != null) {
            final String propTitle = propTitleAndDesc.getKey();
            if (!StringUtils.isEmpty(propTitle)) {
                specBuilder.addJavadoc("Title: $L\n<p>\n", propTitle);
            }

            final String propDesc = propTitleAndDesc.getValue();
            if (!StringUtils.isEmpty(propDesc)) {
                specBuilder.addJavadoc("Description: $L\n<p>\n", propDesc);
            }
        }

        // javadoc: property type
        specBuilder.addJavadoc("Type: {@link $T}\n<p>\n", prop.getType());

        // (optional) javadoc: property type's meta-model
        if (propTypeMmcClassName != null) {
            specBuilder.addJavadoc("Meta-model: {@link $T}\n<p>\n", propTypeMmcClassName);
        }

        // javadoc: property annotations
        final List<String> annotationsStrings = elementFinder.getFieldAnnotations(prop).stream()
                .map(annotMirror -> {
                    final StringBuilder builder = new StringBuilder();
                    builder.append(format("{@literal @}{@link %s}", annotMirror.getAnnotationType().asElement().getSimpleName().toString()));
                    final Map<? extends ExecutableElement, ? extends AnnotationValue> valuesMap = annotMirror.getElementValues();
                    if (!valuesMap.isEmpty()) {
                        builder.append("(");
                        builder.append(join(", ", valuesMap.entrySet().stream()
                                .map(e -> format("%s = %s",
                                                 e.getKey().getSimpleName(),
                                                 e.getValue().toString().replaceAll("@", "{@literal @}")))
                                .toList()));
                        builder.append(")");
                    }
                    return builder.toString();
                })
                .toList();
        specBuilder.addJavadoc("$L", join("<br>\n", annotationsStrings));
    }


    private void writeMetaModelsClass(final Collection<MetaModelConcept> metaModelConcepts) {
        writeMetaModelsClass(metaModelConcepts, empty(), emptyList());
    }

    /// Generates a meta-model collection class, which serves as an entry-point for accessing domain meta-models at design time.
    ///
    /// @param metaModelConcepts         the new meta-models that should be added
    /// @param maybeMetaModelsElement    optionally provides an existing meta-models class, contents of which should be copied
    /// @param inactiveMetaModelElements the inactive meta-models that should be excluded
    ///
    private void writeMetaModelsClass(
            final Collection<MetaModelConcept> metaModelConcepts,
            final Optional<MetaModelsElement> maybeMetaModelsElement,
            final Collection<MetaModelElement> inactiveMetaModelElements)
    {
        printNote("Started generating the meta-models entry point...");

        final Set<MetaModelConcept> newMetaModelConcepts = maybeMetaModelsElement.map(elt -> {
            final Set<MetaModelElement> declaredMetaModels = elt.getMetaModels();
            return metaModelConcepts.stream()
                    .filter(mmc -> !declaredMetaModels.contains(MetaModelElement.asEqual(mmc, elementUtils::getName)))
                    .collect(toCollection(HashSet::new));
        }).orElseGet(() -> new HashSet<>(metaModelConcepts));

        if (newMetaModelConcepts.isEmpty() && inactiveMetaModelElements.isEmpty()) {
            printNote("Aborted meta-models entry point generation as there is nothing to change.");
            return;
        }

        /*
        public final class MetaModels {
            public static final ${METAMODEL} ${ENTITY_NAME}_ = new ${METAMODEL}();

            public static ${METAMODEL_ALIASED} ${ENTITY_NAME}_(final String alias) {
                final ${METAMODEL_ALIASED} aliased = ${METAMODEL_ALIASED}(alias);
                return aliased;
            }
        }
         */
        final SortedSet<FieldSpec> fieldSpecs = new TreeSet<>((f1, f2) -> f1.name.compareTo(f2.name));
        final SortedSet<MethodSpec> methodSpecs = new TreeSet<>((m1, m2) -> m1.name.compareTo(m2.name));

        // generate static fields and methods for new meta-models
        for (final MetaModelConcept mmc: newMetaModelConcepts) {
            final String fieldName = nameFieldForMetaModel(mmc.getEntityElement().getSimpleName().toString());
             // TODO enable for "verbose" mode
//            printNote("New/Updated meta-model, generating field: %s", fieldName);
            fieldSpecs.add(specFieldForMetaModel(mmc.getMetaModelClassName(), fieldName));
             // TODO enable for "verbose" mode
//            printNote("New/Updated aliased meta-model, generating method: %s", fieldName);
            methodSpecs.add(aliasMethodForMetaModel(mmc.getMetaModelAliasedClassName(), fieldName));
        }
        if (!metaModelConcepts.isEmpty()) {
            printNote("Found %s new/updated meta-models: [%s]",
                    metaModelConcepts.size(), metaModelConcepts.stream().map(MetaModelConcept::getSimpleName).collect(joining(",")));
        }

        // if MetaModels class exists, then generate static fields and methods for old meta-models, excluding inactive ones
        if (maybeMetaModelsElement.isPresent()) {
            final MetaModelsElement metaModelsElement = maybeMetaModelsElement.get();
            final Set<MetaModelElement> declaredMetaModels = new HashSet<>(metaModelsElement.getMetaModels());
            declaredMetaModels.removeAll(inactiveMetaModelElements);

            if (!inactiveMetaModelElements.isEmpty()) {
                printNote("Found %s inactive meta-models: [%s]",
                        inactiveMetaModelElements.size(),
                        inactiveMetaModelElements.stream() .map(mm -> mm.getSimpleName().toString()) .sorted().collect(joining(", ")));
            }

            {
                int oldMetaModelCount = 0;
                int oldAliasedMetaModelCount = 0;
                for (final MetaModelElement mme: declaredMetaModels) {
                    // active meta-model must have a coresponding entity
                    final EntityElement entity = entityFinder.findEntityForMetaModel(mme)
                            .orElseThrow(() -> new MetaModelProcessorException("Missing entity for active meta-model [%s]".formatted(mme.getSimpleName())));
                    final String fieldName = nameFieldForMetaModel(entity.getSimpleName().toString());
                    // add a method for an aliased meta-model
                    if (metaModelFinder.isMetaModelAliased(mme)) {
                        // TODO enable for "verbose" mode
                        //                    printNote("Old aliased meta-model, generating method: %s", fieldName);
                        methodSpecs.add(aliasMethodForMetaModel(mme.getMetaModelClassName(), fieldName));
                        oldMetaModelCount++;
                    }
                    // add a field for a regular meta-model
                    else {
                        // TODO enable for "verbose" mode
                        //                    printNote("Old meta-model, generating field: %s", fieldName);
                        fieldSpecs.add(specFieldForMetaModel(mme.getMetaModelClassName(), fieldName));
                        oldAliasedMetaModelCount++;
                    }
                }
                printNote("Unchanged meta-models: %s regular, %s aliased.".formatted(oldMetaModelCount, oldAliasedMetaModelCount));
            }
        }

        // @Generated annotation
        final String dateString = DateTimeUtils.toIsoFormat(DateTimeUtils.zonedNow());
        final AnnotationSpec annotGenerated = buildAtGenerated(dateString);

        final TypeSpec metaModelsTypeSpec = TypeSpec.classBuilder(METAMODELS_CLASS_SIMPLE_NAME)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addJavadoc(format("Generation datetime: %s\n<p>\n", dateString))
                .addJavadoc(format("Generated by {@link %s}.", this.getClass().getCanonicalName()))
                .addAnnotation(annotGenerated)
                .addFields(fieldSpecs)
                .addMethods(methodSpecs)
                .build();

        // ######################## WRITE TO FILE #####################
        final JavaFile javaFile = JavaFile.builder(METAMODELS_CLASS_PKG_NAME, metaModelsTypeSpec).indent(INDENT).build();
        try {
            javaFile.writeTo(filer);
        } catch (final IOException ex) {
            printWarning("Failed to generate [%s]. %s", metaModelsTypeSpec.name, ex.getMessage());
            return;
        }

        printNote("Finished generating the meta-models entry point as [%s].", metaModelsTypeSpec.name);
    }

    /// Creates a [FieldSpec] for field with name `fieldName` of type `metaModelClassName` in `MetaModels` for representing a reference to a domain meta-model.
    ///
    /// ```Java
    /// public static final ${METAMODEL} ${NAME} = new ${METAMODEL}();
    /// ```
    ///
    private static FieldSpec specFieldForMetaModel(final ClassName metaModelClassName, final String fieldName) {
        final var fieldSpec = FieldSpec.builder(metaModelClassName, fieldName)
                .initializer("new $T()", metaModelClassName)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .build();
        return fieldSpec;
    }

    /// Creates a [MethodSpec] for the <code>MetaModels</code> class to instantiate aliased meta-models.
    /// ```Java
    /// public static ${METAMODEL_ALIASED} ${NAME}(final String alias) {
    ///     final ${METAMODEL_ALIASED} aliased = new ${METAMODEL_ALIASED}(alias);
    ///     return aliased;
    /// }
    /// ```
    ///
    /// @param metaModelAliasedClassName name of the meta-model to be accessed
    /// @param methodName                name to create the method with
    ///
    private static MethodSpec aliasMethodForMetaModel(final ClassName metaModelAliasedClassName, final String methodName) {
        final MethodSpec method = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(metaModelAliasedClassName)
                .addParameter(String.class, "alias", Modifier.FINAL)
                .addStatement("final $T aliased = new $T(alias)", metaModelAliasedClassName, metaModelAliasedClassName)
                .addStatement("return aliased")
                .build();
        return method;
    }

    /// A helper method for naming fields in `MetaModels` that represent access points to domain meta-models.
    ///
    private static String nameFieldForMetaModel(final String simpleName) {
        return simpleName + "_";
    }

    /// Makes a field name to be used in a meta-model class to represent a entity property.
    ///
    private static String fieldNameForProp(String propName) {
        return propName + "_pn";
    }

}
