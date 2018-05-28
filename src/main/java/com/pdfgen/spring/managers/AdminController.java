package com.pdfgen.spring.managers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
 
@Controller
@RequestMapping("admin")
public class AdminController {

	
	@RequestMapping("/admin-home")
	public String home(){

		return "admin/admin-home";
	}
}
