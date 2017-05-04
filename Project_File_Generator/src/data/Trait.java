package data;

public class Trait {
	
	public String name;
	
	public Marker markers[];
	
	public boolean checked;
	
	public Trait( ) {
		
	}
	
	public void setName( String nameInput ) {
		name = nameInput;
	}
	
	public void setMarkers( Marker markersInput[] ) {
		markers = markersInput;
	}
}
