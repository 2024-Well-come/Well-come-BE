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

    /**
     * MultipartFile
     */
    public void uploadFile(MultipartFile multipartFile) throws IOException {
        File file = multiPartFileToFile(multipartFile);
        String fileName = System.currentTimeMillis() + "_" + multipartFile.getOriginalFilename();
        amazonS3.putObject(new PutObjectRequest(bucketName, fileName, file));
        file.delete();
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
//    public String uploadImgFromUrl(String imgUrl, String contentId) {
//
//        // 이미지 URL로부터 파일 다운로드 후 S3에 업로드
//        try(InputStream inputStream = new URL(imgUrl).openStream()) {
//            // 파일명 지정
//            String folderPath = "tour-api-images/";
//            String fileName = folderPath + System.currentTimeMillis() + "_" + contentId;
//
//            // 파일 확장자를 기반으로 MIME 타입 결정
//            ObjectMetadata metadata = new ObjectMetadata();
//            String mimeType = getMimeType(imgUrl);
//            metadata.setContentType(mimeType);
//
//            // Content-Length 설정
//            URLConnection connection = new URL(imgUrl).openConnection();
//            int contentLength = connection.getContentLength();
//            if(contentLength > 0){
//                metadata.setContentLength(contentLength);
//            }else{
//                log.warn("Content-Length 정보를 가져올 수 없습니다.");
//            }
//
//            // S3 업로드
//            amazonS3.putObject(new PutObjectRequest(bucketName, fileName, inputStream, metadata));
//
//            // S3 업로드 후 생성되는 객체 URL 반환
//            return amazonS3.getUrl(bucketName, fileName).toString();
//        } catch (FileNotFoundException e){
//            log.warn("FileNotFoundException 발생: imgUrl: {}, contentId: {}", imgUrl, contentId);
//            return null;
//        } catch (IOException e){
//            log.error("IOException 발생: {}", e.getMessage());
//            throw new CustomException(TOUR_API_IMG_S3_UPLOAD_FAILED);
//        } catch (AmazonServiceException e) {
//            log.error("AWS S3 Service 오류 발생: {}", e.getMessage());
//            throw new CustomException(TOUR_API_IMG_S3_UPLOAD_FAILED);
//        } catch (AmazonClientException e) {
//            log.error("AWS S3 Client 오류 발생: {}", e.getMessage());
//            throw new CustomException(TOUR_API_IMG_S3_UPLOAD_FAILED);
//        }
//    }

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
