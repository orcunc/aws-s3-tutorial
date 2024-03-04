package com.colak;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.BucketCannedACL;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
class S3Test {

    private S3Client s3Client;

    public static void main(String[] args) throws URISyntaxException {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        URL resource = contextClassLoader.getResource("test.txt");

        assert resource != null;
        Path filePath = Paths.get(resource.toURI());

        S3Test s3Test = new S3Test();
        s3Test.connect();
        String bucketName = "mybucket";
        s3Test.createBucket(bucketName, true);
        s3Test.listBuckets();

        s3Test.uploadFile(bucketName, "test.txt", filePath);

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

    // Check with "awslocal s3 ls"
    private void createBucket(String bucketName, boolean isPublic) {
        log.info("Begin createBucket");
        if (!doesBucketExist(bucketName)) {
            CreateBucketRequest.Builder builder = CreateBucketRequest.builder().bucket(bucketName);
            if (isPublic) {
                builder.acl(BucketCannedACL.PUBLIC_READ);
            }
            CreateBucketRequest createBucketRequest = builder.build();
            s3Client.createBucket(createBucketRequest);
        }
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

    // Check with "awslocal s3 ls s3://mybucket" and "awslocal s3 cp s3://mybucket/test.txt -"
    private void uploadFile(String bucketName, String fileName, Path filePath) {
        PutObjectRequest request = PutObjectRequest
                .builder()
                .bucket(bucketName)
                .key(fileName)
                .build();
        s3Client.putObject(request, RequestBody.fromFile(filePath));
    }

    private boolean doesBucketExist(String bucketName) {
        try {
            // List the buckets and check if the specified bucket exists
            ListBucketsResponse listBucketsResponse = s3Client.listBuckets();
            return listBucketsResponse
                    .buckets()
                    .stream()
                    .anyMatch(bucket -> bucket.name().equals(bucketName));
        } catch (S3Exception e) {
            // Handle the exception (e.g., if the user doesn't have permission to list buckets)
            return false;
        }
    }
}