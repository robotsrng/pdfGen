package com.pdfgen.spring.managers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class PDFHome {

	@RequestMapping("/")
	public String showPage() { 
		 return "main-menu" ;
	}
} 
 