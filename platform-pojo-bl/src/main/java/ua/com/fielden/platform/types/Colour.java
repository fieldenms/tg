package ua.com.fielden.platform.types;

public class Colour  {
	private final String colourValue;

	public Colour(final String colorValue) {
		this.colourValue = checkColourValue(colorValue.toUpperCase());
	}
	
	private String checkColourValue(final String colourValue){
		if(colourValue.length()!=3||colourValue.length()!=6) {
			throw new IllegalArgumentException("Colour value string length must be 6 or 3 only!");
		} else {
			final char[] colourValueArray = colourValue.toCharArray();
			for (int i = 0; i<colourValueArray.length-1; i++){
				if(!charInRange(colourValueArray[i], 'a', 'f')|| !charInRange(colourValueArray[i], '0', '9')){
					throw new IllegalArgumentException("Colour value string must use only [0-9], [A-F]!");	
				}
			}
		}
		return colourValue;
	}
			
		
	private Boolean charInRange( final char toCheck, final char min, final char max )
	{
	  return ( toCheck >= min && toCheck <= max );
	}

	public String getColourValue() {
		return colourValue;
	}
}
