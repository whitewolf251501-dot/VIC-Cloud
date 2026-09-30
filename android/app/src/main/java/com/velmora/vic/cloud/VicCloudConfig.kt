package com.velmora.vic.cloud

/**
 * Public client configuration for VIC Cloud connectivity.
 * Note: Strictly contains ONLY the public Supabase URL and public Anonymous Key.
 * Service-role keys and backend secrets are NEVER exposed on the mobile client.
 */
object VicCloudConfig {
    const val SUPABASE_URL = "https://zyuawmhmexouvrrcpsli.supabase.co"
    const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Inp5dWF3bWhtZXhvdXZycmNwc2xpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3OTA3MDcwMTAsImV4cCI6MjEwNjI4MzAxMH0.Ww5VsfxnjSUHPFuQVXstdYDURf0QvssQsihUfdCHGAQ"
    const val DEFAULT_DESKTOP_DEVICE_ID = "desktop-windows-primary"
}
