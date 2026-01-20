package ua.com.fielden.platform.web.resources.webui.test_entities;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.entity.NoKey;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.sample.domain.TgFuelType;
import ua.com.fielden.platform.sample.domain.TgNote;
import ua.com.fielden.platform.types.Colour;
import ua.com.fielden.platform.types.Hyperlink;
import ua.com.fielden.platform.types.RichText;

import java.math.BigDecimal;
import java.util.Date;

/// Like [Action1] but with some properties absent.
///
@KeyType(NoKey.class)
@CompanionObject(Action2Co.class)
public class Action2 extends AbstractFunctionalEntityWithCentreContext<NoKey> {

    public enum Properties implements CharSequence {
        str1,
        bool,
        nInt,
        nLong,
        date2,
        colour,
        hyperlink,
        bigDecimal,
        richText,
        tc1,
        tc2,
        tc3,
        tc4,
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

    protected Action2() {
        setKey(NoKey.NO_KEY);
    }

    @IsProperty
    private String str1;

    @IsProperty
    private boolean bool;

    @IsProperty
    private Integer nInt;

    @IsProperty
    private Long nLong;

    @IsProperty
    private Date date2;

    @IsProperty
    private Colour colour;

    @IsProperty
    private Hyperlink hyperlink;

    @IsProperty
    private BigDecimal bigDecimal;

    @IsProperty
    private RichText richText;

    // Properties whose type changed.

    @IsProperty
    private TgFuelType tc1;

    @IsProperty
    private TgNote tc2;

    @IsProperty
    private String tc3;

    @IsProperty
    private BigDecimal tc4;

    public BigDecimal getTc4() {
        return tc4;
    }

    @Observable
    public Action2 setTc4(final BigDecimal tc4) {
        this.tc4 = tc4;
        return this;
    }

    public String getTc3() {
        return tc3;
    }

    @Observable
    public Action2 setTc3(final String tc3) {
        this.tc3 = tc3;
        return this;
    }

    public TgNote getTc2() {
        return tc2;
    }

    @Observable
    public Action2 setTc2(final TgNote tc2) {
        this.tc2 = tc2;
        return this;
    }

    public TgFuelType getTc1() {
        return tc1;
    }

    @Observable
    public Action2 setTc1(final TgFuelType tc1) {
        this.tc1 = tc1;
        return this;
    }

    public BigDecimal getBigDecimal() {
        return bigDecimal;
    }

    @Observable
    public Action2 setBigDecimal(final BigDecimal bigDecimal) {
        this.bigDecimal = bigDecimal;
        return this;
    }

    public RichText getRichText() {
        return richText;
    }

    @Observable
    public Action2 setRichText(final RichText richText) {
        this.richText = richText;
        return this;
    }

    public Integer getNInt() {
        return nInt;
    }

    @Observable
    public Action2 setNInt(final Integer nInt) {
        this.nInt = nInt;
        return this;
    }

    public boolean isBool() {
        return bool;
    }

    @Observable
    public Action2 setBool(final boolean bool) {
        this.bool = bool;
        return this;
    }

    public String getStr1() {
        return str1;
    }

    @Observable
    public Action2 setStr1(final String str1) {
        this.str1 = str1;
        return this;
    }

    public Hyperlink getHyperlink() {
        return hyperlink;
    }

    @Observable
    public Action2 setHyperlink(final Hyperlink hyperlink) {
        this.hyperlink = hyperlink;
        return this;
    }

    public Date getDate2() {
        return date2;
    }

    @Observable
    public Action2 setDate2(final Date date2) {
        this.date2 = date2;
        return this;
    }

    public Colour getColour() {
        return colour;
    }

    @Observable
    public Action2 setColour(final Colour colour) {
        this.colour = colour;
        return this;
    }

    public Long getNLong() {
        return nLong;
    }

    @Observable
    public Action2 setNLong(final Long nLong) {
        this.nLong = nLong;
        return this;
    }

}
