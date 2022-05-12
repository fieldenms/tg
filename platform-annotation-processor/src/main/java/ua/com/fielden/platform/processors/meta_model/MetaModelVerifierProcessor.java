package ua.com.fielden.platform.processors.meta_model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.Elements;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

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

@AutoService(Processor.class)
@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_16)
public class MetaModelVerifierProcessor extends AbstractProcessor {
    
    private static final String LOG_FILENAME = "verifier.log.1";
    private static final String ECLIPSE_OPTION_KEY = "projectdir";
    protected static final List<TypeElement> INACTIVE_META_MODELS = new ArrayList<>();
    
    private Logger logger;
    private ProcessorLogger procLogger;
    private Map<String, String> options;
    private boolean fromMaven;
    private Elements elementUtils;
    private Filer filer;
    private int roundCount;
    
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.elementUtils = processingEnv.getElementUtils();
        this.filer = processingEnv.getFiler();
        this.options = processingEnv.getOptions();
        this.roundCount = 0;

        // processor started from Eclipse?
        final String projectDir = options.get(ECLIPSE_OPTION_KEY);
        this.fromMaven = projectDir == null;
        
        INACTIVE_META_MODELS.clear();

        // log4j configuration
        Configurator.initialize(getLog4jConfig());
        this.logger = LogManager.getLogger(this.getClass());

        // ProcessorLogger
        String logFilename = this.fromMaven ? LOG_FILENAME : projectDir + "/" + LOG_FILENAME;
        String source = this.fromMaven ? "mvn" : "Eclipse";
        this.procLogger = new ProcessorLogger(logFilename, source, logger);
        procLogger.ln();
        procLogger.info(String.format("%s initialized.", this.getClass().getSimpleName()));

        if (Files.exists(Path.of(projectDir + "/target/generated-sources"))) {
            try {
                procLogger.debug("target/generated-sources: " + String.join(", ", Files.walk(Path.of(projectDir + "/target/generated-sources"))
                        .filter(Files::isRegularFile)
                        .map(p -> p.getFileName().toString().split("\\.java")[0])
                        .toList()));
            } catch (IOException e) {
                procLogger.error(e.toString());
            }
        }
    }

    private static String getEntitySimpleName(final String metaModelSimpleName) {
        int index = metaModelSimpleName.lastIndexOf(MetaModelProcessor.META_MODEL_NAME_SUFFIX);

        if (index == -1) {
            return null;
        }

        return metaModelSimpleName.substring(0, index);
    }

    private static String getEntityPackageName(final String metaModelPackageName) {
        int index = metaModelPackageName.lastIndexOf(MetaModelProcessor.META_MODEL_PKG_NAME_SUFFIX);

        if (index == -1) {
            return null;
        }

        return metaModelPackageName.substring(0, index);
    }

    private static String getEntityQualifiedName(final String metaModelQualName) {
        int lastDot = metaModelQualName.lastIndexOf('.');
        String metaModelSimpleName = metaModelQualName.substring(lastDot + 1);
        String metaModelPackageName = metaModelQualName.substring(0, lastDot);
        String entitySimpleName = getEntitySimpleName(metaModelSimpleName);
        String entityPackageName = getEntityPackageName(metaModelPackageName);

        return String.format("%s.%s", entityPackageName, entitySimpleName);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundCount > 0) {
            // the log file is closed after the 1st (0) processing round
            // this should be modified in case more than 1 processing round is needed
            procLogger.end();
            return false;
        }

        procLogger.debug(String.format("=== PROCESSING ROUND %d START ===", roundCount));

        final TypeElement metaModels = elementUtils.getTypeElement(MetaModelProcessor.METAMODELS_CLASS_QUAL_NAME);

        // if MetaModels class does not exist, then do nothing
        if (metaModels == null) {
            procLogger.debug(String.format("%s class does not exist. Exiting.", MetaModelProcessor.METAMODELS_CLASS_SIMPLE_NAME));
            procLogger.debug(String.format("xxx PROCESSING ROUND %d END xxx", roundCount));
            roundCount++;
            return false;
        }

        // for each meta-model in MetaModels
        // if entity doesnt exist for given meta-model
        // delete the meta-model and delete the field
        // else
        // if entity should not be metamodeled anymore
        // delete the meta-model and delete the field

        final Set<VariableElement> originalFields = ElementFinder.findDeclaredFields(metaModels);
        for (VariableElement field: originalFields) {
            // fieldType is a meta-model type
            final TypeElement fieldType = (TypeElement) ((DeclaredType) field.asType()).asElement();
            // the field should be preserved (collected) only if the underlying entity exists AND that entity should still be metamodeled
            final TypeElement entity = getEntityFromMetaModel(fieldType);

            // debug
            if (entity == null)
                procLogger.debug(String.format("Entity for %s does not exist", fieldType.getSimpleName()));

            if (entity == null || !MetaModelProcessor.isMetamodeled(entity)) {
                // debug
                if (entity != null)
                    procLogger.debug(String.format("Entity %s should no longer be metamodeled", entity.getSimpleName()));

                // do not keep this field and delete the meta-model
                procLogger.debug(String.format("Inactive meta-model: %s", fieldType.getSimpleName()));
                INACTIVE_META_MODELS.add(fieldType);
                final boolean deleted = deleteSource(fieldType);
                if (deleted)
                    procLogger.debug(String.format("Deleted %s.", fieldType.getSimpleName()));
            }
        }

        procLogger.debug(String.format("xxx PROCESSING ROUND %d END xxx", roundCount));
        roundCount++;

        // return false in order to prevent claiming all annotations ("*")
        return false;
    }

    private TypeElement getEntityFromMetaModel(final TypeElement metaModel) {
        String entityQualName = getEntityQualifiedName(metaModel.getQualifiedName().toString());
        return elementUtils.getTypeElement(entityQualName);
    }

    private boolean deleteSource(TypeElement typeElement) {
        String sourceFilename = typeElement.getSimpleName().toString() + ".java";
        PackageElement packageElement = elementUtils.getPackageOf(typeElement);

        FileObject fo = null;
        try {
            fo = filer.getResource(StandardLocation.SOURCE_OUTPUT, packageElement.getQualifiedName().toString(), sourceFilename);
        } catch (IOException e) {
            procLogger.error(e.toString());
            return false;
        }


        try {
            Files.delete(Path.of(fo.toUri()));
        } catch (IOException e) {
            procLogger.error(e.toString());
            return false;
        }

        // if the owning package has only 1 element, then remove the package too
        if (packageElement.getEnclosedElements().size() == 1) {
            final String path = fo.toUri().getPath();
            final String packagePath = path.substring(0, path.lastIndexOf('/'));
            try {
                Files.delete(Path.of(packagePath));
            } catch (IOException e) {
                procLogger.error(e.toString());
                // dont return false, since the source was deleted, an empty package may remain
            }
        }

        // file was successfully deleted
        return true;
    }

    private Configuration getLog4jConfig() {
        ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder();

        AppenderComponentBuilder console = builder.newAppender("ConsoleAppender", "Console"); 

        String projectDir = options.get("projectdir");
        String filename = "verifier.log";
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
