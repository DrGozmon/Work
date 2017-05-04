package cli;
import java.io.*;
import java.util.*;

public class VCFToHapmapForMapping {
	
	private static final String versionNumber = "3.0";

	private String[] args;
	
	private static ArrayList<String[]> hmp;
	private static Scanner input;
	private static Object lock = new Object();
	private static int depth = 5;
	private static String parent1, parent2;
	private static int p1Col = 9, p2Col = 10;
	private static int numTaxa;
		
	private static String p1Numeric = "0";
	private static String p2Numeric = "2";
	private static String hetNumeric = "1";
	private static String missingNumeric = "-1";
	
	private static String p1Base = "A";
	private static String p2Base = "C";
	private static String hetBase = "M";
	private static String missingBase = "N";
	
	private static String getHet( String ref, String alt ) {
		if ( ( ref.equals( "A" ) && alt.equals( "G" ) ) || ( ref.equals( "G" ) && alt.equals( "A" ) ) ) {
			return "R";
		} else if ( ( ref.equals( "C" ) && alt.equals( "T" ) ) || ( ref.equals( "T" ) && alt.equals( "C" ) ) ) {
			return "Y";
		} else if ( ( ref.equals( "G" ) && alt.equals( "C" ) ) || ( ref.equals( "C" ) && alt.equals( "G" ) ) ) {
			return "S";
		} else if ( ( ref.equals( "A" ) && alt.equals( "T" ) ) || ( ref.equals( "T" ) && alt.equals( "A" ) ) ) {
			return "W";
		} else if ( ( ref.equals( "G" ) && alt.equals( "T" ) ) || ( ref.equals( "T" ) && alt.equals( "G" ) ) ) {
			return "K";
		} else if ( ( ref.equals( "A" ) && alt.equals( "C" ) ) || ( ref.equals( "C" ) && alt.equals( "A" ) ) ) {
			return "M";
		} else if ( ref.equals( "-" ) || ref.equals( "+" ) || ref.equals( "0" ) ) {
			return alt;
		} else if ( alt.equals( "-" ) || alt.equals( "+" ) || alt.equals( "0" ) ) {
			return ref;
		}
		return "-1";
	}
	
	public static class NumericWorker extends Thread {
		
		public int workerNum;
		public String filename;
		
		public NumericWorker( int workerNum, String filename ) {
			this.filename = filename;
			this.workerNum = workerNum;
		}
		
