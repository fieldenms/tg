package ua.com.fielden.platform.swing.review.report.analysis.multipledec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;

public class NDecPanelModel<T extends AbstractEntity<?>> {

    private final List<DecModel<T>> decs;

    public NDecPanelModel(){
	decs = new ArrayList<DecModel<T>>();
    }

    public DecModel<T> getDec(final int index) {
	return decs.get(index);
    }

    public int getDecCount(){
	return decs.size();
    }

    @SuppressWarnings("unchecked")
    public NDecPanelModel<T> setDecs(final DecModel<T>... dec){
	decs.clear();
	decs.addAll(Arrays.asList(dec));
	return this;
    }
}
