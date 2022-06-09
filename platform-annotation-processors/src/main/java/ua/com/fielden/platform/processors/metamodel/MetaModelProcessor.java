package ua.com.fielden.platform.processors.metamodel;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;
import static ua.com.fielden.platform.processors.metamodel.utils.EntityFinder.isDomainEntity;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
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
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic.Kind;

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
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.processors.metamodel.concepts.MetaModelConcept;
import ua.com.fielden.platform.processors.metamodel.elements.EntityElement;
import ua.com.fielden.platform.processors.metamodel.elements.MetaModelElement;
import ua.com.fielden.platform.processors.metamodel.elements.MetaModelsElement;
import ua.com.fielden.platform.processors.metamodel.elements.PropertyElement;
import ua.com.fielden.platform.processors.metamodel.models.PropertyMetaModel;
import ua.com.fielden.platform.processors.metamodel.utils.ElementFinder;
import ua.com.fielden.platform.processors.metamodel.utils.EntityFinder;
import ua.com.fielden.platform.processors.metamodel.utils.MetaModelFinder;
import ua.com.fielden.platform.utils.Pair;

@AutoService(Processor.class)
@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion. RELEASE_16)
public class MetaModelProcessor extends AbstractProcessor {

    private static final String INDENT = "    ";
    private static final Set<Class<? extends Annotation>> DOMAIN_TYPE_ANNOTATIONS = Set.of(MapEntityTo.class, DomainEntity.class);

    private Filer filer;
    private Elements elementUtils;
    private Types typeUtils;
    private Messager messager;
    private Map<String, String> options;

    private DateTime initDateTime;
    private int roundCount;
    private boolean metaModelsClassVerified;
    private boolean processingOver;

    private static ClassName getMetaModelClassName(final MetaModelElement element) {
        return ClassName.get(element.getPackageName(), element.getSimpleName());
    }

    private static ClassName getMetaModelClassName(MetaModelConcept mmc) {
        return ClassName.get(mmc.getPackageName(), mmc.getSimpleName());
    }
    
    private static ClassName getEntityClassName(EntityElement element) {
        return ClassName.get(element.getPackageName(), element.getSimpleName());
    }

