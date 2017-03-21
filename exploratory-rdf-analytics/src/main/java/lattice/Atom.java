package lattice;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author Zheng ZHANG
 */

public class Atom {

	private ArrayList<Property> propertyCombination = new ArrayList<Property>();
	// Number of subjects belong to this atom (Subjects having properties of this atom).
	private int count;
	// Atom's level in the lattice.
	private int level;
	// In "links" part of the JSON file, "source" and "target" use nodeNumber to indicate atom.
	private int nodeNumber = -1;
	// Only used by function public void writeJSONFile(String filePath, Atom atom) in Lattice.java
	private boolean visited = false;
	public boolean isVisited() {
		return visited;
	}


	public void setVisited(boolean visited) {
		this.visited = visited;
	}


	private ArrayList<Atom> parents = new ArrayList<Atom>();
	private ArrayList<Atom> children = new ArrayList<Atom>();
	
	
	public Atom() {
		// TODO Auto-generated constructor stub
	}
	
	
	public Atom(ArrayList<Property> propertyCombination, int count) {
		super();
		this.propertyCombination = propertyCombination;
		this.level = propertyCombination.size();
		this.count = count;
	}
	
	
	public Atom(ArrayList<Integer> propertyCombinationNumbers) {
		ArrayList<Property> propertyCombinationTemp = new ArrayList<Property>();
		for(Integer number: propertyCombinationNumbers) {
			Property propetyTemp = new Property(number);
			propertyCombinationTemp.add(propetyTemp);
		}
		this.propertyCombination = propertyCombinationTemp;
	}

	
	public int getNodeNumber() {
		return nodeNumber;
	}


	public void setNodeNumber(int nodeNumber) {
		this.nodeNumber = nodeNumber;
	}
	
	public boolean hasSetNodeNumber() {
		return nodeNumber != -1;
	}


	public int getLevel() {
		return level;
	}


	public int getCount() {
		return count;
	}

	
	public ArrayList<Atom> getParents() {
		return parents;
	}
	
	public boolean hasParents() {
		return !parents.isEmpty();
	}


	public ArrayList<Atom> getChildren() {
		return children;
	}


	public ArrayList<Property> getPropertyCombination() {
		return propertyCombination;
	}

	
	public void setCount(int count) {
		this.count = count;
	}

	
	/*
	 * INPUT: [1, 2, 3] => Atom(p1-p2-p3)
	 * OUPUT: [[1, 2], [1, 3], [2, 3]] => Atom(p1-p2) Atom(p1-p3) Atom(p2-p3)
	 * ATTENTION: Don't call this function for the atom in level 0 (all).
	 */
	public ArrayList<ArrayList<Integer>> predictAllParents() {
		ArrayList<Integer> input = new ArrayList<Integer>();
		for(Property p: propertyCombination) {
			input.add(p.getNumber());
		}
		
		ArrayList<ArrayList<Integer>> result = new ArrayList<ArrayList<Integer>>();
		for(int i=0; i<propertyCombination.size(); i++) {
			ArrayList<Integer> parent = (ArrayList<Integer>) input.clone();
			parent.remove(i);
			result.add(parent);
		}
		return result;
	}
	
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		Iterator<Property> it = propertyCombination.iterator();
		while(it.hasNext()) {
			Property property = it.next();
			result.append(property + "-");
		}
		if(result.length() != 0) {
			result.deleteCharAt(result.length()-1);
		} else {
			result.append("all");
		}
		result.append("(" + count + ")");
		return result.toString();
	}
	
	
	// If two atoms' propertyCombination are the same, we say they are equal. We don't care other instances values. 
	@Override
	public boolean equals(Object other){
	    if (other == null) return false;
	    if (other == this) return true;
	    if (!(other instanceof Atom))return false;
	    Atom otherAtom = (Atom)other;
	    
	    if(this.propertyCombination.size() != otherAtom.propertyCombination.size()) return false;
	    for(int i=0; i<propertyCombination.size(); i++) {
	    	if(!this.propertyCombination.get(i).equals(otherAtom.propertyCombination.get(i))) return false;
	    }
	    return true;
	}
	
}
