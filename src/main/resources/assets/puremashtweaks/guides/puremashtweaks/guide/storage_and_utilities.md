---
navigation:
  title: Storage & Utilities
  icon: puremash_battery
  position: 8
item_ids:
  - puremashtweaks:puremash_battery
  - puremashtweaks:creative_battery
  - puremashtweaks:fluid_tank
  - puremashtweaks:creative_fluid_tank
  - puremashtweaks:chunk_loader
---

# Storage & Utilities

High-capacity energy batteries, 3D fluid reservoirs, and chunk loaders.

---

## 1. Energy Storage (Batteries)

<Row gap="10">
  <GameScene zoom="3.5" interactive={true}>
    <ImportStructure src="structures/puremash_battery.nbt" />
  </GameScene>
  <GameScene zoom="3.5" interactive={true}>
    <ImportStructure src="structures/creative_battery.nbt" />
  </GameScene>
  <Column>
    * **PureMash Battery:** Stores **50,000,000 FE** with a 1,500,000 FE/t transfer rate. Retains stored charge when broken and picked up.
    * **Creative Battery:** Outputs infinite **∞ FE** at 10,000,000 FE/t.
  </Column>
</Row>

---

## 2. Fluid Reservoirs

<Row gap="10">
  <GameScene zoom="3.5" interactive={true}>
    <ImportStructure src="structures/fluid_tank.nbt" />
  </GameScene>
  <GameScene zoom="3.5" interactive={true}>
    <ImportStructure src="structures/creative_fluid_tank.nbt" />
  </GameScene>
  <Column>
    * **Fluid Tank (32,000 mB):** Renders stored liquids in 3D in-world and in inventory. Retains fluid data when broken.
    * **Creative Fluid Tank:** Infinite fluid supply. Right-click with any liquid bucket to lock an infinite source; **Shift + Right-Click** with an empty hand to clear it.
  </Column>
</Row>

---

## 3. PureMash Chunk Loader

The <ItemLink id="puremashtweaks:chunk_loader" /> keeps world chunks loaded and active around its position.

<Row gap="10">
  <GameScene zoom="3.5" interactive={true}>
    <ImportStructure src="structures/puremash_chunk_loader.nbt" />
  </GameScene>
  <Column>
    * **Levels 1 to 3 (1x1 to 5x5 Chunks):** Works standalone.
    * **Levels 4 to 6 (9x9 to 17x17 Chunks):** Requires a <ItemLink id="puremashtweaks:moldelonian_core" /> inside the loader slot.
    * **Visualizer:** Click the **B** button in the GUI to toggle glowing 3D boundary walls in the world.
  </Column>
</Row>