package ua.com.fielden.platform.example.dnd;

import java.awt.Color;

import javax.swing.SwingUtilities;

import ua.com.fielden.converter.SvgConverter;
import ua.com.fielden.platform.example.dnd.classes.BogieNodeTest;
import ua.com.fielden.platform.example.dnd.classes.PartModel;
import ua.com.fielden.platform.example.dnd.classes.TestFrame;
import ua.com.fielden.platform.example.dnd.classes.WagonNodeTest;
import ua.com.fielden.platform.example.dnd.classes.WheelsetNodeTest;
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PPath;

public class WidgetFactory {

    private static PartModel bogie;
    private static PartModel[] bogieParts = new PartModel[8];

    private static PartModel[] wheelsetParts = new PartModel[5];

    public static final double BOGIE_WIDTH;
    public static final double BOGIE_HEIGHT;

    public static final double WHEELSET_WIDTH;
    public static final double WHEELSET_HEIGHT;

    public static final int NUM_OF_BOGIE_PARTS;
    public static final int NUM_OF_WHEELSET_PARTS;

    static {
	SvgConverter converter = new SvgConverter("resources/rotables.svg");
	bogie = (PartModel) converter.getPNodeById("bogie", SvgConverter.LOCAL_TRANSFORMATION, PartModel.class, false, 0);
	PPath bogieBounds = (PPath) converter.getPNodeById("bogiebounds", SvgConverter.LOCAL_TRANSFORMATION, PPath.class, false, 0);
	BOGIE_WIDTH = bogieBounds.getWidth();
	BOGIE_HEIGHT = bogieBounds.getHeight();
	double translateX = 0 - bogieBounds.getX();
	double translateY = 0 - bogieBounds.getY();
	for (int partCounter = 0; partCounter < 8; partCounter++) {
	    PartModel part = (PartModel) converter.getPNodeById("part" + Integer.toString(partCounter + 1), SvgConverter.LOCAL_TRANSFORMATION, PartModel.class, false, 0);
	    part.setOriginColor((Color) part.getPaint());
	    part.setOriginStrokeColor((Color) part.getStrokePaint());
	    part.setOriginStroke(part.getStroke());
	    translateBounds(part, translateX, translateY);
	    bogieParts[partCounter] = part;
	}
	bogie.setOriginColor((Color) bogie.getPaint());
	bogie.setOriginStrokeColor((Color) bogie.getStrokePaint());
	bogie.setOriginStroke(bogie.getStroke());
	translateBounds(bogie, translateX, translateY);
	String ids[] = new String[5];
	ids[0] = "smallbottom";
	ids[1] = "smalltop";
	ids[2] = "bigbottom";
	ids[3] = "bigtop";
	ids[4] = "axel";
	PPath wheelsetBounds = (PPath) converter.getPNodeById("wheelsetbounds", SvgConverter.LOCAL_TRANSFORMATION, PPath.class, false, 0);
	WHEELSET_WIDTH = wheelsetBounds.getWidth();
	WHEELSET_HEIGHT = wheelsetBounds.getHeight();
	translateX = 0 - wheelsetBounds.getX();
	translateY = 0 - wheelsetBounds.getY();
	for (int partCounter = 0; partCounter < 5; partCounter++) {
	    PartModel part = (PartModel) converter.getPNodeById(ids[partCounter], SvgConverter.LOCAL_TRANSFORMATION, PartModel.class, false, 0);
	    part.setOriginColor((Color) part.getPaint());
	    part.setOriginStrokeColor((Color) part.getStrokePaint());
	    part.setOriginStroke(part.getStroke());
	    wheelsetParts[partCounter] = part;
	    translateBounds(part, translateX, translateY);
	}
	NUM_OF_BOGIE_PARTS = 8;
	NUM_OF_WHEELSET_PARTS = 5;
    }

    private WidgetFactory() {

    }

