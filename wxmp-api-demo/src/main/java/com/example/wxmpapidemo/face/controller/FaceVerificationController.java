package com.example.wxmpapidemo.face.controller;

import com.example.wxmpapidemo.common.dto.ApiResponse;
import com.example.wxmpapidemo.face.dto.FaceVerifyResultResponse;
import com.example.wxmpapidemo.face.dto.StartFaceVerifyRequest;
import com.example.wxmpapidemo.face.dto.StartFaceVerifyResponse;
import com.example.wxmpapidemo.face.service.FaceVerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/face")
@Tag(name = "人脸核身", description = "微信小程序人脸实名认证相关接口")
public class FaceVerificationController {

    private final FaceVerificationService faceVerificationService;

    public FaceVerificationController(FaceVerificationService faceVerificationService) {
        this.faceVerificationService = faceVerificationService;
    }

    @PostMapping("/verify")
    @Operation(summary = "发起人脸核身", description = "传入姓名和身份证号，获取 verifyId 用于小程序端调起人脸识别")
    public ApiResponse<StartFaceVerifyResponse> startVerify(Authentication authentication,
                                                            @Valid @RequestBody StartFaceVerifyRequest request) {
        String userId = (String) authentication.getPrincipal();
        return ApiResponse.success(faceVerificationService.startVerification(userId, request));
    }

    @GetMapping("/verify/{verificationId}")
    @Operation(summary = "查询人脸核身结果", description = "根据业务流水号查询人脸核身结果")
    public ApiResponse<FaceVerifyResultResponse> queryResult(@PathVariable String verificationId) {
        return ApiResponse.success(faceVerificationService.queryResult(verificationId));
    }
}
