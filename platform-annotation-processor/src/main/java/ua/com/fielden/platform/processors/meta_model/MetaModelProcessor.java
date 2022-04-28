package ua.com.fielden.platform.processors.meta_model;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic.Kind;

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
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;

import ua.com.fielden.platform.annotations.meta_model.DomainEntity;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.utils.Pair;

@AutoService(Processor.class)
@SupportedAnnotationTypes({"ua.com.fielden.platform.entity.annotation.MapEntityTo",
                            "ua.com.fielden.platform.annotations.meta_model.DomainEntity"})
@SupportedSourceVersion(SourceVersion.RELEASE_16)
public class MetaModelProcessor extends AbstractProcessor {

    private static final Class<EntityMetaModel> METAMODEL_SUPERCLASS = EntityMetaModel.class;

    public static final String METAMODELS_CLASS_SIMPLE_NAME = "MetaModels";
    public static final String METAMODELS_CLASS_PKG_NAME = "meta_models";
    public static final String METAMODELS_CLASS_QUAL_NAME = String.format("%s.%s", 
            METAMODELS_CLASS_PKG_NAME, METAMODELS_CLASS_SIMPLE_NAME);

    private static final String INDENT = "    ";
    private static final String LOG_FILENAME = "proc.log";
    private static final String ECLIPSE_OPTION_KEY = "projectdir";

    private Logger logger;
    private ProcessorLogger procLogger;
    private Filer filer;
    private Elements elementUtils;
    private Messager messager;
    private Map<String, String> options;
    private boolean fromMaven;

    public static Set<Class<? extends Annotation>> getSupportedAnnotations() {
        return Set.of(MapEntityTo.class, DomainEntity.class);
    }

    public static List<Class<? extends Annotation>> ignoredPropertyAnnotations() {
        return new ArrayList<>(List.of(IsProperty.class));
    }

    private static ClassName getMetaModelClassName(MetaModelElement element) {
        return ClassName.get(element.getPackageName(), element.getSimpleName());
    }

    private static ClassName getEntityClassName(EntityElement element) {
        return ClassName.get(element.getPackageName(), element.getSimpleName());
    }

    private static boolean isMetamodeled(TypeElement element) {
        return EntityFinder.isEntity(element) && 
                EntityFinder.isDomainEntity(EntityElement.wrapperFor(element));
    }
    
    private static boolean isPropertyTypeMetamodeled(PropertyElement element) {
        TypeElement propType = null;
        try {
            propType = element.getTypeAsTypeElement();
        } catch (Exception e) {
            // property type is not a declared type
            return false;
        }

        return isMetamodeled(propType);
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.filer = processingEnv.getFiler();
        this.elementUtils = processingEnv.getElementUtils();
        this.messager = processingEnv.getMessager();
        this.options = processingEnv.getOptions();

        // processor started from Eclipse?
        final String projectDir = options.get(ECLIPSE_OPTION_KEY);
        this.fromMaven = projectDir == null;

        // log4j configuration
        Configurator.initialize(getLog4jConfig());
        this.logger = LogManager.getLogger(MetaModelProcessor.class);

        // ProcessorLogger
        String logFilename = this.fromMaven ? LOG_FILENAME : projectDir + "/" + LOG_FILENAME;
        String source = this.fromMaven ? "mvn" : "Eclipse";
        this.procLogger = new ProcessorLogger(logFilename, source, logger);
        procLogger.ln();
        procLogger.info(String.format("%s initialized.", this.getClass().getSimpleName()));

        // debug 
        procLogger.debug("Options: [" + String.join(", ", 
                                                    options.keySet().stream()
                                                        .map(k -> String.format("%s=%s", k, options.get(k)))
                                                        .toList()) + "]");
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        procLogger.debug("=== PROCESSING ROUND START ===");

        // debug
        String rootElements = String.join(", ", roundEnv.getRootElements().stream().map(el -> el.getSimpleName()).toList());
        procLogger.debug("rootElements: " + rootElements);

        Set<MetaModelElement> metaModelElements = new HashSet<>();

        final Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWithAny(getSupportedAnnotations());
        for (Element element: annotatedElements) {
            if (element.getKind() != ElementKind.CLASS) {
                Optional<? extends AnnotationMirror> elementAnnotationMirror = element.getAnnotationMirrors().stream()
                        .filter(annotMirror -> getSupportedAnnotations().stream()
                                .map(annotClass -> annotClass.getCanonicalName())
                                .toList()
                                .contains(((TypeElement) annotMirror.getAnnotationType().asElement()).getQualifiedName().toString()))
                        .findAny();
                String annotationName = elementAnnotationMirror.get().getAnnotationType().asElement().getSimpleName().toString();
                messager.printMessage(Kind.ERROR, String.format("Only classes can be annotated with %s", annotationName, element));
                procLogger.debug(String.format("Skipping a non-class element %s", element.toString()));
                continue;
            }

            final TypeElement typeElement = (TypeElement) element;
            final EntityElement entityElement = newEntityElement(typeElement);
            final MetaModelElement metaModelElement = new MetaModelElement(entityElement);
            metaModelElements.add(metaModelElement);
            
            // TODO: optimize by annotating platform level entities with @DomainEntity
            // filter properties of this entity to find entity type ones and include them for meta-model generation
            // this helps find entities that are included from the platform, rather than defined by a domain model,
            // such as User
            final Set<PropertyElement> properties = EntityFinder.findDistinctProperties(entityElement, PropertyElement::getName);
            metaModelElements.addAll(
                    properties.stream()
                    .filter(MetaModelProcessor::isPropertyTypeMetamodeled)
                    // it's safe to call getTypeAsTypeElementOrThrow() since elements were previously filtered
                    .map(propEl -> new MetaModelElement(newEntityElement(propEl.getTypeAsTypeElementOrThrow())))
                    .toList());
        }
        
        for (MetaModelElement element: metaModelElements) {
            writeMetaModel(element, metaModelElements);
        }

        // MetaModels class needs to be regenerated only if something changed
        if (metaModelElements.size() > 0) {
            try {
                writeMetaModelsClass(metaModelElements);
            } catch (IOException e) {
                procLogger.error(e.toString());
            }
        }

        procLogger.debug("xxx PROCESSING ROUND END xxx");

        // the log file is closed here, that is, right at the end of the 1st processing round
        // this should be modified in case more than 1 processing round is needed
        procLogger.end();

        return true;
    }

