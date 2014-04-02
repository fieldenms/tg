package ua.com.fielden.platform.example.entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Title;

/**
 * Represents rotable class entity.
 * 
 * @author 01es
 * 
 */
@KeyType(String.class)
@KeyTitle(value = "Rotable Class No", desc = "Rotable Class key")
@DescTitle(value = "Rotable Class Description", desc = "Rotable class description")
public class RotableClass extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;

    @Title(value = "Tonnage", desc = "Rotable tonnage")
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
