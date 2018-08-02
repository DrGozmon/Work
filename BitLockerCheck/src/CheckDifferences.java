import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Properties;
import java.util.Scanner;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;  

public class CheckDifferences {
	
	private static boolean debug = false;
	
	private static final String FOLDER = "\\\\wrcorp-file03\\IT\\Documentation\\BitLocker\\";
	private static final String OUTPUTFILENAME = "output.txt";
	private static final String DATABASEFILENAME = "Database.csv";
	private static final String ATTACHMENT = "Attachment.csv";
	private static final String TO = "ksenter@withersravenel.com";
	private static final String FROM = "BitLockerStatus@withersravenel.com";
	private static final String HOST = "10.10.10.31";
	
	private static ArrayList<String[]> report;
	private static ArrayList<String[]> database;
	private static ArrayList<String[]> changed;
	private static ArrayList<String[]> notSeen;

	private static void sortUsedSpaceOnly( ArrayList<String[]> usedSpaceOnly ) {
		Collections.sort( usedSpaceOnly, new Comparator<String[]>() {
			public int compare( String[] strings1, String[] strings2 ) {
				return strings1[ 1 ].trim().compareTo( strings2[ 1 ].trim() );
			}
		});
	}
	
	private static void sortReport() {
		Collections.sort( report, new Comparator<String[]>() {
			public int compare( String[] strings1, String[] strings2 ) {
				return strings1[ 0 ].trim().compareTo( strings2[ 0 ].trim() );
			}
		});
	}
	
	private static void sortNotSeen() {
		Collections.sort( notSeen, new Comparator<String[]>() {
			public int compare( String[] strings1, String[] strings2 ) {
				return strings1[ 2 ].trim().compareTo( strings2[ 2 ].trim() );
			}
		});
	}

	private static int searchReport( String[] strings ) {
		for ( int i = 0; i < report.size(); i++ ) {
			if ( strings[ 0 ].trim().equals( report.get( i )[ 0 ].trim() ) ) {
				return i;
			}
		}
		return -1;
	}

	private static int searchDatabase( String[] strings ) {
		for ( int i = 0; i < database.size(); i++ ) {
			if ( strings[ 0 ].trim().equals( database.get( i )[ 0 ].trim() ) ) {
				return i;
			}
		}
		return -1;
	}
	
	private static int searchChanged( String[] strings ) {
		for ( int i = 0; i < changed.size(); i++ ) {
			if ( strings[ 0 ].trim().equals( changed.get( i )[ 0 ].trim() ) ) {
				return i;
			}
		}
		return -1;
	}

	private static void modifyDatabase() {
		changed = new ArrayList<String[]>();
		for ( int i = 0; i < database.size(); i++ ) {
			if ( database.get( i )[ 1 ].trim().equals( "Deleted" ) ) {
				database.remove( i );
			}
		}

		for ( int i = 0; i < database.size(); i++ ) {
			int index = searchReport( database.get( i ) );
			if ( index == -1 ) {
				changed.add( new String[ 4 ] );
				changed.get( changed.size() - 1 )[ 0 ] = database.get( i )[ 0 ].trim();
				changed.get( changed.size() - 1 )[ 1 ] = "Deleted";
				changed.get( changed.size() - 1 )[ 2 ] = "Deleted";
				changed.get( changed.size() - 1 )[ 3 ] = database.get( i )[ 2 ].trim();
				database.get( i )[ 1 ] = "Deleted";
			}
		}

		for ( int i = 0; i < report.size(); i++ ) {
			int index = searchDatabase( report.get( i ) );
			if ( index == -1 ) {
				database.add( report.get( i ) );
				changed.add( new String[ 4 ] );
				changed.get( changed.size() - 1 )[ 0 ] = report.get( i )[ 0 ].trim();
				changed.get( changed.size() - 1 )[ 1 ] = "Empty";
				changed.get( changed.size() - 1 )[ 2 ] = report.get( i )[ 1 ].trim();
				changed.get( changed.size() - 1 )[ 3 ] = report.get( i )[ 2 ].trim();
			} else if ( report.get( i )[ 1 ].startsWith( "Not Found" ) ) {
				continue;
			} else {
				if ( !database.get( index )[ 1 ].trim().equals( report.get( i )[ 1 ].trim() ) ) {
					changed.add( new String[ 4 ] );
					changed.get( changed.size() - 1 )[ 0 ] = report.get( i )[ 0 ].trim();
					changed.get( changed.size() - 1 )[ 1 ] = database.get( index )[ 1 ].trim();
					changed.get( changed.size() - 1 )[ 2 ] = report.get( i )[ 1 ].trim();
					changed.get( changed.size() - 1 )[ 3 ] = report.get( i )[ 2 ].trim();
				}
				database.set( index, report.get( i ) );
			}
		}
	}

