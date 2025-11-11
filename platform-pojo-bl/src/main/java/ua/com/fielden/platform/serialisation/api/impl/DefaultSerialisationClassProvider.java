package ua.com.fielden.platform.serialisation.api.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.basic.config.IApplicationSettings;
import ua.com.fielden.platform.domain.PlatformDomainTypes;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.menu.UserMenuInvisibilityAssociationBatchAction;
import ua.com.fielden.platform.security.UserAndRoleAssociationBatchAction;
import ua.com.fielden.platform.serialisation.api.ISerialisationClassProvider;
import ua.com.fielden.platform.serialisation.jackson.entities.*;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Default implementation of {@link ISerialisationClassProvider}, which relies on the application settings to provide the location of classes to be used in serialisation.
 *
 * @author TG Team
 *
 */
public class DefaultSerialisationClassProvider implements ISerialisationClassProvider {

    protected final ImmutableList<Class<?>> types;

    @Inject
    public DefaultSerialisationClassProvider(final IApplicationSettings settings, final IApplicationDomainProvider applicationDomain) throws Exception {
        final Set<Class<?>> types = new LinkedHashSet<>(); // avoid duplicate types, preserve order
        types.add(Exception.class);
        types.add(StackTraceElement[].class);
        types.addAll(typesForSerialisationTesting());
        types.addAll(applicationDomain.entityTypes()); // app-specific ApplicationDomain holds all types, potentially with TG platform ones ...
        types.addAll(PlatformDomainTypes.types); // ... but if not we ensure here that TG platform types will be present
        types.add(UserAndRoleAssociationBatchAction.class);
        types.add(UserMenuInvisibilityAssociationBatchAction.class);
        types.add(PropertyDescriptor.class);
        this.types = ImmutableList.copyOf(types);
    }

    private List<Class<?>> typesForSerialisationTesting() {
        return Arrays.asList(
                EmptyEntity.class,
                EntityWithBigDecimal.class,
                EntityWithInteger.class,
                EntityWithString.class,
                EntityWithMetaProperty.class,
                EntityWithBoolean.class,
                EntityWithDate.class,
                EntityWithOtherEntity.class,
                EntityWithSameEntity.class,
                OtherEntity.class,
                Entity1WithEntity2.class,
                Entity2WithEntity1.class,
                EntityWithSetOfEntities.class,
                EntityWithListOfEntities.class,
                EntityWithMapOfEntities.class,
                EntityWithPolymorphicProp.class,
                EntityWithDefiner.class,
                // BaseEntity.class,
                SubBaseEntity1.class,
                SubBaseEntity2.class,
                EntityWithCompositeKey.class,
                EntityWithMoney.class,
                EntityWithPolymorphicAEProp.class,
                EntityWithColour.class,
                EntityWithHyperlink.class,
                EntityWithRichText.class
                );
    }

    @Override
    public ImmutableList<Class<?>> classes() {
        return types;
    }

}
