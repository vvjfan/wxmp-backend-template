package com.example.wxmpapidemo.user.repository;

import com.example.wxmpapidemo.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByOpenid(String openid);
    Optional<User> findByPhoneNumber(String phoneNumber);
    boolean existsByOpenid(String openid);
}
