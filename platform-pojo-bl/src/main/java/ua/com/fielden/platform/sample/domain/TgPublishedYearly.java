package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.entity.annotation.mutator.BeforeChange;
import ua.com.fielden.platform.entity.annotation.mutator.Handler;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.sample.domain.validators.TgPublishedYearly_AuthorValidator;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

@KeyType(DynamicEntityKey.class)
@KeyTitle(value = "Key", desc = "Some key description")
@EntityTitle("Published Yearly")
@CompanionObject(ITgPublishedYearly.class)
public class TgPublishedYearly extends AbstractEntity<DynamicEntityKey> {

    protected static final EntityResultQueryModel<TgPublishedYearly> model_ = model();

    @IsProperty
    @Title("Author")
    @BeforeChange(@Handler(TgPublishedYearly_AuthorValidator.class))
    @CompositeKeyMember(1)
    @Optional
    private TgAuthor author;
    
    @IsProperty
    @Title(value = "Count", desc = "Number of publications of given author")
    private Integer qty;

    private static EntityResultQueryModel<TgPublishedYearly> model() {
        final var authorsModel = select(TgAuthorship.class).
                groupBy().prop("author").
                yield().countAll().as("qty").
                yield().prop("author").as("author").
                modelAsEntity(TgPublishedYearly.class);

        final var summaryModel = select(TgAuthorship.class).
                yield().countAll().asRequired("qty").
                yield().val(null).as("author").
                modelAsEntity(TgPublishedYearly.class);

        return select(summaryModel, authorsModel).model();
    }

    @Observable
    public TgPublishedYearly setAuthor(final TgAuthor author) {
        this.author = author;
        return this;
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

}
