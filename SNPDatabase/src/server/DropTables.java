package server;

import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

public class DropTables {

	private static String sha256(String base) {
	    try{
	        MessageDigest digest = MessageDigest.getInstance("SHA-256");
	        byte[] hash = digest.digest(base.getBytes("UTF-8"));
	        StringBuffer hexString = new StringBuffer();

	        for (int i = 0; i < hash.length; i++) {
	            String hex = Integer.toHexString(0xff & hash[i]);
	            if(hex.length() == 1) hexString.append('0');
	            hexString.append(hex);
	        }

	        return hexString.toString();
	    } catch(Exception ex){
	       throw new RuntimeException(ex);
	    }
	}
	
	public static void main(String[] args) {
		MysqlDataSource ds;
		Connection conn;
		ds = new MysqlDataSource();
		ds.setUser( "root" );
		ds.setPassword( "B1oinformatics" );
		ds.setServerName( "localhost" );
		ds.setDatabaseName( "SNPDatabase" );
		try {
			conn = ds.getConnection();
			if ( args.length > 0 && args[ 0 ].equals( "add" ) ) {
				conn.prepareStatement( "INSERT INTO users(Username, Admin, Pword) VALUES ('admin', 1, '" + sha256( "password" ) + "')").execute();
			} else {

				ResultSet rs = conn.prepareStatement( "SHOW TABLES;" ).executeQuery();
				while( rs.next() ) {
					conn.prepareStatement( "DROP TABLE " + rs.getString( 1 ) + ";" ).execute();
				}
				conn.prepareStatement( "CREATE TABLE SNPData(Organism VARCHAR(10) NOT NULL, RSNum VARCHAR(50), Alleles VARCHAR(15), Chr VARCHAR(10), Pos VARCHAR(15), Strand VARCHAR(1), Assembly VARCHAR(2), Center VARCHAR(2), ProtLSID VARCHAR(2), AssayLSID VARCHAR(2), PanelLSID VARCHAR(2), QCCode VARCHAR(2), PRIMARY KEY (RSNum));").execute();
				conn.prepareStatement( "CREATE TABLE SNPs1(Organism VARCHAR(10) NOT NULL, RSNum VARCHAR(50), PRIMARY KEY (RSNum));" ).execute();
				conn.prepareStatement( "CREATE TABLE tables(NumberOfTables INT, PRIMARY KEY (NumberOfTables));").execute();
				conn.prepareStatement( "INSERT INTO tables(NumberOfTables) VALUES ( 1 );").execute();

			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