		public void run() {
			System.out.println( "Worker " + workerNum + " started." );
			while( true ) {
				String currentLine;
				int lineNum = 0;
				String p1, p2;
				boolean write = false;
				synchronized( lock ) {
					if ( !input.hasNextLine() ) {
						System.out.println( "Worker " + workerNum + " finished." );
						break;
					}
					currentLine = input.nextLine();
					if ( currentLine.equals( "" ) ) {
						System.out.println( "Worker " + workerNum + " finished." );
						break;
					}
					p1 = currentLine.split( "\t" )[ p1Col ].split( ":" )[ 0 ];
					p2 = currentLine.split( "\t" )[ p2Col ].split( ":" )[ 0 ];
					String p1DepthString = currentLine.split( "\t" )[ p1Col ].split( ":" )[ 2 ];
					String p2DepthString = currentLine.split( "\t" )[ p2Col ].split( ":" )[ 2 ];
					if ( !p1DepthString.equals( "." ) && !p2DepthString.equals( "." ) && !p1.equals( p2 ) && !p1.equals( "./." ) && !p2.equals( "./." ) && !p1.equals( "0/1" ) && !p1.equals( "1/0" ) && !p2.equals( "0/1" ) && !p2.equals( "1/0" ) ) {
						lineNum = hmp.size();
						hmp.add( new String[ numTaxa + 6 ] );
						write = true;
					}
				}

				if ( write ) {
					String chr = currentLine.split( "\t" )[ 0 ];
					String position = currentLine.split( "\t" )[ 1 ];
					String rs = currentLine.split( "\t" )[ 2 ];
					String ref_allele = currentLine.split( "\t" )[ 3 ];
					String alt_allele = currentLine.split( "\t" )[ 4 ];
					String p1Call = null, p2Call = null;
					
					// Set parent 1 calls
					if ( p1.equals( "0/0" ) ) {
						p1Call = p1Numeric;
					} else if ( p1.equals( "1/1" ) ) {
						p1Call = p1Numeric;
					} else {
						System.err.println( "Something went wrong with ignoring hets/missing/monomorphic for parent 1." );
						System.exit( 1 );
					}
					
					// Set parent 2 calls
					if ( p2.equals( "0/0" ) ) {
						p2Call = p2Numeric;
					} else if ( p2.equals( "1/1" ) ) {
						p2Call = p2Numeric;
					} else {
						System.err.println( "Something went wrong with ignoring hets/missing/monomorphic for parent 2." );
						System.exit( 1 );
					}
					
					String[] temp = new String[ numTaxa ];
					int col = 0;
					for ( int i = 9; i < currentLine.split( "\t" ).length; i++ ) {
						if ( i != p1Col && i != p2Col ) {
							String currentDepth = currentLine.split( "\t" )[ i ].split( ":" )[ 2 ];
							if ( currentDepth.equals( "." ) ) {
								temp[ col ] = missingNumeric;
							} else {
								if ( currentLine.split( "\t" )[ i ].split( ":" )[ 0 ].equals( p1 ) ) {
									temp[ col ] = p1Numeric;
								} else if ( currentLine.split( "\t" )[ i ].split( ":" )[ 0 ].equals( p2 ) ) {
									temp[ col ] = p2Numeric;
								} else if ( currentLine.split( "\t" )[ i ].split( ":" )[ 0 ].equals( "1/0" ) || currentLine.split( "\t" )[ i ].split( ":" )[ 0 ].equals( "0/1" ) ) {
									temp[ col ] = hetNumeric;
								} else {
									temp[ col ] = missingNumeric;
								}
							}
							col++;
						}
					}

					synchronized( lock ) {
						hmp.get( lineNum )[ 0 ] = rs;
						hmp.get( lineNum )[ 1 ] = ref_allele + "/" + alt_allele;
						hmp.get( lineNum )[ 2 ] = chr;
						hmp.get( lineNum )[ 3 ] = position;
						hmp.get( lineNum )[ 4 ] = p1Call;
						hmp.get( lineNum )[ 5 ] = p2Call;
						for ( int i = 0; i < temp.length; i++ ) {
							hmp.get( lineNum )[ i + 6 ] = temp[ i ];
						}
					}
				}
			}
		}
	}

	public static class BaseWorker extends Thread {
		
		public int workerNum;
		public String filename;
		
		public BaseWorker ( int workerNum, String filename ) {
			this.filename = filename;
			this.workerNum = workerNum;
		}
		
