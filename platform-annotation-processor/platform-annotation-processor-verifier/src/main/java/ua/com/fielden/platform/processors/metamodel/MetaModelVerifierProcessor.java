//package ua.com.fielden.platform.processors.metamodel;
//
//import java.io.IOException;
//import java.lang.reflect.InvocationTargetException;
//import java.lang.reflect.Method;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Collection;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import java.util.stream.Collectors;
//
//import javax.annotation.processing.AbstractProcessor;
//import javax.annotation.processing.Filer;
//import javax.annotation.processing.ProcessingEnvironment;
//import javax.annotation.processing.Processor;
//import javax.annotation.processing.RoundEnvironment;
//import javax.annotation.processing.SupportedAnnotationTypes;
//import javax.annotation.processing.SupportedSourceVersion;
//import javax.lang.model.SourceVersion;
//import javax.lang.model.element.Element;
//import javax.lang.model.element.Modifier;
//import javax.lang.model.element.PackageElement;
//import javax.lang.model.element.TypeElement;
//import javax.lang.model.element.VariableElement;
//import javax.lang.model.type.DeclaredType;
//import javax.lang.model.util.Elements;
//import javax.tools.FileObject;
//import javax.tools.StandardLocation;
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
//import com.squareup.javapoet.FieldSpec;
//
//import ua.com.fielden.platform.processors.metamodel.elements.ElementFinder;
//import ua.com.fielden.platform.processors.metamodel.elements.EntityElement;
//import ua.com.fielden.platform.processors.metamodel.elements.MetaModelElement;
//
//@AutoService(Processor.class)
//@SupportedAnnotationTypes("*")
//@SupportedSourceVersion(SourceVersion.RELEASE_16)
//public class MetaModelVerifierProcessor extends AbstractProcessor {
//    
//    private static final String LOG_FILENAME = "verifier-proc.log";
//    private static final String ECLIPSE_OPTION_KEY = "projectdir";
//    protected static final List<TypeElement> INACTIVE_META_MODELS = new ArrayList<>();
//    
//    private ProcessingEnvironment processingEnv;
//    private Logger logger;
//    private ProcessorLogger procLogger;
//    private Map<String, String> options;
//    private boolean fromMaven;
//    private Elements elementUtils;
//    private Filer filer;
//    private int roundCount;
//
//    static {
//        System.out.println(String.format("%s class loaded.", MetaModelVerifierProcessor.class.getSimpleName()));
//    }
//    
//    @Override
//    public synchronized void init(ProcessingEnvironment processingEnv) {
//        super.init(processingEnv);
//        this.processingEnv = processingEnv;
//        this.elementUtils = processingEnv.getElementUtils();
//        this.filer = processingEnv.getFiler();
//        this.options = processingEnv.getOptions();
//        this.roundCount = 0;
//
//        // processor started from Eclipse?
//        final String projectDir = options.get(ECLIPSE_OPTION_KEY);
//        this.fromMaven = projectDir == null;
//        
//        INACTIVE_META_MODELS.clear();
//
//        // log4j configuration
//        Configurator.initialize(getLog4jConfig());
//        this.logger = LogManager.getLogger(this.getClass());
//
//        // ProcessorLogger
//        String logFilename = this.fromMaven ? LOG_FILENAME : projectDir + "/" + LOG_FILENAME;
//        String source = this.fromMaven ? "mvn" : "Eclipse";
//        this.procLogger = new ProcessorLogger(logFilename, source, logger);
//        procLogger.ln();
//        procLogger.info(String.format("%s initialized.", this.getClass().getSimpleName()));
//
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
//    private static String getEntitySimpleName(final String metaModelSimpleName) {
//        int index = metaModelSimpleName.lastIndexOf(MetaModelConstants.META_MODEL_NAME_SUFFIX);
//
//        if (index == -1) {
//            return null;
//        }
//
//        return metaModelSimpleName.substring(0, index);
//    }
//
//    private static String getEntityPackageName(final String metaModelPackageName) {
//        int index = metaModelPackageName.lastIndexOf(MetaModelConstants.META_MODEL_PKG_NAME_SUFFIX);
//
//        if (index == -1) {
//            return null;
//        }
//
//        return metaModelPackageName.substring(0, index);
//    }
//
//    private static String getEntityQualifiedName(final String metaModelQualName) {
//        final int lastDot = metaModelQualName.lastIndexOf('.');
//        final String metaModelSimpleName = metaModelQualName.substring(lastDot + 1);
//        final String metaModelPackageName = metaModelQualName.substring(0, lastDot);
//        final String entitySimpleName = getEntitySimpleName(metaModelSimpleName);
//        final String entityPackageName = getEntityPackageName(metaModelPackageName);
//
//        return String.format("%s.%s", entityPackageName, entitySimpleName);
//    }
//
//    @Override
//    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
//        if (roundCount > 0) {
//            // the log file is closed after the 1st (0) processing round
//            // this should be modified in case more than 1 processing round is needed
//            procLogger.end();
//            return false;
//        }
//
//        procLogger.debug(String.format("=== PROCESSING ROUND %d START ===", roundCount));
//
//        procLogger.debug("annotations: " + String.join(", ", annotations.stream().map(Element::getSimpleName).toList()));
//        procLogger.debug("rootElements: " + String.join(", ", roundEnv.getRootElements().stream().map(Element::getSimpleName).toList()));
//
//        final TypeElement metaModels = elementUtils.getTypeElement(MetaModelConstants.METAMODELS_CLASS_QUAL_NAME);
//
//        // verify
//        List<TypeElement> inactiveMetaModels = new ArrayList<>();
//        if (metaModels != null)
//            inactiveMetaModels.addAll(verify(metaModels));
//        else
//            procLogger.debug(String.format("%s class NOT found.", MetaModelConstants.METAMODELS_CLASS_SIMPLE_NAME));
//            
//        // generate
//        generate(inactiveMetaModels, annotations, roundEnv);
//
//        procLogger.debug(String.format("xxx PROCESSING ROUND %d END xxx", roundCount));
//        roundCount++;
//
//        // return false in order to prevent claiming all annotations ("*")
//        return false;
//    }
//    
//    private void generate(List<TypeElement> inactiveMetaModels, Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
//        MetaModelProcessor generator = getGenerator();
//        if (generator != null) {
//            generator.setInactive(inactiveMetaModels);
//            generator.process(annotations, roundEnv);
//        } else
//            procLogger.debug("generator = null");
//    }
//
//    private List<TypeElement> verify(TypeElement metaModels) {
//        procLogger.debug(String.format("Verifying %s.", metaModels.getSimpleName()));
//
//        final List<TypeElement> inactive = new ArrayList<>();
//        // for each meta-model in MetaModels
//        // if entity doesnt exist for given meta-model
//        // delete the meta-model and delete the field
//        // else
//        // if entity should not be metamodeled anymore
//        // delete the meta-model and delete the field
//
//        final Set<VariableElement> originalFields = ElementFinder.findDeclaredFields(metaModels);
//        for (VariableElement field: originalFields) {
//            // fieldType is a meta-model type
//            final TypeElement fieldType = (TypeElement) ((DeclaredType) field.asType()).asElement();
//            final TypeElement entity = getEntityFromMetaModel(fieldType);
//            procLogger.debug(String.format("Entity %s for meta-model %s", entity, fieldType.getSimpleName()));
//
//            // debug
//            if (entity == null)
//                procLogger.debug(String.format("Entity for %s does not exist", fieldType.getSimpleName()));
//
//            if (entity == null || !MetaModelConstants.isMetamodeled(entity)) {
//                // debug
//                if (entity != null)
//                    procLogger.debug(String.format("Entity %s should no longer be metamodeled", entity.getSimpleName()));
//
//                // do not keep this field and delete the meta-model
//                inactive.add(fieldType);
//                procLogger.debug(String.format("Immitating deletion of %s.", fieldType.getSimpleName()));
////                final boolean deleted = deleteSource(fieldType);
////                if (deleted)
////                    procLogger.debug(String.format("Deleted %s.", fieldType.getSimpleName()));
//            }
//        }
//
//        procLogger.debug("Inactive meta-models: " + String.join(", ", Arrays.toString(inactive.stream().map(TypeElement::getSimpleName).toArray())));
//
////        Set<MetaModelElement> metaModelElements = originalFields.stream()
////            .filter(field -> !INACTIVE_META_MODELS.contains((TypeElement) ((DeclaredType) field.asType()).asElement()))
////            .map(field -> {
////                final TypeElement fieldType = (TypeElement) ((DeclaredType) field.asType()).asElement();
////                final TypeElement entity = getEntityFromMetaModel(fieldType);
////                final EntityElement entityElement = new EntityElement(entity, elementUtils);
////                final MetaModelElement metaModelElement = new MetaModelElement(entityElement, MetaModelConstants.META_MODEL_NAME_SUFFIX, MetaModelConstants.META_MODEL_PKG_NAME_SUFFIX);
////                return metaModelElement;
////            })
////            .collect(Collectors.toSet());
//
//        return inactive;
//    }
//
//    private MetaModelProcessor getGenerator() {
//        Class generatorClass = null;
//        try {
//            generatorClass = this.getClass().getClassLoader().loadClass(MetaModelProcessor.class.getCanonicalName());
//        } catch (ClassNotFoundException e) {
//            procLogger.error(e.toString());
//            return null;
//        }
//
//        MetaModelProcessor generator = null;
//        if (generatorClass != null) {
////            procLogger.debug(String.join("\n", List.of(generatorClass.getMethods()).stream().map(Method::toString).toList()));
//            try {
//                generator = (MetaModelProcessor) generatorClass.getMethod("create").invoke(null);
//            } catch (Exception e) {
//                procLogger.error(e.toString());
//                return null;
//            }
//
//            if (generator != null) {
//                generator.init(processingEnv);
//            }
//        }
//
//        return generator;
//    }
//
//    private TypeElement getEntityFromMetaModel(final TypeElement metaModel) {
//        // TODO handle meta-models that had been already deleted at this point
//        //elementUtils.getPackageOf(metaModel);
//        String entityQualName = getEntityQualifiedName(metaModel.getQualifiedName().toString());
//        return elementUtils.getTypeElement(entityQualName);
//    }
//
//    private boolean deleteSource(TypeElement typeElement) {
//        String sourceFilename = typeElement.getSimpleName().toString() + ".java";
//        PackageElement packageElement = elementUtils.getPackageOf(typeElement);
//
//        FileObject fo = null;
//        try {
//            fo = filer.getResource(StandardLocation.SOURCE_OUTPUT, packageElement.getQualifiedName().toString(), sourceFilename);
//        } catch (IOException e) {
//            procLogger.error(e.toString());
//            return false;
//        }
//
//
//        try {
//            Files.delete(Path.of(fo.toUri()));
//        } catch (IOException e) {
//            procLogger.error(e.toString());
//            return false;
//        }
//
//        // if the owning package has only 1 element, then remove the package too
//        if (packageElement.getEnclosedElements().size() == 1) {
//            final String path = fo.toUri().getPath();
//            final String packagePath = path.substring(0, path.lastIndexOf('/'));
//            try {
//                Files.delete(Path.of(packagePath));
//            } catch (IOException e) {
//                procLogger.error(e.toString());
//                // dont return false, since the source was deleted, an empty package may remain
//            }
//        }
//
//        // file was successfully deleted
//        return true;
//    }
//
//    private Configuration getLog4jConfig() {
//        ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder();
//
//        AppenderComponentBuilder console = builder.newAppender("ConsoleAppender", "Console"); 
//
//        String projectDir = options.get("projectdir");
//        String filename = "verifier.log";
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
//}
