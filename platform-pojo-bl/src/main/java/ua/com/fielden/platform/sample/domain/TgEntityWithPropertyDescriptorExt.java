package ua.com.fielden.platform.sample.domain;

import static ua.com.fielden.platform.entity.annotation.CritOnly.Type.MULTI;
import static ua.com.fielden.platform.entity.annotation.CritOnly.Type.SINGLE;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;

@EntityTitle(value = "TG Entity With Property Descriptor Ext", desc = "Extended TG Entity With Property Descriptor that provides additional selection criteria.")
@CompanionObject(TgEntityWithPropertyDescriptorExtCo.class)
public class TgEntityWithPropertyDescriptorExt extends TgEntityWithPropertyDescriptor {

    protected static final EntityResultQueryModel<TgEntityWithPropertyDescriptorExt> model_ = select(TgEntityWithPropertyDescriptor.class)
        .where()
            .critCondition(select(TgEntityWithPropertyDescriptor.class).where().prop("parent").eq().extProp("id"), "propertyDescriptor", "propertyDescriptorMultiCritCollectional").and()
            .critCondition("propertyDescriptor", "propertyDescriptorSingleCrit").and()
            .critCondition("propertyDescriptor", "propertyDescriptorMultiCrit")
        .yieldAll()
        .modelAsEntity(TgEntityWithPropertyDescriptorExt.class);

    @IsProperty(TgPersistentEntityWithProperties.class)
    @CritOnly(SINGLE)
    @Title(value = "Property Descriptor Single Crit", desc = "Crit-only single property mapped through critCondition on propertyDescriptor property.")
    private PropertyDescriptor<TgPersistentEntityWithProperties> propertyDescriptorSingleCrit;

    @IsProperty(TgPersistentEntityWithProperties.class)
    @CritOnly(MULTI)
    @Title(value = "Property Descriptor Multi Crit", desc = "Crit-only multi property mapped through critCondition on propertyDescriptor property.")
    private PropertyDescriptor<TgPersistentEntityWithProperties> propertyDescriptorMultiCrit;

    @IsProperty(TgPersistentEntityWithProperties.class)
    @CritOnly(MULTI)
    @Title(value = "Property Descriptor Multi Crit Collectional", desc = "Crit-only multi property mapped through critCondition on propertyDescriptor property of collectional association.")
    private PropertyDescriptor<TgPersistentEntityWithProperties> propertyDescriptorMultiCritCollectional;

    @Observable
    public TgEntityWithPropertyDescriptorExt setPropertyDescriptorMultiCritCollectional(final PropertyDescriptor<TgPersistentEntityWithProperties> propertyDescriptorMultiCritCollectional) {
        this.propertyDescriptorMultiCritCollectional = propertyDescriptorMultiCritCollectional;
        return this;
    }

    public PropertyDescriptor<TgPersistentEntityWithProperties> getPropertyDescriptorMultiCritCollectional() {
        return propertyDescriptorMultiCritCollectional;
    }

    @Observable
    public TgEntityWithPropertyDescriptorExt setPropertyDescriptorMultiCrit(final PropertyDescriptor<TgPersistentEntityWithProperties> propertyDescriptorMultiCrit) {
        this.propertyDescriptorMultiCrit = propertyDescriptorMultiCrit;
        return this;
    }

    public PropertyDescriptor<TgPersistentEntityWithProperties> getPropertyDescriptorMultiCrit() {
        return propertyDescriptorMultiCrit;
    }

    @Observable
    public TgEntityWithPropertyDescriptorExt setPropertyDescriptorSingleCrit(final PropertyDescriptor<TgPersistentEntityWithProperties> propertyDescriptorSingleCrit) {
        this.propertyDescriptorSingleCrit = propertyDescriptorSingleCrit;
        return this;
    }

    public PropertyDescriptor<TgPersistentEntityWithProperties> getPropertyDescriptorSingleCrit() {
        return propertyDescriptorSingleCrit;
    }

}