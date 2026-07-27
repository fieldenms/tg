package ua.com.fielden.platform.minheritance;

import com.google.inject.Injector;
import jakarta.inject.Inject;
import ua.com.fielden.platform.entity.exceptions.EntityDefinitionException;
import ua.com.fielden.platform.eql.meta.ISyntheticModelProvider;
import ua.com.fielden.platform.meta.IDomainMetadataUtils;
import ua.com.fielden.platform.reflection.Reflector;

import java.util.List;

import static java.lang.reflect.Modifier.isFinal;
import static ua.com.fielden.platform.utils.EntityUtils.findSyntheticModelFieldFor;
import static ua.com.fielden.platform.utils.EntityUtils.isGeneratedMultiInheritanceEntityType;

/// Performs initialisation of generated multi-inheritance entity types.
/// For each type, generates its EQL model and assigns it to the static field `models_` that must be declared by that type.
///
/// The purpose of this process is to support code where [ISyntheticModelProvider] cannot be used.
/// Such code accesses synthetic models through static fields of corresponding entity types.
///
/// Launched by the IoC framework upon creating an [Injector].
///
public final class SynModelInitService {

    private static final String ERR_MISSING_MODELS_FIELD =
            "Synthetic type [%s] must declare static non-final field [models_] with type List<EntityResultQueryModel>.";

    @Inject
    static void start(
            final IDomainMetadataUtils domainMetadataUtils,
            final ISyntheticModelProvider synModelProvider)
    {
        domainMetadataUtils.registeredEntities()
                .filter(em -> isGeneratedMultiInheritanceEntityType(em.javaType()))
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

    private SynModelInitService() {}

}
