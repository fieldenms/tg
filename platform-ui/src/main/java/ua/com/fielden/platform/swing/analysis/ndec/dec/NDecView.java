package ua.com.fielden.platform.swing.analysis.ndec.dec;

import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.swing.analysis.ndec.IAnalysisDoubleClickListener;
import ua.com.fielden.platform.swing.view.BasePanel;

public class NDecView extends BasePanel{

    private static final long serialVersionUID = -4573644376854599911L;

    //private final NDecModel model;

    private final List<DecView> decViews = new ArrayList<DecView>();

    public NDecView(final NDecModel model){
	super(new MigLayout("fill, insets 0"));
	//this.model = model;
	final JPanel decPanel = new JPanel(new MigLayout("fill, insets 0","[l][c]","[fill, grow]"));
	for(final DecModel decModel : model.getDecs()){
	    final DecView decView = new DecView(decModel);
	    decViews.add(decView);
	    decPanel.add(stubLablel());
	    decPanel.add(getChartTitle(decModel), "wrap");
	    decPanel.add(decView.getCalculatedNumberPanel());
	    decPanel.add(decView.getChartPanel(), "wrap, growx, push");
	}
	add(new JScrollPane(decPanel), "grow");
    }

    private JLabel stubLablel(){
	return new JLabel("");
    }

    private JLabel getChartTitle(final DecModel decModel){
	final JLabel label = new JLabel(decModel.getChartName());
	label.setFont(new Font("SansSerif", Font.BOLD, 18));
	label.setHorizontalTextPosition(JLabel.CENTER);
	return label;
    }


    @Override
    public String getInfo() {
	return "Multiple dec view";
    }

    public void addAnalysisDoubleClickListener(final IAnalysisDoubleClickListener l){
	for(final DecView dec : decViews){
	    dec.getChartPanel().addAnalysisDoubleClickListener(l);
	}
    }

    public void removeAnalysisDoubleClickListener(final IAnalysisDoubleClickListener l){
	for(final DecView dec : decViews){
	    dec.getChartPanel().removeAnalysisDoubleClickListener(l);
	}
    }

}
