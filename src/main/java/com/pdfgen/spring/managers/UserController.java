package com.pdfgen.spring.managers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("login")
public class UserController {


	@RequestMapping(value = {"/login-page"})
	public static void showPage(Model model, @RequestParam(value = "code", required = false) String code) {
		model.addAttribute("message", CodeRepo.getMessage(code));
		model.addAttribute("myUser", new MyUser()) ; 
		if(	SecurityContextHolder.getContext().getAuthentication() != null &&
				SecurityContextHolder.getContext().getAuthentication().isAuthenticated() &&
				!(SecurityContextHolder.getContext().getAuthentication() 
						instanceof AnonymousAuthenticationToken) ) { 
		}
	}

	@RequestMapping("/logout")
	public void logout(HttpServletRequest request, HttpServletResponse response){
		Boolean authFlag = false ;
		if(	SecurityContextHolder.getContext().getAuthentication() != null &&
				SecurityContextHolder.getContext().getAuthentication().isAuthenticated() &&
				!(SecurityContextHolder.getContext().getAuthentication() 
						instanceof AnonymousAuthenticationToken) ) { 
			authFlag = true ;
		}	
		if(authFlag) {
			new SecurityContextLogoutHandler().logout(request, response, SecurityContextHolder.getContext().getAuthentication());
		}else { 
		}
	}

	@GetMapping("/create-user") 
	public String showCreateUser(Model model, @Valid @ModelAttribute("myUser") MyUser myUser, BindingResult br) {
		model.addAttribute("myUser", new MyUser()) ;
		return "login/create-user";
	}

	@PostMapping("/create-user")
	public String createUser(Model model, @Valid @ModelAttribute("myUser") MyUser myUser, BindingResult br) {
		boolean errorFlag = false ; 
		if(!myUser.getPassword().equals(myUser.getPassConf())){ 
			errorFlag = true ;
			model.addAttribute("message0", CodeRepo.getMessage("bad-password-match"));
		}
		if (PdfDBManager.doesUserExist(myUser.getName())) {
			errorFlag = true ;
			model.addAttribute("message", CodeRepo.getMessage("user-exists"));
		}
		if (br.hasErrors()) {
			errorFlag = true ;
			int count = 1 ;
			for (Object object : br.getAllErrors()) {
				FieldError fieldError = (FieldError) object;
				model.addAttribute("message" + String.valueOf(count), fieldError.getDefaultMessage());
			}
		}	
		if(errorFlag) {
			model.addAttribute("message", CodeRepo.getMessage("bad-creation-info"));
			return "login/create-user" ;
		}
		PdfGenManager.userCreate(myUser.getName(), myUser.getPassword(), myUser.getPassConf()) ;
		MyUserDetailsService.getAllUserDetails();
		model.addAttribute("message", CodeRepo.getMessage("good-creation-info"));
		return "login/login-page" ; 		
	}

	public static MyUser findUserByName(String name) {
		return ( PdfGenManager.getUser(name) ) ; 
	}
}
