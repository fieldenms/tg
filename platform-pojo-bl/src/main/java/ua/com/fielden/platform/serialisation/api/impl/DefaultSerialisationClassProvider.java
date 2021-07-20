package ua.com.fielden.platform.serialisation.api.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.inject.Inject;

import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.basic.config.IApplicationSettings;
import ua.com.fielden.platform.entity.functional.master.AcknowledgeWarnings;
import ua.com.fielden.platform.entity.functional.master.PropertyWarning;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.security.SecurityRoleAssociationBatchAction;
import ua.com.fielden.platform.security.UserAndRoleAssociationBatchAction;
import ua.com.fielden.platform.security.user.SecurityTokenInfo;
import ua.com.fielden.platform.security.user.UserRoleTokensUpdater;
import ua.com.fielden.platform.security.user.UserRolesUpdater;
import ua.com.fielden.platform.serialisation.api.ISerialisationClassProvider;
import ua.com.fielden.platform.serialisation.jackson.entities.EmptyEntity;
import ua.com.fielden.platform.serialisation.jackson.entities.Entity1WithEntity2;
import ua.com.fielden.platform.serialisation.jackson.entities.Entity2WithEntity1;
import ua.com.fielden.platform.serialisation.jackson.entities.EntityWithBigDecimal;
import ua.com.fielden.platform.serialisation.jackson.entities.EntityWithBoolean;
import ua.com.fielden.platform.serialisation.jackson.entities.EntityWithColour;
import ua.com.fielden.platform.serialisation.jackson.entities.EntityWithCompositeKey;
import ua.com.fielden.platform.serialisation.jackson.entities.EntityWithDate;
import ua.com.fielden.platform.serialisation.jackson.entities.EntityWithDefiner;
import ua.com.fielden.platform.serialisation.jackson.entities.EntityWithHyperlink;
import ua.com.fielden.platform.serialisation.jackson.entities.EntityWithInteger;
import ua.com.fielden.platform.serialisation.jackson.entities.EntityWithListOfEntities;
import ua.com.fielden.platform.serialisation.jackson.entities.EntityWithMapOfEntities;
import ua.com.fielden.platform.serialisation.jackson.entities.EntityWithMetaProperty;
import ua.com.fielden.platform.serialisation.jackson.entities.EntityWithMoney;
import ua.com.fielden.platform.serialisation.jackson.entities.EntityWithOtherEntity;
import ua.com.fielden.platform.serialisation.jackson.entities.EntityWithPolymorphicAEProp;
import ua.com.fielden.platform.serialisation.jackson.entities.EntityWithPolymorphicProp;
import ua.com.fielden.platform.serialisation.jackson.entities.EntityWithSameEntity;
import ua.com.fielden.platform.serialisation.jackson.entities.EntityWithSetOfEntities;
import ua.com.fielden.platform.serialisation.jackson.entities.EntityWithString;
import ua.com.fielden.platform.serialisation.jackson.entities.OtherEntity;
import ua.com.fielden.platform.serialisation.jackson.entities.SubBaseEntity1;
import ua.com.fielden.platform.serialisation.jackson.entities.SubBaseEntity2;
import ua.com.fielden.platform.web.centre.CentreColumnWidthConfigUpdater;
import ua.com.fielden.platform.web.centre.CentreConfigConfigureAction;
import ua.com.fielden.platform.web.centre.CentreConfigDeleteAction;
import ua.com.fielden.platform.web.centre.CentreConfigDuplicateAction;
import ua.com.fielden.platform.web.centre.CentreConfigEditAction;
import ua.com.fielden.platform.web.centre.CentreConfigLoadAction;
import ua.com.fielden.platform.web.centre.CentreConfigNewAction;
import ua.com.fielden.platform.web.centre.CentreConfigSaveAction;
import ua.com.fielden.platform.web.centre.CentreConfigShareAction;
import ua.com.fielden.platform.web.centre.CentreConfigUpdater;
import ua.com.fielden.platform.web.centre.CustomisableColumn;
import ua.com.fielden.platform.web.centre.LoadableCentreConfig;
import ua.com.fielden.platform.web.centre.OverrideCentreConfig;

/**
 * Default implementation of {@link ISerialisationClassProvider}, which relies on the application settings to provide the location of classes to be used in serialisation.
 *
 * @author TG Team
 *
 */
public class DefaultSerialisationClassProvider implements ISerialisationClassProvider {

    protected final List<Class<?>> types = new ArrayList<>();

    @Inject
    public DefaultSerialisationClassProvider(final IApplicationSettings settings, final IApplicationDomainProvider applicationDomain) throws Exception {
        types.add(Exception.class);
        types.add(StackTraceElement[].class);
        types.addAll(typesForSerialisationTesting());
        types.addAll(applicationDomain.entityTypes());
        types.add(UserAndRoleAssociationBatchAction.class);
        types.add(SecurityRoleAssociationBatchAction.class);
        types.add(UserRolesUpdater.class);
        types.add(UserRoleTokensUpdater.class);
        types.add(SecurityTokenInfo.class);
        types.add(CentreConfigUpdater.class);
        types.add(CustomisableColumn.class);
        types.add(CentreColumnWidthConfigUpdater.class);
        
        types.add(CentreConfigShareAction.class);
        types.add(CentreConfigNewAction.class);
        types.add(CentreConfigDuplicateAction.class);
        types.add(CentreConfigLoadAction.class);
        types.add(CentreConfigEditAction.class);
        types.add(CentreConfigDeleteAction.class);
        types.add(CentreConfigSaveAction.class);
        types.add(LoadableCentreConfig.class);
        types.add(OverrideCentreConfig.class);
        types.add(CentreConfigConfigureAction.class);
        
        types.add(PropertyDescriptor.class);
        types.add(AcknowledgeWarnings.class);
        types.add(PropertyWarning.class);
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
                EntityWithHyperlink.class
                );
    }

    @Override
    public List<Class<?>> classes() {
        return Collections.unmodifiableList(types);
    }

}