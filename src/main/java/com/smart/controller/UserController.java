package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.OrderRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.MyOrder;
import com.smart.entities.User;
import com.smart.helper.Message;
import com.razorpay.*;

@Controller
@RequestMapping("/user")
public class UserController {
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ContactRepository contactRepository;
	
	@Autowired
	private OrderRepository orderRepository;
	
//	Method for adding common data to response
	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {
//		Get the user by userName(Email)
		String userName = principal.getName();
		
		User user = this.userRepository.findByEmail(userName);
		
		model.addAttribute("user", user);
	}
	
//	handler for home dashboard
	
	@GetMapping("/index")
	public String dashboard(Model model) {
		model.addAttribute("title", "User Dashboard");
		
		return "normal/user_dashboard";
	}
	
//	Open add contact form handler
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {
		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact", new Contact());
		
		return "normal/add_contact_form";
	}
	
//	Processing add contact form handler
	@PostMapping("/process-contact")
	public String processContact(@Valid @ModelAttribute("contact") Contact contact, BindingResult result, 
			@RequestParam("profileImage") MultipartFile file, Model model, HttpSession session, Principal principal) {
		try {
			if(result.hasErrors()) {
				System.out.println("Error" + result.toString());
				model.addAttribute("contact", contact);
			
				return "normal/add_contact_form";
			}
		
			String name = principal.getName();
			
			User user = this.userRepository.findByEmail(name);
			
			/* Processing and Uploading File... */
			
			if(file.isEmpty()) {
				/* If the file is empty then try our message */
				
				throw new Exception("Image file is compulsory");
			}
			
			else {
				/* Upload the file to folder and update the name to contact */
				
				contact.setImageUrl(file.getOriginalFilename());
				
				String UPLOAD_DIR = new ClassPathResource("static/image").getFile().getAbsolutePath();
				
				Files.copy(file.getInputStream(), Paths.get(UPLOAD_DIR+File.separator+file.getOriginalFilename()), StandardCopyOption.REPLACE_EXISTING);
				
				System.out.println("Image is Uploaded");
			}
			
			contact.setUser(user);
			
			user.getContact().add(contact);
			
			this.userRepository.save(user);
			
			model.addAttribute("contact", new Contact());
		
			session.setAttribute("message", new Message("Your contact is added successfully !! Add more..", "alert-success"));
		}
		
		catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("contact", contact);
			
			session.setAttribute("message", new Message("Something went wrong, " + e.getMessage() + ", Try again !!", "alert-danger"));
		}
		
		return "normal/add_contact_form";
	}
	
//	Show contacts handler
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page, Model model, Principal principal) {
		model.addAttribute("title", "Show User Contacts");
		
		String userName = principal.getName();
		
		User user = this.userRepository.findByEmail(userName);
		
		Pageable pageable = PageRequest.of(page,5);
		
		Page<Contact> contacts = this.contactRepository.findContactByUserId(user.getId(), pageable);
		
		model.addAttribute("contacts", contacts);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", contacts.getTotalPages());
		
		
		return "normal/show_contacts";
	}
	
//	Showing perticular contact details
	
	@GetMapping("/contact/{cId}")
	public String showContactDetails(@PathVariable("cId") Integer cId, Model model, Principal principal) {
		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		Contact contact = contactOptional.get();
		
		String userName = principal.getName();
		
		User user = this.userRepository.findByEmail(userName);
		
		if(user.getId() == contact.getUser().getId()) {
			model.addAttribute("contact", contact);
			model.addAttribute("title", contact.getName());
		}
		
		return "normal/contact_details";
	}
	
//	Delete Contact Handler
	@GetMapping("/delete/{cId}")
	public String deleteContact(@PathVariable("cId") Integer cId, Model model, Principal principal, HttpSession session) {
		try {
			Optional<Contact> optionalContact = this.contactRepository.findById(cId);
			Contact contact = optionalContact.get();
			
//			Deleting contact image
			File deleteFile = new ClassPathResource("static/image").getFile();
			File file1 = new File(deleteFile, contact.getImageUrl());
			file1.delete();
			
			String userName = principal.getName();
			User user = this.userRepository.findByEmail(userName);
			
			if(user.getId() == contact.getUser().getId()) {
				this.contactRepository.delete(contact);
			}
			
			session.setAttribute("message", new Message("Contact deleted successfully", "alert-success"));
		}
		
		catch(Exception e) {
			e.printStackTrace();
			
			session.setAttribute("message", new Message("Something went wrong" + e.getMessage(), "alert-danger"));
		}
		
		return "redirect:/user/show-contacts/0";
	}
	
