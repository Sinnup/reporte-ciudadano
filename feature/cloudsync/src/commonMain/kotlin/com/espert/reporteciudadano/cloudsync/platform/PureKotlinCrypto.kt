package com.espert.reporteciudadano.cloudsync.platform

/**
 * Pure-Kotlin SHA-256 and HMAC-SHA256 implementations.
 *
 * Used on JS where javax.crypto is unavailable and SubtleCrypto is async.
 * These are correct implementations but not hardware-accelerated.
 *
 * Reference: FIPS PUB 180-4 / RFC 2104
 */
internal object PureKotlinSha256 {

    private val K = intArrayOf(
        0x428a2f98.toInt(), 0x71374491.toInt(), 0xb5c0fbcf.toInt(), 0xe9b5dba5.toInt(),
        0x3956c25b.toInt(), 0x59f111f1.toInt(), 0x923f82a4.toInt(), 0xab1c5ed5.toInt(),
        0xd807aa98.toInt(), 0x12835b01.toInt(), 0x243185be.toInt(), 0x550c7dc3.toInt(),
        0x72be5d74.toInt(), 0x80deb1fe.toInt(), 0x9bdc06a7.toInt(), 0xc19bf174.toInt(),
        0xe49b69c1.toInt(), 0xefbe4786.toInt(), 0x0fc19dc6.toInt(), 0x240ca1cc.toInt(),
        0x2de92c6f.toInt(), 0x4a7484aa.toInt(), 0x5cb0a9dc.toInt(), 0x76f988da.toInt(),
        0x983e5152.toInt(), 0xa831c66d.toInt(), 0xb00327c8.toInt(), 0xbf597fc7.toInt(),
        0xc6e00bf3.toInt(), 0xd5a79147.toInt(), 0x06ca6351.toInt(), 0x14292967.toInt(),
        0x27b70a85.toInt(), 0x2e1b2138.toInt(), 0x4d2c6dfc.toInt(), 0x53380d13.toInt(),
        0x650a7354.toInt(), 0x766a0abb.toInt(), 0x81c2c92e.toInt(), 0x92722c85.toInt(),
        0xa2bfe8a1.toInt(), 0xa81a664b.toInt(), 0xc24b8b70.toInt(), 0xc76c51a3.toInt(),
        0xd192e819.toInt(), 0xd6990624.toInt(), 0xf40e3585.toInt(), 0x106aa070.toInt(),
        0x19a4c116.toInt(), 0x1e376c08.toInt(), 0x2748774c.toInt(), 0x34b0bcb5.toInt(),
        0x391c0cb3.toInt(), 0x4ed8aa4a.toInt(), 0x5b9cca4f.toInt(), 0x682e6ff3.toInt(),
        0x748f82ee.toInt(), 0x78a5636f.toInt(), 0x84c87814.toInt(), 0x8cc70208.toInt(),
        0x90befffa.toInt(), 0xa4506ceb.toInt(), 0xbef9a3f7.toInt(), 0xc67178f2.toInt()
    )

    private val H0 = intArrayOf(
        0x6a09e667.toInt(), 0xbb67ae85.toInt(), 0x3c6ef372.toInt(), 0xa54ff53a.toInt(),
        0x510e527f.toInt(), 0x9b05688c.toInt(), 0x1f83d9ab.toInt(), 0x5be0cd19.toInt()
    )

    fun hash(message: ByteArray): ByteArray {
        val padded = pad(message)
        val h = H0.copyOf()
        val w = IntArray(64)

        var i = 0
        while (i < padded.size) {
            for (j in 0..15) {
                w[j] = ((padded[i + j * 4].toInt() and 0xff) shl 24) or
                       ((padded[i + j * 4 + 1].toInt() and 0xff) shl 16) or
                       ((padded[i + j * 4 + 2].toInt() and 0xff) shl 8) or
                       (padded[i + j * 4 + 3].toInt() and 0xff)
            }
            for (j in 16..63) {
                val s0 = rotr(w[j - 15], 7) xor rotr(w[j - 15], 18) xor (w[j - 15] ushr 3)
                val s1 = rotr(w[j - 2], 17) xor rotr(w[j - 2], 19) xor (w[j - 2] ushr 10)
                w[j] = w[j - 16] + s0 + w[j - 7] + s1
            }
            var (a, b, c, d, e, f, g, hh) = h
            for (j in 0..63) {
                val S1 = rotr(e, 6) xor rotr(e, 11) xor rotr(e, 25)
                val ch = (e and f) xor (e.inv() and g)
                val temp1 = hh + S1 + ch + K[j] + w[j]
                val S0 = rotr(a, 2) xor rotr(a, 13) xor rotr(a, 22)
                val maj = (a and b) xor (a and c) xor (b and c)
                val temp2 = S0 + maj
                hh = g; g = f; f = e; e = d + temp1; d = c; c = b; b = a; a = temp1 + temp2
            }
            h[0] += a; h[1] += b; h[2] += c; h[3] += d
            h[4] += e; h[5] += f; h[6] += g; h[7] += hh
            i += 64
        }

        val result = ByteArray(32)
        for (j in 0..7) {
            result[j * 4] = (h[j] shr 24).toByte()
            result[j * 4 + 1] = (h[j] shr 16).toByte()
            result[j * 4 + 2] = (h[j] shr 8).toByte()
            result[j * 4 + 3] = h[j].toByte()
        }
        return result
    }

    fun hex(message: ByteArray): String = hash(message).toHex()

    private fun pad(message: ByteArray): ByteArray {
        val bitLength = message.size.toLong() * 8
        val paddingLength = ((55 - message.size) % 64 + 64) % 64 + 1
        val padded = ByteArray(message.size + paddingLength + 8)
        message.copyInto(padded)
        padded[message.size] = 0x80.toByte()
        for (i in 0..7) {
            padded[padded.size - 8 + i] = ((bitLength shr (56 - i * 8)) and 0xff).toByte()
        }
        return padded
    }

    private fun rotr(x: Int, n: Int): Int = (x ushr n) or (x shl (32 - n))

    private operator fun IntArray.component1() = this[0]
    private operator fun IntArray.component2() = this[1]
    private operator fun IntArray.component3() = this[2]
    private operator fun IntArray.component4() = this[3]
    private operator fun IntArray.component5() = this[4]
    private operator fun IntArray.component6() = this[5]
    private operator fun IntArray.component7() = this[6]
    private operator fun IntArray.component8() = this[7]
}

internal object PureKotlinHmacSha256 {
    private const val BLOCK_SIZE = 64

    fun compute(key: ByteArray, data: ByteArray): ByteArray {
        val normalizedKey = if (key.size > BLOCK_SIZE) PureKotlinSha256.hash(key) else key
        val paddedKey = ByteArray(BLOCK_SIZE).also { normalizedKey.copyInto(it) }

        val ipad = ByteArray(BLOCK_SIZE) { (paddedKey[it].toInt() xor 0x36).toByte() }
        val opad = ByteArray(BLOCK_SIZE) { (paddedKey[it].toInt() xor 0x5c).toByte() }

        val innerHash = PureKotlinSha256.hash(ipad + data)
        return PureKotlinSha256.hash(opad + innerHash)
    }
}
