---
navigation:
  title: Multifunctional Compressor
  icon: multifunctional_compressor
  position: 4
item_ids:
  - puremashtweaks:multifunctional_compressor
---

# Multifunctional Compressor

The <Color id="puremashtweaks:cyan_glow">Multifunctional Compressor</Color> handles dense material packing, gravitational singularity condensation, and ore/ingot pulverization.

<Row gap="10">
  <GameScene zoom="3.5" interactive={true}>
    <ImportStructure src="structures/multifunctional_compressor.nbt" />
  </GameScene>
  <Column>
    It comes with a base **5,000,000 FE** internal buffer, side upgrade expansion, and native pipeline locking to prevent inventory jams.
  </Column>
</Row>

<RecipeFor id="puremashtweaks:multifunctional_compressor" />

---

## 1. Layout & Upgrades

* **Input Slot (Slot 0):** Insert raw materials, ingots, or ores.
* **Output Slot (Slot 1):** Collects finished items.
* **Upgrade Slots (Slots 2, 3, 4):** Located on the right. Accepts Speed, Capacity, Duplication, and Stack Processing upgrades.

---

## 2. Operating Modes

Click the **Mode Button** on the GUI to cycle through three specialized functions:

#### Compression Mode (Mode 0 - 50 FE/t)
Packs 9 items into their block form (e.g., 9 <ItemLink id="puremashtweaks:synthorium_ingot" /> into 1 <ItemLink id="puremashtweaks:synthorium_block" />). Automatically scans global crafting recipes for 9-to-1 conversions.

#### Singularity Mode (Mode 1 - 250 FE/t)
Condenses large quantities of matter into concentrated Singularities. Consumes items one by one into its accumulator until reaching the target cost (default: 1,000 items) to output the singularity.

#### Dust Crushing Mode (Mode 2 - 100 FE/t)
Pulverizes ingots, raw metals, and ores into fine dusts. Automatically doubles yields when crushing raw ores (1 raw ore = 2x dusts).

---

## 3. Recipe Locking

Click the **Lock Button** to keep automated lines clean:
* **Locked:** Memorizes the item currently in Slot 0 and rejects any other item from entering.
* **Unlocked (Free):** Accepts any valid recipe ingredient.