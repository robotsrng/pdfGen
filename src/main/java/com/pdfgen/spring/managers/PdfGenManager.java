package com.pdfgen.spring.managers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.prefs.Preferences;

import org.apache.commons.io.FileUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;


//import com.pdfgen.spring.run.PdfEmailGUI;
@Controller
@RequestMapping("pGen")
public class PdfGenManager {

	//	public static PdfEmailGUI pGUI ;
	private static File inFile ;
	public static PdfManager pMan ;
	public static PdfDBManager pDB ; 
	private static boolean login = false ;
	private final static String localID = "IDLOCAL" ;
	private static String userIdentification = "IDLOCAL" ;
	public static Preferences prefs ; 

	//*******************************************
	// PAGE REDIRECTION
	//*******************************************

	@GetMapping(value = {"/showForm", "/"})
	public String showForm() {
		return "gen/generator-page" ;
	}

	@GetMapping("/text-upload")
	public String textUpload() {
		return "gen/text-upload";
	}

	//*******************************************
	// CONSTRUCTORS
	//*******************************************

	public PdfGenManager() { 
		prefs = Preferences.userNodeForPackage(this.getClass());
		pMan = new PdfManager(this) ;
		pDB = new PdfDBManager(this) ; 
		checkLocal();
		setPDB() ;
	} 

	private void checkLocal() {
		String resDir = prefs.get("RESDIR_PATH", "user_files");
		Path path = Paths.get(resDir + "/" + userIdentification);
		if (!Files.exists(path)) {
			new File(resDir + "/" + userIdentification).mkdir();
		}else {  
			return ;
		}
	}

	//*******************************************
	// PDF Handler Methods
	//*******************************************

	@PostMapping("addPdfTemplate")
	public static String addPdfTemplate(@RequestParam("file") MultipartFile file) throws IllegalStateException, IOException {
		if((inFile = UpDownController.uploadPdf(file)) == (null)) {
			return "error/badfile" ;
		}
		if(pMan.processNewPdf(userIdentification, inFile)) {
			clearInFile();
			return "gen/add-pdf-confirmation";			
		}else { 
			return "error/fileexists" ;
		}
	}

	@PostMapping("fillPdfFile")
	public String fillPdf(@RequestParam("file") MultipartFile file) { 
		System.out.println(userIdentification);
		if((inFile = UpDownController.uploadText(file)) == (null)) {
			return "error/badfile" ;
		}
		if(!pMan.fillPdf(userIdentification, pMan.parseTextFile(inFile))) { 
			return "error-page" ;
		}
		return "gen/download-page" ;
	}

	@PostMapping("fillPdfText")
	public String fillPdf(@RequestParam("email") String email) { 
		System.out.println(userIdentification);
		if(email.equals(null)) {
			return "error/badfile" ;
		}
		if(!pMan.fillPdf(userIdentification, pMan.parseTextFile(email))) { 
			return "error/error-page" ;
		}
		return "error/download-page" ;
	} 

