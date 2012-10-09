package ua.com.fielden.platform.equery.lifecycle;

import java.util.Arrays;
import java.util.List;

import org.joda.time.DateTime;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.model.SingleResultQueryModel;

/**
*
* This is a container class to be used for carrying around an instance of {@link SingleResultQueryModel}, dynamically generated types (as a list of byte arrays) and other lifecycle properties.
*
* @author TG Team
*
*/
public class LifecycleQueryContainer {

    private final SingleResultQueryModel<? extends AbstractEntity<?>> model;
    private final List<byte[]> binaryTypes;
    private final String propertyName;
    private final DateTime from, to;

    public LifecycleQueryContainer(final SingleResultQueryModel<? extends AbstractEntity<?>> model, //
	    final List<byte[]> binaryTypes,//
	    final String propertyName,//
	    final DateTime from,//
	    final DateTime to) {
	this.model = model;
	this.binaryTypes = binaryTypes;
	this.propertyName = propertyName;
	this.from = from;
	this.to = to;
    }

    public SingleResultQueryModel<? extends AbstractEntity<?>> getModel() {
	return model;
    }

    public List<byte[]> getBinaryTypes() {
	return binaryTypes;
    }

    /**
     * Returns the lifecycle property.
     *
     * @return
     */
    public String getPropertyName() {
	return propertyName;
    }

    public DateTime getFrom() {
	return from;
    }

    public DateTime getTo() {
	return to;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	for (final byte[] bytes : binaryTypes) {
	    result = prime * result + Arrays.hashCode(bytes);
	}
	result = prime * result + ((model == null) ? 0 : model.hashCode());
	result = prime * result + ((propertyName == null) ? 0 : propertyName.hashCode());
	result = prime * result + ((from == null) ? 0 : from.hashCode());
	result = prime * result + ((to == null) ? 0 : to.hashCode());
	return result;
    }

    @Override
    public boolean equals(final Object obj) {
	if (this == obj) {
	    return true;
	}
	if (!obj.getClass().equals(LifecycleQueryContainer.class)) {
	    return false;
	}

	final LifecycleQueryContainer that = (LifecycleQueryContainer) obj;
	if (binaryTypes == null) {
	    if (that.binaryTypes != null) {
		return false;
	    }
	} else if (binaryTypes.size() != that.binaryTypes.size()) {
	    return false;
	} else {
	    for (int index = 0; index < binaryTypes.size(); index++) {
		if (!Arrays.equals(binaryTypes.get(index), that.binaryTypes.get(index)) ) {
		    return false;
		}
	    }
	}
	if (model == null) {
	    if (that.model != null) {
		return false;
	    }
	} else if (!model.equals(that.model)) {
	    return false;
	}
	if(propertyName == null) {
	    if(that.propertyName != null){
		return false;
	    }
	}else if(!propertyName.equals(that.propertyName)){
	    return false;
	}
	if(from == null) {
	    if(that.from != null){
		return false;
	    }
	}else if(!from.equals(that.from)){
	    return false;
	}
	if(to == null) {
	    if(that.to != null){
		return false;
	    }
	}else if(!to.equals(that.to)){
	    return false;
	}
	return true;
    }

}
