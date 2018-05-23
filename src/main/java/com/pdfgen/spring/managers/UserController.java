package com.pdfgen.spring.managers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/login")
public class UserController {




	@RequestMapping(value = {"/", "/login-form"})
	public static String showPage(Model model) {
		model.addAttribute("user", new User()) ;
		return "login" ;
	}

	@RequestMapping("/create-user-form")
	public static String showCreateUser(Model model) {
		model.addAttribute("user", new User()) ;
		return "create-user" ;
	}


	@PostMapping("/create-user")
	public String createUser(@ModelAttribute("user") User user, Model model) {
		Boolean flag = PdfGenManager.userCreate(user.getUsername(), user.getEmail(), user.getPassword(), user.getPassConf()) ;
		if(!flag) {return "error-page" ; }
		else { return "login" ; }
	} 

	@RequestMapping("/user-login")
	public String userLogin(@ModelAttribute("user") User user, Model model) {
		Boolean flag = PdfGenManager.userConnect(user.getUsername(), user.getPassword()) ;
		if(!flag) {return "error-page" ; }
		else { return "login" ; }
	}
}
