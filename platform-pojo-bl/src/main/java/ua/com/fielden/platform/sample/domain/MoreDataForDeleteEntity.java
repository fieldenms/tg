package ua.com.fielden.platform.sample.domain;

import static ua.com.fielden.platform.entity.NoKey.NO_KEY;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.entity.IContinuationData;
import ua.com.fielden.platform.entity.NoKey;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.utils.Pair;

/**
 * More data for delete entity.
 *
 * @author TG Team
 *
 */
@KeyType(NoKey.class)
@CompanionObject(MoreDataForDeleteEntityCo.class)
public class MoreDataForDeleteEntity extends AbstractFunctionalEntityWithCentreContext<NoKey> implements IContinuationData{

    private static final Pair<String, String> entityTitleAndDesc = TitlesDescsGetter.getEntityTitleAndDesc(MoreDataForDeleteEntity.class);
    public static final String ENTITY_TITLE = entityTitleAndDesc.getKey();
    public static final String ENTITY_DESC = entityTitleAndDesc.getValue();

    protected MoreDataForDeleteEntity() {
        setKey(NO_KEY);
    }

    @IsProperty
    @Title(value = "Number", desc = "Number for more data")
    private Integer number;

    @Observable
    public MoreDataForDeleteEntity setNumber(final Integer number) {
        this.number = number;
        return this;
    }

    public Integer getNumber() {
        return number;
    }
}
