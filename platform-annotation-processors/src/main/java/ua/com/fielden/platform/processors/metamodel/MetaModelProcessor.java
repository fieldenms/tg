package ua.com.fielden.platform.processors.metamodel;

import static java.lang.String.format;
import static java.lang.String.join;
import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;
import static ua.com.fielden.platform.processors.metamodel.MetaModelConstants.ANNOTATIONS_THAT_TRIGGER_META_MODEL_GENERATION;
import static ua.com.fielden.platform.processors.metamodel.MetaModelConstants.METAMODELS_CLASS_PKG_NAME;
import static ua.com.fielden.platform.processors.metamodel.MetaModelConstants.METAMODELS_CLASS_QUAL_NAME;
import static ua.com.fielden.platform.processors.metamodel.MetaModelConstants.METAMODELS_CLASS_SIMPLE_NAME;
import static ua.com.fielden.platform.processors.metamodel.MetaModelConstants.METAMODEL_SUPERCLASS_CLASSNAME;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Generated;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic.Kind;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import com.google.auto.service.AutoService;
import com.google.common.base.Stopwatch;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;

import ua.com.fielden.platform.annotations.metamodel.ForType;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.processors.metamodel.concepts.MetaModelConcept;
import ua.com.fielden.platform.processors.metamodel.elements.EntityElement;
import ua.com.fielden.platform.processors.metamodel.elements.MetaModelElement;
import ua.com.fielden.platform.processors.metamodel.elements.MetaModelsElement;
import ua.com.fielden.platform.processors.metamodel.elements.PropertyElement;
import ua.com.fielden.platform.processors.metamodel.exceptions.EntityMetaModelAliasedException;
import ua.com.fielden.platform.processors.metamodel.models.EntityMetaModel;
import ua.com.fielden.platform.processors.metamodel.models.PropertyMetaModel;
import ua.com.fielden.platform.processors.metamodel.utils.ElementFinder;
import ua.com.fielden.platform.processors.metamodel.utils.EntityFinder;
import ua.com.fielden.platform.processors.metamodel.utils.MetaModelFinder;
import ua.com.fielden.platform.utils.Pair;

