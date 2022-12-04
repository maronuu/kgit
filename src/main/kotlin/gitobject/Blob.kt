package gitobject

import java.security.MessageDigest

class Blob(val content: String) {
    val size = content.length

    fun toByteArray(): ByteArray {
        val header = "blob ${this.size}\\0"
        val body = this.content
        return "${header}${body}".toByteArray()
    }

    fun calcHash(): ByteArray {
        return MessageDigest.getInstance("SHA-1").digest(toByteArray())
    }
}

class BlobBuilder {
    fun build(content: String): Blob {
        return Blob(content)
    }
    fun build(bytes: ByteArray): Blob {
        return Blob(bytes.decodeToString())
    }
}