	private static void generateAttachment() throws FileNotFoundException {
		ArrayList<String[]> decrypted = new ArrayList<String[]>();
		ArrayList<String[]> encryptionPaused = new ArrayList<String[]>();
		ArrayList<String[]> encryptionInProgress = new ArrayList<String[]>();
		ArrayList<String[]> protOff = new ArrayList<String[]>();
		ArrayList<String[]> other = new ArrayList<String[]>();
		ArrayList<String[]> usedSpaceOnly = new ArrayList<String[]>();
		ArrayList<String[]> encrypted = new ArrayList<String[]>();
		notSeen = new ArrayList<String[]>();
		for ( int i = 0; i < database.size(); i++ ) {
			int index = searchChanged( database.get( i ) );
			if ( index == -1 ) {
				if ( database.get( i )[ 1 ].trim().equals( "Fully Decrypted" ) ) {
					decrypted.add( database.get( i ) );
				} else if ( database.get( i )[ 1 ].trim().equals( "Fully Encrypted" ) ) {
					encrypted.add( database.get( i ) );
				} else if ( database.get( i )[ 1 ].trim().equals( "Encryption Paused" ) ) {
					encryptionPaused.add( database.get( i ) );
				} else if ( database.get( i )[ 1 ].trim().equals( "Encryption in Progress" ) ) {
					encryptionInProgress.add( database.get( i ) );
				} else if ( database.get( i )[ 1 ].trim().startsWith( "Fully Encrypted - P" ) ) {
					protOff.add( database.get( i ) );
				} else if ( database.get( i )[ 1 ].trim().startsWith( "Used Space Only Encrypted" ) ) {
					usedSpaceOnly.add( database.get( i ) );
				} else if ( ( System.currentTimeMillis() / 1000 ) - ( Long.parseLong( database.get( i )[ 2 ] ) / 1000 ) > 2592000 ) {
					notSeen.add( database.get( i ) );
				} else if ( database.get( i )[ 1 ].trim().startsWith( "Not" ) ) {
					// Don't show computers that aren't visible right now but haven't been offline for more than 30 days.
				} else {
					other.add( database.get( i ) );
				}
			}
		}

		
		PrintStream out = new PrintStream( new File( FOLDER + ATTACHMENT ) );
		
		// Recently changed
		if ( changed.size() != 0 ) {
			out.println( "Recently Changed" );
			out.println( "Hostname,Previous_Status,Current_Status,Timestamp");
			for ( int i = 0; i < changed.size(); i++ ) {
				Date time = new Date( Long.parseLong( changed.get( i )[ 3 ] ) );
				out.println( changed.get( i )[ 0 ].trim() + "," + changed.get( i )[ 1 ].trim() + "," + changed.get( i )[ 2 ].trim() + "," + time );
			}
			out.println();
		}

		// Not seen recently
		if ( notSeen.size() != 0 ) {
			sortNotSeen();
			out.println( "Not Seen Recently ( >30 days )" );
			out.println( "Hostname,Last_Seen" );
			for ( int i = 0; i < notSeen.size(); i++ ) {
				Date time = new Date( Long.parseLong( notSeen.get( i )[ 2 ] ) );
				out.println( notSeen.get( i )[ 0 ].trim() + "," + time );
			}
			out.println();
		}
		// Decrypted
		if ( decrypted.size() != 0 ) {
			out.println( "Fully Decrypted" );
			for ( int i = 0; i < decrypted.size(); i++ ) {
				out.println( decrypted.get( i )[ 0 ].trim() );
			}
			out.println();
		}
		
		// Paused
		if ( encryptionPaused.size() != 0 ) {
			out.println( "Encryption Paused" );
			for ( int i = 0; i < encryptionPaused.size(); i++ ) {
				out.println( encryptionPaused.get( i )[ 0 ].trim() );
			}
			out.println();
		}
		
		// In progress
		if ( encryptionInProgress.size() != 0 ) {
			out.println( "Encryption in Progress" );
			for ( int i = 0; i < encryptionInProgress.size(); i++ ) {
				out.println( encryptionInProgress.get( i )[ 0 ].trim() );
			}
			out.println();
		}
		
		// Encrypted prot off
		if ( protOff.size() != 0 ) {
			out.println( "Fully Encrypted - Protection Off" );
			for ( int i = 0; i < protOff.size(); i++ ) {
				out.println( protOff.get( i )[ 0 ].trim() );
			}
			out.println();
		}
		
		// Other
		if ( other.size() != 0 ) {
			out.println( "Other" );
			out.println( "Hostname,Status" );
			for ( int i = 0; i < other.size(); i++ ) {
				out.println( other.get( i )[ 0 ].trim() + "," + other.get( i )[ 1 ].trim() );
			}
			out.println();
		}
		
		// Used space only
		if ( usedSpaceOnly.size() != 0 ) {
			sortUsedSpaceOnly( usedSpaceOnly );
			out.println( "Used Space Only Encrypted" );
			out.println( "Hostname,Status" );
			for ( int i = 0; i < usedSpaceOnly.size(); i++ ) {
				out.println( usedSpaceOnly.get( i )[ 0 ].trim() + "," + usedSpaceOnly.get( i )[ 1 ].trim() );
			}
			out.println();
		}
		
		// Encrypted
		if ( encrypted.size() != 0 ) {
			out.println( "Fully Encrypted" );
			for ( int i = 0; i < encrypted.size(); i++ ) {
				out.println( encrypted.get( i )[ 0 ].trim() );
			}
			out.println();
		}
		
		
		
		/*PrintStream out = new PrintStream( new File( FOLDER + ATTACHMENT ) );
		out.println( "Hostname,Previous_Status,Current_Status,Timestamp");
		for ( int i = 0; i < changed.size(); i++ ) {
			Date time = new Date( Long.parseLong( changed.get( i )[ 3 ] ) );
			out.println( changed.get( i )[ 0 ] + "," + changed.get( i )[ 1 ] + "," + changed.get( i )[ 2 ] + "," + time );
		}*/
		out.close();
	}
	
