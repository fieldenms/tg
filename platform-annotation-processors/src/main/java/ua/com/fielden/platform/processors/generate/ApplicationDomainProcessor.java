package ua.com.fielden.platform.processors.generate;

import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;
import static ua.com.fielden.platform.processors.metamodel.utils.ElementFinder.getSimpleName;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.TypeElement;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;

import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.domain.PlatformDomainTypes;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.processors.AbstractPlatformAnnotationProcessor;
import ua.com.fielden.platform.processors.metamodel.elements.EntityElement;
import ua.com.fielden.platform.processors.metamodel.utils.ElementFinder;
import ua.com.fielden.platform.processors.metamodel.utils.EntityFinder;

@SupportedAnnotationTypes("*")
public class ApplicationDomainProcessor extends AbstractPlatformAnnotationProcessor {

    public static final String APPLICATION_DOMAIN_SIMPLE_NAME = "ApplicationDomain";
    public static final String APPLICATION_DOMAIN_PKG_NAME = "generated.config";
    public static final String APPLICATION_DOMAIN_QUAL_NAME = APPLICATION_DOMAIN_PKG_NAME + "." + APPLICATION_DOMAIN_SIMPLE_NAME;

    private ElementFinder elementFinder;
    private EntityFinder entityFinder;

    @Override
    public synchronized void init(final ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.elementFinder = new ElementFinder(processingEnv.getElementUtils(), processingEnv.getTypeUtils());
        this.entityFinder = new EntityFinder(processingEnv.getElementUtils(), processingEnv.getTypeUtils());
    }

    @Override
    protected boolean processRound(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        final Set<EntityElement> entities = roundEnv.getRootElements().stream()
            .filter(elt -> entityFinder.isEntityType(elt.asType()))
            .map(elt -> entityFinder.newEntityElement((TypeElement) elt))
            .collect(Collectors.toSet());

        if (entities.isEmpty()) {
            return false;
        }

        final Optional<TypeElement> appDomainElt = elementFinder.findTypeElement(APPLICATION_DOMAIN_QUAL_NAME);
        generateApplicationDomain(appDomainElt, entities);

        return false;
    }

    private void generateApplicationDomain(final Optional<TypeElement> appDomainElt, final Set<EntityElement> entities) {
        final Set<EntityElement> registeredEntities;
        if (appDomainElt.isPresent()) {
            registeredEntities = collectRegisteredEntities(appDomainElt.get());
            registeredEntities.addAll(entities);
        } else {
            registeredEntities = entities;
        }

        writeApplicationDomain(registeredEntities);
    }

    private void writeApplicationDomain(final Set<EntityElement> registeredEntities) {
        final ParameterizedTypeName classExtendsAbstractEntity = ParameterizedTypeName.get(
                ClassName.get(Class.class),
                WildcardTypeName.subtypeOf(ParameterizedTypeName.get(
                        ClassName.get(AbstractEntity.class), WildcardTypeName.subtypeOf(Object.class))));

        // fields of the form: public static final Class<? extends AbstractEntity<?>> $ENTITY_SIMPLE_NAME = $ENTITY_TYPE.class;
        final List<FieldSpec> entityTypesStaticFields = registeredEntities.stream()
                    .map(elt -> FieldSpec.builder(classExtendsAbstractEntity, getSimpleName(elt), PUBLIC, STATIC, FINAL)
                            .initializer("$T.class", ClassName.get(elt.asType()))
                            .build())
                    .toList();

        // public class ApplicationDomain implements IApplicationDomainProvider
        final TypeSpec typeSpec = TypeSpec.classBuilder(APPLICATION_DOMAIN_SIMPLE_NAME)
            .addModifiers(PUBLIC)
            .addSuperinterface(IApplicationDomainProvider.class)
            // private static final Set<Class<? extends AbstractEntity<?>>> entityTypes = new LinkedHashSet<>();
            .addField(FieldSpec.builder(
                    ParameterizedTypeName.get(ClassName.get(Set.class), classExtendsAbstractEntity),
                    "entityTypes",
                    PRIVATE, STATIC, FINAL)
                    .initializer("new $T<>()", LinkedHashSet.class)
                    .build())
            // private static final Set<Class<? extends AbstractEntity<?>>> domainTypes = new LinkedHashSet<>();
            .addField(FieldSpec.builder(
                    ParameterizedTypeName.get(ClassName.get(Set.class), classExtendsAbstractEntity),
                    "domainTypes",
                    PRIVATE, STATIC, FINAL)
                    .initializer("new $T<>()", LinkedHashSet.class)
                    .build())
            /*
             * private static void add(final Class<? extends AbstractEntity<?>> domainType) {
             *     entityTypes.add(domainType);
             *     domainTypes.add(domainType);
             * }
             */
            .addMethod(MethodSpec.methodBuilder("add")
                    .addModifiers(PRIVATE, STATIC)
                    .returns(Void.class)
                    .addParameter(classExtendsAbstractEntity, "domainType", FINAL)
                    .addStatement("entityTypes.add(domainType)")
                    .addStatement("domainTypes.add(domainType)")
                    .build())
            // static field for each registered entity type
            .addFields(entityTypesStaticFields)
            /*
             * static {
             *     entityTypes.addAll(PlatformDomainTypes.types);
             *     // for each static field representing a registered entity type:
             *     add($FIELD_NAME);
             * }
             */
            .addStaticBlock(CodeBlock.join(List.of(
                    CodeBlock.of("entityTypes.addAll($T.types);", PlatformDomainTypes.class),
                    CodeBlock.of(entityTypesStaticFields.stream()
                            .map(field -> "add(%s);".formatted(field.name))
                            .collect(Collectors.joining("\n")))),
                    "\n"))
            /*
             * @Override
             * public List<Class<? extends AbstractEntity<?>>> entityTypes() {
             *     return entityTypes.stream().collect(Collectors.toUnmodifiableList());
             * }
             */
            .addMethod(MethodSpec.methodBuilder("entityTypes")
                    .addAnnotation(Override.class)
                    .addModifiers(PUBLIC)
                    .returns(ParameterizedTypeName.get(ClassName.get(List.class), classExtendsAbstractEntity))
                    .addStatement("return entityTypes.stream().collect($T.toUnmodifiableList())", Collectors.class)
                    .build())
            /*
             * public static List<Class<? extends AbstractEntity<?>>> domainTypes() {
             *     return domainTypes.stream().collect(Collectors.toUnmodifiableList());
             * }
             */
            .addMethod(MethodSpec.methodBuilder("domainTypes")
                    .addModifiers(PUBLIC, STATIC)
                    .returns(ParameterizedTypeName.get(ClassName.get(List.class), classExtendsAbstractEntity))
                    .addStatement("return domainTypes.stream().collect($T.toUnmodifiableList())", Collectors.class)
                    .build())
            .build();

        final JavaFile javaFile = JavaFile.builder(APPLICATION_DOMAIN_PKG_NAME, typeSpec).indent("    ").build();
        try {
            javaFile.writeTo(filer);
        } catch (final IOException ex) {
            printError("Failed to generate %s: %s\n%s", APPLICATION_DOMAIN_QUAL_NAME, ex.getMessage(), ExceptionUtils.getStackTrace(ex));
            return;
        }

        printNote("Generated %s.", APPLICATION_DOMAIN_QUAL_NAME);
    }

    private Set<EntityElement> collectRegisteredEntities(final TypeElement typeElement) {
        return Set.of();
    }

}
