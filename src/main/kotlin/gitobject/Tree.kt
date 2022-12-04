package gitobject

import java.security.MessageDigest


class Entry(
    val fileType: Int,
    val name: String,
    // tree hash is saved as byteArray
    val hash: ByteArray,
) {
    fun toByteArray(): ByteArray {
        val header = "$fileType ${name}\\0"
        return header.toByteArray() + hash
    }
}

class EntryBuilder {
    fun build(header: ByteArray, hash: ByteArray): Entry {
        // header: fileType name
        val fileTypeAndName = header.decodeToString().split("\\s+")
        require(fileTypeAndName.size == 2) { "Invalid entry header format." }
        val fileType = fileTypeAndName[0].toInt()
        val name = fileTypeAndName[1]

        return Entry(fileType, name, hash)
    }
}
class Tree(val entries: List<Entry>) {
    fun toByteArray(): ByteArray {
        val body = this.entries
            .flatMap { it.toByteArray().asIterable() }
            .toByteArray()
//        val body = this.entries
//            .map { it.toByteArray() }
//            .reduce { acc, bytes -> acc + bytes }
        val header = "tree ${body.size}\\0"
        return header.toByteArray() + body
    }

    fun calcHash(): ByteArray {
        return MessageDigest.getInstance("SHA-1").digest(toByteArray())
    }
}

class TreeBuilder {
    fun build(bytes: ByteArray): Tree {
        val byteEntries = bytes
            .decodeToString()
            .split("\\0")
            .map { it.toByteArray() }
        // h1 /0 v1 h2 /0 v2 h3 /0 ...
        var curHeader = byteEntries[0]
        val entries = byteEntries.subList(1, byteEntries.size).map {
            val hash = it.sliceArray(0 until 20)
            val nextHeader = it.sliceArray(20 until it.size)
            val entry = EntryBuilder().build(curHeader.clone(), hash)
            // (v_i, h_{i+1}) -> (v_{i+1}, h_{i+2})
            curHeader = nextHeader
            entry
        }
        return Tree(entries)
    }
}