package ua.com.fielden.platform.entity.meta.test_entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.Dependent;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Required;
import ua.com.fielden.platform.entity.annotation.mutator.AfterChange;
import ua.com.fielden.platform.entity.annotation.mutator.BeforeChange;
import ua.com.fielden.platform.entity.annotation.mutator.Handler;
import ua.com.fielden.platform.entity.meta.test_entities.definers.EntityWithCircularDependentPropertiesAndDefiners_PropTwoAndThree_Definer;
import ua.com.fielden.platform.entity.meta.test_entities.definers.EntityWithCircularDependentPropertiesAndDefiners_PropOne_Definer;
import ua.com.fielden.platform.entity.meta.test_entities.validators.EntityWithCircularDependentPropertiesAndDefiners_PropTree_Validator;

@KeyType(String.class)
public class EntityWithCircularDependentPropertiesAndDefiners extends AbstractEntity<String> {

    @IsProperty
    @Required
    @Dependent({"two", "three"})
    @AfterChange(EntityWithCircularDependentPropertiesAndDefiners_PropOne_Definer.class)
    private String one;

    @IsProperty
    @Dependent({ "three" })
    @AfterChange(EntityWithCircularDependentPropertiesAndDefiners_PropTwoAndThree_Definer.class)
    private String two;

    @IsProperty
    @Dependent({ "two" })
    @BeforeChange(@Handler(EntityWithCircularDependentPropertiesAndDefiners_PropTree_Validator.class))
    @AfterChange(EntityWithCircularDependentPropertiesAndDefiners_PropTwoAndThree_Definer.class)
    private String three;

    public String getOne() {
        return one;
    }

    @Observable
    public void setOne(final String one) {
        this.one = one;
    }

    public String getTwo() {
        return two;
    }

    @Observable
    public void setTwo(final String two) {
        this.two = two;
    }

    public String getThree() {
        return three;
    }

    @Observable
    public void setThree(final String three) {
        this.three = three;
    }

}
