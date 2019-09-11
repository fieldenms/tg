package ua.com.fielden.platform.web.centre.api.dynamicprops;

/**
 * A contract that specifies the tooltip for column header.
 *
 * @author TG Team
 *
 */
public interface IDynamicPropBuilderWithDesc extends IDynamicPropBuilderWidth {

    IDynamicPropBuilderWidth descripton(String desc);
}
