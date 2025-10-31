package ua.com.fielden.platform.tiny;

import jakarta.inject.Inject;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.functional.centre.CentreContextHolder;
import ua.com.fielden.platform.entity.functional.centre.SavingInfoHolder;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.api.SerialiserEngines;

import java.util.Map;

import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.baseEntityType;

@EntityType(TinyHyperlink.class)
public class TinyHyperlinkDao extends CommonEntityDao<TinyHyperlink> implements TinyHyperlinkCo {

    private final ISerialiser serialiser;

    @Inject
    TinyHyperlinkDao(final ISerialiser serialiser) {
        this.serialiser = serialiser;
    }

    @Override
    protected IFetchProvider<TinyHyperlink> createFetchProvider() {
        return FETCH_PROVIDER;
    }

    @Override
    @SessionRequired
    public TinyHyperlink save(
            final Class<? extends AbstractEntity<?>> entityType,
            final Map<String, Object> modifiedProperties,
            final CentreContextHolder centreContextHolder)
    {
        // TODO Perform a transformation.
        //      `modifiedProperties` contains conventional property values, while `modifHolder` requires a special structure used in marshalling.
        final var modifHolder = modifiedProperties;
        final var savingInfoHolder = getEntityFactory().newEntity(SavingInfoHolder.class)
                .setModifHolder(modifHolder)
                .setCentreContextHolder(centreContextHolder);

        return save(entityType, savingInfoHolder);
    }

    @Override
    @SessionRequired
    public TinyHyperlink save(final Class<? extends AbstractEntity<?>> entityType, final SavingInfoHolder savingInfoHolder) {
        final var serialisedSavingInfoHolder = serialiser.serialise(savingInfoHolder, SerialiserEngines.JACKSON);
        final var link = new_()
                .setEntityTypeName(baseEntityType(entityType).getCanonicalName())
                .setSavingInfoHolder(serialisedSavingInfoHolder)
                .setUser(getUser())
                .setCreatedDate(now().toDate());
        return save(link);
    }

}
