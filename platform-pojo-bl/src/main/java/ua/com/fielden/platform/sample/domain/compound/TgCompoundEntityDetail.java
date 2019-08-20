package ua.com.fielden.platform.sample.domain.compound;

import ua.com.fielden.platform.entity.AbstractPersistentEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.DescRequired;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.DisplayDescription;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.utils.Pair;

/** 
 * One-2-One entity object.
 * 
 * @author TG Team
 *
 */
@KeyType(TgCompoundEntity.class)
@KeyTitle("Key")
@CompanionObject(ITgCompoundEntityDetail.class)
@MapEntityTo
@DescTitle("Description")
@DisplayDescription
@DescRequired
public class TgCompoundEntityDetail extends AbstractPersistentEntity<TgCompoundEntity> {

    private static final Pair<String, String> entityTitleAndDesc = TitlesDescsGetter.getEntityTitleAndDesc(TgCompoundEntityDetail.class);
    public static final String ENTITY_TITLE = entityTitleAndDesc.getKey();
    public static final String ENTITY_DESC = entityTitleAndDesc.getValue();

}