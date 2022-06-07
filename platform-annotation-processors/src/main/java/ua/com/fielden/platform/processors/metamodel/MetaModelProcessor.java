package ua.com.fielden.platform.processors.metamodel;

import static java.lang.String.format;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Generated;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.api.LayoutComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.RootLoggerComponentBuilder;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.joda.time.DateTime;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;

import ua.com.fielden.platform.annotations.metamodel.DomainEntity;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.processors.metamodel.concepts.MetaModelConcept;
import ua.com.fielden.platform.processors.metamodel.elements.ElementFinder;
import ua.com.fielden.platform.processors.metamodel.elements.EntityElement;
import ua.com.fielden.platform.processors.metamodel.elements.EntityFinder;
import ua.com.fielden.platform.processors.metamodel.elements.MetaModelElement;
import ua.com.fielden.platform.processors.metamodel.elements.MetaModelFinder;
import ua.com.fielden.platform.processors.metamodel.elements.MetaModelsElement;
import ua.com.fielden.platform.processors.metamodel.elements.PropertyElement;
import ua.com.fielden.platform.processors.metamodel.models.PropertyMetaModel;
import ua.com.fielden.platform.utils.Pair;

@AutoService(Processor.class)
@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_16)
public class MetaModelProcessor extends AbstractProcessor {

    private static final String INDENT = "    ";
    private static final String LOG_FILENAME = "proc.log";
    private static final String ECLIPSE_OPTION_KEY = "projectdir";

    private Logger logger;
    private ProcessorLogger procLogger;
    private Filer filer;
    private Elements elementUtils;
    private Map<String, String> options;
    private Messager messager;
    private boolean fromMaven;
    private int roundCount;
    private DateTime initDateTime;
    private Types typeUtils;
    private boolean metaModelsClassVerified;
    // stores meta-models that are generated during the lifetime of this processor's instance
    // used as a class field so it can be accessed in each round, due to unpredictable incremental compilation environment of Eclipse
    // TODO use an approach with a separate file for storing a list of existing meta-models and an appropriate class
    private final Map<MetaModelConcept, Boolean> metaModelConcepts = new HashMap<>();
    private final Map<MetaModelElement, Boolean> inactiveMetaModels = new HashMap<>();
    
    static {
        System.out.println(format("%s class loaded.", MetaModelProcessor.class.getSimpleName()));
    }
    
    public static Set<Class<? extends Annotation>> getSupportedAnnotations() {
        return Set.of(MapEntityTo.class, DomainEntity.class);
    }

    private static ClassName getMetaModelClassName(final MetaModelElement element) {
        return ClassName.get(element.getPackageName(), element.getSimpleName());
    }

    private static ClassName getMetaModelClassName(MetaModelConcept mmc) {
        return ClassName.get(mmc.getPackageName(), mmc.getSimpleName());
    }
    
    private static ClassName getEntityClassName(EntityElement element) {
        return ClassName.get(element.getPackageName(), element.getSimpleName());
    }

    private static boolean isPropertyTypeMetamodeled(PropertyElement element) {
        TypeElement propType = null;
        try {
            propType = element.getTypeAsTypeElement();
        } catch (Exception e) {
            // property type is not a declared type
            return false;
        }

        return MetaModelConstants.isMetamodeled(propType);
    }
    
    private static String getEntityTitleFromClassName(EntityElement element) {
        final String entityName = element.getSimpleName();
        StringBuilder descriptiveName = new StringBuilder();

        for (int i = 0; i < entityName.length(); i++) {
            char c = entityName.charAt(i);
            if (i > 0 && Character.isUpperCase(c))
                descriptiveName.append(' ');
            descriptiveName.append(c);
        }
        
        return descriptiveName.toString();
    }

