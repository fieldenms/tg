package ua.com.fielden.platform.web.centre.api.dynamic_columns;

/**
 * A contract that specifies the name of a property that holds the value to be displayed.
 *
 * @author TG Team
 *
 */
public interface IDynamicColumnBuilderDisplayProp {

    IDynamicColumnBuilderWithTooltipProp withDisplayProp(final CharSequence displayProp);

}
