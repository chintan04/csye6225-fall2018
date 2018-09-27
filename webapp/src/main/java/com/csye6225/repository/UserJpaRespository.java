package com.csye6225.repository;

import com.csye6225.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Component
public interface UserJpaRespository extends JpaRepository<Users, String>{

}
