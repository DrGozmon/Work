package gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import data.*;

public class FourthWindow extends JFrame {
	
	private static final long serialVersionUID = 1L;
	private static Trait trait;
	private static JTextField[] controls;
	private static JCheckBox[] markerChecks;
	private static JTextField[] comments;
	private static Trait[] traits;
	private static boolean[] traitsToDo;
	private static String[] plateNames;
	private static int windowToMake;
	private static String projectName;
	
	public FourthWindow( String projectNameInput, boolean[] checked, Trait[] traitInput, String[] plateNamesInput ) {
		projectName = projectNameInput;
		traitsToDo = checked;
		plateNames = plateNamesInput;
		traits = traitInput;
		windowToMake = -1;
		for ( int i = 0; i < checked.length; i++ ) {
			if ( checked[ i ] ) {
				windowToMake = i;
				checked[ i ] = false;
				break;
			}
		}
		if ( windowToMake == -1 ) {
			System.out.println( "An error occured" );
			System.exit( 1 );
		}
		trait = traitInput[ windowToMake ];
		setTitle( trait.name );
		setSize( 500, 20 * trait.markers.length + 100 );
		setLocation( 700, 180 );
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLayout( new GridLayout( trait.markers.length + 2, 2 ) );
		add( new JLabel( "Trait: " + trait.name ) );
		add( new JLabel( "Comments:" ) );
		add( new JLabel( "Control" ) );
		markerChecks = new JCheckBox[ trait.markers.length ];
		comments = new JTextField[ trait.markers.length ];
		controls = new JTextField[ trait.markers.length ];
		for ( int i = 0; i < trait.markers.length; i++ ) {
			comments[ i ] = new JTextField();
			markerChecks[ i ] = new JCheckBox( trait.markers[ i ].name );
			markerChecks[ i ].setSelected( true );
			controls[ i ] = new JTextField( "**" );
			add( markerChecks[ i ] );
			add( comments[ i ] );
			add( controls[ i ] );
		}
		JButton next = new JButton( "Next" );
		next.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				boolean selected = false;
				boolean[] checked = new boolean[ trait.markers.length ];
				for ( int i = 0; i < trait.markers.length; i++ ) {
					if ( markerChecks[ i ].isSelected() ) {
						selected = true;
						checked[ i ] = true;
						traits[ windowToMake ].markers[ i ].checked = true;
					}
				}
				boolean moreWindows = false;
				for ( int i = 0; !moreWindows && i < traitsToDo.length; i++ ) {
					if ( traitsToDo[ i ] ) {
						moreWindows = true;
					}
				}
				if ( selected ) {
					for ( int i = 0; i < trait.markers.length; i++ ) {
						if ( checked[ i ] ) {
							trait.markers[ i ].setControl( controls[ i ].getText() );
						}
						if ( !comments[ i ].getText().equals( "" ) && checked[ i ] ) {
							trait.markers[ i ].addComment( comments[ i ].getText( ) );
						}
					}
					setVisible( false );
					if ( moreWindows ) {
						new FourthWindow( projectName, traitsToDo, traits, plateNames );
					} else {
						new ExcelSheetMaker( projectName, traits, plateNames );
					}
				}
			}
		} );
		add( next );
		setVisible( true );
	}

	public static void main( String[] args ) {
		String[] plates = new String[ 2 ];
		plates[ 0 ] = "Plate0";
		plates[ 1 ] = "Plate1";
		Trait[] traits = new Trait[ 1 ];
		boolean[] checked = new boolean[ 1 ];
		checked[ 0 ] = true;
		traits[ 0 ] = new Trait( );
		traits[ 0 ].setName( "Trait0" );
		Marker[] markers = new Marker[ 8 ];
		String[] names = new String[]{ "Marker0", "Marker1", "Marker2", "Marker3", "Marker4", "Marker5", "Marker6", "Marker7" };
		for ( int i = 0; i < 8; i ++ ) {
			markers[ i ] = new Marker();
			markers[ i ].setName( names[ i ] );
		}
		traits[ 0 ].setMarkers( markers );
		
		new FourthWindow( "Test_Project", checked, traits, plates );
	}
}
