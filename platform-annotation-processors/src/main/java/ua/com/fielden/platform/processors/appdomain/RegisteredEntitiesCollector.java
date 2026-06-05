package ua.com.fielden.platform.processors.appdomain;

import ua.com.fielden.platform.processors.appdomain.annotation.ExtendApplicationDomain;
import ua.com.fielden.platform.processors.metamodel.elements.EntityElement;
import ua.com.fielden.platform.processors.metamodel.utils.ElementFinder;
import ua.com.fielden.platform.processors.metamodel.utils.EntityFinder;
import ua.com.fielden.platform.utils.CollectionUtil;
import ua.com.fielden.platform.utils.Pair;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ErrorType;
import javax.tools.Diagnostic;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;
import static javax.tools.Diagnostic.Kind.NOTE;
import static ua.com.fielden.platform.processors.ProcessorOptionDescriptor.parseOptionFrom;
import static ua.com.fielden.platform.processors.appdomain.EntityRegistrationUtils.isRegisterable;
import static ua.com.fielden.platform.processors.metamodel.utils.ElementFinder.TYPE_ELEMENT_FILTER;
import static ua.com.fielden.platform.processors.metamodel.utils.ElementFinder.isGeneric;

public final class RegisteredEntitiesCollector {

    private final ProcessingEnvironment procEnv;
    private final EntityFinder entityFinder;
    private final ElementFinder elementFinder;
    private Optional<Messager> maybeMessager;

    public static RegisteredEntitiesCollector getInstance(final ProcessingEnvironment procEnv) {
        // NOTE potentially optimise by caching instances and results of methods that take inputs from a round environment
        return new RegisteredEntitiesCollector(procEnv);
    }

    private RegisteredEntitiesCollector(final ProcessingEnvironment procEnv) {
        this.procEnv = procEnv;
        this.maybeMessager = Optional.of(procEnv.getMessager());
        this.entityFinder = new EntityFinder(procEnv);
        this.elementFinder = new ElementFinder(procEnv);
    }

    public void withSuppressedMessages(Consumer<RegisteredEntitiesCollector> fn) {
        final Optional<Messager> store = maybeMessager;
        maybeMessager = Optional.empty();
        fn.accept(this);
        maybeMessager = store;
    }

    /**
     * Collects all registered entities from the given round's point of view. The resulting information is complete:
     * it combines the round inputs with the existing state of the world.
     */
    public void collectRegisteredEntities(final RoundEnvironment roundEnv,
                                          final Consumer<? super EntityElement> entityCollector,
                                          final Consumer<? super EntityElement> externalEntityCollector)
    {
        final Optional<ApplicationDomainElement> maybeAppDomainElt = findApplicationDomainInRound(roundEnv)
                .map(elt -> new ApplicationDomainElement(elt, entityFinder))
                .or(this::findApplicationDomain);
        final Pair<List<EntityElement>, Optional<ExtendApplicationDomainMirror>> roundInputs = scanRoundInputs(roundEnv);
        final boolean merged = mergeRegisteredEntities(roundInputs.getKey(), roundInputs.getValue(), maybeAppDomainElt, externalEntityCollector, entityCollector);
        if (!merged) {
            final ApplicationDomainElement appDomainElt= maybeAppDomainElt.get();
            appDomainElt.entities().forEach(entityCollector);
            appDomainElt.externalEntities().forEach(externalEntityCollector);
        }
    }

