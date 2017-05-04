package cli;

import java.io.IOException;
import gui.MainPage;

public class LosiPrograms {
	private static final String versionNumber = "2.0";

	public static final String[] PROGRAMS = {"TagSequences","VCFToHapmapForMapping","CheckCalls","UpdatePositions","FindcM"};
	private static final String[][] INPUTS = 
		{{"Number of bases around SNP","Genome file","HMP file/List of snps","Output file"},
		 {"VCF file", "Output hapmap file", "Numeric|Base|SM", "Depth", "Parent 1", "Parent 2", "Parent 1 Replacement String", "Parent 2 Replacement String", "Het Replacement String", "Missing Replacement String"},
		 {"Input VCF file","Output VCF file", "Summary file"},
		 {"Barley|Wheat|Bed file", "RS column number", "Chr column number"," Pos column number", "Input hapmap file", "Output hapmap file"},
		 {"AGP file", "Hapmap file", "Output file"}};
	private static final boolean[][] MANDATORY = 
		{{false, true, true, true},
		 {true, true, true, false, false, false, false, false, false, false},
		 {true, true, true},
		 {true, false, false, false, true, true},
		 {true, true, true}};
	private static int[] numInputs = { 4, 10, 3, 6, 3 };
	private static boolean[][] isFile = 
		{{false, true, true, true},
		 {true, true, false, false, false, false, false, false, false, false},
		 {true, true, true},
		 {true, false, false, false, true, true},
		 {true, true, true}};

	private static void usage() {
		System.err.println( "\nLosiPrograms version " + versionNumber );
		System.err.println( "Usage:" );
		System.err.println( "\tLProgs <Plugin> [Arguments]" );
		System.err.println( "\tPlugins:" );
		System.err.println( "\t\tVersion - Output version number." );
		System.err.println( "\t\tTagSequences - Get tag sequences from reference genome around SNPs." );
		System.err.println( "\t\tVCFToHapmapForMapping - Convert a vcf file into a hapmap file." );
		System.err.println( "\t\tCheckCalls - Check and attempt to fix the accuracy of vcf genotype calls." );
		System.err.println( "\t\tUpdate - Update SNPs in part 2 chromosomes so their positions reflect the\n\t\t\tcombined chromosome" );
		System.err.println( "\t\tFindcM - Find cM positions for each SNP in a hapmap file");
		System.err.println( "\tArguments:" );
		System.err.println( "\t\tEach plugin has its own set of arguments." );
		System.err.println( "\t\tRun the desired plugin with the argument \"usage\" to view its usage info." );
		System.out.println();
		System.exit( 1 );
	}

	private static void usageProg( String program ) {
		if ( program.toLowerCase().equals( "vcftohapmapformapping" ) ) {
			VCFToHapmapForMapping.usage();
		} else if ( program.toLowerCase().equals( "tagsequences" ) ) {
			TagSequences.usage();
		} else if ( program.toLowerCase().equals( "checkcalls" ) ) {
			Check_Calls.usage();
		} else if ( program.toLowerCase().equals( "update" ) || program.toLowerCase().equals( "updatepositions" ) ) {
			UpdateSNPPositions.usage();
		} else if ( program.toLowerCase().equals( "findcm" ) ) {
			FindcM.usage();
		} else {
			System.err.println( program + " is not a valid plugin." );
		}
		System.exit( 1 );
	}
	
	public static boolean[] getMandatory( int program ) {
		return MANDATORY[ program ];
	}
	
	public static String[] getInputs( int program ) {
		return INPUTS[ program ];
	}
	
	public static int getNumInputs( int program ) {
		return numInputs[ program ];
	}
	
	public static boolean[] getIsFile( int program ) {
		return isFile[ program ];
	}

	public static void main(String[] args) throws InterruptedException, IOException {
		if ( args.length == 0 ) {
			usage();
		} else if ( args.length > 1 && args[ 1 ].toLowerCase().equals( "usage" ) ) {
			usageProg( args[ 0 ] );
		} else if ( args.length > 0 && args[ 0 ].toLowerCase().equals( "gui" ) ) {
			new MainPage();
		} else if ( args[ 0 ].toLowerCase().equals( "version" ) ) {
			System.out.println( "LosiPrograms version " + versionNumber );
			System.exit( 0 );
		} else if ( args[ 0 ].toLowerCase().equals( "tagsequences" ) ) {
			String[] inputArgs = new String[ args.length - 1 ];
			for ( int i = 0; i < inputArgs.length; i++ ) {
				inputArgs[ i ] = args[ i + 1 ];
			}
			new TagSequences( inputArgs ).run();
		} else if ( args[ 0 ].toLowerCase().equals( "vcftohapmapformapping" ) ) {
			if ( args.length < 3 ) {
				usageProg( "VCFToHapmapForMapping" );
			}
			String[] inputArgs = new String[ args.length - 1 ];
			for ( int i = 0; i < inputArgs.length; i++ ) {
				inputArgs[ i ] = args[ i + 1 ];
			}
			new VCFToHapmapForMapping( inputArgs ).run();
		} else if ( args[ 0 ].toLowerCase().equals( "checkcalls" ) ) {
			String[] inputArgs = new String[ args.length - 1 ];
			for ( int i = 0; i < inputArgs.length; i++ ) {
				inputArgs[ i ] = args[ i + 1 ];
			}
			new Check_Calls( inputArgs ).run();
		} else if ( args[ 0 ].toLowerCase().equals( "findcm" ) ) {
			String[] inputArgs = new String[ args.length - 1 ];
			for ( int i = 0; i < inputArgs.length; i++ ) {
				inputArgs[ i ] = args[ i + 1 ];
			}
			new FindcM( inputArgs ).run();
		} else if ( args[ 0 ].toLowerCase().equals( "update" ) || args[ 0 ].toLowerCase().equals( "updatepositions" ) ) {
			String[] inputArgs = new String[ args.length - 1 ];
			if ( args.length > 1 ) {
				for ( int i = 0; i < inputArgs.length; i++ ) {
					inputArgs[ i ] = args[ i + 1 ];
				}
			}
			new UpdateSNPPositions( inputArgs ).run();
		}
	}
}
