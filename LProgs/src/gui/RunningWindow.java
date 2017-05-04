package gui;

import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class RunningWindow extends JFrame {

	private static final long serialVersionUID = 1L;

	public RunningWindow( Point location ) {
		setTitle( "Running..." );
		setSize( 200, 200 );
		setLocation( (int) location.getX(), (int) location.getY() );
		setLayout( new FlowLayout() );
		JLabel wait = new JLabel( "Please wait..." );
		add( wait );
		JButton cancel = new JButton( "Cancel" );
		cancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit( 1 );
			}
		} );
		add( cancel );
		setVisible( true );
	}

}
