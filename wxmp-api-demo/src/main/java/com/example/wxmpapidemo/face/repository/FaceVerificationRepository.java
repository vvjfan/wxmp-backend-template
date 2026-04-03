package com.example.wxmpapidemo.face.repository;

import com.example.wxmpapidemo.face.entity.FaceVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FaceVerificationRepository extends JpaRepository<FaceVerification, Long> {
    Optional<FaceVerification> findByVerificationId(String verificationId);
    Optional<FaceVerification> findByVerifyId(String verifyId);
}
