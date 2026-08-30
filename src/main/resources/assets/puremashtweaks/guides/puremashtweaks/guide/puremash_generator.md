---
navigation:
  title: PureMash Generator
  icon: puremash_generator
  position: 6
item_ids:
  - puremashtweaks:puremash_generator
---

# PureMash Generator

The <Color id="puremashtweaks:cyan_glow">PureMash Generator</Color> converts dense mineral fuels, uranium, and high-energy alloys into massive Forge Energy (FE) output.

<Row gap="10">
  <GameScene zoom="3.5" interactive={true}>
    <ImportStructure src="structures/puremash_generator.nbt" />
  </GameScene>
  <Column>
    Equipped with a **400,000,000 FE** base buffer (expandable up to 2 Billion FE), a 20,000 mB Coolant Tank, and a 100,000 mB Steam Output Tank.
  </Column>
</Row>

---

## 1. Supported Fuels

The generator accepts regular furnace fuels as well as high-density materials:

* **Moldelonian Block / Ingot:** 15,000 FE/t (12,000t) / 5,000 FE/t (1,200t)
* **Uranium Block / Ingot:** 8,000 FE/t (18,000t) / 3,000 FE/t (1,800t)
* **Synthorium Block / Ingot:** 2,500 FE/t (6,000t) / 1,000 FE/t (600t)
* **Redstone & Coal:** Standard burn rates.

---

## 2. Thermodynamics & Water Cooling

Burning high-tier fuels causes core temperature to rise up to **1,500 °C**.
* **Overheating:** Without cooling, temperatures above 200 °C lower generation efficiency.
* **Water Cooling:** Supplying water into the 20,000 mB coolant tank absorbs heat, keeps the core at 100% peak efficiency, and converts the water into **Steam**.

---

## 3. Waste Byproducts & Charging

* **Mineral Waste (Slots 4–6):** Burning dense fuels has a chance to produce residual nuggets (<ItemLink id="puremashtweaks:moldelonian_nugget" />, <ItemLink id="puremashtweaks:synthorium_nugget" />, or Uranium Nuggets).
* **Charge Port (Slot 7):** Insert rechargeable energy items (such as the <ItemLink id="puremashtweaks:moldelonian_core" />) to charge them at 50,000 FE/t.