# Extended Vanilla Endgame (EVE)

![Minecraft 26.1.x](https://img.shields.io/badge/Minecraft-26.1.x-brightgreen) ![Fabric](https://img.shields.io/badge/Loader-Fabric-blue) ![Java 25](https://img.shields.io/badge/Java-25-orange)

A Fabric mod for Minecraft **26.1.2** that keeps the game exactly vanilla until the endgame, then lets you upgrade netherite tools and armor to **+1 through +10** — with costs that roughly double every level.

## How it works

### 1. Upgrade Cores (one per level, consumed on use)

The +1 core is crafted directly:

```
[Diamond Block]  [Crying Obsidian] [Iron Block]
[Crying Obsidian] [Netherite Ingot] [Crying Obsidian]
[Gold Block]     [Crying Obsidian] [Emerald Block]
```

Every higher core is simply **two cores of the previous tier combined** (shapeless, no extra item), so the raw material cost doubles each level — a +10 core is worth 512 +1 cores.

![Upgrade Core +1 recipe](docs/recipe_core1.png)
![Combining cores](docs/recipe_combine.png)

### 2. Upgrade in the smithing table

Smithing table: `Upgrade Core +N` + `your +(N-1) item` + `catalyst`:

| Level | Catalyst |
|-------|----------|
| +1 | Diamond Block |
| +2 | Netherite Ingot |
| +3 | Netherite Block |
| +4 | Nether Star |
| +5 | Heavy Core |
| +6 | Netherite Block |
| +7 | Nether Star |
| +8 | Heavy Core |
| +9 | Nether Star |
| +10 | Heavy Core |

![Applying an upgrade](docs/recipe_apply.png)

Each level grants (cumulative): **+1 attack damage**, **+1 armor**, **+0.5 armor toughness**, **+20% mining speed**, **+25% durability**. Enchantments and damage are preserved.

![Core tier textures](docs/core_preview2.png)

Upgradable: netherite sword/pickaxe/axe/shovel/hoe/helmet/leggings/boots and the Winged Netherite Chestplate (tag `eve:upgradable` — datapacks can extend it).

### 3. The chestplate gate

A plain netherite chestplate **cannot** be upgraded. First craft a **Wing Smithing Template**:

```
[Phantom Membrane] [Netherite Ingot] [Phantom Membrane]
[Netherite Ingot]  [Nether Star]     [Netherite Ingot]
[Phantom Membrane] [Netherite Ingot] [Phantom Membrane]
```

![Wing Smithing Template recipe](docs/recipe_wing_template.png)

Then in the smithing table: `Wing Template` + `Netherite Chestplate` + `Elytra` → **Winged Netherite Chestplate** — full netherite protection *and* elytra flight (vanilla glider component), and it's the only chestplate that accepts +N upgrades.

![Winged Netherite Chestplate recipe](docs/recipe_winged_chestplate.png)

## Building

```
./gradlew build
```

Output: `build/libs/extended-vanilla-endgame-<version>.jar`. Requires Java 25, Fabric Loader ≥ 0.19.3 and Fabric API. Gradle runs on Java 25 (path pinned in `gradle.properties` via `org.gradle.java.home`; adjust for your machine). Uses Mojang official mappings (Yarn was discontinued after 1.21.11).

## TODO

- Custom textures for the wing template and winged chestplate (upgrade cores now have per-tier recolored textures; the other two still reuse vanilla art)
- Elytra wings back-rendering for the winged chestplate (flight works, wings just don't show)
