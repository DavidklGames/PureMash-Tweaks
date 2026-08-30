# ⚡ PureMash Tweaks

![Minecraft Version](https://img.shields.io/badge/Minecraft-1.21.1%20%2F%2026.1.2-brightgreen?style=for-the-badge&logo=minecraft)
![Loader](https://img.shields.io/badge/Loader-NeoForge-orange?style=for-the-badge)
![License](https://img.shields.io/badge/License-MIT-blue?style=for-the-badge)

**PureMash Tweaks** is a feature-rich endgame automation, compression, and logistics expansion mod for Minecraft 26.1.2 (NeoForge). Designed specifically for expert modpacks and high-tier automation lines, it provides ultra-large crafting, multi-mode resource condensing, thermodynamic processing, and chunk loading.

---

## 🛠️ Key Features

### 🌌 9x9 Synthesis Table
An automated, transaction-safe 9x9 crafting matrix with memory card recipe programming and digital logistics support.
* **Memory Card Programming**: Save complex 9x9 patterns to Memory Cards.
* **Liquid Glass Projection**: Renders translucent ghost item projections on the crafting grid when configured.
* **Transaction Safety**: Integrated with NeoForge Transfer API & Snapshot Journals. If an external logistics pipe fails mid-craft, the table performs an instantaneous rollback to prevent inventory clogging.
* **Recipe Toggling**: Built-in "Change" button allows cycling through conflicting vanilla and modded recipe outputs.

### 🌀 Multifunctional Compressor
A heavy-duty multi-mode machine that adapts to 3 distinct manufacturing processes:
* **Compression (Mode 0)**: Packs raw ingots or items into dense storage blocks (e.g., 9 Ingots $\rightarrow$ 1 Block).
* **Singularity Condensation (Mode 1)**: Condenses thousands of materials under gravitational pressure into custom color-tinted Singularities.
* **Dust Crushing (Mode 2)**: Pulverizes ores, ingots, and raw materials into fine powders. Features dynamic auto-scanning for modded ores.
* **Sided I/O Routing & Lock System**: Configure each face (Input/Output/Disabled) and lock the machine to a single item type.

### 🧪 Alchemical Synthesizer
A thermodynamic machine that combines Forge Energy (FE), fluid catalysts, and physical tool interactions across three visual progress routes:
* **Route A (Sifting & Washing)**: Uses Water + Shovels/Paxels to wash blocks (e.g., Gravel $\rightarrow$ Flint/Clay).
* **Route B (Kinetic Milling & Crushing)**: Uses physical tools without fluid to mill wood or crush stone with tool durability consumption.
* **Route C (Hydrothermal Smelting)**: Uses Lava catalysts to auto-smelt raw ores into refined ingots.
* **Power Capacity**: Holds up to **5,000,000 FE** and **8,000 mB** of fluid catalyst.

### ⚓ PureMash Chunk Loader
Forces world chunks to remain loaded continuously across 6 selectable radius levels (1x1 up to 17x17 chunks).
* **Holographic Boundary Renderer**: Displays dynamic in-world laser walls outlining active loaded chunks.
* **Tiered Expansion**: Radius levels 4+ require a **Moldelonian Core** in the machine slot.

### 💎 Synthorium Equipment & Enchantments
* **Synthorium Tool Set**: Ultra-durable tools and armor.
* **Overload Enchantment**: Increases reach distance, accelerates nearby tile entities on the PureMash Core Block, and unlocks **Creative Flight** when applied to full Synthorium armor!
* **Overclock Enchantment**: Dynamically boosts the virtual levels of all other enchantments present on the item.

---

## 📜 Modpack Developer Guide (KubeJS & Configs)

PureMash Tweaks provides triple integration options for modpack creators:

### 1. Direct Config JSONs
Upon first boot, the mod automatically populates `config/PureMash Tweaks/` with documented JSON templates and README files:
* `synthesis_recipes/shaped/` & `synthesis_recipes/shapeless/`
* `compressor_recipes/compressor/`, `singularity/`, & `dust/`
* `alchemical_recipes/`

### 2. KubeJS Integration
Recipes can be registered dynamically via KubeJS server scripts:
```javascript
ServerEvents.recipes(event => {
  // Registered custom recipe schemas for 9x9 synthesis
  event.recipes.puremashtweaks.shaped_synthesis(...)
  event.recipes.puremashtweaks.shapeless_synthesis(...)
})
