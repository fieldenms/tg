package ua.com.fielden.platform.sample.domain;

import jakarta.annotation.Nonnull;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.processors.metamodel.IConvertableToPath;

import java.util.SortedSet;
import java.util.TreeSet;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

@KeyType(String.class)
@KeyTitle(value = "Wagon No", desc = "Wagon number")
@DescTitle(value = "Description", desc = "Wagon description")
@MapEntityTo
@CompanionObject(ITgWagon.class)
public class TgWagon extends AbstractEntity<String> {

    public enum Property implements IConvertableToPath {
        serialNo, wagonClass, slots, firstSlot, internalNumber;

        @Override
        @Nonnull
        public String toPath() {
            return name();
        }
    }

    @IsProperty
    @MapTo
    @Title(value = "Serial No", desc = "Wagon serial number")
    private String serialNo;

    @IsProperty
    @MapTo
    @Title(value = "Class", desc = "Wagon class")
    private TgWagonClass wagonClass;

    @IsProperty(value = TgWagonSlot.class, linkProperty = "wagon")
    @Title(value = "Wagon slots", desc = "A list of slots for the wagon")
    private SortedSet<TgWagonSlot> slots = new TreeSet<TgWagonSlot>();  

    @IsProperty
    @Readonly
    @Calculated
    @Title(value = "Title", desc = "Desc")
    private TgWagonSlot firstSlot;
    protected static final ExpressionModel firstSlot_ = expr().model(select(TgWagonSlot.class).where().prop("wagon").eq().extProp("id").and().prop("position").eq().val(1).model()).model();

    @IsProperty
    @Calculated("1")
    @Title(value = "Internal number")
    @DenyIntrospection
    private Integer internalNumber;

    @Observable
    protected TgWagon setInternalNumber(final Integer internalNumber) {
        this.internalNumber = internalNumber;
        return this;
    }

    public Integer getInternalNumber() {
        return internalNumber;
    }


    @Observable
    protected TgWagon setFirstSlot(final TgWagonSlot firstSlot) {
        this.firstSlot = firstSlot;
        return this;
    }

    public TgWagonSlot getFirstSlot() {
        return firstSlot;
    }

    

    

    
    protected TgWagon() {
    }

    public TgWagon(final String number, final String desc) {
        super(null, number, desc);
    }

    public String getSerialNo() {
        return serialNo;
    }

    @Observable
    public TgWagon setSerialNo(final String serialNo) {
        this.serialNo = serialNo;
        return this;
    }

    public TgWagonClass getWagonClass() {
        return wagonClass;
    }

    @Observable
    public TgWagon setWagonClass(final TgWagonClass wagonClass) {
        this.wagonClass = wagonClass;
        return this;
    }

    public SortedSet<TgWagonSlot> getSlots() {
        return slots;
    }

    @Observable
    protected void setSlots(final SortedSet<TgWagonSlot> slots) {
        this.slots = slots;
    }
}