    @Override
    public synchronized void init(final ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.initDateTime = DateTime.now();
        this.filer = processingEnv.getFiler();
        this.elementUtils = processingEnv.getElementUtils();
        this.typeUtils = processingEnv.getTypeUtils();
        this.messager = processingEnv.getMessager();
        this.options = processingEnv.getOptions();
        this.roundCount = 0;
        this.metaModelConcepts.clear();
        this.inactiveMetaModels.clear();
        this.metaModelsClassVerified = false;

        // processor started from Eclipse?
        final String projectDir = options.get(ECLIPSE_OPTION_KEY);
        this.fromMaven = projectDir == null;

        // log4j configuration
        Configurator.initialize(getLog4jConfig());
        this.logger = LogManager.getLogger(MetaModelProcessor.class);

        // ProcessorLogger
        final String logFilename = this.fromMaven ? LOG_FILENAME : projectDir + "/" + LOG_FILENAME;
        final String source = this.fromMaven ? "mvn" : "Eclipse";
        this.procLogger = new ProcessorLogger(logFilename, source, logger);
        procLogger.info(format("%s initialized.", this.getClass().getSimpleName()));

        // debug 
        procLogger.debug("Options: " + Arrays.toString(options.keySet().stream()
                                                        .map(k -> format("%s=%s", k, options.get(k)))
                                                        .toArray()));
//        if (Files.exists(Path.of(projectDir + "/target/generated-sources"))) {
//            try {
//                procLogger.debug("target/generated-sources: " + Arrays.toString(Files.walk(Path.of(projectDir + "/target/generated-sources"))
//                        .filter(Files::isRegularFile)
//                        .map(p -> p.getFileName().toString().split("\\.java")[0])
//                        .toArray()));
//            } catch (IOException e) {
//                procLogger.error(e.toString());
//            }
//        }
    }

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        final int roundNumber = ++this.roundCount;
        procLogger.debug(format("=== PROCESSING ROUND %d START ===", roundNumber));
        procLogger.debug("annotations: " + Arrays.toString(annotations.stream().map(Element::getSimpleName).toArray()));
        final Set<? extends Element> rootElements = roundEnv.getRootElements();
        procLogger.debug("rootElements: " + Arrays.toString(rootElements.stream().map(Element::getSimpleName).toArray()));

        // TODO detect when rootElements are exclusively test sources and exit


        // find elements annotated with any of @DomainEntity, @MapEntityTo
        final Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWithAny(getSupportedAnnotations());
        procLogger.debug("annotatedElements: " + Arrays.toString(annotatedElements.stream().map(Element::getSimpleName).toArray()));

        // generate meta-models for these elements
        for (Element element: annotatedElements) {
            if (element.getKind() != ElementKind.CLASS) {
                continue;
            }

            final TypeElement typeElement = (TypeElement) element;

            // sanity check that may save you from a bug in the future
            if (!MetaModelConstants.isMetamodeled(typeElement))
                continue;

            final EntityElement entityElement = newEntityElement(typeElement);
            final MetaModelConcept mmc = new MetaModelConcept(entityElement);
            this.metaModelConcepts.putIfAbsent(mmc, false);

            // TODO optimize by finding platform-level entities EXCLUSIVELY, this may save a lot of computation time
            // find properties of this entity that are entity type and include these entities for meta-model generation
            // this helps find entities that are included from the platform, rather than defined by a domain model, such as User
            final List<EntityElement> platformEntities = EntityFinder.findDistinctProperties(entityElement, PropertyElement::getName).stream()
                    .filter(MetaModelProcessor::isPropertyTypeMetamodeled)
                    // it's safe to call getTypeAsTypeElementOrThrow() since elements were previously filtered
                    .map(propEl -> new EntityElement(propEl.getTypeAsTypeElementOrThrow(), elementUtils))
                    .toList(); 
            platformEntities.stream()
                .map(MetaModelConcept::new)
                .forEach(mmc1 -> this.metaModelConcepts.putIfAbsent(mmc1, false));
        }

        procLogger.debug("metaModelConcepts: " + Arrays.toString(this.metaModelConcepts.keySet().stream().map(MetaModelConcept::getSimpleName).toArray()));

        for (MetaModelConcept mmc: getGenerationTargets(this.metaModelConcepts)) {
            if (writeMetaModel(mmc)) {
                markAsGenerated(mmc);
            }
        }

        final TypeElement metaModelsTypeElement = elementUtils.getTypeElement(MetaModelConstants.METAMODELS_CLASS_QUAL_NAME);

