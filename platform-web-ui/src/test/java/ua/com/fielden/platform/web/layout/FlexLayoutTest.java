package ua.com.fielden.platform.web.layout;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.web.interfaces.ILayout.Device;

public class FlexLayoutTest {

    @Test
    public void test_flex_layout_matching() {
	final FlexLayout flexLayout = new FlexLayout();
	flexLayout.whenMedia(Device.DESKTOP).set("[[[],[],[],[]],[[],[],[],[]]]")//
		.whenMedia(Device.TABLET).set("[[[],[],[]],[[],[],[]],[[],[]]]")//
		.whenMedia(Device.PHONE).set("[[[],[]],[[],[]],[[],[]],[[],[]]]");
	final DomElement flexRendered = flexLayout.render();
	assertEquals("the number of children is incorrect", 0, flexRendered.childCount());
	assertEquals("the desktop layout constraints is incorrect", "[[[],[],[],[]],[[],[],[],[]]]", flexRendered.getAttr("whenDesktop").value);
	assertEquals("the tablet layout constraints is incorrect", "[[[],[],[]],[[],[],[]],[[],[]]]", flexRendered.getAttr("whenTablet").value);
	assertEquals("the phone layout constraints is incorrect", "[[[],[]],[[],[]],[[],[]],[[],[]]]", flexRendered.getAttr("whenPhone").value);
    }
}
