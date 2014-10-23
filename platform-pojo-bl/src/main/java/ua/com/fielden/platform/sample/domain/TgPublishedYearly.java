package ua.com.fielden.platform.sample.domain;

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
@KeyTitle(value = "Key", desc = "Some key description")
@EntityTitle("Published Yearly")
@CompanionObject(ITgPublishedYearly.class)
public class TgPublishedYearly extends AbstractEntity<DynamicEntityKey> {
    private static final EntityResultQueryModel<TgPublishedYearly> model_ = model();

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
    private TgAuthor author;

    @Observable
    @EntityExists(TgAuthor.class)
    public TgPublishedYearly setAuthor(final TgAuthor author) {
        this.author = author;
        return this;
    }

    private static EntityResultQueryModel<TgPublishedYearly> model() {
        return select(TgAuthorship.class). //
        groupBy().prop("year"). //
        yield().prop("year").as("year"). //
        yield().countAll().as("qty"). //
        yield().model(select(TgAuthor.class).where().prop("surname").eq().val("GRIES").model()).as("author"). //
        modelAsEntity(TgPublishedYearly.class);
    }

    public TgAuthor getAuthor() {
        return author;
    }

    @Observable
    public TgPublishedYearly setQty(final Integer qty) {
        this.qty = qty;
        return this;
    }

    public Integer getQty() {
        return qty;
    }

    @Observable
    public TgPublishedYearly setYear(final Integer year) {
        this.year = year;
        return this;
    }

    public Integer getYear() {
        return year;
    }
}