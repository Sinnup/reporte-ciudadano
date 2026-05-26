package com.espert.reporteciudadano.cloudsync.data.datasource.remote

import com.espert.reporteciudadano.cloudsync.platform.AwsCredentials
import com.espert.reporteciudadano.cloudsync.platform.signAwsRequest
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.datetime.Clock

/**
 * Uploads photo files to S3 using raw HTTP requests with SigV4 signing.
 *
 * Idempotency:
 * 1. Issues a HeadObject request first.
 * 2. If the object already exists (HTTP 200), skips the upload.
 * 3. If the object does not exist (HTTP 404), proceeds with PutObject.
 *
 * S3 key format: reports/<reportId>/<localFilename>
 */
class S3DataSource(
    private val httpClient: HttpClient,
    private val credentials: AwsCredentials,
    private val fileReader: LocalFileReader
) {
    private val host get() = "${credentials.s3BucketName}.s3.${credentials.region}.amazonaws.com"
    private val baseUrl get() = "https://$host"

    suspend fun putPhoto(reportId: String, localPath: String): Result<Unit> = runCatching {
        val filename = localPath.substringAfterLast('/')
        val s3Key = "reports/$reportId/$filename"
        val objectUrl = "$baseUrl/$s3Key"

        val now = Clock.System.now()
        val amzDate = now.toAmzDate()
        val dateStamp = now.toDateStamp()

        // HeadObject check — skips upload if object already exists
        val headHeaders = signAwsRequest(
            method = "HEAD",
            host = host,
            path = "/$s3Key",
            queryParams = "",
            payload = ByteArray(0),
            service = "s3",
            credentials = credentials,
            amzDate = amzDate,
            dateStamp = dateStamp
        )

        val headResponse = httpClient.head(objectUrl) {
            headHeaders.forEach { (k, v) -> header(k, v) }
        }

        when (headResponse.status) {
            HttpStatusCode.OK -> return Result.success(Unit) // Already exists — skip
            HttpStatusCode.NotFound -> Unit // Proceed with upload
            else -> error("S3 HeadObject unexpected status: ${headResponse.status}")
        }

        // Read the file bytes
        val fileBytes = fileReader.readBytes(localPath)
            ?: error("Could not read photo file: $localPath")

        // PutObject
        val putNow = Clock.System.now()
        val putAmzDate = putNow.toAmzDate()
        val putDateStamp = putNow.toDateStamp()

        val putHeaders = signAwsRequest(
            method = "PUT",
            host = host,
            path = "/$s3Key",
            queryParams = "",
            payload = fileBytes,
            service = "s3",
            credentials = credentials,
            amzDate = putAmzDate,
            dateStamp = putDateStamp,
            additionalHeaders = mapOf("content-type" to "image/jpeg")
        )

        val putResponse = httpClient.put(objectUrl) {
            putHeaders.forEach { (k, v) -> header(k, v) }
            setBody(fileBytes)
            contentType(ContentType.Image.JPEG)
        }

        if (!putResponse.status.isSuccess()) {
            error("S3 PutObject failed: ${putResponse.status} — ${putResponse.bodyAsText()}")
        }
    }
}

/** Platform-agnostic file reader. Actuals provided in each platform source set. */
expect class LocalFileReader() {
    fun readBytes(path: String): ByteArray?
}

private fun kotlinx.datetime.Instant.toAmzDate(): String {
    val str = toString()
    return str.replace("-", "").replace(":", "").substringBefore(".").replace("T", "T") + "Z"
}

private fun kotlinx.datetime.Instant.toDateStamp(): String =
    toString().substringBefore("T").replace("-", "")
