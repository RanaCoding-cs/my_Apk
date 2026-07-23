package com.example.security

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object VaultCryptoManager {
    private const val ALGORITHM = "AES/CBC/PKCS5Padding"

    data class EncryptedResult(
        val cipherTextBase64: String,
        val ivBase64: String
    )

    private fun deriveKey(pinOrMasterKey: String): SecretKeySpec {
        val md = MessageDigest.getInstance("SHA-256")
        val keyBytes = md.digest(pinOrMasterKey.toByteArray(Charsets.UTF_8))
        return SecretKeySpec(keyBytes, "AES")
    }

    fun encrypt(plainText: String, pinOrMasterKey: String): EncryptedResult {
        val secretKey = deriveKey(pinOrMasterKey)
        val cipher = Cipher.getInstance(ALGORITHM)

        val ivBytes = ByteArray(16)
        SecureRandom().nextBytes(ivBytes)
        val ivSpec = IvParameterSpec(ivBytes)

        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec)
        val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

        return EncryptedResult(
            cipherTextBase64 = Base64.encodeToString(encryptedBytes, Base64.NO_WRAP),
            ivBase64 = Base64.encodeToString(ivBytes, Base64.NO_WRAP)
        )
    }

    fun decrypt(cipherTextBase64: String, ivBase64: String, pinOrMasterKey: String): String {
        return try {
            val secretKey = deriveKey(pinOrMasterKey)
            val cipher = Cipher.getInstance(ALGORITHM)

            val cipherTextBytes = Base64.decode(cipherTextBase64, Base64.NO_WRAP)
            val ivBytes = Base64.decode(ivBase64, Base64.NO_WRAP)
            val ivSpec = IvParameterSpec(ivBytes)

            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)
            val decryptedBytes = cipher.doFinal(cipherTextBytes)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            "*** Decryption Error: Invalid PIN or Key ***"
        }
    }

    fun hashPin(pin: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest("SALT_DEV_WORKSPACE_$pin".toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