    private void writeMetaModel(final MetaModelElement metaModelElement, Set<MetaModelElement> metaModelElements) {
        final EntityElement entity = metaModelElement.getEntityElement();

        // ######################## PROPERTIES ########################
        final Set<PropertyElement> properties = new HashSet<>();

        final EntityElement entityParent = EntityFinder.getParent(entity, elementUtils);
        final boolean isEntitySuperclassMetamodeled = isMetamodeled(entityParent.getTypeElement());

        if (isEntitySuperclassMetamodeled) {
            // find only declared properties
            properties.addAll(EntityFinder.findDeclaredProperties(entity));
        } else {
            // find all properties (declared + inherited from <? extends AbstractEntity))
            properties.addAll(EntityFinder.findDistinctProperties(entity, PropertyElement::getName));
        }

        List<FieldSpec> fieldSpecs = new ArrayList<>();
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
            if (isPropertyTypeMetamodeled(prop)) {
                MetaModelElement propTypeMetaModelElement = new MetaModelElement(newEntityElement(prop.getTypeAsTypeElementOrThrow()));
                ClassName propTypeMetaModelClassName = getMetaModelClassName(propTypeMetaModelElement);
                // property type is target for meta-model generation
                // private Supplier<[METAMODEL_NAME]> [PROP_NAME];
                ParameterizedTypeName propTypeName = ParameterizedTypeName.get(ClassName.get(Supplier.class), propTypeMetaModelClassName);
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

            ClassName propTypeMetaModelClassName = null;
            if (isPropertyTypeMetamodeled(prop)) {
                MetaModelElement propTypeMetaModelElement = new MetaModelElement(newEntityElement(prop.getTypeAsTypeElementOrThrow()));
                propTypeMetaModelClassName = getMetaModelClassName(propTypeMetaModelElement);
                /* property type is target for meta-model generation

                public [METAMODEL_NAME] [PROP_NAME]() {
                    return [PROP_NAME].get();
                }
                 */
                methodSpecBuilder = MethodSpec.methodBuilder(propName)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(propTypeMetaModelClassName)
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
            if (propTypeMetaModelClassName != null) {
                methodSpecBuilder = methodSpecBuilder.addJavadoc("Meta-model: {@link $T}\n<p>\n", propTypeMetaModelClassName);
            }

            // javadoc: all annotations of a property (except ignored ones)
            final List<String> annotNames = ElementFinder.getFieldAnnotationsExcept(prop.toVariableElement(), ignoredPropertyAnnotations()).stream()
                    .map(a -> String.format("{@link %s}", ElementFinder.getAnnotationMirrorSimpleName(a)))
                    .toList();
            methodSpecBuilder = methodSpecBuilder.addJavadoc("Annotations: $L", String.join(", ", annotNames));

            methodSpecs.add(methodSpecBuilder.build());
        }


        /*
        public static Class<? extends AbstractEntity> getModelClass() {
            return [ENTITY].class;
        }
         */
        final ClassName modelClassName = getEntityClassName(entity);
        final ClassName abstractEntityClassName = ClassName.get(AbstractEntity.class);
        final ParameterizedTypeName returnType = ParameterizedTypeName.get(
                ClassName.get(Class.class),
                WildcardTypeName.subtypeOf(abstractEntityClassName)
                );

        MethodSpec getModelMethod = MethodSpec.methodBuilder("getModelClass")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(returnType)
                .addStatement("return $T.class", modelClassName)
                .build();

        methodSpecs.add(getModelMethod);

        // ######################## CONSTRUCTORS ######################
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

            if (isPropertyTypeMetamodeled(prop)) {
                MetaModelElement propTypeMetaModelElement = new MetaModelElement(newEntityElement(prop.getTypeAsTypeElementOrThrow()));
                ClassName propTypeMetaModelClassName = getMetaModelClassName(propTypeMetaModelElement);

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

        MethodSpec constructor = constructorBuilder.addCode(constructorStatementsBuilder.build()).build();
        methodSpecs.add(constructor);

        // the empty constructor
        /*
        public [ENTITY]MetaModel() {
            this("");
        }
         */
        MethodSpec emptyConstructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addStatement("this(\"\")")
                .build();
        methodSpecs.add(emptyConstructor);

        // class declaration
        /*
        public class [ENTITY]MetaModel extends [EntityMetaModel | [PARENT]MetaModel] {
            ...
        }
         */
        ClassName metaModelSuperclassClassName;
        if (isEntitySuperclassMetamodeled) {
            MetaModelElement mme = new MetaModelElement(entityParent);
            metaModelSuperclassClassName = ClassName.get(mme.getPackageName(), mme.getSimpleName());
        } else {
            metaModelSuperclassClassName = ClassName.get(METAMODEL_SUPERCLASS);
        }

        final String metaModelName = metaModelElement.getSimpleName();
        final String metaModelPkgName = metaModelElement.getPackageName();
        final String now = DateTime.now().toString("dd-MM-YYYY HH:mm:ss.SSS z");

        TypeSpec metaModel = TypeSpec.classBuilder(metaModelName)
                .addJavadoc("Auto-generated meta-model for {@link $T}.\n<p>\n", modelClassName)
                .addJavadoc(String.format("Generation datetime: %s", now))
                .addModifiers(Modifier.PUBLIC)
                .superclass(metaModelSuperclassClassName)
                .addFields(fieldSpecs)
                .addMethods(methodSpecs)
                .build();

        // ######################## WRITE TO FILE #####################
        JavaFile javaFile = JavaFile.builder(metaModelPkgName, metaModel).indent(INDENT).build();
        try {
            javaFile.writeTo(filer);
        } catch (IOException e) {
            procLogger.error(e.toString());
        }

        procLogger.info(String.format("Generated %s for entity %s.", metaModel.name, entity.getSimpleName()));
    }

    private void writeMetaModelsClass(Set<MetaModelElement> metaModelElements) throws IOException {
        /*
        public final class MetaModels {
            public static final [ENTITY]MetaModel [ENTITY] = new [ENTITY]MetaModel();
        }
         */

        final TypeElement typeElement = elementUtils.getTypeElement(METAMODELS_CLASS_QUAL_NAME);
        List<FieldSpec> fieldSpecs = new ArrayList<>();

        // if MetaModels class already exists
        if (typeElement != null) { 
            Set<VariableElement> fields = ElementFinder.findDeclaredFields(typeElement);

            // collect existing fields by mapping them from VariableElement to FieldSpec
            List<FieldSpec> existingFieldSpecs = fields.stream().map(field -> {
                String fieldName = field.getSimpleName().toString();
                TypeElement fieldTypeElement = (TypeElement) ((DeclaredType) field.asType()).asElement();
                String fieldTypePkgName = elementUtils.getPackageOf(fieldTypeElement).getQualifiedName().toString();
                ClassName className = ClassName.get(fieldTypePkgName, ElementFinder.getVariableTypeSimpleName(field));
                return FieldSpec.builder(className, fieldName)
                        .initializer("new $T()", className)
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                        .build();
            }).toList();
            fieldSpecs.addAll(existingFieldSpecs);
        }

        boolean write = false;
        for (MetaModelElement metaModelElement: metaModelElements) {
            if (typeElement != null) {
                // find an existing field coresponding to this meta-model
                FieldSpec fieldSpec = fieldSpecs.stream()
                        .filter(fs -> fs.type.equals(getMetaModelClassName(metaModelElement)))
                        .findAny()
                        .orElse(null);
                // if found, then skip
                if (fieldSpec != null)
                    continue;
            }

            write = true;
            final EntityElement entityElement = metaModelElement.getEntityElement();

            // create a field for this meta-model
            final ClassName fieldTypeName = getMetaModelClassName(metaModelElement);
            final String fieldName = entityElement.getSimpleName();
            fieldSpecs.add(FieldSpec.builder(fieldTypeName, fieldName)
                    .initializer("new $T()", fieldTypeName)
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .build());
        }
        
        if (write) {
            TypeSpec metaModelsTypeSpec = TypeSpec.classBuilder(METAMODELS_CLASS_SIMPLE_NAME)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addFields(fieldSpecs)
                    .build();

            // ######################## WRITE TO FILE #####################
            JavaFile javaFile = JavaFile.builder(METAMODELS_CLASS_PKG_NAME, metaModelsTypeSpec).indent(INDENT).build();
            javaFile.writeTo(filer);

            if (typeElement != null)
                procLogger.info(String.format("Regenerated %s.", metaModelsTypeSpec.name));
            else
                procLogger.info(String.format("Generated %s.", metaModelsTypeSpec.name));
        }
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