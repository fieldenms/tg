package ua.master.com.fielden.platform.sample.domain.tgpublishedyearly;

import java.util.Map;

import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.sample.domain.TgPublishedYearly;
import ua.com.fielden.platform.swing.ei.editors.development.IPropertyEditor;
import ua.com.fielden.platform.swing.view.BaseNotifPanel;
import ua.com.fielden.platform.swing.view.ViewToolbars;

import com.google.inject.Inject;

/**
 * Master view for entity {@link TgPublishedYearly}.
 *
 * @author Developers
 *
 */
public class TgPublishedYearlyMasterView extends BaseNotifPanel<TgPublishedYearlyMasterModel> {


    @Inject
    public TgPublishedYearlyMasterView(final TgPublishedYearlyMasterModel model) {
        super(model.defaultTitle(), model);
        final JPanel componentsPanel = new JPanel(new MigLayout("insets 0", "[:50:][grow,fill,:250:]", "[c]"));
        final Map<String, IPropertyEditor> editors = model.getEditors();

        // componentsPanel has one column property editor layout; add necessary properties below
        addAndWrap(componentsPanel, editors, "year");
        addAndWrap(componentsPanel, editors, "qty");
        addAndWrap(componentsPanel, editors, "author");

        // put components panel and toolbar panel together
        final JPanel mainPanel = new JPanel(new MigLayout("", "[grow, fill]", "[fill]20[]"));
        mainPanel.add(componentsPanel, "wrap");
        mainPanel.add(ViewToolbars.NO_DELETE_ACTION.toolbar(model));
        add(mainPanel);
        // bind this view and the provided model
        model.setView(this);
    }

    @Override
    public String getInfo() {
        return "<html>" + "<h3>Main</h3>" + "A facility for modifying" + TitlesDescsGetter.getEntityTitleAndDesc(TgPublishedYearly.class).getKey() + " instances." + "</html>";
    }

}