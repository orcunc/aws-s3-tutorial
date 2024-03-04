package com.colak;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.BucketCannedACL;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;

import java.net.URI;

@Slf4j
class S3Test {

    private S3Client s3Client;

    public static void main(String[] args) {
        S3Test s3Test = new S3Test();
        s3Test.connect();
        s3Test.createBucket("mybucket", true);
        s3Test.listBuckets();

    }

    private void connect() {
        AwsBasicCredentials awsBasicCredentials = AwsBasicCredentials.create("test", "test");
        StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(awsBasicCredentials);

        s3Client = S3Client.builder()
                // Region doesn't matter for LocalStack
                .region(Region.EU_CENTRAL_1)
                .endpointOverride(URI.create("http://s3.localhost.localstack.cloud:4566"))
                .credentialsProvider(credentialsProvider)
                .build();
    }

    private void createBucket(String bucketName, boolean isPublic) {
        log.info("Begin createBucket");
        CreateBucketRequest.Builder builder = CreateBucketRequest.builder().bucket(bucketName);
        if (isPublic) {
            builder.acl(BucketCannedACL.PUBLIC_READ);
        }
        CreateBucketRequest createBucketRequest = builder.build();
        s3Client.createBucket(createBucketRequest);
        log.info("End createBucket");
    }

    private void listBuckets() {
        log.info("Begin listBuckets");
        ListBucketsResponse listBucketsResponse = s3Client.listBuckets();
        for (Bucket bucket : listBucketsResponse.buckets()) {
            log.info("Bucket name : {}", bucket.name());
        }
        log.info("End listBuckets");
    }
}