    private static String getEntityTitleFromClassName(EntityElement element) {
        final String entityName = element.getSimpleName();
        StringBuilder descriptiveName = new StringBuilder();

        for (int i = 0; i < entityName.length(); i++) {
            char c = entityName.charAt(i);
            if (i > 0 && Character.isUpperCase(c)) {
                descriptiveName.append(' ');
            }
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
        messager.printMessage(Kind.NOTE, format("Options: %s", options.keySet().stream().map(k -> format("%s=%s", k, options.get(k))).sorted().collect(joining(", "))));
        this.roundCount = 0;
        this.metaModelsClassVerified = false;

        messager.printMessage(Kind.NOTE, format("%s initialized.", this.getClass().getSimpleName()));
    }

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        final int roundNumber = ++this.roundCount;
        messager.printMessage(Kind.NOTE, format("=== PROCESSING ROUND %d START ===", roundNumber));
        messager.printMessage(Kind.NOTE, format("annotations: %s%n", annotations.stream().map(Element::getSimpleName).map(Name::toString).sorted().collect(joining(", "))));
        final Set<? extends Element> rootElements = roundEnv.getRootElements();
        messager.printMessage(Kind.NOTE, format("rootElements: %s%n", rootElements.stream().map(Element::getSimpleName).map(Name::toString).sorted().collect(joining(", "))));
        
        // manually control the end of processing to skip redundant rounds
        if (processingOver) {
            endRound(roundNumber, roundEnv.processingOver());
            return false;
        }

        // TODO detect when rootElements are exclusively test sources and exit

        final Map<MetaModelConcept, Boolean> metaModelConcepts = collectEntitiesForMetaModelGeneration(roundEnv);
        final Map<MetaModelElement, Boolean> inactiveMetaModels = new HashMap<>();

        for (MetaModelConcept mmc: getGenerationTargets(metaModelConcepts)) {
            if (writeMetaModel(mmc)) {
                metaModelConcepts.put(mmc, true);
            }
        }

        final TypeElement metaModelsTypeElement = elementUtils.getTypeElement(MetaModelConstants.METAMODELS_CLASS_QUAL_NAME);
        // if MetaModels class exists
        if (metaModelsTypeElement != null) { 
        messager.printMessage(Kind.NOTE, format("%s found.", MetaModelConstants.METAMODELS_CLASS_QUAL_NAME));
            final MetaModelsElement metaModelsElement = new MetaModelsElement(metaModelsTypeElement, elementUtils);

            // verify MetaModels
            if (!this.metaModelsClassVerified) {
                final List<MetaModelElement> inactive = findInactiveMetaModels(metaModelsElement);
                this.metaModelsClassVerified = true;

                messager.printMessage(Kind.NOTE, format("Inactive meta-models: %s", inactive.stream().map(MetaModelElement::getSimpleName).collect(joining(", "))));

                for (MetaModelElement imm: inactive) {
                    inactiveMetaModels.putIfAbsent(imm, false);
                }

                if (!inactiveMetaModels.isEmpty()) {
                    final List<MetaModelElement> regenerationTargets = getGenerationTargets(inactiveMetaModels);
                    // process inactive meta-models
                    for (final MetaModelElement mme: regenerationTargets) {
                        if (writeEmptyMetaModel(mme)) {
                            inactiveMetaModels.put(mme, true);
                        }
                    }

                    // regenerate meta-models that reference the inactive ones
                    final List<MetaModelElement> activeMetaModels = metaModelsElement.getMetaModels().stream()
                            .filter(mme -> !regenerationTargets.contains(mme))
                            .toList();
                    regenerateMetaModelsWithReferenceTo(activeMetaModels, regenerationTargets);
                }
            }

            //  TODO delete inactive meta-models java sources
            // final boolean deleted = deleteJavaSources(inactiveMetaModels);

            if (!metaModelConcepts.isEmpty() || !inactiveMetaModels.isEmpty()) {
                //  regenerate the MetaModels class by adding new fields and removing inactive ones
                writeMetaModelsClass(metaModelConcepts.keySet(), metaModelsElement, inactiveMetaModels.keySet());
            }
        } else {
            if (!metaModelConcepts.isEmpty()) {
                // generate the MetaModels class
                writeMetaModelsClass(metaModelConcepts.keySet());
            }
        }
        
        // manually "end" the processing after everything was regenerated
        processingOver = true;
        messager.printMessage(Kind.NOTE, "Processing is effectively over. Skipping subsequent rounds.");

        endRound(roundNumber, roundEnv.processingOver());
        // must return false to avoid claiming all annotations (as defined by @SupportedAnnotationTypes("*")) to allow other processors to run
        return false;
    }

    /**
     * Processes {@code roundEnv} to collect entity classes for processing.
     * Returns a map with instances of {@link MetaModelConcept}, representing each entity that require a meta-model, and a corresponding boolean value, indicating if a meta-model was actually generated ({@code false} initially).   
     *
     * @param roundEnv
     * @return
     */
    private Map<MetaModelConcept, Boolean> collectEntitiesForMetaModelGeneration(final RoundEnvironment roundEnv) {
        final Map<MetaModelConcept, Boolean> metaModelConcepts = new HashMap<>();
        // find classes annotated with any of DOMAIN_TYPE_ANNOTATIONS
        final Set<TypeElement> annotatedElements = roundEnv.getElementsAnnotatedWithAny(DOMAIN_TYPE_ANNOTATIONS).stream()
                                                           .filter(element -> element.getKind() == ElementKind.CLASS) // just in case make sure identified elements are classes
                                                           .map(el -> (TypeElement) el).collect(toSet());
        messager.printMessage(Kind.NOTE, format("annotatedElements: %s%n", annotatedElements.stream().map(Element::getSimpleName).map(Name::toString).sorted().collect(joining(", "))));

        // generate meta-models for these elements
        for (final TypeElement typeElement: annotatedElements) {
            final EntityElement entityElement = newEntityElement(typeElement);
            final MetaModelConcept mmc = new MetaModelConcept(entityElement);
            metaModelConcepts.putIfAbsent(mmc, false);

            // traverse all properties for the current entity element to ensure that any entity-typed properties get their type included as a meta-model
            // this is mainly important to pick up entity types that come from other project dependencies, such as the TG platform itself
            EntityFinder.findProperties(entityElement).stream()
                        .filter(EntityFinder::isPropertyOfDomainEntityType)
                        .map(pel -> new EntityElement(pel.getTypeAsTypeElementOrThrow(), elementUtils))
                        .forEach(eel -> metaModelConcepts.putIfAbsent(new MetaModelConcept(eel), false));
        }

        messager.printMessage(Kind.NOTE, format("metaModelConcepts: %s%n", metaModelConcepts.keySet().stream().map(MetaModelConcept::getSimpleName).sorted().collect(joining(", "))));
        return metaModelConcepts;
    }

