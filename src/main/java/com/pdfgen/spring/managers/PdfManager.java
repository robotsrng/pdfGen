package com.pdfgen.spring.managers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

//import javax.swing.JFileChooser;
//import javax.swing.JOptionPane;
//import javax.swing.filechooser.FileSystemView;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDTerminalField;
import org.springframework.stereotype.Controller;

@Controller
public class PdfManager {

	public static File inFile = null ;
	public static PDDocument pDoc;
	public static PDAcroForm pForm;
	public static ArrayList<String> theFields = new ArrayList<String>() ;  
	public static ArrayList<String> userSavedFiles = new ArrayList<String>() ; 
	private static String resDir = "/home/srng/Documents/user_files" ;
	private static String fileName ;
	private static String tableName ;
	private static PdfGenManager pCon = null ;

	//*******************************************
	// CONSTRUCTORS
	//*******************************************

	public PdfManager(PdfGenManager con) {
		createInitDir();
		this.pCon = con ;
		setResDir(con.prefs.get("RESDIR_PATH", resDir)) ;
	}


	//*******************************************
	// Helpers
	//*******************************************

	public static void clearInfo() {
		inFile = null ;
		pForm = null ;
		theFields = new ArrayList<String>() ;
		fileName = "";
		tableName = "";
		userSavedFiles.clear();
	}

	private static boolean checkConnection() { 
		if(pCon != null && pCon.checkConnection()) { 
			return true ;
		}else { return false; } 
	}

	private static boolean existCheck(String username, String fileName) {
		getSavedFiles(username);
		for(String key : userSavedFiles) { 
			if(fileName.equals(key)) { 
				//if(JOptionPane.showOptionDialog(null, "This file exists already, would you like to overwrite it?", "File Exists", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null) == 1) {
				return true ;
				//}
			}
		}
		return false ;
	}

	//*******************************************
	// PDF HANDLERS
	//*******************************************

