package com.pdfgen.spring.managers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;


@Component
public class PdfDBManager {


	private static Connection dbCon = null ;


	// SQL Server Connection information
	private final String dbURL =  "jdbc:mysql://localhost:3306/mysql" ; 
	private final String username = "root"; 
	private final String password = "toor" ;

	//*******************************************
	// CONSTRUCTORS
	//*******************************************

	public PdfDBManager(PdfGenManager pCon) { 
		connectDB();
	}
	
	//*******************************************
	// SQL Handler Methods 
	//*******************************************

	public void createTable(String username, String tableName, ArrayList<String> theFields) {
		Statement stmt = null ;
		try {
			stmt = dbCon.createStatement();
			String sql = "CREATE TABLE IF NOT EXISTS " + username + "." + tableName +
					"(id INTEGER not NULL AUTO_INCREMENT, " ;
			for (String field : theFields) { 
				sql += (" " + field.trim() + " NVARCHAR(20), ") ;
			}
			sql += "date_time DATETIME, PRIMARY KEY ( id ))" ; 
			stmt.executeUpdate(sql) ;
			System.out.println("SQL Table Creation Executed (No change if table exists already)");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void addDataToDB(String username, String tableName, ArrayList<String> textArray) { 
		Statement stmt = null;
		String columns = "" ;
		String values = "" ;
		for (String line : textArray) { 
			String[] temp = line.split(",") ;
			if(temp.length < 2|| temp[1] == null || temp[1].equals("") ) { continue ; } 
			columns += ("`" + temp[0].trim().toLowerCase() + "` , ") ;
			values +=  "'" + temp[1] + "'" +  ", " ;
		}
		try { 
			stmt = dbCon.createStatement() ;
			String sql = "INSERT INTO " + username + "." + tableName + " (id, " 
					+ columns + "date_time) VALUES (id, " 
					+ values + "NOW()) ; "; 
			stmt.executeUpdate(sql);
			System.out.println("Data Added to Database");
		}catch(Exception se) {  
			se.printStackTrace();
		}
	}

	public static List<String> getTableHeadersFromDB(String username, String tableName) {
		List<String> headers = new ArrayList<>() ;
		try { 
			PreparedStatement stmt = dbCon.prepareStatement("SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = ? AND TABLE_SCHEMA=?;") ;
			stmt.setString(1, tableName);
			stmt.setString(2, username);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				headers.add(rs.getString(1)) ;
			}
			return headers ;
		}catch(Exception e) {  
			e.printStackTrace();
			return null ;
		}
	}

	public static List<List<String>> getDataFromTable(String username, String tableName) {
		List<List<String>> rows = new ArrayList<>(); 
		try { 
			String qry = "SELECT * FROM  " + username + "." + tableName ;
			PreparedStatement stmt = dbCon.prepareStatement(qry) ; 
			ResultSet rs = stmt.executeQuery();
			int columnCount = rs.getMetaData().getColumnCount() ;
			while (rs.next()) {
				List<String> dataCells = new ArrayList<>() ;
				for(int i = 1 ; i < columnCount; i++) { 
					dataCells.add(rs.getString(i)) ;					 
				}
				rows.add(dataCells);
			}
			return rows ;
		}catch(Exception e) {  
			e.printStackTrace();
			return null ;
		}
	}

	//*******************************************
	// DB Connection Methods
	//*******************************************

	public boolean checkConnection() { 
		try {
			return dbCon.isValid(2) ;
		} catch (SQLException se) {
			se.printStackTrace();
			return false ;
		} catch( NullPointerException e) {
			return false ;
		}
	}

	public void connectDB() {
		try {
			dbCon = DriverManager.getConnection(dbURL, username, password) ;
			System.out.println("Connected") ;
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	//*******************************************
	// USER CREDENTIAL HANDLER METHODS
	//*******************************************

	public boolean createUser(String user, String pass) {
		PreparedStatement stmt = null ;
		try {
			stmt = dbCon.prepareStatement("INSERT INTO spring_security.user VALUES(user_id, ?, ?, ?) ;");
			stmt.setString(1, user);
			stmt.setString(2, pass);
			stmt.setString(3, pass);
			stmt.executeUpdate() ;
			String idQry = "SELECT `user_id` FROM spring_security.user WHERE `username` = '" + user + "' ;";
			ResultSet rs = stmt.executeQuery(idQry) ;
			rs.next();
			int userID = rs.getInt(1);
			stmt = dbCon.prepareStatement("INSERT INTO spring_security.user_role VALUES(?, ?, ?) ;");
			stmt.setInt(1, userID);
			stmt.setString(2, user);
			stmt.setInt(3, 2);
			stmt.executeUpdate() ;
			createSchema(user);
			System.out.println("User Created : " + user);
		} catch (SQLException e) {
			System.out.println("User Already Exists") ;
			e.printStackTrace();
			return false ;
		} 
		return true ;
	}

	private void createSchema(String user) {
		PreparedStatement stmt = null ;
		try {
			String sql = "CREATE SCHEMA IF NOT EXISTS `" + user + "`" ;
			stmt = dbCon.prepareStatement(sql);
			stmt.executeUpdate() ;
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static boolean doesUserExist(String username) {
		Statement stmt = null ;
		try {
			stmt = dbCon.createStatement();
			String sql = "SELECT `username` FROM spring_security.user WHERE `username` = '" + username + "' ;";

			ResultSet rs = stmt.executeQuery(sql) ;
			if(!rs.next()) { 
				return false ;
				//TODO add second check here to see if there is a schema created.
			}else {
				System.out.println("User Aleady Exists");
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return true ;
		}
	}

	public static ArrayList<MyUser> getAllUsers() { 
		Statement stmt = null ;
		ArrayList<MyUser> users = new ArrayList<MyUser>() ;
		try {
			stmt = dbCon.createStatement();
			String sql = "SELECT * FROM spring_security.user ;";
			ResultSet rs = stmt.executeQuery(sql) ;
			while(rs.next()) { 
				MyUser tempUser = new MyUser(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4));
				users.add(tempUser);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return users ; 
	} 

	public static String getUserAuthority(String username) {  
		PreparedStatement stmt = null ;
		try {
			stmt = dbCon.prepareStatement("select user.username, role from spring_security.user inner join spring_security.user_role on(user.user_id=user_role.user_id) inner join spring_security.role on(user_role.role_id=role.role_id) where user.username= ?") ;
			stmt.setString(1, username); 
			ResultSet rs = stmt.executeQuery() ; 
			if(rs.next()) { 
				return rs.getString(2);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("NULL AUTH");
		return null ;
	}
	
	public static void setUserAuthority(String username, String auth) { 
		if(!auth.equals("1") && !auth.equals("2")) { 
			return ;
		}
		PreparedStatement stmt = null ;
		try {
			stmt = dbCon.prepareStatement("update spring_security.user inner join spring_security.user_role on(user.user_id=user_role.user_id) inner join spring_security.role on(user_role.role_id=role.role_id) set user_role.role_id = ? where user.username= ? ;") ;
			stmt.setInt(1, Integer.valueOf(auth));
			stmt.setString(2, username); 
			stmt.executeUpdate() ; 
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return ;
	}

	public static MyUser getUser(String username) {
		Statement stmt = null ;
		try { 
			stmt = dbCon.createStatement();
			String sql = "SELECT username, password FROM spring_security.user WHERE `username` = '" + username + "' ;";
			ResultSet rs = stmt.executeQuery(sql) ;
			if(!rs.next()) { 
				return null ;
				//TODO add second check here to see if there is a schema created.
			}else {
				MyUser user = new MyUser();
				user.setName(rs.getString(1));
				user.setPassword(rs.getString(2));
				return user ;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static List<MyUser> findUser(String search) {
		PreparedStatement stmt = null ;
		List<MyUser> users = new ArrayList<>() ;
		try { 
			stmt = dbCon.prepareStatement("SELECT username FROM spring_security.user WHERE `username` LIKE '%" + search + "%' ;") ;
			ResultSet rs = stmt.executeQuery() ;
			while(rs.next()) {
				users.add(getUser(rs.getString(1))) ;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		return users;
	}

	public static List<String> findUserFiles(String username) {
		List<String> files = new ArrayList<>() ;
		PreparedStatement stmt = null ;
		try {
			stmt = dbCon.prepareStatement("SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA=?");
			stmt.setString(1, username);
			ResultSet rs = stmt.executeQuery() ;
			while(rs.next()) {
				files.add(rs.getString(1));
			}
			return files ;
		} catch (SQLException e) {
			e.printStackTrace();
			return null ;
		}
	}

	public String getHashedPass(String user) {
		PreparedStatement stmt = null ;
		try {
			stmt = dbCon.prepareStatement("SELECT password FROM spring_security.user WHERE `username` = ? ;");
			stmt.setString(1,  user);
			ResultSet rs = stmt.executeQuery() ;
			rs.next() ;
			System.out.println("GET HASHED PASS : " + rs.getString(1));
			return rs.getString(1) ;
		}catch(SQLException se) {
			System.out.println("No user with that username.");
			return null;
		}catch(Exception e) { 
			e.printStackTrace(); 
			return null ; 
		}

	}

	public static boolean changePassword(String user, String oldPassword, String newPassword) {
		PreparedStatement stmt = null ;
		try {
			stmt = dbCon.prepareStatement("UPDATE spring_security.user SET password = ? WHERE username = ?  ;");
			stmt.setString(1, newPassword);
			stmt.setString(2, user);
			System.out.println(stmt) ;
			stmt.executeUpdate() ;
			System.out.println("Password changed for user: " + user) ;
			//JOptionPane.showMessageDialog(null, "Password changed for user: " + user) ;
		} catch (SQLException e) {
			e.printStackTrace();
			return false ;
		}
		return true ;
	}

	public static void deleteUser(String user) {
		PreparedStatement stmt = null ;
		try {
			stmt = dbCon.prepareStatement("delete from spring_security.user WHERE username = ? ;" ); 
			stmt.setString(1, user);
			stmt.executeUpdate() ;
			stmt = dbCon.prepareStatement("delete from spring_security.user_role WHERE username = ? ;" );
			stmt.setString(1, user);
			stmt.executeUpdate() ;
			stmt = dbCon.prepareStatement("DROP SCHEMA " + user + "  ;" );
			stmt.executeUpdate() ;
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void deleteUserTable(String user, String filename) {
		PreparedStatement stmt = null ;
		try {
			stmt = dbCon.prepareStatement("DROP TABLE " + user + "." + filename + " ;" ); 
			stmt.executeUpdate() ;
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static void deleteUserRecord(String user, String filename, ArrayList<String> record) {
		for(String st : record) { 
			System.out.println(st);
		}
		PreparedStatement stmt = null ;
		try {
			stmt = dbCon.prepareStatement("DELETE FROM " + user + "." + filename + " where id = ? ");
			stmt.setInt(1, Integer.valueOf(record.get(0).replaceFirst(Pattern.quote("["), "")));
			System.out.println(stmt);
			stmt.executeUpdate() ;
		} catch (SQLException e) {
			e.printStackTrace();
		}
	
	}
}


