package com.example.wxmpapidemo.message.repository;

import com.example.wxmpapidemo.message.entity.SubscribeMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubscribeMessageRepository extends JpaRepository<SubscribeMessage, Long> {
    List<SubscribeMessage> findByOpenid(String openid);
    List<SubscribeMessage> findByStatus(String status);
}
