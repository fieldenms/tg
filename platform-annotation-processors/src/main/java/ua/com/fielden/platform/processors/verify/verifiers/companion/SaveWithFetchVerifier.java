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
import static ua.com.fielden.platform.processors.metamodel.utils.ElementFinder.isSameType;
import static ua.com.fielden.platform.processors.metamodel.utils.ElementFinder.streamDeclaredMethods;

public class SaveWithFetchVerifier extends AbstractCompanionVerifier {

    private static final String ERR_MUST_IMPLEMENT_ISAVE_WITH_FETCH = "[%s] must implement [ISaveWithFetch] to override save-with-fetch.";

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
                    return Optional.of(new ViolatingElement(typeElement, ERROR, ERR_MUST_IMPLEMENT_ISAVE_WITH_FETCH.formatted(typeElement.getSimpleName())));
                }

                return Optional.empty();
            }
        });
    }

}
