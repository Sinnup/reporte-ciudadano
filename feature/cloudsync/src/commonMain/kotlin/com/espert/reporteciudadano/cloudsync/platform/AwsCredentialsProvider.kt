package com.espert.reporteciudadano.cloudsync.platform

/**
 * Holds the AWS credentials and configuration needed for DynamoDB and S3 access.
 *
 * Never hardcode these values. Load them at runtime from a gitignored aws.properties file.
 *
 * aws.properties format (copy to androidApp/src/main/assets/aws.properties on Android,
 * or add to Xcode's "Copy Bundle Resources" phase on iOS — both paths are gitignored):
 *
 *   aws.accessKeyId=AKIA...
 *   aws.secretAccessKey=wJalr...
 *   aws.region=us-east-1
 *   aws.dynamodb.tableName=reporteciudadano-reports
 *   aws.s3.bucketName=reporteciudadano-photos
 */
data class AwsCredentials(
    val accessKeyId: String,
    val secretAccessKey: String,
    val region: String,
    val dynamoDbTableName: String,
    val s3BucketName: String
)

expect fun loadAwsCredentials(): AwsCredentials
