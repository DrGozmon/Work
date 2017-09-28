package cli;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImageResizer {
	
	private static final int width = 1920;
	private static final int height = 1080;
	private static final String inputFolder = "I:/Digital Signs/_Original_Images/";
	private static final String outputFolder = "I:/Digital Signs/_Resized_Images/";

	public static void resize( String filename ) throws IOException {
		File inputFile = new File( inputFolder + filename);
		BufferedImage inputImage = ImageIO.read( inputFile );
		
		int newWidth = width, newHeight = height;
		
		if ( inputImage.getWidth() > inputImage.getHeight() ) {
			double ratio = (double) newWidth / (double) inputImage.getWidth();
			newHeight = (int) Math.round( (double) inputImage.getHeight() * ratio );
		} else {
			double ratio = (double) newHeight / (double) inputImage.getHeight();
			newWidth = (int) Math.round( (double) inputImage.getWidth() * ratio );
		}
		
		BufferedImage outputImage = new BufferedImage( newWidth, newHeight, inputImage.getType() );
		
		Graphics2D g2d = outputImage.createGraphics();
		g2d.drawImage( inputImage, 0, 0, newWidth, newHeight, null );
		g2d.dispose();
		
		String formatName = filename.substring( filename.lastIndexOf( "." ) + 1 );
		ImageIO.write( outputImage, formatName, new File( outputFolder + filename ) );
	}

	public static void main( String[] args ) {
		File dir = new File( inputFolder );
		File[] directoryListing = dir.listFiles();
		if ( directoryListing != null ) {
			for ( File child : directoryListing ) {
				if ( !child.getName().equals( "Thumbs.db" ) ) {
					try {
						resize( child.getName() );
					} catch (IOException e) {
						System.err.println( "Failed to open image: " + child.getName() );
						e.printStackTrace();
					}
				}
			}
		}
	}
}