	private static void report() throws FileNotFoundException {
		try {
			Scanner in = new Scanner( new File( FOLDER + OUTPUTFILENAME ) );
			in.nextLine();
			in.nextLine();
			in.nextLine();
			report = new ArrayList<String[]>();
			String line = in.nextLine();
			long time = System.currentTimeMillis();
			do {
				report.add( new String[ 3 ] );
				report.get( report.size() - 1 )[ 0 ] = line.split( "   " )[ 0 ];
				report.get( report.size() - 1 )[ 1 ] = line.split( "   " )[ 1 ];
				report.get( report.size() - 1 )[ 2 ] = Long.toString( time );
				if ( !in.hasNextLine() ) {
					break;
				}
				line = in.nextLine();
			} while ( !line.equals( "" ) );
			in.close();

			sortReport();

			File databaseFile = new File( FOLDER + DATABASEFILENAME );

			in = new Scanner( databaseFile );
			database = new ArrayList<String[]>();
			line = in.nextLine();
			do {
				database.add( new String[ 3 ] );
				database.get( database.size() - 1 )[ 0 ] = line.split( "," )[ 0 ];
				database.get( database.size() - 1 )[ 1 ] = line.split( "," )[ 1 ];
				database.get( database.size() - 1 )[ 2 ] = line.split( "," )[ 2 ];
				if ( !in.hasNextLine() ) {
					break;
				}
				line = in.nextLine();
			} while ( !line.equals( "" ) );
			in.close();

			modifyDatabase();

			PrintStream out = new PrintStream( databaseFile );
			for ( int i = 0; i < database.size(); i++ ) {
				out.println( database.get( i )[ 0 ] + "," + database.get( i )[ 1 ] + "," + database.get( i )[ 2 ] );
			}

			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		generateAttachment();
	}
	
	private static void email() throws FileNotFoundException {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		Properties props = System.getProperties();
		props.put( "mail.smtp.host", HOST );
		props.put( "mail.smtp.port", "25");
		props.put( "mail.debug", debug );
		props.put( "mail.smtp.auth", "false" );
		
		Session session = Session.getDefaultInstance( props ); 
		
		try {
			MimeMessage message = new MimeMessage( session );
			message.setFrom( new InternetAddress( FROM ) );
			message.addRecipient( Message.RecipientType.TO, new InternetAddress( TO ) );
			message.setSubject( "Bitlocker Encryption Report - " + dateFormat.format( date ) );
			BodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setText( "BitLocker encryption report for " + dateFormat.format( date) );
			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart( messageBodyPart );
			
			messageBodyPart = new MimeBodyPart();
			DataSource source = new FileDataSource( FOLDER + ATTACHMENT );
			messageBodyPart.setDataHandler( new DataHandler (source ) );
			messageBodyPart.setFileName( ATTACHMENT );
			multipart.addBodyPart( messageBodyPart );
			 
			message.setContent( multipart );

			Transport.send( message );
			System.out.println( "Message sent successfully." );
			//File outFile = new File( FOLDER + ATTACHMENT );
			//outFile.delete();
		} catch ( MessagingException mex ) {
			mex.printStackTrace();
		}
	}

	public static void main( String[] args ) {
		if ( args.length > 0 ) {
			debug = Boolean.parseBoolean( args[ 0 ] );
		}
		try {
			report();
			email();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

}
