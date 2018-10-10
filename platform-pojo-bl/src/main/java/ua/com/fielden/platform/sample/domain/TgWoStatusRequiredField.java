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
    @Final
    private TgWorkOrderStatus woStatus;
    
    @IsProperty(TgWorkOrder.class)
    @CompositeKeyMember(2)
    @Final
    private PropertyDescriptor<TgWorkOrder> requiredProperty;

    protected TgWoStatusRequiredField() {
    }

    /**
     * The main constructor.
     *
     * @param
     */
    public TgWoStatusRequiredField(final TgWorkOrderStatus woStatus, final PropertyDescriptor<TgWorkOrder> requiredField) {
        setWoStatus(woStatus);
        setRequiredProperty(requiredField);
    }

    public TgWorkOrderStatus getWoStatus() {
        return woStatus;
    }

    @Observable
    public void setWoStatus(final TgWorkOrderStatus woStatus) {
        this.woStatus = woStatus;
    }

    public PropertyDescriptor<TgWorkOrder> getRequiredProperty() {
        return requiredProperty;
    }

    @Observable
    public void setRequiredProperty(final PropertyDescriptor<TgWorkOrder> requiredField) {
        this.requiredProperty = requiredField;
    }
}