	public void handleFileDrop(File file) { 
		String mime = "" ;
		try {
			mime = Files.probeContentType(file.toPath()) ;
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(mime.equals("application/pdf")) { 
			pMan.processNewPdf(userIdentification, file);
		}
		else { 
			pMan.fillPdf(userIdentification, pMan.parseTextFile(file));
		}
	}

	public void createDBTable(String username, String tableName, ArrayList<String> theFields) { 
		pDB.createTable(username, tableName, theFields);
	}

	public void addDataToDB(String username, String tableName, ArrayList<String> textArray) {
		pDB.addDataToDB(username, tableName, textArray);
	}

	//*******************************************
	// USER CREDENTIALS 
	//*******************************************

	public static boolean validateUser(String user, char[] pass) {
		String storedPass = pDB.getHashedPass(user) ;
		if(storedPass == null) { 
			//JOptionPane.showMessageDialog(null, "Invalid User") ;
			System.out.println("Invalid User") ;
			userDisConnect();
			return false ;
		}
		if(pass.toString().equals(storedPass)) {
			System.out.println(user + " is connected.");
			//JOptionPane.showMessageDialog(null, user + " is connected.") ;
			return true;
		}
		userDisConnect() ;
		System.out.println("Bad Login Info. Check username and password.");
		//JOptionPane.showMessageDialog(null, "Bad Login Info. Check username and password.");
		return false ;
	}

	public static boolean userConnect(String username) {
		/*JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel,  BoxLayout.Y_AXIS));
		JLabel lblUser = new JLabel("Enter username:");
		JLabel lblPass = new JLabel("Enter password:");
		JTextField user = new JTextField(15) ;
		user.setText(prefs.get("LAST_USER", ""));
		JPasswordField pass = new JPasswordField(15);
		pass.requestFocusInWindow();
		panel.add(lblUser);
		panel.add(user);
		panel.add(lblPass);
		panel.add(pass);
		String[] options = new String[]{"OK", "Cancel", "New User"};
		int option = JOptionPane.showOptionDialog(null, panel, "User Login",
				JOptionPane.NO_OPTION, JOptionPane.PLAIN_MESSAGE,
				null, options, options[0]);
		if(option == 1) {
			return;
		}
		if(option == 0){*/
		setPDB();
		setUsername(username) ;
		prefs.put("LAST_USER", userIdentification);
		setLogin(true) ;
		//				pGUI.setConnectEnable(false);
		return login;
	}


	public static void userDisConnect() { 
		System.out.println(userIdentification + " disconnected.");
		resetUsername();
		setLogin(false);
		pMan.clearInfo();
		//		pGUI.setConnectEnable(true);
	}

	public static boolean userCreate(String name, String userEmail, String userPassword, String userPassConf) { 
		//		JPanel panel = new JPanel();
		//		panel.setLayout(new BoxLayout(panel,  BoxLayout.Y_AXIS));
		//		JLabel lblCreate = new JLabel("Enter new user information") ; 
		//		JLabel lblUser = new JLabel("Enter username:");
		//		JLabel lblEmail = new JLabel("Enter email:");
		//		JLabel lblPass = new JLabel("Enter password:");
		//		JLabel lblPass2 = new JLabel("Re-Enter password:");
		//		JTextField user = new JTextField(25) ;
		//		JTextField email = new JTextField(25) ;
		//		JPasswordField pass = new JPasswordField(25);
		//		JPasswordField pass2 = new JPasswordField(25);
		//		panel.add(lblCreate);
		//		panel.add(lblUser);
		//		panel.add(user);
		//		panel.add(lblEmail);
		//		panel.add(email);
		//		panel.add(lblPass);
		//		panel.add(pass);
		//		panel.add(lblPass2);
		//		panel.add(pass2);
		//
		//		String[] options = new String[]{"OK", "Cancel"};
		//		int option = JOptionPane.showOptionDialog(null, panel, "User Creation",
		//				JOptionPane.NO_OPTION, JOptionPane.PLAIN_MESSAGE,
		//				null, options, options[0]);
		//		
		//		
		//		if(option == 0){
		//			if(!Arrays.equals(pass.getPassword(), pass2.getPassword())) { //TODO ADD VALIDATION FUNCTION
		//				JOptionPane.showMessageDialog(null, "Your passwords do not match") ;
		//				pass.setText("");
		//				pass2.setText("");
		//			}else if(!pDB.checkUser(user.getText())) { 
		//				pDB.createUser(user.getText(), email.getText(), (PasswordEncryptor.buildHashedPass(pass.getPassword()))) ;
		//				pMan.createUserDir(user.getText()) ;
		//			}
		//		}else if(option == 1) { 
		//			return ;
		//		}
		String username = name.trim();
		String email = userEmail.trim().toLowerCase();
		String password = userPassword.trim();
		String passConf = userPassConf.trim();
		setPDB() ;
		if(!passwordValidation(password, passConf)) {System.out.println(1); return false; } //TODO Add error code reporting
		if(pDB.doesUserExist(username)) { System.out.println(2); return false; }
		pDB.createUser(username, email, password) ; 
		pMan.createUserDir(username);
		return true;
	}

	public static boolean passwordValidation(String password, String passConf) { 
		if(!password.equals(passConf)) {return false; }
		return true;
	}

	//*******************************************
	// Getters and Setters
	//*******************************************

	public String getUsername() {
		return userIdentification;
	}

	public static void setUsername(String username) {
		userIdentification = username;
	}

	public static void resetUsername() { 
		userIdentification = localID ;
	}

	public static void setLogin(boolean status) { 
		login = status ;
	}

	public boolean isLogin() { 
		return login ;
	}

	public static void setPDB() {
		if(!checkConnection()) {
			pDB.connectToDB();
		}
	}

	public static boolean checkConnection() { 
		if (pDB == null) { return false ; } 
		return pDB.checkConnection() ;
	}

	public void changeDirectory() { 
		pMan.setTopLevelDir(userIdentification);
	}

	private static void clearInFile() throws IOException {
		inFile.delete();
		inFile = null ;
		FileUtils.cleanDirectory(new File("user_files/temp"));
	}

	public static MyUser getUser(String name) {
		return(pDB.getUser(name)) ;
	}
}