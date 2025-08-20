package site.thatkid.soulBound.managers

import org.bukkit.plugin.java.JavaPlugin
import java.net.HttpURLConnection
import java.net.URL


class DiscordBot(private val plugin: JavaPlugin) {

    fun sendToDiscord(message: String) {
        try {
            val url: URL = URL("http://BOT_IP:3000/mc") // change to BOT_IP + port - add a config in a .yml file maybe
            val conn: HttpURLConnection = url.openConnection() as HttpURLConnection // Open a connection to the URL
            conn.setRequestProperty("User-Agent", "Mozilla/5.0") // Set a user agent to avoid issues with some servers with a firewall that blocks non browser requests
            conn.connectTimeout = 5000 // Set a timeout for the connection
            conn.readTimeout = 5000 // Set a timeout for reading the response
            conn.setRequestMethod("POST")
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setDoOutput(true)

            val json = "{\"content\":\"" + message + "\"}"
            conn.getOutputStream().use { os ->
                os.write(json.toByteArray())
            }
            conn.getResponseCode() // triggers the request
            conn.disconnect()
        } catch (error: Exception) {

            error.printStackTrace()
        }
    }
}