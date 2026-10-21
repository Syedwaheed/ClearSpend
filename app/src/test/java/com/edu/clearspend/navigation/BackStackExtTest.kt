package com.edu.clearspend.navigation

import androidx.navigation3.runtime.NavKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BackStackExtTest {

    @Test
    fun `Should replace the whole stack`() {
        val stack = mutableListOf<NavKey>(Route.Splash, Route.Onboarding)

        stack.replaceAll(Route.Dashboard)

        assertEquals(listOf(Route.Dashboard), stack)
    }

    @Test
    fun `Should pop back to an existing route`() {
        val stack = mutableListOf<NavKey>(Route.ConnectBank, Route.BankSelection, Route.Connecting)

        stack.popTo(Route.ConnectBank)

        assertEquals(listOf<NavKey>(Route.ConnectBank), stack)
    }

    @Test
    fun `Should make the route the root When popping to a missing route`() {
        val stack = mutableListOf<NavKey>(Route.Splash, Route.Connecting)

        stack.popTo(Route.ConnectBank)

        assertEquals(listOf<NavKey>(Route.ConnectBank), stack)
    }

    @Test
    fun `Should keep Home as root When selecting another tab`() {
        val stack = mutableListOf<NavKey>(Route.Dashboard)

        stack.selectTab(Route.Transactions)

        assertEquals(listOf<NavKey>(Route.Dashboard, Route.Transactions), stack)
    }

    @Test
    fun `Should highlight the tab under the Send sheet`() {
        val stack = listOf<NavKey>(Route.Dashboard, Route.Accounts, Route.SendMoney)

        assertEquals(1, stack.currentTabIndex())
    }

    @Test
    fun `Should show the bottom nav only on main tabs`() {
        assertTrue(listOf<NavKey>(Route.Dashboard).showsBottomNav())
        assertFalse(listOf<NavKey>(Route.ConnectBank).showsBottomNav())
        assertFalse(listOf<NavKey>(Route.Dashboard, Route.Settings).showsBottomNav())
    }
}
