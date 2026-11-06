package dev.craftanything.app.core

object NativeBridge {

    /**
     * Retrieves the version string directly from the Rust core engine.
     */
    external fun getEngineVersion(): String

    /**
     * Sends a JSON request payload to the Rust core and returns the response JSON.
     */
    external fun processPayload(input: String): String

    /**
     * Safe wrapper that falls back if native library is not linked.
     */
    fun safeGetVersion(): String {
        return try {
            getEngineVersion()
        } catch (e: UnsatisfiedLinkError) {
            "0.1.0-unlinked"
        }
    }

    /**
     * Safe wrapper for payload processing.
     */
    fun safeProcessPayload(input: String): String {
        return try {
            processPayload(input)
        } catch (e: UnsatisfiedLinkError) {
            """{"status":"Error","data":"Native library unavailable: ${e.message}"}"""
        }
    }
}
