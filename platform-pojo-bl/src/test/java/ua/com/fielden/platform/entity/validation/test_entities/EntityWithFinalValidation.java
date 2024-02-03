/**
 *
 */
package ua.com.fielden.platform.entity.validation.test_entities;

import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.validation.annotation.Final;

/**
 * Entity for testing final validation.
 * 
 * @author TG Team
 */
@MapEntityTo
@KeyType(String.class)
public class EntityWithFinalValidation extends AbstractEntity<String> {

    public static final String ENTITY_TITLE = getEntityTitleAndDesc(EntityWithFinalValidation.class).getKey();

    @IsProperty
    @MapTo
    @Final(persistedOnly = false)
    @Title("Non-persisted As Final")
    private String propNonPersistedAsFinal;

    @IsProperty
    @Final
    private String propNonNullAsFinalValue;

    @IsProperty
    @Final(nullIsValueForPersisted = true)
    private String propNullAsFinalValue;

    @IsProperty
    @MapTo
    @Final(persistedOnly = false, nullIsValueForPersisted = true)
    private String propNonPersistedAndNullAsFinalValue;

    /**
     * Exposed to mimic persisted instances.
     */
    @Override
    public void setId(final Long id) {
        super.setId(id);
    }

    @Observable
    public EntityWithFinalValidation setPropNonPersistedAndNullAsFinalValue(final String propNonPersistedAndNullAsFinalValue) {
        this.propNonPersistedAndNullAsFinalValue = propNonPersistedAndNullAsFinalValue;
        return this;
    }

    public String getPropNonPersistedAndNullAsFinalValue() {
        return propNonPersistedAndNullAsFinalValue;
    }

    @Observable
    public EntityWithFinalValidation setPropNonPersistedAsFinal(final String propNonPersistedAsFinal) {
        this.propNonPersistedAsFinal = propNonPersistedAsFinal;
        return this;
    }

    public String getPropNonPersistedAsFinal() {
        return propNonPersistedAsFinal;
    }

    @Observable
    public EntityWithFinalValidation setPropNullAsFinalValue(final String propWithLength) {
        this.propNullAsFinalValue = propWithLength;
        return this;
    }

    public String getPropNullAsFinalValue() {
        return propNullAsFinalValue;
    }

    @Observable
    public EntityWithFinalValidation setPropNonNullAsFinalValue(final String name) {
        this.propNonNullAsFinalValue = name;
        return this;
    }

    public String getPropNonNullAsFinalValue() {
        return propNonNullAsFinalValue;
    }

}