//	Open Update Form Handler
	@PostMapping("/update-contact/{cId}")
	public String updateForm(@PathVariable("cId") Integer cId, Model m) {
		m.addAttribute("title", "Update Contact");
		
		Optional<Contact> optionalContact = this.contactRepository.findById(cId);
		Contact contact = optionalContact.get();
		
		m.addAttribute("contact", contact);
		
		return "normal/update_form";
	}
	
//	Update Contact Handler
	@PostMapping("/process-update")
	public String updateContact(@ModelAttribute("contact") Contact contact,@RequestParam("profileImage") MultipartFile file, Model model, HttpSession session, Principal principal) {
		try {
//			Old contact details
			Optional<Contact> optionalContact = this.contactRepository.findById(contact.getcId());
			Contact oldContact = optionalContact.get();
			
			if(!file.isEmpty()) {
//				Delete the old photo
				
				File deleteFile = new ClassPathResource("static/image").getFile();
				File file1 = new File(deleteFile, oldContact.getImageUrl());
				file1.delete();
				 
//				Update new photo
				contact.setImageUrl(file.getOriginalFilename());
				
				String UPLOAD_DIR = new ClassPathResource("static/image").getFile().getAbsolutePath();
				
				Files.copy(file.getInputStream(), Paths.get(UPLOAD_DIR+File.separator+file.getOriginalFilename()), StandardCopyOption.REPLACE_EXISTING);
			}
			
			else {
				contact.setImageUrl(oldContact.getImageUrl());
			}
			
			String userName = principal.getName();
			User user = this.userRepository.findByEmail(userName);
			
			contact.setUser(user);
			
			this.contactRepository.save(contact);
			
			session.setAttribute("message", new Message("Contact updated successfully", "alert-success"));
		}
		
		catch(Exception e) {
			e.printStackTrace();
			
			session.setAttribute("message", new Message("Something went wrong" + e.getMessage(), "alert-danger"));
		}
		
		return "redirect:/user/contact/"+contact.getcId();
	}
	
//	Your profile handler
	@GetMapping("/profile")
	public String showProfile(Model model) {
		model.addAttribute("title", "Your Profile");
		
		return "normal/profile";
	}
	
//	Open Settings Handler
	@GetMapping("/settings")
	public String openSettings(Model m) {
		m.addAttribute("title", "Your Settings");
		return "normal/settings";
	}
	
	
//	Change password handler
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword") String oldPassword, @RequestParam("newPassword") String newPassword, Principal principal, HttpSession session) {
		System.out.println("OLD PASSWORD" + oldPassword);
		System.out.println("NEW PASSWORD" + newPassword);
		
		String username = principal.getName();
		User user = userRepository.findByEmail(username);
		
		if(this.bCryptPasswordEncoder.matches(oldPassword, user.getPassword())) {
//			Change the password
			user.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
			this.userRepository.save(user);
			
			session.setAttribute("message", new Message("Your password is successfully changed", "alert-success"));
		}
		
		else {
//			Error...
			session.setAttribute("message", new Message("Please enter correct old password !!", "alert-danger"));
			return "redirect:/user/settings";
		}
		
		return "redirect:/user/index";
	}
	
//	Creating order for payment
	@PostMapping("/create_order")
	@ResponseBody
	public String createOrder(@RequestBody Map<String, Object> data, Principal principal) throws RazorpayException {
		int amt = Integer.parseInt(data.get("amount").toString());
		
		RazorpayClient razorpayClient = new RazorpayClient("rzp_test_kMQGApV6nacP6c", "tSsb48LagCnxlhIZtgiDQnq8");
		
		JSONObject object = new JSONObject();
		object.put("amount", amt*100);
		object.put("currency", "INR");
		object.put("receipt", "txn_235425");
		
//		Creating new Order
		
		Order order = razorpayClient.orders.create(object);
		
		System.out.println(order);
		
//		If you want then you can save this to your database
		MyOrder myOrder = new MyOrder();
		
		myOrder.setAmount(order.get("amount")+"");
		myOrder.setOrderId(order.get("id"));
		myOrder.setPaymentId(null);
		myOrder.setStatus("created");
		
		String email = principal.getName();
		User user = this.userRepository.findByEmail(email);
		
		myOrder.setUser(user);
		myOrder.setReceipt(order.get("receipt"));
		
		this.orderRepository.save(myOrder);
		
		return order.toString();
	}
	
	@PostMapping("/update_order")
	public ResponseEntity<?> updateOrder(@RequestBody Map<String, Object> data){
		MyOrder myOrder = this.orderRepository.findByOrderId(data.get("order_id").toString());
		
		myOrder.setPaymentId(data.get("payment_id").toString());
		myOrder.setStatus(data.get("status").toString());
		
		this.orderRepository.save(myOrder);
		
		return ResponseEntity.ok(Map.of("msg","updated"));
	}
}
