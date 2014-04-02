package ua.com.fielden.platform.swing.categorychart;

import java.util.EventObject;

/**
 * The event that represent current chart type changes in the {@link SwitchChartsModel} instance.
 * 
 * @author TG Team
 * 
 */
public class ChartTypeChangedEvent extends EventObject {

    private static final long serialVersionUID = 8427595520295666263L;

    public ChartTypeChangedEvent(final SwitchChartsModel<?, ?> source) {
        super(source);
    }

    @Override
    public SwitchChartsModel<?, ?> getSource() {
        return (SwitchChartsModel<?, ?>) super.getSource();
    }

}
