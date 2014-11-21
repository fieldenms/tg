package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.entity.validation.annotation.Final;

/**
 * Represents a work order status required field.
 *
 * @author TG Team
 *
 */
@KeyType(DynamicEntityKey.class)
public class TgWoStatusRequiredField extends AbstractEntity<DynamicEntityKey> {
    private static final long serialVersionUID = 1L;

    @IsProperty
    @CompositeKeyMember(1)
    private TgWorkOrderStatus woStatus;
    @IsProperty(TgWorkOrder.class)
    @CompositeKeyMember(2)
    private PropertyDescriptor<TgWorkOrder> requiredProperty;

    /**
     * Default constructor for instantiation by Hibernate.
     */
    protected TgWoStatusRequiredField() {
        super(null, null, "");
        setKey(new DynamicEntityKey(this));
    }

    /**
     * The main constructor.
     *
     * @param
     */
    public TgWoStatusRequiredField(final TgWorkOrderStatus woStatus, final PropertyDescriptor<TgWorkOrder> requiredField) {
        this();
        setKey(new DynamicEntityKey(this));
        setWoStatus(woStatus);
        setRequiredProperty(requiredField);
    }

    public TgWorkOrderStatus getWoStatus() {
        return woStatus;
    }

    @Final
    @Observable
    public void setWoStatus(final TgWorkOrderStatus woStatus) {
        this.woStatus = woStatus;
    }

    public PropertyDescriptor<TgWorkOrder> getRequiredProperty() {
        return requiredProperty;
    }

    @Final
    @Observable
    public void setRequiredProperty(final PropertyDescriptor<TgWorkOrder> requiredField) {
        this.requiredProperty = requiredField;
    }
}