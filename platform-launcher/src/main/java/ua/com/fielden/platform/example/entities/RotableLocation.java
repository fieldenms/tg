package ua.com.fielden.platform.example.entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;

@KeyTitle(value = "Rotable location", desc = "Rotable location key")
@DescTitle(value = "Rotable location description", desc = "description of the rotable location")
public abstract class RotableLocation<T extends Comparable<T>> extends AbstractEntity<T> {

    protected RotableLocation() {
    }

    public RotableLocation(final Long id, final T key, final String desc) {
        super(id, key, desc);
    }

    public static void main(final String[] args) {
        final Class<?> propType = PropertyTypeDeterminator.determinePropertyType(RotableLocation.class, "key");
    }
}
