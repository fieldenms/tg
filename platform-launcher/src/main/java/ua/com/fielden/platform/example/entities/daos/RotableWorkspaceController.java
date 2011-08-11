package ua.com.fielden.platform.example.entities.daos;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.dao.annotations.Transactional;
import ua.com.fielden.platform.example.entities.Bogie;
import ua.com.fielden.platform.example.entities.BogieSlot;
import ua.com.fielden.platform.example.entities.IBogieDao;
import ua.com.fielden.platform.example.entities.IRotableWorkspaceController;
import ua.com.fielden.platform.example.entities.IWheelsetDao;
import ua.com.fielden.platform.example.entities.Rotable;
import ua.com.fielden.platform.example.entities.RotableWorkspace;
import ua.com.fielden.platform.example.entities.Wheelset;

import com.google.inject.Inject;

public class RotableWorkspaceController implements IRotableWorkspaceController {

    private final IBogieDao bogieDao;
    private final IWheelsetDao wheelsetDao;

    @Inject
    protected RotableWorkspaceController(final IBogieDao bogieDao, final IWheelsetDao wheelsetDao) {
	this.bogieDao = bogieDao;
	this.wheelsetDao = wheelsetDao;
    }

    @SuppressWarnings("unchecked")
    @Transactional
    @Override
    public List<Rotable> save(final RotableWorkspace rotableWorkspace) {
	final List<Rotable> inWorkshop = new ArrayList<Rotable>();

	for (final Bogie bogie : rotableWorkspace.getBogies()) {
	    if (bogie.getProperty("location").isChangedFromOriginal()) {
		bogie.setLocation(bogie.getLocation());
		bogieDao.save(bogie);
	    }
	    if (rotableWorkspace.getWorkshop().equals(bogie.getLocation())) {
		inWorkshop.add(bogie);
	    }
	}

	for (final Wheelset wheelset : rotableWorkspace.getWheelsets()) {
	    if (wheelset.getProperty("location").isChangedFromOriginal()) {
		// in case where wheelset has been fitted or defitted from bogie slot
		if (wheelset.getLocation() instanceof BogieSlot) {

		    final Bogie bogie = ((BogieSlot) wheelset.getLocation()).getBogie();
		    if (inWorkshop.contains(bogie)) {
			wheelset.setLocation(bogie.getLocation());
			inWorkshop.add(wheelset);
		    } else {
			wheelset.setLocation(wheelset.getLocation());
		    }
		} else {
		    wheelset.setLocation(wheelset.getLocation());
		    if (rotableWorkspace.getWorkshop().equals(wheelset.getLocation())) {
			inWorkshop.add(wheelset);
		    }
		}
		wheelsetDao.save(wheelset);
	    } else {
		// in case where wheelset has been added to workspace (being in workshop) and stayed there or has been defitted as part of bogie defittment
		if (wheelset.getLocation() instanceof BogieSlot) {

		    final Bogie bogie = ((BogieSlot) wheelset.getLocation()).getBogie();
		    if (inWorkshop.contains(bogie)) {
			wheelset.setLocation(bogie.getLocation());
			inWorkshop.add(wheelset);
		    }
		    wheelsetDao.save(wheelset);
		} else {
		    if (rotableWorkspace.getWorkshop().equals(wheelset.getLocation())) {
			inWorkshop.add(wheelset);
		    }
		}
	    }
	}

	return inWorkshop;
    }
}
