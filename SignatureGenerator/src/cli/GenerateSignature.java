package cli;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Scanner;

public class GenerateSignature {

	private static String name;
	private static String office;
	private static String direct = "";
	private static String mobile;
	private static String email;
	private static String title;
	private static String address;
	private static char location;
	private static String ext;
	private static boolean keepgoing = false;
	private static Scanner input;
	private static boolean first = true;

	public static void main(String[] args) {
		do {
			Scanner in = new Scanner( System.in );

			if ( args.length == 0 ) {
				System.out.println( "Lines with a * are required" );
				System.out.print( "Name*: ");
				name = in.nextLine();
				while ( name.equals( "" ) ) {
					System.out.println( "Name is required" );
					System.out.print( "Name*: ");
					name = in.nextLine();
				}
				System.out.print( "Email (leave out @withersravenel.com)*: " );
				email = in.nextLine();
				while ( email.equals( "" ) ) {
					System.out.println( "Email is required" );
					System.out.print( "Email*: ");
					name = in.nextLine();
				}
				System.out.print( "Location (c/g/p/r/w)*: ");
				location = in.nextLine().toLowerCase().charAt( 0 );
				while ( location != 'c' && location != 'g' && location != 'p' && location != 'r' && location != 'w' ) {
					System.out.println( "Location is required" );
					System.out.print( "Location (c/g/p/r/w)*: ");
					location = in.nextLine().toLowerCase().charAt( 0 );
				}
				System.out.print( "Title: ");
				title = in.nextLine();
				System.out.print( "Extension: ");
				ext = in.nextLine();
				System.out.println( "Input phone number with no hyphens or spaces (xxxxxxxxxx)" );
				System.out.print( "Mobile: ");
				mobile = in.nextLine();
			} else {
				keepgoing = true;
				try {
					if ( first ) {
						input = new Scanner( new File( args[ 0 ] ) );
					}
					String[] line = input.nextLine().split( "," );
					if ( line[ 0 ].equals( "name" ) ) {
						line = input.nextLine().split( "," );
					} else if ( line.length == 0 ) {
						break;
					}
					name = line[ 0 ];
					email = line[ 1 ];
					location = line[ 2 ].charAt( 0 );
					title = line[ 3 ];
					ext = line[ 4 ];
					mobile = line[ 5 ];
					if ( !input.hasNextLine() ) {
						keepgoing = false;
					}
				} catch ( Exception e ) {
					System.err.println( "Error opening input file" );
				}
			}

			if ( !mobile.equals( "" ) ) {
				String temp = mobile.substring( 0, 3 ) + "." + mobile.substring( 3, 6 ) + "." + mobile.substring( 6 );
				mobile = temp;
			}
			if ( !ext.equals( "" ) ) {
				switch ( ext.charAt( 0 ) ) {
				case '1':
					direct = "919.535.5" + ext;
					break;
				case '2':
					direct = "919.535.5" + ext;
					break;
				case '3':
					direct = "919.238.0" + ext;
					break;
				case '4':
					direct = "919.238.0" + ext;
					break;
				case '5':
					direct = "910.509.6" + ext;
					break;
				}
			}
			switch ( location ) {
			case 'c':
				office = "919.469.3340";
				address = "115 MacKenan Drive <b><font size=\"1\" color=\"#7AAB65\">  &thinsp; | &thinsp; </font></b> Cary, NC 27511 <br>";
				break;
			case 'g':
				office = "336.605.3009";
				address = "7011 Albert Pick Rd. Suite G <b><font size=\"1\" color=\"#7AAB65\"> &thinsp; | &thinsp; </font></b>Greensboro, NC 27409<br>";
				break;
			case 'p':
				office = "919.469.3340";
				address = "55 Grant Drive, Suite D <b><font size=\"1\" color=\"#7AAB65\">  &thinsp; | &thinsp; </font></b> Pittsboro, NC 27312 <br>";
				break;
			case 'r':
				office = "919.469.3340";
				address = "137 S Wilmington Street <b><font size=\"1\" color=\"#7AAB65\">  &thinsp; | &thinsp; </font></b> Suite 200<b><font size=\"1\" color=\"#7AAB65\"> &thinsp; | &thinsp; </font></b>Raleigh, NC 27601<br>";
				break;
			case 'w':
				office = "910.256.9277";
				address = "1410 Commonwealth Drive, Suite 101 <b><font size=\"1\" color=\"#7AAB65\">  &thinsp; | &thinsp; </font></b> Wilmington, NC 28403 <br>";
				break;
			}

			try {
				PrintStream out = new PrintStream( new File( "Z:\\WR Signatures\\HTML - New Brand 2015\\" + email + ".htm" ) );
				out.println( "<html xmlns=\"http://www.w3.org/1999/xhtml\"\n xmlns:v=\"urn:schemas-microsoft-com:vml\"\n xmlns:o=\"urn:schemas-microsoft-com:office:office\">\n<head><!--[if gte mso 9]><xml>\n <o:OfficeDocumentSettings>\n  <o:AllowPNG/>\n  <o:PixelsPerInch>96</o:PixelsPerInch>\n </o:OfficeDocumentSettings>\n</xml><![endif]--></head>\n<body>\n<b><span style=\"FONT-FAMILY: Ubuntu, Arial; FONT-SIZE: 12pt\">" );
				out.println( name );
				out.println( "</b><br><span style=\"FONT-FAMILY: Lato, Arial; FONT-SIZE: 10pt\">" );
				if ( !title.equals( "" ) ) {
					out.println( title );
				}
				out.println( "<span style='mso-fareast-font-family:\"Times New Roman\";\ndisplay:none;mso-hide:all'>WithersRavenel <o:p></o:p></span>\n<br>\n\n<span style=\"FONT-FAMILY: Lato, Arial; FONT-SIZE: 12pt\">\n<!--[if gte vml 1]><v:shapetype id=\"_x0000_t75\"\n coordsize=\"21600,21600\" o:spt=\"75\" o:preferrelative=\"t\" path=\"m@4@5l@4@11@9@11@9@5xe\"\n filled=\"f\" stroked=\"f\">\n <v:stroke joinstyle=\"miter\"/>\n <v:formulas>\n  <v:f eqn=\"if lineDrawn pixelLineWidth 0\"/>\n  <v:f eqn=\"sum @0 1 0\"/>\n  <v:f eqn=\"sum 0 0 @1\"/>\n  <v:f eqn=\"prod @2 1 2\"/>\n  <v:f eqn=\"prod @3 21600 pixelWidth\"/>\n  <v:f eqn=\"prod @3 21600 pixelHeight\"/>\n  <v:f eqn=\"sum @0 0 1\"/>\n  <v:f eqn=\"prod @6 1 2\"/>\n  <v:f eqn=\"prod @7 21600 pixelWidth\"/>\n  <v:f eqn=\"sum @8 21600 0\"/>\n  <v:f eqn=\"prod @7 21600 pixelHeight\"/>\n  <v:f eqn=\"sum @10 21600 0\"/>\n </v:formulas>\n <v:path o:extrusionok=\"f\" gradientshapeok=\"t\" o:connecttype=\"rect\"/>\n <o:lock v:ext=\"edit\" aspectratio=\"t\"/>\n</v:shapetype><v:shape id=\"Picture_x0020_10\" o:spid=\"_x0000_i1029\" type=\"#_x0000_t75\"\n alt=\"WithersRavenel\" href=\"http://www.withersravenel.com/\" style='width:185.25pt;\n height:36.75pt;visibility:visible;mso-wrap-style:square' o:button=\"t\">\n <v:fill o:detectmouseclick=\"t\"/>\n <v:imagedata src=\"http://withersravenel.com/wp-content/uploads/2015/08/EmailSignatureLogo.png\"/>\n</v:shape><![endif]-->\n</v:shape><![endif]--><br>" );
				out.println( "</b><span style=\"FONT-FAMILY: Lato, Arial; FONT-SIZE: 9pt\"><font color=\"#8C8681\">\n" + address );
				out.print( "Office: " + office );
				out.print( "<b><font size=\"1\" color=\"#7AAB65\">" );
				if ( !direct.equals( "" ) || !mobile.equals( "" ) ) {
					out.print( " &thinsp; | &thinsp; </font></b>" );
				}
				if ( !direct.equals( "" ) ) {
					out.println( "Direct: " + direct );
					if ( !mobile.equals( "" ) ) {
						out.print( "<br></b><span style=\"FONT-FAMILY: Lato, Arial; FONT-SIZE: 9pt\"><font color=\"#8C8681\">Mobile: " + mobile );
					}
				} else if ( !mobile.equals( "" ) ) {
					out.println( "Mobile: " + mobile );
				}
				out.println( "<br>" );
				out.print( "<a href=\"mailto:" + email + "@withersravenel.com\">" + email + "@withersravenel.com</a>" );
				out.print( "<br>\n<br>\n<a href=\"https://www.linkedin.com/company/1040027?trk=tyah&trkInfo=clickedVertical%3Acompany%2CclickedEntityId%3A1040027%2Cidx%3A21-3%2CtarId%3A1439227492366%2Ctas%3Awithersravenel\"><img title=\"LinkedIn\" img src=\"http://withersravenel.com/wp-content/uploads/2015/09/linkedin.png\" alt=\"LinkedIn\"/></a>\n<a href=\"https://www.facebook.com/withersravenel?ref=bookmarks\"><img title=\"Facebook\"  img src=\"http://withersravenel.com/wp-content/uploads/2015/09/facebook.png\" alt=\"Facebook\"/></a>\n<a href=\"https://twitter.com/WithersRavenel\"><img title=\"Twitter\" img src=\"http://withersravenel.com/wp-content/uploads/2015/09/twitter.png\" alt=\"Twitter\"/></a>\n<a href=\"https://www.youtube.com/channel/UC1Y4TLZTX7cTZxW_zKAXz_Q\"><img title=\"YouTube\" img src=\"http://withersravenel.com/wp-content/uploads/2015/09/youtube.png\"alt=\"Youtube\"/></a>\n<br>\n<br>\n<a href=\"http://withersravenel.com/wp-content/uploads/2015/09/Confidentiality.png\">CONFIDENTIALITY AND NONDISCLOSURE</a>\n</html></body>" );
				System.out.println( "Signature file saved as Z:\\WR Signatures\\HTML - New Brand 2015\\" + email + ".htm");
			} catch (FileNotFoundException e) {
				System.err.println( "Could not create file" );
				e.printStackTrace();
			}		
			in.close();
			first = false;
		} while ( keepgoing == true );
	}

}