/**
 * Annotation processor that generates meta-models for domain entities.
 *
 * @author TG Team
 *
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes("*")
public class MetaModelProcessor extends AbstractProcessor {

    private static final String INDENT = "    ";

    private Filer filer;
    private Elements elementUtils;
    private Messager messager;
    private Map<String, String> options;

    private ElementFinder elementFinder;
    private EntityFinder entityFinder;
    private MetaModelFinder metaModelFinder;

    // this collection is used to track meta-models that were generated during one of the rounds of the current compilation cycle
    // this tracking helps avoiding attempts to regenerate already generated meta-models during the current compilation cycle, which otherwise results in FilerException
    private final Set<MetaModelConcept> allGeneratedMetaModels = new HashSet<>();
    
    private DateTime initDateTime;
    private int roundNumber;

    @Override
    public synchronized void init(final ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.initDateTime = DateTime.now();
        this.filer = processingEnv.getFiler();
        this.elementUtils = processingEnv.getElementUtils();
        this.messager = processingEnv.getMessager();
        this.options = processingEnv.getOptions();
        messager.printMessage(Kind.NOTE, format("Options: %s", options.keySet().stream().map(k -> format("%s=%s", k, options.get(k))).sorted().collect(joining(", "))));
        this.roundNumber = 0;

        this.elementFinder = new ElementFinder(processingEnv.getElementUtils(), processingEnv.getTypeUtils());
        this.entityFinder = new EntityFinder(processingEnv.getElementUtils(), processingEnv.getTypeUtils());
        this.metaModelFinder = new MetaModelFinder(processingEnv.getElementUtils(), processingEnv.getTypeUtils());

        messager.printMessage(Kind.NOTE, format("%s initialized.", this.getClass().getSimpleName()));
    }

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        this.roundNumber = this.roundNumber + 1;
        final Stopwatch stopwatchProcess = Stopwatch.createStarted();
        
        messager.printMessage(Kind.NOTE, format(">>> PROCESSING ROUND %d START >>>", roundNumber));
        messager.printMessage(Kind.NOTE, format("annotations: [%s]", annotations.stream().map(Element::getSimpleName).map(Name::toString).sorted().collect(joining(", "))));
        final Set<? extends Element> rootElements = roundEnv.getRootElements();
        messager.printMessage(Kind.NOTE, format("rootElements: [%s]", rootElements.stream().map(Element::getSimpleName).map(Name::toString).sorted().collect(joining(", "))));

        // we need the meta-model entry point if it exists, as it provides a definitive list of meta-models that have been generated previously
        // this information is used for processing optimisation and identification of meta-models that should be deactivated
        final Optional<MetaModelsElement> maybeMetaModelsElement = ofNullable(elementUtils.getTypeElement(METAMODELS_CLASS_QUAL_NAME))
                .map(metaModelsTypeElement -> new MetaModelsElement(metaModelsTypeElement,
                                                                    metaModelFinder.findMetaModels(metaModelsTypeElement)));
        
        // meta-models need to be generated at every processing round for the matching entities, which where included into the round by the compiler
        // except those that were already generated on one of the subsequent rounds during the current compilation process
        // also, if the meta-models entry point already exists, we should use it to identify existing meta-models to avoid excessive meta-model generation 
        final var generatedMetaModels = collectEntitiesForMetaModelGeneration(roundEnv, maybeMetaModelsElement)
                                        .filter(mmc -> writeMetaModel(mmc)).collect(Collectors.toSet());
        allGeneratedMetaModels.addAll(generatedMetaModels);

        // generation or re-generation of the meta-models entry point class should occur only during the first round of processing
        if (roundNumber == 1) {
            if (maybeMetaModelsElement.isEmpty()) {
                // if the MetaModels class does not yet exist, let's generate it to include meta-models, which were generated during this first round 
                writeMetaModelsClass(generatedMetaModels);
            } else {
                // if MetaModels class already exists, we need to analyse its content and identify meta-models that represent "inactive" entities -- those that either no longer exist or are not considered to be domain entities.
                // such meta-models need to be regenerated as empty and abstract classes and MetaModels should be re-generated without those fields
                messager.printMessage(Kind.NOTE, format("Entry point [%s] already exists. It needs to be processed and re-generated.", METAMODELS_CLASS_QUAL_NAME));

                // analyse fields in the MetaModels class to identify meta-models that do not represent domain entities any longer
                final Set<MetaModelElement> inactiveMetaModels = findInactiveMetaModels(maybeMetaModelsElement.get());

                messager.printMessage(Kind.NOTE, format("Inactive meta-models: [%s]", inactiveMetaModels.stream().map(MetaModelElement::getSimpleName).collect(joining(", "))));

                // deactivate meta-models by regenerating them as abstract classes to prevent their instantiation
                final Set<MetaModelElement> deactivedMetaModels = inactiveMetaModels.stream().filter(mme -> writeInactiveMetaModel(mme)).collect(toSet());

                // regenerate the MetaModels class by adding new fields and removing those that represent deactivated meta-models 
                writeMetaModelsClass(generatedMetaModels, maybeMetaModelsElement, deactivedMetaModels);
            }
        }

        stopwatchProcess.stop();
        messager.printMessage(Kind.NOTE, format("<<< PROCESSING ROUND %d END [%s millis] <<<", roundNumber, stopwatchProcess.elapsed(TimeUnit.MILLISECONDS)));
        // must return false to avoid claiming all annotations (as defined by @SupportedAnnotationTypes("*")) to allow other processors to run
        return false;
    }

    /**
     * Processes {@code roundEnv} to collect entity classes for processing.
     * Returns a set with instances of {@link MetaModelConcept}, representing each entity that require a meta-model to be generated.   
     *
     * @param roundEnv
     * @param maybeMetaModelsElement – qualified names for meta-models from the meta-models entry point class
     * @return
     */
    private Stream<MetaModelConcept> collectEntitiesForMetaModelGeneration(final RoundEnvironment roundEnv, final Optional<MetaModelsElement> maybeMetaModelsElement) {
        // find classes annotated with any of DOMAIN_TYPE_ANNOTATIONS
        final Set<TypeElement> annotatedElements = roundEnv.getElementsAnnotatedWithAny(ANNOTATIONS_THAT_TRIGGER_META_MODEL_GENERATION).stream()
                // just in case make sure identified elements are top-level classes
                // TODO support visible nested classes
                .filter(element -> elementFinder.isTopLevelClass(element))
                .map(el -> (TypeElement) el).collect(toSet());
        messager.printMessage(Kind.NOTE, format("annotatedElements: [%s]", annotatedElements.stream().map(Element::getSimpleName).map(Name::toString).sorted().collect(joining(", "))));
        if (annotatedElements.isEmpty()) {
            messager.printMessage(Kind.NOTE, "There is nothing to process.");
            return Stream.empty();
        }

        // let's process each type element representing a domain entity
        // all relevant types will have a meta-model concept created for them and their properties explored for the purpose of meta-modelling 
        final Set<MetaModelConcept> metaModelConcepts = new HashSet<>();
        final var existingMetaModels = maybeMetaModelsElement.map(mme -> mme.getMetaModels().stream().map(m -> m.getQualifiedName().toString()).collect(toSet())).orElse(Collections.emptySet());
        for (final TypeElement typeElement: annotatedElements) {
            final EntityElement entityElement = entityFinder.newEntityElement(typeElement);
            final MetaModelConcept mmc = new MetaModelConcept(entityElement);
            if (!allGeneratedMetaModels.contains(mmc)) {
                // domain entities that came in the round environment should have their meta-models generated always.
                metaModelConcepts.add(mmc);

                // traverse all properties for the current entity element to ensure that any entity-typed properties get their type considered for meta-model generation
                // this is mainly important to pick up entity types that come from other project dependencies, such as the TG platform itself
                explorePropsOf(entityElement, existingMetaModels, metaModelConcepts);
                
            }
        }

        messager.printMessage(Kind.NOTE, format("metaModelConcepts: [%s]", metaModelConcepts.stream().map(MetaModelConcept::getSimpleName).sorted().collect(joining(", "))));
        return metaModelConcepts.stream();
    }
    
    /**
     * Analyses properties of {@code entityElement} if they could be a subject of meta-modelling.
     * This process proceeds recursively breadth-first with all the relevant meta-model concepts added to the {@code metaModelConcepts} collection.
     *
     * @param entityElement
     * @param existingMetaModels
     * @param metaModelConcepts
     */
    private void explorePropsOf(final EntityElement entityElement, final Set<String> existingMetaModels, final Set<MetaModelConcept> metaModelConcepts) {
        final Set<MetaModelConcept> metaModelConceptsFromProps = entityFinder.findProperties(entityElement).stream()
                .filter(entityFinder::isPropertyOfDomainEntityType)
                .map(pel -> new MetaModelConcept(entityFinder.newEntityElement(pel.getTypeAsTypeElementOrThrow())))
                // we should not include types that already have their meta-models generated or meta-model concepts included for processing
                .filter(mmc -> !existingMetaModels.contains(mmc.getQualifiedName()) && !metaModelConcepts.contains(mmc))
                .collect(toSet());
        // if there were some types identified, then add them as meta-model concepts for generation and recursively process properties of those types...
        if (!metaModelConceptsFromProps.isEmpty()) {
            metaModelConcepts.addAll(metaModelConceptsFromProps);
            // recursively process each type...
            metaModelConceptsFromProps.stream()
            .map(MetaModelConcept::getEntityElement)
            .forEach(ee -> explorePropsOf(ee, existingMetaModels, metaModelConcepts));
        }
    }

    /**
     * Re-generates a meta-model class, represented by {@code mme}, as an interface with default visibility in order to make it unusable.
     * This is necessary to invalidate meta-models for entities that have been removed or are no longer recognised as domain entities. 
     *
     * @param mme
     * @return
     */
    private boolean writeInactiveMetaModel(final MetaModelElement mme) {
        final TypeSpec.Builder emptyMetaModelBuilder = TypeSpec.interfaceBuilder(mme.getSimpleName().toString());

        // @Generated annotation
        final AnnotationSpec generatedAnnotation = AnnotationSpec.builder(ClassName.get(Generated.class))
                .addMember("value", "$S", this.getClass().getCanonicalName())
                .addMember("date", "$S", initDateTime.toString())
                .build();
        final String datetime = initDateTime.toString("dd-MM-YYYY HH:mm:ss.SSS z");
         emptyMetaModelBuilder
                .addJavadoc("INACTIVE auto-generated meta-model.\n<p>\n")
                .addJavadoc(format("Generation datetime: %s\n<p>\n", datetime))
                .addJavadoc(format("Generated by {@link %s}\n<p>\n", this.getClass().getCanonicalName()))
                .addAnnotation(generatedAnnotation);

        final TypeSpec emptyMetaModel = emptyMetaModelBuilder.build();

        // ######################## WRITE TO FILE #####################
        final JavaFile javaFile = JavaFile.builder(mme.getPackageName(), emptyMetaModel).indent(INDENT).build();
        try {
            javaFile.writeTo(filer);
        } catch (final IOException ex) {
            messager.printMessage(Kind.WARNING, ex.getMessage());
            return false;
        }

        messager.printMessage(Kind.NOTE, format("Generated empty meta-model %s.", mme.getSimpleName().toString()));
        return true;
    }

    /**
     * Check the content of {@code metaModelsElement} to identify fields that represent meta-models for entities, which do not exists or are not domain entities.
     *
     * @param metaModelsElement
     * @return a set of inactive meta-models; could be empty
     */
    private Set<MetaModelElement> findInactiveMetaModels(final MetaModelsElement metaModelsElement) {
        messager.printMessage(Kind.NOTE, format("Verifying %s.", metaModelsElement.getSimpleName().toString()));
        final Set<MetaModelElement> inactive = new LinkedHashSet<>();
        for (final MetaModelElement mme: metaModelsElement.getMetaModels()) {
            final EntityElement entity = entityFinder.findEntityForMetaModel(mme);
            if (entity == null || !entityFinder.isEntityThatNeedsMetaModel(entity)) {
                if (entity != null) {
                    messager.printMessage(Kind.NOTE, format("Entity %s is no longer a domain entity.", entity.getSimpleName().toString()));
                } else {
                    messager.printMessage(Kind.NOTE, format("Entity for %s does not exist anymore.", mme.getSimpleName().toString()));
                }
                inactive.add(mme);
            }
        }
        return inactive;
    }

    /**
     * A convenient wrapper invoking {@link #writeMetaModel(MetaModelConcept, Predicate)} with predicate {@code EntityFinder::isPropertyOfDomainEntityType} to generate matching fields having the type of a corresponding meta-model.
     *
     * @param mmc
     * @return
     */
    private boolean writeMetaModel(final MetaModelConcept mmc) {
        return writeMetaModel(mmc, entityFinder::isPropertyOfDomainEntityType);
    }

    /**
     * Generates and writes a meta-model source file for an entity, represented by {@code mmc}.
     * <p>
     * A meta-model is generated in 2 forms:
     * <ol>
     *  <li> A regular meta-model which extends {@link EntityMetaModel}.
     *  <li> An aliased meta-model which extends 1. and provides aliasing capabilities.
     * </ol>
     * Therefore 2 source files are generated for every entity.
     * <p>
     * Properties, which test positive for COVID... {@code propertyTypeMetamodeledTest} are generated as such that have a meta-model on their own.
     * All other properties are generated as instances of {@link PropertyMetaModel}, which are terminal and do not support property traversing.
     * <p>
     * A super class for meta-models is determined based on the entity type hierarchy.
     * If an entity extends another domain entity (i.e., it has a meta-model), then the meta-model for that entity would be used as a super class for the meta-model being generated.
     *
     * @param mmc
     * @param propertyTypeMetamodeledTest
     * @return
     */
    private boolean writeMetaModel(final MetaModelConcept mmc, final Predicate<PropertyElement> propertyTypeMetamodeledTest) {
        final String thisMetaModelName = mmc.getSimpleName();
        final String thisMetaModelPkgName = mmc.getPackageName();

        final EntityElement entityElement = mmc.getEntityElement();
        final EntityElement entityParent = entityFinder.getParent(entityElement);
        final boolean isEntitySuperclassMetamodeled = entityFinder.isEntityThatNeedsMetaModel(entityParent);
        // collect properties to process
        final Set<PropertyElement> properties = new LinkedHashSet<>();
        if (isEntitySuperclassMetamodeled) {
            // find only declared properties
            properties.addAll(entityFinder.findDeclaredProperties(entityElement));
        } else {
            // find all properties (declared + inherited from <? extends AbstractEntity))
            properties.addAll(entityFinder.findProperties(entityElement));
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
                fieldSpecBuilder = FieldSpec.builder(propTypeName, propName)
                                            .addModifiers(Modifier.PRIVATE);
            } else {
                // private final PropertyMetaModel ${PROPERTY}; 
                fieldSpecBuilder = FieldSpec.builder(ClassName.get(PropertyMetaModel.class), propName)
                                            .addModifiers(Modifier.PRIVATE, Modifier.FINAL);
            }
            fieldSpecs.add(fieldSpecBuilder.build());
        }

        // ######################## METHODS ###########################
        // methods to access property meta-models
        final List<MethodSpec> methodSpecs = new ArrayList<>();
        for (final PropertyElement prop: properties) {
            final var propName = prop.getSimpleName().toString();
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
                                 .addStatement("return $L.get()", propName);
            } else {
                propTypeMmcClassName = null;
                /*
                public PropertyMetaModel ${PROPERTY}() {
                    return ${PROPERTY};
                }
                 */
                methodSpecBuilder.addModifiers(Modifier.PUBLIC)
                                 .returns(ClassName.get(PropertyMetaModel.class))
                                 .addStatement("return $L", propName);
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
                                propName, "value")
                        .addStatement("return $L", "value")
                        .unindent().add("}")
                        .build();
                final CodeBlock code = CodeBlock.builder()
                        .addStatement("this.$L = $L", propName, lambda.toString())
                        .build();
                constructorStatementsBuilder.add(code);
            } else {
                // this.${PROPERTY} = new PropertyMetaModel ( joinPath( ${PROPERTY}_ ) );
                constructorStatementsBuilder.addStatement(
                        "this.$L = new $T(joinPath($L))", 
                        propName, ClassName.get(PropertyMetaModel.class), propName_);
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
        public class ${METAMODEL} extends [EntityMetaModel | ${PARENT_METAMODEL}] {
            ...
        }
         */

        // sort methods alphabetically
        methodSpecs.sort((ms1, ms2) -> ms1.name.compareTo(ms2.name));

        // Let's now create a meta-model class by putting all the parts together
        final TypeSpec.Builder metaModelBuilder = TypeSpec.classBuilder(thisMetaModelName)
                .addModifiers(Modifier.PUBLIC)
                .superclass(determineMetaModelSuperClassName(entityParent, isEntitySuperclassMetamodeled))
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
        
        // @ForType annotation
        final AnnotationSpec annotForType = AnnotationSpec.builder(ForType.class)
            .addMember("value", "$T.class", entityClassName)
            .build();
        

        // @Generated annotation
        final AnnotationSpec annotGenerated = AnnotationSpec.builder(Generated.class)
                .addMember("value", "$S", this.getClass().getCanonicalName())
                .addMember("date", "$S", initDateTime.toString())
                .build();

        final String datetime = initDateTime.toString("dd-MM-YYYY HH:mm:ss.SSS z");
        final TypeSpec metaModelSpec = metaModelBuilder.addJavadoc("Auto-generated meta-model for {@link $T}.\n<p>\n", entityClassName)
                 .addJavadoc(format("Generation datetime: %s\n<p>\n", datetime))
                 .addJavadoc(format("Generated by {@link %s}\n<p>\n", this.getClass().getCanonicalName()))
                 .addAnnotation(annotGenerated)
                 .addAnnotation(annotForType)
                 .build();

        // ######################## WRITE TO FILE #####################
        final JavaFile metaModelJavaFile = JavaFile.builder(thisMetaModelPkgName, metaModelSpec).indent(INDENT).build();
        try {
            metaModelJavaFile.writeTo(filer);
        } catch (final IOException ex) {
            messager.printMessage(Kind.WARNING, ex.getMessage());
            return false;
        }
        
        // ############## Generate an aliased meta-model #############
        /*
        public class ${METAMODEL_ALIASED} extends ${METAMODEL} {
            public final String alias;

            public ${METAMODEL_ALIASED}(String alias) {
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
                .addAnnotation(annotForType)
                .addModifiers(Modifier.PUBLIC)
                .superclass(ClassName.get(thisMetaModelPkgName, thisMetaModelName))
                .addField(String.class, "alias", Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(String.class, "alias")
                        .addStatement("super(alias)")
                        .addCode(CodeBlock.builder()
                                .beginControlFlow("if (alias.isBlank())")
                                .addStatement("throw new $T($S)", EntityMetaModelAliasedException.class,
                                        "Alias can't be blank")
                                .endControlFlow()
                                .addStatement("this.alias = alias")
                                .build())
                        .build())
                .addMethod(MethodSpec.methodBuilder("toPath")
                        .addModifiers(Modifier.PUBLIC)
                        .addAnnotation(Override.class)
                        .returns(String.class)
                        .addStatement("return this.path.isEmpty() ? this.alias : this.path")
                        .build())
                .build();

        final JavaFile metaModelAliasedJavaFile = JavaFile.builder(thisMetaModelPkgName, metaModelAliasedSpec).indent(INDENT).build();
        try {
            metaModelAliasedJavaFile.writeTo(filer);
        } catch (final IOException ex) {
            messager.printMessage(Kind.WARNING, ex.getMessage());
            return false;
        }

        messager.printMessage(Kind.NOTE, format("Generated %s for entity %s.", metaModelSpec.name, entityElement.getSimpleName().toString()));
        return true;
    }

    /**
     * Determines appropriate super class for a meta-model to extend. It can either be the default meta-model super class or another meta-model class.
     *
     * @param entityParent
     * @param isEntitySuperclassMetamodeled
     * @return
     */
    private static ClassName determineMetaModelSuperClassName(final EntityElement entityParent, final boolean isEntitySuperclassMetamodeled) {
        if (isEntitySuperclassMetamodeled) {
            final MetaModelConcept parentMmc = new MetaModelConcept(entityParent);
            return ClassName.get(parentMmc.getPackageName(), parentMmc.getSimpleName());
        } else {
            return METAMODEL_SUPERCLASS_CLASSNAME;
        }
    }

    /**
     * Add Javadoc to {@code specBuilder}, which describes an entity property.
     *
     * @param prop
     * @param specBuilder
     * @param propTypeMmcClassName
     * @return
     */
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
                    builder.append(format("{@literal @}{@link %s}", elementFinder.getAnnotationMirrorSimpleName(annotMirror)));
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


    private void writeMetaModelsClass(Collection<MetaModelConcept> metaModelConcepts) {
        writeMetaModelsClass(metaModelConcepts, empty(), emptyList());
    }

    /**
     * Generates a meta-models collection class that acts as a provider of meta-model instances and thus is to be used as an entry point.
     *
     * @param metaModelConcepts new meta-models that should be added
     * @param maybeMetaModelsElement optionally provides an existing meta-models class, contents of which should be copied
     * @param inactiveMetaModelElements inactive meta-models that should be excluded
     */
    private void writeMetaModelsClass(final Collection<MetaModelConcept> metaModelConcepts, final Optional<MetaModelsElement> maybeMetaModelsElement, final Collection<MetaModelElement> inactiveMetaModelElements) {
        messager.printMessage(Kind.NOTE, "Started generating the meta-models entry point...");
        if (metaModelConcepts.isEmpty() && inactiveMetaModelElements.isEmpty()) {
            messager.printMessage(Kind.NOTE, "Aborted generating the meta-models entry point as there are no meta-models to include.");
            return;
        }
        /*
        public final class MetaModels {
            private static final ConcurrentHashMap<EntityMetaModel, WeakReference<EntityMetaModel>> aliasedMetaModelCache = new ConcurrentHashMap<>();

            public static final ${METAMODEL} ${ENTITY_NAME}_ = new ${METAMODEL}();
            
            public static ${METAMODEL_ALIASED} ${ENTITY_NAME}_(String alias) {
                ${METAMODEL_ALIASED} aliased = ${METAMODEL_ALIASED}(alias);
                WeakReference<EntityMetaModel> previous = aliasedMetaModelCache.putIfAbsent(aliased, new WeakReference<>(aliased));
                return previous == null ? aliased : (${METAMODEL_ALIASED}) previous.get();
            }
        }
         */
        final SortedSet<FieldSpec> fieldSpecs = new TreeSet<>((f1, f2) -> f1.name.compareTo(f2.name));
        final SortedSet<MethodSpec> methodSpecs = new TreeSet<>((m1, m2) -> m1.name.compareTo(m2.name));
        
        // generate static field for caching aliased meta-models
        // store it in a separate variable so that it is not sorted and is generated as the first field
        final FieldSpec aliasedMetaModelCacheField = specFieldAliasedMetaModelCache("aliasedMetaModelCache");

        // generate static fields and methods for new meta-models
        for (final MetaModelConcept mmc: metaModelConcepts) {
            final String fieldName = nameFieldForMetaModel(mmc.getEntityElement().getSimpleName().toString());
            messager.printMessage(Kind.NOTE, format("New/Updated meta-model, generating field: %s", fieldName));
            fieldSpecs.add(specFieldForMetaModel(mmc.getMetaModelClassName(), fieldName));
            
            messager.printMessage(Kind.NOTE, format("New/Updated aliased meta-model, generating method: %s", fieldName));
            methodSpecs.add(aliasMethodForMetaModel(mmc.getMetaModelAliasedClassName(), fieldName));
        }

        // if MetaModels class exists, then collect its members for the active *unchanged* meta-models 
        if (maybeMetaModelsElement.isPresent()) {
            final MetaModelsElement metaModelsElement = maybeMetaModelsElement.get();
            final List<MetaModelElement> activeUnchangedMetaModels = metaModelsElement.getMetaModels().stream()
                    // skip inactive
                    .filter(mme -> !inactiveMetaModelElements.contains(mme))
                    // skip updated active
                    .filter(mme -> metaModelConcepts.stream().noneMatch(mmc -> metaModelFinder.isSameMetaModel(mmc, mme)))
                    .toList();

            messager.printMessage(Kind.NOTE, format("Inactive meta-models: [%s]", inactiveMetaModelElements.stream()
                    .map(mm -> mm.getSimpleName().toString())
                    .sorted().collect(joining(", "))));
            
            for (final MetaModelElement mme: activeUnchangedMetaModels) {
                final EntityElement entity = entityFinder.findEntityForMetaModel(mme);
                final String fieldName = nameFieldForMetaModel(entity.getSimpleName().toString());
                // add a method for an aliased meta-model
                if (metaModelFinder.isMetaModelAliased(mme)) {
                    messager.printMessage(Kind.NOTE, format("Old aliased meta-model, generating method: %s", fieldName));
                    methodSpecs.add(aliasMethodForMetaModel(mme.getMetaModelClassName(), fieldName));
                }
                // add a field for a regular meta-model
                else {
                    messager.printMessage(Kind.NOTE, format("Old meta-model, generating field: %s", fieldName));
                    fieldSpecs.add(specFieldForMetaModel(mme.getMetaModelClassName(), fieldName));
                }
            }
        }

        // @Generated annotation
        final AnnotationSpec generatedAnnotation = AnnotationSpec.builder(ClassName.get(Generated.class))
                .addMember("value", "$S", this.getClass().getCanonicalName())
                .addMember("date", "$S", initDateTime.toString())
                .build();

        final String dateTimeString = initDateTime.toString("dd-MM-YYYY HH:mm:ss.SSS z");
        final TypeSpec metaModelsTypeSpec = TypeSpec.classBuilder(METAMODELS_CLASS_SIMPLE_NAME)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addJavadoc(format("Generation datetime: %s\n<p>\n", dateTimeString))
                .addJavadoc(format("Generated by {@link %s}.", this.getClass().getCanonicalName()))
                .addAnnotation(generatedAnnotation)
                .addField(aliasedMetaModelCacheField)
                .addFields(fieldSpecs)
                .addMethods(methodSpecs)
                .build();

        // ######################## WRITE TO FILE #####################
        final JavaFile javaFile = JavaFile.builder(METAMODELS_CLASS_PKG_NAME, metaModelsTypeSpec).indent(INDENT).build();
        try {
            javaFile.writeTo(filer);
        } catch (final IOException ex) {
            messager.printMessage(Kind.WARNING, ex.getMessage());
            return;
        }

        messager.printMessage(Kind.NOTE, format("Finished generating the meta-models entry point as [%s].", metaModelsTypeSpec.name));
    }

    /**
     * Creates a {@link FieldSpec} for field with name {@code fieldName} of type {@code metaModelClassName} in {@code MetaModels} for representing a reference to a domain meta-model. 
     * <p>
     * <pre>
     * public static final ${METAMODEL} ${NAME} = new ${METAMODEL}();
     * </pre>
     * 
     * @param metaModelClassName
     * @param fieldName
     * @return
     */
    private static FieldSpec specFieldForMetaModel(final ClassName metaModelClassName, final String fieldName) {
        final var fieldSpec = FieldSpec.builder(metaModelClassName, fieldName)
                .initializer("new $T()", metaModelClassName)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .build();
        return fieldSpec;
    }

    /**
     * Creates a {@link FieldSpec} for a cache for aliased meta-models.
     * <p>
     * <pre>
     * private static final ConcurrentHashMap&lt;EntityMetaModel, WeakReference&lt;EntityMetaModel>>
     *     aliasedMetaModelCache = new ConcurrentHashMap<>();
     * </pre>
     * @param string name to create the field with
     * @return
     */
    private static FieldSpec specFieldAliasedMetaModelCache(final String fieldName) {
        // WeakReference<EntityMetaModel>
        final ParameterizedTypeName paramTypeNameWeakReference = ParameterizedTypeName.get(
                WeakReference.class, EntityMetaModel.class);
        // ConcurrentHashMap<EntityMetaModel, WeakReference<EntityMetaModel>>
        final ParameterizedTypeName fieldTypeName = ParameterizedTypeName.get(
                ClassName.get(ConcurrentHashMap.class), 
                ClassName.get(EntityMetaModel.class), paramTypeNameWeakReference);

        final FieldSpec fieldSpec = FieldSpec.builder(fieldTypeName, fieldName)
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .initializer("new $T<>()", ConcurrentHashMap.class)
                .build();
        return fieldSpec;
    }
    
    /**
     * Creates a {@link MethodSpec} for the <code>MetaModels</code> class to access aliased meta-models.
     * <pre>
     * public static ${METAMODEL_ALIASED} ${NAME}(String alias) {
     *     ${METAMODEL_ALIASED} aliased = new ${METAMODEL_ALIASED}(alias);
     *     WeakReference&lt;EntityMetaModel&gt; previous = aliasedMetaModelCache.putIfAbsent(aliased, new WeakReference<>(aliased));
     *     return previous == null ? aliased : (${METAMODEL_ALIASED}) previous.get();
     * }
     * 
     * </pre>
     * @param metaModelAliasedClassName name of the meta-model to be accessed
     * @param methodName name to create the method with
     * @return
     */
    private static MethodSpec aliasMethodForMetaModel(final ClassName metaModelAliasedClassName, final String methodName) {
        final MethodSpec method = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(metaModelAliasedClassName)
                .addParameter(String.class, "alias")
                .addStatement("$T aliased = new $T(alias)", metaModelAliasedClassName, metaModelAliasedClassName)
                .addStatement("$T<$T> previous = aliasedMetaModelCache.putIfAbsent(aliased, new $T<>(aliased))", WeakReference.class, EntityMetaModel.class, WeakReference.class)
                .addStatement("return previous == null ? aliased : ($T) previous.get()", metaModelAliasedClassName)
                .build();
        return method;
    }

    /**
     * A helper method for naming fields in {@code MetaModels} that represent access points to domain meta-models.
     *
     * @param simpleName
     * @return
     */
    private static String nameFieldForMetaModel(final String simpleName) {
        return simpleName + "_";
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
      return SourceVersion.latestSupported();
    }

}