package ua.com.fielden.platform.processors.meta_model;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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

    private static final Class<EntityMetaModel> META_MODEL_SUPERCLASS = EntityMetaModel.class;
    
    public static final String META_MODELS_CLASS_SIMPLE_NAME = "MetaModels";
    public static final String META_MODELS_CLASS_PACKAGE_NAME = "meta_models";
    public static final String INDENT = "    ";
    
    private Logger logger;
    private Filer filer;
    private Elements elementUtils;
    private Messager messager;
    
    private static String metaModelsClassQualifiedName() {
        return String.format("%s.%s", 
                META_MODELS_CLASS_PACKAGE_NAME, META_MODELS_CLASS_SIMPLE_NAME);
    }
    
    public static Set<Class<? extends Annotation>> getSupportedAnnotations() {
        return Set.of(MapEntityTo.class, DomainEntity.class);
    }
    
    public static List<Class<? extends Annotation>> ignoredPropertyAnnotations() {
        return new ArrayList<>(List.of(IsProperty.class));
    }
    
    public static List<String> includedInheritedPropertiesNames() {
        return new ArrayList<>(List.of("active", "key", "desc"));
    }

    private static ClassName getMetaModelClassName(MetaModelElement element) {
        return ClassName.get(element.getPackageName(), element.getSimpleName());
    }

    private static ClassName getEntityClassName(EntityElement element) {
        return ClassName.get(element.getPackageName(), element.getSimpleName());
    }
    
    /**
     * Find unique-by-name properties of an entity.
     */
    private static Set<PropertyElement> findUniqueProperties(EntityElement entityElement) {
        Set<PropertyElement> uniqueProperties = new HashSet<>();
        Set<String> uniquePropNames = new HashSet<>();
        Set<PropertyElement> properties = EntityFinder.findProperties(entityElement, includedInheritedPropertiesNames());

        for (PropertyElement prop: properties) {
            String name = prop.getName();
            if (!uniquePropNames.contains(name)) {
                uniqueProperties.add(prop);
                uniquePropNames.add(name);
            }
        }
        
        return uniqueProperties;
    }
    
    private static boolean isPropertyTypeMetaModelTarget(PropertyElement element) {
        TypeElement propType = null;
        try {
            propType = element.getTypeAsTypeElement();
        } catch (Exception e) {
            return false;
        }

        for (Class<? extends Annotation> annotClass: getSupportedAnnotations()) {
            if (propType.getAnnotation(annotClass) != null) {
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.filer = processingEnv.getFiler();
        this.elementUtils = processingEnv.getElementUtils();
        this.messager = processingEnv.getMessager();
        // log4j configuration
        Configurator.initialize(getConfig());
        this.logger = LogManager.getLogger(MetaModelProcessor.class);
        logger.info("Initialized");
    }
    
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        logger.debug("=== PROCESSING ROUND START ===");

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
                logger.debug(String.format("Skipping a non-class element %s", element.toString()));
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
            final Set<PropertyElement> propertyElements = findUniqueProperties(entityElement);
            metaModelElements.addAll(
                    propertyElements.stream()
                    .filter(MetaModelProcessor::isPropertyTypeMetaModelTarget)
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
                logger.error(e.toString());
            }
        }

        logger.debug("xxx PROCESSING ROUND END xxx");
        return true;
    }

    private void writeMetaModel(final MetaModelElement metaModelElement, Set<MetaModelElement> metaModelElements) {
        // ######################## PROPERTIES ########################
        final EntityElement entityElement = metaModelElement.getEntityElement();
        final Set<PropertyElement> propertyElements = findUniqueProperties(entityElement);
        List<FieldSpec> fieldSpecs = new ArrayList<>();
        
        for (PropertyElement prop: propertyElements) {
            FieldSpec.Builder fieldSpecBuilder = null;
            final String propName = prop.getName();

            // ### static property holding the property's name ###
            // private static final String [PROP_NAME]_ = "[PROP_NAME]";
            fieldSpecs.add(FieldSpec.builder(String.class, propName + "_")
                    .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                    .initializer("$S", propName)
                    .build());
            
            // ### instance property ###
            if (isPropertyTypeMetaModelTarget(prop)) {
                MetaModelElement propTypeMetaModelElement = new MetaModelElement(newEntityElement(prop.getTypeAsTypeElementOrThrow()));
                ClassName propTypeMetaModelClassName = getMetaModelClassName(propTypeMetaModelElement);
                // property type is target for meta-model generation
                // private Supplier<[META_MODEL_NAME]> [PROP_NAME];
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

        for (PropertyElement prop: propertyElements) {
            MethodSpec.Builder methodSpecBuilder = null;
            final String propName = prop.getName();

            ClassName propTypeMetaModelClassName = null;
            if (isPropertyTypeMetaModelTarget(prop)) {
                MetaModelElement propTypeMetaModelElement = new MetaModelElement(newEntityElement(prop.getTypeAsTypeElementOrThrow()));
                propTypeMetaModelClassName = getMetaModelClassName(propTypeMetaModelElement);
                /* property type is target for meta-model generation
                
                public [META_MODEL_NAME] [PROP_NAME]() {
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
            return [ENTITY_NAME].class;
        }
        */
        final ClassName modelClassName = getEntityClassName(entityElement);
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
        public [ENTITY_NAME]MetaModel(String path) {
            super(path);
            ...
        }
        */
        MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String.class, "path")
                .addStatement("super(path)");

        CodeBlock.Builder constructorStatementsBuilder = CodeBlock.builder();
        for (PropertyElement prop: propertyElements) {
            final String propName = prop.getName();

            if (isPropertyTypeMetaModelTarget(prop)) {
                MetaModelElement propTypeMetaModelElement = new MetaModelElement(newEntityElement(prop.getTypeAsTypeElementOrThrow()));
                ClassName propTypeMetaModelClassName = getMetaModelClassName(propTypeMetaModelElement);

                /* property type is target for meta-model generation

                this.[PROP_NAME] = () -> {
                    [META_MODEL_NAME] value = new [META_MODEL_NAME](joinPath([PROP_NAME]_));
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
        public [ENTITY_NAME]MetaModel() {
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
        public final class [ENTITY_NAME]MetaModel extends EntityMetaModel {
            ...
        }
        */
        final ClassName metaModelSuperclassClassName = ClassName.get(META_MODEL_SUPERCLASS);
        final String metaModelName = metaModelElement.getSimpleName();
        final String metaModelPkgName = metaModelElement.getPackageName();
        final String now = DateTime.now().toString("dd-MM-YYYY HH:mm:ss.SSS z");

        TypeSpec metaModel = TypeSpec.classBuilder(metaModelName)
                .addJavadoc("Auto-generated meta-model for {@link $T}.\n<p>\n", modelClassName)
                .addJavadoc(String.format("Generation datetime: %s", now))
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .superclass(metaModelSuperclassClassName)
                .addFields(fieldSpecs)
                .addMethods(methodSpecs)
                .build();

        // ######################## WRITE TO FILE #####################
        JavaFile javaFile = JavaFile.builder(metaModelPkgName, metaModel).indent(INDENT).build();
        try {
            javaFile.writeTo(filer);
        } catch (IOException e) {
            logger.error(e.toString());
        }
        
        logger.info(String.format("Generated %s for entity %s.", metaModel.name, entityElement.getSimpleName()));
    }
    
    private void writeMetaModelsClass(Set<MetaModelElement> metaModelElements) throws IOException {
        /*
        public final class MetaModels {
            public static final [ENTITY_NAME]MetaModel [ENTITY_NAME] = new [ENTITY_NAME]MetaModel();
        }
        */

        final TypeElement typeElement = elementUtils.getTypeElement(metaModelsClassQualifiedName());
        List<FieldSpec> fieldSpecs = new ArrayList<>();
        
        // if MetaModels class already exists
        if (typeElement != null) { 
            logger.debug("MetaModels class exists");

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

        for (MetaModelElement metaModelElement: metaModelElements) {
            // if a field for this meta-model already exists, then skip it
            // since changes to a particular meta-model do not affect the MetaModels class
            FieldSpec fieldSpec = fieldSpecs.stream()
                    .filter(fs -> fs.type.equals(getMetaModelClassName(metaModelElement)))
                    .findAny()
                    .orElse(null);
            if (fieldSpec != null) {
                continue;
            }
            
            final EntityElement entityElement = metaModelElement.getEntityElement();

            // create a field for this meta-model
            final ClassName fieldTypeName = getMetaModelClassName(metaModelElement);
            final String fieldName = entityElement.getSimpleName();
            fieldSpecs.add(FieldSpec.builder(fieldTypeName, fieldName)
                    .initializer("new $T()", fieldTypeName)
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .build());
        }

        TypeSpec metaModelsTypeSpec = TypeSpec.classBuilder(META_MODELS_CLASS_SIMPLE_NAME)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addFields(fieldSpecs)
                .build();

        // ######################## WRITE TO FILE #####################
        JavaFile javaFile = JavaFile.builder(META_MODELS_CLASS_PACKAGE_NAME, metaModelsTypeSpec).indent(INDENT).build();
        javaFile.writeTo(filer);

        logger.info(String.format("Generated %s.", metaModelsTypeSpec.name));
    }
    
    private EntityElement newEntityElement(TypeElement typeElement) {
        return new EntityElement(typeElement, elementUtils);
    }

    private Configuration getConfig() {
        ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder();

        AppenderComponentBuilder console = builder.newAppender("ConsoleAppender", "Console"); 

        AppenderComponentBuilder file = builder.newAppender("FileAppender", "File"); 
        file.addAttribute("fileName", "processor.log");
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