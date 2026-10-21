package com.edu.feature.auth.data.fake

import com.edu.core.domain.storage.SecureDataStorage

class FakeSecureDataStorage : SecureDataStorage {
    val values = mutableMapOf<String, Any>()
    var failWrites = false

    override suspend fun putString(key: String, value: String) {
        if (failWrites) throw java.io.IOException("disk")
        values[key] = value
    }
    override suspend fun getString(key: String): String? = values[key] as? String
    override suspend fun putInt(key: String, value: Int) { values[key] = value }
    override suspend fun getInt(key: String): Int? = values[key] as? Int
    override suspend fun remove(key: String) { values.remove(key) }
    override suspend fun clear() { values.clear() }
    override fun contains(key: String): Boolean = values.containsKey(key)
}
