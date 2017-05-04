package cli;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Scanner;

public class TagSequences {
	
	private String hmpName;
	private String genomeName;
	private static PrintStream outFile;
	private static ArrayList<Character> genome;
	private static ArrayList<String> chr;
	private static ArrayList<Integer> positions;
	private static Scanner file;
	
	private static int around = 100;
	
	private static void readChr( String filename, String chromosome ) {
		try {
			boolean beenThrough = false;
			System.out.println( "Reading chromosome: " + chromosome );
			file = new Scanner( new File( filename ) );
			genome = new ArrayList<Character>();
			while ( !file.nextLine().contains ( chromosome ) ) {
				if ( !file.hasNextLine() && !beenThrough ) {
					file = new Scanner( new File( filename ) );
					beenThrough = true;
				} else if ( !file.hasNextLine() && beenThrough ) {
					System.out.println( "Chromosome '" + chromosome + "' does not exist in genome file. Exiting." );
					System.exit(1);
				}
			}
			String input = file.nextLine();
			while ( !input.contains( ">" ) ) {
				for ( int i = 0; i < input.length(); i++ ) {
					genome.add( input.charAt( i ) );
				}
				if ( file.hasNextLine() ) {
					input = file.nextLine();
				} else {
					break;
				}
			}
			System.out.println( "Chromosome successfully read" );
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private static void readHMP( String filename ) {
		try {
			Scanner file = new Scanner( new File( filename ) );
			positions = new ArrayList<Integer>();
			chr = new ArrayList<String>();
			String header = file.nextLine();
			int posColumn = 3;
			int chrColumn = 2;
			if ( header.split( "\t" ).length != 1 ) {
				for ( int i = 0; i < header.length(); i++ ) {
					if ( header.split( "\t" )[ i ].equals( "pos" ) || header.split( "\t" )[ i ].equals( "Pos" ) || header.split( "\t" )[ i ].equals( "position" ) || header.split( "\t" )[ i ].equals( "Position" )) {
						posColumn = i;
						break;
					}
				}
				for ( int i = 0; i < header.length(); i++ ) {
					if ( header.split( "\t" )[ i ].equals( "chr" ) || header.split( "\t" )[ i ].equals( "Chr" ) || header.split( "\t" )[ i ].equals( "chrom" ) || header.split( "\t" )[ i ].equals( "Chrom" ) || header.split( "\t" )[ i ].equals( "Chromosome" ) || header.split( "\t" )[ i ].equals( "chromosome" ) ) {
						chrColumn = i;
						break;
					}
				}
				while ( file.hasNextLine() ) {
					String[] line = file.nextLine().split( "\t" );
					positions.add( Integer.parseInt( line[ posColumn ] ) - 1 );
					chr.add( line[ chrColumn ] );
				}
			} else {
				while ( file.hasNextLine() ) {
					String[] line = file.nextLine().split( "_" );
					positions.add( Integer.parseInt( line[ 1 ] ) - 1 );
					chr.add( line[ 0 ].substring( 1 ) );
				}
			}
			file.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private static void getTags( String filename ) throws IOException {
		readChr( filename, chr.get( 0 ) );
		int before = around;
		if ( positions.get( 0 ) < around ) {
			before = positions.get( 0 );
		}
		System.out.println( "Finding tags on chromosome " + chr.get( 0 ) );
		int after = around;
		if ( after > genome.size() - positions.get( 0 ) ) {
			after = genome.size() - positions.get( 0 );
		}
		outFile.println( ">S" + chr.get( 0 ) + "_" + ( positions.get( 0 ) + 1 ) + ":" + ( before + 1 ) + ":" + ( before + 1 + after ) );
		for ( int j = positions.get( 0 ) - before; j < positions.get( 0 ); j++ ) {
			outFile.print( genome.get( j ) );
		}
		outFile.print( genome.get( positions.get( 0 ) ) );
		for ( int j = positions.get( 0 ) + 1; j <= positions.get( 0 ) + around && j < genome.size(); j++ ) {
			outFile.print( genome.get( j ) );
		}
		outFile.println();
		for ( int i = 1; i < positions.size(); i++ ) {
			if ( !chr.get( i ).equals( chr.get( i - 1 ) ) ) {
				System.out.println( "Tags for chromosome " + chr.get( i - 1 ) + " finished" );
				readChr( filename, chr.get( i ) );
				System.out.println( "Finding tags on chromosome " + chr.get( i ) );
			}
			before = around;
			if ( positions.get( i ) < around ) {
				before = positions.get( i );
			}
			after = around;
			if ( after > genome.size() - positions.get( i ) ) {
				after = genome.size() - positions.get( i );
			}
			outFile.println( ">S" + chr.get( i ) + "_" + ( positions.get( i ) + 1 ) + ":" + ( before + 1 ) + ":" + ( before + 1 + after ) );
			for ( int j = positions.get( i ) - before; j < positions.get( i ); j++ ) {
				outFile.print( genome.get( j ) );
			}
			outFile.print( genome.get( positions.get( i ) ) );
			for ( int j = positions.get( i ) + 1; j <= positions.get( i ) + around && j < genome.size(); j++ ) {
				outFile.print( genome.get( j ) );
			}
			outFile.println();
		}
		System.out.println( "Finished finding tag sequences" );
	}
	
	public void run() throws IOException {
		readHMP( hmpName );
		getTags( genomeName );
		file.close();
	}
	
	public TagSequences( String[] args ) throws IOException {
		if ( args.length == 4 ) {
			around = Integer.parseInt( args[ 0 ] );
			hmpName = args[ 2 ];
			genomeName = args[ 1 ];
			outFile = new PrintStream( new File( args[ 3 ] ) );
		} else {
			hmpName = args[ 1 ];
			genomeName = args[ 0 ];
			outFile = new PrintStream( new File( args[ 2 ] ) );
		}
		
	}
	
	public static void usage() {
		System.out.println( "Usage: java LProgs TagSequences [Number of bases around SNP] <Genome_File.fa> <HMP_File.hmp.txt | List_of_SNPs.txt> <OutputFile.txt>");
		System.out.println( "\tProgram assumes that SNPs are sorted by chromosome. If SNPs are not sorted by chromosome" );
		System.out.println( "\tthe run will take much longer.");
		System.exit( 0 );
	}
	
	public static void main( String[] args ) {
		try {
			if ( args.length == 3 || args.length == 4 ) {
				TagSequences tags = new TagSequences( args );
				tags.run();
			} else {
				usage();
			}
		} catch ( IOException e ) {
			e.printStackTrace();
		}
	}
}
