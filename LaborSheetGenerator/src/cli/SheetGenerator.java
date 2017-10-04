package cli;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.JFileChooser;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class SheetGenerator {

	private static final String rates = "H:\\StaffRates.csv";
	private static final String[] columns = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "AA", "AB", "AC", "AD", "AE", "AF", "AG", "AH", "AI", "AJ", "AK", "AL", "AM", "AN", "AO", "AP", "AQ", "AR", "AS", "AT", "AU", "AV", "AW", "AX", "AY", "AZ", "BA", "BB", "BC", "BD", "BE", "BF", "BG", "BH", "BI", "BJ", "BK", "BL", "BM", "BN", "BO", "BP", "BQ", "BR", "BS", "BT", "BU", "BV", "BW", "BX", "BY", "BZ", "CA", "CB", "CC"};


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
	
	private static void generateSheet( String projectFileName, ArrayList<String[]> tasks ) {
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
			boldFont.setFontName( "Lato" );
			boldFont.setColor( IndexedColors.BLACK.getIndex() );
			boldFont.setBold(true);
			boldFont.setItalic(false);

			XSSFFont italicFont = workbook.createFont();
			italicFont.setFontHeightInPoints( ( short ) 10 );
			italicFont.setFontName( "Lato" );
			italicFont.setColor( IndexedColors.BLACK.getIndex() );
			italicFont.setBold(false);
			italicFont.setItalic(true);

			XSSFFont boldItalicFont = workbook.createFont();
			boldItalicFont.setFontHeightInPoints( ( short ) 10 );
			boldItalicFont.setFontName( "Lato" );
			boldItalicFont.setColor( IndexedColors.BLACK.getIndex() );
			boldItalicFont.setBold(true);
			boldItalicFont.setItalic(true);

			XSSFFont headerFont = workbook.createFont();
			headerFont.setFontHeightInPoints( ( short ) 14 );
			headerFont.setFontName( "Ubuntu" );
			headerFont.setColor( IndexedColors.BLACK.getIndex() );
			headerFont.setBold(true);
			headerFont.setItalic(false);

			XSSFCellStyle greenStyle = workbook.createCellStyle();
			greenStyle.setFont( headerFont );
		    greenStyle.setFillForegroundColor( new XSSFColor( new Color( 122, 171, 101 ) ) );
		    greenStyle.setFillPattern( FillPatternType.SOLID_FOREGROUND );
			greenStyle.setAlignment( HorizontalAlignment.CENTER );
			greenStyle.setBorderLeft( BorderStyle.MEDIUM );
			greenStyle.setBorderRight( BorderStyle.MEDIUM );
			greenStyle.setBorderTop( BorderStyle.MEDIUM );
			greenStyle.setBorderBottom( BorderStyle.MEDIUM );
			greenStyle.setVerticalAlignment( VerticalAlignment.CENTER );

			XSSFCellStyle whiteTaskStyle = workbook.createCellStyle();
			whiteTaskStyle.setFont( defaultFont );
			whiteTaskStyle.setAlignment( HorizontalAlignment.CENTER );
			whiteTaskStyle.setBorderLeft( BorderStyle.THIN );
			whiteTaskStyle.setBorderRight( BorderStyle.THIN );
			whiteTaskStyle.setBorderTop( BorderStyle.THIN );
			whiteTaskStyle.setBorderBottom( BorderStyle.THIN );
			
			XSSFCellStyle greyTaskStyle = workbook.createCellStyle();
			greyTaskStyle.cloneStyleFrom( whiteTaskStyle );
			greyTaskStyle.setFillForegroundColor( new XSSFColor( new Color( 237, 237, 237 ) ) );
			greyTaskStyle.setFillPattern( FillPatternType.SOLID_FOREGROUND );
			
			XSSFCellStyle whiteFeeStyle = workbook.createCellStyle();
			whiteFeeStyle.cloneStyleFrom( whiteTaskStyle );
			whiteFeeStyle.setDataFormat( (short) 8 );
			
			XSSFCellStyle greyFeeStyle = workbook.createCellStyle();
			greyFeeStyle.cloneStyleFrom( greyTaskStyle );
			greyFeeStyle.setDataFormat( (short) 8 );
			
			XSSFCellStyle whiteTaskHeaderStyle = workbook.createCellStyle();
			whiteTaskHeaderStyle.setFont( boldItalicFont );
			whiteTaskHeaderStyle.setBorderLeft( BorderStyle.THIN );
			whiteTaskHeaderStyle.setBorderRight( BorderStyle.MEDIUM );
			whiteTaskHeaderStyle.setBorderTop( BorderStyle.THIN );
			whiteTaskHeaderStyle.setBorderBottom( BorderStyle.THIN );
			whiteTaskHeaderStyle.setAlignment( HorizontalAlignment.LEFT );
			
			XSSFCellStyle greyTaskHeaderStyle = workbook.createCellStyle();
			greyTaskHeaderStyle.cloneStyleFrom( whiteTaskHeaderStyle );
			greyTaskHeaderStyle.setFillForegroundColor( new XSSFColor( new Color( 237, 237, 237 ) ) );
			greyTaskHeaderStyle.setFillPattern( FillPatternType.SOLID_FOREGROUND );
			
			XSSFCellStyle whiteWorkItemStyle = workbook.createCellStyle();
			whiteWorkItemStyle.setFont( defaultFont );
			whiteWorkItemStyle.setBorderLeft( BorderStyle.THIN );
			whiteWorkItemStyle.setBorderRight( BorderStyle.MEDIUM );
			whiteWorkItemStyle.setBorderTop( BorderStyle.THIN );
			whiteWorkItemStyle.setBorderBottom( BorderStyle.THIN );
			whiteWorkItemStyle.setAlignment( HorizontalAlignment.RIGHT );
			
			XSSFCellStyle greyWorkItemStyle = workbook.createCellStyle();
			greyWorkItemStyle.cloneStyleFrom( whiteWorkItemStyle );
			greyWorkItemStyle.setFillForegroundColor( new XSSFColor( new Color( 237, 237, 237 ) ) );
			greyWorkItemStyle.setFillPattern( FillPatternType.SOLID_FOREGROUND );
			
			XSSFCellStyle whiteFeeHoursStyle = workbook.createCellStyle();
			whiteFeeHoursStyle.cloneStyleFrom( whiteWorkItemStyle );
			whiteFeeHoursStyle.setFont( italicFont );
			
			XSSFCellStyle greyFeeHoursStyle = workbook.createCellStyle();
			greyFeeHoursStyle.cloneStyleFrom( greyWorkItemStyle );
			greyFeeHoursStyle.setFont( italicFont );

			XSSFCellStyle rateStyle = workbook.createCellStyle();
			rateStyle.setFont( defaultFont );
			rateStyle.setAlignment( HorizontalAlignment.CENTER );
			rateStyle.setDataFormat( (short) 8 );
			rateStyle.setFillForegroundColor( new XSSFColor( new Color( 237, 237, 237 ) ) );
			rateStyle.setFillPattern( FillPatternType.SOLID_FOREGROUND );
			rateStyle.setBorderLeft( BorderStyle.THIN );
			rateStyle.setBorderRight( BorderStyle.THIN );
			rateStyle.setBorderBottom( BorderStyle.THIN );

			XSSFCellStyle rateHeaderStyle = workbook.createCellStyle();
			rateHeaderStyle.cloneStyleFrom( rateStyle );
			rateHeaderStyle.setAlignment( HorizontalAlignment.RIGHT );
			rateHeaderStyle.setBorderRight( BorderStyle.MEDIUM );
			
			XSSFCellStyle staffStyle = workbook.createCellStyle();
			staffStyle.setFont( boldFont );
			staffStyle.setAlignment( HorizontalAlignment.CENTER );
			staffStyle.setVerticalAlignment( VerticalAlignment.CENTER );
			staffStyle.setWrapText(true);
			staffStyle.setBorderLeft( BorderStyle.THIN );
			staffStyle.setBorderRight( BorderStyle.THIN );
			staffStyle.setBorderBottom( BorderStyle.THIN );
			
			XSSFCellStyle whiteTotalStyle = workbook.createCellStyle();
			whiteTotalStyle.cloneStyleFrom( whiteWorkItemStyle );
			whiteTotalStyle.setFont( boldFont );
			
			XSSFCellStyle greyTotalStyle = workbook.createCellStyle();
			greyTotalStyle.cloneStyleFrom( greyWorkItemStyle );
			greyTotalStyle.setFont( boldFont );
			
			XSSFCellStyle totalFeesHeaderStyle = workbook.createCellStyle();
			totalFeesHeaderStyle.setFont( boldFont );
			totalFeesHeaderStyle.setAlignment( HorizontalAlignment.CENTER );
			totalFeesHeaderStyle.setVerticalAlignment( VerticalAlignment.CENTER );
			totalFeesHeaderStyle.setBorderLeft( BorderStyle.MEDIUM );
			totalFeesHeaderStyle.setBorderRight( BorderStyle.MEDIUM );
			totalFeesHeaderStyle.setBorderTop( BorderStyle.MEDIUM );
			totalFeesHeaderStyle.setBorderBottom( BorderStyle.MEDIUM );
			totalFeesHeaderStyle.setWrapText( true );
			
			XSSFCellStyle totalHoursStyle = workbook.createCellStyle();
			totalHoursStyle.cloneStyleFrom( totalFeesHeaderStyle );
			totalHoursStyle.setVerticalAlignment( VerticalAlignment.BOTTOM );
			totalHoursStyle.setWrapText( false );
			
			XSSFCellStyle totalFeesStyle = workbook.createCellStyle();
			totalFeesStyle.cloneStyleFrom( totalFeesHeaderStyle );
			totalFeesStyle.setDataFormat( (short) 8 );
			totalFeesStyle.setVerticalAlignment( VerticalAlignment.BOTTOM );
			totalFeesStyle.setWrapText( false );
			
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
			staffRow.setHeightInPoints( (float) 100 );
			staffRow.getCell( 0 ).setCellValue( "Staff" );
			sheet.setColumnWidth( staffRow.getCell( 0 ).getColumnIndex(), 256 * 50 );
			staffRow.getCell( 0 ).setCellStyle( greenStyle );
			ratesRow.getCell( 0 ).setCellValue( "Rate" );
			ratesRow.getCell( 0 ).setCellStyle( rateHeaderStyle );

			for ( int i = 1; i <= staffRates.size(); i++ ) {
				staffRow.createCell( i );
				staffRow.getCell( i ).setCellValue( staffRates.get( i - 1 )[ 0 ] );
				staffRow.getCell( i ).setCellStyle( staffStyle );
				ratesRow.createCell( i );
				ratesRow.getCell( i ).setCellValue( Integer.parseInt( staffRates.get( i - 1 )[ 1 ] ) );
				ratesRow.getCell( i ).setCellStyle( rateStyle );
			}
			staffRow.createCell( 78 );
			staffRow.getCell( 78 ).setCellStyle( totalFeesHeaderStyle );
			ratesRow.createCell( 78 );
			ratesRow.getCell( 78 ).setCellStyle( totalFeesHeaderStyle );
			sheet.addMergedRegion( new CellRangeAddress( 1, 2, 78, 78 ) );
			staffRow.getCell( 78 ).setCellValue( "Task Subtotals" );
			
			
			int row = 3;
			
			for ( int i = 0; i < tasks.size(); i++ ) {
				XSSFRow task = sheet.createRow( row );
				task.createCell( 0 );
				task.getCell( 0 ).setCellValue( tasks.get( i )[ 1 ].equals( "" ) ? tasks.get( i )[ 0 ] : tasks.get( i )[ 1 ] );
				if ( task.getRowNum() % 2 != 0 ) { // Odd rows are white
					task.getCell( 0 ).setCellStyle( whiteTaskHeaderStyle );
				} else { // Even rows are grey
					task.getCell( 0 ).setCellStyle( greyTaskHeaderStyle );
				}
				for ( int col = 1; col <= 77; col++ ) {
					task.createCell( col );
					task.getCell( col ).setCellValue( "" );
					if ( task.getRowNum() % 2 == 1 ) { // Odd rows are white
						task.getCell( col ).setCellStyle( whiteTaskStyle );
					} else { // Even rows are grey
						task.getCell( col ).setCellStyle( greyTaskStyle );
					}
				}
				row++;
				
				for ( int j = 2; j < tasks.get( i ).length; j++ ) {
					XSSFRow workItem = sheet.createRow( row );
					workItem.createCell( 0 );
					workItem.getCell( 0 ).setCellValue( tasks.get( i )[ j ] );
					if ( workItem.getRowNum() % 2 == 1 ) { // Odd rows are white
						workItem.getCell( 0 ).setCellStyle( whiteWorkItemStyle );
					} else { // Even rows are grey
						workItem.getCell( 0 ).setCellStyle( greyWorkItemStyle );
					}
					for ( int col = 1; col <= 77; col++ ) {
						workItem.createCell( col );
						if ( workItem.getRowNum() % 2 == 1 ) { // Odd rows are white
							workItem.getCell( col ).setCellStyle( whiteTaskStyle );
						} else { // Even rows are grey
							workItem.getCell( col ).setCellStyle( greyTaskStyle );
						}
					}
					row++;
				}
				XSSFRow hours = sheet.createRow( row );
				row++;
				XSSFRow fee = sheet.createRow( row );
				row++;
				hours.createCell( 0 );
				hours.getCell( 0 ).setCellValue( "Sub Total Hours" );
				fee.createCell( 0 );
				fee.getCell( 0 ).setCellValue( "Sub Total Fee" );
				if ( hours.getRowNum() % 2 == 1 ) {
					hours.getCell( 0 ).setCellStyle( whiteFeeHoursStyle );
				} else {
					hours.getCell( 0 ).setCellStyle( greyFeeHoursStyle );
				}
				if ( fee.getRowNum() % 2 == 1 ) {
					fee.getCell( 0 ).setCellStyle( whiteFeeHoursStyle );
				} else {
					fee.getCell( 0 ).setCellStyle( greyFeeHoursStyle );
				}
				for ( int col = 1; col <= 77; col++ ) {
					hours.createCell( col );
					hours.getCell( col ).setCellFormula( "sum(" + columns[ col ] + ( row - tasks.get( i ).length ) + ":" + columns[ col ] + ( row - 2 ) + ")" );
					if ( hours.getRowNum() % 2 == 1 ) {
						hours.getCell( col ).setCellStyle( whiteTaskStyle );
					} else {
						hours.getCell( col ).setCellStyle( greyTaskStyle );
					}
					fee.createCell( col );
					fee.getCell( col ).setCellFormula( columns[ col ] + "$3*" + columns[ col ] + ( row - 1 ) );
					if ( fee.getRowNum() % 2 == 1 ) {
						fee.getCell( col ).setCellStyle( whiteFeeStyle );
					} else {
						fee.getCell( col ).setCellStyle( greyFeeStyle );
					}
				}
			}
			
			ArrayList<Integer> fees = new ArrayList<Integer>();
			ArrayList<Integer> hours = new ArrayList<Integer>();
			
			for ( int i = 1; i <= sheet.getLastRowNum(); i++ ) {
				String line = sheet.getRow( i ).getCell( 0 ).getStringCellValue();
				if ( line.equals( "Sub Total Hours" ) ) {
					hours.add( i + 1 );
				} else if ( line.equals( "Sub Total Fee" ) ) {
					fees.add( i + 1 );
				}
			}
			
			// Totals at the bottom of the sheet
			XSSFRow hoursRow = sheet.createRow( row );
			row++;
			XSSFRow feesRow = sheet.createRow( row );
			row++;
			hoursRow.createCell( 0 );
			hoursRow.getCell( 0 ).setCellValue( "TOTAL HOURS" );
			feesRow.createCell( 0 );
			feesRow.getCell( 0 ).setCellValue( "TOTAL FEE" );
			
			if ( hoursRow.getRowNum() % 2 == 1 ) {
				hoursRow.getCell( 0 ).setCellStyle( whiteTotalStyle );
				feesRow.getCell( 0 ).setCellStyle( greyTotalStyle );
			} else {
				feesRow.getCell( 0 ).setCellStyle( whiteTotalStyle );
				hoursRow.getCell( 0 ).setCellStyle( greyTotalStyle );
			}
			for ( int i = 1; i <= 77; i++ ) {
				hoursRow.createCell( i );
				feesRow.createCell( i );
				String col = columns[ i ];
				String sumHours = "";
				String sumFees = "";
				for ( int j = 0; j < fees.size(); j++ ) {
					if ( j != 0 ) {
						sumHours += ",";
						sumFees += ",";
					}
					sumHours += col + hours.get( j );
					sumFees += col + fees.get( j );
				}
				hoursRow.getCell( i ).setCellFormula( "sum(" + sumHours + ")" );
				feesRow.getCell( i ).setCellFormula( "sum(" + sumFees + ")" );
				if ( hoursRow.getRowNum() % 2 == 1 ) {
					hoursRow.getCell( i ).setCellStyle( whiteTaskStyle );
					feesRow.getCell( i ).setCellStyle( greyFeeStyle );
				} else {
					feesRow.getCell( i ).setCellStyle( whiteFeeStyle );
					hoursRow.getCell( i ).setCellStyle( greyTaskStyle );
				}
			}
			
			// Total totals at the bottom right corner
			hoursRow.createCell( 78 );
			feesRow.createCell( 78 );
			hoursRow.getCell( 78 ).setCellFormula( "sum(B" + ( hoursRow.getRowNum() + 1 ) + ":BZ" + ( hoursRow.getRowNum() + 1 ) + ")" );
			feesRow.getCell( 78 ).setCellFormula( "sum(B" + ( feesRow.getRowNum() + 1 ) + ":BZ" + ( feesRow.getRowNum() + 1 ) + ")" );
			hoursRow.getCell( 78 ).setCellStyle( totalHoursStyle );
			feesRow.getCell( 78 ).setCellStyle( totalFeesStyle );
			
			// Totals along the side of the sheet
			for ( int i = 3; i <= feesRow.getRowNum() - 2; i++ ) {
				sheet.getRow( i ).createCell( 78 );
				sheet.getRow( i ).getCell( 78 ).setCellStyle( totalFeesStyle );
			}
			for ( int i = 0; i < fees.size(); i++ ) {
				if ( i == 0 ) {
					sheet.addMergedRegion( new CellRangeAddress( 3, fees.get( i ) - 1, 78, 78 ) );
					sheet.getRow( 3 ).getCell( 78 ).setCellFormula( "sum(B" + ( fees.get( i ) ) + ":BZ" + ( fees.get( i ) ) + ")" );
				} else {
					sheet.addMergedRegion( new CellRangeAddress( fees.get( i - 1 ), fees.get( i ) - 1, 78, 78 ) );
					sheet.getRow( fees.get( i - 1 ) ).getCell( 78 ).setCellFormula( "sum(B" + ( fees.get( i ) ) + ":BZ" + ( fees.get( i ) ) + ")" );
				}
			}
			
			// Resize columns
			for ( int i = 1; i <= 78; i++ ) {
				sheet.setColumnWidth( i, 3660 );
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
		String filename;
		if ( !args[ 0 ].equals( "debug" ) ) {
			JFileChooser fc = new JFileChooser();
			fc.setCurrentDirectory( new File( "H:\\" ) );
			int returnVal = fc.showOpenDialog( null );
			if ( returnVal != JFileChooser.APPROVE_OPTION ) {
				System.exit( 0 );
			}

			filename = fc.getSelectedFile().getAbsolutePath();
		} else { 
			filename = "H:\\Labor Spreadsheet.csv";
		}
		generateSheet( filename, readCSV( filename ) );
	}

}
