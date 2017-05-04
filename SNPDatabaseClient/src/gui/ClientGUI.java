package gui;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.xml.bind.DatatypeConverter;

import cli.Client;

public class ClientGUI {

	private static final int PORT_NUMBER = 26119;

	private static final String RSAKEY = "30820153020100300D06092A864886F70D01010105000482013D3082013902010002410094D1592097F7086121C0BDAABF6506FA69A139DA04715ADFA5D01DD2081F0BA1AC2A92CCFEC5FAA69D4D64DCD3127AEC31B1924FF4063EFDDCA7A22CA206FE6302030100010240371B3E71BF540E9A4931620A744D28C4599106272136087C809E0C2CD62D08D5077A9C9086E3AD36F60BCA187BEC3E97462006809C51B4389325AE22CFD00101022100DDD2E5FE1663EAC4998FFE5F49573B232231F199D6207C5A16CCA6904426BC43022100ABBEF818149A0DE9FF528F552D86D0DBC9AC232C519AD0C04DBE46C17A69A3610220145872A5E271D9A79C9A1B6FABEF674A2AA9C00A271CA5AB31AA25A0D7CF61B7022062CA8CE73D2288926B5DD0390CE119CEF3A8E2EAA15E4E69EFCE5DE0F94B3EC1022001F28BA80B7D9753CF32F555438557FB8E758640819649C6201E253F1D437506";

	private static String hostIP;
	private static String user;
	private static char[] pass;

	private static DataInputStream input;
	private static DataOutputStream output;
	private static Cipher AESEncrypter;
	private static Cipher AESDecrypter;
	private static Socket sock = null;

	public ClientGUI() {
	}

