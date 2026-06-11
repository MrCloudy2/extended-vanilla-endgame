# Changelog

## 0.3.0 — for Minecraft 26.1.x

### Added
- Per-tier textures for all 10 Upgrade Cores, recolored from the vanilla netherite upgrade template with a gold arrow
- Tier ladder: weathered stone, copper, gold (silver arrow), emerald, diamond, lapis, amethyst, redstone, netherite (ember arrow) — with sparkle glints appearing from +6 and multiplying upward
- **+10 is animated**: a 12-frame iridescent shimmer cycling through every hue (vanilla resource-pack animation, no code)

### Changed
- Upgrade Core +1 recipe takes one each of diamond, iron, gold and emerald block (was 4 diamond blocks)
- Combining cores is now catalyst-free: two cores of a tier = one core of the next

## 0.2.0 — for Minecraft 26.1.x

### Updated
- Ported to **Minecraft 26.1.2** (Fabric Loader ≥ 0.19.3, Fabric API, Java 25)
- Migrated to Mojang official mappings (Yarn ended with 1.21.11)
- The Winged Netherite Chestplate now uses the vanilla glider component — flight behaves exactly like an elytra

### Added
- Upgrade cap raised from **+5 to +10**
- Tiered Upgrade Cores (+1 to +10): combine two cores of the same tier into one of the next — no extra items needed, cost doubles every level (a +10 core is worth 512 +1 cores)

### Changed
- Upgrade Core +1 recipe now takes one each of **diamond, iron, gold and emerald block** in the corners (was 4 diamond blocks), still with crying obsidian and a netherite ingot
- Applying an upgrade still needs a per-level catalyst in the smithing table, now up to +10: diamond block → netherite ingot → netherite block → nether star → heavy core → netherite block → nether star → heavy core → nether star → heavy core

### Known issues
- Items reuse vanilla placeholder textures
- Elytra wings don't render on your back while gliding with the Winged Netherite Chestplate (flight works fine)

## 0.1.0 — for Minecraft 1.21.1

Initial release.

- Game stays 100% vanilla until you craft the endgame items
- Upgrade netherite tools and armor to **+1 through +5** in the smithing table using a craftable Upgrade Core plus an escalating catalyst
- Each level adds +1 attack damage, +1 armor, +0.5 toughness, +20% mining speed, +25% durability — enchantments and damage preserved
- Netherite chestplate must first be fused with an elytra (Wing Smithing Template + chestplate + elytra) into the **Winged Netherite Chestplate** — full netherite protection with elytra flight — before it can be upgraded
