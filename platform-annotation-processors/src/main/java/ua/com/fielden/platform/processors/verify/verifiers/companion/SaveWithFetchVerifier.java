package ua.com.fielden.platform.processors.verify.verifiers.companion;

import ua.com.fielden.platform.companion.ISaveWithFetch;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.processors.verify.AbstractTypeElementVerifier;
import ua.com.fielden.platform.processors.verify.ViolatingElement;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import java.util.List;
import java.util.Optional;

import static javax.tools.Diagnostic.Kind.ERROR;
import static javax.tools.Diagnostic.Kind.MANDATORY_WARNING;
import static ua.com.fielden.platform.processors.metamodel.utils.ElementFinder.isSameType;
import static ua.com.fielden.platform.processors.metamodel.utils.ElementFinder.streamDeclaredMethods;

public class SaveWithFetchVerifier extends AbstractCompanionVerifier {

    public SaveWithFetchVerifier(final ProcessingEnvironment processingEnv) {
        super(processingEnv);
    }

    @Override
    protected List<ViolatingElement> verify(final CompanionRoundEnvironment roundEnv) {
        return roundEnv.findViolatingElements(new AbstractTypeElementVerifier() {
            @Override
            public Optional<ViolatingElement> verify(final TypeElement typeElement) {
                final boolean overridesSaveWithFetch = streamDeclaredMethods(typeElement)
                        .anyMatch(execElt -> execElt.getSimpleName().contentEquals("save")
                                             && execElt.getParameters().size() == 2
                                             && isSameType(execElt.getParameters().get(1).asType(), Optional.class)
                                             && elementFinder.isSubtype(execElt.getParameters().getFirst().asType(), AbstractEntity.class));

                if (overridesSaveWithFetch && !elementFinder.isSubtype(typeElement.asType(), ISaveWithFetch.class)) {
                    return Optional.of(new ViolatingElement(typeElement, ERROR, errMustImplementSaveWithFetch(typeElement.getSimpleName())));
                }


                final var maybeOverriddenSave = streamDeclaredMethods(typeElement)
                        .filter(execElt -> execElt.getSimpleName().contentEquals("save")
                                           && execElt.getParameters().size() == 1
                                           && elementFinder.isSubtype(execElt.getParameters().getFirst().asType(), AbstractEntity.class))
                        .findAny();

                if (maybeOverriddenSave.isPresent()) {
                    if (overridesSaveWithFetch) {
                        return Optional.of(new ViolatingElement(maybeOverriddenSave.get(), ERROR, errMustNotOverrideBothSaves()));
                    }
                    else {
                        return Optional.of(new ViolatingElement(maybeOverriddenSave.get(), MANDATORY_WARNING, warnShouldMigrateToSaveWithFetch()));
                    }
                }

                return Optional.empty();
            }
        });
    }

    private static String errMustImplementSaveWithFetch(final CharSequence coName) {
        return "[%s] must implement [%s] to override save-with-fetch.".formatted(coName, ISaveWithFetch.class.getSimpleName());
    }

    private static String errMustNotOverrideBothSaves() {
        return """
               Method save(AbstractEntity) may be overridden only if you know what you are doing. \
               The primary save method is save-with-fetch, and its signature is save(AbstractEntity, Optional<fetch>).""";
    }

    private static String warnShouldMigrateToSaveWithFetch() {
        return """
               This implementation of save(AbstractEntity) should be replaced with save-with-fetch. \
               Please override save(AbstractEntity, Optional<fetch>) instead.""";
    }

}
