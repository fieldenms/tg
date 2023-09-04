package ua.com.fielden.platform.sample.domain.compound;

import ua.com.fielden.platform.entity.AbstractPersistentEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.DescRequired;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.DisplayDescription;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
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

    // for one-2-one relationships always return the description of the main entity
    // this is why we need to redefine the desc property for this one-2-one entity in order to be able to mutate it independently for Web UI testing purposes.
    @IsProperty
    @MapTo
    private String desc;

    @Override
    public String getDesc() {
        return desc;
    }

    @Override
    @Observable
    public TgCompoundEntityDetail setDesc(final String desc) {
        this.desc = desc;
        return this;
    }

}