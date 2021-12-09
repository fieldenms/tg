package ua.com.fielden.platform.sample.domain;

import static ua.com.fielden.platform.entity.annotation.CritOnly.Type.SINGLE;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.util.Date;

import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.platform.web_api.IWebApi;

/**
 * Entity for for {@link IWebApi} testing -- synthetic based on persistent with crit-only single criterion.
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
@CompanionObject(ITgWebApiEntitySyntheticSingle.class)
public class TgWebApiEntitySyntheticSingle extends TgWebApiEntity {
    
    private static final Pair<String, String> entityTitleAndDesc = TitlesDescsGetter.getEntityTitleAndDesc(TgWebApiEntitySyntheticSingle.class);
    public static final String ENTITY_TITLE = entityTitleAndDesc.getKey();
    public static final String ENTITY_DESC = entityTitleAndDesc.getValue();
    
    private static final EntityResultQueryModel<TgWebApiEntitySyntheticSingle> model_ = model();
    
    @IsProperty
    @CritOnly(SINGLE)
    private Date date;
    
    @Observable
    public TgWebApiEntitySyntheticSingle setDate(final Date date) {
        this.date = date;
        return this;
    }
    
    public Date getDate() {
        return date;
    }
    
    private static EntityResultQueryModel<TgWebApiEntitySyntheticSingle> model() {
        return select(TgWebApiEntity.class).where()
            .prop("dateProp").ge().iParam("date")
            .yieldAll()
            .modelAsEntity(TgWebApiEntitySyntheticSingle.class);
    }
    
}