package gui;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import cli.LosiPrograms;

public class ProgramInputs extends JFrame {

	private static final long serialVersionUID = 1L;

	public ProgramInputs( int programNumber, Point location ) {
		setTitle( LosiPrograms.PROGRAMS[ programNumber ] );
		int windowWidth = 800;
		int windowHeight = LosiPrograms.getNumInputs( programNumber ) * 40;
		setSize(windowWidth, windowHeight);
		setLocation( ( int ) location.getX(), ( int ) location.getY() );
		setLayout( new GridLayout( LosiPrograms.getNumInputs( programNumber ) + 2, 3 ) );
		String[] inputs = new String[ LosiPrograms.getNumInputs( programNumber ) ];
		boolean[] mandatory = new boolean[ LosiPrograms.getNumInputs( programNumber ) ];
		JTextField[] text = new JTextField[ LosiPrograms.getNumInputs( programNumber ) ];
		JButton[] browse = new JButton[ LosiPrograms.getNumInputs( programNumber ) ];
		for ( int i = 0; i < inputs.length; i++ ) {
			inputs[ i ] = LosiPrograms.getInputs( programNumber )[ i ];
			mandatory[ i ] = LosiPrograms.getMandatory( programNumber )[ i ];
			text[ i ] = new JTextField();
			browse[ i ] = new JButton( "Browse" );
			browse[ i ].putClientProperty( "row", i );
			browse[ i ].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JFileChooser browser = new JFileChooser();
					int successVal = browser.showOpenDialog(null);
					if ( successVal == JFileChooser.APPROVE_OPTION ) {
						String fileName = browser.getSelectedFile().getAbsolutePath();
						JButton btn = (JButton) e.getSource();
						int index = (int) btn.getClientProperty( "row" );
						text[ index ].setText( fileName );
					}
				}
			} );
		}
		
		JLabel mandatoryStatement = new JLabel( "Required inputs marked with *" );
		
		add( mandatoryStatement );
		add( new JLabel( "" ) );
		add( new JLabel( "" ) );
		
		JButton next = new JButton( "Next" );
		JButton back = new JButton( "Back" );
		
		next.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ArrayList<String> args = new ArrayList<String>();
				String[] inArgs = null;
				if ( LosiPrograms.PROGRAMS[ programNumber ].equals( "VCFToHapmapForMapping" ) ) {
					args.add( "-i" );
					args.add( text[ 0 ].getText() );
					args.add( "-o" );
					args.add( text[ 1 ].getText() );
					args.add( "-t" );
					args.add( text[ 2 ].getText() );
					if ( !text[ 3 ].getText().isEmpty() ) {
						args.add( "-d" );
						args.add( text[ 3 ].getText() );
					}
					if ( !text[ 4 ].getText().isEmpty() && !text[ 5 ].getText().isEmpty() ) {
						args.add( "-p1" );
						args.add( text[ 4 ].getText() );
						args.add( "-p2" );
						args.add( text[ 5 ].getText() );
					}
					if ( !text[ 6 ].getText().isEmpty() && !text[ 7 ].getText().isEmpty() && !text[ 8 ].getText().isEmpty() && !text[ 9 ].getText().isEmpty() ) {
						args.add( "-r" );
						args.add( text[ 6 ].getText() + "," + text[ 7 ].getText() + "," + text[ 8 ].getText() + "," + text[ 9 ].getText() );
					}
					inArgs = new String[ args.size() + 1 ];
					inArgs[ 0 ] = LosiPrograms.PROGRAMS[ programNumber ];
					for ( int i = 0; i < args.size(); i++ ) {
						inArgs[ i + 1 ] = args.get( i );
					}
				} else {
					for ( int i = 0; i < text.length; i++ ) {
						if ( !( text[ i ].getText().isEmpty() || !mandatory[ i ] ) ) {
							args.add( text[ i ].getText() );
						}
					}
					inArgs = new String[ args.size() + 1 ];
					inArgs[ 0 ] = LosiPrograms.PROGRAMS[ programNumber ];
					for ( int i = 0; i < args.size(); i++ ) {
						inArgs[ i + 1 ] = args.get( i );
					}
				}
				setVisible( false );
				JFrame running = new JFrame();
				running.setTitle( "Running..." );
				running.setSize( 200, 200 );
				running.setLocation( (int) location.getX(), (int) location.getY() );
				running.setLayout( new FlowLayout() );
				JLabel wait = new JLabel( "Please wait..." );
				running.add( wait );
				JButton cancel = new JButton( "Cancel" );
				cancel.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						System.exit( 1 );
					}
				} );
				running.add( cancel );
				running.setVisible( true );
				try {
					LosiPrograms.main( inArgs );
				} catch (InterruptedException | IOException e1) {
					e1.printStackTrace();
				}
				running.setVisible( false );
				running.dispose();
				
				dispose();
			}
		} );

		back.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new MainPage();
				setVisible( false );
				dispose();
			}
		} );
		
		for ( int i = 0; i < inputs.length; i++ ) {
			JLabel label = new JLabel();
			label.setHorizontalAlignment( JLabel.RIGHT );
			if ( mandatory[ i ] ) {
				label.setText( inputs[ i ] + "*: " );
			} else {
				label.setText( inputs[ i ] + ": " );
			}
			add( label );
			add( text[ i ] );
			if ( LosiPrograms.getIsFile( programNumber )[ i ] ) {
				add( browse[ i ] );
			} else {
				add( new JLabel( "" ) );
			}
		}
		
		add( back );
		add( new JLabel( "" ) );
		add( next );
		
		
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);
	}
}
