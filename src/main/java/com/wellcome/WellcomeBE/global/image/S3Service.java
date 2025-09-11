package com.wellcome.WellcomeBE.global.image;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.util.IOUtils;
import com.wellcome.WellcomeBE.global.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import static com.wellcome.WellcomeBE.global.exception.CustomErrorCode.TOUR_API_IMG_S3_UPLOAD_FAILED;

@Service
@Slf4j
public class S3Service {

    @Autowired
    private AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucketName}")
    private String bucketName;

    private static final int retryLimit = 2;

    public String uploadDetailImageFromUrl(String imageUrl, String contentId) {
        final int MEMORY_THRESHOLD = 5 * 1024 * 1024; // 5MB 이하: 메모리 사용
        String fileExtension = getFileExtension(imageUrl);
        String folderPath = "detail-images/" + contentId + "/";
        String fileName = folderPath + UUID.randomUUID() + "." + fileExtension;

        URL url;
        try {
            url = new URL(imageUrl);
        } catch (MalformedURLException e) {
            log.error("잘못된 URL: {}", imageUrl, e);
            return null;
        }

        try {
            URLConnection connection = url.openConnection();
            int contentLength = connection.getContentLength(); // -1일 수 있음

            // InputStream 준비
            try (InputStream inputStream = connection.getInputStream()) {

                if (contentLength > 0 && contentLength <= MEMORY_THRESHOLD) {
                    // 작은 파일: 메모리에 올리고 재시도 가능하게 처리
                    byte[] bytes = inputStream.readAllBytes();
                    try (ByteArrayInputStream repeatableStream = new ByteArrayInputStream(bytes)) {
                        ObjectMetadata metadata = new ObjectMetadata();
                        metadata.setContentLength(bytes.length);
                        metadata.setContentType(getMimeType(fileExtension));

                        amazonS3.putObject(new PutObjectRequest(bucketName, fileName, repeatableStream, metadata));
                    }

                } else {
                    // 큰 파일 또는 길이 알 수 없는 파일: 임시 파일 사용
                    Path tempFile = Files.createTempFile("s3upload-", "." + fileExtension);
                    Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);

                    try (InputStream fileStream = Files.newInputStream(tempFile)) {
                        ObjectMetadata metadata = new ObjectMetadata();
                        if (contentLength > 0) metadata.setContentLength(contentLength);
                        metadata.setContentType(getMimeType(fileExtension));

                        amazonS3.putObject(new PutObjectRequest(bucketName, fileName, fileStream, metadata));
                    } finally {
                        Files.deleteIfExists(tempFile);
                    }
                }

            }

            return amazonS3.getUrl(bucketName, fileName).toString();

        } catch (FileNotFoundException e) {
            log.warn("FileNotFoundException 발생: imgUrl: {}, contentId: {}", imageUrl, contentId);
            return null;
        }catch (AmazonServiceException e) {
            log.error("AWS S3 서비스 오류: imgUrl: {}, contentId: {}", imageUrl, contentId);
            throw new CustomException(TOUR_API_IMG_S3_UPLOAD_FAILED);
        } catch (IOException | AmazonClientException e){
            log.error("AWS S3 오류 발생: imgUrl: {}, contentId: {}", imageUrl, contentId);
            throw new CustomException(TOUR_API_IMG_S3_UPLOAD_FAILED);
        }
    }


    private String getFileExtension(String url) {
        int lastDot = url.lastIndexOf('.');
        return (lastDot != -1) ? url.substring(lastDot + 1).toLowerCase() : "jpg";
    }

    public String uploadImgFile(MultipartFile multipartFile) throws IOException {
        File file = multiPartFileToFile(multipartFile);

        // 'UUID.확장자' 형태의 파일명으로 이미지 업로드
        String originalFileName = multipartFile.getOriginalFilename();
        String fileExtension =  originalFileName.substring(originalFileName.lastIndexOf(".") + 1);

        String folderPath = "community-images/";
        String fileName = folderPath + UUID.randomUUID() + "." + fileExtension;
        amazonS3.putObject(new PutObjectRequest(bucketName, fileName, file));
        file.delete();

        return amazonS3.getUrl(bucketName, fileName).toString();
    }

    private File multiPartFileToFile(MultipartFile file) throws IOException {
        File tempFile = File.createTempFile("temp", file.getOriginalFilename());
        try (FileOutputStream fileOutputStream = new FileOutputStream(tempFile)) {
            fileOutputStream.write(file.getBytes());
        }
        return tempFile;
    }

    @Value("${app.default.image.url}")
    private String defaultImageUrl;

    public String getDefaultImageUrl() {
        return defaultImageUrl;
    }

    /**
     *이미지URL (한국관광공사API제공 이미지)
     */
    public String uploadImgFromUrl(String imgUrl, String contentId) {
        String imgUploadFailedTag = "[이미지 업로드 실패] ";

        for(int attempt = 1; attempt <= retryLimit; attempt++) {
            try{
                // 타임아웃 설정
                URL url = new URL(imgUrl);
                URLConnection connection = url.openConnection();
                connection.setConnectTimeout(3000);
                connection.setReadTimeout(5000);
                connection.connect();

                // 이미지 URL로부터 파일 다운로드 후 S3에 업로드
                try(InputStream inputStream = new URL(imgUrl).openStream()) {
                    byte[] imageBytes = IOUtils.toByteArray(inputStream);
                    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(imageBytes);

                    // 파일명 지정
                    String folderPath = "tour-api-images/";
                    String fileName = folderPath + System.currentTimeMillis() + "_" + contentId;

                    // 파일 확장자를 기반으로 MIME 타입 결정, Content Length 설정
                    ObjectMetadata metadata = new ObjectMetadata();
                    String mimeType = getMimeType(imgUrl);
                    metadata.setContentType(mimeType);

                    metadata.setContentLength(imageBytes.length);

                    // S3 업로드
                    amazonS3.putObject(new PutObjectRequest(bucketName, fileName, byteArrayInputStream, metadata));

                    // S3 업로드 후 생성되는 객체 URL 반환
                    return amazonS3.getUrl(bucketName, fileName).toString();
                }
            } catch (MalformedURLException e) {
                log.warn(imgUploadFailedTag + "잘못된 URL 형식: contentId={}, imgUrl={}", contentId, imgUrl);
                return null;
            } catch (FileNotFoundException e){
                log.warn(imgUploadFailedTag + "해당하는 이미지 파일 없음: contentId={}, imgUrl={}", contentId, imgUrl);
                return null;
            } catch (AmazonServiceException e) {
                log.error(imgUploadFailedTag + "AWS S3 서비스 오류: contentId={}, imgUrl={}, error={}", contentId, imgUrl, e.getMessage());
                throw new CustomException(TOUR_API_IMG_S3_UPLOAD_FAILED);
            } catch (IOException | AmazonClientException e){
                log.warn("[이미지 업로드 재시도 {}/{}] contentId={}, imgUrl={}, causeType={}, error={}", attempt, retryLimit,
                        contentId, imgUrl, e.getClass().getSimpleName(), e.getMessage());

                // 3초 대기 후 재시도
                if (attempt < retryLimit) {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                } else {
                    log.error(imgUploadFailedTag + "재시도 실패: contentId={}, imgUrl={}, error={}", contentId, imgUrl, e.getMessage());
                    throw new CustomException(TOUR_API_IMG_S3_UPLOAD_FAILED);
                }
            }
        }
        return null;
    }

    private String getMimeType(String url){
        if (url != null && url.lastIndexOf(".") != -1) {
            String ext = url.substring(url.lastIndexOf(".") + 1).toLowerCase();
            switch (ext) {
                case "jpg", "jpeg", "JPG" -> {
                    return "image/jpeg";
                }
                case "png" -> {
                    return "image/png";
                }
                case "bmp" -> {
                    return "image/bmp";
                }
            }
        }
        return null;
    }

}