    /**
     * Collects all registered entities by merging inputs of a given round with the existing state of the world.
     *
     * @param roundEntities  entity elements that were found among root elements of a given round
     * @param maybeRoundExtension  {@code ApplicationDomain} extension that was found among root elements of a given round
     * @param maybeAppDomainElt  {@code ApplicationDomain} element, either found among root elements of a given round,
     *                           or in the global processing environment
     * @param entityCollector  called for each discovered registered entity, whether it is used depends on the return type
     * @param externalEntityCollector  called for each discovered external registered entity, whether it is used depends on the return type
     *
     * @return {@code true} if merging was performed (i.e., there was meaningful information in round inputs),
     * {@code false} if there was no need for merging (collector arguments will not have been used in this case)
     */
    public boolean mergeRegisteredEntities(final Collection<EntityElement> roundEntities,
                                           final Optional<ExtendApplicationDomainMirror> maybeRoundExtension,
                                           final Optional<ApplicationDomainElement> maybeAppDomainElt,
                                           final Consumer<? super EntityElement> entityCollector,
                                           final Consumer<? super EntityElement> externalEntityCollector)
    {
        return ifEmptyOrMap(maybeAppDomainElt, () -> {
            roundEntities.stream().filter(EntityRegistrationUtils::isRegisterable).forEach(entityCollector);
            maybeRoundExtension.or(this::findApplicationDomainExtension)
                    .map(mirr -> mirr.streamEntityElements(entityFinder)).orElseGet(Stream::empty)
                    .forEach(externalEntityCollector);
            return true;
        }, appDomainElt -> {
            final Set<EntityElement> toUnregister = new HashSet<>();
            final Set<EntityElement> toRegister = new HashSet<>();

            // without external ones
            final Set<EntityElement> currentRegisteredEntities = appDomainElt.entities();

            // analyse input entities
            // * input entities -- are there any new ones we need to register?
            toRegister.addAll(roundEntities.stream()
                    .peek(this::warnIfGeneric)
                    .filter(ent -> isRegisterable(ent) && !currentRegisteredEntities.contains(ent))
                    .toList());
            // * unregisterable entities -- were any of them registered? (we need to unregister them)
            toUnregister.addAll(roundEntities.stream()
                    .filter(ent -> currentRegisteredEntities.contains(ent) && !isRegisterable(ent))
                    .toList());

            // analyse the input extension if it exists
            final Set<EntityElement> currentExternalRegisteredEntities = appDomainElt.externalEntities();

            final Set<EntityElement> externalEntitiesFromExtension = maybeRoundExtension.map(mirr -> mirr.streamEntityElements(entityFinder).collect(toSet()))
                    .orElseGet(Set::of);
            // NOTE: we assume that external entities are always registerable entities, thus don't perform additional checks
            // * are there any new external entities we need to register?
            final List<EntityElement> externalToRegister = externalEntitiesFromExtension.stream()
                    .filter(ent -> !currentExternalRegisteredEntities.contains(ent))
                    .toList();
            // * are there any external entities that need to be unregistered?
            // only if an extension has been modified
            final List<EntityElement> externalToUnregister = maybeRoundExtension.isEmpty() ? List.of() :
                    currentExternalRegisteredEntities.stream()
                            .filter(ent -> !externalEntitiesFromExtension.contains(ent))
                            .toList();

            // analyse ApplicationDomain
            // are there any missing entity types (e.g., due to removal)?
            final List<ErrorType> missingRegisteredTypes = appDomainElt.errorTypes();

            maybeMessager.ifPresent(messager -> {
                if (!toRegister.isEmpty()) {
                    messager.printMessage(NOTE, "Registering (%s): [%s]".formatted(
                            toRegister.size(),
                            CollectionUtil.toString(toRegister.stream().map(EntityElement::getSimpleName).toList(), ", ")));
                }
                if (!externalToRegister.isEmpty()) {
                    messager.printMessage(NOTE, "Registering external (%s): [%s]".formatted(
                            externalToRegister.size(),
                            CollectionUtil.toString(externalToRegister.stream().map(EntityElement::getSimpleName).toList(), ", ")));
                }
                if (!toUnregister.isEmpty()) {
                    messager.printMessage(NOTE, "Unregistering (%s): [%s]".formatted(
                            toUnregister.size(), CollectionUtil.toString(toUnregister.stream().map(EntityElement::getSimpleName).toList(), ", ")));
                }
                if (!externalToUnregister.isEmpty()) {
                    messager.printMessage(NOTE, "Unregistering external (%s): [%s]".formatted(
                            externalToUnregister.size(),
                            CollectionUtil.toString(externalToUnregister.stream().map(EntityElement::getSimpleName).toList(), ", ")));
                }
                if (!missingRegisteredTypes.isEmpty()) {
                    messager.printMessage(NOTE, "Missing (%s): [%s]".formatted(
                            missingRegisteredTypes.size(), CollectionUtil.toString(missingRegisteredTypes, ", ")));
                }
            });

            // consider if we actually need to merge
            if (!missingRegisteredTypes.isEmpty() || !toUnregister.isEmpty() || !externalToUnregister.isEmpty() // anything to exclude?
                    || !toRegister.isEmpty() || !externalToRegister.isEmpty()) {  // anything to include?
                final Set<EntityElement> registeredEntities = new HashSet<>(appDomainElt.entities());
                registeredEntities.removeAll(toUnregister);
                registeredEntities.addAll(toRegister);
                registeredEntities.forEach(entityCollector);

                final Set<EntityElement> externalRegisteredEntities = new HashSet<>(appDomainElt.externalEntities());
                externalRegisteredEntities.removeAll(externalToUnregister);
                externalRegisteredEntities.addAll(externalToRegister);
                externalRegisteredEntities.forEach(externalEntityCollector);

                return true;
            } else {
                return false;
            }
        });
    }

