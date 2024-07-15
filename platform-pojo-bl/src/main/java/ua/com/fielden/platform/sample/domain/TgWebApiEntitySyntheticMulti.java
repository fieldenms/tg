package ua.com.fielden.platform.sample.domain;

import static ua.com.fielden.platform.criteria.generator.impl.CriteriaReflector.from;
import static ua.com.fielden.platform.criteria.generator.impl.CriteriaReflector.to;
import static ua.com.fielden.platform.entity.annotation.CritOnly.Type.MULTI;
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
 * Entity for for {@link IWebApi} testing -- synthetic based on persistent with crit-only multi criterion.
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
@CompanionObject(ITgWebApiEntitySyntheticMulti.class)
public class TgWebApiEntitySyntheticMulti extends TgWebApiEntity {
    
    private static final Pair<String, String> entityTitleAndDesc = TitlesDescsGetter.getEntityTitleAndDesc(TgWebApiEntitySyntheticMulti.class);
    public static final String ENTITY_TITLE = entityTitleAndDesc.getKey();
    public static final String ENTITY_DESC = entityTitleAndDesc.getValue();
    
    private static final EntityResultQueryModel<TgWebApiEntitySyntheticMulti> model_ = model();
    
    @IsProperty
    @CritOnly(MULTI)
    private Date datePeriod;
    
    @Observable
    public TgWebApiEntitySyntheticMulti setDatePeriod(final Date datePeriod) {
        this.datePeriod = datePeriod;
        return this;
    }
    
    public Date getDatePeriod() {
        return datePeriod;
    }
    
    private static EntityResultQueryModel<TgWebApiEntitySyntheticMulti> model() {
        return select(TgWebApiEntity.class).where()
            .prop("dateProp").ge().iParam(from("datePeriod")).and().prop("dateProp").lt().iParam(to("datePeriod"))
            .yieldAll()
            .modelAsEntity(TgWebApiEntitySyntheticMulti.class);
    }
    
}