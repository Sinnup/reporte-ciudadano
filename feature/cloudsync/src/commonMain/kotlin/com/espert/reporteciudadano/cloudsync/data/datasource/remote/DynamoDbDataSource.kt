package com.espert.reporteciudadano.cloudsync.data.datasource.remote

import com.espert.reporteciudadano.cloudsync.platform.AwsCredentials
import com.espert.reporteciudadano.cloudsync.platform.signAwsRequest
import com.espert.reporteciudadano.domain.model.CitizenReport
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.datetime.Clock
import kotlinx.serialization.json.*

/**
 * Uploads CitizenReport metadata to DynamoDB using a raw HTTP PutItem request with SigV4 signing.
 *
 * Idempotency: uses ConditionExpression `attribute_not_exists(id)`.
 * A ConditionalCheckFailedException response (HTTP 400 with specific error code) is treated as success.
 */
class DynamoDbDataSource(
    private val httpClient: HttpClient,
    private val credentials: AwsCredentials
) {
    private val host get() = "dynamodb.${credentials.region}.amazonaws.com"
    private val endpoint get() = "https://$host"

    suspend fun putReport(report: CitizenReport): Result<Unit> = runCatching {
        val now = Clock.System.now()
        val amzDate = now.toAmzDate()
        val dateStamp = now.toDateStamp()

        val item = buildJsonObject {
            putJsonObject("id") { put("S", report.id) }
            putJsonObject("title") { put("S", report.title) }
            putJsonObject("description") { put("S", report.description) }
            putJsonObject("latitude") { put("N", report.location.latitude.toString()) }
            putJsonObject("longitude") { put("N", report.location.longitude.toString()) }
            putJsonObject("status") { put("S", report.status.name) }
            putJsonObject("createdAt") { put("N", report.createdAt.toString()) }
        }

        val requestBody = buildJsonObject {
            put("TableName", credentials.dynamoDbTableName)
            put("Item", item)
            put("ConditionExpression", "attribute_not_exists(id)")
        }.toString()

        val payloadBytes = requestBody.encodeToByteArray()

        val headers = signAwsRequest(
            method = "POST",
            host = host,
            path = "/",
            queryParams = "",
            payload = payloadBytes,
            service = "dynamodb",
            credentials = credentials,
            amzDate = amzDate,
            dateStamp = dateStamp,
            additionalHeaders = mapOf("content-type" to "application/x-amz-json-1.0")
        )

        val response = httpClient.post(endpoint) {
            headers.forEach { (k, v) -> header(k, v) }
            header("X-Amz-Target", "DynamoDB_20120810.PutItem")
            setBody(requestBody)
            contentType(ContentType.parse("application/x-amz-json-1.0"))
        }

        when {
            response.status.isSuccess() -> Unit
            response.status == HttpStatusCode.BadRequest -> {
                val body = response.bodyAsText()
                if (body.contains("ConditionalCheckFailedException")) {
                    // Item already exists — idempotent, treat as success
                    Unit
                } else {
                    error("DynamoDB PutItem failed: ${response.status} — $body")
                }
            }
            else -> error("DynamoDB PutItem failed: ${response.status} — ${response.bodyAsText()}")
        }
    }
}

private fun kotlinx.datetime.Instant.toAmzDate(): String {
    val str = toString() // e.g. "2023-01-01T12:00:00.000Z"
    return str.replace("-", "").replace(":", "").substringBefore(".").replace("T", "T") + "Z"
}

private fun kotlinx.datetime.Instant.toDateStamp(): String =
    toString().substringBefore("T").replace("-", "")
