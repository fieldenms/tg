/**
 *
 */
package ua.com.fielden.platform.entity.validation.test_entities;

import ua.com.fielden.platform.entity.annotation.IsProperty;

/**
 * Class for testing of domain validators registration.
 * 
 * @author Yura
 */
public class SubClass1SubClass extends SubClass1 {

    private static final long serialVersionUID = 3028077876302006244L;

    /**
     * Getters/setters are not required for test
     */
    @IsProperty
    @SuppressWarnings("unused")
    private Integer intProp;

}
