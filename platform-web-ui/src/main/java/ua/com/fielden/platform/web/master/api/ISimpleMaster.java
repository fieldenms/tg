package ua.com.fielden.platform.web.master.api;

import ua.com.fielden.platform.basic.IValueMatcher;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.sample.domain.TgWorkOrder;
import ua.com.fielden.platform.web.interfaces.ILayout.Device;
import ua.com.fielden.platform.web.interfaces.ILayout.Orientation;
import ua.com.fielden.platform.web.master.api.helpers.IPropertySelector;

/**
 * This contract is an entry point for Simple Master API -- an embedded domain specific language for constructing simple entity masters.
 *
 * @see <a href="https://github.com/fieldenms/tg/wiki/Web-UI-Design:-Entity-Masters">Entity Masters Wiki</a>
 *
 * @author TG Team
 *
 */
public interface ISimpleMaster {
    <T extends AbstractEntity<?>> IPropertySelector<T> forEntity(Class<T> type);

    // TODO Needs to be removed in a fullness of time. This method exists here purely to demonstrate API fluency as part of the development.
    public static void apiExample(final ISimpleMaster sm) {
        sm.forEntity(TgWorkOrder.class)
                .addProp("vehicle").asAutocompleter().byDesc().also()
                .addProp("status").asAutocompleter().withMatcher(IValueMatcher.class).byDescOnly()
                .setLayoutFor(Device.DESKTOP, Orientation.LANDSCAPE, "[][flex]")
                .setLayoutFor(Device.TABLET, Orientation.LANDSCAPE, "[][flex]")
                .setLayoutFor(Device.TABLET, Orientation.PORTRAIT, "[][flex]")
                .done();
    }
}
