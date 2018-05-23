package com.pdfgen.spring.managers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;


@Component
@RequestMapping("/user")
public class PdfDBManager {

	private PdfGenManager pCon = null ;
	
	private Connection dbCon = null ;
	

	// SQL Server Connection information
	private String dbURL ; 
	private String username; 
	private String password;

	//*******************************************
	// CONSTRUCTORS
	//*******************************************
	
	public PdfDBManager(PdfGenManager pCon) { 
		this.pCon = pCon;
		dbURL = pCon.prefs.get("DB_URL", "jdbc:mysql://localhost:3306/mysql") ; 
		username = pCon.prefs.get("DB_USER", "hbstudent") ; 
		password = pCon.prefs.get("DB_PASS", "hbstudent") ; 
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
				sql += (" " + field.trim().toLowerCase() + " NVARCHAR(20), ") ;
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
	
	public void connectToDB() {
		dBAccessPrompt();
		connectDB();
	}

	public void dBAccessPrompt() { 
		/*JTextField db = new JTextField(25);
		db.setText(dbURL);
		JTextField user = new JTextField(15);
		user.setText(username);
		JPasswordField pass = new JPasswordField(15);
		pass.setText(password);

		JPanel myPanel = new JPanel();
		myPanel.add(new JLabel("Database URL: "));
		myPanel.add(db);
		myPanel.add(Box.createHorizontalStrut(15)); 

		myPanel.add(new JLabel("Username: "));
		myPanel.add(user);
		myPanel.add(Box.createHorizontalStrut(15)); 

		myPanel.add(new JLabel("Password: "));
		myPanel.add(pass);
		myPanel.add(Box.createHorizontalStrut(15));
*/
		//int result = JOptionPane.showConfirmDialog(null, myPanel, 
		//		"Please enter Database Information", JOptionPane.OK_CANCEL_OPTION);
		//if (result == JOptionPane.OK_OPTION) {
	//		dbURL = db.getText();
	//		username = user.getText();
	//		password = String.copyValueOf(pass.getPassword());
			pCon.prefs.put("DB_URL", dbURL) ; 
			pCon.prefs.put("DB_USER", username) ; 
			pCon.prefs.put("DB_PASS", password) ; 
		//}
	}
	
	private void connectDB() {
		try {
			System.out.println("Connected") ;
			dbCon = DriverManager.getConnection(dbURL, username, password) ;
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	//*******************************************
	// USER CREDENTIAL HANDLER METHODS
	//*******************************************

	public boolean createUser(String user, String email, String hashedPass) {
		PreparedStatement stmt = null ;
		try {
			stmt = dbCon.prepareStatement("INSERT INTO user_cred.user_credentials VALUES(id, ?, ?, ?, NOW() ) ;");
			stmt.setString(1, user);
			stmt.setString(2, email);
			stmt.setString(3, hashedPass);
			stmt.executeUpdate() ;
			createSchema(user);
			System.out.println("User Created");
			//JOptionPane.showMessageDialog(null, "User Created") ;
		} catch (SQLException e) {
			System.out.println("User Already Exists") ;
			//JOptionPane.showMessageDialog(null, "User Already Exists");
			e.printStackTrace();
			return false ;
		}
		return true ;
	}

	private void createSchema(String user) {
		PreparedStatement stmt = null ;
		try {
			String sql = "CREATE SCHEMA IF NOT EXISTS " + user ;
			stmt = dbCon.prepareStatement(sql);
			stmt.executeUpdate() ;
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public boolean checkUser(String text) {
		Statement stmt = null ;
		try {
			stmt = dbCon.createStatement();
			String sql = "SELECT `username` FROM user_cred.user_credentials WHERE `username` = '" + text + "' ;";
			ResultSet rs = stmt.executeQuery(sql) ;
			if(!rs.next()) { 
				return true ;

				//TODO add second check here to see if there is a schema created.


			}else {
				System.out.println("User Aleady Exists");
				//JOptionPane.showMessageDialog(null, "User Already Exists");
				return false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return true ;
		}
	}

	public String getHashedPass(String user) {
		PreparedStatement stmt = null ;
		try {
			stmt = dbCon.prepareStatement("SELECT * FROM user_cred.user_credentials WHERE `username` = ? ;");
			stmt.setString(1,  user);
			ResultSet rs = stmt.executeQuery() ;
			rs.next() ;
			return rs.getString(4) ;
		}catch(SQLException se) {
			System.out.println("No user with that username.");
			return null;
		}catch(Exception e) { 
			e.printStackTrace(); 
			return null ; 
		}

	}

	public boolean changePassword(String user, String oldPassword, String newPassword) {
		PreparedStatement stmt = null ;
		try {
			stmt = dbCon.prepareStatement("INSERT INTO user_cred.user_credentials WHERE `username` = ? COLUMN(`hashed_pass`) VALUES(?) ;");
			stmt.setString(1, user);
			stmt.setString(2, newPassword);
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
}


