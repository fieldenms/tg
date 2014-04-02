package ua.com.fielden.platform.test.domain.entities;

import ua.com.fielden.platform.entity.annotation.KeyType;

/**
 * 
 * Represents a workshop entity, which can be both contractor and internal corporate workshop.
 * 
 * @author 01es
 * 
 */
@KeyType(String.class)
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
