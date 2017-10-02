package cli;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.JFileChooser;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class SheetGenerator {

	private static final String rates = "H:\\StaffRates.csv";

	private static ArrayList<String[]> readRates() throws FileNotFoundException {
		ArrayList<String[]> ret = new ArrayList<String[]>();
		Scanner in = new Scanner( new File( rates ) );
		do {
			String[] line = in.nextLine().split( "," );
			if ( line.length == 1 ) {
				break;
			}
			ret.add( line );
		} while ( in.hasNextLine() );
		in.close();
		return ret;
	}

	private static void generateSheet( String projectFileName ) {
		try {
			ArrayList<String[]> staffRates = readRates();
			String filename = projectFileName.replaceAll( "csv", "xlsx" );
			File file = new File( filename );
			if ( file.exists() ) {
				file.delete();
			}
			XSSFWorkbook workbook = new XSSFWorkbook();
			XSSFSheet sheet = workbook.createSheet( "Labor Estimate" );

			// Style stuff
			XSSFFont defaultFont = workbook.createFont();
			defaultFont.setFontHeightInPoints( ( short ) 10 );
			defaultFont.setFontName( "Lato" );
			defaultFont.setColor( IndexedColors.BLACK.getIndex() );
			defaultFont.setBold(false);
			defaultFont.setItalic(false);

			XSSFFont boldFont = workbook.createFont();
			boldFont.setFontHeightInPoints( ( short ) 10 );
			boldFont.setFontName( "Calibri" );
			boldFont.setColor( IndexedColors.BLACK.getIndex() );
			boldFont.setBold(true);
			boldFont.setItalic(false);

			XSSFFont italicFont = workbook.createFont();
			italicFont.setFontHeightInPoints( ( short ) 10 );
			italicFont.setFontName( "Calibri" );
			italicFont.setColor( IndexedColors.BLACK.getIndex() );
			italicFont.setBold(false);
			italicFont.setItalic(true);

			XSSFFont boldItalicFont = workbook.createFont();
			boldItalicFont.setFontHeightInPoints( ( short ) 10 );
			boldItalicFont.setFontName( "Calibri" );
			boldItalicFont.setColor( IndexedColors.BLACK.getIndex() );
			boldItalicFont.setBold(true);
			boldItalicFont.setItalic(true);

			XSSFFont headerFont = workbook.createFont();
			headerFont.setFontHeightInPoints( ( short ) 14 );
			headerFont.setFontName( "Ubuntu" );
			headerFont.setColor( IndexedColors.BLACK.getIndex() );
			headerFont.setBold(true);
			headerFont.setItalic(false);

			CellStyle greenStyle = workbook.createCellStyle();
			greenStyle.setFont( headerFont );
			/* byte[] rgb = new byte[3];
		    rgb[0] = (byte) 122; // red
		    rgb[1] = (byte) 171; // green
		    rgb[2] = (byte) 101; // blue
		    greenStyle.setFillForegroundColor( new XSSFColor( rgb, new DefaultIndexedColorMap() ).getIndex() );
		    greenStyle.setFillPattern( FillPatternType.SOLID_FOREGROUND );*/
			greenStyle.setAlignment( HorizontalAlignment.CENTER );
			greenStyle.setBorderLeft( BorderStyle.MEDIUM );
			greenStyle.setBorderRight( BorderStyle.MEDIUM );
			greenStyle.setBorderTop( BorderStyle.MEDIUM );
			greenStyle.setBorderBottom( BorderStyle.MEDIUM );

			CellStyle greyStyle = workbook.createCellStyle();
			greyStyle.setFont( defaultFont );
			greyStyle.setFillForegroundColor( IndexedColors.GREY_25_PERCENT.getIndex() );
			greyStyle.setFillPattern( FillPatternType.SOLID_FOREGROUND );

			CellStyle taskStyle = workbook.createCellStyle();
			taskStyle.setFont( defaultFont );

			CellStyle rateStyle = workbook.createCellStyle();
			rateStyle.setFont( defaultFont );
			rateStyle.setAlignment( HorizontalAlignment.CENTER );

			CellStyle rateHeaderStyle = workbook.createCellStyle();
			rateHeaderStyle.cloneStyleFrom( rateStyle );
			rateHeaderStyle.setAlignment( HorizontalAlignment.RIGHT );

			// Create sheet
			XSSFRow header = sheet.createRow( (short) 0 );
			sheet.addMergedRegion( new CellRangeAddress( 0, 0, 1, 27 ) );
			sheet.addMergedRegion( new CellRangeAddress( 0, 0, 28, 50 ) );
			sheet.addMergedRegion( new CellRangeAddress( 0, 0, 51, 56 ) );
			sheet.addMergedRegion( new CellRangeAddress( 0, 0, 57, 77 ) );
			for ( int i = 1; i <= 77; i++ ) {
				header.createCell( i );
				header.getCell( i ).setCellStyle( greenStyle );
			}
			header.getCell( 1 ).setCellValue( "Engineering / Planning" );
			header.getCell( 28 ).setCellValue( "Geomatics" );
			header.getCell( 51 ).setCellValue( "Administrative" );
			header.getCell( 57 ).setCellValue( "Environmental / Geology" );

			XSSFRow staffRow = sheet.createRow( (short) 1 );
			XSSFRow ratesRow = sheet.createRow( (short) 2 );
			staffRow.createCell( 0 );
			ratesRow.createCell( 0 );
			staffRow.setHeightInPoints( (float) 50 );
			staffRow.getCell( 0 ).setCellValue( "Staff" );
			staffRow.getCell( 0 ).setCellStyle( greenStyle );
			ratesRow.getCell( 0 ).setCellValue( "Rate" );
			ratesRow.getCell( 0 ).setCellStyle( rateHeaderStyle );

			for ( int i = 1; i <= staffRates.size(); i++ ) {
				staffRow.createCell( i );
				staffRow.getCell( i ).setCellValue( staffRates.get( i - 1 )[ 0 ] );
				ratesRow.createCell( i );
				ratesRow.getCell( i ).setCellValue( Integer.parseInt( staffRates.get( i - 1 )[ 1 ] ) );
			}

			// Write excel workbook
			FileOutputStream fileOut = new FileOutputStream( filename );
			workbook.write( fileOut );
			fileOut.close();
			workbook.close();
			System.exit( 0 );
		} catch (IOException e) {
			System.out.println( e );
			System.exit( 1 );
		}
	}

	private static ArrayList<String[]> readCSV( String filename ) {
		try {
			Scanner in = new Scanner( new File( filename ) );
			ArrayList<String[]> input = new ArrayList<String[]>();
			do {
				String line[] = in.nextLine().split( "," );
				if ( line.length <= 1 ) {
					break;
				}
				if ( !line[ 0 ].equals( "Task" ) ) {
					input.add( line );
				}
			} while ( in.hasNextLine() );
			in.close();
			return input;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void main( String[] args ) {
		// Allow user to select input csv file
		JFileChooser fc = new JFileChooser();
		fc.setCurrentDirectory( new File( "H:\\" ) );
		int returnVal = fc.showOpenDialog( null );
		if ( returnVal != JFileChooser.APPROVE_OPTION ) {
			System.exit( 0 );
		}
		String filename = fc.getSelectedFile().getAbsolutePath();

		// Read Selected csv file
		readCSV( filename );

		generateSheet( filename );
	}

}
