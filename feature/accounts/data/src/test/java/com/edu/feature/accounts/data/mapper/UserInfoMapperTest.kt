package com.edu.feature.accounts.data.mapper

import com.edu.feature.accounts.data.remote.dto.UserInfoDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UserInfoMapperTest {

    private val mapper = UserInfoMapper()

    @Test
    fun `Should trim the full name`() {
        assertEquals("John Doe", mapper.toDomain(UserInfoDto(fullName = " John Doe "))?.fullName)
    }

    @Test
    fun `Should return null When the name is missing`() {
        assertNull(mapper.toDomain(UserInfoDto(fullName = null)))
    }
}
