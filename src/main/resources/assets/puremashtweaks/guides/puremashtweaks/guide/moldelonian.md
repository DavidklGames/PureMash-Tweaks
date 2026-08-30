---
navigation:
  title: Moldelonian
  icon: moldelonian_ingot
  position: 2
item_ids:
  - puremashtweaks:suspicious_end_stone
  - puremashtweaks:moldelonian_smithing_template
  - puremashtweaks:moldelonian_ingot
  - puremashtweaks:moldelonian_block
  - puremashtweaks:moldelonian_nugget
  - puremashtweaks:moldelonian_dust
  - puremashtweaks:moldelonian_sword
  - puremashtweaks:moldelonian_pickaxe
  - puremashtweaks:moldelonian_axe
  - puremashtweaks:moldelonian_shovel
  - puremashtweaks:moldelonian_hoe
  - puremashtweaks:moldelonian_paxel
  - puremashtweaks:moldelonian_helmet
  - puremashtweaks:moldelonian_chestplate
  - puremashtweaks:moldelonian_leggings
  - puremashtweaks:moldelonian_boots
  - puremashtweaks:moldelonian_apple
---

# Moldelonian

<Color id="puremashtweaks:moldelonian_gold">Moldelonian</Color> is an endgame alloy forged by combining base metals, precious minerals, and modded ingots into a single material.

---

## 1. End Archaeology & Smithing Template

<Row gap="10">
  <GameScene zoom="3.5" interactive={true}>
    <ImportStructure src="structures/suspicious_end_stone.nbt" />
  </GameScene>
  <Column>
    Use a Brush on <ItemLink id="puremashtweaks:suspicious_end_stone" /> across the surface of the End islands (End Highlands and around End Cities) to recover the <ItemLink id="puremashtweaks:moldelonian_smithing_template" />.
  </Column>
</Row>

### Template Duplication
Once you have one template, duplicate it with 7x End Stone and 1x <ItemLink id="puremashtweaks:moldelonian_ingot" />.

<RecipeFor id="puremashtweaks:moldelonian_smithing_template" />

---

## 2. Ingot Processing & Food

Moldelonian ingots can be crafted on the 9x9 Synthesis Table or smelted from dust.

<Row gap="10">
  <GameScene zoom="3.5" interactive={true}>
    <ImportStructure src="structures/moldelonian_block.nbt" />
  </GameScene>
  <Column>
    <ItemGrid>
      <ItemIcon id="puremashtweaks:moldelonian_ingot" />
      <ItemIcon id="puremashtweaks:moldelonian_block" />
      <ItemIcon id="puremashtweaks:moldelonian_nugget" />
      <ItemIcon id="puremashtweaks:moldelonian_dust" />
    </ItemGrid>
  </Column>
</Row>

<RecipeFor id="puremashtweaks:moldelonian_block" />

### Moldelonian Apple
Surround a Synthorium Apple with 8x Moldelonian Ingots to create the <ItemLink id="puremashtweaks:moldelonian_apple" />. Consuming it provides:
* **Instant Health II**
* **Absorption V** (3:00)
* **Resistance IV** (0:35)
* **Regeneration V** (0:30)
* **Strength III** (1:30)
* **Fire Resistance** (5:00)

<RecipeFor id="puremashtweaks:moldelonian_apple" />

---

## 3. Smithing Upgrades & The Paxel

Upgrade your Synthorium gear on a vanilla Smithing Table using the Moldelonian Template and a Moldelonian Ingot.

<ItemGrid>
  <ItemIcon id="puremashtweaks:moldelonian_sword" />
  <ItemIcon id="puremashtweaks:moldelonian_pickaxe" />
  <ItemIcon id="puremashtweaks:moldelonian_axe" />
  <ItemIcon id="puremashtweaks:moldelonian_shovel" />
  <ItemIcon id="puremashtweaks:moldelonian_hoe" />
</ItemGrid>

### Moldelonian Paxel (65.0 Damage)
The <ItemLink id="puremashtweaks:moldelonian_paxel" /> combines pickaxe, shovel, and axe functionality with **65.0 Base Damage** and 6,500 durability. When enchanted with Overload, it breaks bedrock.

---

## 4. Moldelonian Armor Stats

A full set of Moldelonian Armor provides:
* **100 Total Armor Defense** (18 Helmet, 36 Chestplate, 28 Leggings, 18 Boots)
* **50.0 Total Armor Toughness** (12.5 per piece)
* **100% Knockback Immunity**
* Full compatibility with Overload flight and Overdrive perks.

<ItemGrid>
  <ItemIcon id="puremashtweaks:moldelonian_helmet" />
  <ItemIcon id="puremashtweaks:moldelonian_chestplate" />
  <ItemIcon id="puremashtweaks:moldelonian_leggings" />
  <ItemIcon id="puremashtweaks:moldelonian_boots" />
</ItemGrid>