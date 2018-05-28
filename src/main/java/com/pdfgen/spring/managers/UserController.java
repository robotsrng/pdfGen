package com.pdfgen.spring.managers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.session.HttpSessionDestroyedEvent;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("login")
public class UserController {

	@GetMapping(value = {"/login-page"})
	public static String showPage(Model model) {
		model.addAttribute("user", new MyUser()) ;
		return "login/login-page" ;
	}

	@RequestMapping("/create-user-form")
	public static String showCreateUser(Model model) {
		model.addAttribute("user", new MyUser()) ;
		return "login/create-user" ;
	}


	@PostMapping("/create-user")
	public String createUser(@ModelAttribute("user") MyUser user, Model model) {
		if (PdfDBManager.doesUserExist(user.getName())) {
			return "error/error-page" ;
		}
		//TODO ADD INFO VALIDATION HERE
		Boolean flag = PdfGenManager.userCreate(user.getName(), user.getEmail(), user.getPassword(), user.getPassConf()) ;
		MyUserDetailsService.getAllUserDetails();
		return "login/login-page" ; 		
	}

	@PostMapping("/login-page")
	public void userLogin(@ModelAttribute("user") MyUser user, Model model, HttpServletRequest req) {
		;
	}
	
	@EventListener
	public void userLoginLogic(AuthenticationSuccessEvent event) {
		Authentication auth = event.getAuthentication() ;
		System.out.println("YEAH");
		String username = auth.getName() ;
		System.out.println(username);
		PdfGenManager.userConnect(username);
		System.out.println("YEAH Again...") ;
	}
	
	@EventListener
	public void userLoginLogic( HttpSessionDestroyedEvent event) {
			
		System.out.println("OH NO");
		PdfGenManager.userDisConnect();
		
	}

	@RequestMapping("/login-error")
	public String loginError(){
		return "error/login-error";
	}

	@RequestMapping("/login-success")
	public String loginSuccess(){
		return "login/login-success" ;
	}

	@RequestMapping("/logout-success")
	public String logoutSuccess(){
		return "login/logout-success" ;
	}

	@GetMapping("/logout")
	public String logout(HttpServletRequest request, HttpServletResponse response){
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null){    
			new SecurityContextLogoutHandler().logout(request, response, auth);
		}
		return "redirect:/login/logout-success";
	}

	public static MyUser findUserByName(String name) {
		return ( PdfGenManager.getUser(name) ) ; 
	}
}
