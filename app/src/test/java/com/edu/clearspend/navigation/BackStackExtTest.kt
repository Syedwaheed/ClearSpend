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

class TabBackStackTest {

    private val histories: Map<Route, MutableList<NavKey>> = mapOf(
        Route.Dashboard to mutableListOf(Route.Dashboard),
        Route.Accounts to mutableListOf(Route.Accounts),
        Route.Transactions to mutableListOf(Route.Transactions),
        Route.Analytics to mutableListOf(Route.Analytics),
    )

    @Test
    fun `Should treat Home as the tab When only Home and a pushed screen are showing`() {
        assertEquals(Route.Dashboard, listOf<NavKey>(Route.Dashboard, Route.Card).currentTab())
    }

    @Test
    fun `Should find the tab beneath a pushed screen`() {
        assertEquals(Route.Accounts, listOf<NavKey>(Route.Dashboard, Route.Accounts, Route.AccountDetail("a")).currentTab())
    }

    @Test
    fun `Should restore a tab's own back stack When switching back to it`() {
        val stack = mutableListOf<NavKey>(Route.Dashboard, Route.Accounts, Route.AccountDetail("a"))

        stack.switchTab(Route.Dashboard, histories)
        stack.switchTab(Route.Accounts, histories)

        assertEquals(listOf<NavKey>(Route.Dashboard, Route.Accounts, Route.AccountDetail("a")), stack)
    }

    @Test
    fun `Should restore Home's back stack When switching back to Home`() {
        val stack = mutableListOf<NavKey>(Route.Dashboard, Route.Card)

        stack.switchTab(Route.Accounts, histories)
        stack.switchTab(Route.Dashboard, histories)

        assertEquals(listOf<NavKey>(Route.Dashboard, Route.Card), stack)
    }

    @Test
    fun `Should pop to the tab root When the showing tab is re-selected`() {
        val stack = mutableListOf<NavKey>(Route.Dashboard, Route.Accounts, Route.AccountDetail("a"))

        stack.switchTab(Route.Accounts, histories)

        assertEquals(listOf<NavKey>(Route.Dashboard, Route.Accounts), stack)
    }

    @Test
    fun `Should not save the Send sheet into a tab's history`() {
        val stack = mutableListOf<NavKey>(Route.Dashboard, Route.Accounts, Route.SendMoney)

        stack.switchTab(Route.Transactions, histories)

        assertEquals(listOf<NavKey>(Route.Accounts), histories.getValue(Route.Accounts))
    }

    @Test
    fun `Should open a tab fresh When it has no history`() {
        val stack = mutableListOf<NavKey>(Route.Dashboard)

        stack.switchTab(Route.Analytics, histories)

        assertEquals(listOf<NavKey>(Route.Dashboard, Route.Analytics), stack)
    }
}
