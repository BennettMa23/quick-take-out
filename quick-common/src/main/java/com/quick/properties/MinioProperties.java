package com.quick.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "quick.minio")
public class MinioProperties {
    private String endpoint;
    private String accessKey;
    private String secretKey;
//    private String bucketName;

}
