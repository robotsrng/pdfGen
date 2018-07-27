package com.pdfgen.spring.managers;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PDFHome implements ErrorController{

    private static final String PATH = "/error";
	
	@RequestMapping("/")
	public String showPage() { 
		 return "index" ;
	}
	
	@RequestMapping("/success")
	public String success(Model model, @RequestParam(value="code", required=false) String code) { 
		model.addAttribute("message", CodeRepo.getMessage(code));
		return "success" ;
	}

	@RequestMapping("/error")
	public String error(Model model, @RequestParam(value="code", required=false) String code) { 
		model.addAttribute("message", CodeRepo.getMessage(code));
		return "error" ;
	}

	@Override
	public String getErrorPath() {
		
		return null;
	}	
	
	@RequestMapping("generator-page")
	public String adminGeneratorPage(Model model, @ModelAttribute("user") String user) {
		model.addAttribute("user", user);
		return "generator-page" ;
	}
} 
 