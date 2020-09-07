package ua.com.fielden.platform.sample.domain.compound;

import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.DescRequired;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.DisplayDescription;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.utils.Pair;

/** 
 * Master entity object.
 * 
 * @author TG Team
 *
 */
@KeyType(String.class)
@KeyTitle("Key")
@CompanionObject(ITgCompoundEntity.class)
@MapEntityTo
@DescTitle("Description")
@DisplayDescription
@DescRequired
public class TgCompoundEntity extends ActivatableAbstractEntity<String> {

    private static final Pair<String, String> entityTitleAndDesc = TitlesDescsGetter.getEntityTitleAndDesc(TgCompoundEntity.class);
    public static final String ENTITY_TITLE = entityTitleAndDesc.getKey();
    public static final String ENTITY_DESC = entityTitleAndDesc.getValue();
    
    @Override
    @Observable
    public TgCompoundEntity setActive(final boolean active) {
        super.setActive(active);
        return this;
    }

}