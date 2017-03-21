package lattice;

import java.util.ArrayList;

/**
 * @author Zheng ZHANG
 */

public class Property {
	
	private int number;
	private String shorterName;
	// Distinct values of this property.
	private ArrayList<String> objects;
	
	
	public Property(int number, String shorterName) {
		this.number = number;
		this.shorterName = shorterName;
	}
	
	public Property(int number) {
		this.number = number;
	}

	public int getNumber() {
		return number;
	}



	@Override
	public String toString() {
		return this.shorterName;
	}
	
	// If two Properties number are identical, we say that they are equal. We don't care other instances values.
	@Override
	public boolean equals(Object other){
	    if (other == null) return false;
	    if (other == this) return true;
	    if (!(other instanceof Property))return false;
	    Property otherProperty = (Property)other;
	  
	    if(this.number == otherProperty.number) {
	    	return true;
	    } else {
	    	return false;
	    }
	}
	
}
