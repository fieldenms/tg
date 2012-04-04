package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.validation.annotation.DefaultController;
import ua.com.fielden.platform.sample.domain.controller.ITgBogieClass;

@KeyType(String.class)
@MapEntityTo
@DescTitle("Description")
@DefaultController(ITgBogieClass.class)
public class TgBogieClass extends AbstractEntity<String> {

    private static final long serialVersionUID = 1L;

//    @IsProperty(BogieClassCompatibility.class)
//    private Set<BogieClassCompatibility> compatibles = new HashSet<BogieClassCompatibility>();
    @IsProperty @MapTo
    private Integer tonnage;


    protected TgBogieClass() {

    }

    public TgBogieClass (final String code, final String desc, final Integer tonnage) {
	super(null, code, desc);
	setTonnage(tonnage);
    }

    public Integer getTonnage() {
        return tonnage;
    }

    @Observable
    public TgBogieClass setTonnage(final Integer tonnage) {
        this.tonnage = tonnage;
        return this;
    }

//    public Set<BogieClassCompatibility> getCompatibles() {
//	return this.compatibles;
//    }
//
//    @Observable
//    protected void setCompatibles(final Set<BogieClassCompatibility> compatibles) {
//	this.compatibles = compatibles;
//    }
//
    public int getNumberOfWheelsets() {
	return 2;
    }
}
