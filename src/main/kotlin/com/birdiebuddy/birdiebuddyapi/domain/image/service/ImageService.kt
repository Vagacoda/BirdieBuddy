package com.birdiebuddy.birdiebuddyapi.domain.image.service

import com.birdiebuddy.birdiebuddyapi.domain.image.ImageUploadResponse
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayInputStream
import java.io.IOException
import java.util.Locale
import javax.imageio.ImageIO
import javax.imageio.stream.MemoryCacheImageInputStream
import com.birdiebuddy.birdiebuddyapi.domain.image.ImageEntity
import com.birdiebuddy.birdiebuddyapi.domain.image.ImageRepository
import org.springframework.transaction.annotation.Transactional

@Service
class ImageService(
    private val imageRepository: ImageRepository
) {
    @Transactional
    fun upload(
        image: MultipartFile,
        userId: Long
    ): ImageUploadResponse {
        // 빈 파일 차단
        require(!image.isEmpty) {
            "이미지 파일을 선택해주세요."
        }

        // 파일 하나당 최대 5MB
        require(image.size <= 5L * 1024 * 1024) {
            "이미지 파일은 5MB 이하만 업로드할 수 있습니다."
        }

        // 실제 사진 데이터
        val bytes = image.bytes

        // 기존 함수로 이미지 형식·해상도·데이터 검사
        val extension = validateImage(bytes)

        // 검사에서 확인한 형식으로 응답에 사용할 이미지 타입 결정
        val contentType = when (extension) {
            "jpg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            else -> throw IllegalArgumentException(
                "지원하지 않는 이미지 형식입니다."
            )
        }

        // DB에 저장할 이미지 객체 생성
        val imageEntity = ImageEntity(
            contentType = contentType,
            data = bytes,
            uploaderId = userId
        )

        // 사진 바이트와 업로더 정보를 DB에 저장
        val savedImage = imageRepository.save(imageEntity)

        val imageId = checkNotNull(savedImage.id) {
            "저장된 이미지의 ID가 없습니다."
        }

        // 이후 만들 이미지 조회 API 주소 반환
        return ImageUploadResponse(
            url = "/api/images/$imageId"
        )
    }

    private fun validateImage(bytes: ByteArray): String {
        try {
            MemoryCacheImageInputStream(
                ByteArrayInputStream(bytes)
                // 바이트 배열을 이미지 처리 도구가 읽을 수 있는 형태로 감쌈
            ).use { input ->
                // 파일 내용을 해석할 수 있는 이미지 리더 탐색
                val readers = ImageIO.getImageReaders(input)

                require(readers.hasNext()) {
                    "읽을 수 없는 이미지 파일입니다."
                }

                val reader = readers.next()

                try {
                    reader.input = input

                    val extension = when (
                        reader.formatName.lowercase(Locale.ROOT)
                    ) {
                        "jpeg", "jpg" -> "jpg"
                        "png" -> "png"
                        "gif" -> "gif"
                        else -> throw IllegalArgumentException(
                            "JPG, PNG, GIF 이미지만 업로드할 수 있습니다."
                        )
                    }

                    // 압축 파일은 작아도 이미지 해상도가 너무 크면
                    // 메모리를 많이 사용하므로 디코딩 전에 제한
                    val width = reader.getWidth(0)
                    val height = reader.getHeight(0)

                    require(
                        width in 1..8000 &&
                                height in 1..8000 &&
                                width.toLong() * height <= 20_000_000L
                    ) {
                        "이미지는 가로·세로 각각 8000픽셀 이하, 총 2000만 픽셀 이하여야 합니다."
                    }

                    // 첫 번째 이미지를 실제로 읽을 수 있는지 검사
                    // GIF도 첫 프레임을 검사하며 저장은 원본 그대로 수행
                    val decoded = reader.read(0)
                    requireNotNull(decoded) {
                        "이미지 데이터를 읽을 수 없습니다."
                    }
                    decoded.flush()

                    return extension
                } finally {
                    // 성공하거나 예외가 발생해도 리더 자원 해제
                    reader.dispose()
                }
            }
        } catch (exception: IOException) {
            throw IllegalArgumentException(
                "손상되었거나 읽을 수 없는 이미지 파일입니다.",
                exception
            )
        }
    }
}