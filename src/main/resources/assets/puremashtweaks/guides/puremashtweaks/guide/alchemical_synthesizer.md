---
navigation:
  title: Alchemical Synthesizer
  icon: alchemical_synthesizer
  position: 5
item_ids:
  - puremashtweaks:alchemical_synthesizer
---

# Alchemical Synthesizer

The <Color id="puremashtweaks:cyan_glow">Alchemical Synthesizer</Color> is a multi-route thermodynamic machine. It processes ores, dusts, and mineral blocks using tools, fluid catalysts, and Forge Energy (FE).

<Row gap="10">
  <GameScene zoom="3.5" interactive={true}>
    <ImportStructure src="structures/alchemical_synthesizer.nbt" />
  </GameScene>
  <Column>
    Featuring an internal **16,000 mB** hydraulic fluid tank, a **5,000,000 FE** energy buffer, and a large 20-slot output inventory with instant auto-ejection.
  </Column>
</Row>

<RecipeFor id="puremashtweaks:alchemical_synthesizer" />

---

## 1. Interface & Fluid Port

* **Fluid Valve (Slot 0):** Hydraulic port for the internal tank. Click with a Water or Lava bucket to deposit liquid, or click with an empty bucket to extract 1,000 mB.
* **Material Input (Slot 1):** Place ores, dusts, or raw materials.
* **Tool Catalyst (Slot 2):** Accepts pickaxes, shovels, axes, or paxels.
* **Output Grid (Slots 3–22):** 20 output slots with automated push to adjacent inventories.
* **Upgrade Slots (Slots 23–25):** Located on the right for Speed, Capacity, Duplication, and Stack upgrades.

---

## 2. Processing Routes

Three progress arrows in the GUI indicate which process is active:

#### Route A: Sifting & Washing (Hydro-Thermal)
* **Requires:** Water (250 mB) + Shovel/Paxel + Input Block.
* **Result:** Washes and separates materials (e.g., Gravel into Flint or Clay).
* **Visual:** Top, Middle, and Bottom arrows light up.

#### Route B: Kinetic Crushing & Cutting (Mechanical)
* **Requires:** Tool + Input Material (no fluid needed).
* **Result:** Crushes stone or cuts timber (e.g., Cobblestone + Pickaxe into Gravel; Logs + Axe into 6x Planks). Consumes tool durability.
* **Visual:** Middle and Bottom arrows light up.

#### Route C: Electric Smelting (Furnace Mode)
* **Requires:** Input Item + Energy (no fluids or tools needed).
* **Result:** Smelts any vanilla or modded dust/ore into its ingot form using electricity (100 to 500 FE/t).
* **Visual:** Middle arrow lights up.

#### Route D: Alchemical Synthesis
* **Requires:** Fluid Catalyst + Tool + Custom Reagents.
* **Result:** Runs custom JSON synthesis routes, including alloy blending and dual-output recipes.

---

## 3. Direct Redstone Charging

If you are running the machine without cables, place **Redstone Dust** in Slot 1 for **+5,000 FE**, or a **Redstone Block** for **+45,000 FE**.