		public void run() {
			System.out.println( "Worker " + workerNum + " started." );
			while ( true ) {
				String currentLine;
				int lineNum = 0;
				String p1, p2;
				int p1Depth, p2Depth;
				String p1DepthString, p2DepthString;
				boolean write = false;
				synchronized( lock ) {
					if ( !input.hasNextLine() ) {
						System.out.println( "Worker " + workerNum + " finished." );
						break;
					}
					currentLine = input.nextLine();
					if ( currentLine.equals( "" ) ) {
						System.out.println( "Worker " + workerNum + " finished." );
						break;
					}
					p1 = currentLine.split( "\t" )[ p1Col ].split( ":" )[ 0 ];
					p2 = currentLine.split( "\t" )[ p2Col ].split( ":" )[ 0 ];
					p1DepthString = currentLine.split( "\t" )[ p1Col ].split( ":" )[ 2 ];
					p2DepthString = currentLine.split( "\t" )[ p2Col ].split( ":" )[ 2 ];
					if ( !p1DepthString.equals( "." ) && !p2DepthString.equals( "." ) && !p1.equals( p2 ) && !p1.equals( "./." ) && !p2.equals( "./." ) && !p1.equals( "0/1" ) && !p1.equals( "1/0" ) && !p2.equals( "0/1" ) && !p2.equals( "1/0" ) ) {
						lineNum = hmp.size();
						hmp.add( new String[ numTaxa + 6 ] );
						write = true;
					}
				}

				if ( write ) {
					p1Depth = Integer.parseInt( p1DepthString );
					p2Depth = Integer.parseInt( p2DepthString );
					String chr = currentLine.split( "\t" )[ 0 ];
					String position = currentLine.split( "\t" )[ 1 ];
					String rs = currentLine.split( "\t" )[ 2 ];
					String ref_allele = currentLine.split( "\t" )[ 3 ];
					String alt_allele = currentLine.split( "\t" )[ 4 ];
					String p1Call = null, p2Call = null;
					
					// Set parent 1 calls
					if ( p1.equals( "0/0" ) || p1.equals( "1/1" ) ) {
						if ( p1Depth > depth ) {
							p1Call = p1Base;
						} else {
							p1Call = p1Base.toLowerCase();
						}
					} else {
						System.err.println( "Something went wrong with ignoring hets/missing/monomorphic for parent 1." );
						System.exit( 1 );
					}
					
					// Set parent 2 calls
					if ( p2.equals( "0/0" ) || p2.equals( "1/1" ) ) {
						if ( p2Depth > depth ) {
							p2Call = p2Base;
						} else {
							p2Call = p2Base.toLowerCase();
						}
					} else {
						System.err.println( "Something went wrong with ignoring hets/missing/monomorphic for parent 2." );
						System.exit( 1 );
					}
					
					int currentDepth;
					String[] temp = new String[ numTaxa ];
					int col = 0;
					for ( int i = 9; i < currentLine.split( "\t" ).length; i++ ) {
						if ( i != p1Col && i != p2Col ) {
							String depthString = currentLine.split( "\t" )[ i ].split( ":" )[ 2 ];
							if ( depthString.equals( "." ) ) {
								temp[ col ] = missingBase;
							} else {
								currentDepth = Integer.parseInt( currentLine.split( "\t" )[ i ].split( ":" )[ 2 ] );
								if ( currentLine.split( "\t" )[ i ].split( ":" )[ 0 ].equals( p1 ) ) {
									if ( currentDepth > depth ) {
										temp[ col ] = p1Base;
									} else {
										temp[ col ] = p1Base.toLowerCase();
									}
								} else if ( currentLine.split( "\t" )[ i ].split( ":" )[ 0 ].equals( p2 ) ) {
									if ( currentDepth > depth ) {
										temp[ col ] = p2Base;
									} else {
										temp[ col ] = p2Base.toLowerCase();
									}
								} else if ( currentLine.split( "\t" )[ i ].split( ":" )[ 0 ].equals( "1/0" ) || currentLine.split( "\t" )[ i ].split( ":" )[ 0 ].equals( "0/1" ) ) {
									if ( currentDepth > depth ) {
										temp[ col ] = hetBase;
									} else {
										temp[ col ] = hetBase.toLowerCase();
									}
								} else {
									temp[ col ] = missingBase;
								}
							}
							col++;
						}
					}
					
					synchronized( lock ) {
						hmp.get( lineNum )[ 0 ] = rs;
						hmp.get( lineNum )[ 1 ] = ref_allele + "/" + alt_allele;
						hmp.get( lineNum )[ 2 ] = chr;
						hmp.get( lineNum )[ 3 ] = position;
						hmp.get( lineNum )[ 4 ] = p1Call;
						hmp.get( lineNum )[ 5 ] = p2Call;
						for ( int i = 0; i < temp.length; i++ ) {
							hmp.get( lineNum )[ i + 6 ] = temp[ i ];
						}
					}
				}
			}
		}
	}
	
	public static class SoftMaskWorker extends Thread {
		
		public int workerNum;
		public String filename;
		
		public SoftMaskWorker ( int workerNum, String filename ) {
			this.filename = filename;
			this.workerNum = workerNum;
		}
		
		public void run() {
			System.out.println( "Worker " + workerNum + " started." );
			while( true ) {
				String currentLine;
				int lineNum = 0;
				synchronized( lock ) {
					if ( !input.hasNextLine() ) {
						System.out.println( "Worker " + workerNum + " finished." );
						break;
					}
					currentLine = input.nextLine();
					if ( currentLine.equals( "" ) ) {
						System.out.println( "Worker " + workerNum + " finished." );
						break;
					} 
					lineNum = hmp.size();
					hmp.add( new String[ numTaxa + 6 ] );
				}
				
				String chr = currentLine.split( "\t" )[ 0 ];
				String position = currentLine.split( "\t" )[ 1 ];
				String rs = currentLine.split( "\t" )[ 2 ];
				String ref_allele = currentLine.split( "\t" )[ 3 ];
				String alt_allele = currentLine.split( "\t" )[ 4 ];
				
				int currentDepth;
				String[] temp = new String[ numTaxa + 2 ];
				int length = currentLine.split( "\t" ).length;
				for ( int i = 9; i < length; i++ ) {
					String depthString = currentLine.split( "\t" )[ i ].split( ":" )[ 2 ];
					if ( !depthString.equals( "." ) ) {
						currentDepth = Integer.parseInt( currentLine.split( "\t" )[ i ].split( ":" )[ 2 ] );
						String current = currentLine.split( "\t" )[ i ].split( ":" )[ 0 ];
						if ( current.equals( "0/0" ) ) {
							temp[ i - 9 ] = ( currentDepth > depth ) ? ref_allele : ref_allele.toLowerCase();
						} else if ( current.equals( "1/1" ) ) {
							temp[ i - 9 ] = ( currentDepth > depth ) ? alt_allele : alt_allele.toLowerCase();
						} else if ( current.equals( "1/0" ) || current.equals( "0/1" ) ) {
							temp[ i - 9 ] = ( currentDepth > depth ) ? getHet( ref_allele, alt_allele ) : getHet( ref_allele, alt_allele ).toLowerCase();
						} else {
							temp[ i - 9 ] = "N";
						}
					} else {
						temp[ i - 9 ] = "N";
					}
				}
				synchronized( lock ) {
					hmp.get( lineNum )[ 0 ] = rs;
					hmp.get( lineNum )[ 1 ] = ref_allele + "/" + alt_allele;
					hmp.get( lineNum )[ 2 ] = chr;
					hmp.get( lineNum )[ 3 ] = position;
					int length1 = temp.length;
					for ( int i = 0; i < length1; i++ ) {
						hmp.get( lineNum )[ i + 4 ] = temp[ i ];
					}
				}
			}
		}
	}

