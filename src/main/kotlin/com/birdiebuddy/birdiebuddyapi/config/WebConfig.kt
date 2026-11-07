package com.birdiebuddy.birdiebuddyapi.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.nio.file.Files
import java.nio.file.Path

@Configuration
class WebConfig(
    // ImageService와 동일한 저장 경로 사용
    @Value("\${app.upload-dir:uploads}")
    private val uploadDir: String
) : WebMvcConfigurer {

    override fun addResourceHandlers(
        registry: ResourceHandlerRegistry
    ) {
        val directory = Path.of(uploadDir)
            .toAbsolutePath()
            .normalize()

        Files.createDirectories(directory)

        // 운영체제에 맞는 경로를 file: 형태의 주소로 변환
        val location = directory.toUri().toString()
            .let { if (it.endsWith("/")) it else "$it/" }

        registry
            // 브라우저에서 요청할 URL
            .addResourceHandler("/uploads/**")
            // 실제 이미지 파일이 저장된 폴더
            .addResourceLocations(location)
    }
}