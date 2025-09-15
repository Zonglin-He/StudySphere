package com.example.controller;

import com.example.entity.RestBean;
import com.example.service.ImageService;
import io.minio.errors.ErrorResponseException;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.nio.charset.StandardCharsets;

@RestController
@Slf4j
public class ObjectController {

    @Resource
    ImageService service;

    @GetMapping("/images/**")
    public void imageFetch(HttpServletRequest request, HttpServletResponse response) throws Exception {
        this.fetchImage(request, response);
    }

    private void fetchImage(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String imagePath = request.getServletPath().substring(7);
        try (ServletOutputStream stream = response.getOutputStream()) {
            if (imagePath.length() <= 13) {
                response.setStatus(404);
                response.setContentType("application/json");
                stream.write(RestBean.failure(404, "Not found").asJsonString().getBytes(StandardCharsets.UTF_8));
                stream.flush();
            } else {
                try {
                    service.fetchImageFromMinio(stream, imagePath);
                    response.setHeader("Cache-Control", "max-age=2592000");
                    response.setHeader("Content-Type", "image/jpg");
                } catch (ErrorResponseException e) {
                    if (e.response().code() == 404) {
                        response.setStatus(404);
                        response.setContentType("application/json");
                        stream.write(RestBean.failure(404, "Not found").asJsonString().getBytes(StandardCharsets.UTF_8));
                    } else {
                        log.error("Error while fetching image from Minio", e.getMessage(), e);
                    }
                    stream.flush();
                }
            }
        }
    }
}
