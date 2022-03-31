package ua.com.fielden.platform.processors.meta_model;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

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
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

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
    private class MetaModelClazz {

        private TypeElement typeElement;
        
        MetaModelClazz(TypeElement typeElement) {
            this.typeElement = typeElement;
        }

        public TypeElement getTypeElement() {
            return typeElement;
        }

        public void setTypeElement(TypeElement typeElement) {
            this.typeElement = typeElement;
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
            MetaModelClazz other = (MetaModelClazz) obj;
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
    }
    
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        logger.info("=== PROCESSING ROUND START ===");

        Set<MetaModelClazz> metaModelClazzes = new HashSet<>();

        final Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(MapEntityTo.class);
        for (Element element: annotatedElements) {
            if (element.getKind() != ElementKind.CLASS) {
                messager.printMessage(
                        Kind.ERROR,
                        String.format("Only classes can be annotated with %s", GenerateMetaModel.class),
                        element);
                continue;
            }

            final TypeElement typeElement = (TypeElement) element;
            final MetaModelClazz metaModelClazz = new MetaModelClazz(typeElement);
            metaModelClazzes.add(metaModelClazz);


            writeMetaModel(typeElement, metaModelClazz);

            metaModelClazzes.add(metaModelClazz);
        }

        if (metaModelClazzes.size() > 0) {
            try {
                writeMetaModelsClass(metaModelClazzes);
            } catch (IOException e) {
                logger.error(e.toString());
            }
        }

        logger.debug("xxx PROCESSING ROUND END xxx");
        return true;
    }

    private void writeMetaModel(final TypeElement entityTypeElement, final MetaModelClazz metaModelClazz) {
        /* ==========
         * Properties
         * ========== */
        final Set<VariableElement> properties = findEntityAllProperties(entityTypeElement);

        List<FieldSpec> fieldSpecs = new ArrayList<>();
        
        FieldSpec.Builder fieldSpecBuilder = null;
        for (VariableElement prop: properties) {
            final String propName = prop.getSimpleName().toString();
            final TypeMirror propType = prop.asType();

            /* static property holding the property's name
             
            private static final String [NAME]_ = "[NAME]";
            */
            fieldSpecs.add(FieldSpec.builder(String.class, propName + "_")
                    .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                    .initializer("$S", propName)
                    .build());
            
            /* instance property capturing both property's name and context
            
            if property's type is another annotated entity
                public final [META_MODEL_NAME] [PROP_NAME];
            else
                public final PropertyMetaModel [PROP_NAME]; 
            */
            ClassName propTypeMetaModelClassName = null;
            if (propType.getKind() == TypeKind.DECLARED) { 
                final Element propTypeAsElement = ((DeclaredType) propType).asElement(); 
                if (propTypeAsElement.getAnnotation(GenerateMetaModel.class) != null) {
                    final String propTypeSimpleName = propTypeAsElement.getSimpleName().toString();
                    final String propTypePkgName = elementUtils.getPackageOf(propTypeAsElement).getQualifiedName().toString();
                    propTypeMetaModelClassName = ClassName.get(propTypePkgName + META_MODEL_PKG_NAME_SUFFIX, propTypeSimpleName + META_MODEL_NAME_SUFFIX);
                }
            }
            
            if (propTypeMetaModelClassName != null) {
                fieldSpecBuilder = FieldSpec.builder(propTypeMetaModelClassName, propName);
            } else {
                fieldSpecBuilder = FieldSpec.builder(ClassName.get(PropertyMetaModel.class), propName);
            }
            
            // javadoc: property title and description
            final Pair<String, String> propTitleAndDesc = EntityFinder.getPropTitleAndDesc(prop);
            if (propTitleAndDesc != null) {
                final String propTitle = propTitleAndDesc.getKey();
                if (propTitle.length() > 0) {
                    fieldSpecBuilder = fieldSpecBuilder.addJavadoc("Title: $L\n<p>\n", propTitle);
                }
                
                final String propDesc = propTitleAndDesc.getValue();
                if (propDesc.length() > 0) {
                    fieldSpecBuilder = fieldSpecBuilder.addJavadoc("Description: $L\n<p>\n", propDesc);
                }
            }
            
            // javadoc: property type
            fieldSpecBuilder = fieldSpecBuilder.addJavadoc("Type: {@link $T}\n<p>\n", propType);
            
            // javadoc: property type's meta-model
            if (propTypeMetaModelClassName != null) {
                fieldSpecBuilder = fieldSpecBuilder.addJavadoc("Meta-model: {@link $T}\n<p>\n", propTypeMetaModelClassName);
            }
            
            // javadoc: all annotations of a property (except ignored ones)
            final List<String> annotNames = ElementFinder.getFieldAnnotationsExcept(prop, getIgnoredPropertyAnnotations()).stream()
                    .map(a -> String.format("{@link %s}", ElementFinder.getAnnotationMirrorSimpleName(a)))
                    .toList();
            fieldSpecBuilder = fieldSpecBuilder.addJavadoc("Annotations: $L\n<p>\n", String.join(", ", annotNames));
            
            fieldSpecs.add(fieldSpecBuilder.addModifiers(Modifier.PUBLIC, Modifier.FINAL).build());
        }
        
        /* =======
         * Methods
         * ======= */

        List<MethodSpec> methodSpecs = new ArrayList<>();

        /*
        public static Class<?> getModelClass() {
            return [NAME].class;
        }
        */
        final ClassName modelClassName = ClassName.get(metaModelClazz.getEntityPkgName(), metaModelClazz.getEntityName());
        final ParameterizedTypeName returnType = ParameterizedTypeName.get(ClassName.get(Class.class), WildcardTypeName.subtypeOf(Object.class));
        MethodSpec getModelMethod = MethodSpec.methodBuilder("getModelClass")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(returnType)
                .addStatement("return $T.class", modelClassName)
                .build();

        methodSpecs.add(getModelMethod);

        /* ============
         * Constructors
         * ============ */
        
        /* 
        public [NAME]MetaModel(String context) {
            super(context);
            for each property
                if property's type is also annotated
                    this.[PROP_NAME] = new [PROP_TYPE_NAME]MetaModel(joinPath([PROP_NAME]_));
                else
                    this.[PROP_NAME] = new PropertyMetaModel(joinPath([PROP_NAME]_));
        }
        */
        MethodSpec.Builder constructorBuilder =  MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String.class, "path")
                .addStatement("super(path)");

        CodeBlock.Builder constructorStatementsBuilder = CodeBlock.builder();
        
        for (VariableElement prop: properties) {
            final String propName = prop.getSimpleName().toString();
            final TypeMirror propType = prop.asType();

            ClassName propTypeMetaModelClassName = null;
            if (propType.getKind() == TypeKind.DECLARED) { 
                final Element propTypeAsElement = ((DeclaredType) propType).asElement(); 
                if (propTypeAsElement.getAnnotation(GenerateMetaModel.class) != null) {
                    final String propTypeSimpleName = propTypeAsElement.getSimpleName().toString();
                    final String propTypePackageName = elementUtils.getPackageOf(propTypeAsElement).getQualifiedName().toString();
                    propTypeMetaModelClassName = ClassName.get(propTypePackageName + META_MODEL_PKG_NAME_SUFFIX, propTypeSimpleName + META_MODEL_NAME_SUFFIX);
                }
            }

            if (propTypeMetaModelClassName != null) {
                constructorStatementsBuilder = constructorStatementsBuilder.addStatement(
                        "this.$L = new $T(joinPath($L_))", 
                        propName, propTypeMetaModelClassName, propName);
            } else {
                constructorStatementsBuilder = constructorStatementsBuilder.addStatement(
                        "this.$L = new $T(joinPath($L_))", 
                        propName, ClassName.get(PropertyMetaModel.class), propName);
            }
        }

        MethodSpec constructor = constructorBuilder.addCode(constructorStatementsBuilder.build()).build();
        methodSpecs.add(constructor);
                
        /* =====================
         * The empty constructor
         * ===================== */

        /*
        public [NAME]MetaModel() {
            this("");
        }
        */
        MethodSpec emptyConstructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addStatement("this(\"\")")
                .build();
        methodSpecs.add(emptyConstructor);


        /*
        public final class [NAME]MetaModel extends EntityMetaModel {
            ...
        }
        */
        final ClassName metaModelSuperclassClassName = ClassName.get(META_MODEL_SUPERCLASS);
        final String metaModelName = metaModelClazz.getMetaModelName();
        final String metaModelPkgName = metaModelClazz.getMetaModelPkgName();

