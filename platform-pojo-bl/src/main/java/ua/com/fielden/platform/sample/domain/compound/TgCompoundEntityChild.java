package ua.com.fielden.platform.sample.domain.compound;

import java.util.Date;

import ua.com.fielden.platform.entity.AbstractPersistentEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.DescRequired;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.DisplayDescription;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.utils.Pair;

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
    
}