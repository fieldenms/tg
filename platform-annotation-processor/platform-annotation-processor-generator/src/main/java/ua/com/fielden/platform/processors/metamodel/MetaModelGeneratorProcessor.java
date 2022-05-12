//package ua.com.fielden.platform.processors.metamodel;
//
//import java.io.IOException;
//import java.lang.annotation.Annotation;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.util.ArrayList;
//import java.util.HashSet;
//import java.util.LinkedHashSet;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import java.util.function.Supplier;
//import java.util.stream.Collectors;
//
//import javax.annotation.processing.AbstractProcessor;
//import javax.annotation.processing.Filer;
//import javax.annotation.processing.Messager;
//import javax.annotation.processing.ProcessingEnvironment;
//import javax.annotation.processing.Processor;
//import javax.annotation.processing.RoundEnvironment;
//import javax.annotation.processing.SupportedAnnotationTypes;
//import javax.annotation.processing.SupportedSourceVersion;
//import javax.lang.model.SourceVersion;
//import javax.lang.model.element.AnnotationValue;
//import javax.lang.model.element.Element;
//import javax.lang.model.element.ExecutableElement;
//import javax.lang.model.element.Modifier;
//import javax.lang.model.element.TypeElement;
//import javax.lang.model.element.VariableElement;
//import javax.lang.model.type.DeclaredType;
//import javax.lang.model.util.Elements;
//
//import org.apache.logging.log4j.Level;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//import org.apache.logging.log4j.core.config.Configuration;
//import org.apache.logging.log4j.core.config.Configurator;
//import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
//import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
//import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
//import org.apache.logging.log4j.core.config.builder.api.LayoutComponentBuilder;
//import org.apache.logging.log4j.core.config.builder.api.RootLoggerComponentBuilder;
//import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
//import org.joda.time.DateTime;
//
//import com.google.auto.service.AutoService;
//import com.squareup.javapoet.ClassName;
//import com.squareup.javapoet.CodeBlock;
//import com.squareup.javapoet.FieldSpec;
//import com.squareup.javapoet.JavaFile;
//import com.squareup.javapoet.MethodSpec;
//import com.squareup.javapoet.ParameterizedTypeName;
//import com.squareup.javapoet.TypeSpec;
//import com.squareup.javapoet.WildcardTypeName;
//
//import ua.com.fielden.platform.annotations.metamodel.DomainEntity;
//import ua.com.fielden.platform.entity.AbstractEntity;
//import ua.com.fielden.platform.entity.annotation.DescTitle;
//import ua.com.fielden.platform.entity.annotation.MapEntityTo;
//import ua.com.fielden.platform.processors.metamodel.elements.ElementFinder;
//import ua.com.fielden.platform.processors.metamodel.elements.EntityElement;
//import ua.com.fielden.platform.processors.metamodel.elements.EntityFinder;
//import ua.com.fielden.platform.processors.metamodel.elements.MetaModelElement;
//import ua.com.fielden.platform.processors.metamodel.elements.PropertyElement;
//import ua.com.fielden.platform.processors.metamodel.models.PropertyMetaModel;
//import ua.com.fielden.platform.utils.Pair;
//
//@AutoService(Processor.class)
//@SupportedAnnotationTypes({"ua.com.fielden.platform.entity.annotation.MapEntityTo",
//                            "ua.com.fielden.platform.annotations.metamodel.DomainEntity"})
//@SupportedSourceVersion(SourceVersion.RELEASE_16)
//public class MetaModelGeneratorProcessor extends AbstractProcessor {
//
//    private static final String INDENT = "    ";
//    private static final String LOG_FILENAME = "generator-proc.log";
//    private static final String ECLIPSE_OPTION_KEY = "projectdir";
//
//    private Logger logger;
//    private ProcessorLogger procLogger;
//    private Filer filer;
//    private Elements elementUtils;
//    private Messager messager;
//    private Map<String, String> options;
//    private boolean fromMaven;
//    private int roundCount;
//    private List<TypeElement> inactive;
//    
//    static {
//        System.out.println(String.format("%s class loaded.", MetaModelProcessor.class.getSimpleName()));
//    }
//    
//    public static MetaModelProcessor create() {
//        return new MetaModelProcessor();
//    }
//    
//    public static Set<Class<? extends Annotation>> getSupportedAnnotations() {
//        return Set.of(MapEntityTo.class, DomainEntity.class);
//    }
//
//    private static ClassName getMetaModelClassName(MetaModelElement element) {
//        return ClassName.get(element.getPackageName(), element.getSimpleName());
//    }
//
//    private static ClassName getEntityClassName(EntityElement element) {
//        return ClassName.get(element.getPackageName(), element.getSimpleName());
//    }
//
//    private static boolean isPropertyTypeMetamodeled(PropertyElement element) {
//        TypeElement propType = null;
//        try {
//            propType = element.getTypeAsTypeElement();
//        } catch (Exception e) {
//            // property type is not a declared type
//            return false;
//        }
//
//        return MetaModelConstants.isMetamodeled(propType);
//    }
//    
//    private static String getEntityTitleFromClassName(EntityElement element) {
//        final String entityName = element.getSimpleName();
//        StringBuilder descriptiveName = new StringBuilder();
//
//        for (int i = 0; i < entityName.length(); i++) {
//            char c = entityName.charAt(i);
//            if (i > 0 && Character.isUpperCase(c))
//                descriptiveName.append(' ');
//            descriptiveName.append(c);
//        }
//        
//        return descriptiveName.toString();
//    }
//
//    private static MetaModelElement newMetaModelElement(EntityElement entityElement) {
//        return new MetaModelElement(entityElement, MetaModelConstants.META_MODEL_NAME_SUFFIX, MetaModelConstants.META_MODEL_PKG_NAME_SUFFIX);
//    }
//
//    /**
//     * Maps a field of the generated MetaModels class to a {@link FieldSpec}
//     * @param field
//     * @param elementUtils
//     * @return
//     */
//    private static FieldSpec getFieldSpecFromMetaModelsClassField(final VariableElement field, Elements elementUtils) {
//        String fieldName = field.getSimpleName().toString();
//        TypeElement fieldTypeElement = (TypeElement) ((DeclaredType) field.asType()).asElement();
//        String fieldTypePkgName = elementUtils.getPackageOf(fieldTypeElement).getQualifiedName().toString();
//        ClassName className = ClassName.get(fieldTypePkgName, ElementFinder.getVariableTypeSimpleName(field));
//
//        return FieldSpec.builder(className, fieldName)
//                .initializer("new $T()", className)
//                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
//                .build();
//    }
//
//    @Override
//    public synchronized void init(ProcessingEnvironment processingEnv) {
//        super.init(processingEnv);
//        this.filer = processingEnv.getFiler();
//        this.elementUtils = processingEnv.getElementUtils();
//        this.messager = processingEnv.getMessager();
//        this.options = processingEnv.getOptions();
//        this.roundCount = 0;
//
//        // processor started from Eclipse?
//        final String projectDir = options.get(ECLIPSE_OPTION_KEY);
//        this.fromMaven = projectDir == null;
//
//        // log4j configuration
//        Configurator.initialize(getLog4jConfig());
//        this.logger = LogManager.getLogger(MetaModelProcessor.class);
//
//        // ProcessorLogger
//        String logFilename = this.fromMaven ? LOG_FILENAME : projectDir + "/" + LOG_FILENAME;
//        String source = this.fromMaven ? "mvn" : "Eclipse";
//        this.procLogger = new ProcessorLogger(logFilename, source, logger);
//        procLogger.ln();
//        procLogger.info(String.format("%s initialized.", this.getClass().getSimpleName()));
//
//        // debug 
//        procLogger.debug("Options: [" + String.join(", ", 
//                                                    options.keySet().stream()
//                                                        .map(k -> String.format("%s=%s", k, options.get(k)))
//                                                        .toList()) + "]");
//        if (Files.exists(Path.of(projectDir + "/target/generated-sources"))) {
//            try {
//                procLogger.debug("target/generated-sources: " + String.join(", ", Files.walk(Path.of(projectDir + "/target/generated-sources"))
//                        .filter(Files::isRegularFile)
//                        .map(p -> p.getFileName().toString().split("\\.java")[0])
//                        .toList()));
//            } catch (IOException e) {
//                procLogger.error(e.toString());
//            }
//        }
//    }
//
//    @Override
//    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
//        if (roundCount > 0) {
//            // the log file is closed after the 1st (0) processing round
//            // this should be modified in case more than 1 processing round is needed
//            procLogger.end();
//            return true;
//        }
//
//        procLogger.debug(String.format("=== PROCESSING ROUND %d START ===", roundCount));
//
//        //procLogger.debug("Inactive meta-models: " + String.join(", ", MetaModelVerifierProcessor.INACTIVE_META_MODELS.stream().map(TypeElement::getSimpleName).toList()));
//        // debug
//        procLogger.debug("annotations: " + String.join(", ", annotations.stream().map(Element::getSimpleName).toList()));
//        String rootElements = String.join(", ", roundEnv.getRootElements().stream().map(el -> el.getSimpleName()).toList());
//        procLogger.debug("rootElements: " + rootElements);
//
//        Set<MetaModelElement> metaModelElements = new HashSet<>();
//
//        final Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWithAny(getSupportedAnnotations());
//        procLogger.debug("annotatedElements: " + String.join(", ", annotatedElements.stream().map(Element::getSimpleName).toList()));
//        for (Element element: annotatedElements) {
////            if (element.getKind() != ElementKind.CLASS) {
////                Optional<? extends AnnotationMirror> elementAnnotationMirror = element.getAnnotationMirrors().stream()
////                        .filter(annotMirror -> getSupportedAnnotations().stream()
////                                .map(annotClass -> annotClass.getCanonicalName())
////                                .toList()
////                                .contains(((TypeElement) annotMirror.getAnnotationType().asElement()).getQualifiedName().toString()))
////                        .findAny();
////                String annotationName = elementAnnotationMirror.get().getAnnotationType().asElement().getSimpleName().toString();
////                messager.printMessage(Kind.ERROR, String.format("Only classes can be annotated with %s", annotationName, element));
////                procLogger.debug(String.format("Skipping a non-class element %s", element.toString()));
////                continue;
////            }
//
//            final TypeElement typeElement = (TypeElement) element;
//            if (!MetaModelConstants.isMetamodeled(typeElement))
//                continue;
//
//            final EntityElement entityElement = newEntityElement(typeElement);
//            final MetaModelElement metaModelElement = newMetaModelElement(entityElement);
//            metaModelElements.add(metaModelElement);
//
//            // find properties of this entity that are entity type and include these entities for meta-model generation
//            // this helps find entities that are included from the platform, rather than defined by a domain model,
//            // such as User
//            final Set<PropertyElement> properties = EntityFinder.findDistinctProperties(entityElement, PropertyElement::getName);
//            metaModelElements.addAll(
//                    properties.stream()
//                    .filter(MetaModelGeneratorProcessor::isPropertyTypeMetamodeled)
//                    // it's safe to call getTypeAsTypeElementOrThrow() since elements were previously filtered
//                    .map(propEl -> newMetaModelElement(newEntityElement(propEl.getTypeAsTypeElementOrThrow())))
//                    .toList());
//        }
//        
//        if (inactive != null) {
//            final List<String> inactiveNames = inactive.stream().map(te -> te.getQualifiedName().toString()).toList();
//            metaModelElements = metaModelElements.stream()
//                    .filter(mme -> !inactiveNames.contains(mme.getEntityElement().getTypeElement().getQualifiedName()))
//                    .collect(Collectors.toSet());
//        }
//
//        procLogger.debug("metaModelElements: " + String.join(", ", metaModelElements.stream().map(MetaModelElement::getSimpleName).toList()));
//
//        final DateTime now = DateTime.now();
//        
//        for (MetaModelElement element: metaModelElements) {
//            writeMetaModel(element, metaModelElements, now);
//        }
//
//        // MetaModels class needs to be regenerated only if something changed
//        if (!metaModelElements.isEmpty() || (inactive != null && !inactive.isEmpty())) {
//            try {
//                writeMetaModelsClass(metaModelElements, now);
//            } catch (IOException e) {
//                procLogger.error(e.toString());
//            }
//        }
//
//        procLogger.debug(String.format("xxx PROCESSING ROUND %d END xxx", roundCount));
//
//        roundCount++;
//
//        return true;
//    }
//
//    private void writeMetaModel(final MetaModelElement metaModelElement, Set<MetaModelElement> metaModelElements, final DateTime generationDateTime) {
//        final EntityElement entity = metaModelElement.getEntityElement();
////        procLogger.debug("Entity: " + entity.getSimpleName());
//
//        // ######################## PROPERTIES ########################
//        Set<PropertyElement> properties = new LinkedHashSet<>();
//
//        final EntityElement entityParent = EntityFinder.getParent(entity, elementUtils);
//        final boolean isEntitySuperclassMetamodeled = MetaModelConstants.isMetamodeled(entityParent.getTypeElement());
//
//        if (isEntitySuperclassMetamodeled) {
//            // find only declared properties
//            properties.addAll(EntityFinder.findDeclaredProperties(entity));
//        } else {
//            // find all properties (declared + inherited from <? extends AbstractEntity))
//            properties.addAll(EntityFinder.findDistinctProperties(entity, PropertyElement::getName));
//        }
//        
//        // capture "id" property for persistent entities and those that extend persistent entities
//        if (EntityFinder.isPersistentEntity(entity) || EntityFinder.doesExtendPersistentEntity(entity)) {
//            VariableElement idField = ElementFinder.findField(entity.getTypeElement(), "id");
//            if (!properties.stream().map(PropertyElement::getName).toList().contains("id"))
//                properties.add(new PropertyElement(idField));
//        }
//        
//        // property `desc` should be considered only if entity or any of its supertypes are annotated with `@DescTitle`
//        final boolean descConsidered = properties.stream()
//                .map(PropertyElement::getName)
//                .anyMatch(pname -> pname.equals("desc"));
//        if (descConsidered) {
//            if (entity.getTypeElement().getAnnotation(DescTitle.class) == null) {
//                List<EntityElement> supertypes = EntityFinder.findParents(entity, elementUtils);
//                final boolean parentAnnotatedWithDescTitle = supertypes.stream()
//                        .anyMatch(el -> el.getTypeElement().getAnnotation(DescTitle.class) != null);
//                if (!parentAnnotatedWithDescTitle)
//                    properties.removeIf(prop -> prop.getName().equals("desc"));
//            }
//        }
//
////        procLogger.debug("Properties: " + Arrays.toString(properties.stream().map(PropertyElement::getName).toArray()));
//
//        List<FieldSpec> fieldSpecs = new ArrayList<>();
//        for (PropertyElement prop: properties) {
//            FieldSpec.Builder fieldSpecBuilder = null;
//            final String propName = prop.getName();
//
//            // ### static property holding the property's name ###
//            // private static final String [PROP_NAME]_ = "[PROP_NAME]";
//            fieldSpecs.add(FieldSpec.builder(String.class, propName + "_")
//                    .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
//                    .initializer("$S", propName)
//                    .build());
//
//            // ### instance property ###
//            if (isPropertyTypeMetamodeled(prop)) {
//                MetaModelElement propTypeMetaModelElement = newMetaModelElement(newEntityElement(prop.getTypeAsTypeElementOrThrow()));
//                ClassName propTypeMetaModelClassName = getMetaModelClassName(propTypeMetaModelElement);
//                // property type is target for meta-model generation
//                // private Supplier<[METAMODEL_NAME]> [PROP_NAME];
//                ParameterizedTypeName propTypeName = ParameterizedTypeName.get(ClassName.get(Supplier.class), propTypeMetaModelClassName);
//                fieldSpecBuilder = FieldSpec.builder(propTypeName, propName)
//                        .addModifiers(Modifier.PRIVATE);
//            } else {
//                // private final PropertyMetaModel [PROP_NAME]; 
//                fieldSpecBuilder = FieldSpec.builder(ClassName.get(PropertyMetaModel.class), propName)
//                        .addModifiers(Modifier.PRIVATE, Modifier.FINAL);
//            }
//
//            fieldSpecs.add(fieldSpecBuilder.build());
//        }
//
//        // ######################## METHODS ###########################
//        List<MethodSpec> methodSpecs = new ArrayList<>();
//
//        for (PropertyElement prop: properties) {
//            MethodSpec.Builder methodSpecBuilder = null;
//            final String propName = prop.getName();
//
//            ClassName propTypeMetaModelClassName = null;
//            if (isPropertyTypeMetamodeled(prop)) {
//                MetaModelElement propTypeMetaModelElement = newMetaModelElement(newEntityElement(prop.getTypeAsTypeElementOrThrow()));
//                propTypeMetaModelClassName = getMetaModelClassName(propTypeMetaModelElement);
//                /* property type is target for meta-model generation
//
//                public [METAMODEL_NAME] [PROP_NAME]() {
//                    return [PROP_NAME].get();
//                }
//                 */
//                methodSpecBuilder = MethodSpec.methodBuilder(propName)
//                        .addModifiers(Modifier.PUBLIC)
//                        .returns(propTypeMetaModelClassName)
//                        .addStatement("return $L.get()", propName);
//            } else {
//                /*
//                public PropertyMetaModel [PROP_NAME]() {
//                    return [PROP_NAME];
//                }
//                 */
//                methodSpecBuilder = MethodSpec.methodBuilder(propName)
//                        .addModifiers(Modifier.PUBLIC)
//                        .returns(ClassName.get(PropertyMetaModel.class))
//                        .addStatement("return $L", propName);
//            }
//
//            // javadoc: property title and description
//            final Pair<String, String> propTitleAndDesc = EntityFinder.getPropTitleAndDesc(prop);
//            if (propTitleAndDesc != null) {
//                final String propTitle = propTitleAndDesc.getKey();
//                if (propTitle.length() > 0) {
//                    methodSpecBuilder = methodSpecBuilder.addJavadoc("Title: $L\n<p>\n", propTitle);
//                }
//
//                final String propDesc = propTitleAndDesc.getValue();
//                if (propDesc.length() > 0) {
//                    methodSpecBuilder = methodSpecBuilder.addJavadoc("Description: $L\n<p>\n", propDesc);
//                }
//            }
//
//            // javadoc: property type
//            methodSpecBuilder = methodSpecBuilder.addJavadoc("Type: {@link $T}\n<p>\n", prop.getType());
//
//            // (optional) javadoc: property type's meta-model
//            if (propTypeMetaModelClassName != null) {
//                methodSpecBuilder = methodSpecBuilder.addJavadoc("Meta-model: {@link $T}\n<p>\n", propTypeMetaModelClassName);
//            }
//
//            // javadoc: property annotations
//            final List<String> annotationsStrings = ElementFinder.getFieldAnnotations(prop.toVariableElement()).stream()
//                    .map(annotMirror -> {
//                        String str = String.format("{@literal @}{@link %s}", ElementFinder.getAnnotationMirrorSimpleName(annotMirror));
//                        Map<? extends ExecutableElement, ? extends AnnotationValue> valuesMap = annotMirror.getElementValues();
//                        if (!valuesMap.isEmpty()) {
//                            str += "(";
//                            str += String.join(", ", valuesMap.entrySet().stream()
//                                    .map(e -> String.format("%s = %s", 
//                                            e.getKey().getSimpleName(), 
//                                            e.getValue().toString().replaceAll("@", "{@literal @}")))
//                                    .toList());
//                            str += ")";
//                        }
//                        return str;
//                    })
//                    .toList();
//            methodSpecBuilder = methodSpecBuilder.addJavadoc("$L", String.join("<br>\n", annotationsStrings));
//
//            methodSpecs.add(methodSpecBuilder.build());
//        }
//
//
//        /*
//        @Override
//        public static Class<[ENTITY]> getEntityClass() {
//            return [ENTITY].class;
//        }
//         */
//        final ClassName entityClassName = getEntityClassName(entity);
//        final ClassName abstractEntityClassName = ClassName.get(AbstractEntity.class);
//        final ParameterizedTypeName returnType = ParameterizedTypeName.get(
//                ClassName.get(Class.class), WildcardTypeName.subtypeOf(abstractEntityClassName)); 
//
//        MethodSpec getModelMethod = MethodSpec.methodBuilder("getEntityClass")
//                .addAnnotation(Override.class)
//                .addModifiers(Modifier.PUBLIC)
//                .returns(returnType)
//                .addStatement("return $T.class", entityClassName)
//                .build();
//
//        methodSpecs.add(getModelMethod);
//
//        // ######################## CONSTRUCTORS ######################
//        /*
//        public [ENTITY]MetaModel(String path) {
//            super(path);
//            ...
//        }
//         */
//        MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder()
//                .addModifiers(Modifier.PUBLIC)
//                .addParameter(String.class, "path")
//                .addStatement("super(path)");
//
//        CodeBlock.Builder constructorStatementsBuilder = CodeBlock.builder();
//        for (PropertyElement prop: properties) {
//            final String propName = prop.getName();
//
//            if (isPropertyTypeMetamodeled(prop)) {
//                MetaModelElement propTypeMetaModelElement = newMetaModelElement(newEntityElement(prop.getTypeAsTypeElementOrThrow()));
//                ClassName propTypeMetaModelClassName = getMetaModelClassName(propTypeMetaModelElement);
//
//                /* property type is target for meta-model generation
//
//                this.[PROP_NAME] = () -> {
//                    [METAMODEL_NAME] value = new [METAMODEL_NAME](joinPath([PROP_NAME]_));
//                    [PROP_NAME] = () -> value;
//                    return value;
//                };
//                 */
//                CodeBlock lambda = CodeBlock.builder()
//                        .add("() -> {\n").indent()
//                        .addStatement(
//                                "$T $L = new $T(joinPath($L_))", 
//                                propTypeMetaModelClassName, "value", propTypeMetaModelClassName, propName)
//                        .addStatement(
//                                "$L = () -> $L",
//                                propName, "value")
//                        .addStatement("return $L", "value")
//                        .unindent().add("}")
//                        .build();
//                CodeBlock code = CodeBlock.builder()
//                        .addStatement("this.$L = $L", propName, lambda.toString())
//                        .build();
//                constructorStatementsBuilder = constructorStatementsBuilder.add(code);
//            } else {
//                // this.[PROP_NAME] = new PropertyMetaModel(joinPath([PROP_NAME]_));
//                constructorStatementsBuilder = constructorStatementsBuilder.addStatement(
//                        "this.$L = new $T(joinPath($L_))", 
//                        propName, ClassName.get(PropertyMetaModel.class), propName);
//            }
//        }
//
//        MethodSpec constructor = constructorBuilder.addCode(constructorStatementsBuilder.build()).build();
//        methodSpecs.add(constructor);
//
//        // the empty constructor
//        /*
//        public [ENTITY]MetaModel() {
//            this("");
//        }
//         */
//        MethodSpec emptyConstructor = MethodSpec.constructorBuilder()
//                .addModifiers(Modifier.PUBLIC)
//                .addStatement("this(\"\")")
//                .build();
//        methodSpecs.add(emptyConstructor);
//
//        // class declaration
//        /*
//        public class [ENTITY]MetaModel extends [EntityMetaModel | [PARENT]MetaModel] {
//            ...
//        }
//         */
//        ClassName metaModelSuperclassClassName;
//        if (isEntitySuperclassMetamodeled) {
//            MetaModelElement mme = newMetaModelElement(entityParent);
//            metaModelSuperclassClassName = ClassName.get(mme.getPackageName(), mme.getSimpleName());
//        } else {
//            metaModelSuperclassClassName = ClassName.get(MetaModelConstants.METAMODEL_SUPERCLASS);
//        }
//
//        final String metaModelName = metaModelElement.getSimpleName();
//        final String metaModelPkgName = metaModelElement.getPackageName();
//
//        // sort fields alphabetically
//        fieldSpecs.sort((fs1, fs2) -> fs1.name.compareTo(fs2.name));
//
//        // sort methods alphabetically
//        methodSpecs.sort((ms1, ms2) -> ms1.name.compareTo(ms2.name));
//
//        TypeSpec metaModel = TypeSpec.classBuilder(metaModelName)
//                .addModifiers(Modifier.PUBLIC)
//                .superclass(metaModelSuperclassClassName)
//                .addFields(fieldSpecs)
//                .addMethods(methodSpecs)
//                .build();
//        
//        // javadoc
//        final Pair<String, String> entityTitleAndDesc = EntityFinder.getEntityTitleAndDesc(entity);
//        if (entityTitleAndDesc != null) {
//            final String title = entityTitleAndDesc.getKey();
//            if (title.length() > 0)
//                metaModel = metaModel.toBuilder().addJavadoc(String.format("Title: %s\n<p>\n", title)).build();
//
//            final String desc = entityTitleAndDesc.getValue();
//            if (desc.length() > 0)
//                metaModel = metaModel.toBuilder().addJavadoc(String.format("Description: %s\n<p>\n", desc)).build();
//        } else {
//            final String title = getEntityTitleFromClassName(entity);
//            metaModel = metaModel.toBuilder().addJavadoc(String.format("Title: %s\n<p>\n", title)).build();
//        }
//
//        final String datetime = generationDateTime.toString("dd-MM-YYYY HH:mm:ss.SSS z");
//        metaModel = metaModel.toBuilder()
//                .addJavadoc("Auto-generated meta-model for {@link $T}.\n<p>\n", entityClassName)
//                .addJavadoc(String.format("Generation datetime: %s\n<p>\n", datetime))
//                .addJavadoc(String.format("Generated by {@link %s}.", this.getClass().getSimpleName()))
//                .build();
//
//
//        // ######################## WRITE TO FILE #####################
//        final JavaFile javaFile = JavaFile.builder(metaModelPkgName, metaModel).indent(INDENT).build();
//        try {
//            javaFile.writeTo(filer);
//        } catch (IOException e) {
//            procLogger.error(e.toString());
//        }
//
//        procLogger.info(String.format("Generated %s for entity %s.", metaModel.name, entity.getSimpleName()));
//    }
//
//    public void writeMetaModelsClass(Set<MetaModelElement> metaModelElements, final DateTime generationDateTime) throws IOException {
//        /*
//        public final class MetaModels {
//            public static final [ENTITY]MetaModel [ENTITY] = new [ENTITY]MetaModel();
//        }
//         */
//
//        final TypeElement typeElement = elementUtils.getTypeElement(MetaModelConstants.METAMODELS_CLASS_QUAL_NAME);
//        List<FieldSpec> fieldSpecs = new ArrayList<>();
//
//        // if MetaModels class already exists
//        if (typeElement != null) { 
//            procLogger.debug(String.format("%s found.", MetaModelConstants.METAMODELS_CLASS_QUAL_NAME));
//            Set<VariableElement> fields = ElementFinder.findDeclaredFields(typeElement);
//            
//            //// MetaModelVerifierProcessor runs before this processor and detects inactive meta-models, thus they must be excluded from generation
//            if (!inactive.isEmpty()) {
//                // TODO instead of comparing qualified names MetaModelElement should be used, since the objects being compared are source code elements that stand for meta-models
//                final List<String> inactiveMetaModelsNames = inactive.stream()
//                        .map(mm -> mm.getQualifiedName().toString())
//                        .toList();
//                fields = fields.stream()
//                            .filter(field -> {
//                                TypeElement fieldType = (TypeElement) ((DeclaredType) field.asType()).asElement();
//                                return !inactiveMetaModelsNames.contains(fieldType.getQualifiedName().toString());
//                            })
//                            .collect(Collectors.toSet());
//            }
//
//            // collect existing fields by mapping them from VariableElement to FieldSpec
//            List<FieldSpec> existingFieldSpecs = fields.stream()
//                    .map(field -> getFieldSpecFromMetaModelsClassField(field, elementUtils))
//                    .toList();
//            fieldSpecs.addAll(existingFieldSpecs);
//        } else
//            procLogger.debug(String.format("%s NOT found.", MetaModelConstants.METAMODELS_CLASS_QUAL_NAME));
//
//        //boolean write = !MetaModelVerifierProcessor.INACTIVE_META_MODELS.isEmpty();
//        boolean write = (typeElement == null) || !inactive.isEmpty();
//
//        for (MetaModelElement metaModelElement: metaModelElements) {
//            if (typeElement != null) {
//                // find an existing field coresponding to this meta-model
//                FieldSpec fieldSpec = fieldSpecs.stream()
//                        .filter(fs -> fs.type.equals(getMetaModelClassName(metaModelElement)))
//                        .findAny()
//                        .orElse(null);
//                // if found, then skip
//                if (fieldSpec != null)
//                    continue;
//            }
//
//            // this point has been reached if there is a new meta-model that has not been added as a field to MetaModels yet, so we want to write a new file
//            write = true;
//            final EntityElement entityElement = metaModelElement.getEntityElement();
//
//            // create a field for this meta-model
//            final ClassName fieldTypeName = getMetaModelClassName(metaModelElement);
//            final String fieldName = entityElement.getSimpleName();
//            fieldSpecs.add(FieldSpec.builder(fieldTypeName, fieldName)
//                    .initializer("new $T()", fieldTypeName)
//                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
//                    .build());
//        }
//        
//        if (write) {
//            final String datetime = generationDateTime.toString("dd-MM-YYYY HH:mm:ss.SSS z");
//            TypeSpec metaModelsTypeSpec = TypeSpec.classBuilder(MetaModelConstants.METAMODELS_CLASS_SIMPLE_NAME)
//                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
//                    .addJavadoc(String.format("Generation datetime: %s\n<p>\n", datetime))
//                    .addJavadoc(String.format("Generated by {@link %s}.", this.getClass().getSimpleName()))
//                    .addFields(fieldSpecs)
//                    .build();
//
//            // ######################## WRITE TO FILE #####################
//            JavaFile javaFile = JavaFile.builder(MetaModelConstants.METAMODELS_CLASS_PKG_NAME, metaModelsTypeSpec).indent(INDENT).build();
//            javaFile.writeTo(filer);
//
//            if (typeElement != null)
//                procLogger.info(String.format("Regenerated %s.", metaModelsTypeSpec.name));
//            else
//                procLogger.info(String.format("Generated %s.", metaModelsTypeSpec.name));
//        } else if (typeElement != null)
//            procLogger.debug(String.format("Leaving %s as is", MetaModelConstants.METAMODELS_CLASS_QUAL_NAME));
//    }
//
//    private EntityElement newEntityElement(TypeElement typeElement) {
//        return new EntityElement(typeElement, elementUtils);
//    }
//    
//    public void setInactive(List<TypeElement> inactive) {
//        this.inactive = inactive;
//    }
//    
//    private Configuration getLog4jConfig() {
//        ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder();
//
//        AppenderComponentBuilder console = builder.newAppender("ConsoleAppender", "Console"); 
//
//        String projectDir = options.get("projectdir");
//        String filename = "processor.log";
//        filename = projectDir == null ? filename : projectDir + '/' + filename;
//        AppenderComponentBuilder file = builder.newAppender("FileAppender", "File"); 
//        file.addAttribute("fileName", filename);
//        file.addAttribute("append", "true");
//
//        LayoutComponentBuilder layout = builder.newLayout("PatternLayout");
//        layout.addAttribute("pattern", "%highlight{%d{yyyy-MM-dd HH:mm:ss.SSS} [%-5level] %c{1} --- %msg%n}{ERROR=red}");
//        console.add(layout);
//        file.add(layout);
//
//        builder.add(console);
//        builder.add(file);
//
//        RootLoggerComponentBuilder rootLogger = builder.newRootLogger(Level.DEBUG);
//        rootLogger.add(builder.newAppenderRef("ConsoleAppender"));
//        rootLogger.add(builder.newAppenderRef("FileAppender"));
//        builder.add(rootLogger);
//
//        return builder.build();
//    }
//}
