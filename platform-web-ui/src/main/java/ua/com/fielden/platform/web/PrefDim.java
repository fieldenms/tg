package ua.com.fielden.platform.web;

import org.apache.commons.lang.StringUtils;

/**
 * A structure to specify preferred dimensions (width and height) for Web UI vies.
 * Both <code>width</code> and <code>height</code> are strings to support JS expressions.
 * There are two separate factory methods -- one for making dimensions based on integer values and one for JS expressions.
 * <p> 
 * The use of JS expression is necessary to be able to compute dimensions at the client side dynamically.
 * For example, one could use <code>document.body</code> as the basis for preferred dimension with 50 and 100 pixels offset for width and height respectively.
 * <pre>
 *    final PrefDim dim = mkDim("document.body.clientWidth - 50", "document.body.clientHeight - 100");
 * <pre> 
 * <p>
 * Property <code>unit</code> provides a way to specify the unit of measure that is used for dimensions. At this stage it could either be "px" (default) or "%".
 * This comes handy when there is a need to specify dimensions relative to a container.
 * 
 * @author TG Team
 *
 */
public class PrefDim {
	
	/** Unit of measure such as pixels or percent. */
	public enum Unit {
		PX("px"), PRC("%");
		
		public final String value;
		
		private Unit(final String value) {
			this.value = value;
		}
	}
	
	public final String width;
	public final String height;
	public final Unit unit;
	
	private PrefDim(final String width, final String height, final Unit unit) {
		if (StringUtils.isEmpty(width) || StringUtils.isEmpty(height)) {
			throw new IllegalArgumentException("Both width and height should contain values.");
		}
		this.width = width;
		this.height = height;
		this.unit = unit;
	}
	
	public static PrefDim mkDim(final String widthExpr, final String heightExpr, final Unit unit) {
		return new PrefDim(widthExpr, heightExpr, unit);
	}
	
	public static PrefDim mkDim(final String widthExpr, final String heightExpr) {
		return new PrefDim(widthExpr, heightExpr, Unit.PX);
	}
	
	public static PrefDim mkDim(final int width, final int height, final Unit unit) {
		return new PrefDim(String.valueOf(width), String.valueOf(height), unit);
	}
	
	public static PrefDim mkDim(final int width, final int height) {
		return new PrefDim(String.valueOf(width), String.valueOf(height), Unit.PX);
	}
}