	public static boolean processNewPdf(String username, File file) {
		setInFile(file);
		setFileName(file.getName()) ;
		System.out.println(fileName);
		setTableName() ;
		createUserDir(username) ;
		try {
			pDoc = PDDocument.load(file) ;
			pForm = pDoc.getDocumentCatalog().getAcroForm();
			if ( existCheck(username, fileName)) { System.out.println("File already exists"); return false ; } 
			theFields = (ArrayList<String>) getFields(pForm);
			pDoc.save(resDir + "/" + username + "/" + fileName) ;
			createTable(username);
			//JOptionPane.showMessageDialog(null, "PDF file added.") ;
			System.out.println("PDF Uploaded");
			return true;
		}catch (IOException e) {
			System.out.println("BAD FILE OR EXTENSION") ;
			e.printStackTrace();
			//JOptionPane.showMessageDialog(null, "BAD FILE OR EXTENSION") ;
			clearInfo();
			return false;
		}finally { 
			try {
				pDoc.close();
				clearInfo() ;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	//TODO Clean up the pdf parsing. Like the checkbox options etc.
	public static boolean fillPdf(String username, String[] rawTextArray, boolean reprint ) {
		//CHECKS IS FILE IS THERE AND SETS FILE NAME
		setFileName(rawTextArray[0].trim()) ;
		setTableName();
		if((retrievePdf(username, fileName ) == null)){System.out.println("PMANFILL ERROR 0" );return false;}
		ArrayList<String> data = new ArrayList<String>() ;
		for (String line : rawTextArray) { 
			String[] temp = line.split(",", -1) ;
			if(temp.length < 2) { continue ; } 
			try {
				pForm.getField(temp[0]).setValue(temp[1]);
				data.add(line);
			}catch(IllegalArgumentException ie) { 
				try {
					pForm.getField(temp[0]).setValue("Off") ; // should probably change this. it handles only check boxes but badly
				} catch (IOException e) {;}
			}catch(NullPointerException ne) { 
				//		JOptionPane.showMessageDialog(null, ("Your input has a field name not found in the PDF\nIt will be skipped.\nFIELD NAME > " + temp[0]));
				continue;
			} catch (IOException e) {
				System.out.println("PMANFILL ERROR");
				return false;
				//		JOptionPane.showMessageDialog(null, "There is an issue with the fields in the email, please check input.") ;
			}
		}
		//TODO	This is where we return download page ;
		/*JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory()) ;
		int returnValue = jfc.showSaveDialog(null);
		if (returnValue == JFileChooser.APPROVE_OPTION) {
			try {
				pDoc.save(jfc.getSelectedFile());
			} catch (IOException e) {
				e.printStackTrace();
			}*/
		if(!reprint) {
			addDataToDB(username, data);
		}
		UpDownController.setDownload(pDoc, fileName) ; 
		return true;
	}

	public static PDDocument retrievePdf(String username, String fileName) {
		try {
			System.out.println(username);
			pDoc = PDDocument.load(new File(resDir + "/" + username + "/" + fileName)) ;
			pForm = pDoc.getDocumentCatalog().getAcroForm();
			theFields = (ArrayList<String>) getFields(pForm);
			return pDoc ;
		}catch(FileNotFoundException fe) { 
			fe.printStackTrace();
			System.out.println("File Not Found");
			return null ;
		}catch (IOException e) {
			e.printStackTrace();
			return null ;
		}
	}

	public static String[] parseTextFile(File textFile) { 
		FileReader in = null ;
		BufferedReader br = null ;
		try {
			in = new FileReader(textFile) ;
			br = new BufferedReader(in) ;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		String email = "" ;
		String line ;
		try {
			while ( (line = br.readLine() ) != null ) { 
				email += (line) + " / " ; 
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return (email.split(" / ", -1)) ;
	}

	public static String[] parseTextFile(String textArea) { 
		return (textArea.split("\n")) ;
	}

	//*******************************************
	// MYSQL DATABASE FUNCTIONS
	//*******************************************

	public static void createTable(String username) {
		if(!checkConnection()) { return ;} 
		if(fileName == null || theFields == null) { System.out.println("No file for table creation.") ; return ; }
		pCon.createDBTable(username, tableName, theFields);
	}

	public static void addDataToDB(String username, ArrayList<String> textArray) { 
		if(!checkConnection()) { return ;} 
		pCon.addDataToDB(username, tableName, textArray) ; 
	}

	//*******************************************
	// Getters and Setters
	//*******************************************

	public static void setFileName(String file) { 
		fileName = "" ;
		if(!file.substring(file.length() - 4).startsWith(".p")) { 
			fileName +=  file + ".pdf" ;
		}else {
			fileName = file;
		}
	}

	private static void getSavedFiles(String username) {
		File dir = new File(resDir + "/" + username);
		File[] directoryListing = dir.listFiles();
		if (directoryListing != null) {
			for (File file : directoryListing) {
				userSavedFiles.add(file.getName());
			}
		} else {
			System.out.println("CANNOT FIND RESOURCES");
		}
		System.out.println(username + " File List Retrieved") ;
	}

	public static List<String> getFields(PDAcroForm pForm) { 
		if( pForm == null ) {
			System.out.println("NO FIELDS") ; 
			return  Collections.emptyList();
		}
		return StreamSupport.stream(pForm.getFieldTree().spliterator(), false)
				.filter(field -> (field instanceof PDTerminalField))
				.map(field -> field.getFullyQualifiedName())
				.collect(Collectors.toList());
	}

	public File getInFile() {
		return inFile;
	}

	public static void setInFile(File file) {
		inFile = file;
	}

	private void setResDir(String res) {
		resDir = res ;
	}

	public static String getResDir() {
		return resDir;
	}

	public void setTopLevelDir(String username) {
		//JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
		//jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		//int returnValue = jfc.showOpenDialog(null);
		//if (returnValue == JFileChooser.APPROVE_OPTION) {
		//	setResDir(jfc.getSelectedFile().getAbsolutePath());
		//	pCon.prefs.put("RESDIR_PATH", jfc.getSelectedFile().getAbsolutePath());
		//}
		//createUserDir(username);
	}

	public String getFileName() {
		return fileName;
	}

	private static void setTableName() {
		if(fileName.substring((fileName.length() - 4)).startsWith(".p")) { 
			tableName =  fileName.substring(0, (fileName.length() - 4)) ;
		}else {
			tableName = fileName;
		}
	}

	public static void createUserDir(String username) {
		Path path = Paths.get(resDir + "/" + username);
		if (!Files.exists(path)) {
			new File(resDir + File.separator + username).mkdir();
		}else { 
			System.out.println("This user already has an existing profile");
			//	JOptionPane.showOptionDialog(null, "This user already has an existing profile.", "User Already Exists", 1, 3, null, null, path) ; 
			return ;
		}
	}

	public void createInitDir() {
		File folder = new File(resDir);
		if (folder.exists() && folder.isDirectory()) {
		} else {
			folder.mkdirs();
		}
		folder = new File(resDir + File.separator + "temp");
		if (folder.exists() && folder.isDirectory()) {
		} else {
			folder.mkdirs();
		}
	}

	public static void deleteUserDir(String user) {
		Path path = Paths.get(resDir + "/" + user);
		try {
			Files.delete(path);
		} catch (IOException e) {
			System.out.println("User Directory already deleted");		
		}
	}

	public static void deleteUserFile(String user, String filename) {
		Path path = Paths.get(resDir + "/" + user + "/" + filename + ".pdf");
		try {
			Files.delete(path);
		} catch (Exception e) {
			System.out.println("User already deleted");		
		}
	}

}