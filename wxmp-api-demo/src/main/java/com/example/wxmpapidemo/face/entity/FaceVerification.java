package com.example.wxmpapidemo.face.entity;

import com.example.wxmpapidemo.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@Table(name = "face_verifications")
public class FaceVerification extends BaseEntity {

    @EqualsAndHashCode.Include
    @Column(name = "verification_id", nullable = false, unique = true)
    private String verificationId;

    @Column(name = "openid", nullable = false)
    private String openid;

    @Column(name = "real_name", nullable = false)
    private String realName;

    @Column(name = "id_number", nullable = false)
    private String idNumber;

    @Column(name = "verify_id")
    private String verifyId;

    @Column(name = "out_seq_no")
    private String outSeqNo;

    @Column(name = "cert_hash")
    private String certHash;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "verify_ret")
    private Integer verifyRet;

    @Column(name = "error_msg")
    private String errorMsg;
}
