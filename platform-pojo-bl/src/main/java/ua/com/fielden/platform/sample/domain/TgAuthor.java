package ua.com.fielden.platform.sample.domain;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.util.Date;

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
import ua.com.fielden.platform.entity.annotation.PersistentType;
import ua.com.fielden.platform.entity.annotation.Readonly;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.entity.validation.annotation.EntityExists;
import ua.com.fielden.platform.types.Hyperlink;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.types.markers.IHyperlinkType;
import ua.com.fielden.platform.types.markers.IUtcDateTimeType;

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

    @IsProperty
    @MapTo
    @Title(value = "Web", desc = "A web page")
    @PersistentType(userType = IHyperlinkType.class)
    private Hyperlink webpage;
    
    @IsProperty
    @MapTo
    @Title(value = "DOB (UTC)", desc = "Date of birth in UTC")
    @PersistentType(userType = IUtcDateTimeType.class)
    private Date utcDob;
    
    @IsProperty
    @MapTo
    @Title(value = "DOB", desc = "Date of birth in Local")
    private Date dob;

    @IsProperty
    private Money honorarium;
    
    @IsProperty
    private TgPersonName pseudonym;

    @Observable
    public TgAuthor setPseudonym(final TgPersonName pseudonym) {
        this.pseudonym = pseudonym;
        return this;
    }

    public TgPersonName getPseudonym() {
        return pseudonym;
    }
 
    @Observable
    public TgAuthor setHonorarium(final Money honorarium) {
        this.honorarium = honorarium;
        return this;
    }

    public Money getHonorarium() {
        return honorarium;
    }

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
    public TgAuthor setName(final TgPersonName name) {
        this.name = name;
        return this;
    }

    @Observable
    public TgAuthor setSurname(final String surname) {
        this.surname = surname;
        return this;
    }
    
    @Observable
    public TgAuthor setWebpage(final Hyperlink webpage) {
        this.webpage = webpage;
        return this;
    }

    public Hyperlink getWebpage() {
        return webpage;
    }

    @Observable
    public TgAuthor setUtcDob(final Date utcDob) {
        this.utcDob = utcDob;
        return this;
    }

    public Date getUtcDob() {
        return utcDob;
    }

    @Observable
    public TgAuthor setDob(final Date dob) {
        this.dob = dob;
        return this;
    }

    public Date getDob() {
        return dob;
    }
}