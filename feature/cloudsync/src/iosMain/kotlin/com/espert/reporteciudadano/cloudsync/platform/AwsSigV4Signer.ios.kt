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

actual fun sha256Hex(data: String): String {
    val bytes = data.encodeToByteArray()
    val result = ByteArray(CC_SHA256_DIGEST_LENGTH.toInt())
    bytes.usePinned { pinnedBytes ->
        result.usePinned { pinnedResult ->
            CC_SHA256(pinnedBytes.addressOf(0), bytes.size.convert(), pinnedResult.addressOf(0))
        }
    }
    return result.toHex()
}
