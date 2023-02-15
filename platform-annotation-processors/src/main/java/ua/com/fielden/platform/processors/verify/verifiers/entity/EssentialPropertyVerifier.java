package ua.com.fielden.platform.processors.verify.verifiers.entity;

import java.util.List;
import java.util.Optional;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.Diagnostic.Kind;

import ua.com.fielden.platform.processors.metamodel.elements.EntityElement;
import ua.com.fielden.platform.processors.metamodel.elements.PropertyElement;
import ua.com.fielden.platform.processors.metamodel.utils.EntityFinder;
import ua.com.fielden.platform.processors.verify.ViolatingElement;

public class EssentialPropertyVerifier extends AbstractComposableEntityVerifier {

    public EssentialPropertyVerifier(final ProcessingEnvironment processingEnv) {
        super(processingEnv);
    }

    @Override
    protected List<AbstractEntityVerifier> createComponents(final ProcessingEnvironment procEnv) {
        return List.of(new AccessorPresence(procEnv));
    }

    /**
     * All properties must have a coresponding accessor method with a name starting with "get" or "is".
     */
    static class AccessorPresence extends AbstractEntityVerifier {

        public static final String errMissingAccessor(final String propName) {
            return "Missing accessor for property [%s].".formatted(propName);
        }

        protected AccessorPresence(final ProcessingEnvironment processingEnv) {
            super(processingEnv);
        }

        @Override
        protected List<ViolatingElement> verify(final EntityRoundEnvironment roundEnv) {
            return roundEnv.acceptDeclaredPropertiesVisitor(new PropertyVisitor(entityFinder));
        }

        private class PropertyVisitor extends AbstractPropertyVerifyingVisitor {
            public PropertyVisitor(final EntityFinder entityFinder) {
                super(entityFinder);
            }

            @Override
            public Optional<ViolatingElement> visitProperty(final EntityElement entity, final PropertyElement property) {
                if (entityFinder.findDeclaredPropertyAccessor(entity, property.getSimpleName().toString()).isEmpty()) {
                    return Optional.of(new ViolatingElement(property.element(), Kind.ERROR, errMissingAccessor(property.getSimpleName().toString())));
                }
                return Optional.empty();
            }
        }

    }

}
