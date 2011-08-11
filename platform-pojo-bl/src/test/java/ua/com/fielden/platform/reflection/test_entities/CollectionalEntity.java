package ua.com.fielden.platform.reflection.test_entities;

import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;

/**
 * Test class, which represent an entity derived directly from AbstractEntity with collectional properties.
 * 
 * @author 01es
 * 
 */
@KeyType(String.class)
public class CollectionalEntity extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;

    @IsProperty(String.class)
    private List<String> strCollectionalProperty1;

    @IsProperty(String.class)
    private List<String> strCollectionalProperty2;

    @IsProperty(Integer.class)
    private List<Integer> intCollectionalProperty;

    protected CollectionalEntity() {
    }

    public List<Integer> getIntCollectionalProperty() {
	return intCollectionalProperty;
    }

    @Observable
    public void setIntCollectionalProperty(final List<Integer> strCollectionalProperty) {
	this.intCollectionalProperty = strCollectionalProperty;
    }

    public List<String> getStrCollectionalProperty1() {
	return strCollectionalProperty1;
    }

    @Observable
    public void setStrCollectionalProperty1(final List<String> strCollectionalProperty1) {
	this.strCollectionalProperty1 = strCollectionalProperty1;
    }

    public List<String> getStrCollectionalProperty2() {
	return strCollectionalProperty2;
    }

    @Observable
    public void setStrCollectionalProperty2(final List<String> strCollectionalProperty2) {
	this.strCollectionalProperty2 = strCollectionalProperty2;
    }
}
