package com.pdfgen.spring.managers;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;

import org.hibernate.validator.constraints.Length;
import org.springframework.data.annotation.Transient;

@Entity
@Table(name = "user")
public class MyUser {

	
	public MyUser() {
	}
	
	public MyUser(int userId,
			String name,
			String password,
			String passConf) {
		super();
		this.userId = userId;
		this.username = name;
		this.password = password;
		this.passConf = passConf;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "user_id")
	private int userId;

	@Column(name = "username")
	@NotEmpty(message="*Username cannot be empty")
	private String username;
	
	@Column(name = "password")
	@Length(min = 2, message = "Your password must have at least 2 characters")
	@NotEmpty(message = "*Please provide your password")
	@Transient
	private String password;

	@Length(min = 2, message = "")
	@NotEmpty(message = "*Please repeat your password")
	@Transient
	private String passConf;

	
	public int getId() {
		return userId;
	}

	public void setId(int id) {
		this.userId = id;
	}

	public String getName() {
		return username;
	}
	
	public void setName(String name) {
		this.username = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPassConf() {
		return passConf;
	}

	public void setPassConf(String passConf) {
		this.passConf = passConf;
	}
}