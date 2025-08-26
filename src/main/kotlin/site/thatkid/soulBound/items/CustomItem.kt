package site.thatkid.soulBound.items
import org.bukkit.Material
import net.kyori.adventure.text.Component
data class CustomItem (
    val id: Component,
    val type: Component = Component.text("normal"),
    val modelData: Int,
    val displayName: Component,
    val lore: List<Component>,
    val material: Material = Material.APPLE,
)