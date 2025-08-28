package ua.com.fielden.platform.reflection.test_entities;

import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.sample.domain.TgAuthor;
import ua.com.fielden.platform.sample.domain.TgCategory;

import static ua.com.fielden.platform.entity.annotation.CritOnly.Type.MULTI;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;

@KeyType(String.class)
@MapEntityTo
public class EntityWithPropertiesOfActivatableTypes extends ActivatableAbstractEntity<String> {

    @IsProperty
    @Calculated
    private TgCategory calcCategory;
    protected static final ExpressionModel calcCategory_ = expr().val(null).model();

    @IsProperty
    @Calculated
    private TgAuthor calcAuthor;
    protected static final ExpressionModel calcAuthor_ = expr().val(null).model();

    @IsProperty
    @CritOnly(MULTI)
    private TgCategory categoryCrit;

    @IsProperty
    @CritOnly(MULTI)
    private TgAuthor authorCrit;

    @IsProperty
    private TgCategory plainCategory;

    @IsProperty
    private TgAuthor plainAuthor;

    @IsProperty
    @MapTo
    private TgCategory category;

    @IsProperty
    @MapTo
    private TgAuthor author;

    public TgAuthor getAuthor() {
        return author;
    }

    @Observable
    public EntityWithPropertiesOfActivatableTypes setAuthor(final TgAuthor author) {
        this.author = author;
        return this;
    }

    public TgCategory getCategory() {
        return category;
    }

    @Observable
    public EntityWithPropertiesOfActivatableTypes setCategory(final TgCategory category) {
        this.category = category;
        return this;
    }

    public TgAuthor getPlainAuthor() {
        return plainAuthor;
    }

    @Observable
    public EntityWithPropertiesOfActivatableTypes setPlainAuthor(final TgAuthor plainAuthor) {
        this.plainAuthor = plainAuthor;
        return this;
    }

    public TgCategory getPlainCategory() {
        return plainCategory;
    }

    @Observable
    public EntityWithPropertiesOfActivatableTypes setPlainCategory(final TgCategory plainCategory) {
        this.plainCategory = plainCategory;
        return this;
    }

    @Observable
    public EntityWithPropertiesOfActivatableTypes setAuthorCrit(final TgAuthor authorCrit) {
        this.authorCrit = authorCrit;
        return this;
    }

    public TgAuthor getAuthorCrit() {
        return authorCrit;
    }


    @Observable
    public EntityWithPropertiesOfActivatableTypes setCategoryCrit(final TgCategory categoryCrit) {
        this.categoryCrit = categoryCrit;
        return this;
    }

    public TgCategory getCategoryCrit() {
        return categoryCrit;
    }


    @Observable
    protected EntityWithPropertiesOfActivatableTypes setCalcAuthor(final TgAuthor calcAuthor) {
        this.calcAuthor = calcAuthor;
        return this;
    }

    public TgAuthor getCalcAuthor() {
        return calcAuthor;
    }


    @Observable
    protected EntityWithPropertiesOfActivatableTypes setCalcCategory(final TgCategory calcCategory) {
        this.calcCategory = calcCategory;
        return this;
    }

    public TgCategory getCalcCategory() {
        return calcCategory;
    }

}
