package ua.com.fielden.platform.web.resources.webui.test_entities;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.entity.NoKey;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.sample.domain.TgPersonName;
import ua.com.fielden.platform.types.Colour;
import ua.com.fielden.platform.types.Hyperlink;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.types.RichText;

import java.math.BigDecimal;
import java.util.Date;

@KeyType(NoKey.class)
@CompanionObject(Action1Co.class)
public class Action1 extends AbstractFunctionalEntityWithCentreContext<NoKey> {

    public enum Properties implements CharSequence {
        str1,
        str2,
        bool,
        nInt,
        nLong,
        date1,
        date2,
        colour,
        hyperlink,
        money,
        bigDecimal,
        richText,
        personName,
        ;

        @Override
        public int length() {
            return name().length();
        }

        @Override
        public char charAt(final int index) {
            return name().charAt(index);
        }

        @Override
        public CharSequence subSequence(final int start, final int end) {
            return name().subSequence(start, end);
        }
    }

    protected Action1() {
        setKey(NoKey.NO_KEY);
    }

    @IsProperty
    private String str1;

    @IsProperty
    private String str2;

    @IsProperty
    private boolean bool;

    @IsProperty
    private Integer nInt;

    @IsProperty
    private Long nLong;

    @IsProperty
    private Date date1;

    @IsProperty
    private Date date2;

    @IsProperty
    private Colour colour;

    @IsProperty
    private Hyperlink hyperlink;

    @IsProperty
    private Money money;

    @IsProperty
    private BigDecimal bigDecimal;

    @IsProperty
    private RichText richText;

    @IsProperty
    private TgPersonName personName;

    public String getStr2() {
        return str2;
    }

    @Observable
    public Action1 setStr2(final String str2) {
        this.str2 = str2;
        return this;
    }

    public TgPersonName getPersonName() {
        return personName;
    }

    public BigDecimal getBigDecimal() {
        return bigDecimal;
    }

    @Observable
    public Action1 setBigDecimal(final BigDecimal bigDecimal) {
        this.bigDecimal = bigDecimal;
        return this;
    }

    @Observable
    public Action1 setPersonName(final TgPersonName personName) {
        this.personName = personName;
        return this;
    }

    public RichText getRichText() {
        return richText;
    }

    @Observable
    public Action1 setRichText(final RichText richText) {
        this.richText = richText;
        return this;
    }

    public Money getMoney() {
        return money;
    }

    @Observable
    public Action1 setMoney(final Money money) {
        this.money = money;
        return this;
    }

    public Integer getNInt() {
        return nInt;
    }

    @Observable
    public Action1 setNInt(final Integer nInt) {
        this.nInt = nInt;
        return this;
    }

    public boolean isBool() {
        return bool;
    }

    @Observable
    public Action1 setBool(final boolean bool) {
        this.bool = bool;
        return this;
    }

    public String getStr1() {
        return str1;
    }

    @Observable
    public Action1 setStr1(final String str1) {
        this.str1 = str1;
        return this;
    }

    public Hyperlink getHyperlink() {
        return hyperlink;
    }

    @Observable
    public Action1 setHyperlink(final Hyperlink hyperlink) {
        this.hyperlink = hyperlink;
        return this;
    }

    public Date getDate2() {
        return date2;
    }

    @Observable
    public Action1 setDate2(final Date date2) {
        this.date2 = date2;
        return this;
    }

    public Colour getColour() {
        return colour;
    }

    @Observable
    public Action1 setColour(final Colour colour) {
        this.colour = colour;
        return this;
    }

    public Date getDate1() {
        return date1;
    }

    @Observable
    public Action1 setDate1(final Date date1) {
        this.date1 = date1;
        return this;
    }

    public Long getNLong() {
        return nLong;
    }

    @Observable
    public Action1 setNLong(final Long nLong) {
        this.nLong = nLong;
        return this;
    }

}