        // if MetaModels class exists
        if (metaModelsTypeElement != null) { 
            procLogger.debug(format("%s found.", MetaModelConstants.METAMODELS_CLASS_QUAL_NAME));
            final MetaModelsElement metaModelsElement = new MetaModelsElement(metaModelsTypeElement, elementUtils);

            // verify MetaModels
            if (!this.metaModelsClassVerified) {
                final List<MetaModelElement> inactive = findInactiveMetaModels(metaModelsElement);
                this.metaModelsClassVerified = true;

                procLogger.debug("Inactive meta-models: " + Arrays.toString(inactive.stream().map(MetaModelElement::getSimpleName).toArray()));

                for (MetaModelElement imm: inactive)
                    this.inactiveMetaModels.putIfAbsent(imm, false);

                if (!this.inactiveMetaModels.isEmpty()) {
                    final List<MetaModelElement> regenerationTargets = getGenerationTargets(this.inactiveMetaModels);
                    // handle inactive meta-models
                    regenerateInactiveMetaModels(regenerationTargets);
                    // regenerate meta-models that reference the inactive ones
                    final List<MetaModelElement> activeMetaModels = metaModelsElement.getMetaModels().stream()
                            .filter(mme -> !regenerationTargets.contains(mme))
                            .toList();
                    regenerateMetaModelsWithReferenceTo(activeMetaModels, regenerationTargets);
                }
            }
            
            //  TODO delete inactive meta-models java sources
//            final boolean deleted = deleteJavaSources(inactiveMetaModels);

            if (!this.metaModelConcepts.isEmpty() || !this.inactiveMetaModels.isEmpty()) {
                //  regenerate the MetaModels class by adding new fields and removing inactive ones
                procLogger.debug(format("Regenerating %s.", metaModelsElement.getSimpleName()));
                writeMetaModelsClass(this.metaModelConcepts.keySet(), metaModelsElement, this.inactiveMetaModels.keySet());
            }
        } else {
            if (!this.metaModelConcepts.isEmpty()) {
                // generate the MetaModels class
                writeMetaModelsClass(this.metaModelConcepts.keySet());
            }
        }
        
        // everything has been regenerated up to this point
        this.metaModelConcepts.clear();
        this.inactiveMetaModels.clear();

