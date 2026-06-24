# Changelog

## 1.0.0 — for Minecraft 26.1.x / 26.2

### Added
- **Upgraded Shulker Box**: upgrade any shulker box (Core +1 + shulker box + iron block) into one that can store normal shulker boxes — but not other upgraded boxes, so no infinite nesting. It's a real shulker box (animated lid, keeps its contents when broken), comes in all 16 colours plus the default, preserves colour and contents through the upgrade, and is dyeable like a vanilla shulker box (combine with a dye to recolour).
- Custom purpur Wing Smithing Template texture (white arrow).
- Upgraded shulker boxes can be **washed in a water cauldron** to reset to the default colour (keeps contents, uses one water level), like vanilla shulkers.
- Upgraded shulker box items carry a small gold emblem on the lid so they're recognisable next to vanilla shulkers.

## 0.3.4 — for Minecraft 26.1.x

### Added
- Netherite spears can now be upgraded (added to the upgrade system; they gain +1 attack damage and +25% durability per level, and appear in the JEI category).

## 0.3.3 — for Minecraft 26.1.x

### Added
- **JEI integration**: a "Gear Upgrade" category showing every tier (+1..+10) as its own entry per upgradable item — the Upgrade Core, the item at +(N-1), the catalyst, and the +N result. Requires JEI (optional). Core crafting, combining, and the winged chestplate already show in JEI's built-in categories.
- Each Upgrade Core's tooltip now names the catalyst to combine it with (e.g. "Combine with: Block of Iron").

## 0.3.2 — for Minecraft 26.1.x

### Added
- **Absorbing Sponge**: upgrade a plain sponge (Core +1 + sponge + iron block) into a sponge that drinks lava as well as water, up to +7. It waits dry until liquid first touches it, absorbs once, and turns wet; breaking it always drops the dry sponge back (with its tier), so it is reusable. Water and lava radii grow on alternating levels (lava always smaller); the item is fireproof.
- Custom item texture for the Winged Netherite Chestplate (netherite chestplate over elytra wings).

### Changed
- Reworked upgrade catalysts: +1 iron block, +2 emerald block, +3 gold block, +4 diamond, +5 diamond block, +6 netherite ingot, +7 netherite block, +8 enchanted golden apple, +9 nether star, +10 heavy core.

## 0.3.1 — for Minecraft 26.1.x

### Fixed
- Upgrading swords corrupted the item (rejected inventory clicks, no further upgrades). The sword's vanilla "instantly mines" rule has speed `Float.MAX_VALUE`; the mining-speed bonus overflowed it to Infinity, which fails Minecraft's component validation. Such speeds are now left untouched. Swords upgraded on 0.3.0 should be re-upgraded in the smithing table (this repairs them) or replaced.

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
