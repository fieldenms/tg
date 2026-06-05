package ua.com.fielden.platform.audit;

import com.google.inject.Injector;
import jakarta.inject.Inject;
import ua.com.fielden.platform.entity.exceptions.EntityDefinitionException;
import ua.com.fielden.platform.eql.meta.ISyntheticModelProvider;
import ua.com.fielden.platform.meta.IDomainMetadataUtils;
import ua.com.fielden.platform.reflection.Reflector;

import java.util.List;

import static java.lang.reflect.Modifier.isFinal;
import static ua.com.fielden.platform.audit.AuditUtils.isSynAuditEntityType;
import static ua.com.fielden.platform.audit.AuditUtils.isSynAuditPropEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.findSyntheticModelFieldFor;

/// Performs the initialisation of synthetic audit-entity types.
///
/// For each such type, this method generates its EQL model and assigns it to the static field `models_`,
/// which must be declared by that type.
///
/// The purpose of this process is to support scenarios where [ISyntheticModelProvider] cannot be used.
/// In such cases, synthetic models are accessed via the static fields of their corresponding entity types.
///
/// This method is invoked automatically by the IoC framework during the creation of an [Injector].
///
/// It has an effect only when auditing is enabled.
///
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
            // If generation is occurring, definitions of audit types may be malformed, so we do nothing.
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
                                throw new EntityDefinitionException(ERR_MISSING_MODELS_FIELD.formatted(entityType.getSimpleName()));
                            }
                        });
            }
        }
    }

    private SynAuditModelInitService() {}

}