        endRound(roundNumber, roundEnv.processingOver());
        return false;
    }

    private <T> List<T> getGenerationTargets(Map<T, Boolean> metaModels) {
        return metaModels.entrySet().stream()
                .filter(e -> e.getValue().equals(Boolean.FALSE))
                .map(Entry::getKey)
                .toList();
    }
    
    private void markAsGenerated(MetaModelConcept metaModelConcept) {
        this.metaModelConcepts.put(metaModelConcept, true);
    }

    private void markAsRegenerated(MetaModelElement inactiveMetaModelElement) {
        this.inactiveMetaModels.put(inactiveMetaModelElement, true);
    }
    
    private void endRound(final int n, final boolean processingOver) {
        procLogger.debug(format("xxx PROCESSING ROUND %d END xxx", n));
        if (processingOver) {
            procLogger.info("### LAST ROUND. PROCESSING OVER ###");
            procLogger.ln();
            procLogger.ln();
            procLogger.end();
        }
    }

    /**
     * Regenerates the meta-models that reference any of the {@code referencedMetaModels}.
     * @param metaModels - a collection of meta-models to be regenerated if a reference is found
     * @param referencedMetaModels - the set of referenced meta-models
     * @return
     */
    private List<MetaModelElement> regenerateMetaModelsWithReferenceTo(Collection<MetaModelElement> metaModels, List<MetaModelElement> referencedMetaModels) {
        final List<MetaModelElement> regenerated = new ArrayList<>();

        for (MetaModelElement mme: metaModels) {
            final Set<MetaModelElement> referencedByThisMetaModel = MetaModelFinder.findReferencedMetaModels(mme, elementUtils);
            // if the set of referenced meta-models intersects with the set of trigger meta-models - regenerate this meta-model
            if (!Collections.disjoint(referencedByThisMetaModel, referencedMetaModels)) {
                final Set<MetaModelElement> intersection = Set.copyOf(referencedByThisMetaModel);
                intersection.retainAll(referencedMetaModels);
                procLogger.debug(format("%s references %s. Regenerating.", mme.getSimpleName(), Arrays.toString(intersection.stream().map(MetaModelElement::getSimpleName).toArray())));
                if (writeMetaModel(mme))
                    regenerated.add(mme);
            }
        }
        return regenerated;
    }

    /**
     * Regenerates meta-models for entities that have a property of any of the entity types provided by {@code referencedEntities}.
     * @param metaModels
     * @param referencedEntities
     */
    private List<MetaModelElement> regenerateMetaModelsForEntitiesWithReferenceTo(final Collection<MetaModelElement> metaModels, final Set<EntityElement> referencedEntities) {
        final List<MetaModelElement> regenerated = new ArrayList<>();

        for (MetaModelElement mme: metaModels) {
            final EntityElement entity = EntityFinder.findEntityForMetaModel(mme, elementUtils);
            final Set<EntityElement> referencedByThisEntity = EntityFinder.findProperties(entity).stream()
                    .filter(EntityFinder::isPropertyEntityType)
                    .map(propEl -> new EntityElement(propEl.getTypeAsTypeElementOrThrow(), elementUtils))
                    // keep those that are contained in referencedEntities
                    .filter(entityEl -> referencedEntities.contains(entityEl))
                    .collect(Collectors.toSet());

            if (!referencedByThisEntity.isEmpty())  {
                procLogger.debug(format("%s references %s. Regenerating %s.", entity.getSimpleName(), Arrays.toString(referencedByThisEntity.stream().map(EntityElement::getSimpleName).toArray()), mme.getSimpleName()));
                final List<TypeMirror> referencedTypes = referencedByThisEntity.stream()
                        .map(EntityElement::asType)
                        .toList();
                // provide a custom test for property type being metamodeled to take into account those entities that had their meta-model generated in this round
                if (writeMetaModel(mme, prop -> 
                                    isPropertyTypeMetamodeled(prop) ||
                                    ElementFinder.isFieldOfType(prop.getVariableElement(), referencedTypes, typeUtils)))
                    regenerated.add(mme);
            }
        }
        
        return regenerated;
    }

    private void regenerateInactiveMetaModels(final Collection<MetaModelElement> inactiveMetaModels) {
        for (MetaModelElement mme: inactiveMetaModels) {
            if (writeEmptyMetaModel(mme))
                markAsRegenerated(mme);
        }
    }

    private boolean writeEmptyMetaModel(MetaModelElement mme) {
        TypeSpec.Builder emptyMetaModelBuilder = TypeSpec.classBuilder(mme.getSimpleName())
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);

        // if this typeElement extends another class that is not Object, then preserve the hierarchy
        final TypeElement superclass = ElementFinder.getSuperclassOrNull(mme.getTypeElement());
        if (!ElementFinder.equals(superclass, Object.class))
            emptyMetaModelBuilder = emptyMetaModelBuilder.superclass(superclass.asType());

        // @Generated annotation
        final AnnotationSpec generatedAnnotation = AnnotationSpec.builder(ClassName.get(Generated.class))
                .addMember("value", "$S", this.getClass().getCanonicalName())
                .addMember("date", "$S", initDateTime.toString())
                .build();
        final String datetime = initDateTime.toString("dd-MM-YYYY HH:mm:ss.SSS z");
        emptyMetaModelBuilder = emptyMetaModelBuilder
                .addJavadoc("INACTIVE auto-generated meta-model.\n<p>\n")
                .addJavadoc(format("Generation datetime: %s\n<p>\n", datetime))
                .addJavadoc(format("Generated by {@link %s}\n<p>\n", this.getClass().getCanonicalName()))
                .addAnnotation(generatedAnnotation);

        final TypeSpec emptyMetaModel = emptyMetaModelBuilder.build();

        // ######################## WRITE TO FILE #####################
        final JavaFile javaFile = JavaFile.builder(mme.getPackageName(), emptyMetaModel).indent(INDENT).build();
        try {
            javaFile.writeTo(filer);
        } catch (IOException e) {
            procLogger.error(e.toString());
            return false;
        }

        procLogger.debug(format("Generated empty meta-model %s.", mme.getSimpleName()));
        return true;
    }

    private List<MetaModelElement> findInactiveMetaModels(MetaModelsElement metaModelsElement) {
        procLogger.debug(format("Verifying %s.", metaModelsElement.getSimpleName()));

        final List<MetaModelElement> inactive = new ArrayList<>();

        for (MetaModelElement mme: metaModelsElement.getMetaModels()) {
            final EntityElement entity = EntityFinder.findEntityForMetaModel(mme, elementUtils);

            // debug
            if (entity == null)
                procLogger.debug(format("Entity for %s does not exist", mme.getSimpleName()));

            if (entity == null || !MetaModelConstants.isMetamodeled(entity.getTypeElement())) {
                // debug
                if (entity != null)
                    procLogger.debug(format("Entity %s should no longer be metamodeled", entity.getSimpleName()));

                inactive.add(mme);
            }
        }

        return inactive;
    }

    private boolean writeMetaModel(final MetaModelConcept mmc) {
        return writeMetaModel(mmc, MetaModelProcessor::isPropertyTypeMetamodeled);
    }

    private boolean writeMetaModel(final MetaModelConcept mmc, final Predicate<PropertyElement> propertyTypeMetamodeledTest) {
        // ######################## PROPERTIES ########################
        Set<PropertyElement> properties = new LinkedHashSet<>();

        final EntityElement entityElement = mmc.getEntityElement();
        final EntityElement entityParent = EntityFinder.getParent(entityElement, elementUtils);
        final boolean isEntitySuperclassMetamodeled = MetaModelConstants.isMetamodeled(entityParent.getTypeElement());

        if (isEntitySuperclassMetamodeled) {
            // find only declared properties
            properties.addAll(EntityFinder.findDeclaredProperties(entityElement));
        } else {
            // find all properties (declared + inherited from <? extends AbstractEntity))
            properties.addAll(EntityFinder.findDistinctProperties(entityElement, PropertyElement::getName));
        }

//        procLogger.debug("Properties: " + Arrays.toString(properties.stream().map(PropertyElement::getName).toArray()));

        SortedSet<FieldSpec> fieldSpecs = new TreeSet<>((f1, f2) -> f1.name.compareTo(f2.name));
        for (PropertyElement prop: properties) {
            FieldSpec.Builder fieldSpecBuilder = null;
            final String propName = prop.getName();

            // ### static property holding the property's name ###
            // private static final String [PROP_NAME]_ = "[PROP_NAME]";
            fieldSpecs.add(FieldSpec.builder(String.class, propName + "_")
                    .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                    .initializer("$S", propName)
                    .build());

            // ### instance property ###
            if (propertyTypeMetamodeledTest.test(prop)) {
                final MetaModelConcept propTypeMmc = new MetaModelConcept(newEntityElement(prop.getTypeAsTypeElementOrThrow()));
                final ClassName propTypeMmcClassName = getMetaModelClassName(propTypeMmc);
                // property type is target for meta-model generation
                // private Supplier<[METAMODEL_NAME]> [PROP_NAME];
                final ParameterizedTypeName propTypeName = ParameterizedTypeName.get(ClassName.get(Supplier.class), propTypeMmcClassName);
                fieldSpecBuilder = FieldSpec.builder(propTypeName, propName)
                        .addModifiers(Modifier.PRIVATE);
            } else {
                // private final PropertyMetaModel [PROP_NAME]; 
                fieldSpecBuilder = FieldSpec.builder(ClassName.get(PropertyMetaModel.class), propName)
                        .addModifiers(Modifier.PRIVATE, Modifier.FINAL);
            }

            fieldSpecs.add(fieldSpecBuilder.build());
        }

        // ######################## METHODS ###########################
        List<MethodSpec> methodSpecs = new ArrayList<>();

        for (PropertyElement prop: properties) {
            MethodSpec.Builder methodSpecBuilder = null;
            final String propName = prop.getName();

            ClassName propTypeMmcClassName = null;
            if (propertyTypeMetamodeledTest.test(prop)) {
                final MetaModelConcept propTypeMmc = new MetaModelConcept(newEntityElement(prop.getTypeAsTypeElementOrThrow()));
                propTypeMmcClassName = getMetaModelClassName(propTypeMmc);
                /* property type is target for meta-model generation

                public [METAMODEL_NAME] [PROP_NAME]() {
                    return [PROP_NAME].get();
                }
                 */
                methodSpecBuilder = MethodSpec.methodBuilder(propName)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(propTypeMmcClassName)
                        .addStatement("return $L.get()", propName);
            } else {
                /*
                public PropertyMetaModel [PROP_NAME]() {
                    return [PROP_NAME];
                }
                 */
                methodSpecBuilder = MethodSpec.methodBuilder(propName)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(ClassName.get(PropertyMetaModel.class))
                        .addStatement("return $L", propName);
            }

            // javadoc: property title and description
            final Pair<String, String> propTitleAndDesc = EntityFinder.getPropTitleAndDesc(prop);
            if (propTitleAndDesc != null) {
                final String propTitle = propTitleAndDesc.getKey();
                if (propTitle.length() > 0) {
                    methodSpecBuilder = methodSpecBuilder.addJavadoc("Title: $L\n<p>\n", propTitle);
                }

                final String propDesc = propTitleAndDesc.getValue();
                if (propDesc.length() > 0) {
                    methodSpecBuilder = methodSpecBuilder.addJavadoc("Description: $L\n<p>\n", propDesc);
                }
            }

            // javadoc: property type
            methodSpecBuilder = methodSpecBuilder.addJavadoc("Type: {@link $T}\n<p>\n", prop.getType());

            // (optional) javadoc: property type's meta-model
            if (propTypeMmcClassName != null) {
                methodSpecBuilder = methodSpecBuilder.addJavadoc("Meta-model: {@link $T}\n<p>\n", propTypeMmcClassName);
            }

            // javadoc: property annotations
            final List<String> annotationsStrings = ElementFinder.getFieldAnnotations(prop.getVariableElement()).stream()
                    .map(annotMirror -> {
                        String str = format("{@literal @}{@link %s}", ElementFinder.getAnnotationMirrorSimpleName(annotMirror));
                        Map<? extends ExecutableElement, ? extends AnnotationValue> valuesMap = annotMirror.getElementValues();
                        if (!valuesMap.isEmpty()) {
                            str += "(";
                            str += String.join(", ", valuesMap.entrySet().stream()
                                    .map(e -> format("%s = %s", 
                                            e.getKey().getSimpleName(), 
                                            e.getValue().toString().replaceAll("@", "{@literal @}")))
                                    .toList());
                            str += ")";
                        }
                        return str;
                    })
                    .toList();
            methodSpecBuilder = methodSpecBuilder.addJavadoc("$L", String.join("<br>\n", annotationsStrings));

            methodSpecs.add(methodSpecBuilder.build());
        }


        /*
        @Override
        public static Class<[ENTITY]> getEntityClass() {
            return [ENTITY].class;
        }
         */
        final ClassName entityClassName = getEntityClassName(entityElement);
        final ClassName abstractEntityClassName = ClassName.get(AbstractEntity.class);
        final ParameterizedTypeName returnType = ParameterizedTypeName.get(
                ClassName.get(Class.class), WildcardTypeName.subtypeOf(abstractEntityClassName)); 

        MethodSpec getModelMethod = MethodSpec.methodBuilder("getEntityClass")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(returnType)
                .addStatement("return $T.class", entityClassName)
                .build();

        methodSpecs.add(getModelMethod);

        // ######################## CONSTRUCTORS ######################
        final List<MethodSpec> constructors = new ArrayList<>();

        /*
        public [ENTITY]MetaModel(String path) {
            super(path);
            ...
        }
         */
        MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String.class, "path")
                .addStatement("super(path)");

        CodeBlock.Builder constructorStatementsBuilder = CodeBlock.builder();
        for (PropertyElement prop: properties) {
            final String propName = prop.getName();

            if (propertyTypeMetamodeledTest.test(prop)) {
                MetaModelConcept propTypeMmc = new MetaModelConcept(newEntityElement(prop.getTypeAsTypeElementOrThrow()));
                ClassName propTypeMetaModelClassName = getMetaModelClassName(propTypeMmc);

                /* property type is target for meta-model generation

                this.[PROP_NAME] = () -> {
                    [METAMODEL_NAME] value = new [METAMODEL_NAME](joinPath([PROP_NAME]_));
                    [PROP_NAME] = () -> value;
                    return value;
                };
                 */
                CodeBlock lambda = CodeBlock.builder()
                        .add("() -> {\n").indent()
                        .addStatement(
                                "$T $L = new $T(joinPath($L_))", 
                                propTypeMetaModelClassName, "value", propTypeMetaModelClassName, propName)
                        .addStatement(
                                "$L = () -> $L",
                                propName, "value")
                        .addStatement("return $L", "value")
                        .unindent().add("}")
                        .build();
                CodeBlock code = CodeBlock.builder()
                        .addStatement("this.$L = $L", propName, lambda.toString())
                        .build();
                constructorStatementsBuilder = constructorStatementsBuilder.add(code);
            } else {
                // this.[PROP_NAME] = new PropertyMetaModel(joinPath([PROP_NAME]_));
                constructorStatementsBuilder = constructorStatementsBuilder.addStatement(
                        "this.$L = new $T(joinPath($L_))", 
                        propName, ClassName.get(PropertyMetaModel.class), propName);
            }
        }

        final MethodSpec constructor = constructorBuilder.addCode(constructorStatementsBuilder.build()).build();
        constructors.add(constructor);

        // the empty constructor
        /*
        public [ENTITY]MetaModel() {
            this("");
        }
         */
        final MethodSpec emptyConstructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addStatement("this(\"\")")
                .build();
        constructors.add(emptyConstructor);

        // class declaration
        /*
        public class [ENTITY]MetaModel extends [EntityMetaModel | [PARENT]MetaModel] {
            ...
        }
         */
        ClassName metaModelSuperclassClassName;
        if (isEntitySuperclassMetamodeled) {
            final MetaModelConcept parentMmc = new MetaModelConcept(entityParent);
            metaModelSuperclassClassName = ClassName.get(parentMmc.getPackageName(), parentMmc.getSimpleName());
        } else {
            metaModelSuperclassClassName = ClassName.get(MetaModelConstants.METAMODEL_SUPERCLASS);
        }

        final String metaModelName = mmc.getSimpleName();
        final String metaModelPkgName = mmc.getPackageName();

        // sort methods alphabetically
        methodSpecs.sort((ms1, ms2) -> ms1.name.compareTo(ms2.name));

        TypeSpec metaModel = TypeSpec.classBuilder(metaModelName)
