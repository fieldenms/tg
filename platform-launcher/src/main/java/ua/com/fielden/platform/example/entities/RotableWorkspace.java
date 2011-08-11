package ua.com.fielden.platform.example.entities;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to hold all bogie and wheelset instances that are visualised in the workspace tabsheet.
 *
 * @author nc
 *
 */
public class RotableWorkspace {

    private final WagonWithRotables wagonWithRotables;
    private final Workshop workshop;

    private final List<Bogie> bogies = new ArrayList<Bogie>();
    private final List<Wheelset> wheelsets = new ArrayList<Wheelset>();


    public void addBogie(final Bogie bogie) {
	bogies.add(bogie);
    }

    public void addWheelset(final Wheelset wheelset) {
	wheelsets.add(wheelset);
    }

    public RotableWorkspace(final WagonWithRotables wagonWithRotables, final Workshop workshop) {
	this.wagonWithRotables = wagonWithRotables;
	this.workshop = workshop;

	for (final BogieWithRotables bogieWithRotables : wagonWithRotables.getBogiesRotables()) {
	    for (final Wheelset wheelset : bogieWithRotables.getWheelsets()) {
		wheelsets.add(wheelset);
	    }
	    bogies.add(bogieWithRotables.getBogie());

	}
    }

    public List<Bogie> getBogies() {
	return bogies;
    }

    public List<Wheelset> getWheelsets() {
	return wheelsets;
    }

    public WagonWithRotables getWagonWithRotables() {
        return wagonWithRotables;
    }

    public Workshop getWorkshop() {
        return workshop;
    }
}
