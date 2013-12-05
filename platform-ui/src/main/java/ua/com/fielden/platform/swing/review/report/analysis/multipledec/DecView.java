package ua.com.fielden.platform.swing.review.report.analysis.multipledec;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reportquery.AnalysisModelChangedEvent;
import ua.com.fielden.platform.reportquery.AnalysisModelChangedListener;
import ua.com.fielden.platform.swing.utils.DummyBuilder;

public class DecView<T extends AbstractEntity<?>>{

    private final DecModel<T> model;

    private final JPanel calculatedNumberPanel;
    private final DecChartPanel chartPanel;

    public DecView(final DecModel<T> model) {
	this.model = model;

	if(model.getCalcValuesNumber() > 0){
	    String rowConstraints= "";
	    for(int calcNumberIndex = 0; calcNumberIndex < model.getCalcValuesNumber()-1; calcNumberIndex++){
		rowConstraints += "[t]";
	    }
	    calculatedNumberPanel = new JPanel(new MigLayout("fill, insets 5", "[l][r]", rowConstraints + (model.getCalcValuesNumber() == 1 ? "[t]":"[t, grow]")));
	    for(final CalculatedNumber<T> number : model.getCalcNumbers()){
		calculatedNumberPanel.add(DummyBuilder.label(number.getCaption()+": ", new Color(23, 92, 154)));
		final JLabel label = new JLabel(number.getConvertedNumber());
		number.addPropertyChangeListener(createNumberChangeListener(label));
		calculatedNumberPanel.add(label, "wrap");

	    }
	}else{
	    calculatedNumberPanel = new JPanel();
	}
	chartPanel = new DecChartPanel(model.createChart());
	model.getChartModel().addAnalysisModelChangedListener(createChartModelChangedListener(chartPanel));
    }

    public JPanel getCalculatedNumberPanel(){
	return calculatedNumberPanel;
    }

    public DecChartPanel getChartPanel() {
	return chartPanel;
    }

    private AnalysisModelChangedListener createChartModelChangedListener(final DecChartPanel chartPanel) {
	return new AnalysisModelChangedListener() {

	    @Override
	    public void cahrtModelChanged(final AnalysisModelChangedEvent event) {
		chartPanel.setChart(getModel().createChart());
	    }
	};
    }

    private PropertyChangeListener createNumberChangeListener(final JLabel label) {
	return new PropertyChangeListener() {

	    @SuppressWarnings("unchecked")
	    @Override
	    public void propertyChange(final PropertyChangeEvent evt) {
		label.setText(((CalculatedNumber<T>)evt.getSource()).getConvertedNumber());
	    }
	};
    }

    public DecModel<T> getModel() {
	return model;
    }

}
