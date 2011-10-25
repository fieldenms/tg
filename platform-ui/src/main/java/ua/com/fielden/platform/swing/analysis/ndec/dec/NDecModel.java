package ua.com.fielden.platform.swing.analysis.ndec.dec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NDecModel {

    private final List<DecModel> decs;

    public NDecModel(){
	decs = new ArrayList<DecModel>();
    }

    public DecModel getDec(final int index) {
	return decs.get(index);
    }

    public int getDecCount(){
	return decs.size();
    }

    public NDecModel setDecs(final DecModel... dec){
	decs.clear();
	decs.addAll(Arrays.asList(dec));
	return this;
    }
}
