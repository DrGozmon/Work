package cli;
import java.io.*;
import java.util.*;

public class UpdateSNPPositions {
	private static final String wheatBed = "/home/gbg_lab_admin/Array_60TB/GBS_Reference_Genomes/Wheat_IWGSC_WGA_v1.0/Wheat_IWGSC_WGA_v1.0_pseudomolecules/161010_Chinese_Spring_v1.0_pseudomolecules_parts_to_chr.bed";
	private static final String barleyBed = "/home/gbg_lab_admin/Array_60TB/GBS_Reference_Genomes/Barley_Jan2016/pseudomolecules/150831_barley_pseudomolecules_parts_to_full_chromosomes.bed";
	private static String bed;
	private static String prevChr = "None";
	private static int part1Length = 0;
	private static boolean barley = false;

	private String[] args;

	private static int RSCol = 0, chrCol = 2, posCol = 3;

	public static void usage() {
		System.err.println( "Usage:");
		System.err.println( "\t<ProgramName> <barley|wheat|bedFile.bed> [RS Column] [Chr Column] [Position Column] <input.hmp.txt> <output.hmp.txt>");
		System.err.println( "\t\tColumns are indexes starting at 0");
		System.exit(1);
	}

	private static int getLength( String chr ) {
		int length = -1;
		try {
			Scanner bedFile = new Scanner( new File( bed ) );
			while ( bedFile.hasNextLine() ) {
				String[] line = bedFile.nextLine().split( "\t" );
				if ( line[ 0 ].contains( chr + "_part1"  ) ) {
					length = Integer.parseInt( line[ 2 ] );
					break;
				}
			}
			bedFile.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit( 1 );
		}
		//System.err.println( length );
		return length;
	}

	public void run() {
		if ( args[ 0 ].toLowerCase().equals( "barley" ) ) {
			bed = barleyBed;
			barley = true;
		} else if ( args[ 0 ].toLowerCase().equals( "wheat" ) ) {
			bed = wheatBed;
			barley = false;
		} else {
			bed = args[ 0 ];
		}
		Scanner input = null;
		try {
			if ( args.length > 3 ) {
				input = new Scanner( new File( args[ 4 ] ) );
			} else {
				input = new Scanner( new File( args[ 1 ] ) );
			}
		} catch ( FileNotFoundException e ) {
			e.printStackTrace();
		}
		PrintStream outFile = null;
		try {
			if ( args.length > 3 ) {
				outFile = new PrintStream( new File( args[ 5 ] ) );
			} else {
				outFile = new PrintStream( new File( args[ 2 ] ) );
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		String header = input.nextLine();
		do {
			outFile.println( header );
			if ( header.startsWith( "##" ) ) {
				header = input.nextLine();
				RSCol = 2;
				chrCol = 0;
				posCol = 1;
			}
			if ( header.startsWith( "#C")) {
				outFile.println( header );
				break;
			}
		} while ( header.startsWith( "#" ) );
		if ( args.length > 3 ) {
			RSCol = Integer.parseInt( args[ 1 ] );
			chrCol = Integer.parseInt( args[ 2 ] );
			posCol = Integer.parseInt( args[ 3 ] );
		}
		while ( input.hasNextLine() ) {
			String[] line = input.nextLine().split( "\t" );
			if ( barley ) {
				//Barley
				if ( line[chrCol].contains( "H" ) && line[ chrCol ].split( "H" )[ 1 ].equals( "2" ) ) {
					if ( !prevChr.equals( line[ chrCol ] ) ) {
						part1Length = getLength( line[ chrCol ].split( "H" )[ 0 ] + "H" );
						if ( part1Length == -1 ) {
							System.err.println( "Chromosome " + line[ chrCol ] + " does not exist in the BED file." );
							System.exit( 1 );
						}
						prevChr = line[ chrCol ];
					}
					int newLen = part1Length + Integer.parseInt( line[ posCol ] );
					line[ posCol ] = Integer.toString( newLen );
					line[ RSCol ] = line[ RSCol ].split( "H" )[ 0 ] + "H_" + Integer.toString( newLen );
					line[ chrCol ] = line[ chrCol ].split( "H" )[ 0 ] + "H";
				} else {
					line[ RSCol ] = line[ RSCol ].split( "H" )[ 0 ] + "H_" + line[ posCol ];
					line[ chrCol ] = line[ chrCol ].split( "H" )[ 0 ] + "H";
				}
			} else {
				//Wheat
				if ( line[ chrCol ].contains( "_" ) && line[ chrCol ].split( "_" )[ 1 ].equals( "2" ) ) {
					if ( !prevChr.equals( line[ chrCol ] ) ) {
						part1Length = getLength( line[ chrCol ].split( "_" )[ 0 ] );
						if ( part1Length == -1 ) {
							System.err.println( "Chromosome " + line[ chrCol ] + " does not exist in the BED file." );
							System.exit( 1 );
						}
						prevChr = line[ chrCol ];
					}
					int newLen = part1Length + Integer.parseInt( line[ posCol ] );
					line[ posCol ] = Integer.toString( newLen );
					line[ RSCol ] = line[ RSCol ].split( "_" )[ 0 ] + "_" + Integer.toString( newLen );
					line[ chrCol ] = line[ chrCol ].split( "_" )[ 0 ];
				} else {
					line[ RSCol ] = line[ RSCol ].split( "_" )[ 0 ] + "_" + line[ posCol ];
					line[ chrCol ] = line[ chrCol ].split( "_" )[ 0 ];
				}
			}
			String output = line[ 0 ];
			for ( int i = 1; i < line.length; i++ ) {
				output += "\t" + line[ i ];
			}
			outFile.println( output );
		}
		input.close();
	}

	public UpdateSNPPositions(String[] args) {
		this.args = args;
	}

	public static void main( String[] args ) {
		new UpdateSNPPositions( args ).run();
	}

}
