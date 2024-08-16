package ua.com.fielden.platform.web.centre.api.dynamic_columns;

/**
 * A contract that specifies the name of a property that holds the group name.
 *
 * @author TG Team
 *
 */
public interface IDynamicColumnBuilderGroupProp {

    IDynamicColumnBuilderDisplayProp withGroupProp(final CharSequence groupProp);

}
