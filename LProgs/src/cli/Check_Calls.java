package cli;
import java.io.*;
import java.util.*;
public class Check_Calls {
	
	private static final String versionNumber = "1.0";
	
	private static Scanner input;
	private static BufferedWriter output;
	private static int[] table, taxaCounts, taxaCounts200;
	private static String[] taxa;
	private static Object lock = new Object();
	
	private static ArrayList<ArrayList<String>> calls;
	private static ArrayList<ArrayList<String>> snps;
	private static String[] toPrint = new String[ 10 ];
	private static int nextLineToPrint = 0, threadsRunning = 0, currentLine = 0;
	
	private static boolean keepWriting = true;
	
	private static Printer out;
	
	private static String[] args;
	
	public static void usage() {
		System.err.println();
		System.err.println( "Check_Calls version: " + versionNumber );
		System.err.println();
		System.err.println( "Usage:" );
		System.err.println( "\tjava Check_Calls <input.vcf> <output.vcf> [SummaryFile.txt]" );
		System.err.println( "\t\tIf no name for summary file default name will be Summary.txt");
		System.exit( 1 );
	}
	
	private static void tableGen() {
		table = new int[]{0,1,1,1,1,1,1,2,2,2,2,2,2,2,3,3,3,3,3,3,3,4,4,4,4,4,4,4,5,5,5,
				5,5,5,6,6,6,6,6,6,6,7,7,7,7,7,7,7,8,8,8,8,8,8,8,9,9,9,9,9,9,10,10,10,10,
				10,10,10,11,11,11,11,11,11,11,12,12,12,12,12,12,13,13,13,13,13,13,13,14,
				14,14,14,14,14,14,15,15,15,15,15,15,15,16,16,16,16,16,16,17,17,17,17,17,
				17,17,18,18,18,18,18,18,18,19,19,19,19,19,19,19,20,20,20,21,21,21,21,22,
				22,22,23,23,23,24,24,24,25,25,25,26,26,26,27,27,27,28,28,28,29,29,29,30,
				30,30,30,31,31,31,32,32,32,33,33,33,34,34,35,35,35,36,36,36,37,37,37,38,
				38,38,39,39,39,40,40,40,41,41,41,42,42,42,43};
	}
	
	public static class Printer extends Thread {
		
		public Printer() {}
		
		public synchronized void write( boolean notify, boolean writer, String[] line, int lineNum ) {
			if ( notify ) {
				notifyAll();
			} else if ( writer ) {
				while ( toPrint[ 0 ] == null ) {
					if ( !keepWriting ) {
						return;
					}
					try {
						wait();
					} catch (InterruptedException e) {
						System.err.println( "Wait inturrupted at writer." );
						System.exit( 1 );
					}
				}
				try {
					output.write( toPrint[ 0 ] + "\n" );
				} catch (IOException e) {
					System.err.println( "Unable to write to file." );
					System.exit( 1 );
				}
				for ( int i = 1; i < toPrint.length; i++ ) {
					toPrint[ i - 1 ] = toPrint[ i ];
				}
				toPrint[ toPrint.length - 1 ] = null;
				nextLineToPrint++;
				notifyAll();
			} else {
				while ( lineNum - nextLineToPrint >= 10 ) {
					try {
						wait();
					} catch (InterruptedException e) {
						System.err.println( "Wait inturrupted adding to write queue." );
						System.exit( 1 );
					}
				}
				toPrint[ lineNum - nextLineToPrint ] = line[ 0 ];
				if ( line.length != 1 ) {
					toPrint[ lineNum - nextLineToPrint ] += "\t";
				}
				for ( int i = 1; i < line.length; i++ ) {
					toPrint[ lineNum - nextLineToPrint ] += line[ i ];
					if ( i < line.length - 1 ) {
						toPrint[ lineNum - nextLineToPrint ] += "\t";
					}
				}
				notifyAll();
			}
		}
		
		public void run() {
			while ( keepWriting ) {
				write( false, true, null, 0 );
			}
		}
	}

