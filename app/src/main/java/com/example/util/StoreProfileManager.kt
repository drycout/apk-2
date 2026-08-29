package com.example.util

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.StoreProfile

class StoreProfileManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("djandes_store_profile_prefs", Context.MODE_PRIVATE)

    fun getStoreProfile(): StoreProfile {
        return StoreProfile(
            name = prefs.getString("store_name", "Djandes") ?: "Djandes",
            tagline = prefs.getString("store_tagline", "Sweet & Savoury") ?: "Sweet & Savoury",
            description = prefs.getString(
                "store_desc",
                "DJANDES adalah home made kue basah lokal yang menyajikan berbagai macam kue tradisional dan modern dengan cita rasa autentik dan kualitas terbaik."
            ) ?: "",
            logo = prefs.getString(
                "store_logo",
                "https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/logo.png"
            ) ?: "https://cdn.jsdelivr.net/gh/jandess/katalog@main/img/logo.png",
            whatsapp = prefs.getString("store_whatsapp", "6285812006225") ?: "6285812006225",
            instagram = prefs.getString("store_instagram", "djandes15") ?: "djandes15",
            tiktok = prefs.getString("store_tiktok", "djandes15") ?: "djandes15",
            address = run {
                val savedAddr = prefs.getString("store_address", null)
                if (savedAddr == null || savedAddr == "Jl. Anggrek, RT 004 / RW 013, Tegalrejo, Sawentar, Kanigoro, Blitar") {
                    "Jl. Anggrek RT 004 / RW 013, Tegalrejo - Sawentar, Kanigoro - Blitar"
                } else {
                    savedAddr
                }
            },
            receiptGreeting = prefs.getString(
                "receipt_greeting",
                "Terima kasih atas pesanan Anda. Simpan nota ini saat pengambilan."
            ) ?: "Terima kasih atas pesanan Anda. Simpan nota ini saat pengambilan.",
            showReceiptGreeting = prefs.getBoolean("show_receipt_greeting", true),
            showSocialMedia = prefs.getBoolean("show_social_media", true),
            showNotesOnReceipt = prefs.getBoolean("show_notes_on_receipt", true)
        )
    }

    fun saveStoreProfile(profile: StoreProfile) {
        prefs.edit()
            .putString("store_name", profile.name)
            .putString("store_tagline", profile.tagline)
            .putString("store_desc", profile.description)
            .putString("store_logo", profile.logo)
            .putString("store_whatsapp", profile.whatsapp)
            .putString("store_instagram", profile.instagram)
            .putString("store_tiktok", profile.tiktok)
            .putString("store_address", profile.address)
            .putString("receipt_greeting", profile.receiptGreeting)
            .putBoolean("show_receipt_greeting", profile.showReceiptGreeting)
            .putBoolean("show_social_media", profile.showSocialMedia)
            .putBoolean("show_notes_on_receipt", profile.showNotesOnReceipt)
            .apply()
    }
}
