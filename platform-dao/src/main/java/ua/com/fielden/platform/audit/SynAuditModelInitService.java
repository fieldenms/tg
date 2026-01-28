package ua.com.fielden.platform.audit;

import com.google.inject.Injector;
import jakarta.inject.Inject;
import ua.com.fielden.platform.entity.exceptions.EntityDefinitionException;
import ua.com.fielden.platform.eql.meta.ISyntheticModelProvider;
import ua.com.fielden.platform.meta.IDomainMetadataUtils;
import ua.com.fielden.platform.reflection.Reflector;

import java.util.List;

import static java.lang.String.format;
import static java.lang.reflect.Modifier.isFinal;
import static ua.com.fielden.platform.audit.AuditUtils.isSynAuditEntityType;
import static ua.com.fielden.platform.audit.AuditUtils.isSynAuditPropEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.findSyntheticModelFieldFor;

/**
 * Performs initialisation of synthetic audit-entity types.
 * For each synthetic audit-entity type, generates its EQL model and assigns it to the static field {@code models_} that must be declared by that type.
 * <p>
 * The purpose of this process is to support code where {@link ISyntheticModelProvider} cannot be used.
 * Such code accesses synthetic models through static fields of corresponding entity types.
 * <p>
 * Launched by the IoC framework upon creating an {@link Injector}.
 * <p>
 * Effectful only if auditing is enabled.
 */
public final class SynAuditModelInitService {

    private static final String ERR_MISSING_MODELS_FIELD =
            "Synthetic audit type [%s] must declare static non-final field [models_] with type List<EntityResultQueryModel>.";

    @Inject
    static void start(
            final IDomainMetadataUtils domainMetadataUtils,
            final ISyntheticModelProvider synModelProvider,
            final AuditingMode auditingMode)
    {
        switch (auditingMode) {
            // If generation is occuring, definitions of audit types may be malformed, so we do nothing.
            // Synthetic models should not be used in this mode anyway.
            case GENERATION -> {}
            case DISABLED -> {}
            case ENABLED -> {
                domainMetadataUtils.registeredEntities()
                        .filter(em -> isSynAuditEntityType(em.javaType()) || isSynAuditPropEntityType(em.javaType()))
                        .forEach(em -> {
                            final var entityType = em.javaType();
                            final var field = findSyntheticModelFieldFor(entityType);
                            final var hasModelsField = field != null
                                                       && field.getName().equals("models_")
                                                       && List.class.isAssignableFrom(field.getType())
                                                       && !isFinal(field.getModifiers());
                            if (hasModelsField) {
                                Reflector.assignStatic(field, synModelProvider.getModels(entityType));
                            }
                            else {
                                throw new EntityDefinitionException(format(ERR_MISSING_MODELS_FIELD, entityType.getSimpleName()));
                            }
                        });
            }
        }
    }

    private SynAuditModelInitService() {}

}
