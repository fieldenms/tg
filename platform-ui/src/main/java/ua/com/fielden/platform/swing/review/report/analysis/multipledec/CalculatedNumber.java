package ua.com.fielden.platform.swing.review.report.analysis.multipledec;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import javax.swing.event.EventListenerList;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reportquery.AnalysisModelChangedEvent;
import ua.com.fielden.platform.reportquery.AnalysisModelChangedListener;
import ua.com.fielden.platform.swing.review.report.analysis.chart.ICategoryAnalysisDataProvider;

public abstract class CalculatedNumber<T extends AbstractEntity<?>> {

    private final String caption;

    private final EventListenerList listenerList;

    private final NumberFormat numberFormat;

    private Number number;

    @SuppressWarnings("unchecked")
    public CalculatedNumber(final String caption, final ICategoryAnalysisDataProvider<Comparable<?>, Number, List<T>> chartModel, final CalculatedNumber<T>... calculatedNumbers){
	this.caption = caption;
	this.number = 0;

	this.numberFormat = new DecimalFormat("#,##0.00");
	numberFormat.setRoundingMode(RoundingMode.HALF_UP);
	this.listenerList = new EventListenerList();

	if(chartModel != null){
	    chartModel.addAnalysisModelChangedListener(createChartModelChangedListener());
	}

	for(final CalculatedNumber<T> number : calculatedNumbers){
	    number.addPropertyChangeListener(createNumberPropertyChangeListener());
	}
    }

    @SuppressWarnings("unchecked")
    public CalculatedNumber(final String caption, final CalculatedNumber<T>... calculatedNumbers){
	this(caption, null, calculatedNumbers);
    }

    private PropertyChangeListener createNumberPropertyChangeListener() {
	return new PropertyChangeListener() {

	    @Override
	    public void propertyChange(final PropertyChangeEvent evt) {
		setNumber(calculate());
	    }
	};
    }

    private AnalysisModelChangedListener createChartModelChangedListener() {
	return new AnalysisModelChangedListener() {

	    @Override
	    public void cahrtModelChanged(final AnalysisModelChangedEvent event) {
		setNumber(calculate());
	    }
	};
    }

    abstract protected Number calculate();

    public final void setNumber(final Number number) {
	final Number oldNumber = this.number;
	this.number = number;
	notifyNumberChanged(new PropertyChangeEvent(this, "number", oldNumber, number));
    }

    public Number getNumber() {
	return number;
    }

    public String getCaption() {
	return caption;
    }

    public String getConvertedNumber(){
	return numberFormat.format(number);
    }

    @Override
    public String toString() {
	return caption + ": " + numberFormat.format(number);
    }

    public void addPropertyChangeListener(final PropertyChangeListener l) {
	listenerList.add(PropertyChangeListener.class, l);
    }

    public void removePropertyChangeListener(final PropertyChangeListener l){
	listenerList.remove(PropertyChangeListener.class, l);
    }

    private void notifyNumberChanged(final PropertyChangeEvent event){
	// Guaranteed to return a non-null array
	final Object[] listeners = listenerList.getListenerList();
	// Process the listeners last to first, notifying
	// those that are interested in this event
	for (int i = listeners.length-2; i>=0; i-=2) {
	    if (listeners[i]==PropertyChangeListener.class) {
		((PropertyChangeListener)listeners[i+1]).propertyChange(event);
	    }
	}

    }

    /**
     * Calculates the average for the specified series of values in the given {@link ICategoryAnalysisDataProvider} instance.
     *
     * @param series
     * @param chartModel
     * @return
     */
    public static <T extends AbstractEntity<?>> BigDecimal calculateAvg(final String series, final ICategoryAnalysisDataProvider<Comparable<?>, Number, List<T>> chartModel) {
	final int categoryCount = chartModel.getCategoryDataEntryCount();
	if (categoryCount != 0) {
	    return calculateTotal(series, chartModel).divide(BigDecimal.valueOf(categoryCount), RoundingMode.HALF_UP);
	} else {
	    return BigDecimal.ZERO;
	}
    }

    /**
     * Calculates the total for specified serial of values in the given {@link ICategoryAnalysisDataProvider} instance.
     *
     * @param series
     * @param chartModel
     * @return
     */
    public static <T extends AbstractEntity<?>> BigDecimal calculateTotal(final String series, final ICategoryAnalysisDataProvider<Comparable<?>, Number, List<T>> chartModel) {
	final int categoryCount = chartModel.getCategoryDataEntryCount();
	BigDecimal sum = BigDecimal.ZERO;
	for(int categoryIndex = 0; categoryIndex < categoryCount; categoryIndex++){
	    sum = sum.add(new BigDecimal(chartModel.getAggregatedDataValue(categoryIndex, series).toString()));
	}
	return sum;
    }

}