    private <T> List<T> getGenerationTargets(Map<T, Boolean> metaModels) {
        return metaModels.entrySet().stream()
                .filter(e -> e.getValue().equals(Boolean.FALSE))
                .map(Entry::getKey)
                .toList();
    }
    

    private void endRound(final int n, final boolean processingOver) {
        messager.printMessage(Kind.NOTE, format("xxx PROCESSING ROUND %d END xxx", n));
        if (processingOver) {
            messager.printMessage(Kind.NOTE, "### LAST ROUND. PROCESSING OVER ###");
        }
    }

    /**
     * Regenerates the meta-models that reference any of the {@code referencedMetaModels}.
     *
     * @param metaModels - a collection of meta-models to be regenerated if a reference is found
     * @param referencedMetaModels - the set of referenced meta-models
     * @return
     */
    private List<MetaModelElement> regenerateMetaModelsWithReferenceTo(final Collection<MetaModelElement> metaModels, final List<MetaModelElement> referencedMetaModels) {
        final List<MetaModelElement> regenerated = new ArrayList<>();

        for (final MetaModelElement mme: metaModels) {
            final Set<MetaModelElement> referencedByThisMetaModel = MetaModelFinder.findReferencedMetaModels(mme, elementUtils);
            // if the set of referenced meta-models intersects with the set of trigger meta-models - regenerate this meta-model
            if (!Collections.disjoint(referencedByThisMetaModel, referencedMetaModels)) {
                final Set<MetaModelElement> intersection = new HashSet<>(referencedByThisMetaModel);
                intersection.retainAll(referencedMetaModels);
                messager.printMessage(Kind.NOTE, format("%s references %s. Regenerating.", mme.getSimpleName(), intersection.stream().map(MetaModelElement::getSimpleName).collect(joining(", "))));
                if (writeMetaModel(mme)) {
                    regenerated.add(mme);
                }
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
                    .filter(EntityFinder::isPropertyOfEntityType)
                    .map(propEl -> new EntityElement(propEl.getTypeAsTypeElementOrThrow(), elementUtils))
                    // keep those that are contained in referencedEntities
                    .filter(entityEl -> referencedEntities.contains(entityEl))
                    .collect(Collectors.toSet());

            if (!referencedByThisEntity.isEmpty())  {
                messager.printMessage(Kind.NOTE, format("%s references %s. Regenerating %s.", entity.getSimpleName(), referencedByThisEntity.stream().map(EntityElement::getSimpleName).sorted().collect(joining(", ")), mme.getSimpleName()));
                final List<TypeMirror> referencedTypes = referencedByThisEntity.stream()
                        .map(EntityElement::asType)
                        .toList();
                // provide a custom test for property type being metamodeled to take into account those entities that had their meta-model generated in this round
                if (writeMetaModel(mme, prop -> 
                                    EntityFinder.isPropertyOfDomainEntityType(prop) ||
                                    ElementFinder.isFieldOfType(prop.getVariableElement(), referencedTypes, typeUtils))) {
                    regenerated.add(mme);
                }
            }
        }
        
        return regenerated;
    }

    private boolean writeEmptyMetaModel(MetaModelElement mme) {
        TypeSpec.Builder emptyMetaModelBuilder = TypeSpec.classBuilder(mme.getSimpleName())
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);

        // if this typeElement extends another class that is not Object, then preserve the hierarchy
        final TypeElement superclass = ElementFinder.getSuperclassOrNull(mme.getTypeElement());
        if (!ElementFinder.equals(superclass, Object.class)) {
            emptyMetaModelBuilder = emptyMetaModelBuilder.superclass(superclass.asType());
        }

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
            messager.printMessage(Kind.ERROR, e.toString());
            return false;
        }

