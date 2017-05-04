package data;

public class Marker {

	public String name;
	
	public String comments;
	
	public boolean checked;
	
	public String control = "**";
	
	public Marker( ) {
		
	}
	
	public void addComment( String commentsInput ) {
		comments = commentsInput;
	}
	
	public void setName( String nameInput ) {
		name = nameInput;
	}
	
	public void setControl ( String controlInput ) {
		control = controlInput;
	}

}
