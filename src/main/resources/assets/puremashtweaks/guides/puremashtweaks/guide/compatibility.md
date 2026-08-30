---
navigation:
  title: Cross-Mod Compatibility
  icon: synthorium_plate
  position: 12
---

# Cross-Mod Compatibility

PureMash Tweaks integrates cleanly with popular tech, magic, and resource generation mods.

---

## 1. Productive Bees

Two custom bee species automate Synthorium and Moldelonian production:

### SynthBee (Tier 4 Resource Bee)
* **Breeding:** Breed a **Netherite Bee** with a **Diamond Bee**.
* **Flower Block:** Requires a <ItemLink id="puremashtweaks:synthorium_block" />.
* **Output:** Centrifuging Synthorium Honeycombs yields <ItemLink id="puremashtweaks:synthorium_nugget" /> and wax.

### MoldelBee (Tier 5 Endgame Bee)
* **Conversion:** Use a <ItemLink id="puremashtweaks:moldelonian_block" /> on a **SynthBee** in the world.
* **Flower Block:** Requires a <ItemLink id="puremashtweaks:moldelonian_block" />.
* **Output:** Centrifuging Moldelonian Honeycombs yields <ItemLink id="puremashtweaks:moldelonian_nugget" />.

---

## 2. Productive Metalworks

PureMash metals can be melted, alloyed, and cast in high-temperature foundries:

* **Melting Points:** 2,200 °C for Synthorium / 2,900 °C for Moldelonian.
* **Casting:** Cast molten metals into Blocks (810 mB), Ingots (90 mB), Plates (270 mB), Rods (135 mB), Nuggets (10 mB), and Molten Buckets (1,000 mB).
* **Alchemical Casting:** Cast 80 mB of molten metal onto an Apple to produce the <ItemLink id="puremashtweaks:synthorium_apple" /> or <ItemLink id="puremashtweaks:moldelonian_apple" />.

---

## 3. Silent Gear

Synthorium and Moldelonian ingots act as high-tier materials in Silent Gear:

* **Synthorium Material:** Features Sturdy IV, Flexible III, Synergistic III, and Magnetic II.
* **Synthorium Rod:** Part substitute for tool rods (+50% durability, +20% mining speed, +0.2 attack speed).
* **Moldelonian Material:** Endgame material granting **65.0 Attack Damage**, **100 Armor**, and **50.0 Armor Toughness**.

---

## 4. KubeJS Scripting

Modpack creators can register custom recipes for all PureMash machines in `server_scripts`:

* **9x9 Synthesis Table:** `event.recipes.puremashtweaks.shaped_synthesis` and `shapeless_synthesis`.
* **Multifunctional Compressor:** `event.recipes.puremashtweaks.compression`, `singularity`, and `dust`.
* **Alchemical Synthesizer:** `event.recipes.puremashtweaks.alchemical`.