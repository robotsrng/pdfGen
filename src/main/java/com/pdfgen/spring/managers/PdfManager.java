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

	public File inFile = null ;
	public static PDDocument pDoc;
	public static PDAcroForm pForm;
	public static ArrayList<String> theFields = new ArrayList<String>() ;  
	public static ArrayList<String> userSavedFiles = new ArrayList<String>() ; 
	private String resDir = "user_files" ;
	private String fileName ;
	private String tableName ;
	private PdfGenManager pCon = null ;

	//*******************************************
	// CONSTRUCTORS
	//*******************************************

	public PdfManager(PdfGenManager con) {
		this.pCon = con ;
		setResDir(con.prefs.get("RESDIR_PATH", resDir)) ;
	}


	//*******************************************
	// Helpers
	//*******************************************

	public void clearInfo() {
		inFile = null ;
		pForm = null ;
		theFields = new ArrayList<String>() ;
		fileName = "";
		tableName = "";
		userSavedFiles.clear();
	}

	private boolean checkConnection() { 
		if(pCon != null && pCon.checkConnection()) { 
			return true ;
		}else { return false; } 
	}

	private boolean existCheck(String username, String fileName) {
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

	public boolean processNewPdf(String username, File file) {
		setInFile(file);
		setFileName(file.getName()) ;
		setTableName() ;
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
	public boolean fillPdf(String username, String[] rawTextArray) {
		//CHECKS IS FILE IS THERE AND SETS FILE NAME
		if(!(retrievePdf(username, rawTextArray[0].trim()))){System.out.println("PMANFILL ERROR 0" );return false;}
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
		addDataToDB(username, data); 
		UpDownController.setDownload(pDoc, fileName) ; 
		return true;
	}

public boolean retrievePdf(String username, String fileName) {
	setFileName(fileName) ;
	setTableName();
	try {
		pDoc = PDDocument.load(new File(resDir + "/" + username + "/" + fileName)) ;
		pForm = pDoc.getDocumentCatalog().getAcroForm();
		theFields = (ArrayList<String>) getFields(pForm);
		return true ;
	}catch(FileNotFoundException fe) { 
		//JOptionPane.showMessageDialog(null, "Appropriate File Not Found, File Not In System.");
		return false ;
	}catch (IOException e) {
		e.printStackTrace();
		return false ;
	}
}

public String[] parseTextFile(File textFile) { 
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

public String[] parseTextFile(String textArea) { 
	System.out.println(textArea);
	return (textArea.split("\n")) ;
}

//*******************************************
// MYSQL DATABASE FUNCTIONS
//*******************************************

public void createTable(String username) {
	if(!checkConnection()) { return ;} 
	if(fileName == null || theFields == null) { System.out.println("No file for table creation.") ; return ; }
	pCon.createDBTable(username, tableName, theFields);
}

public void addDataToDB(String username, ArrayList<String> textArray) { 
	if(!checkConnection()) { return ;} 
	pCon.addDataToDB(username, tableName, textArray) ; 
}

//*******************************************
// Getters and Setters
//*******************************************

public void setFileName(String fileName) { 
	this.fileName = fileName ;
}

private void getSavedFiles(String username) {
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

public void setInFile(File inFile) {
	this.inFile = inFile;
}

private void setResDir(String resDir) {
	this.resDir = resDir ;
}

public String getResDir() {
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

private void setTableName() {
	tableName =  fileName.substring(0, (fileName.length() - 4)) ;
}

public void createUserDir(String username) {
	Path path = Paths.get(resDir + "/" + username);
	if (!Files.exists(path)) {
		new File(resDir + "/" + username).mkdir();
	}else {
	System.out.println("This user already has an existing profile");
	//	JOptionPane.showOptionDialog(null, "This user already has an existing profile.", "User Already Exists", 1, 3, null, null, path) ; 
		return ;
	}
}
}