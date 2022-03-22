package ua.com.fielden.platform.processors.meta_model;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
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

import ua.com.fielden.platform.annotations.meta_model.GenerateMetaModel;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.utils.Pair;

@AutoService(Processor.class)
@SupportedAnnotationTypes("ua.com.fielden.platform.annotations.meta_model.GenerateMetaModel")
@SupportedSourceVersion(SourceVersion.RELEASE_16)
public class MetaModelProcessor extends AbstractProcessor {

    private static final Class<MetaModel> META_MODEL_SUPERCLASS = MetaModel.class;
    
    private static final String META_MODELS_CLASS_SIMPLE_NAME = "MetaModels";
    private static final String META_MODELS_CLASS_PACKAGE_NAME = "meta_models";
    private static final String META_MODEL_PKG_NAME_SUFFIX = ".meta";
    private static final String META_MODEL_NAME_SUFFIX = "MetaModel";
    private static final String INDENT = "    ";
    
    private static final List<Class<? extends Annotation>> PROP_ANNOTATIONS_IGNORE = new ArrayList<>(List.of(IsProperty.class, Title.class));
    
    private Logger logger;
    private Filer filer;
    private Elements elementUtils;
    
    private class MetaModelClazz {

        private String modelName;
        private String modelPkgName;
        
        MetaModelClazz(String modelName, String modelPkgName) {
            this.modelName = modelName;
            this.modelPkgName = modelPkgName;
        }
        
        public String getModelName() {
            return modelName;
        }

        public void setModelName(String modelName) {
            this.modelName = modelName;
        }

        public String getModelPkgName() {
            return modelPkgName;
        }

        public void setModelPkgName(String modelPkgName) {
            this.modelPkgName = modelPkgName;
        }
        
        public String getMetaModelName() {
            return modelName + META_MODEL_NAME_SUFFIX;
        }
        
        public String getMetaModelPkgName() {
            return modelPkgName + META_MODEL_PKG_NAME_SUFFIX;
        }
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.filer = processingEnv.getFiler();
        this.elementUtils = processingEnv.getElementUtils();
        Configurator.initialize(getConfig());
        this.logger = LogManager.getLogger(MetaModelProcessor.class);

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
    
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        logger.info("=== PROCESSING ROUND START ===");

        List<MetaModelClazz> metaModelClazzes = new ArrayList<>();