//                .addOriginatingElement(entityElement.getTypeElement())
                .addModifiers(Modifier.PUBLIC)
                .superclass(metaModelSuperclassClassName)
                .addFields(fieldSpecs)
                .addMethods(constructors)
                .addMethods(methodSpecs)
                .build();
        
        // javadoc
        final Pair<String, String> entityTitleAndDesc = EntityFinder.getEntityTitleAndDesc(entityElement);
        if (entityTitleAndDesc != null) {
            final String title = entityTitleAndDesc.getKey();
            if (title.length() > 0)
                metaModel = metaModel.toBuilder().addJavadoc(format("Title: %s\n<p>\n", title)).build();

            final String desc = entityTitleAndDesc.getValue();
            if (desc.length() > 0)
                metaModel = metaModel.toBuilder().addJavadoc(format("Description: %s\n<p>\n", desc)).build();
        } else {
            final String title = getEntityTitleFromClassName(entityElement);
            metaModel = metaModel.toBuilder().addJavadoc(format("Title: %s\n<p>\n", title)).build();
        }

        // @Generated annotation
        final AnnotationSpec generatedAnnotation = AnnotationSpec.builder(ClassName.get(Generated.class))
                .addMember("value", "$S", this.getClass().getCanonicalName())
                .addMember("date", "$S", initDateTime.toString())
                .build();

        final String datetime = initDateTime.toString("dd-MM-YYYY HH:mm:ss.SSS z");
        metaModel = metaModel.toBuilder()
                .addJavadoc("Auto-generated meta-model for {@link $T}.\n<p>\n", entityClassName)
                .addJavadoc(format("Generation datetime: %s\n<p>\n", datetime))
                .addJavadoc(format("Generated by {@link %s}\n<p>\n", this.getClass().getCanonicalName()))
