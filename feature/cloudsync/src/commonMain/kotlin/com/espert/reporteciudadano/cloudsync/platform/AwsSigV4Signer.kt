package com.espert.reporteciudadano.cloudsync.platform

/**
 * Platform-specific HMAC-SHA256 implementation.
 *
 * Android: javax.crypto.Mac with HmacSHA256
 * iOS: CommonCrypto.CCHmac via Kotlin/Native
 * JS: SubtleCrypto (synchronous wrapper)
 * WasmJS: stub (sync not supported; network calls are no-ops on WasmJS)
 */
expect fun hmacSha256(key: ByteArray, data: ByteArray): ByteArray

/** Hex-encode a byte array. */
fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }

/** SHA-256 of a String, returned as hex. */
expect fun sha256Hex(data: String): String

/** SHA-256 of raw bytes, returned as hex. Use this for binary payloads. */
expect fun sha256Hex(data: ByteArray): String

/**
 * AWS Signature Version 4 request signing.
 *
 * All string-to-sign construction and canonical request building is pure Kotlin.
 * Only the HMAC-SHA256 primitive is platform-specific.
 */
data class SignedHeaders(
    val headers: Map<String, String>
)

/**
 * Signs an AWS HTTP request using Signature Version 4.
 *
 * @param method HTTP method (GET, PUT, POST, etc.)
 * @param host e.g. "dynamodb.us-east-1.amazonaws.com"
 * @param path URI path e.g. "/"
 * @param queryParams URL query parameters (sorted by key, already URL-encoded)
 * @param payload Request body bytes
 * @param service AWS service name e.g. "dynamodb" or "s3"
 * @param credentials AWS credentials
 * @param amzDate ISO 8601 date-time in the format "20230101T120000Z"
 * @param dateStamp Date-only stamp e.g. "20230101"
 * @param additionalHeaders Extra headers to include in the canonical request (already lower-cased keys)
 * @return Map of headers to add to the request including Authorization and x-amz-date
 */
fun signAwsRequest(
    method: String,
    host: String,
    path: String,
    queryParams: String,
    payload: ByteArray,
    service: String,
    credentials: AwsCredentials,
    amzDate: String,
    dateStamp: String,
    additionalHeaders: Map<String, String> = emptyMap()
): Map<String, String> {
    val payloadHash = sha256Hex(payload)

    // Canonical headers: host + x-amz-date + x-amz-content-sha256 + any additional
    val canonicalHeadersMap = buildMap {
        put("host", host)
        put("x-amz-content-sha256", payloadHash)
        put("x-amz-date", amzDate)
        putAll(additionalHeaders.mapKeys { it.key.lowercase() })
    }

    val sortedHeaders = canonicalHeadersMap.entries.sortedBy { it.key }
    val canonicalHeaders = sortedHeaders.joinToString("\n") { "${it.key}:${it.value}" } + "\n"
    val signedHeadersStr = sortedHeaders.joinToString(";") { it.key }

    val canonicalRequest = listOf(
        method.uppercase(),
        path,
        queryParams,
        canonicalHeaders,
        signedHeadersStr,
        payloadHash
    ).joinToString("\n")

    val credentialScope = "$dateStamp/${credentials.region}/$service/aws4_request"
    val stringToSign = listOf(
        "AWS4-HMAC-SHA256",
        amzDate,
        credentialScope,
        sha256Hex(canonicalRequest)
    ).joinToString("\n")

    // Derive the signing key
    val signingKey = deriveSigningKey(credentials.secretAccessKey, dateStamp, credentials.region, service)
    val signature = hmacSha256(signingKey, stringToSign.encodeToByteArray()).toHex()

    val authorizationHeader =
        "AWS4-HMAC-SHA256 Credential=${credentials.accessKeyId}/$credentialScope, " +
        "SignedHeaders=$signedHeadersStr, " +
        "Signature=$signature"

    return buildMap {
        put("Authorization", authorizationHeader)
        put("x-amz-date", amzDate)
        put("x-amz-content-sha256", payloadHash)
        put("host", host)
        putAll(additionalHeaders)
    }
}

private fun deriveSigningKey(
    secretKey: String,
    dateStamp: String,
    region: String,
    service: String
): ByteArray {
    val kDate = hmacSha256(("AWS4$secretKey").encodeToByteArray(), dateStamp.encodeToByteArray())
    val kRegion = hmacSha256(kDate, region.encodeToByteArray())
    val kService = hmacSha256(kRegion, service.encodeToByteArray())
    return hmacSha256(kService, "aws4_request".encodeToByteArray())
}
