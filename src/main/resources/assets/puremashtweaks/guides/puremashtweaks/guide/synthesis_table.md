---
navigation:
  title: Synthesis Table
  icon: synthesis_table
  position: 3
item_ids:
  - puremashtweaks:synthesis_table
  - puremashtweaks:memory_card
---

# Synthesis Table

The <Color id="puremashtweaks:cyan_glow">Synthesis Table</Color> is a 9x9 automated crafting workstation designed for complex endgame recipes, cores, and creative chips.

<Row gap="10">
  <GameScene zoom="3.5" interactive={true}>
    <ImportStructure src="structures/synthesis_table.nbt" />
  </GameScene>
  <Column>
    With an 81-slot grid, transaction rollback protection, and zero-tick auto-ejection to adjacent networks (AE2 / pipes), it runs large automation patterns reliably.
  </Column>
</Row>

<RecipeFor id="puremashtweaks:synthesis_table" />

---

## 1. Slots & Memory Cards

* **9x9 Matrix (Slots 0–80):** Main recipe layout grid.
* **Output (Slot 81):** Displays and holds the crafted item.
* **Memory Card Slot (Slot 82):** Visible only when **Automation** is turned **On**. Used to insert or encode cards.

<RecipeFor id="puremashtweaks:memory_card" />

---

## 2. Automation Modes

Use the buttons on the left panel to configure behavior:

#### Active Button (On / Off)
Toggles automation support and reveals the Memory Card slot (Slot 82).

#### Craft Mode (Mode 0)
Standard automation. When a configured Memory Card is in Slot 82, external logistics (pipes, AE2) are locked to inserting ingredients strictly into the exact matching grid slots.

#### Add / Encoder Mode (Mode 1)
Writes a recipe pattern to a blank card:
1. Turn Automation **On** and set Mode to **Add**.
2. Place the physical ingredients in the 9x9 grid in the desired shape.
3. Put an empty <ItemLink id="puremashtweaks:memory_card" /> into Slot 82.
4. Click **Save** on the left panel. The recipe is saved to the card, leaving your items in place.

---

## 3. Holograms & Clearing Cards

* **Ghost Overlay:** When Automation is active with a configured card, a semi-transparent overlay shows required ingredients directly in the GUI.
* **3D World Hologram:** Renders a floating, rotating 3D preview of the output above the block in the world.
* **Wiping Cards:** Hold a configured Memory Card, look straight up (pitch &lt; -60°), and right-click to clear it.

---

## 4. Logistics & Safety

* **0-Tick Auto-Ejection:** When a recipe finishes, the table immediately pushes the output into adjacent inventories or AE2 Pattern Providers.
* **Rollback Journal:** If external pipes supply only part of the ingredients and stop, the table reverts the transaction to prevent half-filled grid jams.