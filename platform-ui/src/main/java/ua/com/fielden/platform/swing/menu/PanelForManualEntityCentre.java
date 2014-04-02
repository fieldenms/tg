package ua.com.fielden.platform.swing.menu;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.model.DefaultUiModel;
import ua.com.fielden.platform.swing.review.report.centre.configuration.ManualCentreConfigurationView;
import ua.com.fielden.platform.swing.view.BaseNotifPanel;

public class PanelForManualEntityCentre<T extends AbstractEntity<?>> extends BaseNotifPanel<DefaultUiModel> {

    private static final long serialVersionUID = -4713076113440549853L;

    private final ManualCentreConfigurationView<T> manualEntityCentre;

    private final String description;

    @SuppressWarnings("unchecked")
    public PanelForManualEntityCentre(final String caption, final String description, final ManualCentreConfigurationView<T> manualEntityCentre) {
        super(caption, new DefaultUiModel(true));
        this.description = description;
        this.manualEntityCentre = manualEntityCentre;
        getHoldingPanel().removeAll();
        getHoldingPanel().setLayout(new MigLayout("fill, insets 0", "[fill, grow]", "[c, grow, fill]"));
        add(manualEntityCentre.getProgressLayer());
        getModel().setView(this);
    }

    @Override
    public void buildUi() {
        manualEntityCentre.open();
    }

    @Override
    public String getInfo() {
        return description;
    }

    public ManualCentreConfigurationView<T> getManualEntityCentre() {
        return manualEntityCentre;
    }

}
