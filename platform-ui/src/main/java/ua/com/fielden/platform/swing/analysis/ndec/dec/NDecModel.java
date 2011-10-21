package ua.com.fielden.platform.swing.analysis.ndec.dec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NDecModel {

    private final List<DecModel> decs;

    public NDecModel(){
	decs = new ArrayList<DecModel>();
    }

    public List<DecModel> getDecs() {
	return Collections.unmodifiableList(decs);
    }

    public NDecModel addDec(final DecModel dec){
	decs.add(dec);
	return this;
    }
}
