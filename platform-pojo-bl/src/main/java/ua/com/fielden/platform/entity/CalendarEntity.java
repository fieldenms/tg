package ua.com.fielden.platform.entity;

import static ua.com.fielden.platform.entity.NoKey.NO_KEY;

import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.utils.Pair;

/**
 * Master entity for work activity calendar view.
 *
 * @author TG Team
 *
 */
@KeyType(NoKey.class)
@CompanionObject(CalendarEntityCo.class)
public class CalendarEntity extends AbstractFunctionalEntityWithCentreContext<NoKey> {

    private static final Pair<String, String> entityTitleAndDesc = TitlesDescsGetter.getEntityTitleAndDesc(CalendarEntity.class);
    public static final String ENTITY_TITLE = entityTitleAndDesc.getKey();
    public static final String ENTITY_DESC = entityTitleAndDesc.getValue();

    public CalendarEntity() {
        setKey(NO_KEY);
    }

}