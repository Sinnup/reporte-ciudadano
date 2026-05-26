package com.espert.reporteciudadano.cloudsync.platform

import android.content.Context
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.Properties

// Reads aws.properties from the Android assets folder.
// The file must be placed at androidApp/src/main/assets/aws.properties by the developer.
// That path is gitignored — it must never be committed to source control.
actual fun loadAwsCredentials(): AwsCredentials {
    val loader = AndroidCredentialsLoader()
    return loader.load()
}

private class AndroidCredentialsLoader : KoinComponent {
    private val context: Context by inject()

    fun load(): AwsCredentials {
        val props = context.assets.open("aws.properties").use { stream ->
            Properties().apply { load(stream) }
        }
        return AwsCredentials(
            accessKeyId = props.getProperty("aws.accessKeyId")
                ?: error("aws.accessKeyId missing in aws.properties"),
            secretAccessKey = props.getProperty("aws.secretAccessKey")
                ?: error("aws.secretAccessKey missing in aws.properties"),
            region = props.getProperty("aws.region")
                ?: error("aws.region missing in aws.properties"),
            dynamoDbTableName = props.getProperty("aws.dynamodb.tableName")
                ?: error("aws.dynamodb.tableName missing in aws.properties"),
            s3BucketName = props.getProperty("aws.s3.bucketName")
                ?: error("aws.s3.bucketName missing in aws.properties")
        )
    }
}
