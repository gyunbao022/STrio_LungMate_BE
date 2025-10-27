package com.example.strio01.config.auth;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.strio01.user.dto.AuthInfo;
import com.example.strio01.user.entity.UserInfoEntity;
import com.example.strio01.user.repository.UserInfoRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PrincipalDetailesService implements UserDetailsService {

	@Autowired
	private UserInfoRepository adminRepository;

	// 1. AuthenticationProvider에서 loadUserByUsername(String memberEmail)을 호출한다.
	// 2. loadUserByUsername(String adminId)에서는 DB에서 adminId에 해당하는 데이터를 검색해서
	// UserDetails에 담아서 리턴해준다.
	// 3. AuthenticationProvider에서 UserDetailes받아서 Authentication에 저장을 함으로써 결국
	// Security Session에 저장을 한다.

	@Override
	public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
		log.info("PrincipalDetailesService => loadUserByUsername() => adminId:{}", userId);
		System.out.println("====== login ======: 2");
		Optional<UserInfoEntity> optMembersEntity = adminRepository.findById(userId);
		
		if(optMembersEntity.isEmpty()) {
			throw new UsernameNotFoundException(userId + "사용자명이 존재하지 않습니다.");
		}
		
		UserInfoEntity adminEntity = optMembersEntity.get();
		log.info("adminId:{} pwd:{} name:{} role:{} email:{}",  adminEntity.getUserId(), adminEntity.getPasswd(),adminEntity.getUserName(),adminEntity.getRoleCd(),adminEntity.getEmail());
		System.out.println("====== login ======: 3");
		AuthInfo authInfo = AuthInfo.builder()
				.userId(adminEntity.getUserId())
				.passwd(adminEntity.getPasswd())
				.userName(adminEntity.getUserName())
				.roleCd(adminEntity.getRoleCd())
				.email(adminEntity.getEmail())
				.build();
		return new PrincipalDetails(authInfo);
	}

}
