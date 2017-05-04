package gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import cli.LosiPrograms;

public class MainPage extends JFrame {

	private static final long serialVersionUID = 1L;
	
	public JButton[] buttons;
	
	public MainPage() {
		setTitle( "LProgs" );
		int windowWidth = 250;
		int windowHeight = 300;
		setSize(windowWidth, windowHeight);
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		int width = gd.getDisplayMode().getWidth();
		int height = gd.getDisplayMode().getHeight();
		setLocation( ( width/2 ) - ( windowWidth/2 ), ( height/2 ) - ( windowHeight/2 ) );
		setLayout( new GridLayout( LosiPrograms.PROGRAMS.length + 3, 1 ) );
		JLabel menuName = new JLabel( "Select a program" );
		menuName.setHorizontalAlignment( JLabel.CENTER );
		add( menuName );
		buttons = new JButton[ LosiPrograms.PROGRAMS.length ];
		for ( int i = 0; i < LosiPrograms.PROGRAMS.length; i++ ) {
			buttons[ i ] = new JButton( LosiPrograms.PROGRAMS[ i ] );
			buttons[ i ].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JButton buttonPressed = (JButton) e.getSource();
					String name = buttonPressed.getText();
					int button = -1;
					for ( int i = 0; i < LosiPrograms.PROGRAMS.length; i++ ) {
						if ( name.equals( LosiPrograms.PROGRAMS[ i ] ) ) {
							button = i;
							break;
						}
					}
					Point location = getLocation();
					new ProgramInputs( button, location );
					setVisible( false );
					dispose();
				}
			} );
			add( buttons[ i ] );
		}
		add( new JLabel() );
		JButton exit = new JButton( "Exit" );
		exit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible( false );
				System.exit( 0 );
			}
		} );
		add( exit );

		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);
	}

}
