package ua.com.fielden.platform.web.centre.api.dynamicprops;

/**
 * A contract that adds new dynamic property for specified group
 *
 * @author TG Team
 *
 */
public interface IDynamicPropBuilderAddProp {

    IDynamicPropBuilderWithTitle addProp(final String keyPropValue);
}
