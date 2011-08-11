package ua.com.fielden.platform.test.domain.entities;

import java.awt.Color;

/**
 * Represents the concept of rotable statuses.
 *
 * @author 01es
 *
 */
public enum RotableStatus {
    S("Serviceable", null), U("Unserviceable", new Color(130, 130, 130)), R("Repairable", new Color(105, 100, 188)), RW("Repairable under warranty", new Color(125, 255, 125));

    private final String desc;
    private final Color color;

    RotableStatus(final String desc, final Color color) {
	this.desc = desc;
	this.color = color;
    }

    public Color getColor() {
	return color;
    }

    public String getDesc() {
        return desc;
    }
}