	public static void usage() {
		System.err.println();
		System.err.println( "Version: " + versionNumber );
		System.err.println( "Usage: java VCFToHapmapForMapping <args>" );
		System.err.println( "Arguments:" );
		System.err.println( "\t-i <VCF file> - Input VCF File" );
		System.err.println( "\t-o <.hmp.txt file> - Output Hapmap File" );
		System.err.println( "\t-t <numeric | base | sm> - numeric transformation/soft mask with parents/soft mask no parents" );
		System.err.println( "\t-d <depth> - Optional. Only used when using \"base\" or \"sm.\" No arg default = 5 )" );
		System.err.println( "\t-p1 <parent 1> - Optional. Name of parent 1. Used with \"numeric\" or \"base.\"" );
		System.err.println( "\t-p2 <parent 2> - Optional. Name of parent 2. Used with \"numeric\" or \"base.\"" );
		System.err.println( "\t\t-p1 and -p2 must be used together or not at all." );
		System.err.println( "\t\tParents will be moved to the beginning of the Taxa in the file." );
		System.err.println( "\t\tDefault is to assume parents 1 and 2 are in columns 10 and 11." );
		System.err.println( "\t-r <parent1Replacement,parent2Replacement,hetReplacement,missingReplacement> - ");
		System.err.println( "\t\tOptional. Specify the desired replacement strings for each genotype call.");
		System.err.println( "\t\tNot used when running with \"sm\" type." );
		System.err.println();
		System.exit( 1 );
	}
	