//        AnnotationSpec entityMetaModelAnnotation = AnnotationSpec.builder(EntityMetaModel.class).addMember("value", "$T.class", modelClassName).build();

        TypeSpec metaModel = TypeSpec.classBuilder(metaModelName)
                .addJavadoc("Auto-generated meta-model for {@link $T}\n<p>\n", modelClassName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .superclass(metaModelSuperclassClassName)
//                .addAnnotation(entityMetaModelAnnotation)
                .addFields(fieldSpecs)
                .addMethods(methodSpecs)
                .build();

        /* ===============
         * Writing to file
         * =============== */
        JavaFile javaFile = JavaFile.builder(metaModelPkgName, metaModel).indent(INDENT).build();
        try {
            javaFile.writeTo(filer);
        } catch (IOException e) {
            logger.error(e.toString());
        }
        
        logger.info(String.format("Generated %s", metaModel.name));
    }
    
    private void writeMetaModelsClass(List<MetaModelClazz> metaModelClazzes) throws IOException {
        logger.debug(String.format("Generating %s", META_MODELS_CLASS_SIMPLE_NAME));
        
        /*
        public final class MetaModels {
            public static final [NAME]MetaModel [NAME] = new [NAME]MetaModel();
        }
        */


        final String qualifiedName = String.format("%s.%s", META_MODELS_CLASS_PACKAGE_NAME, META_MODELS_CLASS_SIMPLE_NAME);
        final TypeElement typeElement = elementUtils.getTypeElement(qualifiedName);

        List<FieldSpec> fieldSpecs = new ArrayList<>();
        
        // if MetaModels.java exists
        if (typeElement != null) { 
            logger.debug("MetaModels exists");

            Set<VariableElement> fields = ElementFinder.findFields(typeElement);
            List<FieldSpec> existingFieldSpecs = fields.stream().map(prop -> {
                String propName = prop.getSimpleName().toString();
                TypeElement propTypeElement = (TypeElement) ((DeclaredType) prop.asType()).asElement();
                String propTypePkgName = elementUtils.getPackageOf(propTypeElement).getQualifiedName().toString();
                ClassName className = ClassName.get(propTypePkgName, ElementFinder.getVariableTypeSimpleName(prop));
                return FieldSpec.builder(className, propName)
                        .initializer("new $T()", className)
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                        .addJavadoc("Previously generated") // debug
                        .build();
            }).toList();
            fieldSpecs.addAll(existingFieldSpecs);
        }

        // for m in metaModels
        for (MetaModelClazz clazz: metaModelClazzes) {
            // if m field already in MetaModels.java - skip
            if (typeElement != null) {
                List<VariableElement> clazzProperties = ElementFinder.findFields(typeElement).stream()
                        .filter(varEl -> 
                            varEl.getSimpleName().toString().equals(clazz.getEntityName()) &&
                            ElementFinder.getVariableTypeSimpleName(varEl).equals(clazz.getMetaModelName()))
                        .toList();
                if (clazzProperties.size() > 0) {
                    continue;
                }
            }

            // write m field to MetaModels
            final ClassName metaModelClassName = ClassName.get(clazz.getMetaModelPkgName(), clazz.getMetaModelName());
            final String propName = clazz.getEntityName();
            fieldSpecs.add(FieldSpec.builder(metaModelClassName, propName)
                    .initializer("new $T()", metaModelClassName)
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .addJavadoc("Newly generated") // debug
                    .build());
        }
        TypeSpec metaModelsTypeSpec = TypeSpec.classBuilder(META_MODELS_CLASS_SIMPLE_NAME)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addFields(fieldSpecs)
                .build();

        JavaFile javaFile = JavaFile.builder(META_MODELS_CLASS_PACKAGE_NAME, metaModelsTypeSpec).indent(INDENT).build();
        javaFile.writeTo(filer);

        logger.info(String.format("Generated %s", metaModelsTypeSpec.name));
    }
    
    private static Set<VariableElement> findEntityAllProperties(TypeElement typeElement) {
        final Set<VariableElement> properties = EntityFinder.findEntityProperties(typeElement);
        final List<String> propertiesNames = properties.stream().map(prop -> prop.getSimpleName().toString()).toList();
        final List<VariableElement> inheritedProperties = EntityFinder.findEntityInheritedProperties(typeElement).stream()
                .filter(inhProp -> {
                    final String inhPropName = inhProp.getSimpleName().toString();
                    return getIncludedInheritedPropertiesNames().contains(inhPropName) &&
                            !(propertiesNames.contains(inhPropName));
                })
                .toList();
        properties.addAll(inheritedProperties);
        return properties;
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


