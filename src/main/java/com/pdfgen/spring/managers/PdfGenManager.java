package com.pdfgen.spring.managers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.prefs.Preferences;

import org.apache.commons.io.FileUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;


@Controller
@RequestMapping("gen")
public class PdfGenManager {

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

	@RequestMapping(value = {"/show-form"})
	public String showForm(Model model) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String currentPrincipalName = authentication.getName();
		model.addAttribute("user", currentPrincipalName);
		return "gen/generator-page" ;
	}
	
	@RequestMapping(value = {"/admin-show-form"})
	public String showForm(Model model, @ModelAttribute("user") String user ) {
		model.addAttribute("user", user);
		return "gen/generator-page" ;
	}

	@GetMapping("/text-upload")
	public String textUpload(Model model, @ModelAttribute("user") String user) {
		model.addAttribute("user", user);
		return "gen/text-upload";
	}
	
	@GetMapping("/admin-home")
	public String adminHome(Model model) {
		return "gen/admin-home" ;
	}
	
	@RequestMapping("/file-management")
	public String userDataManagement(Model model, @ModelAttribute("user") String user) {
		if(user.equals(null) || user.isEmpty() ) { 
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			String currentPrincipalName = authentication.getName();
			model.addAttribute("user", currentPrincipalName);
			model.addAttribute("userFiles", PdfDBManager.findUserFiles(currentPrincipalName) ) ;		
		}else { 
			model.addAttribute("user", user);
			model.addAttribute("userFiles", PdfDBManager.findUserFiles(user) ) ;		
		}
		return "file-list" ;
	}

	//*******************************************
	// CONSTRUCTORS
	//*******************************************

	public PdfGenManager() { 
		prefs = Preferences.userNodeForPackage(this.getClass());
		pMan = new PdfManager(this) ;
		pDB = new PdfDBManager(this) ; 
		setPDB() ;
	} 

	//*******************************************
	// PDF Handler Methods
	//*******************************************

	@PostMapping("/addPdfTemplate")
	public static String addPdfTemplate(Model model, @ModelAttribute("user") String user, @RequestParam("file") MultipartFile file) throws IllegalStateException, IOException {
		if((inFile = UpDownController.uploadPdf(file)) == (null)) {
			model.addAttribute("message", CodeRepo.getMessage("bad-file"));
			return "error" ;
		}
		if(PdfManager.processNewPdf(user, inFile)) {
			clearInFile();
			model.addAttribute("message", CodeRepo.getMessage("add-pdf-confirmation"));
			return "success";			
		}else { 
			model.addAttribute("message", CodeRepo.getMessage("file-exists"));
			clearInFile();
			return "error" ;
		}
	}

	@RequestMapping("/fillPdfFile")
	public String fillPdf(Model model, @ModelAttribute("user") String user, @RequestParam("file") MultipartFile file) { 
		if((inFile = UpDownController.uploadText(file)) == (null)) {
			model.addAttribute("message", CodeRepo.getMessage("bad-file"));
			return "error" ;
		}
		if(!PdfManager.fillPdf(user, PdfManager.parseTextFile(inFile), false)) {
			model.addAttribute("message", CodeRepo.getMessage("bad-email-file"));
			return "error" ;
		}
		return "download-page" ;
	}
	
	@RequestMapping("/download-user-file")
	private String downloadUserFile(Model model, @ModelAttribute("user") String user, @ModelAttribute("filename") String filename) {
		UpDownController.setDownload(PdfManager.retrievePdf(user, filename), filename + ".pdf");
		return "download-page" ;
	}
	
	@RequestMapping("/view-file-database")
	private String viewFileDatabase(Model model, @ModelAttribute("user") String user, @ModelAttribute("filename") String filename) {
		model.addAttribute("tableHeaders", PdfDBManager.getTableHeadersFromDB(user, filename)) ;
		model.addAttribute("tableData", PdfDBManager.getDataFromTable(user, filename));
		return "record-list" ;
	}

	@RequestMapping("print-from-database")
	public String printFromDatabase(Model model, @ModelAttribute("user") String user, @ModelAttribute("filename") String filename, @ModelAttribute("header") ArrayList<String> header, @ModelAttribute("data") ArrayList<String> data) {
		PdfManager.setFileName(filename);
		PdfManager.fillPdf(user, PdfManager.parseTextFile(parseDatabase(filename, header, data)), true) ;
		return "download-page" ;
	}
	
	public String parseDatabase(String filename, ArrayList<String> headers, ArrayList<String> data) { 
		String email = filename + "\n" ; 
		email += (headers.get(0).substring(1) + ", " + data.get(0).substring(1) + "\n") ;
		for (int i = 1 ; i < headers.size()-2 ; i += 1 ) {
			email += (headers.get(i) + ", " + data.get(i) + "\n") ;
		}
		email += (headers.get(headers.size()-2).substring(0,headers.get(headers.size()-2).length()) + ", " + data.get(data.size()-1).substring(0,data.get(data.size()-1).length()-1) + "\n") ;
		return email ;

	}
	
	@RequestMapping("/delete-file")
	private String deleteFile(Model model, @ModelAttribute("user") String user, @ModelAttribute("filename") String filename) {
		PdfManager.deleteUserFile(user, filename);
		PdfDBManager.deleteUserTable(user, filename);
		model.addAttribute("user", user ) ;
		model.addAttribute("userFiles", PdfDBManager.findUserFiles(user) ) ;
		return "file-list" ;
	}
	
	@PostMapping("fillPdfText")
	public String fillPdf(Model model, @ModelAttribute("user") String user, @ModelAttribute("email") String email) { 
		if(email.equals(null)) {
			return "error" ;
		}
		if(!PdfManager.fillPdf(user, PdfManager.parseTextFile(email), false)) { 
			return "error" ;
		}
		return "ownload-page" ;
	}
	
	@RequestMapping("/delete-record")
	private String deleteRecord(Model model, @ModelAttribute("user") String user, @ModelAttribute("filename") String filename, @ModelAttribute("record") ArrayList<String> record) {
		PdfDBManager.deleteUserRecord(user, filename, record);
		model.addAttribute("user", user ) ;
		model.addAttribute("tableHeaders", PdfDBManager.getTableHeadersFromDB(user, filename)) ;
		model.addAttribute("tableData", PdfDBManager.getDataFromTable(user, filename));
		return "record-list" ;
	}
	
	public String fillPdf(String user, String filename, String data) { 
		if(data.equals(null)) {
			return "error" ;
		}
		if(!PdfManager.fillPdf(userIdentification, PdfManager.parseTextFile(data), false)) { 
			return "error" ;
		}
		return "download-page" ;
	}

	public void handleFileDrop(File file) { 
		String mime = "" ;
		try {
			mime = Files.probeContentType(file.toPath()) ;
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(mime.equals("application/pdf")) { 
			PdfManager.processNewPdf(userIdentification, file);
		}
		else { 
			PdfManager.fillPdf(userIdentification, PdfManager.parseTextFile(file), false);
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
			System.out.println("Invalid User") ;
			userDisConnect();
			return false ;
		}
		if(pass.toString().equals(storedPass)) {
			System.out.println(user + " is connected.");
			return true;
		}
		userDisConnect() ;
		System.out.println("Bad Login Info. Check username and password.");
		return false ;
	}

	public static boolean userConnect(String username) {
		setPDB();
		setUsername(username) ;
		setLogin(true) ;
		return login;
	}


	public static void userDisConnect() { 
		System.out.println(userIdentification + " disconnected.");
		resetUsername();
		setLogin(false);
		PdfManager.clearInfo();
	}

	public static boolean userCreate(String name, String userPassword, String userPassConf) { 
		String username = name.trim();
		String password = userPassword.trim();
		String passConf = userPassConf.trim();
		setPDB() ;
		if(!passwordValidation(password, passConf)) {return false; }
		if(PdfDBManager.doesUserExist(username)) {return false; }
		pDB.createUser(username, password) ;
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
			pDB.connectDB();
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
		FileUtils.cleanDirectory(new File(PdfManager.getResDir() + File.separator + "temp"));
	}

	public static MyUser getUser(String name) {
		return(PdfDBManager.getUser(name)) ;
	}
}