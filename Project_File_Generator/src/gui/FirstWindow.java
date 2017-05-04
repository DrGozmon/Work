package gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class FirstWindow extends JFrame {

	private JTextField numberOfPlates;
	private JTextField projectName;
	private int plates;
	private String traitTableName;
	
	private static final long serialVersionUID = 1L;

	public FirstWindow( String traitTableNameInput ) {
		traitTableName = traitTableNameInput;
		setTitle("New Project");
		setSize(300,120);
		setLocation(810,440);
		setLayout(new FlowLayout());
		
		JLabel projectNameLabel = new JLabel( "Project name:" );
		add( projectNameLabel );
		
		projectName = new JTextField();
		projectName.setColumns( 25 );
		add( projectName );
		
		JLabel platesLabel = new JLabel("How many plates?");
		add(platesLabel);
		
		numberOfPlates = new JTextField();
		numberOfPlates.setColumns( 10 );
		add(numberOfPlates);
		numberOfPlates.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				if ( !numberOfPlates.getText().equals( "" ) && !projectName.getText().equals( "" ) ) {
					plates = Integer.parseInt(numberOfPlates.getText());
					setVisible(false);
					nextWindow( traitTableName );
				}
			}
		});
		
		JButton nextButton = new JButton("Next");
		nextButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if ( !numberOfPlates.getText().equals( "" ) && !projectName.getText().equals( "" ) ) {
					plates = Integer.parseInt(numberOfPlates.getText());
					setVisible(false);
					nextWindow( traitTableName );
				}
			}
		} );
		add(nextButton);
		
			
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);
	}
	
	public void nextWindow( String traitTableName ) {
		new SecondWindow( projectName.getText(), plates, traitTableName );
	}
	
	public int getPlates() {
		return plates;
	}
	
	public static void main(String[] args) {
		/*FirstWindow numPlatesWindow = */new FirstWindow( "Trait_Marker_Database.txt" );
	}

}