	public void run() throws InterruptedException, IOException {
		long startTime = System.currentTimeMillis();
		String outFile = null;
		String type = null;
		boolean in = false, out = false, tp = false, par1 = false, par2 = false;
		for ( int i = 0; i < args.length; i++ ) {
			if ( args[ i ].equals( "-i" ) && i < args.length - 1 ) {
				input = new Scanner( new File( args[ i + 1 ] ) );
				in = true;
			} else if ( args[ i ].toLowerCase().equals( "-o" ) && i < args.length - 1 ) {
				outFile = args[ i + 1 ];
				out = true;
			} else if ( args[ i ].toLowerCase().equals( "-t" ) && i < args.length - 1 ) {
				type = args[ i + 1 ];
				tp = true;
			} else if ( args[ i ].toLowerCase().equals( "-d" ) && i < args.length - 1 ) {
				depth = Integer.parseInt( args[ i + 1 ] ); 
			} else if ( args[ i ].toLowerCase().equals( "-p1" ) && i < args.length - 1 ) {
				parent1 = args[ i + 1 ];
				par1 = true;
			} else if ( args[ i ].toLowerCase().equals( "-p2" ) && i < args.length - 1 ) {
				parent2 = args[ i + 1 ];
				par2 = true;
			} else if ( args[ i ].toLowerCase().equals( "-r" ) && i < args.length - 1 ) {
				if ( !tp ) {
					System.err.println( "-chars flag must come after the -t flag." );
					usage();
				} else if ( args[ i + 1 ].split( "," ).length != 4 ) {
					System.err.println( "4 replacement strings required.");
				} else {
					if ( type.toLowerCase().equals( "numeric" ) ) {
						p1Numeric = args[ i + 1 ].split( "," )[ 0 ];
						p2Numeric = args[ i + 1 ].split( "," )[ 1 ];
						hetNumeric = args[ i + 1 ].split( "," )[ 2 ];
						missingNumeric = args[ i + 1 ].split( "," )[ 3 ];
					} else {
						p1Base = args[ i + 1 ].split( "," )[ 0 ];
						p2Base = args[ i + 1 ].split( "," )[ 1 ];
						hetBase = args[ i + 1 ].split( "," )[ 2 ];
						missingBase = args[ i + 1 ].split( "," )[ 3 ];
					}
				}
			}
		}
		if ( ( !in || !out || ! tp ) || ( !par1 ^ !par2 ) ) {
			usage();
		}

		int cores = Runtime.getRuntime().availableProcessors();
		System.out.println();
		System.out.println( "This programs assumes that there are no indels in the input file." );
		System.out.println();
		System.out.println( cores + " CPU cores found." );
		System.out.println( "Running with " + cores + " threads." );

		String currentLine;
		do {
			currentLine = input.nextLine();
		} while ( currentLine.startsWith( "##" ) );
		ArrayList<String> line = new ArrayList<String>( 1000 );
		for ( int i = 0; i < currentLine.split( "\t" ).length; i++ ) {
			line.add( currentLine.split( "\t" )[ i ] );
			if ( par1 && parent1.equals( currentLine.split( "\t" )[ i ] ) ) {
				p1Col = i;
			} else if ( par2 && parent2.equals( currentLine.split( "\t" )[ i ] ) ) {
				p2Col = i;
			}
		}
		hmp = new ArrayList<String[]>();
		hmp.add( new String[ line.size() - 3 ] );
		hmp.get( 0 )[ 0 ] = "rs";
		hmp.get( 0 )[ 1 ] = "allele";
		hmp.get( 0 )[ 2 ] = "chrom";
		hmp.get( 0 )[ 3 ] = "pos";
		hmp.get( 0 )[ 4 ] = currentLine.split( "\t" )[ p1Col ];
		hmp.get( 0 )[ 5 ] = currentLine.split( "\t" )[ p2Col ];
		numTaxa = line.size() - 11;
		int col = 6;
		for ( int i = 9; i < line.size(); i++ ) {
			if ( i != p1Col && i != p2Col ) {
				hmp.get( 0 )[ col ] = line.get( i );
				col++;
			}
		}

		if ( type.toLowerCase().equals( "base" ) ) {
			BaseWorker[] threads = new BaseWorker[ cores ];
			for ( int i = 0; i < cores; i++ ) {
				threads[ i ] = new BaseWorker( i, args[ 0 ] );
				threads[ i ].start();
			}
			for ( int i = 0; i < cores; i++ ) {
				threads[ i ].join();
			}
		} else if ( type.toLowerCase().equals( "numeric" ) ) {
			NumericWorker[] threads = new NumericWorker[ cores ];
			for ( int i = 0; i < cores; i++ ) {
				threads[ i ] = new NumericWorker( i, args[ 0 ] );
				threads[ i ].start();
			}
			for ( int i = 0; i < cores; i++ ) {
				threads[ i ].join();
			}
		} else if ( type.toLowerCase().equals( "sm" ) ) {
			SoftMaskWorker[] threads = new SoftMaskWorker[ cores ];
			for ( int i = 0; i < cores; i++ ) {
				threads[ i ] = new SoftMaskWorker( i, args[ 0 ] );
				threads[ i ].start();
			}
			for ( int i = 0; i < cores; i++ ) {
				threads[ i ].join();
			}
		} else {
			usage();
		}
		BufferedWriter output = new BufferedWriter( new FileWriter( outFile ) );
		int height = hmp.size();
		int width = hmp.get( 0 ).length - 2;
		for ( int i = 0; i < height; i++ ) {
			for ( int j = 0; j < width; j++ ) {
				output.write( hmp.get( i )[ j ] );
				if ( j != width - 1 ) {
					output.write( "\t" );
				}
			}
			output.write( "\n" );
		}
		output.close();
		input.close();
		long endTime   = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		System.out.println( "Finished in " + totalTime + " milliseconds." );
	}
	
	public VCFToHapmapForMapping( String[] args ) {
		this.args = args;
	}

	public static void main(String[] args) throws InterruptedException, IOException {
		VCFToHapmapForMapping vcftohmp = new VCFToHapmapForMapping( args );
		vcftohmp.run();
	}
}
