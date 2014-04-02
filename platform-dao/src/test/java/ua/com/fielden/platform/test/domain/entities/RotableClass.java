package ua.com.fielden.platform.test.domain.entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.KeyType;

/**
 * Represents rotable class entity.
 * 
 * @author 01es
 * 
 */
@KeyType(String.class)
public class RotableClass extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;

    private Integer tonnage; // most likely should be immutable

    /**
     * Constructor for Hibernate.
     */
    protected RotableClass() {

    }

    public RotableClass(final String code, final String desc) {
        super(null, code, desc);
    }

    public Integer getTonnage() {
        return tonnage;
    }

    protected void setTonnage(final Integer tonnage) {
        this.tonnage = tonnage;
    }
}
