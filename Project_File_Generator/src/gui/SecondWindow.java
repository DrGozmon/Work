package gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class SecondWindow extends JFrame {

	private int plateQuantity;
	private JTextField[] plateNames;
	private String traitTableName;
	private String projectName;
	
	private static final long serialVersionUID = 1L;

	public SecondWindow( String projectNameInput, int plates, String traitTableNameInput ) {
		projectName = projectNameInput;
		traitTableName = traitTableNameInput;
		setTitle("Plate Names");
		plateQuantity = plates;
		setSize(200,25*plateQuantity+80);
		setLocation(810,440);
		JLabel header = new JLabel("Input plate names:");
		setLayout(new FlowLayout());
		add(header);
		plateNames = new JTextField[plateQuantity];
		for ( int i = 0; i < plateQuantity; i++ ) {
			plateNames[i] = new JTextField();
			plateNames[i].setColumns(15);
			add(plateNames[i]);
		}
		JButton next = new JButton("Next");
		next.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				int test = 0;
				String[] names = new String[plateQuantity];
				for ( int i = 0; i < plateQuantity; i++ ) {
					if ( plateNames[i].getText().equals("") ) {
						test++;
					}
				}
				if ( test == 0 ) {
					setVisible(false);
					for ( int i = 0; i < plateQuantity; i++ ) {
						names[i] = new String(plateNames[i].getText());
					}
					new ThirdWindow( projectName, names, traitTableName );
				}
			}
		});
		add(next);
		
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setVisible(true);
	}

}
