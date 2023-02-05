package com.smart.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "contact")
public class Contact {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "cid")
	private int cId;
	
	@NotBlank(message = "Name field is required")
	@Size(min = 2, max = 20, message = "Min 2 and max 20 characters are allowed")
	private String name;
	
	@NotBlank(message = "Nickname field is required")
	@Size(min = 2, max = 20, message = "Min 2 and max 20 characters are allowed")
	private String nickName;
	
	@Email(regexp = "^[a-zA-Z0-9+_.-]+@[a-zA-Z0-9.-]+$")
	@Column(unique = true)
	private String email;
	
	@Column(length = 5000)
	@NotBlank(message = "Description can not be empty !!")
	private String description;
	
	@NotBlank(message = "Work field is required")
	@Size(min = 4, max = 30, message = "Min 4 and max 30 character are allowed")
	private String work;
	
	@NotBlank(message = "Phone field is required")
	@Size(min = 10, max = 13, message = "Min 10 and max 13 numbers are allowed")
	private String phone;
	
	private String imageUrl;
	
	@ManyToOne
	@JsonIgnore
	private User user;
	
	public Contact() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Contact(int cId, String name, String nickName, String email, String description, String work, String phone,
			String imageUrl, User user) {
		super();
		this.cId = cId;
		this.name = name;
		this.nickName = nickName;
		this.email = email;
		this.description = description;
		this.work = work;
		this.phone = phone;
		this.imageUrl = imageUrl;
		this.user = user;
	}

	public int getcId() {
		return cId;
	}

	public void setcId(int cId) {
		this.cId = cId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getWork() {
		return work;
	}

	public void setWork(String work) {
		this.work = work;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	@Override
	public String toString() {
		return "Contact [cId=" + cId + ", name=" + name + ", nickName=" + nickName + ", email=" + email
				+ ", description=" + description + ", work=" + work + ", phone=" + phone + ", imageUrl=" + imageUrl
				+ ", user=" + user + "]";
	}
	
}
