package ua.com.fielden.platform.eql.entities;

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

@KeyType(DynamicEntityKey.class)
@MapEntityTo
public class TgtAuthor extends AbstractEntity<DynamicEntityKey> {

    @IsProperty
    @MapTo
    @Title("Name")
    @CompositeKeyMember(1)
    private TgtPersonName name;

    @IsProperty
    @MapTo
    @Title("Surname")
    @CompositeKeyMember(2)
    private String surname;
    
    @IsProperty
    @MapTo
    @Title(value = "Patronymic")
    @Optional
    @CompositeKeyMember(3)
    private String patronymic;

    @IsProperty
    @Readonly
    @Calculated
    @Title("Has more than 1 publication")
    private boolean hasMultiplePublications;
    private static ExpressionModel hasMultiplePublications_ = expr().caseWhen().model(select(TgtAuthorship.class).where().prop("author").eq().extProp("id").yield().countAll().modelAsPrimitive()). //
    gt().val(1).then().val(true).otherwise().val(false).endAsBool().model();

    @IsProperty
    @Readonly
    @Calculated
    @Title("Has name David and multiple publications")
    private boolean hasMultiplePublicationsAndNamedDavid;
    private static ExpressionModel hasMultiplePublicationsAndNamedDavid_ = expr().caseWhen().begin().prop("hasMultiplePublications").eq().val(true).and().prop("name.key").eq().val("David").end().then().val(true).otherwise().val(false).endAsBool().model();

    @IsProperty
    @Readonly
    @Calculated
    @Title("Last royalty")
    private TgtAuthorRoyalty lastRoyalty;
    private static ExpressionModel lastRoyalty_ = expr().model(select(TgtAuthorRoyalty.class).where().prop("authorship.author").eq().extProp("id").model()).model();

    @Observable
    protected TgtAuthor setHasMultiplePublications(final boolean hasMultiplePublications) {
        this.hasMultiplePublications = hasMultiplePublications;
        return this;
    }

    public boolean getHasMultiplePublications() {
        return hasMultiplePublications;
    }

    @Observable
    private TgtAuthor setLastRoyalty(final TgtAuthorRoyalty lastRoyalty) {
        this.lastRoyalty = lastRoyalty;
        return this;
    }

    public TgtAuthorRoyalty getLastRoyalty() {
        return lastRoyalty;
    }

    public TgtPersonName getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    @Observable
    @EntityExists(TgtPersonName.class)
    public TgtAuthor setName(final TgtPersonName name) {
        this.name = name;
        return this;
    }

    @Observable
    public TgtAuthor setSurname(final String surname) {
        this.surname = surname;
        return this;
    }
    
    @Observable
    protected TgtAuthor setHasMultiplePublicationsAndNamedDavid(final boolean hasMultiplePublicationsAndNamedDavid) {
        this.hasMultiplePublicationsAndNamedDavid = hasMultiplePublicationsAndNamedDavid;
        return this;
    }

    public boolean getHasMultiplePublicationsAndNamedDavid() {
        return hasMultiplePublicationsAndNamedDavid;
    }

    @Observable
    public TgtAuthor setPatronymic(final String patronymic) {
        this.patronymic = patronymic;
        return this;
    }

    public String getPatronymic() {
        return patronymic;
    }
}