//                .addJavadoc("Originating elements: {@link $L}", entityElement.getTypeElement().getQualifiedName())
                .addAnnotation(generatedAnnotation)
                .build();


        // ######################## WRITE TO FILE #####################
        final JavaFile javaFile = JavaFile.builder(metaModelPkgName, metaModel).indent(INDENT).build();
        try {
            javaFile.writeTo(filer);
        } catch (IOException e) {
            procLogger.error(e.toString());
            return false;
        }

        procLogger.info(format("Generated %s for entity %s.", metaModel.name, entityElement.getSimpleName()));
        return true;
    }

    private boolean writeMetaModel(final MetaModelElement metaModelElement) {
        final EntityElement entityElement = EntityFinder.findEntityForMetaModel(metaModelElement, elementUtils);
        final MetaModelConcept metaModelConcept = new MetaModelConcept(entityElement);
        return writeMetaModel(metaModelConcept);
    }

    private boolean writeMetaModel(final MetaModelElement metaModelElement, final Predicate<PropertyElement> propertyTypeMetamodeledTest) {
        final EntityElement entityElement = EntityFinder.findEntityForMetaModel(metaModelElement, elementUtils);
        final MetaModelConcept metaModelConcept = new MetaModelConcept(entityElement);
        return writeMetaModel(metaModelConcept, propertyTypeMetamodeledTest);
    }

    private boolean writeMetaModelsClass(Collection<MetaModelConcept> metaModelConcepts) {
        return writeMetaModelsClass(metaModelConcepts, null, null);
    }

    /**
     * Generates the meta-models collection class that has a field for each meta-model in {@code metaModelConcepts}, as well as the existing fields that are provided by {@code metaModelsElement} apart from those that are inactive ({@code inactiveMetaModelElements}).
     * @param metaModelConcepts
     * @param metaModelsElement
     * @param inactiveMetaModelElements
     */
    private boolean writeMetaModelsClass(final Collection<MetaModelConcept> metaModelConcepts, final MetaModelsElement metaModelsElement, final Collection<MetaModelElement> inactiveMetaModelElements) {
        /*
        public final class MetaModels {
            public static final [ENTITY]MetaModel [ENTITY] = new [ENTITY]MetaModel();
        }
         */
        final SortedSet<FieldSpec> fieldSpecs = new TreeSet<>((f1, f2) -> f1.name.compareTo(f2.name));

        // generate fields for new meta-models
        for (final MetaModelConcept mmc: metaModelConcepts) {
            final String fieldName = nameFieldForMetaModel(mmc.getEntityElement().getSimpleName());
            fieldSpecs.add(specFieldForMetaModel(getMetaModelClassName(mmc), fieldName));
        }

        // if MetaModels class exists, then collect the meta-models and filter inactive ones
        if (metaModelsElement != null) {
            final Set<MetaModelElement> activeMetaModelElements = new HashSet<>();
            activeMetaModelElements.addAll(metaModelsElement.getMetaModels().stream()
                    .filter(mme -> !inactiveMetaModelElements.contains(mme))
                    .toList());

            for (final MetaModelElement mme: activeMetaModelElements) {
                final EntityElement entity = EntityFinder.findEntityForMetaModel(mme, elementUtils);
                // generate a field for this meta-model
                final String fieldName = nameFieldForMetaModel(entity.getSimpleName());
                fieldSpecs.add(specFieldForMetaModel(getMetaModelClassName(mme), fieldName));
            }
        }

        // @Generated annotation
        final AnnotationSpec generatedAnnotation = AnnotationSpec.builder(ClassName.get(Generated.class))
                .addMember("value", "$S", this.getClass().getCanonicalName())
                .addMember("date", "$S", initDateTime.toString())
                .build();
        
        final String dateTimeString = initDateTime.toString("dd-MM-YYYY HH:mm:ss.SSS z");
        final TypeSpec metaModelsTypeSpec = TypeSpec.classBuilder(MetaModelConstants.METAMODELS_CLASS_SIMPLE_NAME)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addJavadoc(format("Generation datetime: %s\n<p>\n", dateTimeString))
                .addJavadoc(format("Generated by {@link %s}.", this.getClass().getCanonicalName()))
                .addAnnotation(generatedAnnotation)
                .addFields(fieldSpecs)
                .build();

        // ######################## WRITE TO FILE #####################
        final JavaFile javaFile = JavaFile.builder(MetaModelConstants.METAMODELS_CLASS_PKG_NAME, metaModelsTypeSpec).indent(INDENT).build();
        try {
            javaFile.writeTo(filer);
        } catch (IOException e) {
            procLogger.error(e);
            return false;
        }

        procLogger.info(format("Generated %s.", metaModelsTypeSpec.name));
        return true;
    }

    /**
     * Creates a {@link FieldSpec} for field with name {@code fieldName} of type {@code metaModelClassName} in {@code MetaModels} for representing a reference to a domain meta-model. 
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
     * A helper method for naming fields in {@code MetaModels} that represent access points to domain meta-models.
     *
     * @param simpleName
     * @return
     */
    private static String nameFieldForMetaModel(final String simpleName) {
        return simpleName + "_";
    }

    private EntityElement newEntityElement(TypeElement typeElement) {
        return new EntityElement(typeElement, elementUtils);
    }

    private Configuration getLog4jConfig() {
        ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder();

        AppenderComponentBuilder console = builder.newAppender("ConsoleAppender", "Console"); 

        String projectDir = options.get("projectdir");
        String filename = "processor.log";
        filename = projectDir == null ? filename : projectDir + '/' + filename;
        AppenderComponentBuilder file = builder.newAppender("FileAppender", "File"); 
        file.addAttribute("fileName", filename);
        file.addAttribute("append", "true");

        LayoutComponentBuilder layout = builder.newLayout("PatternLayout");
        layout.addAttribute("pattern", "%highlight{%d{yyyy-MM-dd HH:mm:ss.SSS} [%-5level] %c{1} --- %msg%n}{ERROR=red}");
        console.add(layout);
        file.add(layout);

        builder.add(console);
        builder.add(file);

        RootLoggerComponentBuilder rootLogger = builder.newRootLogger(Level.DEBUG);
        rootLogger.add(builder.newAppenderRef("ConsoleAppender"));
        rootLogger.add(builder.newAppenderRef("FileAppender"));
        builder.add(rootLogger);

        return builder.build();
    }
}
