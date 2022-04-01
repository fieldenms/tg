package ua.com.fielden.platform.processors.meta_model;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
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

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;

import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.utils.Pair;

@AutoService(Processor.class)
@SupportedAnnotationTypes("ua.com.fielden.platform.entity.annotation.MapEntityTo")
@SupportedSourceVersion(SourceVersion.RELEASE_16)
public class MetaModelProcessor extends AbstractProcessor {

    private static final Class<EntityMetaModel> META_MODEL_SUPERCLASS = EntityMetaModel.class;
    
    private static final String META_MODELS_CLASS_SIMPLE_NAME = "MetaModels";
    private static final String META_MODELS_CLASS_PACKAGE_NAME = "meta_models";
    private static final String META_MODELS_CLASS_QUALIFIED_NAME = String.format("%s.%s", META_MODELS_CLASS_PACKAGE_NAME, META_MODELS_CLASS_SIMPLE_NAME);
    private static final String META_MODEL_PKG_NAME_SUFFIX = ".meta";
    private static final String META_MODEL_NAME_SUFFIX = "MetaModel";
    private static final String INDENT = "    ";
    
    private Logger logger;
    private Filer filer;
    private Elements elementUtils;
    private Messager messager;
    
    private static List<Class<? extends Annotation>> getIgnoredPropertyAnnotations() {
        return new ArrayList<>(List.of(IsProperty.class, Title.class));
    }
    
    private static List<String> getIncludedInheritedPropertiesNames() {
        return new ArrayList<>(List.of("active", "key", "desc"));
    }
    
    /**
     * A helper class for conversion of package and class names between an entity and its meta-model. 
     */
    private class MetaModelElement {

        private TypeElement typeElement;

        MetaModelElement(TypeElement typeElement) {
            this.typeElement = typeElement;
        }

        public TypeElement getTypeElement() {
            return typeElement;
        }

        public String getEntityName() {
            return typeElement.getSimpleName().toString();
        }

        public String getEntityPkgName() {
            return elementUtils.getPackageOf(typeElement).getQualifiedName().toString();
        }

        public String getMetaModelName() {
            return getEntityName() + META_MODEL_NAME_SUFFIX;
        }

        public String getMetaModelPkgName() {
            return getEntityPkgName() + META_MODEL_PKG_NAME_SUFFIX;
        }
        
        public Set<PropertyElement> getProperties() {
            final Set<PropertyElement> properties = EntityFinder.findDeclaredProperties(typeElement);
            final List<String> declaredPropertiesNames = properties.stream().map(prop -> prop.getName()).toList();

            final List<PropertyElement> inheritedProperties = EntityFinder.findInheritedProperties(typeElement).stream()
                    .filter(prop -> {
                        String propName = prop.getName();
                        return getIncludedInheritedPropertiesNames().contains(propName) &&
                                !(declaredPropertiesNames.contains(propName));
                    })
                    .toList();
            properties.addAll(inheritedProperties);

            return properties;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getEnclosingInstance().hashCode();
            result = prime * result + Objects.hash(typeElement);
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            MetaModelElement other = (MetaModelElement) obj;
            if (!getEnclosingInstance().equals(other.getEnclosingInstance()))
                return false;
            return Objects.equals(typeElement, other.typeElement);
        }

        private MetaModelProcessor getEnclosingInstance() {
            return MetaModelProcessor.this;
        }
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

