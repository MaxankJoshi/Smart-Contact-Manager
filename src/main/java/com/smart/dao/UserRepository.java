package com.smart.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smart.entities.User;

public interface UserRepository extends JpaRepository<User,Integer>{
//	Ist way
	
//	@Query("select u FROM User u WHERE u.email= :email")
//	public User getUserByUserName(@Param("email") String email);
	
//	IInd way
	
	public User findByEmail(String Email);
}
