package gui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import data.*;

public class ThirdWindow extends JFrame {

	private static JCheckBox[] traitList;
	private static Trait[] traits;
	private static String[] plateNames;
	private static String projectName;
	private static final long serialVersionUID = 1L;

	public ThirdWindow( String projectNameInput, String plateNamesInput[], String traitTableName ) {
		projectName = projectNameInput;
		traits = makeTraitTable( traitTableName );
		plateNames = plateNamesInput;
		setTitle( "Traits" );
		setSize( 300, traits.length * 20 + 100 );
		setLocation(700, 180);
		setLayout(new GridLayout( traits.length + 2, 1 ) );
		JLabel header = new JLabel( "Which Traits?" );
		add( header );
		
		traitList = new JCheckBox[ traits.length ];
		for ( int i = 0; i < traitList.length; i++ ) {
			traitList[ i ] = new JCheckBox( traits[ i ].name );
			
			add( traitList[ i ] );
		}
		
		JButton next = new JButton( "Next" );
		add( next );
		next.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				boolean selected = false;
				boolean[] checked = new boolean[ traits.length ];
				for ( int i = 0; i < traits.length; i++ ) {
					if ( traitList[ i ].isSelected() ) {
						selected = true;
						checked[ i ] = true;
						traits[ i ].checked = true;
					}
				}
				if ( selected ) {
					setVisible( false );
					nextWindow( checked );
				}
			}
		} );
		
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);
	}

	private static void nextWindow( boolean[] checked ) {
		new FourthWindow( projectName, checked, traits, plateNames );
	}
	
	private static Trait[] makeTraitTable( String traitTableName ) {
		Scanner inputFile;
		try {
			inputFile = new Scanner( new File( traitTableName ) );
		
			int lines = 0;
			int[] markerCounts = new int[ 1000 ];
			while ( inputFile.hasNextInt() ) {
				markerCounts[ lines ] = inputFile.nextInt();
				lines++;
			}
			
			Trait[] traits = new Trait[ lines ];
			for ( int i = 0; i < lines; i++ ) {
				traits[ i ] = new Trait();
				Marker[] markers = new Marker[ markerCounts[ i ] ];
				for ( int j = 0; j < markerCounts[ i ]; j++ ) {
					markers[ j ] = new Marker();
				}
				traits[ i ].setMarkers( markers );
			}
			
			String input;
			int traitNum = 0;
			int markerNum = 0;
			boolean trait = true;
			while(inputFile.hasNext()) {
				if ( trait ) {
					traits[ traitNum ].setName( inputFile.next() );
					markerNum = 0;
					trait = false;
				} else {
					input = inputFile.next();
					if ( input.equals( "ENDOFLINE" ) ) {
						trait = true;
						traitNum++;
					} else {
						traits[ traitNum ].markers[ markerNum ].setName( input );
						markerNum++;
					}
				}
			}
			inputFile.close();
			return traits;
		} catch (FileNotFoundException e) {
			System.err.println( "Couldn't open file" );
			return null;
		}
	}
	
	public static void main( String args[] ) {
		String[] names = new String[2];
		names[0] = "Plate0";
		names[1] = "Plate1";
		new ThirdWindow( "Test_Project", names, args[0] );
	}
}
