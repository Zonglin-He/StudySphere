package com.example.controller;

import com.example.entity.RestBean;
import com.example.service.ImageService;
import com.example.utils.Const;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/api/image")
public class ImageController {

    @Resource
    ImageService service;

    @PostMapping("/avatar")
    public RestBean<String> uploadAvatar(@RequestParam("file") MultipartFile file,
                                         @RequestAttribute(Const.ATTR_USER_ID) int id) throws IOException {
        if (file.getSize() > 1024 * 100) {
            return RestBean.failure(400, "The size of avatar cannot be larger than 100 KB");}
        log.info("Uploading avatar file...");
        String url = service.uploadAvatar(file, id);
        if (url != null) {
            log.info("Successfully uploaded avatar file " + file.getSize());
            return RestBean.success(url);}
        else {return RestBean.failure(400, "Failed to upload avatar file! Please contact administrator");}
    }

    @PostMapping("/cache")
    public RestBean<String> uploadImage(@RequestParam("file") MultipartFile file,
                                        @RequestAttribute(Const.ATTR_USER_ID) int id,
                                        HttpServletResponse response) throws IOException {
        if (file.getSize() > 1024 * 100 * 5) {
            return RestBean.failure(400, "The size of picture file cannot be larger than 5 MB");
        }
        log.info("Uploading picture file...");
        String url = service.uploadImage(file, id);
        if (url != null) {
            log.info("Successfully uploaded picture file " + file.getSize());
            return RestBean.success(url);}
        else {
            response.setStatus(400);
            return RestBean.failure(400, "Failed to upload picture file! Please contact administrator");}
    }
}
