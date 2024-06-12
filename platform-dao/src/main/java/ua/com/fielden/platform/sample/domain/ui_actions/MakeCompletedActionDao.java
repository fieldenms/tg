package ua.com.fielden.platform.sample.domain.ui_actions;

import static ua.com.fielden.platform.entity.validation.custom.DefaultEntityValidator.validateWithoutCritOnly;

import java.util.Date;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.sample.domain.TgPersistentEntityWithProperties;

/**
 * DAO implementation for companion object {@link MakeCompletedActionCo}.
 *
 * @author TG Team
 */
@EntityType(MakeCompletedAction.class)
public class MakeCompletedActionDao extends CommonEntityDao<MakeCompletedAction> implements MakeCompletedActionCo {

    @Override
    @SessionRequired
    public MakeCompletedAction save(final MakeCompletedAction entity) {
        final TgPersistentEntityWithProperties masterEntity = entity.getMasterEntity();
        masterEntity.isValid(validateWithoutCritOnly)
                .ifFailure(Result::throwRuntime); // make sure that masterEntity is valid before continuing

        masterEntity.setDateProp(
                new Date()); // record date to easily observe updated instance in parent masters / centres
        masterEntity.setCompleted(true); // make completed

        final TgPersistentEntityWithProperties savedMasterEntity = co$(TgPersistentEntityWithProperties.class).save(
                masterEntity);
        entity.getProperty("masterEntity").setValue(savedMasterEntity,
                                                    true); // it is important to set the same entity with enforcement; otherwise previous unsaved version will be preserved and it would not be suitable for parent entity master binding

        return super.save(entity);
    }

}