        final Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(GenerateMetaModel.class);
        for (Element element: annotatedElements) {
            final String simpleName = ((TypeElement) element).getSimpleName().toString();
            final String pkgName = elementUtils.getPackageOf(element).getQualifiedName().toString();

            final MetaModelClazz metaModelClazz = new MetaModelClazz(simpleName, pkgName);

            final List<VariableElement> properties = findEntityProperties((TypeElement) element);

            writeMetaModel(metaModelClazz, properties);

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

    private void writeMetaModel(final MetaModelClazz metaModelClazz, final List<VariableElement> properties) {
        /* ==========
         * Properties
         * ========== */

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
                public final String [PROP_NAME]; 
            */
            ClassName propTypeMetaModelName = null;
            final Element propTypeAsElement = ((DeclaredType) propType).asElement(); 
            if ((propType.getKind() == TypeKind.DECLARED) && (propTypeAsElement.getAnnotation(GenerateMetaModel.class) != null)) {
                final String propTypeSimpleName = propTypeAsElement.getSimpleName().toString();
                final String propTypePkgName = elementUtils.getPackageOf(propTypeAsElement).getQualifiedName().toString();
                propTypeMetaModelName = ClassName.get(propTypePkgName + META_MODEL_PKG_NAME_SUFFIX, propTypeSimpleName + META_MODEL_NAME_SUFFIX);
                
                fieldSpecBuilder = FieldSpec.builder(propTypeMetaModelName, propName);
            } else {
                fieldSpecBuilder = FieldSpec.builder(String.class, propName);
            }
            
            // javadoc: property title and description
            final Pair<String, String> propTitleAndDesc = getPropTitleAndDesc(prop);
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
            if (propTypeMetaModelName != null) {
                fieldSpecBuilder = fieldSpecBuilder.addJavadoc("Meta-model: {@link $T}\n<p>\n", propTypeMetaModelName);
            }
            
            // javadoc: all annotations of a property (except ignored ones)
            final List<String> annotNames = getFieldAnnotationsExcept(prop, PROP_ANNOTATIONS_IGNORE).stream()
                    .map(a -> String.format("{@link %s}", ((TypeElement) a.getAnnotationType().asElement()).getSimpleName().toString()))
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
        final ClassName modelClassName = ClassName.get(metaModelClazz.getModelPkgName(), metaModelClazz.getModelName());
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
                    this.[PROP_NAME] = new [PROP_TYPE_NAME]MetaModel (joinContext ([PROP_NAME]_) );
                else
                    this.[PROP_NAME] = joinContext([PROP_NAME]_);
        }
        */
        MethodSpec.Builder constructorBuilder =  MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(String.class, "context")
                .addStatement("super(context)");

        CodeBlock.Builder constructorStatementsBuilder = CodeBlock.builder();
        
        for (VariableElement prop: properties) {
            final String propName = prop.getSimpleName().toString();
            final TypeMirror propType = prop.asType();
            
            final Element propTypeAsElement = ((DeclaredType) propType).asElement(); 
            if ((propType.getKind() == TypeKind.DECLARED) && (propTypeAsElement.getAnnotation(GenerateMetaModel.class) != null)) {
                final String propTypeSimpleName = propTypeAsElement.getSimpleName().toString();
                final String propTypePackageName = elementUtils.getPackageOf(propTypeAsElement).getQualifiedName().toString();
                ClassName fieldTypeMetaModelName = ClassName.get(propTypePackageName + META_MODEL_PKG_NAME_SUFFIX, propTypeSimpleName + META_MODEL_NAME_SUFFIX);
                
                constructorStatementsBuilder = constructorStatementsBuilder.addStatement(
                        "this.$L = new $T(joinContext($L_))", 
                        propName, fieldTypeMetaModelName, propName);
            } else {
                constructorStatementsBuilder = constructorStatementsBuilder.addStatement(
                        "this.$L = joinContext($L_)", 
                        propName, propName);
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
        public final class [NAME]MetaModel extends MetaModel {
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

            List<VariableElement> fields = findFields(typeElement);
            List<FieldSpec> existingFieldSpecs = fields.stream().map(prop -> {
                String propName = prop.getSimpleName().toString();
                TypeElement propTypeElement = (TypeElement) ((DeclaredType) prop.asType()).asElement();
                String propTypePkgName = elementUtils.getPackageOf(propTypeElement).getQualifiedName().toString();
                ClassName className = ClassName.get(propTypePkgName, getVariableTypeSimpleName(prop));
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
                List<VariableElement> clazzProperties = findFields(typeElement).stream()
                        .filter(varEl -> 
                            varEl.getSimpleName().toString().equals(clazz.getModelName()) &&
                            getVariableTypeSimpleName(varEl).equals(clazz.getMetaModelName()))
                        .toList();
                if (clazzProperties.size() > 0) {
                    continue;
                }
            }

            // write m field to MetaModels
            final ClassName metaModelClassName = ClassName.get(clazz.getMetaModelPkgName(), clazz.getMetaModelName());
            final String propName = clazz.getModelName();
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
    
    private List<VariableElement> findFields(TypeElement typeElement) {
        List<VariableElement> fields = new ArrayList<>();

        List<VariableElement> enclosedFields = typeElement.getEnclosedElements().stream()
                .filter(el -> el.getKind() == ElementKind.FIELD)
                .map(el -> (VariableElement) el)
                .toList();
        fields.addAll(enclosedFields);
        
        return fields;
    }

    private List<VariableElement> findFieldsAnnotatedWith(TypeElement typeElement, Class<? extends Annotation> annotationClass) {
        return findFields(typeElement).stream()
                .filter(el -> el.getAnnotation(annotationClass) != null)
                .toList();
    }

    private List<VariableElement> findEntityProperties(TypeElement typeElement) {
        return findFieldsAnnotatedWith(typeElement, IsProperty.class);
    }
    
    private List<? extends AnnotationMirror> getFieldAnnotations(VariableElement field) {
        List<AnnotationMirror> annotations = new ArrayList<>();

        // guard against non-fields
        if (field.getKind() != ElementKind.FIELD) {
            return annotations;
        }

        annotations.addAll(field.getAnnotationMirrors());
        
        return annotations;
    }
    
    private List<? extends AnnotationMirror> getFieldAnnotationsExcept(VariableElement field, List<Class<? extends Annotation>> ignoredAnnotationsClasses) {
        List<? extends AnnotationMirror> annotations = getFieldAnnotations(field);

        List<String> ignoredAnnotationNames = ignoredAnnotationsClasses.stream()
                .map(annotClass -> annotClass.getCanonicalName())
                .toList();
        
        return annotations.stream()
                .filter(annotMirror -> {
                    String annotQualifiedName = ((TypeElement) annotMirror.getAnnotationType().asElement()).getQualifiedName().toString();
                    return !ignoredAnnotationNames.contains(annotQualifiedName);
                })
                .toList();
    }
    
    private AnnotationMirror getPropAnnotationMirror(VariableElement prop, Class<? extends Annotation> annotationClass) {
        final String annotClassCanonicalName = annotationClass.getCanonicalName();
        
        for (AnnotationMirror annotMirror: prop.getAnnotationMirrors()) {
            String qualifiedName = ((TypeElement) annotMirror.getAnnotationType().asElement()).getQualifiedName().toString();
            if (qualifiedName.equals(annotClassCanonicalName)) {
                return annotMirror;
            }
        }
        
        return null;
    }
    
    private Pair<String, String> getPropTitleAndDesc(VariableElement prop) {
        AnnotationMirror titleAnnotationMirror = getPropAnnotationMirror(prop, Title.class);
        
        if (titleAnnotationMirror == null) {
            return null;
        }
        
        List<Object> values = titleAnnotationMirror.getElementValues().values().stream()
                .map(v -> v.getValue())
                .toList();

        String title = "";
        String desc = "";
        
        try {
            title = (String) values.get(0);
        } catch (Exception e) {
        }

        try {
            desc = (String) values.get(1);
        } catch (Exception e) {
        }
        
        return Pair.pair(title, desc);
    }
    
    private Pair<String, String> getEntityKeyTitleAndDesc() {
        return null;
    }
    
    private Pair<String, String> getEntityDescTitleAndDesc() {
        return null;
    }
    
    private String getVariableTypeSimpleName(VariableElement varElement) {
        return ((DeclaredType) varElement.asType()).asElement().getSimpleName().toString();
    }
}