        final Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(MapEntityTo.class);
        for (Element element: annotatedElements) {
            if (element.getKind() != ElementKind.CLASS) {
                messager.printMessage(Kind.ERROR, String.format("Only classes can be annotated with %s", MapEntityTo.class.getSimpleName()), element);
                logger.debug(String.format("Skipping a non-class element %s", element.toString()));
                continue;
            }

            final TypeElement typeElement = (TypeElement) element;
            final MetaModelElement metaModelElement = new MetaModelElement(typeElement);
            metaModelElements.add(metaModelElement);

            // filter properties of this entity to find entity type ones and include them for meta-model generation
            // this helps find entities that are included from the platform, rather than defined by a domain model,
            // such as User
            final Set<PropertyElement> propertyElements = metaModelElement.getProperties();
            metaModelElements.addAll(
                    propertyElements.stream()
                    .filter(EntityFinder::isPropertyEntityType)
                    // it's safe to call getTypeAsTypeElementOrThrow() since elements were previously filtered
                    .map(propEl -> new MetaModelElement(propEl.getTypeAsTypeElementOrThrow()))
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
        final Set<PropertyElement> propertyElements = metaModelElement.getProperties();
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
            if (EntityFinder.isPropertyEntityType(prop)) {
                MetaModelElement propTypeMetaModelElement = new MetaModelElement(prop.getTypeAsTypeElementOrThrow());
                ClassName propTypeMetaModelClassName = getMetaModelClassName(propTypeMetaModelElement);

                if (propTypeMetaModelElement.equals(metaModelElement)) {
                    // property is of the same type as the owning entity
                    // private Supplier<[META_MODEL_NAME]> [PROP_NAME];
                    ParameterizedTypeName propTypeName = ParameterizedTypeName.get(ClassName.get(Supplier.class), propTypeMetaModelClassName);
                    fieldSpecBuilder = FieldSpec.builder(propTypeName, propName)
                            .addModifiers(Modifier.PRIVATE);
                } else {
                    // property is entity type
                    // private final [META_MODEL_NAME] [PROP_NAME];
                    fieldSpecBuilder = FieldSpec.builder(propTypeMetaModelClassName, propName)
                            .addModifiers(Modifier.PRIVATE, Modifier.FINAL);
                }
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
            if (EntityFinder.isPropertyEntityType(prop)) {
                MetaModelElement propTypeMetaModelElement = new MetaModelElement(prop.getTypeAsTypeElementOrThrow());
                propTypeMetaModelClassName = getMetaModelClassName(propTypeMetaModelElement);
            
                if (propTypeMetaModelElement.equals(metaModelElement)) {
                    /* property is of the same type as the owning entity

                    public [META_MODEL_NAME] [PROP_NAME]() {
                        return [PROP_NAME].get();
                    }
                     */
                    methodSpecBuilder = MethodSpec.methodBuilder(propName)
                            .addModifiers(Modifier.PUBLIC)
                            .returns(propTypeMetaModelClassName)
                            .addStatement("return $L.get()", propName);
                } else {
                    /* property is entity type

                    public [META_MODEL_NAME] [PROP_NAME]() {
                        return [PROP_NAME];
                    }
                     */
                    methodSpecBuilder = MethodSpec.methodBuilder(propName)
                            .addModifiers(Modifier.PUBLIC)
                            .returns(propTypeMetaModelClassName)
                            .addStatement("return $L", propName);
                }
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
            final List<String> annotNames = ElementFinder.getFieldAnnotationsExcept(prop.toVariableElement(), getIgnoredPropertyAnnotations()).stream()
                    .map(a -> String.format("{@link %s}", ElementFinder.getAnnotationMirrorSimpleName(a)))
                    .toList();
            methodSpecBuilder = methodSpecBuilder.addJavadoc("Annotations: $L\n<p>\n", String.join(", ", annotNames));
            
            methodSpecs.add(methodSpecBuilder.build());
        }

        /*
        public static Class<?> getModelClass() {
            return [ENTITY_NAME].class;
        }
        */
        final ClassName modelClassName = getEntityClassName(metaModelElement);
        final ParameterizedTypeName returnType = ParameterizedTypeName.get(ClassName.get(Class.class), WildcardTypeName.subtypeOf(Object.class));
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

            if (EntityFinder.isPropertyEntityType(prop)) {
                MetaModelElement propTypeMetaModelElement = new MetaModelElement(prop.getTypeAsTypeElementOrThrow());
                ClassName propTypeMetaModelClassName = getMetaModelClassName(propTypeMetaModelElement);
            
                if (propTypeMetaModelElement.equals(metaModelElement)) {
                    /* property is of the same type as the owning entity
                     
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
                    // property is entity type
                    // this.[PROP_NAME] = new [PROP_TYPE_NAME]MetaModel(joinPath([PROP_NAME]_));
                    constructorStatementsBuilder = constructorStatementsBuilder.addStatement(
                            "this.$L = new $T(joinPath($L_))", 
                            propName, propTypeMetaModelClassName, propName);
                }
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
        final String metaModelName = metaModelElement.getMetaModelName();
        final String metaModelPkgName = metaModelElement.getMetaModelPkgName();

        TypeSpec metaModel = TypeSpec.classBuilder(metaModelName)
                .addJavadoc("Auto-generated meta-model for {@link $T}\n<p>\n", modelClassName)
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
        
        logger.info(String.format("Generated %s", metaModel.name));
    }
    
    private void writeMetaModelsClass(Set<MetaModelElement> metaModelElements) throws IOException {
        /*
        public final class MetaModels {
            public static final [ENTITY_NAME]MetaModel [ENTITY_NAME] = new [ENTITY_NAME]MetaModel();
        }
        */

        final TypeElement typeElement = elementUtils.getTypeElement(META_MODELS_CLASS_QUALIFIED_NAME);
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
                        .addJavadoc("Previously generated") // debug
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

            // create a field for this meta-model
            final ClassName fieldTypeName = getMetaModelClassName(metaModelElement);
            final String fieldName = metaModelElement.getEntityName();
            fieldSpecs.add(FieldSpec.builder(fieldTypeName, fieldName)
                    .initializer("new $T()", fieldTypeName)
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .addJavadoc("Newly generated") // debug
                    .build());
        }

        TypeSpec metaModelsTypeSpec = TypeSpec.classBuilder(META_MODELS_CLASS_SIMPLE_NAME)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addFields(fieldSpecs)
                .build();

        // ######################## WRITE TO FILE #####################
        JavaFile javaFile = JavaFile.builder(META_MODELS_CLASS_PACKAGE_NAME, metaModelsTypeSpec).indent(INDENT).build();
        javaFile.writeTo(filer);

        logger.info(String.format("Generated %s", metaModelsTypeSpec.name));
    }
    
    private static ClassName getMetaModelClassName(MetaModelElement element) {
        return ClassName.get(element.getMetaModelPkgName(), element.getMetaModelName());
    }

    private static ClassName getEntityClassName(MetaModelElement element) {
        return ClassName.get(element.getEntityPkgName(), element.getEntityName());
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