    /*
     * public PNode createWagon(int numOfSpots) { PComposite composite = new PComposite(); PPath leftCopy = (PPath) leftSegment.clone(); composite.addChild(leftCopy); boolean
     * isSlotedNow = true; int numOfAddedSlots = 0; while (numOfAddedSlots != numOfSpots) { Rectangle2D bounds = leftCopy.getBounds(); if (isSlotedNow) { PPath slotedCopy = (PPath)
     * slotedSegment.clone(); slotedCopy.setOffset(new Point2D.Double(bounds.getX() + bounds.getWidth(), bounds.getY())); composite.addChild(slotedCopy); numOfAddedSlots++;
     * isSlotedNow = false; } else { PPath emptyCopy = (PPath) emptySegment.clone(); emptyCopy.setOffset(new Point2D.Double(bounds.getX() + bounds.getWidth(), bounds.getY()));
     * composite.addChild(emptyCopy); isSlotedNow = true; } } Rectangle2D bounds = leftCopy.getBounds(); PPath rightCopy = (PPath) rightSegment.clone(); rightCopy.setOffset(new
     * Point2D.Double(bounds.getX() + bounds.getWidth(), bounds.getY())); composite.addChild(rightCopy); return composite; }
     */

    private static void translateBounds(PNode bogie2, double d, double e) {
	bogie2.setX(bogie2.getX() + d);
	bogie2.setY(bogie2.getY() + e);
	for (int childrenCounter = 0; childrenCounter < bogie2.getChildrenCount(); childrenCounter++) {
	    translateBounds(bogie2.getChild(childrenCounter), d, e);
	}
    }

    public static PartModel getBogie() {
	return (PartModel) bogie.clone();
    }

    public static PartModel getBogiePart(int index) {
	if ((index >= 0) && (index < 8)) {
	    return (PartModel) bogieParts[index].clone();
	} else {
	    throw (new IndexOutOfBoundsException("there are only eight parts in the bogieParts array"));
	}
    }

    public static PartModel getWheelsetPart(int index) {
	if ((index >= 0) && (index < 5)) {
	    return (PartModel) wheelsetParts[index].clone();
	} else {
	    throw (new IndexOutOfBoundsException("there are only five parts in the wheelsetParts array"));
	}
    }

    public static void main(final String[] args) {
	SwingUtilities.invokeLater(new Runnable() {
	    public void run() {
		final TestFrame frame = new TestFrame() {
		    private static final long serialVersionUID = 1L;

		    @Override
		    public void initWidgets(final PLayer nodeLayer) {
			WagonNodeTest wagon = new WagonNodeTest();
			BogieNodeTest bogie1 = new BogieNodeTest();
			BogieNodeTest bogie2 = new BogieNodeTest();
			WheelsetNodeTest wheelSet1 = new WheelsetNodeTest();
			WheelsetNodeTest wheelSet2 = new WheelsetNodeTest();
			WheelsetNodeTest wheelSet3 = new WheelsetNodeTest();
			WheelsetNodeTest wheelSet4 = new WheelsetNodeTest();
			nodeLayer.addChild(wagon);
			nodeLayer.addChild(bogie1);
			nodeLayer.addChild(bogie2);
			nodeLayer.addChild(wheelSet1);
			nodeLayer.addChild(wheelSet2);
			nodeLayer.addChild(wheelSet3);
			nodeLayer.addChild(wheelSet4);
			wagon.attach(null, bogie1, false);
			wagon.attach(null, bogie2, false);
			bogie1.attach(null, wheelSet1, false);
			bogie1.attach(null, wheelSet2, false);
			bogie2.attach(null, wheelSet3, false);
			bogie2.attach(null, wheelSet4, false);
			getCanvas().getCamera().translateView(-100, -100);
			/*
			 * nodeLayer.addChild(new WagonNodeTest()); nodeLayer.addChild(new BogieNodeTest()); nodeLayer.addChild(new BogieNodeTest()); nodeLayer.addChild(new
			 * WheelsetNodeTest()); nodeLayer.addChild(new WheelsetNodeTest()); nodeLayer.addChild(new WheelsetNodeTest()); nodeLayer.addChild(new WheelsetNodeTest());
			 */

		    }

		};
		frame.setTitle("Sendbox: Abstract Bogie Widget");
		frame.setSize(1000, 600);
		frame.setVisible(true);
	    }
	});
    }
}
