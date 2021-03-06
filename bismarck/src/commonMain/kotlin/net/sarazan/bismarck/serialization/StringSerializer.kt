package net.sarazan.bismarck.serialization

object StringSerializer : Serializer<String> {

    @OptIn(ExperimentalStdlibApi::class)
    override fun deserialize(bytes: ByteArray): String? {
        return bytes.decodeToString()
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun serialize(data: String): ByteArray {
        return data.encodeToByteArray()
    }
}
