package com.smart.controller;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;

@Controller
public class HomeController {

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
	private UserRepository userRepository;
	
//	Home handler
	@GetMapping({"/home","/"})
	public String home(Model m) {
		m.addAttribute("title", "Home - Smart Contact Manager");
		
		return "home";
	}
	
//	About handler
	@GetMapping("/about")
	public String about(Model m) {
		m.addAttribute("title", "About - Smart Contact Manager");
		
		return "about";
	}
	
//	Signup handler
	@GetMapping("/signup")
	public String signup(Model m) {
		m.addAttribute("title", "Register - Smart Contact Manager");
		m.addAttribute("user", new User());
		
		return "signup";
	}
	
//	handler for registring user
	@PostMapping("/do_register")
	public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult result,
			@RequestParam(value = "agreement", defaultValue = "false") boolean agreement, 
			Model m, HttpSession session) {
		try {
			if(!agreement) {
				throw new Exception("You have not agreed the terms and conditions");
			}
			
			if(result.hasErrors()) {
				System.out.println("ERROR" + result.toString());
				m.addAttribute("user", user);
				
				return "signup";
			}
		
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			
			System.out.println("Agreement"+agreement);
			System.out.println("User"+user);
		
			this.userRepository.save(user);
		
			m.addAttribute("user", new User());
			
			session.setAttribute("message", new Message("Successfully Registered !!", "alert-success"));
			
			return "signup";
		}
		
		catch(Exception e) {
			e.printStackTrace();
			m.addAttribute("user", user);
			
			session.setAttribute("message", new Message("Something went wrong !!" + e.getMessage(), "alert-danger"));
			
			return "signup";
		}
	}
	
//	Handler for custom login
	@GetMapping("/signin")
	public String customLogin(Model m) {
		m.addAttribute("title", "Login - Smart Contact Manager");
		
		return "login";
	}
}
