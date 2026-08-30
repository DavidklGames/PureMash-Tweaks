---
navigation:
  title: Universal Cables & Logistics
  icon: synthorium_universal_cable
  position: 7
item_ids:
  - puremashtweaks:synthorium_universal_cable
  - puremashtweaks:moldelonian_universal_cable
  - puremashtweaks:configuration_wrench
  - puremashtweaks:distribution_filter
---

# Universal Cables & Logistics

<Color id="puremashtweaks:cyan_glow">Universal Cables</Color> are 3-in-1 logistics conduits that transport **Items**, **Fluids**, and **Energy (FE)** simultaneously through a single block space.

<Row gap="10">
  <GameScene zoom="3.5" interactive={true}>
    <ImportStructure src="structures/synthorium_universal_cable.nbt" />
  </GameScene>
  <GameScene zoom="3.5" interactive={true}>
    <ImportStructure src="structures/moldelonian_universal_cable.nbt" />
  </GameScene>
  <Column>
    * **Synthorium Tier:** 50,000 FE/t | 1,000 mB/t | 8 items/cycle
    * **Moldelonian Tier:** 100,000 FE/t | 10,000 mB/t | 64 items/cycle
  </Column>
</Row>

<RecipeFor id="puremashtweaks:synthorium_universal_cable" />
<RecipeFor id="puremashtweaks:moldelonian_universal_cable" />

---

## 1. Wrench Configuration

Hold a <ItemLink id="puremashtweaks:configuration_wrench" /> and **Shift + Right-Click** any connected side of a cable to cycle modes:
1. **Insert (Default):** Pushes resources into adjacent inventories.
2. **Extract (Nozzle):** Pulls resources out of the connected block.
3. **Disconnect:** Breaks connection on that specific face.

<RecipeFor id="puremashtweaks:configuration_wrench" />

---

## 2. Cable GUI & Channels

Right-click any active **Extraction Nozzle** with an empty hand to open the Cable GUI. Use the left tabs to switch channels:
* **Energy Channel (Tab 0):** Power routing.
* **Item Channel (Tab 1):** Item filtering and routing.
* **Fluid Channel (Tab 2):** Fluid filtering and routing.

---

## 3. Distribution Modes & Filters

Install a **Speed Upgrade** into the cable upgrade slot to unlock configuration buttons:
* **Distribution Modes:** Round-Robin, Random, or Dynamically (balanced split across destinations).
* **Redstone Control:** Ignored, Active with Signal, or Active without Signal.
* **Filter Policy:** Whitelist (Allow) or Blacklist (Block).

---

## 4. Distribution Filters (Targeted Routing)

Shift + Right-click any container in the world with a <ItemLink id="puremashtweaks:distribution_filter" /> to bind its coordinates and dimension.

<RecipeFor id="puremashtweaks:distribution_filter" />

Placing the bound filter into a Cable Filter GUI unlocks:
* **Destination Priority (1–99):** High-priority containers are filled first.
* **Stock Limit (Cap):** Limits the item count allowed in the target container (default: **64**).
* **Target Slot Sequences:** Restrict extraction to specific slot indices (e.g. `6, 1, 4`).
* **Durability Thresholds:** Extract damaged tools when durability falls below a value (e.g. `<= 25%` or `< 500`).