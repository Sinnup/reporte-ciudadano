package com.espert.reporteciudadano.cloudsync.platform

import kotlinx.cinterop.*
import platform.CoreCrypto.*

actual fun hmacSha256(key: ByteArray, data: ByteArray): ByteArray {
    val result = ByteArray(CC_SHA256_DIGEST_LENGTH.toInt())
    key.usePinned { pinnedKey ->
        data.usePinned { pinnedData ->
            result.usePinned { pinnedResult ->
                CCHmac(
                    algorithm = kCCHmacAlgSHA256,
                    key = pinnedKey.addressOf(0),
                    keyLength = key.size.convert(),
                    data = pinnedData.addressOf(0),
                    dataLength = data.size.convert(),
                    macOut = pinnedResult.addressOf(0)
                )
            }
        }
    }
    return result
}

actual fun sha256Hex(data: String): String = sha256Hex(data.encodeToByteArray())

actual fun sha256Hex(data: ByteArray): String {
    val result = ByteArray(CC_SHA256_DIGEST_LENGTH.toInt())
    data.usePinned { pinnedData ->
        result.usePinned { pinnedResult ->
            CC_SHA256(pinnedData.addressOf(0), data.size.convert(), pinnedResult.addressOf(0))
        }
    }
    return result.toHex()
}
