package ua.com.fielden.platform.processors.verify;

import javax.lang.model.element.TypeElement;
import java.util.Optional;

/// A base type for verifiers that verify instances of [TypeElement].
///
public abstract class AbstractTypeElementVerifier implements IElementVerifier<TypeElement> {

    @Override
    public abstract Optional<ViolatingElement> verify(final TypeElement typeElement);

}