	public static class Worker extends Thread {

		public int workerNum, workingLine;

		public Worker( int number ) {
			workerNum = number;
		}

		public void run() {
			while ( true ) {
				String[] line;
				synchronized( lock ) {
					if ( !input.hasNextLine() ) {
						threadsRunning--;
						if ( threadsRunning == 0 ) {
							keepWriting = false;
							out.write( true, false, null, 0 );
						}
						break;
					}
					line = input.nextLine().split( "\t" );
					workingLine = currentLine;
					currentLine++;
				}
				if ( line[ 0 ].startsWith( "##" ) ) {
					// Do nothing to these lines.
				} else if ( line[ 0 ].startsWith( "#CHROM" ) ) {
					// Do nothing with this line but get info from it.
					calls = new ArrayList<ArrayList<String>>();
					snps = new ArrayList<ArrayList<String>>();
					for ( int i = 9; i < line.length; i++ ) {
						calls.add( new ArrayList<String>() );
						snps.add( new ArrayList<String>() );
					}
					taxaCounts = new int[ line.length - 9 ];
					taxa = new String[ line.length - 9 ];
					taxaCounts200 = new int[ line.length - 9 ];
					for ( int i = 9; i < line.length; i++ ) {
						taxa[ i - 9 ] = line[ i ];
					}
				} else {
					// Read and modify these lines if needed.
					while ( taxaCounts200 == null ) {
					}
					for ( int col = 9; col < line.length; col++ ) {
						if ( line[ col ].split( ":" )[ 1 ].split( "," ).length > 1 ) {
							if ( Integer.parseInt( line[ col ].split( ":" )[ 2 ] ) < 200 ) {
								if ( ( line[ col ].split( ":" )[ 0 ].equals( "1/1" ) || line[ col ].split( ":" )[ 0 ]
										.equals( "0/0" ) ) && ( line[ col ].split( ":" )[ 1 ].split( "," ).length == 2 
										&& line[ col ].split( ":" )[ 4 ].equals( "255,0,255" ) ) ) {
									int ref = Integer.parseInt( line[ col ].split( ":" )[ 1 ].split( "," )[ 0 ] );
									int alt = Integer.parseInt( line[ col ].split( ":" )[ 1 ].split( "," )[ 1 ] );
									if ( ref != -1 && alt != -1 ) {
										if ( ref > alt ) {
											if ( alt >= table[ ref + alt ] ) {
												synchronized( lock ) {
													taxaCounts[ col - 9 ]++;
													calls.get( col - 9 ).add( line[ col ] );
													snps.get( col - 9 ).add( line[ 2 ] );
												}
											}
										} else {
											if ( ref >= table[ ref + alt ] ) {
												synchronized( lock ) {
													taxaCounts[ col - 9 ]++;
													calls.get( col - 9 ).add( line[ col ] );
													snps.get( col - 9 ).add( line[ 2 ] );
												}
											}
										}
									}
								}
							} else {
								if ( ( line[ col ].split( ":" )[ 0 ].equals( "1/1" ) || line[ col ].split( ":" )[ 0 ]
										.equals( "0/0" ) ) && line[ col ].split( ":" )[ 1 ].split( "," ).length == 2 
										&& ( line[ col ].split( ":" )[ 1 ].split( "," ).length == 2 && line[ col ]
										.split( ":" )[ 4 ].equals( "255,0,255" ) ) ) {
									int ref = Integer.parseInt( line[ col ].split( ":" )[ 1 ].split( "," )[ 0 ] );
									int alt = Integer.parseInt( line[ col ].split( ":" )[ 1 ].split( "," )[ 1 ] );
									if ( ref > alt && alt >= ( ref + alt ) * .1 ) {
										synchronized( lock ) {
											taxaCounts200[ col - 9 ]++;
											calls.get( col - 9 ).add( line[ col ] );
											snps.get( col - 9 ).add( line[ 2 ] );
											String sub = line[ col ].substring( 3 );
											String replace;
											if ( line[ col ].split( ":" )[ 0 ].equals( "1/1" ) ) {
												replace = "1/0" + sub;
											} else {
												replace = "0/1" + sub;
											}
											line[ col ] = replace;
										}
									} else if ( ref >= ( ref + alt ) * .1 ) {
										synchronized( lock ) {
											taxaCounts200[ col - 9 ]++;
											calls.get( col - 9 ).add( line[ col ] );
											snps.get( col - 9 ).add( line[ 2 ] );
											String sub = line[ col ].substring( 3 );
											String replace;
											if ( line[ col ].split( ":" )[ 0 ].equals( "1/1" ) ) {
												replace = "1/0" + sub;
											} else {
												replace = "0/1" + sub;
											}
											line[ col ] = replace;
										}
									}
								}
							}
						}
					}
				}
				out.write( false, false, line, workingLine );
			}
		}
	}
	
