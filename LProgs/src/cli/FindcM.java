package cli;
import java.io.*;
import java.util.*;

public class FindcM {
	
	private static String[] args;
	private static ArrayList<ArrayList<String>> AGP;

	private static void readAGP( String AGPName ) {
		try {
			Scanner input = new Scanner( new File( AGPName ) );
			AGP = new ArrayList<ArrayList<String>>();
			while ( input.hasNextLine() ) {
				String line = input.nextLine();
				AGP.add( new ArrayList<String>() );
				for ( int i = 0; i < line.split( "\t" ).length; i++ ) {
					AGP.get( AGP.size() - 1 ).add( line.split( "\t" )[ i ] );
				}
			}
			input.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	private static void findPopseq( String HMPName ) {
		try {
			Scanner input = new Scanner( new File( HMPName ) );
			PrintStream output = null;
			try {
				output = new PrintStream( new File( args[ 2 ] ) );
			} catch ( FileNotFoundException e ) {
				e.printStackTrace();
			}
			input.nextLine();
			while ( input.hasNextLine() ) {
				String[] line  = input.nextLine().split( "\t" );
				String chr = line[ 2 ];
				int pos = Integer.parseInt( line[ 3 ] );
				for ( int i = 1; i < AGP.size(); i++ ) {
					if ( AGP.get( i ).get( 1 ).contains( chr ) && Integer.parseInt( AGP.get( i ).get( 2 ) )
							<= pos && Integer.parseInt( AGP.get( i ).get( 3 ) ) >= pos ) {
						output.print( line[ 0 ] );
						for ( int j = 0; j < AGP.get( i ).size(); j ++ ) {
							output.print( "\t" + AGP.get( i ).get( j ) );
						}
						output.println();
						break;
					}
				}
			}
			input.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public static void usage() {
		System.err.println( "Usage:" );
		System.err.println( "\t<ProgramName> <AGPFile> <HapmapFile> > <OutputFile>" );
	}
	
	public void run() {
		readAGP( args[ 0 ] );
		System.err.println( "Done reading" );
		findPopseq( args[ 1 ] );
	}
	
	public FindcM( String[] argsIn ) {
		args = argsIn;
	}
	
	public static void main( String[] args ) {
		new FindcM( args ).run();
	}

}
