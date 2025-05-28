package com.developerChallenge.developer_Challenge_backend.DTOs;

public class AuthenticationResponse {

	private String token;
	private String role;

	public Object getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public AuthenticationResponse(String token,String role) {
		super();
		this.role =  role;
		this.token = token;
	}
	
	
}
