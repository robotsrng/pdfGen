package com.pdfgen.spring.managers;

import java.util.ArrayList;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("admin")
public class AdminController {


	@GetMapping(value = {"/admin-home"} )
	public String home(){
		return "admin/admin-home";
	}

	@RequestMapping("/search-users")
	public String searchMyUsers(Model model, @ModelAttribute("search") String search, @RequestParam(value = "code", required = false) String code) {
		model.addAttribute("myUsers", PdfDBManager.findUser(search)) ;
		model.addAttribute("message", CodeRepo.getMessage(code)) ;
		return "user-list";
	}

	@RequestMapping("/download-user-file")
	private String downloadUserFile(Model model, @ModelAttribute("user") String user, @ModelAttribute("filename") String filename) {
		UpDownController.setDownload(PdfManager.retrievePdf(user, filename), filename + ".pdf");
		return "download-page" ;
	}

	@RequestMapping("/view-file-database")
	private String viewFileDatabase(Model model, @ModelAttribute("user") String user, @ModelAttribute("filename") String filename) {
		model.addAttribute("user", user);
		model.addAttribute("tableHeaders", PdfDBManager.getTableHeadersFromDB(user, filename)) ;
		model.addAttribute("tableData", PdfDBManager.getDataFromTable(user, filename));
		return "record-list" ;
	}

	@RequestMapping("/delete-file")
	private String deleteFile(Model model, @ModelAttribute("user") String user, @ModelAttribute("filename") String filename) {
		PdfManager.deleteUserFile(user, filename);
		PdfDBManager.deleteUserTable(user, filename);
		model.addAttribute("user", user ) ;
		model.addAttribute("userFiles", PdfDBManager.findUserFiles(user) ) ;
		return "admin/user-saved-files" ;
	}

	@RequestMapping("/delete-record")
	private String deleteRecord(Model model, @ModelAttribute("user") String user, @ModelAttribute("filename") String filename, @ModelAttribute("record") ArrayList<String> record) {
		PdfDBManager.deleteUserRecord(user, filename, record);
		model.addAttribute("user", user ) ;
		model.addAttribute("userFiles", PdfDBManager.findUserFiles(user) ) ;
		return "admin/user-saved-files" ;
	}

	@RequestMapping("admin-generator-page")
	public String adminGeneratorPage(Model model, @ModelAttribute("user") String user) {
		model.addAttribute("user", user);
		return "generator-page" ;
	}

	@RequestMapping("admin-change-privilege")
	public String changeUserPrivilege(Model model, @ModelAttribute("user") String user, @ModelAttribute("role") String role,  @ModelAttribute("oldPass") String oldPass, @ModelAttribute("newPass") String newPass, @ModelAttribute("passConf") String passConf ) {
		PdfDBManager.setUserAuthority(user, role);
		model.addAttribute("userPriv", PdfDBManager.getUserAuthority(user));
		return "admin/user-privileges" ;
	}
	
	@RequestMapping("admin-change-password")
	public String changeUserPassword(Model model, @ModelAttribute("user") String user, @ModelAttribute("role") String role, @ModelAttribute("oldPass") String oldPass, @ModelAttribute("newPass") String newPass, @ModelAttribute("passConf") String passConf  ) {
		model.addAttribute("oldPass", oldPass) ; 
		model.addAttribute("newPass", newPass) ;
		model.addAttribute("passConf", passConf);
		if (!oldPass.equals(newPass) && newPass.equals(passConf)) { 
			PdfDBManager.changePassword(user, oldPass, newPass);
			model.addAttribute("message", CodeRepo.getMessage("password-change-success"));
			return "admin/user-privileges" ;
		}else { 
			model.addAttribute("message", CodeRepo.getMessage("bad-login")) ;
			return "admin/user-privileges" ;
		}
	}
 
	@RequestMapping("admin-delete-user")
	public String deleteUser(Model model, @ModelAttribute("user") String user) {
		model.addAttribute("user", user) ;
		return "admin/delete-user-confirmation" ;
	}

	@RequestMapping("delete-user-confirmation")
	public String deleteUserConfirmation(Model model, @ModelAttribute("user") String user, @ModelAttribute("userConf") String userConf) {
		model.addAttribute("userConf", userConf) ;
		return "admin/delete-user-confirmation" ;
	} 

	@RequestMapping("delete-user-final") 
	public String deleteUserFinal(Model model,  @ModelAttribute("user") String user, @ModelAttribute("userConf") String userConf  ) {
		if(user.equals(userConf)){ 
			PdfDBManager.deleteUser(user) ;
			PdfManager.deleteUserDir(user);
			model.addAttribute("myUsers", PdfDBManager.findUser("")) ;
			model.addAttribute("message", CodeRepo.getMessage("user-delete-conf"));
			return "user-list" ; 
		}
		model.addAttribute("myUsers", PdfDBManager.findUser("")) ;
		model.addAttribute("message", CodeRepo.getMessage("user-delete-name-bad-match"));
		return "user-list" ;
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

}
