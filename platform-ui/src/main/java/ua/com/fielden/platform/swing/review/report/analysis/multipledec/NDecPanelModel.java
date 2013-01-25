package ua.com.fielden.platform.swing.review.report.analysis.multipledec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;

public class NDecPanelModel<T extends AbstractEntity<?>> {

    private final Map<String, DecModel<T>> decsMap;

    private final List<String> orderedProperties;

    public NDecPanelModel(){
	decsMap = new HashMap<>();
	orderedProperties = new ArrayList<>();
    }

    public DecModel<T> getDec(final int index) {
	return decsMap.get(orderedProperties.get(index));
    }

    public int getDecCount(){
	return orderedProperties.size();
    }

    public NDecPanelModel<T> setDecs(final List<String> orderedProperties, final Map<String, DecModel<T>> decsMap){
	this.orderedProperties.clear();
	if(orderedProperties != null){
	    this.orderedProperties.addAll(orderedProperties);
	}
	this.decsMap.clear();
	if(decsMap != null){
	    this.decsMap.putAll(decsMap);
	}
	return this;
    }
}
