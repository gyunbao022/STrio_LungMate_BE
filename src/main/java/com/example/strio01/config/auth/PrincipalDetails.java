package com.example.strio01.config.auth;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.strio01.user.dto.AuthInfo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PrincipalDetails implements UserDetails{	
	private static final long serialVersionUID = 1L;
	
	private AuthInfo authInfo;
	
	public PrincipalDetails() {
	
	}
	
	public PrincipalDetails(AuthInfo authInfo) {
		this.authInfo = authInfo;		
	}
	
	public AuthInfo getAuthInfo() {
		return authInfo;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
<<<<<<< Updated upstream
	    Collection<GrantedAuthority> collect = new ArrayList<>();
	    // 기본 USER
	    collect.add(() -> "ROLE_USER");
	    // 🔴 DB의 역할 코드 반영 (예: "ADMIN" 이면 ROLE_ADMIN 부여)
	    if (authInfo != null && authInfo.getRoleCd() != null) {
	        String role = authInfo.getRoleCd().trim().toUpperCase();
	        if (role.equals("ADMIN") || role.equals("ROLE_ADMIN")) {
	            collect.add(() -> "ROLE_ADMIN");
	        }
	    }
	    return collect;
=======
		Collection<GrantedAuthority> collect = new ArrayList<GrantedAuthority>();
		//기본 권한 추가 (USER)
		//collect.add(() -> "ROLE_USER");
		
		//추가 권한(ADMIN일 경우만)  //// old 
//		if(authInfo.getAuthRole().toString().equals("ADMIN")) {
//			collect.add(() -> "ROLE_ADMIN");
//		}
		
	    if (authInfo == null || authInfo.getRoleCd() == null) {
	        collect.add(() -> "ROLE_USER");
	        return collect;
	    }

	    // DB 값 예시: roleCd = "A", "D", "X"
	    String roleCd = authInfo.getRoleCd().trim().toUpperCase();
	    System.out.println("======================================:::"); 	
	    System.out.println("======================== roleCd:::"+roleCd);
	    switch (roleCd) {
	        case "A":
	            collect.add(() -> "ROLE_ADMIN");
	            break;
	        case "D":
	            collect.add(() -> "ROLE_DOCTOR");
	            break;
	        case "X":
	            collect.add(() -> "ROLE_XRAY_OPERATOR");
	            break;
	        default:
	            collect.add(() -> "ROLE_USER");
	            break;
	    }		

		return collect;
>>>>>>> Stashed changes
	}

	

	@Override
	public String getPassword() {		
		//return authInfo.getMemberPass();
		return authInfo.getPasswd();
	}

	@Override
	public String getUsername() {
		log.info("PrincipalDetails -> getUsername()-> adminId:{}",  authInfo.getUserId());
		//return authInfo.getMemberEmail();
		return authInfo.getUserId();
	}
	
	// 계정의 만료 여부를 리턴합니다.
	// true: 계정이 만료되지 않음 (정상 사용 가능 상태)
	// false: 계정이 만료됨 (로그인 불가)
	// 일반적으로 계정 사용 기간 제한이 있는 경우 사용되며, 기간이 지난 계정은 인증 실패 처리됩니다.
	@Override
	public boolean isAccountNonExpired() {
	    return true; // 이 예제에서는 계정이 항상 만료되지 않은 것으로 처리합니다.
	}

	// 계정의 잠김 여부를 리턴합니다.
	// true: 계정이 잠기지 않음 (정상 사용 가능 상태)
	// false: 계정이 잠겨 있음 (로그인 불가)
	// 보통 여러 번 로그인 실패 시 계정을 잠그는 기능에 사용됩니다.
	@Override
	public boolean isAccountNonLocked() {
	    return true; // 이 예제에서는 계정이 항상 잠기지 않은 것으로 처리합니다.
	}

	// 사용자의 자격 증명(비밀번호)의 만료 여부를 리턴합니다.
	// true: 자격 증명(비밀번호)이 만료되지 않음
	// false: 자격 증명이 만료됨 (로그인 불가)
	// 보안 정책상 비밀번호를 일정 기간마다 변경해야 할 경우 유효합니다.
	@Override
	public boolean isCredentialsNonExpired() {
	    return true; // 이 예제에서는 비밀번호가 항상 유효한 것으로 처리합니다.
	}

	// 계정의 활성화 여부를 리턴합니다.
	// true: 계정이 활성화됨 (정상 사용 가능 상태)
	// false: 계정이 비활성화됨 (로그인 불가)
	// 일반적으로 이메일 인증이 완료되지 않았거나 관리자가 비활성화한 계정에 대해 사용됩니다.
	@Override
	public boolean isEnabled() {
	    return true; // 이 예제에서는 계정이 항상 활성화된 것으로 처리합니다.
	}
	
}
