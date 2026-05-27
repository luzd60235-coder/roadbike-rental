package com.example.roadbikerental.controller.admin;

import com.example.roadbikerental.common.annotation.RoleRequired;
import com.example.roadbikerental.common.enums.RoleType;
import com.example.roadbikerental.common.exception.BusinessException;
import com.example.roadbikerental.common.result.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * 后台图片上传控制器。
 */
@RestController
@RequestMapping("/api/admin/uploads")
@RoleRequired(RoleType.ADMIN)
public class AdminUploadController {

    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(
            Arrays.asList("jpg", "jpeg", "png", "webp")
    );

    @PostMapping(value = "/bike-cover", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<String> uploadBikeCover(@RequestPart("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择要上传的图片");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BusinessException("图片大小不能超过 5MB");
        }

        String extension = resolveExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException("仅支持 JPG、PNG、WEBP 格式图片");
        }

        File uploadDir = new File(System.getProperty("user.dir"), "uploads/bike-covers");
        if (!uploadDir.exists() && !uploadDir.mkdirs()) {
            throw new BusinessException("图片目录创建失败");
        }

        String fileName = UUID.randomUUID().toString().replace("-", "") + "." + extension;
        File targetFile = new File(uploadDir, fileName);
        try {
            file.transferTo(targetFile);
        } catch (IOException ex) {
            throw new BusinessException("图片上传失败");
        }

        return ApiResponse.success("/uploads/bike-covers/" + fileName);
    }

    @PostMapping(value = "/store-cover", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<String> uploadStoreCover(@RequestPart("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择要上传的图片");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BusinessException("图片大小不能超过 5MB");
        }

        String extension = resolveExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException("仅支持 JPG、PNG、WEBP 格式图片");
        }

        File uploadDir = new File(System.getProperty("user.dir"), "uploads/store-covers");
        if (!uploadDir.exists() && !uploadDir.mkdirs()) {
            throw new BusinessException("图片目录创建失败");
        }

        String fileName = UUID.randomUUID().toString().replace("-", "") + "." + extension;
        File targetFile = new File(uploadDir, fileName);
        try {
            file.transferTo(targetFile);
        } catch (IOException ex) {
            throw new BusinessException("图片上传失败");
        }

        return ApiResponse.success("/uploads/store-covers/" + fileName);
    }

    private String resolveExtension(String originalFilename) {
        if (!StringUtils.hasText(originalFilename)) {
            return "";
        }
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == originalFilename.length() - 1) {
            return "";
        }
        return originalFilename.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }
}
