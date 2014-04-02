package ua.com.fielden.platform.example.dnd.classes;

import ua.com.fielden.platform.example.entities.Bogie;

public class BogieDragModel extends AbstractBogieWidget<WheelsetDragModel> {

    private Bogie bogie;

    @Override
    public boolean canDrag() {
        return true;
    }

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public BogieDragModel(final Bogie bogie) {
        super(WheelsetDragModel.class, "bogie");
        setBogie(bogie);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Bogie getRotable() {
        return bogie;
    }

    @Override
    protected boolean canAccept(final int slotIndex, final WheelsetDragModel widgetToTest) {
        return true;
    }

    public void setBogie(final Bogie bogie) {
        this.bogie = bogie;
    }

}
