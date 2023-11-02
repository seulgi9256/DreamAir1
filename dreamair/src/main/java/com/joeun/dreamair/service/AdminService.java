package com.joeun.dreamair.service;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.joeun.dreamair.dto.Admin;
import com.joeun.dreamair.dto.Users;

public interface AdminService {
  
    /**
     * 관리자
     */
    // 관리자 로그인
    public void admin_login(Admin admin, HttpServletRequest request) throws Exception;

    // 관리자  인증(로그인) - id
    // public Admin login(String adminId) throws Exception;

    // // 관리자 권한 등록
    // public int insertAuth(Auth adminAuth) throws Exception;

    // 관리자 등록
    public int insert(Admin admin) throws Exception;

    
    /**
     * 사용자 관리
     */
    // 사용자(회원, 비회원) 전체 조회
    public List<Users> user_list() throws Exception;

    // 등록
    public int user_insert(int userNo) throws Exception;

    // 수정
    public int user_update(Users user) throws Exception;

    // 삭제
    public int user_delete(int userNo) throws Exception;

}
