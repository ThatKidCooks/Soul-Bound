package site.thatkid.soulBound.managers

import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI


class DiscordBot(private val plugin: JavaPlugin, private val uri: String) {

    private var client: WebSocketClient? = null
    private val pluginRef = plugin

    init {
        try {
            client = object : WebSocketClient(URI(uri)) {
                override fun onOpen(handshakedata: ServerHandshake?) {
                    plugin.logger.info("[DiscordBridge] Connected to Discord bot WebSocket!")
                }

                override fun onMessage(message: String?) {
                    message?.let {
                        plugin.logger.info("[DiscordBridge] Received message: $it") // log to see if it works

                        plugin.server.dispatchCommand(plugin.server.consoleSender, message) // command - add some checks later.
                    }
                }

                override fun onClose(code: Int, reason: String?, remote: Boolean) {
                    Bukkit.getLogger().warning("[DiscordBridge] Connection closed: $reason")
                }

                override fun onError(ex: Exception?) {
                    ex?.printStackTrace()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun connect() {
        client?.connect()
    }

    fun disconnect() {
        if (client?.isOpen == true) {
            client?.close()
        }
    }

    fun sendMessage(msg: String) {
        if (client?.isOpen == true) {
            client?.send(msg)
        }
    }
}