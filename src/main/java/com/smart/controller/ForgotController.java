package com.smart.controller;

import java.util.Random;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;
import com.smart.services.EmailService;

@Controller
public class ForgotController {	
	Random random = new Random(1000);
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	  
	@GetMapping("/forgot")
	public String openEmailForm(Model model) {
		model.addAttribute("title", "Email Verification");
		return "forgot_email_form";
	}
	
	@PostMapping("/send-otp")
	public String send_otp(@RequestParam("email") String email, HttpSession session,Model model) {
		model.addAttribute("title", "Otp Verification");
		try {
			int otp = random.nextInt(999999);
			
//			Write code for send otp	to email...
			
			String subject = "OTP From SCM";
			String message = "<div style='border:1px solid #e2e2e2; padding:20px'>"
							+ "<h1> OTP is = " + "<b>" + otp + "</n>" + "</h1>" + "</div>";
			String to = email;
			
			boolean flag = this.emailService.sendEmail(subject,message,to);
			
			if(flag) {
				session.setAttribute("myOtp", otp);
				session.setAttribute("email", email);
				return "verify-otp";
			}
			
			else {
				session.setAttribute("message", new Message("Check your email id !!" , "alert-danger"));
				
				return "forgot_email_form";
			}
		}
		
		catch(Exception e) {
			e.printStackTrace();
			
			session.setAttribute("message", new Message("Something went wrong !!" + e.getMessage(), "alert-danger"));
			return "forgot_email_form";
		}
	}
	
//	Verify OTP
	@PostMapping("/verify-otp")
	public String verifyOtp(@RequestParam("otp") int otp,HttpSession session,Model model) {
		model.addAttribute("title", "Change Password Form");
		int myOtp = (int) session.getAttribute("myOtp");
		String email = (String) session.getAttribute("email");
		
		if(myOtp==otp) {
//			Password change form
			User user = this.userRepository.findByEmail(email);
			
			if(user==null) {
//				Send error message
				session.setAttribute("message", new Message("User does not exist with this email id !!" , "alert-danger"));
				
				return "forgot_email_form";
			}
			
			else {
//				Send change password form
				
				return "password_change_form";
			}
		}
		
		else {
			session.setAttribute("message", new Message("You have entered wrong otp","alert-danger"));
			return "verify-otp";
		}
	}
	
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("newPassword") String newPassword, HttpSession session) {
		String email = (String) session.getAttribute("email");
		User user = this.userRepository.findByEmail(email);
		
		user.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
		
		this.userRepository.save(user);
		
		return "redirect:/signin?change=password changed successfully...";
	}
}
