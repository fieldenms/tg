package ua.com.fielden.platform.sample.domain.compound;

import ua.com.fielden.platform.entity.AbstractPersistentEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.utils.Pair;

import java.util.Date;

/** 
 * One-2-Many entity object.
 * 
 * @author TG Team
 *
 */
@KeyType(DynamicEntityKey.class)
@KeyTitle("Key")
@CompanionObject(ITgCompoundEntityChild.class)
@MapEntityTo
@DescTitle("Description")
@DisplayDescription
@DescRequired
public class TgCompoundEntityChild extends AbstractPersistentEntity<DynamicEntityKey> {

    private static final Pair<String, String> entityTitleAndDesc = TitlesDescsGetter.getEntityTitleAndDesc(TgCompoundEntityChild.class);
    public static final String ENTITY_TITLE = entityTitleAndDesc.getKey();
    public static final String ENTITY_DESC = entityTitleAndDesc.getValue();

    @IsProperty
    @MapTo
    @CompositeKeyMember(1)
    private TgCompoundEntity tgCompoundEntity;
    
    @IsProperty
    @MapTo
    @Title("Date")
    @CompositeKeyMember(2)
    private Date date;
    
    @Observable
    public TgCompoundEntityChild setDate(final Date date) {
        this.date = date;
        return this;
    }
    
    public Date getDate() {
        return date;
    }
    
    @Observable
    public TgCompoundEntityChild setTgCompoundEntity(final TgCompoundEntity value) {
        this.tgCompoundEntity = value;
        return this;
    }
    
    public TgCompoundEntity getTgCompoundEntity() {
        return tgCompoundEntity;
    }

    @Override
    @Observable
    public TgCompoundEntityChild setDesc(String desc) {
        return super.setDesc(desc);
    }

}
