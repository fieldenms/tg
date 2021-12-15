package ua.com.fielden.platform.sample.domain.ui_actions;

import static ua.com.fielden.platform.entity.NoKey.NO_KEY;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.entity.NoKey;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.SkipEntityExistsValidation;
import ua.com.fielden.platform.sample.domain.TgPersistentEntityWithProperties;
import ua.com.fielden.platform.utils.Pair;

/**
 * Functional entity to make {@link TgPersistentEntityWithProperties} entity completed and save.
 * It applies all user modifications, sets 'completed' property and 'dateProp' and saves the instance.
 * Completed instance is not editable. {@link MakeCompletedAction} demonstrates how saved
 * {@link TgPersistentEntityWithProperties} instance should be bound into parent entity master.
 *
 * @author TG Team
 *
 */
@KeyType(NoKey.class)
@CompanionObject(MakeCompletedActionCo.class)
public class MakeCompletedAction extends AbstractFunctionalEntityWithCentreContext<NoKey> {

    private static final Pair<String, String> entityTitleAndDesc = getEntityTitleAndDesc(MakeCompletedAction.class);
    public static final String ENTITY_TITLE = entityTitleAndDesc.getKey();
    public static final String ENTITY_DESC = entityTitleAndDesc.getValue();

    public MakeCompletedAction() {
        setKey(NO_KEY);
    }

    @IsProperty
    @SkipEntityExistsValidation
    private TgPersistentEntityWithProperties masterEntity;

    @Observable
    public MakeCompletedAction setMasterEntity(final TgPersistentEntityWithProperties masterEntity) {
        this.masterEntity = masterEntity;
        return this;
    }

    public TgPersistentEntityWithProperties getMasterEntity() {
        return masterEntity;
    }

}