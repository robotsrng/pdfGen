package com.pdfgen.spring.managers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
 
@Controller
@RequestMapping("/updown")
public class UpDownController {

	public static File inFile;
	public static PDDocument downFile;
	public static String downFileName;


	public static File uploadPdf(MultipartFile file) { 
		if(!FilenameUtils.getExtension(file.getOriginalFilename()).equals("pdf")) { 
			return null;
		}
		try {
			inFile = new File("user_files/temp/" + file.getOriginalFilename()) ;
			FileOutputStream fos = new FileOutputStream(inFile); 
			fos.write(file.getBytes());
			fos.close(); 
		}catch(Exception e) { 
			return null;
		}
		return inFile;
	}
	
	public static File uploadText(MultipartFile file) { 
		try {
			inFile = new File("user_files/temp/" + file.getOriginalFilename()) ;
			FileOutputStream fos = new FileOutputStream(inFile); 
			fos.write(file.getBytes());
			fos.close(); 
		}catch(Exception e) { 
			return null;
		}
		return inFile;
	}
	
	@GetMapping("/dl")
	@ResponseBody
	public static boolean downloadCompletePdf(HttpServletResponse response) { 
		try {
			File download = new File ("user_files/temp/" + downFileName ) ;
			downFile.save(download);
			FileInputStream is = new FileInputStream(download) ;
			response.setContentType("application/pdf");
			response.setHeader("Content-Disposition", "attachment; filename=\""+ downFileName +"\"");
			
			IOUtils.copy(is, response.getOutputStream()) ; 
		}catch(Exception e) { 
			System.out.println("ERROR DOWNLOAD PROCESS");
			return false;
		}
		return true;
	}

	public static void setDownload(PDDocument pDoc, String fileName) {
		downFile = pDoc ;
		downFileName = fileName;
	}
}
