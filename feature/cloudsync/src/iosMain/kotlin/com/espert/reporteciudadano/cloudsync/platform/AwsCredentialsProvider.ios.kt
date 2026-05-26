package com.espert.reporteciudadano.cloudsync.platform

import platform.Foundation.NSBundle
import platform.Foundation.NSString
import platform.Foundation.stringWithContentsOfFile
import platform.Foundation.NSUTF8StringEncoding

// Reads aws.properties from the iOS main bundle.
// The file must be added to the Xcode project's "Copy Bundle Resources" phase.
// The file is gitignored — it must never be committed to source control.
actual fun loadAwsCredentials(): AwsCredentials {
    val path = NSBundle.mainBundle.pathForResource("aws", ofType = "properties")
        ?: error("aws.properties not found in Bundle.main. Add it to the Xcode 'Copy Bundle Resources' phase.")

    @Suppress("UNCHECKED_CAST")
    val content = NSString.stringWithContentsOfFile(path, NSUTF8StringEncoding, null) as? String
        ?: error("Could not read aws.properties from bundle.")

    val props = parseProperties(content)

    return AwsCredentials(
        accessKeyId = props["aws.accessKeyId"]
            ?: error("aws.accessKeyId missing in aws.properties"),
        secretAccessKey = props["aws.secretAccessKey"]
            ?: error("aws.secretAccessKey missing in aws.properties"),
        region = props["aws.region"]
            ?: error("aws.region missing in aws.properties"),
        dynamoDbTableName = props["aws.dynamodb.tableName"]
            ?: error("aws.dynamodb.tableName missing in aws.properties"),
        s3BucketName = props["aws.s3.bucketName"]
            ?: error("aws.s3.bucketName missing in aws.properties")
    )
}

private fun parseProperties(content: String): Map<String, String> =
    content.lines()
        .asSequence()
        .map { it.trim() }
        .filter { it.isNotEmpty() && !it.startsWith('#') && it.contains('=') }
        .associate { line ->
            val idx = line.indexOf('=')
            line.substring(0, idx).trim() to line.substring(idx + 1).trim()
        }
