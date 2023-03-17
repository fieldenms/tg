package ua.com.fielden.platform.processors.generate;

import java.util.Optional;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.TypeElement;

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
    }

    private Set<EntityElement> collectRegisteredEntities(final TypeElement typeElement) {
        return Set.of();
    }

}
