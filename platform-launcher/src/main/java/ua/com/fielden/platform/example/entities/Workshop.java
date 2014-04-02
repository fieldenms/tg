package ua.com.fielden.platform.example.entities;

import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;

/**
 * 
 * Represents a workshop entity, which can be both contractor and internal corporate workshop.
 * 
 * @author 01es
 * 
 */
@EntityTitle(value = "", desc = "")
@KeyType(String.class)
@KeyTitle(value = "Workshop No", desc = "Workshop Number")
@DescTitle(value = "Workshop desc", desc = "Workshop description")
public class Workshop extends RotableLocation<String> {
    private static final long serialVersionUID = 1L;

    private boolean contractorWorkshop;

    /**
     * Constructor for Hibernate.
     */
    protected Workshop() {

    }

    /**
     * The main constructor.
     * 
     * @param name
     * @param desc
     */
    public Workshop(final String name, final String desc) {
        super(null, name, desc);
    }

    public boolean isContractorWorkshop() {
        return contractorWorkshop;
    }

    protected void setContractorWorkshop(final boolean value) {
        this.contractorWorkshop = value;
    }
}
