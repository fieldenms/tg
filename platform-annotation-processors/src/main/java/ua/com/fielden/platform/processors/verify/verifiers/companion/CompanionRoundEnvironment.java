package ua.com.fielden.platform.processors.verify.verifiers.companion;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.processors.metamodel.utils.ElementFinder;
import ua.com.fielden.platform.processors.verify.AbstractRoundEnvironment;
import ua.com.fielden.platform.processors.verify.AbstractTypeElementVerifier;
import ua.com.fielden.platform.processors.verify.ViolatingElement;

import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static ua.com.fielden.platform.utils.StreamUtils.typeFilter;

/// A round environment wrapper designed to operate on entity companions.
///
public class CompanionRoundEnvironment extends AbstractRoundEnvironment<TypeElement, AbstractTypeElementVerifier> {
    private final ElementFinder elementFinder;

    /// Holds root type elements for entity companions in the current round.
    ///
    private List<TypeElement> companions;

    public CompanionRoundEnvironment(final RoundEnvironment roundEnv, final Messager messager, final ElementFinder elementFinder) {
        super(roundEnv, messager);
        this.elementFinder = elementFinder;
    }

    /// Returns a list of companion elements being processed in the current round.
    /// The result is memoized.
    ///
    public List<TypeElement> listCompanions() {
        if (companions == null) {
            companions = streamRootElements()
                    .filter(elt -> ElementKind.CLASS.equals(elt.getKind()))
                    .mapMulti(typeFilter(TypeElement.class))
                    .filter(typeElt -> elementFinder.isSubtype(typeElt.asType(), IEntityDao.class))
                    .toList();
        }
        return companions;
    }

    @Override
    public List<ViolatingElement> findViolatingElements(final AbstractTypeElementVerifier verifier) {
        final List<ViolatingElement> violators = new ArrayList<>();

        listCompanions().stream()
            .map(verifier::verify)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .forEach(ve -> {
                ve.printMessage(messager);
                violators.add(ve);
            });

        return violators;
    }

}
