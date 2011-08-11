package ua.com.fielden.platform.example.entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;

@EntityTitle(value = "Equipment Class", desc = "Equipment Class entity")
@KeyTitle("Vehicle Class")
@DescTitle("Description")
@KeyType(String.class)
public class EqClass extends AbstractEntity<String> {

    /**
     * 
     */
    private static final long serialVersionUID = 5166275363338184012L;

}
