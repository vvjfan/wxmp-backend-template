package com.example.wxmpapidemo.face.service;

import com.example.wxmpapidemo.common.exception.WxApiException;
import com.example.wxmpapidemo.config.WxConfig;
import com.example.wxmpapidemo.face.dto.FaceVerifyResultResponse;
import com.example.wxmpapidemo.face.dto.StartFaceVerifyRequest;
import com.example.wxmpapidemo.face.dto.StartFaceVerifyResponse;
import com.example.wxmpapidemo.face.entity.FaceVerification;
import com.example.wxmpapidemo.face.repository.FaceVerificationRepository;
import com.example.wxmpapidemo.message.service.WxAccessTokenService;
import com.example.wxmpapidemo.user.entity.User;
import com.example.wxmpapidemo.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@Service
public class FaceVerificationService {

    private static final Logger log = LoggerFactory.getLogger(FaceVerificationService.class);

    private static final String GET_VERIFY_ID_URL = "https://api.weixin.qq.com/cityservice/face/identify/getverifyid";
    private static final String GET_VERIFY_RESULT_URL = "https://api.weixin.qq.com/cityservice/face/identify/queryverifyinfo";

    private final FaceVerificationRepository faceVerificationRepository;
    private final UserRepository userRepository;
    private final WxAccessTokenService accessTokenService;
    private final WxConfig wxConfig;
    private final RestClient restClient;

    public FaceVerificationService(FaceVerificationRepository faceVerificationRepository,
                                   UserRepository userRepository,
                                   WxAccessTokenService accessTokenService,
                                   WxConfig wxConfig) {
        this.faceVerificationRepository = faceVerificationRepository;
        this.userRepository = userRepository;
        this.accessTokenService = accessTokenService;
        this.wxConfig = wxConfig;
        this.restClient = RestClient.builder().build();
    }

    public StartFaceVerifyResponse startVerification(String userId, StartFaceVerifyRequest request) {
        User user = userRepository.findById(Long.valueOf(userId))
                .orElseThrow(() -> new WxApiException(404, "用户不存在"));

        if (user.getOpenid() == null) {
            throw new WxApiException(400, "用户未授权，无法获取 openid");
        }

        String accessToken = accessTokenService.getAccessToken();
        String outSeqNo = generateOutSeqNo();
        String verificationId = UUID.randomUUID().toString().replace("-", "");

        String certHash = calculateCertHash(request.getRealName(), request.getIdNumber());

        FaceVerification record = FaceVerification.builder()
                .verificationId(verificationId)
                .openid(user.getOpenid())
                .realName(request.getRealName())
                .idNumber(request.getIdNumber())
                .outSeqNo(outSeqNo)
                .certHash(certHash)
                .status("PENDING")
                .build();

        faceVerificationRepository.save(record);

        String url = GET_VERIFY_ID_URL + "?access_token=" + accessToken;

        Map<String, Object> body = Map.of(
                "out_seq_no", outSeqNo,
                "cert_info", Map.of(
                        "cert_type", "IDENTITY_CARD",
                        "cert_name", request.getRealName(),
                        "cert_no", request.getIdNumber()
                ),
                "openid", user.getOpenid()
        );

        Map<String, Object> response = restClient.post()
                .uri(url)
                .header("Content-Type", "application/json")
                .body(body)
                .retrieve()
                .body(Map.class);

        if (response == null) {
            record.setStatus("FAILED");
            record.setErrorMsg("微信返回空响应");
            faceVerificationRepository.save(record);
            throw new WxApiException(500, "获取 verifyId 失败：微信返回空响应");
        }

        Number errcode = (Number) response.get("errcode");
        String errmsg = (String) response.get("errmsg");

        if (errcode == null || errcode.intValue() != 0) {
            record.setStatus("FAILED");
            record.setErrorMsg(errmsg);
            faceVerificationRepository.save(record);
            throw new WxApiException(400, "获取 verifyId 失败: " + errmsg);
        }

        String verifyId = (String) response.get("verify_id");
        Number expiresIn = (Number) response.get("expires_in");

        record.setVerifyId(verifyId);
        record.setStatus("INITIATED");
        faceVerificationRepository.save(record);

        log.info("人脸核身发起成功，verificationId: {}, verifyId: {}", verificationId, verifyId);

        return new StartFaceVerifyResponse(verificationId, verifyId, expiresIn != null ? expiresIn.intValue() : 3600);
    }

    public FaceVerifyResultResponse queryResult(String verificationId) {
        FaceVerification record = faceVerificationRepository.findByVerificationId(verificationId)
                .orElseThrow(() -> new WxApiException(404, "核身记录不存在"));

        if ("SUCCESS".equals(record.getStatus()) || "FAILED".equals(record.getStatus())) {
            return new FaceVerifyResultResponse(
                    record.getVerificationId(),
                    record.getStatus(),
                    record.getVerifyRet(),
                    record.getErrorMsg()
            );
        }

        String accessToken = accessTokenService.getAccessToken();
        String url = GET_VERIFY_RESULT_URL + "?access_token=" + accessToken;

        Map<String, Object> body = Map.of(
                "verify_id", record.getVerifyId(),
                "out_seq_no", record.getOutSeqNo(),
                "cert_hash", record.getCertHash(),
                "openid", record.getOpenid()
        );

        Map<String, Object> response = restClient.post()
                .uri(url)
                .header("Content-Type", "application/json")
                .body(body)
                .retrieve()
                .body(Map.class);

        if (response == null) {
            throw new WxApiException(500, "查询核身结果失败：微信返回空响应");
        }

        Number errcode = (Number) response.get("errcode");
        String errmsg = (String) response.get("errmsg");

        if (errcode != null && errcode.intValue() != 0) {
            return new FaceVerifyResultResponse(
                    record.getVerificationId(),
                    "QUERY_FAILED",
                    errcode.intValue(),
                    errmsg
            );
        }

        Number verifyRet = (Number) response.get("verify_ret");
        int ret = verifyRet != null ? verifyRet.intValue() : -1;

        String status;
        if (ret == 10000) {
            status = "SUCCESS";
        } else {
            status = "FAILED";
        }

        record.setStatus(status);
        record.setVerifyRet(ret);
        faceVerificationRepository.save(record);

        log.info("人脸核身结果查询成功，verificationId: {}, status: {}, verifyRet: {}",
                verificationId, status, ret);

        return new FaceVerifyResultResponse(
                record.getVerificationId(),
                status,
                ret,
                null
        );
    }

    private String generateOutSeqNo() {
        return "FV" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private String calculateCertHash(String certName, String certNo) {
        String certTypeBase64 = Base64.getEncoder().encodeToString("IDENTITY_CARD".getBytes(StandardCharsets.UTF_8));
        String certNameBase64 = Base64.getEncoder().encodeToString(certName.getBytes(StandardCharsets.UTF_8));
        String certNoBase64 = Base64.getEncoder().encodeToString(certNo.getBytes(StandardCharsets.UTF_8));

        String concat = "cert_type=" + certTypeBase64
                + "&cert_name=" + certNameBase64
                + "&cert_no=" + certNoBase64;

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(concat.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("计算 cert_hash 失败", e);
        }
    }
}