        messager.printMessage(Kind.NOTE, format("Generated empty meta-model %s.", mme.getSimpleName()));
        return true;
    }

    private List<MetaModelElement> findInactiveMetaModels(MetaModelsElement metaModelsElement) {
        messager.printMessage(Kind.NOTE, format("Verifying %s.", metaModelsElement.getSimpleName()));
        final List<MetaModelElement> inactive = new ArrayList<>();

        for (MetaModelElement mme: metaModelsElement.getMetaModels()) {
            final EntityElement entity = EntityFinder.findEntityForMetaModel(mme, elementUtils);
            // debug
            if (entity == null) {
                messager.printMessage(Kind.NOTE, format("Entity for %s does not exist", mme.getSimpleName()));
            }

            if (entity == null || !isDomainEntity(entity.getTypeElement())) {
                // debug
                if (entity != null) {
                    messager.printMessage(Kind.NOTE, format("Entity %s should no longer be metamodeled", entity.getSimpleName()));
                }
                inactive.add(mme);
            }
        }

        return inactive;
    }

    private boolean writeMetaModel(final MetaModelConcept mmc) {
        return writeMetaModel(mmc, EntityFinder::isPropertyOfDomainEntityType);
    }

    private boolean writeMetaModel(final MetaModelConcept mmc, final Predicate<PropertyElement> propertyTypeMetamodeledTest) {
        // ######################## PROPERTIES ########################
        final Set<PropertyElement> properties = new LinkedHashSet<>();

        final EntityElement entityElement = mmc.getEntityElement();
        final EntityElement entityParent = EntityFinder.getParent(entityElement, elementUtils);
        final boolean isEntitySuperclassMetamodeled = isDomainEntity(entityParent.getTypeElement());

        if (isEntitySuperclassMetamodeled) {
            // find only declared properties
            properties.addAll(EntityFinder.findDeclaredProperties(entityElement));
        } else {
            // find all properties (declared + inherited from <? extends AbstractEntity))
            properties.addAll(EntityFinder.findProperties(entityElement));
        }


        SortedSet<FieldSpec> fieldSpecs = new TreeSet<>((f1, f2) -> f1.name.compareTo(f2.name));
        for (PropertyElement prop: properties) {
            FieldSpec.Builder fieldSpecBuilder = null;
            final String propName = prop.getName();

            // ### static property holding the property's name ###
            // private static final String ${PROPERTY}_ = "${PROPERTY}";
            fieldSpecs.add(FieldSpec.builder(String.class, propName + "_")
                    .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                    .initializer("$S", propName)
                    .build());

            // ### instance property ###
            if (propertyTypeMetamodeledTest.test(prop)) {
                final MetaModelConcept propTypeMmc = new MetaModelConcept(newEntityElement(prop.getTypeAsTypeElementOrThrow()));
                final ClassName propTypeMmcClassName = getMetaModelClassName(propTypeMmc);
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
        final List<MethodSpec> methodSpecs = new ArrayList<>();
        for (final PropertyElement prop: properties) {
            final String propName = prop.getName();
            final MethodSpec.Builder methodSpecBuilder = MethodSpec.methodBuilder(propName);

            final ClassName propTypeMmcClassName;
            if (propertyTypeMetamodeledTest.test(prop)) {
                final MetaModelConcept propTypeMmc = new MetaModelConcept(newEntityElement(prop.getTypeAsTypeElementOrThrow()));
                propTypeMmcClassName = getMetaModelClassName(propTypeMmc);
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

        /*
        @Override
        public static Class<${ENTITY}> getEntityClass() {
            return ${ENTITY}.class;
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
        public ${METAMODEL}(String path) {
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

                this.${PROPERTY} = () -> {
                    ${METAMODEL} value = new ${METAMODEL} ( joinPath( ${PROPERTY}_ ) );
                    ${PROPERTY} = () -> value;
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
                // this.${PROPERTY} = new PropertyMetaModel ( joinPath( ${PROPERTY}_ ) );
                constructorStatementsBuilder = constructorStatementsBuilder.addStatement(
                        "this.$L = new $T(joinPath($L_))", 
                        propName, ClassName.get(PropertyMetaModel.class), propName);
            }
        }

        final MethodSpec constructor = constructorBuilder.addCode(constructorStatementsBuilder.build()).build();
        constructors.add(constructor);

        // the empty constructor
        /*
        public ${METAMODEL} {
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
        public class ${METAMODEL} extends [EntityMetaModel | ${PARENT_METAMODEL}] {
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
            if (!title.isEmpty()) {
                metaModel = metaModel.toBuilder().addJavadoc(format("Title: %s\n<p>\n", title)).build();
            }

            final String desc = entityTitleAndDesc.getValue();
            if (!desc.isEmpty()) {
                metaModel = metaModel.toBuilder().addJavadoc(format("Description: %s\n<p>\n", desc)).build();
            }
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
                .addAnnotation(generatedAnnotation)
                .build();


        // ######################## WRITE TO FILE #####################
        final JavaFile javaFile = JavaFile.builder(metaModelPkgName, metaModel).indent(INDENT).build();
        try {
            javaFile.writeTo(filer);
        } catch (IOException e) {
            messager.printMessage(Kind.ERROR, e.toString());
            return false;
        }

        messager.printMessage(Kind.NOTE, format("Generated %s for entity %s.", metaModel.name, entityElement.getSimpleName()));
        return true;
    }

    /**
     * Add Javadoc to {@code specBuilder}, which describes an entity property.
     *
     * @param prop
     * @param specBuilder
     * @param propTypeMmcClassName
     * @return
     */
    private static void buildJavadoc(final PropertyElement prop, final MethodSpec.Builder specBuilder, final ClassName propTypeMmcClassName) {
        // javadoc: property title and description
        final Pair<String, String> propTitleAndDesc = EntityFinder.getPropTitleAndDesc(prop);
        if (propTitleAndDesc != null) {
            final String propTitle = propTitleAndDesc.getKey();
            if (propTitle.length() > 0) {
                specBuilder.addJavadoc("Title: $L\n<p>\n", propTitle);
            }

            final String propDesc = propTitleAndDesc.getValue();
            if (propDesc.length() > 0) {
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
        specBuilder.addJavadoc("$L", String.join("<br>\n", annotationsStrings));
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
        messager.printMessage(Kind.NOTE, "Started generating the meta-models entry point...");
        /*
        public final class MetaModels {
            public static final ${METAMODEL} ${ENTITY_NAME} = new ${METAMODEL}();
        }
         */
        final SortedSet<FieldSpec> fieldSpecs = new TreeSet<>((f1, f2) -> f1.name.compareTo(f2.name));

        // generate fields for new meta-models
        for (final MetaModelConcept mmc: metaModelConcepts) {
            final String fieldName = nameFieldForMetaModel(mmc.getEntityElement().getSimpleName());
            messager.printMessage(Kind.NOTE, format("New/Updated meta-model, generating field: %s", fieldName));
            fieldSpecs.add(specFieldForMetaModel(getMetaModelClassName(mmc), fieldName));
        }

        // if MetaModels class exists, then collect its fields for the active *unchanged* meta-models 
        if (metaModelsElement != null) {
            final List<MetaModelElement> activeUnchangedMetaModels = metaModelsElement.getMetaModels().stream()
                    // skip inactive
                    .filter(mme -> !inactiveMetaModelElements.contains(mme))
                    // skip updated active
                    .filter(mme -> metaModelConcepts.stream().noneMatch(mmc -> MetaModelFinder.isSameMetaModel(mmc, mme)))
                    .toList();

            messager.printMessage(Kind.NOTE, format("Inactive meta-models: %s", inactiveMetaModelElements.stream()
                    .map(mm -> mm.getSimpleName())
                    .sorted().collect(joining(", "))));
            
            for (final MetaModelElement mme: activeUnchangedMetaModels) {
                final EntityElement entity = EntityFinder.findEntityForMetaModel(mme, elementUtils);
                final String fieldName = nameFieldForMetaModel(entity.getSimpleName());
                messager.printMessage(Kind.NOTE, format("Old meta-model, generating field: %s", fieldName));
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
            messager.printMessage(Kind.ERROR, e.toString());
            return false;
        }

        messager.printMessage(Kind.NOTE, format("Finished generating the meta-models entry point as [%s].", metaModelsTypeSpec.name));
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

    private EntityElement newEntityElement(final TypeElement typeElement) {
        return new EntityElement(typeElement, elementUtils);
    }

}