    /**
     * Scans inputs of a given round for entity elements and an extension of {@code ApplicationDomain}.
     */
    public Pair<List<EntityElement>, Optional<ExtendApplicationDomainMirror>> scanRoundInputs(final RoundEnvironment roundEnv) {
        final List<EntityElement> entities = roundEnv.getRootElements().stream()
                .filter(elt -> entityFinder.isEntityType(elt.asType()))
                .map(elt -> entityFinder.newEntityElement((TypeElement) elt))
                .toList();
        final Optional<ExtendApplicationDomainMirror> maybeExtension = findApplicationDomainExtensionInRound(roundEnv);
        return Pair.pair(entities, maybeExtension);
    }

    public Optional<ApplicationDomainElement> findApplicationDomainInRound(final RoundEnvironment roundEnv) {
        final String appDomainFqn = ApplicationDomainProcessor.getApplicationDomainFqn(procEnv.getOptions());
        return roundEnv.getRootElements().stream()
                .mapMulti(TYPE_ELEMENT_FILTER)
                .filter(elt -> elt.getQualifiedName().contentEquals(appDomainFqn))
                .findFirst()
                .map(elt -> new ApplicationDomainElement(elt, entityFinder));
    }

    /**
     * Finds an {@code ApplicationDomain} extension among round inputs.
     * Missing annotation {@link ExtendApplicationDomain} is interpreted as if it's present but initialised with empty/default values.
     */
    public Optional<ExtendApplicationDomainMirror> findApplicationDomainExtensionInRound(final RoundEnvironment roundEnv) {
        final String fqn = parseOptionFrom(procEnv.getOptions(), ApplicationDomainProcessor.APP_DOMAIN_EXTENSION_OPT_DESC);
        return roundEnv.getRootElements().stream()
                .mapMulti(TYPE_ELEMENT_FILTER)
                .filter(elt -> elt.getQualifiedName().contentEquals(fqn))
                .findFirst()
                .map(elt -> ExtendApplicationDomainMirror.fromAnnotatedOrEmpty(elt, elementFinder));
    }

    public Optional<ApplicationDomainElement> findApplicationDomain() {
        final String appDomainFqn = ApplicationDomainProcessor.getApplicationDomainFqn(procEnv.getOptions());
        return entityFinder.findTypeElement(appDomainFqn).map(elt -> new ApplicationDomainElement(elt, entityFinder));
    }

    /**
     * Finds an {@code ApplicationDomain} extension in the global processing environment.
     * Missing annotation {@link ExtendApplicationDomain} is interpreted as if it's present but initialised with empty/default values.
     */
    public Optional<ExtendApplicationDomainMirror> findApplicationDomainExtension() {
        final String fqn = parseOptionFrom(procEnv.getOptions(), ApplicationDomainProcessor.APP_DOMAIN_EXTENSION_OPT_DESC);
        return elementFinder.findTypeElement(fqn)
                .map(elt -> ExtendApplicationDomainMirror.fromAnnotatedOrEmpty(elt, elementFinder));
    }

    private void warnIfGeneric(EntityElement entity) {
        maybeMessager.ifPresent(m -> {
            if (isGeneric(entity)) {
                m.printMessage(Diagnostic.Kind.WARNING,
                        "Entity %s won't be registered because it's a generic type.".formatted(entity.getQualifiedName()));
            }
        });
    }

    private static <T, R> R ifEmptyOrMap(Optional<T> optional, Supplier<? extends R> ifEmpty, Function<? super T, R> mapper) {
        return optional.isEmpty() ? ifEmpty.get() : mapper.apply(optional.get());
    }

}
