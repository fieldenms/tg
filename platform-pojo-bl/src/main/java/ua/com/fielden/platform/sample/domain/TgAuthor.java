package ua.com.fielden.platform.sample.domain;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Optional;
import ua.com.fielden.platform.entity.annotation.Readonly;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.entity.validation.annotation.EntityExists;

@KeyTitle("Author")
@KeyType(DynamicEntityKey.class)
@MapEntityTo
@CompanionObject(ITgAuthor.class)
public class TgAuthor extends AbstractEntity<DynamicEntityKey> {
    private static final long serialVersionUID = 1L;

    @IsProperty
    @MapTo
    @Title("Name")
    @CompositeKeyMember(1)
    private TgPersonName name;

    @IsProperty
    @MapTo
    @Title("Surname")
    @CompositeKeyMember(2)
    private String surname;
    
    @IsProperty
    @MapTo
    @Title(value = "Patronymic", desc = "Desc")
    @Optional
    @CompositeKeyMember(3)
    private String patronymic;

    @Observable
    public TgAuthor setPatronymic(final String patronymic) {
        this.patronymic = patronymic;
        return this;
    }

    public String getPatronymic() {
        return patronymic;
    }

    @IsProperty
    @Readonly
    @Calculated
    @Title("Has more than 1 publication")
    private boolean hasMultiplePublications;
    private static ExpressionModel hasMultiplePublications_ = expr().caseWhen().model(select(TgAuthorship.class).where().prop("author").eq().extProp("id").yield().countAll().modelAsPrimitive()). //
    gt().val(1).then().val(true).otherwise().val(false).endAsBool().model();

    @IsProperty
    @Readonly
    @Calculated
    @Title("Last royalty")
    private TgAuthorRoyalty lastRoyalty;
    private static ExpressionModel lastRoyalty_ = expr().model(select(TgAuthorRoyalty.class).where().prop("authorship.author").eq().extProp("id").model()).model();

    @Observable
    protected TgAuthor setHasMultiplePublications(final boolean hasMultiplePublications) {
        this.hasMultiplePublications = hasMultiplePublications;
        return this;
    }

    public boolean getHasMultiplePublications() {
        return hasMultiplePublications;
    }

    @Observable
    private TgAuthor setLastRoyalty(final TgAuthorRoyalty lastRoyalty) {
        this.lastRoyalty = lastRoyalty;
        return this;
    }

    public TgAuthorRoyalty getLastRoyalty() {
        return lastRoyalty;
    }

    public TgPersonName getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    @Observable
    @EntityExists(TgPersonName.class)
    public TgAuthor setName(final TgPersonName name) {
        this.name = name;
        return this;
    }

    @Observable
    public TgAuthor setSurname(final String surname) {
        this.surname = surname;
        return this;
    }
}