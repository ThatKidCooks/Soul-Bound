package site.thatkid.soulBound.items
import net.kyori.adventure.text.Component

object ItemRegistry {
    val items = listOf(
        CustomItem(
            id = Component.text("aquatic_heart"),
            modelData = 1,
            displayName = Component.text("§bAquatic Heart"),
            lore = listOf(
                Component.text("§7Born from the ocean. Swam away."),
                Component.text(""),
                Component.text("§f✧ §7Permanent §bDolphin’s Grace §7& §9Conduit Power"),
                Component.text(""),
                Component.text("§3§lPower — Tidal Surge"),
                Component.text("§7If in water, gain §cStrength III §7for §f5s"),
                Component.text("§8Cooldown: 100 seconds")
            )
        ),
        CustomItem(
            id = Component.text("crowned_heart"),
            modelData = 2,
            displayName = Component.text("§aCrowned Heart"),
            lore = listOf(
                Component.text("§7A heart split between two souls."),
                Component.text("§7Fueled by conflict and loyalty."),
                Component.text(""),
                Component.text("§f✧ §7Permanent §fSpeed I"),
                Component.text(""),
                Component.text("§a§lPower — Smash"),
                Component.text("§7Damages and blasts away enemies within §e6 blocks"),
                Component.text("§7Deals §c3 hearts§7 to mobs and players."),
                Component.text("§8Cooldown: 20 seconds")
            )
        ),
        CustomItem(
            id = Component.text("fire_heart"),
            modelData = 3,
            displayName = Component.text("§cFire Heart"),
            lore = listOf(
                Component.text("§7Few can withstand the heart of fire."),
                Component.text("§cThe Nether will not hand it to you — it will burn away the unworthy."),
                Component.text(""),
                Component.text("§f✧ §7Permanent §cFire Resistance §7& §9Strength when on Fire"),
                Component.text(""),
                Component.text("§3§lPower — Lava Surge"),
                Component.text("§cLaunch Enemies into the Air & Set the Ground on Fire"),
                Component.text("§8Cooldown: 100 seconds")
            )
        ),
        CustomItem(
            id = Component.text("frozen_heart"),
            modelData = 4,
            displayName = Component.text("§1Frozen Heart"),
            lore = listOf(
                Component.text("§7Born in the Icy Lakes"),
                Component.text(""),
                Component.text("§f✧ §7Permanent §7Freeze Resistance §7& §7and a 10% chance to"),
                Component.text("§7freeze an entity on hit for §f5 seconds"),
                Component.text(""),
                Component.text("§3§lPower — Frozen Surge"),
                Component.text("§cFreeze all nearby entities making it so they can't jump for §f10 seconds"),
                Component.text("§8Cooldown: 100 seconds")
            )
        ),
        CustomItem(
            id = Component.text("ghastly_heart"),
            modelData = 5,
            displayName = Component.text("§5Ghastly Heart"),
            lore = listOf(
                Component.text("§7Born from fire, hidden in smoke..."),
                Component.text(""),
                Component.text("§f✧ §7Permanent §fInvisibility"),
                Component.text("§7  (does not hide armor)"),
                Component.text(""),
                Component.text("§5§lPower — Phantom Veil"),
                Component.text("§7Become §finvisible (true)§7 and gain §bSpeed II§7 for §f15s"),
                Component.text("§7Armor hidden. Entity hidden."),
                Component.text("§8Cooldown: 45 seconds")
            )
        ),
        CustomItem(
            id = Component.text("golem_heart"),
            modelData = 6,
            displayName = Component.text("§7Golem Heart"),
            lore = listOf(
                Component.text("§7Forged from iron and determination."),
                Component.text(""),
                Component.text("§f✧ §7Permanent §fResistance I"),
                Component.text(""),
                Component.text("§7§lPower — Iron Might"),
                Component.text("§7Slam the ground, knocking back enemies"),
                Component.text("§7and gaining §cStrength II §7and §fResistance II §7for §f10s"),
                Component.text("§7Also grants §fknockback immunity §7for §f10s"),
                Component.text("§8Cooldown: 90 seconds")
            )
        ),
        CustomItem(
            id = Component.text("haste_heart"),
            modelData = 7,
            displayName = Component.text("§6Haste Heart"),
            lore = listOf(
                Component.text("§7Obtained by mining 5000 deepslate blocks!"),
                Component.text("§7Grants constant Haste I."),
                Component.text("§aAbility:"),
                Component.text("§f- Haste Surge: Speed III & Haste III for 40s"),
                Component.text("§f- Breaks a 3x3 cube around you"),
                Component.text("§8Cooldown: 90 seconds")
            )
        ),
        CustomItem(
            id = Component.text("speed_heart"),
            modelData = 8,
            displayName = Component.text("§eSpeed Heart"),
            lore = listOf(
                Component.text("§7Born from the wind. Runs endlessly."),
                Component.text(""),
                Component.text("§f✧ §7Permanent §bSpeed II"),
                Component.text(""),
                Component.text("§a§lPower — Lightning Dash"),
                Component.text("§7Dash forward and gain"),
                Component.text("§7§fSpeed IV §7and §eJump Boost II §7for §f10s"),
                Component.text("§8Cooldown: 60 seconds")
            )
        ),
        CustomItem(
            id = Component.text("strength_heart"),
            modelData = 9,
            displayName = Component.text("§4Strength Heart"),
            lore = listOf(
                Component.text("§7Kill 10 players to earn this."),
                Component.text(""),
                Component.text("§f✧ §7Permanent §cStrength I"),
                Component.text(""),
                Component.text("§4§lPower — Unstoppable Force"),
                Component.text("§7Gain §cStrength II §7and §bSpeed II §7for §f15s"),
                Component.text("§8Cooldown: 60 seconds")
            )
        ),
        CustomItem(
            id = Component.text("trader_heart"),
            modelData = 10,
            displayName = Component.text("§2Trader Heart"),
            lore = listOf(
                Component.text("§7Unlock by earning the"),
                Component.text("§fHero of the Village §7advancement."),
                Component.text(""),
                Component.text("§f✧ §7Permanent §aHero of the Village I"),
                Component.text(""),
                Component.text("§2§lPower — Royal Bargain"),
                Component.text("§7Grants §aHero of the Village 255 §7for §f100s"),
                Component.text("§8Cooldown: 2m 30s")
            )
        ),
        CustomItem(
            id = Component.text("warden_heart"),
            modelData = 11,
            displayName = Component.text("§1Warden Heart"),
            lore = listOf(
                Component.text("§7Unlock by defeating the"),
                Component.text("§fWarden §7without dying."),
                Component.text(""),
                Component.text("§f✧ §7Nearby mobs tremble in your presence."),
                Component.text("§f✧ §7Darkness creeps into the hearts of enemies."),
                Component.text(""),
                Component.text("§9§lPower — Sonic Pulse"),
                Component.text("§7Unleash a §bSonic Boom §7that damages and knocks back enemies"),
                Component.text("§8Cooldown: 2m 30s")
            )
        ),
        CustomItem(
            id = Component.text("wise_heart"),
            modelData = 12,
            displayName = Component.text("§fWise Heart"),
            lore = listOf(
                Component.text("§7Wise doesn't mean old."),
                Component.text(""),
                Component.text("§f✧ §7Permanent §aHealth Boost"),
                Component.text(""),
                Component.text("§6§lPower — Arcane Insight"),
                Component.text("§7Reveal nearby players with §eGlowing§7,"),
                Component.text("§7Gain §dRegeneration II §7and §eAbsorption II §7for §f10s"),
                Component.text("§8Cooldown: 3 minutes")
            )
        ),
        CustomItem(
            id = Component.text("wither_heart"),
            modelData = 13,
            displayName = Component.text("§8Wither Heart"),
            lore = listOf(
                Component.text("§7A heart that carries the burden of decay."),
                Component.text("§7It withers the soul, but grants power."),
                Component.text(""),
                Component.text("§f✧ §710% chance §fto inflict Wither I §1on hit"),
                Component.text(""),
                Component.text("§a§lPower — Wither Blast"),
                Component.text("§7Unleashes a blast of withering energy"),
                Component.text("§7that shoots wither §1heads in the direction you are facing."),
                Component.text("§8Cooldown: 30 seconds")
            )
        ),
    ).associateBy { it.modelData }
}