	public void run() throws InterruptedException, IOException {
		input = new Scanner( new File( args[ 0 ] ) );

		tableGen();
		
		output = new BufferedWriter( new FileWriter( args[ 1 ] ) );
		
		out = new Printer();
		out.start();

		// Multiprocessing
		int cores = Runtime.getRuntime().availableProcessors();
		System.err.println( cores + " CPU cores found." );
		System.err.println( "Running with " + cores + " threads." );
		Worker[] workers = new Worker[ cores ];
		for ( int i = 0; i < cores; i++ ) {
			workers[ i ] = new Worker( i );
			workers[ i ].start();
			threadsRunning++;
		}
		for ( int i = 0; i < cores; i++ ) {
			workers[ i ].join();
		}
		out.join();
		
		BufferedWriter summaryWriter;
		if ( args.length == 4 ) {
			summaryWriter = new BufferedWriter( new FileWriter( args[ 2 ] ) );
		} else {
			summaryWriter = new BufferedWriter( new FileWriter( "Summary.txt" ) );
		}
		for ( int i = 0; i < taxa.length; i++ ) {
			if ( snps.get( i ).size() > 0 ) {
				summaryWriter.write( taxa[ i ] + "\t" + taxa[ i ] );
				if ( i < taxa.length - 1 ) {
					summaryWriter.write( "\t" );
				}
			}
		}
		summaryWriter.write( "\n" );
		for ( int i = 0; i < taxa.length; i++ ) {
			if ( snps.get( i ).size() > 0 ) {
				summaryWriter.write( "SNP\tCall");
				if ( i < taxa.length - 1 ) {
					summaryWriter.write( "\t" );
				}
			}
		}
		summaryWriter.write( "\n" );
		int max = 0;
		for ( int i = 0; i < taxaCounts.length; i++ ) {
			if ( taxaCounts[ i ] + taxaCounts200[ i ] > max ) {
				max = taxaCounts[ i ] + taxaCounts200[ i ];
			}
		}
		for ( int row = 0; row < max; row++ ) {
			for ( int i = 0; i < taxa.length; i++ ) {
				if ( snps.get( i ).size() > 0 ) {
					if ( snps.get( i ).size() > row ) {
						summaryWriter.write( snps.get( i ).get( row ) + "\t" + calls.get( i ).get( row ) );
					} else {
						summaryWriter.write( "\t" );
					}
					if ( i < taxa.length - 1 ) {
						summaryWriter.write( "\t" );
					}
				}
			}
			summaryWriter.write( "\n" );
		}

		System.out.println( "Taxa\tNum_Incorrect_Calls_with_<200_calls\tNum_Incorrect_Calls_with_>=200_calls" );
		for ( int i = 0; i < taxa.length; i++ ) {
			System.out.println( taxa[ i ] + "\t" + taxaCounts[ i ] + "\t" + taxaCounts200[ i ] );
		}
		input.close();
		output.close();
		summaryWriter.close();
	}
	
	public Check_Calls( String[] argsIn ) {
		args = argsIn;
	}

	public static void main( String[] args ) throws InterruptedException, IOException {
		Check_Calls run = new Check_Calls( args );
		run.run();
	}
}
