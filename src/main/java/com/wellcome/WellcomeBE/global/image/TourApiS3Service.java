package com.wellcome.WellcomeBE.global.image;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.wellcome.WellcomeBE.global.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import static com.wellcome.WellcomeBE.global.exception.CustomErrorCode.TOUR_API_IMG_S3_UPLOAD_FAILED;

@Service
@Slf4j
public class TourApiS3Service {

    @Autowired
    private AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.tour_api_img.bucketName}")
    private String bucketName;

    public String uploadImg(String imgUrl, String contentId) {

        // 이미지 URL로부터 파일 다운로드 후 S3에 업로드
        try(InputStream inputStream = new URL(imgUrl).openStream()) {
            // 파일명 지정
            String fileName = System.currentTimeMillis() + "_" + contentId;

            // 파일 확장자를 기반으로 MIME 타입 결정
            ObjectMetadata metadata = new ObjectMetadata();
            String mimeType = getMimeType(imgUrl);
            metadata.setContentType(mimeType);

            // Content-Length 설정
            URLConnection connection = new URL(imgUrl).openConnection();
            int contentLength = connection.getContentLength();
            if(contentLength > 0){
                metadata.setContentLength(contentLength);
            }else{
                log.warn("Content-Length 정보를 가져올 수 없습니다.");
            }

            // S3 업로드
            amazonS3.putObject(new PutObjectRequest(bucketName, fileName, inputStream, metadata));

            // S3 업로드 후 생성되는 객체 URL 반환
            return amazonS3.getUrl(bucketName, fileName).toString();
        } catch(IOException e){
            log.error("IOException 발생: {}", e.getMessage());
            throw new CustomException(TOUR_API_IMG_S3_UPLOAD_FAILED);
        } catch (AmazonServiceException e) {
            log.error("AWS S3 Service 오류 발생: {}", e.getMessage());
            throw new CustomException(TOUR_API_IMG_S3_UPLOAD_FAILED);
        } catch (AmazonClientException e) {
            log.error("AWS S3 Client 오류 발생: {}", e.getMessage());
            throw new CustomException(TOUR_API_IMG_S3_UPLOAD_FAILED);
        }
    }

    private String getMimeType(String url){
        if (url != null && url.lastIndexOf(".") != -1) {
            String ext = url.substring(url.lastIndexOf(".") + 1).toLowerCase();
            switch (ext){
                case "jpg": case "jpeg": case "JPG":
                    return "image/jpeg";

                case "png":
                    return "image/png";

                case "bmp":
                    return "image/bmp";
            }
        }
        return null;
    }

}
