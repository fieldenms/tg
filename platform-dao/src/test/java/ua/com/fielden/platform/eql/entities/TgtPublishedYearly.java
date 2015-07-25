package ua.com.fielden.platform.eql.entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.annotation.mutator.BeforeChange;
import ua.com.fielden.platform.entity.annotation.mutator.Handler;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.validation.annotation.EntityExists;
import ua.com.fielden.platform.sample.domain.validators.TgPublishedYearly_AuthorValidator;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

/**
 * Master entity object.
 * 
 * @author Developers
 * 
 */
@KeyType(DynamicEntityKey.class)
public class TgtPublishedYearly extends AbstractEntity<DynamicEntityKey> {
    private static final EntityResultQueryModel<TgtPublishedYearly> model_ = model();

    @IsProperty
    @Title(value = "Year", desc = "Year of publication")
    @CompositeKeyMember(1)
    private Integer year;

    @IsProperty
    @Title(value = "Count", desc = "Number of publication in given year")
    @CompositeKeyMember(2)
    private Integer qty;

    @IsProperty
    @Title(value = "Most productive author", desc = "Desc")
    @BeforeChange(@Handler(TgPublishedYearly_AuthorValidator.class))
    private TgtAuthor author;

    @Observable
    @EntityExists(TgtAuthor.class)
    public TgtPublishedYearly setAuthor(final TgtAuthor author) {
        this.author = author;
        return this;
    }

    private static EntityResultQueryModel<TgtPublishedYearly> model() {
        return select(TgtAuthorship.class). //
        groupBy().prop("year"). //
        yield().prop("year").as("year"). //
        yield().countAll().as("qty"). //
        yield().model(select(TgtAuthor.class).where().prop("surname").eq().val("GRIES").model()).as("author"). //
        modelAsEntity(TgtPublishedYearly.class);
    }

    public TgtAuthor getAuthor() {
        return author;
    }

    @Observable
    public TgtPublishedYearly setQty(final Integer qty) {
        this.qty = qty;
        return this;
    }

    public Integer getQty() {
        return qty;
    }

    @Observable
    public TgtPublishedYearly setYear(final Integer year) {
        this.year = year;
        return this;
    }

    public Integer getYear() {
        return year;
    }
}