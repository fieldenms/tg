package ua.com.fielden.platform.processors.verify.verifiers.entity;

import static ua.com.fielden.platform.processors.metamodel.utils.ElementFinder.getSimpleName;
import java.util.List;
import java.util.Optional;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.tools.Diagnostic.Kind;

import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.processors.metamodel.elements.EntityElement;
import ua.com.fielden.platform.processors.metamodel.elements.PropertyElement;
import ua.com.fielden.platform.processors.metamodel.utils.ElementFinder;
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
                if (entityFinder.findDeclaredPropertyAccessor(entity, getSimpleName(property.element())).isEmpty()) {
                    return Optional.of(new ViolatingElement(
                            property.element(), Kind.ERROR, errMissingAccessor(getSimpleName(property.element()))));
                }
                return Optional.empty();
            }
        }

    }

    /**
     * All properties must have a coresponding setter that is either public or protected, and annotated with {@link Observable}.
     */
    static class PropertySetterVerifier extends AbstractEntityVerifier {
        private static final Class<Observable> AT_OBSERVABLE_CLASS = Observable.class;

        protected PropertySetterVerifier(final ProcessingEnvironment processingEnv) {
            super(processingEnv);
        }

        public static final String errMissingSetter(final String propName) {
            return "Missing setter for property [%s].".formatted(propName);
        }

        public static final String errMissingObservable(final String setterName) {
            return "Missing @%s for property setter [%s].".formatted(AT_OBSERVABLE_CLASS.getSimpleName(), setterName);
        }

        public static final String errNotPublicNorProtected(final String setterName) {
            return "Setter [%s] must be declared public or protected".formatted(setterName);
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
                // setter should be declared
                final Optional<ExecutableElement> maybeSetter = entityFinder.findDeclaredPropertySetter(entity, getSimpleName(property.element()));
                if (maybeSetter.isEmpty()) {
                    return Optional.of(new ViolatingElement(
                            property.element(), Kind.ERROR, errMissingSetter(getSimpleName(property.element()))));
                }
                final ExecutableElement setter = maybeSetter.get();

                // should be annotated with @Observable
                if (setter.getAnnotation(AT_OBSERVABLE_CLASS) == null) {
                    return Optional.of(new ViolatingElement(
                            property.element(), Kind.ERROR, errMissingObservable(getSimpleName(setter))));
                }

                // should be public or protected
                if (!ElementFinder.isPublic(setter) && !ElementFinder.isProtected(setter)) {
                    return Optional.of(new ViolatingElement(
                            property.element(), Kind.ERROR, errNotPublicNorProtected(getSimpleName(setter))));
                }

                return Optional.empty();
            }
        }

    }

    /**
     * Collectional properties must be declared final.
     */
    static class CollectionalPropertyVerifier extends AbstractEntityVerifier {
        protected CollectionalPropertyVerifier(final ProcessingEnvironment processingEnv) {
            super(processingEnv);
        }

        public static final String errMustBeFinal(final String propName) {
            return "Collectional property [%s] must be declared final.".formatted(propName);
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
                System.out.println(property);
                if (!entityFinder.isCollectionalProperty(property)) {
                    return Optional.empty();
                }
                if (!EntityFinder.isFinal(property.element())) {
                    return Optional.of(new ViolatingElement(
                            property.element(), Kind.ERROR, errMustBeFinal(getSimpleName(property.element()))));
                }
                return Optional.empty();
            }
        }
    }

}
