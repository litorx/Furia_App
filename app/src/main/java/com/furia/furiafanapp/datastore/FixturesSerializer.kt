package com.furia.furiafanapp.datastore

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream

object FixturesSerializer : Serializer<Fixtures> {
    override val defaultValue: Fixtures = Fixtures.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): Fixtures {
        try {
            return Fixtures.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: Fixtures, output: OutputStream) = t.writeTo(output)
}
