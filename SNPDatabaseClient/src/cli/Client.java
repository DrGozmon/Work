package cli;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Scanner;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import gui.ClientGUI;

public class Client {

	private static final int PORT_NUMBER = 26119;

	private static final String RSAKEY = "30820153020100300D06092A864886F70D01010105000482013D3082013902010002410094D1592097F7086121C0BDAABF6506FA69A139DA04715ADFA5D01DD2081F0BA1AC2A92CCFEC5FAA69D4D64DCD3127AEC31B1924FF4063EFDDCA7A22CA206FE6302030100010240371B3E71BF540E9A4931620A744D28C4599106272136087C809E0C2CD62D08D5077A9C9086E3AD36F60BCA187BEC3E97462006809C51B4389325AE22CFD00101022100DDD2E5FE1663EAC4998FFE5F49573B232231F199D6207C5A16CCA6904426BC43022100ABBEF818149A0DE9FF528F552D86D0DBC9AC232C519AD0C04DBE46C17A69A3610220145872A5E271D9A79C9A1B6FABEF674A2AA9C00A271CA5AB31AA25A0D7CF61B7022062CA8CE73D2288926B5DD0390CE119CEF3A8E2EAA15E4E69EFCE5DE0F94B3EC1022001F28BA80B7D9753CF32F555438557FB8E758640819649C6201E253F1D437506";

	/** Utility function to read a length then a byte array from the
    given stream.  TCP doesn't respect message boundaries, but this
    is essentially a technique for marking the start and end of
    each message in the byte stream.  This can also be used by the
    client. */
	public static byte[] getMessage( DataInputStream input ) throws IOException {
		int len = input.readInt();
		byte[] msg = new byte[ len ];
		input.readFully( msg );
		return msg;
	}

	/** Function analogous to the previous one, for sending messages. */
	public static void putMessage( DataOutputStream output, byte[] msg ) throws IOException {
		// Write the length of the given message, followed by its contents.
		output.writeInt( msg.length );
		output.write( msg, 0, msg.length );
		output.flush();
	}

	public static void main(String[] args) {
		if ( args.length > 0 && args[ 0 ].equalsIgnoreCase( "nogui" ) ) {
			if ( args.length != 2 ) {
				System.out.println( "Usage: Client [nogui] [host]" );
				System.exit( -1 );
			}
			try ( Socket sock = new Socket( args[ 1 ], PORT_NUMBER ); Scanner scanner = new Scanner( System.in ) ){
				// Try to create a socket connection to the server.

				// Get formatted input/output streams for talking with the server.
				DataInputStream input = new DataInputStream( sock.getInputStream() );
				DataOutputStream output = new DataOutputStream( sock.getOutputStream() );

				// Get a username from the user and send it to the server.
				System.out.print( "Username: " );
				String name = scanner.nextLine();
				output.writeUTF( name );
				output.flush();

				String success = input.readUTF();
				if ( !success.equals( "Success" ) ) {
					System.out.println( "Username or password is incorrect" );
					System.exit( 1 );
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
				System.out.print( "Password: " );
				putMessage( output, RSAEncrypter.doFinal( scanner.nextLine().getBytes( "UTF-8" ) ) );

				// Wait for success message
				success = new String( RSADecrypter.doFinal( getMessage( input ) ), "UTF-8" );

				if ( !success.equals( "Success" ) ) {
					System.out.println( "Username or password is incorrect." );
					System.exit( 1 );
				}
				// Get the challenge string (really a byte array) from the server.
				byte[] challenge = getMessage( input );

				// Encrypt the challenge with our private key and send it back.
				byte[] encrypted = RSAEncrypter.doFinal( challenge );
				putMessage( output, encrypted );

				// Get the symmetric key from the server and make AES
				// encrypt/decrypt objects for it.
				byte[] symKey = RSADecrypter.doFinal( getMessage( input ) );
				SecretKey key = new SecretKeySpec( symKey, "AES" );
				Cipher AESEncrypter = Cipher.getInstance( "AES/ECB/PKCS5Padding" );
				Cipher AESDecrypter = Cipher.getInstance( "AES/ECB/PKCS5Padding" );
				AESDecrypter.init( Cipher.DECRYPT_MODE,  key );
				AESEncrypter.init( Cipher.ENCRYPT_MODE,  key );

				// Do communication with server here

				while ( true ) {
					// Prompt for command
					System.out.print( "Command: " );
					String command = scanner.nextLine();

					// Break out of loop if requested
					if ( command.equalsIgnoreCase( "exit" ) ) {
						putMessage( output, AESEncrypter.doFinal( "exit".getBytes() ) );
						System.exit( 0 );
					}

					// Encrypt and send command to server
					byte[] bytes = command.getBytes( "UTF-8" );
					putMessage( output, AESEncrypter.doFinal( bytes ) );

					String res = "none";
					while( !res.equals( "done" ) ) {
						// Get response
						res = new String( AESDecrypter.doFinal( getMessage( input ) ), "UTF-8" );

						// Display something based on response
						if ( res.equals( "file" ) ) {
							System.out.print( "Input filename: " );
							String filename = scanner.nextLine();
							Scanner file = new Scanner( new File( filename ) );
							while( file.hasNextLine() ) {
								putMessage(output, AESEncrypter.doFinal( file.nextLine().getBytes() ) );
							}
							putMessage( output, AESEncrypter.doFinal( "done".getBytes() ) );
							file.close();
						} else if ( res.equals( "NotAdmin" ) ) {
							System.out.println( "You must be an admin to use that command" );
						} else if ( res.equals( "organism" ) ) {
							System.out.print( "Organism: " );
							String organism = scanner.nextLine();
							putMessage( output, AESEncrypter.doFinal( organism.getBytes() ) );
						} else if ( res.equals( "shutdown" ) ) {
							System.out.println( "Server shutting down" );
							System.exit( 0 );
						} else if ( res.equals( "getInfo" ) ) {
							System.out.print( "List of taxa: " );
							String taxaFilename = scanner.nextLine();
							System.out.print( "List of SNPs: " );
							String snpsFilename = scanner.nextLine();
							System.out.print( "Output file name: " );
							String outFilename = scanner.nextLine();
							Scanner taxaFile = new Scanner( new File( taxaFilename ) );
							String taxa = taxaFile.nextLine();
							while ( taxaFile.hasNextLine() ) {
								String line = taxaFile.nextLine();
								taxa += "\t" + line;
							}
							taxaFile.close();
							putMessage( output, AESEncrypter.doFinal( taxa.getBytes() ) );
							Scanner snpsFile = new Scanner( new File( snpsFilename ) );
							String snps = snpsFile.nextLine();
							while ( snpsFile.hasNextLine() ) {
								snps += "\t" + snpsFile.nextLine();
							}
							snpsFile.close();
							putMessage( output, AESEncrypter.doFinal( snps.getBytes() ) );
							String returnedValue = new String( AESDecrypter.doFinal( getMessage( input ) ), "UTF-8" );
							BufferedWriter fileOut = new BufferedWriter( new FileWriter( outFilename ) );
							fileOut.write( returnedValue );
							fileOut.close();
							System.out.println( "Requested data can be found in " + outFilename );
						} else if ( res.equals( "badCommand" ) ) {
							System.out.println( "Command not recognized" );
						}
					}
				}

			} catch( IOException e ){
				System.err.println( "IO Error: " + e );
				e.printStackTrace();
				System.exit( 1 );
			} catch( GeneralSecurityException e ){
				System.err.println( "Encryption error: " + e );
				e.printStackTrace();
				System.exit( 1 );
			}
		} else {
			new ClientGUI().run();
		}

	}

}
