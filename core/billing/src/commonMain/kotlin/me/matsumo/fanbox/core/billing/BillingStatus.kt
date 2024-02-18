package me.matsumo.fanbox.core.billing

interface BillingStatus {
    fun init()
    fun update()
    fun finish()
}
