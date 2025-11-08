package com.stas.user_service.dto;

import java.util.List;

public class UserSignupDto {
    private String name;
    private String email;
    private String password;
    private List<String> roles; // optional: ["USER"], ["ADMIN"]
    // getters/setters
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public List<String> getRoles() {
		return roles;
	}
	public void setRoles(List<String> roles) {
		this.roles = roles;
	}
}