package com.cartwave.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

/**
 * Thin wrapper around AWS S3 SDK v2 for uploading/deleting objects.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AwsS3Service {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name:cartwave-assets}")
    private String bucketName;

    @Value("${aws.s3.base-url:https://s3.amazonaws.com/cartwave-assets}")
    private String baseUrl;

    /**
     * Upload a multipart file and return its public URL.
     *
     * @param folder e.g. "products", "stores"
     * @param file   the uploaded file
     * @return public S3 URL
     */
    public String upload(String folder, MultipartFile file) {
        String key = folder + "/" + UUID.randomUUID() + "-" + sanitize(file.getOriginalFilename());
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();
            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file to S3: " + e.getMessage(), e);
        }
        return baseUrl + "/" + key;
    }

    /**
     * Delete an object by its full URL (or just its S3 key).
     */
    public void delete(String urlOrKey) {
        String key = urlOrKey.startsWith("http") ? urlOrKey.replace(baseUrl + "/", "") : urlOrKey;
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build());
        log.info("Deleted S3 object: {}", key);
    }

    private String sanitize(String filename) {
        if (filename == null) return "file";
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