	public void run() {
		JFrame ipPrompt = new JFrame();
		ipPrompt.setTitle( "Log In" );
		ipPrompt.setLayout( new GridLayout( 4,2 ) );
		int windowWidth = 300;
		int windowHeight = 110;
		ipPrompt.setSize(windowWidth, windowHeight);
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		int width = gd.getDisplayMode().getWidth();
		int height = gd.getDisplayMode().getHeight();
		ipPrompt.setLocation( ( width/2 ) - ( windowWidth/2 ), ( height/2 ) - ( windowHeight/2 ) );
		JTextField hostnameField = new JTextField();
		JTextField uname = new JTextField();
		JPasswordField pword = new JPasswordField();

		hostnameField.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				if ( !hostnameField.getText().equals( "" ) && !uname.getText().equals( "" ) && pword.getPassword().length != 0 ) {
					hostIP = hostnameField.getText();
					user = uname.getText();
					pass = pword.getPassword();
					ipPrompt.setVisible( false );
					ipPrompt.dispose();
					connect( ipPrompt.getLocation() );
				}
			}
		});
		uname.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				if ( !hostnameField.getText().equals( "" ) && !uname.getText().equals( "" ) && pword.getPassword().length != 0 ) {
					hostIP = hostnameField.getText();
					user = uname.getText();
					pass = pword.getPassword();
					ipPrompt.setVisible( false );
					ipPrompt.dispose();
					connect( ipPrompt.getLocation() );
				}
			}
		});
		pword.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				if ( !hostnameField.getText().equals( "" ) && !uname.getText().equals( "" ) && pword.getPassword().length != 0 ) {
					hostIP = hostnameField.getText();
					user = uname.getText();
					pass = pword.getPassword();
					ipPrompt.setVisible( false );
					ipPrompt.dispose();
					connect( ipPrompt.getLocation() );
				}
			}
		});
		ipPrompt.add( new JLabel( "Hostname: ", SwingConstants.RIGHT ) );
		ipPrompt.add( hostnameField );
		ipPrompt.add( new JLabel( "Username: ", SwingConstants.RIGHT ) );
		ipPrompt.add( uname );
		ipPrompt.add( new JLabel( "Password: ", SwingConstants.RIGHT ) );
		ipPrompt.add( pword );

		JButton exit = new JButton( "Exit" );
		exit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ipPrompt.setVisible( false );
				ipPrompt.dispose();
				System.exit( 0 );
			}
		} );
		ipPrompt.add( exit );

		JButton next = new JButton( "Next" );
		next.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				if ( !hostnameField.getText().equals( "" ) && !uname.getText().equals( "" ) && pword.getPassword().length != 0 ) {
					hostIP = hostnameField.getText();
					user = uname.getText();
					pass = pword.getPassword();
					ipPrompt.setVisible( false );
					ipPrompt.dispose();
					connect( ipPrompt.getLocation() );
				}
			}
		});
		ipPrompt.add( next );

		ipPrompt.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		ipPrompt.setVisible( true );
	}

	private static void ipFail( String message ) {
		JFrame ipPrompt = new JFrame();
		ipPrompt.setTitle( "Enter Hostname" );
		ipPrompt.setLayout( new GridLayout( 5,2 ) );
		int windowWidth = 300;
		int windowHeight = 110;
		ipPrompt.setSize(windowWidth, windowHeight);
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		int width = gd.getDisplayMode().getWidth();
		int height = gd.getDisplayMode().getHeight();
		ipPrompt.setLocation( ( width/2 ) - ( windowWidth/2 ), ( height/2 ) - ( windowHeight/2 ) );
		ipPrompt.add( new JLabel( message ) );
		ipPrompt.add( new JLabel() );
		JTextField hostnameField = new JTextField();
		JTextField uname = new JTextField();
		JPasswordField pword = new JPasswordField();

		hostnameField.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				if ( !hostnameField.getText().equals( "" ) && !uname.getText().equals( "" ) && pword.getPassword().length != 0 ) {
					hostIP = hostnameField.getText();
					user = uname.getText();
					pass = pword.getPassword();
					ipPrompt.setVisible( false );
					ipPrompt.dispose();
					connect( ipPrompt.getLocation() );
				}
			}
		});
		uname.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				if ( !hostnameField.getText().equals( "" ) && !uname.getText().equals( "" ) && pword.getPassword().length != 0 ) {
					hostIP = hostnameField.getText();
					user = uname.getText();
					pass = pword.getPassword();
					ipPrompt.setVisible( false );
					ipPrompt.dispose();
					connect( ipPrompt.getLocation() );
				}
			}
		});
		pword.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				if ( !hostnameField.getText().equals( "" ) && !uname.getText().equals( "" ) && pword.getPassword().length != 0 ) {
					hostIP = hostnameField.getText();
					user = uname.getText();
					pass = pword.getPassword();
					ipPrompt.setVisible( false );
					ipPrompt.dispose();
					connect( ipPrompt.getLocation() );
				}
			}
		});
		ipPrompt.add( new JLabel( "Hostname: ", SwingConstants.RIGHT ) );
		ipPrompt.add( hostnameField );
		ipPrompt.add( new JLabel( "Username: ", SwingConstants.RIGHT ) );
		ipPrompt.add( uname );
		ipPrompt.add( new JLabel( "Password: ", SwingConstants.RIGHT ) );
		ipPrompt.add( pword );

		JButton exit = new JButton( "Exit" );
		exit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ipPrompt.setVisible( false );
				ipPrompt.dispose();
				System.exit( 0 );
			}
		} );
		ipPrompt.add( exit );

		JButton next = new JButton( "Next" );
		next.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				if ( !hostnameField.getText().equals( "" ) && !uname.getText().equals( "" ) && pword.getPassword().length != 0 ) {
					hostIP = hostnameField.getText();
					user = uname.getText();
					pass = pword.getPassword();
					ipPrompt.setVisible( false );
					ipPrompt.dispose();
					connect( ipPrompt.getLocation() );
				}
			}
		});
		ipPrompt.add( next );

		ipPrompt.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		ipPrompt.setVisible( true );
	}

	private static void connect( Point location ) {
		try {
			sock = new Socket( hostIP, PORT_NUMBER );
			// Try to create a socket connection to the server.

			// Get formatted input/output streams for talking with the server.
			input = new DataInputStream( sock.getInputStream() );
			output = new DataOutputStream( sock.getOutputStream() );

			// Get a username from the user and send it to the server.
			output.writeUTF( user );
			output.flush();

			String success = input.readUTF();
			if ( !success.equals( "Success" ) ) {
				ipFail( "Incorrect username or password" );
			}

			// Try to read the user's private key.
			String hexKey = RSAKEY;
			byte[] rawKey = DatatypeConverter.parseHexBinary( hexKey );

			// Make a key specification based on this key.
			PKCS8EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec( rawKey );

			// Get an RSA key based on this specification
			KeyFactory keyFactory = KeyFactory.getInstance( "RSA" );
			PrivateKey privateKey = keyFactory.generatePrivate( privKeySpec );

			// Make a cipher object that will encrypt using this key.
			Cipher RSAEncrypter = Cipher.getInstance( "RSA" );
			RSAEncrypter.init( Cipher.ENCRYPT_MODE, privateKey );

			// Make another cipher object that will decrypt using this key.
			Cipher RSADecrypter = Cipher.getInstance( "RSA" );
			RSADecrypter.init( Cipher.DECRYPT_MODE, privateKey );

			// Get a password and send it to the server encrypted with RSA
			String temppass = "";
			for ( int i = 0; i < pass.length; i++ ) {
				temppass += pass[ i ];
			}
			Client.putMessage( output, RSAEncrypter.doFinal( temppass.getBytes( "UTF-8" ) ) );

			// Wait for success message
			success = new String( RSADecrypter.doFinal( Client.getMessage( input ) ), "UTF-8" );

			if ( !success.equals( "Success" ) ) {
				ipFail( "Incorrect username or password" );
			}
			// Get the challenge string (really a byte array) from the server.
			byte[] challenge = Client.getMessage( input );

			// Encrypt the challenge with our private key and send it back.
			byte[] encrypted = RSAEncrypter.doFinal( challenge );
			Client.putMessage( output, encrypted );

			// Get the symmetric key from the server and make AES
			// encrypt/decrypt objects for it.
			byte[] symKey = RSADecrypter.doFinal( Client.getMessage( input ) );
			SecretKey key = new SecretKeySpec( symKey, "AES" );
			AESEncrypter = Cipher.getInstance( "AES/ECB/PKCS5Padding" );
			AESDecrypter = Cipher.getInstance( "AES/ECB/PKCS5Padding" );
			AESDecrypter.init( Cipher.DECRYPT_MODE,  key );
			AESEncrypter.init( Cipher.ENCRYPT_MODE,  key );
			secondWindow( location );
		} catch (IOException e) {
			ipFail( "Incorrect hostname" );
		} catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeySpecException e) {
			e.printStackTrace();
		}
	}

	private static void secondWindow( Point location ) {
		JFrame window = new JFrame();
		window.setLocation( location );
		window.setTitle( "Menu" );
		window.setSize( 300, 500 );
		window.setLayout( new GridLayout( 6, 1 ) );
		JButton changePassword = new JButton( "Change Password" );
		JButton get = new JButton( "Get Data from Database" );
		JButton add = new JButton( "Add File to Database (admin)" );
		JButton shutdown = new JButton( "Shutdown Server (admin)" );
		JButton addUser = new JButton( "Add User (admin)" );
		JButton exit = new JButton( "Exit" );
		changePassword.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				window.setVisible( false );
				window.dispose();
				changePassword( window.getLocation(), null );
			}
		});
		addUser.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {

			}
		});
		get.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {

			}
		});
		add.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {

			}
		});
		shutdown.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {

			}
		});
		exit.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				window.setVisible( false );
				window.dispose();
				try {
					putMessage( output, AESEncrypter.doFinal( "exit".getBytes() ) );
					sock.close();
					System.exit( 0 );
				} catch (IllegalBlockSizeException | BadPaddingException | IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		window.add( get );
		window.add( changePassword );
		window.add( add );
		window.add( addUser );
		window.add( shutdown );
		window.add( exit );

		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setVisible( true );
	}

	/** Utility function to read a length then a byte array from the
    given stream.  TCP doesn't respect message boundaries, but this
    is essentially a technique for marking the start and end of
    each message in the byte stream.  This can also be used by the
    client. */
	private static byte[] getMessage( DataInputStream input ) throws IOException {
		int len = input.readInt();
		byte[] msg = new byte[ len ];
		input.readFully( msg );
		return msg;
	}

	/** Function analogous to the previous one, for sending messages. */
	private static void putMessage( DataOutputStream output, byte[] msg ) throws IOException {
		// Write the length of the given message, followed by its contents.
		output.writeInt( msg.length );
		output.write( msg, 0, msg.length );
		output.flush();
	}
	//start
	private static void changePassword( Point location, String message ) {
		JFrame window = new JFrame();
		window.setTitle( "Change Password" );
		if ( message == null ) {
			window.setLayout( new GridLayout( 4, 2 ) );
		} else {
			window.setLayout( new GridLayout( 5, 2 ) );
		}
		JButton cancel = new JButton( "Cancel" );
		cancel.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				window.setVisible( false );
				window.dispose();
				secondWindow( window.getLocation() );
			}
		});
		JButton change = new JButton( "Change Password" );
		change.addActionListener( new ActionListener() {
			public void actionPerformed( ActionEvent e ) {
				if ( true ) {
					// TODO
					//change password here
					//actionlistener should go here to check for if the old pass is actually that users password
					//will most likely check which button was pressed and do conditional logic accordingly
					//actionlistener should check to see if the passwords match
					/*int match = 0;
					for( int i = 0; i < pass.length; i++ ){
						if( pass[i] == oldPass.getPassword()[i] )
							match++;
					}*/
					
					//if both of the above cases are good then change password
					try {
						putMessage( output, AESEncrypter.doFinal( "change".getBytes( "UTF-8" ) ) );
						String res = new String( AESDecrypter.doFinal( getMessage( input ) ), "UTF-8" );

					} catch (IllegalBlockSizeException | BadPaddingException | IOException e1) {
						e1.printStackTrace();
					}
					window.setVisible( false );
					window.dispose();
					secondWindow( window.getLocation() );
				}
			}
		});
		//seems to have just built the objects here
		JPasswordField oldPass = new JPasswordField();
		JPasswordField newPass = new JPasswordField();
		JPasswordField newPass2 = new JPasswordField();
		//most likely checks if there was a failed attempt and displays why
		if ( message != null ) {
			window.add( new JLabel( message ) );
			window.add( new JLabel() );
		}
		//generation of labels and objects within the GUI
		window.add( new JLabel( "Current password: " ) );
		window.add( oldPass );
		window.add( new JLabel( "New password:" ) );
		window.add( newPass );
		window.add( new JLabel( "Retype new password:" ) );
		window.add( newPass2 );
		window.add( cancel );
		window.add( change );

		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setVisible( true );
	}

	public static void main( String args[] ) {
		new ClientGUI().run();
	}
}