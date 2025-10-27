package com.example.strio01.user.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
//@AllArgsConstructor
@Builder
public class AuthInfo {
    private String userId;
    private String passwd;
    private String userName;
    private String roleCd;
    private String email;
    
    
    public AuthInfo(String userId, String passwd, String userName) {
    	this.userId = userId;
    	this.passwd = passwd;
    	this.userName = userName;
    }
   /* 
    public AuthInfo(String userId, String passwd, String userName, String roleCd) {
    	this.userId = userId;
    	this.passwd = passwd;
    	this.userName = userName;
    	this.roleCd = roleCd;
    }*/
        
    public AuthInfo(String userId, String passwd, String userName, String roleCd, String email) {
    	this.userId = userId;
    	this.passwd = passwd;
    	this.userName = userName;
    	this.roleCd = roleCd;
    	this.email = email;
    }   
    
    public AuthInfo(String userId, String userName, String roleCd, String email) {
    	this.userId = userId;
    	this.userName = userName;
    	this.roleCd = roleCd;
    	this.email = email;
    }     
    
}
