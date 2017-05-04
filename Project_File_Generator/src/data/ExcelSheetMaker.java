package data;

import java.util.Random;

import java.io.*;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.*;
import org.apache.poi.ss.usermodel.*;

public class ExcelSheetMaker {

	public ExcelSheetMaker( String projectName, Trait[] traits, String[] plateNames ) {
		try {
			String projectFileName = projectName.replaceAll( " ",  "_" );
			String filename = projectFileName + ".xls";
			HSSFWorkbook workbook = new HSSFWorkbook();
			HSSFSheet sheet = workbook.createSheet( "Sheet1" );
			
			//Style stuff
			
			HSSFPalette palette = workbook.getCustomPalette();
			
			HSSFFont defaultFont = workbook.createFont();
			defaultFont.setFontHeightInPoints( ( short ) 11 );
			defaultFont.setFontName( "Calibri" );
			defaultFont.setColor( IndexedColors.BLACK.getIndex() );
			defaultFont.setBold(false);
		    defaultFont.setItalic(false);
		    
		    HSSFFont boldFont = workbook.createFont();
		    boldFont.setFontHeightInPoints( ( short ) 11 );
		    boldFont.setFontName( "Calibri" );
		    boldFont.setColor( IndexedColors.BLACK.getIndex() );
		    boldFont.setBold(true);
		    boldFont.setItalic(false);
		    
		    CellStyle traitStyle = workbook.createCellStyle();
		    traitStyle.setFont( boldFont );
		    traitStyle.setFillForegroundColor( IndexedColors.GREY_50_PERCENT.getIndex() );
		    traitStyle.setFillPattern( CellStyle.SOLID_FOREGROUND );
		    traitStyle.setBorderBottom( CellStyle.BORDER_THIN );
		    traitStyle.setBorderTop( CellStyle.BORDER_THIN );
		    
		    CellStyle markerStyle = workbook.createCellStyle();
		    markerStyle.setFont( defaultFont );
		    
		    CellStyle redStyle = workbook.createCellStyle();
		    redStyle.setFont( defaultFont );
		    redStyle.setBorderBottom( CellStyle.BORDER_THIN );
		    redStyle.setBorderTop( CellStyle.BORDER_THIN );
		    redStyle.setBorderRight( CellStyle.BORDER_THIN );
		    redStyle.setBorderLeft( CellStyle.BORDER_THIN );
		    redStyle.setFillForegroundColor( IndexedColors.RED.getIndex() );
		    redStyle.setFillPattern( CellStyle.SOLID_FOREGROUND );
		    
		    CellStyle priorityStyle = workbook.createCellStyle();
		    priorityStyle.setFont( defaultFont );
		    HSSFColor sequenced = palette.findSimilarColor(215, 230, 190);
		    priorityStyle.setFillForegroundColor( sequenced.getIndex() );
		    priorityStyle.setFillPattern( CellStyle.SOLID_FOREGROUND );
		    priorityStyle.setAlignment( CellStyle.ALIGN_CENTER );

		    
		    CellStyle rightSide = workbook.createCellStyle();
		    rightSide.setBorderLeft( CellStyle.BORDER_THIN );
		    		    
		    //Name
			HSSFRow name = sheet.createRow( ( short ) 0 );
			name.createCell( 0 );
			CellStyle headerStyle = workbook.createCellStyle();
			headerStyle.setFont( boldFont );
			name.getCell( 0 ).setCellStyle( headerStyle );
			name.getCell( 0 ).setCellValue( projectName );
			
			//Header
			HSSFRow header = sheet.createRow( ( short ) 3 );
			for ( int i = 0; i < plateNames.length; i++ ) {
				header.createCell( i + 1 );
				header.getCell( i + 1 ).setCellValue( plateNames[ i ] );
				header.getCell( i + 1 ).setCellStyle( headerStyle );
			}
			header.createCell( plateNames.length + 1 );
			header.createCell( plateNames.length + 2 );
			header.createCell( plateNames.length + 3 );
			header.createCell( plateNames.length + 5 );
			header.getCell( plateNames.length + 1 ).setCellValue( "Control (** indicates none required)" );
			header.getCell( plateNames.length + 1 ).setCellStyle( headerStyle );
			header.getCell( plateNames.length + 2 ).setCellValue( "Priority (1 = highest)" );
			header.getCell( plateNames.length + 2 ).setCellStyle( headerStyle );
			header.getCell( plateNames.length + 3 ).setCellValue( "Comments" );
			header.getCell( plateNames.length + 3 ).setCellStyle( headerStyle );
			header.getCell( plateNames.length + 5 ).setCellValue( "Control Placement" );
			header.getCell( plateNames.length + 5 ).setCellStyle( headerStyle );

			//Rows
			short row = 4;
			int i = 0;
			while ( true ) {
				if ( traits[ i ].checked ) {
					sheet.createRow( row );
					sheet.getRow( row ).createCell( 0 );
					sheet.getRow( row ).getCell( 0 ).setCellValue( traits[ i ].name );
					sheet.getRow( row ).getCell( 0 ).setCellStyle( traitStyle );
					for ( int j = 1; j <= plateNames.length; j++ ) {
						sheet.getRow( row ).createCell( j );
						sheet.getRow( row ).getCell( j ).setCellStyle( traitStyle );
					}
					sheet.getRow( row ).createCell( plateNames.length + 1 );
					sheet.getRow( row ).getCell( plateNames.length + 1 ).setCellStyle( rightSide );
					row++;
					for ( int j = 0; j < traits[ i ].markers.length; j++ ) {
						if ( traits[ i ].markers[ j ].checked ) {
							sheet.createRow( row );
							sheet.getRow( row ).createCell( 0 );
							sheet.getRow( row ).getCell( 0 ).setCellValue( traits[ i ].markers[ j ].name );
							sheet.getRow( row ).getCell( 0 ).setCellStyle( markerStyle );
							for ( int a = 1; a <= plateNames.length; a++ ) {
								sheet.getRow( row ).createCell( a );
								sheet.getRow( row ).getCell( a ).setCellStyle( redStyle );
							}
							sheet.getRow( row ).createCell( plateNames.length + 1 );
							sheet.getRow( row ).getCell( plateNames.length + 1 ).setCellValue( traits[ i ].markers[ j ].control );
							sheet.getRow( row ).getCell( plateNames.length + 1 ).setCellStyle( rightSide );
							sheet.getRow( row ).createCell( plateNames.length + 2 );
							sheet.getRow( row ).getCell( plateNames.length + 2 ).setCellValue( 1 );
							sheet.getRow( row ).getCell( plateNames.length + 2 ).setCellStyle( priorityStyle );
							sheet.getRow( row ).createCell( plateNames.length + 3 );
							sheet.getRow( row ).getCell( plateNames.length + 3 ).setCellValue( traits[ i ].markers[ j ].comments );
							sheet.getRow( row ).getCell( plateNames.length + 3 ).setCellStyle( markerStyle );
							row++;
						}
					}
				}
				i++;
				if ( i >= traits.length ) {
					break;
				}
			}
			
			if ( row < 15 ) {
				for ( short a = (short) (row + 1); a <= 15; a++ ) {
					sheet.createRow( a );
				}
			}
			String[] labels = { "not yet run", "PCR run but not validated", "PCR validated and approved for sequencing", "sequenced/KASP run", "analyzed", "entered into allele report" };
			short[] colors = { IndexedColors.RED.getIndex(), palette.findSimilarColor( 250, 192, 144 ).getIndex(), IndexedColors.YELLOW.getIndex(), sequenced.getIndex(), IndexedColors.GREY_25_PERCENT.getIndex(), IndexedColors.GREY_80_PERCENT.getIndex() };
			CellStyle[] scaleColors = new CellStyle[ 6 ];
			for ( int num = 0; num < 6; num++ ) {
				scaleColors[ num ] = workbook.createCellStyle();
			    scaleColors[ num ].setFillForegroundColor( colors[ num ] );
			    scaleColors[ num ].setFillPattern( CellStyle.SOLID_FOREGROUND );
			    scaleColors[ num ].setBorderBottom( CellStyle.BORDER_THIN );
			    scaleColors[ num ].setBorderTop( CellStyle.BORDER_THIN );
			    scaleColors[ num ].setBorderLeft( CellStyle.BORDER_THIN );
			    scaleColors[ num ].setBorderRight( CellStyle.BORDER_THIN );
			}
			int index = 0;
			for ( short a = 10; a <=15; a++ ) {
				sheet.getRow( a ).createCell( plateNames.length + 5 );
				sheet.getRow( a ).getCell( plateNames.length + 5 ).setCellStyle( scaleColors[ index ] );
				sheet.getRow( a ).createCell( plateNames.length + 6 );
				sheet.getRow( a ).getCell( plateNames.length + 6 ).setCellStyle( markerStyle );
				sheet.getRow( a ).getCell( plateNames.length + 6 ).setCellValue( labels[ index ] );
				index++;
			}
			
			
			FileOutputStream fileOut = new FileOutputStream( filename );
			workbook.write( fileOut );
			fileOut.close();
			workbook.close();
			System.exit( 0 );
		} catch ( Exception e ) {
			System.out.println( e );
			System.exit( 1 );
		}
	}
	public static void main( String[] args ) {
		String[] plates = new String[ 5 ];
		plates[ 0 ] = "Plate0";
		plates[ 1 ] = "Plate1";
		plates[ 2 ] = "Plate2";
		plates[ 3 ] = "Plate3";
		plates[ 4 ] = "Plate4";
		Random randomInt = new Random();
		Trait[] traits = new Trait[ 7 ];
		for ( int i = 0; i < traits.length; i++ ) {
			traits[ i ] = new Trait();
			traits[ i ].setName( "Trait" + i );
			traits[ i ].checked  = true;
			traits[ i ].markers = new Marker[ randomInt.nextInt( 9 ) + 1 ];
			for ( int j = 0; j < traits[ i ].markers.length; j++ ) {
				traits[ i ].markers[ j ] = new Marker();
				traits[ i ].markers[ j ].setName( "Marker" + j );
				traits[ i ].markers[ j ].checked = true;
			}
		}
		new ExcelSheetMaker( "Test Project", traits, plates );
	}
}
