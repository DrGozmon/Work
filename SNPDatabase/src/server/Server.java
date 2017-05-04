package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

public class Server {

	private static final int THREADS = 20;

	private static final int PORT_NUMBER = 26119;

	private static final String RSAKEY = "305C300D06092A864886F70D0101010500034B00304802410094D1592097F7086121C0BDAABF6506FA69A139DA04715ADFA5D01DD2081F0BA1AC2A92CCFEC5FAA69D4D64DCD3127AEC31B1924FF4063EFDDCA7A22CA206FE630203010001";

	private static Server server;

	private MysqlDataSource ds;
	private Connection conn;
	private ArrayList<String[]> taxa;
	private int numTaxa;
	private int numTables;

	/** List of all the user records. */
	private ArrayList< UserRec > userList = new ArrayList< UserRec >();


	/** Record for an individual user. */
	private static class UserRec {
		// Name of this user.
		String name;

		// Admin?
		boolean admin;

		// This user's public key.
		PublicKey publicKey;

		String password;
	}

	public Server() {
		ds = new MysqlDataSource();
		ds.setUser( "root" );
		ds.setPassword( "B1oinformatics" );
		ds.setServerName( "localhost" );
		ds.setDatabaseName( "SNPDatabase" );
		try {
			conn = ds.getConnection();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		taxa = new ArrayList<String[]>();
		try {
			PreparedStatement ps = conn.prepareStatement( "SELECT * FROM tables;" );
			ResultSet rs = executeQuery( ps );
			rs.next();
			numTables = rs.getInt( 1 );
			for ( int i = 1; i <= numTables; i++ ) {
				taxa.add( new String[ 1000 ] );
				rs = executeQuery( conn.prepareStatement( "SHOW COLUMNS FROM SNPs" + i + ";" ) );
				int j = 0;
				rs.next();
				rs.next();
				while ( rs.next() ) {
					taxa.get( i - 1 )[ j ] = rs.getString( 1 );
					numTaxa++;
					j++;
				}
			}
			ps.close();
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static String sha256(String base) {
		try{
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(base.getBytes("UTF-8"));
			StringBuffer hexString = new StringBuffer();

			for (int i = 0; i < hash.length; i++) {
				String hex = Integer.toHexString(0xff & hash[i]);
				if(hex.length() == 1) hexString.append('0');
				hexString.append(hex);
			}

			return hexString.toString();
		} catch(Exception ex){
			throw new RuntimeException(ex);
		}
	}

	private static void setUp() throws Exception {
		/*Scanner input = new Scanner( new File( "/home/gbg_lab_admin/Array_60TB/Andrew_Dev_Folder/SNPDatabase/users.txt" ) );
		int userCount = input.nextInt();
		for ( int k = 0; k < userCount; k++ ) {
			// Create a record for the next user.
			UserRec rec = new UserRec();
			rec.name = input.next();

			// Check to see if user is an admin
			rec.admin = Boolean.parseBoolean( input.next() );

			// Get the key as a string of hex digits and turn it into a byte array.
			String hexKey = input.nextLine().trim();
			byte[] rawKey = DatatypeConverter.parseHexBinary( hexKey );

			// Make a key specification based on this key.
			X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec( rawKey );

			// Make an RSA key based on this specification
			KeyFactory keyFactory = KeyFactory.getInstance( "RSA" );
			rec.publicKey = keyFactory.generatePublic( pubKeySpec );

			// Add this user to the list of all users.
			userList.add( rec );
		}
		input.close();*/ //Old method

		// New method
		String stmt = "SELECT * FROM users;";
		PreparedStatement ps = server.conn.prepareStatement( stmt );
		ResultSet rs = executeQuery( ps );
		while ( rs.next() ) {
			UserRec rec = new UserRec();
			rec.name = rs.getString( 1 );
			rec.admin = rs.getBoolean( 2 );
			String hexKey = RSAKEY.trim();
			byte[] rawKey = DatatypeConverter.parseHexBinary( hexKey );
			X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec( rawKey );
			KeyFactory keyFactory = KeyFactory.getInstance( "RSA" );
			rec.publicKey = keyFactory.generatePublic( pubKeySpec );
			rec.password = rs.getString( 3 );
			server.userList.add( rec );
		}
	}

	@SuppressWarnings("resource")
	public void run( String[] args ) {
		ServerSocket serverSocket = null;

		try {
			setUp();
			serverSocket = new ServerSocket( PORT_NUMBER );
		} catch( Exception e ) {
			System.err.println( "Can't initialize server: " + e );
			System.exit( 1 );
		}
		while ( true ) {
			try {
				Socket sock = serverSocket.accept();

				HandleClient worker = new HandleClient( sock );
				worker.start();
			} catch ( Exception e ) {
				System.err.println( "Failure accepting client " + e );
			}
		}
	}

	private class HandleClient extends Thread {

		Socket sock;

		private HandleClient( Socket sock ) {
			this.sock = sock;
		}

		public void run() {
			try {// Get formatted input/output streams for this thread.  These can read and write
				// strings, arrays of bytes, ints, lots of things.
				DataOutputStream output = new DataOutputStream( sock.getOutputStream() );
				DataInputStream input = new DataInputStream( sock.getInputStream() );

				// Get the username.
				String username = input.readUTF();

				// Make a random sequence of bytes to use as a challenge string.
				Random rand = new Random();
				byte[] challenge = new byte [ 16 ];
				rand.nextBytes( challenge );

				// Make a session key for communiating over AES.  We use it later, if the
				// client successfully authenticates.
				byte[] sessionKey = new byte [ 16 ];
				rand.nextBytes( sessionKey );

				// Find this user.  We don't need to synchronize here, since the set of users never
				// changes.
				UserRec rec = null;
				for ( int i = 0; rec == null && i < userList.size(); i++ ) {
					if ( userList.get( i ).name.equals( username ) ) {
						rec = userList.get( i );
						output.writeUTF( "Success" );
						output.flush();
					}
				}

				// Did we find a record for this user?
				if ( rec != null ) {
					// We need this to make sure the client properly encrypted
					// the challenge.
					Cipher RSADecrypter = Cipher.getInstance( "RSA" );
					RSADecrypter.init( Cipher.DECRYPT_MODE, rec.publicKey );

					// And this to send the session key 
					Cipher RSAEncrypter = Cipher.getInstance( "RSA" );
					RSAEncrypter.init( Cipher.ENCRYPT_MODE, rec.publicKey );					

					// Get password and hash it with sha256
					String pword = sha256( new String( RSADecrypter.doFinal( getMessage( input ) ), "UTF-8" ) );

					if ( pword.equals( rec.password ) ) {
						
						putMessage( output, RSAEncrypter.doFinal( "Success".getBytes( "UTF-8" ) ) );
						
						System.out.println( username + " connected" );

						// Send the client the challenge.
						putMessage( output, challenge );
						//System.out.println( "Sent challenge" );

						// Get back the client's encrypted challenge.
						byte[] encrypted = getMessage( input );
						//System.out.println( "got message back" );

						// Make sure the client properly encrypted the challenge.
						byte[] originalMessage = RSADecrypter.doFinal( encrypted );
						for ( int i = 0; i < ( originalMessage.length < challenge.length ? originalMessage.length: challenge.length ); i++ ) {
							if ( originalMessage[ i ] != challenge[ i ] ) {
								throw new IOException( "Decryption error" );
							}
						}

						// Send the client the session key (encrypted)
						putMessage( output, RSAEncrypter.doFinal( sessionKey ) );

						// Make AES cipher objects to encrypt and decrypt with
						// the session key.
						SecretKey key = new SecretKeySpec( sessionKey, "AES" );
						Cipher AESEncrypter = Cipher.getInstance( "AES/ECB/PKCS5Padding" );
						Cipher AESDecrypter = Cipher.getInstance( "AES/ECB/PKCS5Padding" );
						AESEncrypter.init( Cipher.ENCRYPT_MODE,  key );
						AESDecrypter.init( Cipher.DECRYPT_MODE,  key );

						// Do communication with clients here

						while ( true ) {
							// Get command from client
							byte[] encryptedCommand = getMessage( input );
							byte[] commandBytes = AESDecrypter.doFinal( encryptedCommand );
							String command = new String( commandBytes, "UTF-8" );

							// Respond based on what the command is
							if ( command.equals( "exit" ) ) { // Exit
								break;
							} else if ( command.equals( "change" ) ) {
								putMessage( output, AESEncrypter.doFinal( "password".getBytes( "UTF-8" ) ) );
								String pass = new String( AESDecrypter.doFinal( getMessage( input ) ), "UTF-8" );
							} else if ( command.equals( "add" ) ) { // Add file to database
								if ( rec.admin ) {
									// Ask what organism will be entered
									putMessage( output, AESEncrypter.doFinal( "organism".getBytes( "UTF-8" ) ) );
									String organism = new String( AESDecrypter.doFinal( getMessage( input ) ), "UTF-8" );
									// Ask for a file to be sent
									putMessage( output, AESEncrypter.doFinal( "file".getBytes( "UTF-8" ) ) );
									// Read in that file
									long startTime = System.nanoTime();
									server.addHMP( organism, input, AESDecrypter);
									long endTime = System.nanoTime();
									System.out.println( "Time elapsed: " + ( ( endTime - startTime ) / 1000000 ) + " milliseconds." );
									putMessage( output, AESEncrypter.doFinal( "done".getBytes( "UTF-8" ) ) );
								} else { // Not an admin
									putMessage( output, AESEncrypter.doFinal( "NotAdmin".getBytes( "UTF-8" ) ) );
									putMessage( output, AESEncrypter.doFinal( "done".getBytes( "UTF-8" ) ) );
								}
							} else if ( command.equals( "shutdown" ) ) { // Shutdown server
								if ( rec.admin ) {
									System.out.println( "Server shutting down" );
									putMessage( output, AESEncrypter.doFinal( "shutdown".getBytes( "UTF-8" ) ) );
									System.exit( 0 );
								} else { // Not an admin
									putMessage( output, AESEncrypter.doFinal( "NotAdmin".getBytes( "UTF-8" ) ) );
								}
							} else if ( command.equals( "get" ) ) { // Get data from database
								putMessage( output, AESEncrypter.doFinal( "getInfo".getBytes( "UTF-8" ) ) );
								String[] taxa = new String( AESDecrypter.doFinal( getMessage( input ) ), "UTF-8" ).split( "\t" );
								String[] snps = new String( AESDecrypter.doFinal( getMessage( input ) ), "UTF-8" ).split( "\t" );
								putMessage( output, AESEncrypter.doFinal( getEntries( taxa, snps ).getBytes( "UTF-8" ) ) );

								putMessage( output, AESEncrypter.doFinal( "done".getBytes( "UTF-8" ) ) );
							} else {
								putMessage( output, AESEncrypter.doFinal( "badCommand".getBytes( "UTF-8" ) ) );
								putMessage( output, AESEncrypter.doFinal( "done".getBytes( "UTF-8" ) ) );
							}


						}
						System.out.println( username + " disconnected" );
					} else {
						putMessage( output, RSAEncrypter.doFinal( "Failure".getBytes( "UTF-8" ) ) );
						return;
					}
				} else {
					output.writeUTF( "Failure" );
					output.flush();
				}
			} catch ( IOException e ) {
				System.out.println( "IO Error: " + e );
			} catch( GeneralSecurityException e ){
				System.err.println( "Encryption error: " + e );
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				try {
					// Close the socket on the way out.
					sock.close();
				} catch ( Exception e ) {
				}
			}
		}
	}

	private static String getEntries( String[] taxa, String[] snps ) throws SQLException {
		ArrayList<ArrayList<String>> returnList = new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<String>> taxaPerTable = new ArrayList<ArrayList<String>>();
		for ( int i = 0; i < server.numTables; i++ ) {
			taxaPerTable.add( new ArrayList<String>() );
		}
		for ( int i = 0; i < taxa.length; i++ ) {
			for ( int j = 0; j < server.numTables; j++ ) {
				if ( Arrays.asList( server.taxa.get( j ) ).contains( taxa[ i ] ) ) {
					taxaPerTable.get( j ).add( taxa[ i ] );
				}
			}
		}
		boolean firstTable = true;
		for ( int i = 0; i < server.numTables; i++ ) {
			if ( !taxaPerTable.get( i ).isEmpty() ) {
				String stmt = "SELECT " + taxaPerTable.get( i ).get( 0 );
				String second = " FROM SNPs" + ( i + 1 ) + " WHERE RSNum IN (?";
				for ( int j = 1; j < taxaPerTable.get( i ).size(); j++ ) {
					stmt += "," + taxaPerTable.get( i ).get( j );
				}
				for ( int j = 1; j < snps.length / 2; j++ ) {
					second += ",?";
				}
				second += ");";
				stmt += second;
				PreparedStatement ps = server.conn.prepareStatement( stmt );
				/*int j;
				for ( j = 0; j < taxaPerTable.get( i ).size(); j++ ) {
					ps.setString( j + 1, taxaPerTable.get( i ).get( j ) );
				}*/
				int ind = 0;
				for ( int k = 1; k <= ps.getParameterMetaData().getParameterCount(); k++ ) {
					ps.setString( k, snps[ ind ] + snps[ ind + 1 ] );
					ind += 2;
				}
				ResultSet rs = executeQuery( ps );

				if ( firstTable ) {
					firstTable = false;
					String statement = "SELECT * FROM SNPData WHERE RSNum IN (?";
					for ( int j = 1; j < snps.length / 2; j++ ) {
						statement += ",?";
					}
					statement += ");";
					PreparedStatement prep = server.conn.prepareStatement( statement );
					ind = 0;
					for ( int j = 1; j <= prep.getParameterMetaData().getParameterCount(); j++ ) {
						prep.setString( j, snps[ ind ] + snps[ ind + 1 ] );
						ind += 2;
					}
					ResultSet result = executeQuery( prep );
					while ( result.next() ) {
						ArrayList<String> row = new ArrayList<String>();
						int index = 1;
						while ( index <= 12 ) {
							if ( index != 2 ) {
								row.add( result.getString( index++ ) );
							} else {
								row.add( result.getString( index++ ).substring( result.getString( 1 ).length() ) );
							}
						}
						returnList.add( row );
					}
				}

				int row = 0;
				while ( rs.next() ) {
					int index = 1;
					while ( index <= taxaPerTable.get( i ).size() ) {
						if ( rs.getString( index ) != null ) {
							returnList.get( row ).add( rs.getString( index++ ) );
						} else {
							returnList.get( row ).add( "N" );
							index++;
						}
					}
					row++;
				}
			}
		}
		ArrayList<String> header = new ArrayList<String>();
		header.add( "Organism" );
		header.add( "RS#" );
		header.add( "Alleles" );
		header.add( "Chromosome" );
		header.add( "Position" );
		header.add( "Strand" );
		header.add( "Assembly" );
		header.add( "Center" );
		header.add( "ProtLSID" );
		header.add( "AssayLSID" );
		header.add( "PanelLSID" );
		header.add( "QCCode" );
		for ( int i = 0; i < taxaPerTable.size(); i++ ) {
			for ( int j = 0; j < taxaPerTable.get( i ).size(); j++ ) {
				header.add( taxaPerTable.get( i ).get( j ) );
			}
		}
		returnList.add( 0, header );
		String returnStr = "";
		for ( int i = 0; i < returnList.size(); i++ ) {
			for ( int j = 0; j < returnList.get( i ).size(); j++ ) {
				if ( j != 0 ) {
					returnStr += "\t";
				}
				returnStr += returnList.get( i ).get( j );
			}
			if ( i != returnList.size() - 1 ) {
				returnStr += "\n";
			}
		}
		return returnStr;
	}

	/** Utility function to read a length then a byte array from the
    given stream.  TCP doesn't respect message boundaries, but this
    is essentially a technique for marking the start and end of
    each message in the byte stream.  This can also be used by the
    client. */
	private byte[] getMessage( DataInputStream input ) throws IOException {
		int len = input.readInt();
		byte[] msg = new byte[ len ];
		input.readFully( msg );
		return msg;
	}

	/** Function analogous to the previous one, for sending messages. */
	private void putMessage( DataOutputStream output, byte[] msg ) throws IOException {
		// Write the length of the given message, followed by its contents.
		output.writeInt( msg.length );
		output.write( msg, 0, msg.length );
		output.flush();
	}

	/**
	 * Reads a hapmap file into the database
	 * @param filename The name of the input file
	 * @param organism The organism being added to the table
	 * @throws IOException 
	 * @throws BadPaddingException 
	 * @throws IllegalBlockSizeException 
	 * @throws UnsupportedEncodingException 
	 */
	private synchronized void addHMP( String organism, DataInputStream input, Cipher AESDecrypter ) throws UnsupportedEncodingException, IllegalBlockSizeException, BadPaddingException, IOException {
		try {

			String[] header = new String( AESDecrypter.doFinal( getMessage( input ) ), "UTF-8" ).split( "\t" );
			this.addColumns( header );
			AddEntryWorker[] worker = new AddEntryWorker[ THREADS ];
			int stop = THREADS;
			while( true ) {
				for ( int i = 0; i < stop; i++ ) {
					String line = new String( AESDecrypter.doFinal( getMessage( input ) ), "UTF-8" );
					if ( line.equals( "done" ) ) {
						stop = i;
						break;
					}
					worker[ i ] = new AddEntryWorker( organism, header, line.split( "\t" ) );
					worker[ i ].start();
				}
				for ( int i = 0; i < stop; i++ ) {
					worker[ i ].join();
				}
				if ( stop != THREADS ) {
					break;
				}
			}
		} catch (FileNotFoundException | SQLException | InterruptedException e) {
			e.printStackTrace();
		}

	}

	private synchronized static void execute( PreparedStatement ps ) throws SQLException {
		ps.execute();
		ps.close();
	}

	private synchronized static ResultSet executeQuery( PreparedStatement ps ) throws SQLException {
		return ps.executeQuery();
	}

	/**
	 * Adds a single line of a hapmap file to the database
	 * @param header The header to the file
	 * @param line The current line being added
	 * @throws SQLException
	 */
	private class AddEntryWorker extends Thread {
		private String organism;
		private String[] header;
		private String[] line;

		private AddEntryWorker( String organism, String[] header, String[] line ) {
			this.organism = organism;
			this.header = header;
			this.line = line;
		}

		public void run() {
			try {
				boolean[] existance = new boolean[ header.length ];
				String check = "SELECT * FROM SNPData WHERE RSNum=?;";
				PreparedStatement prep;
				prep = conn.prepareStatement( check );
				prep.setString( 1, line[ 0 ] );
				ResultSet rs = executeQuery( prep );
				PreparedStatement ps = null;
				if ( rs.next() && rs.getString( 2 ).equalsIgnoreCase( line[ 0 ] ) ) {
					// Update
					for ( int j = 1; j <= numTables; j++ ) {
						int index = 0;
						while ( taxa.get( j - 1 ).length > index && taxa.get( j - 1 )[ index ] != null ) {
							index++;
						}
						boolean anythingInTable = false;
						String stmt = "UPDATE SNPs" + j + " SET Organism='" + organism + "'";
						for ( int i = 11; i < line.length; i++ ) {
							if ( Arrays.asList( taxa.get( j - 1 ) ).contains( header[ i ] ) ) {
								anythingInTable = true;
								stmt += ( ", `" + header[ i ] + "`=?" );
							}
						}
						if ( anythingInTable ) {
							stmt += " WHERE RSNum=?;";
							ps = conn.prepareStatement( stmt );
							//ps.setString( 1, line[ 1 ] );
							int ind = 1;
							for ( int i = 11; i < line.length; i++ ) {
								if ( Arrays.asList( taxa.get( j - 1 ) ).contains( header[ i ] ) ) {
									ps.setString( ind, line[ i ] );
									ind++;
								}
							}
							int params = ps.getParameterMetaData().getParameterCount();
							ps.setString( params, organism + line[ 0 ] );
							execute( ps );
						}
					}
				} else {
					// Add
					for ( int j = 0; j < numTables; j++ ) {
						for ( int i = 0; i < header.length; i++ ) {
							if ( Arrays.asList( taxa.get( j ) ).contains( header[ i ] ) ) {
								existance[ i ] = true;
							} else {
								existance[ i ] = false;
							}
						}
						String stmt = "INSERT INTO SNPs" + ( j + 1 ) + "( Organism, RSNum";
						String entries = "VALUES ( ?, ?";
						if ( j == 0 ) {
							String stmt2 = "INSERT INTO SNPData ( Organism, RSNum, Alleles, Chr, Pos, Strand, Assembly, Center, ProtLSID, AssayLSID, PanelLSID, QCCode ) VALUES( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? );";
							PreparedStatement ps2 = conn.prepareStatement( stmt2 );
							ps2.setString( 1, organism );
							ps2.setString( 2, organism + line[ 0 ] );
							for ( int i = 3; i <= 12; i++ ) {
								ps2.setString( i, line[ i - 2 ] );
							}
							execute( ps2 );
							ps2.close();
						}
						for ( int i = 11; i < line.length; i++ ) {
							if ( existance[ i ] ) {
								stmt += ( ", `" + header[ i ] + "`" );
								entries += ", ?";
							}
						}
						stmt += ( " ) " + entries + " );" );
						ps = conn.prepareStatement( stmt );
						ps.setString( 1, organism );
						int index = 3;
						ps.setString( 2, organism + line[ 0 ] );
						for ( int i = 11; i < line.length; i++ ) {
							if ( existance[ i ] ) {
								ps.setString( index, line[ i ] );
								index++;
							}
						}
						execute( ps );
					}
				}
				//ps.close();
				rs.close();
				prep.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Adds fields (Taxa) as needed
	 * Sorts alphabetically
	 * @param header The header to the file
	 * @throws SQLException
	 */
	private synchronized void addColumns( String[] header ) throws SQLException {
		int changed = 0;
		for ( int i = 11; i < header.length; i++ ) {
			boolean exists = false;
			for ( int j = 0; j < taxa.size(); j++ ) {
				if ( Arrays.asList( taxa.get( j ) ).contains( header[ i ] ) ) {
					exists = true;
					break;
				}
			}
			if ( !exists ) {
				if ( numTaxa != 0 && numTaxa % 1000.0 == 0 ) {
					changed++;
					numTables++;
					String stmt = "CREATE TABLE SNPs" + numTables + "(Organism VARCHAR(10) NOT NULL, RSNum VARCHAR(50), PRIMARY KEY (RSNum));";
					PreparedStatement ps = conn.prepareStatement( stmt );
					execute( ps );
					execute( conn.prepareStatement( "UPDATE tables SET NumberOfTables=" + numTables + ";" ) );
					taxa.add( new String[ 1000 ] );
				}
				String stmt = "ALTER TABLE SNPs" + numTables + " ADD `" + header[ i ] + "` VARCHAR(2);";
				PreparedStatement ps = conn.prepareStatement( stmt );
				execute( ps );
				numTaxa++;
				taxa.get( numTables - 1 )[ numTaxa - ( ( numTables - 1 ) * 1000 ) - 1 ] = header[ i ];
			}
		}
		for ( int i = 0; i < changed; i++ ) {
			String stmt = "SELECT Organism,RSNum FROM SNPData;";
			PreparedStatement ps = conn.prepareStatement( stmt );
			ResultSet rs = executeQuery( ps );
			while ( rs.next() ) {
				stmt = "INSERT INTO SNPs" + ( numTables - i ) + " ( Organism, RSNum ) VALUES ( ?,? );";
				PreparedStatement prep = conn.prepareStatement( stmt );
				prep.setString( 1, rs.getString( 1 ) );
				prep.setString( 2, rs.getString( 2 ) );
				execute( prep );
			}
		}
	}

	public static void main( String[] args ) {
		server = new Server();
		server.run( args );
	}
}
