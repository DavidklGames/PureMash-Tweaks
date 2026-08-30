---
navigation:
  title: Synthorium
  icon: synthorium_ingot
  position: 1
item_ids:
  - puremashtweaks:synthorium_debris
  - puremashtweaks:synthorium_scrap
  - puremashtweaks:synthorium_ingot
  - puremashtweaks:synthorium_nugget
  - puremashtweaks:synthorium_dust
  - puremashtweaks:synthorium_block
  - puremashtweaks:synthorium_rod
  - puremashtweaks:synthorium_sword
  - puremashtweaks:synthorium_pickaxe
  - puremashtweaks:synthorium_axe
  - puremashtweaks:synthorium_shovel
  - puremashtweaks:synthorium_hoe
  - puremashtweaks:synthorium_paxel
  - puremashtweaks:synthorium_helmet
  - puremashtweaks:synthorium_chestplate
  - puremashtweaks:synthorium_leggings
  - puremashtweaks:synthorium_boots
  - puremashtweaks:synthorium_apple
---

# Synthorium

<Color id="puremashtweaks:cyan_glow">Synthorium</Color> is a high-energy mineral found in the deepest layers of the Overworld crust. It serves as the entry tier for all advanced PureMash tech.

---

## 1. Finding Debris & Ingot Crafting

<Row gap="10">
  <GameScene zoom="3.5" interactive={true}>
    <ImportStructure src="structures/synthorium_debris.nbt" />
  </GameScene>
  <Column>
    <ItemLink id="puremashtweaks:synthorium_debris" /> generates naturally between **Y = -64** and **Y = 0**. Smelt it in a furnace or blast furnace to produce <ItemLink id="puremashtweaks:synthorium_scrap" />.
  </Column>
</Row>

<RecipeFor id="puremashtweaks:synthorium_scrap" />

### Ingot Recipe
Combine **4x Synthorium Scrap** with **4x Diamonds** in any crafting grid to get a <ItemLink id="puremashtweaks:synthorium_ingot" />.

<RecipeFor id="puremashtweaks:synthorium_ingot" />

---

## 2. Crafting Components & Food

Synthorium can be packed into storage blocks, broken down into nuggets, drawn into rods, or ground into dust.

<Row gap="10">
  <GameScene zoom="3.5" interactive={true}>
    <ImportStructure src="structures/synthorium_block.nbt" />
  </GameScene>
  <Column>
    <ItemGrid>
      <ItemIcon id="puremashtweaks:synthorium_ingot" />
      <ItemIcon id="puremashtweaks:synthorium_block" />
      <ItemIcon id="puremashtweaks:synthorium_nugget" />
      <ItemIcon id="puremashtweaks:synthorium_dust" />
      <ItemIcon id="puremashtweaks:synthorium_rod" />
    </ItemGrid>
  </Column>
</Row>

<RecipeFor id="puremashtweaks:synthorium_block" />
<RecipeFor id="puremashtweaks:synthorium_rod" />

### Synthorium Apple
Surround an Apple with 8x Synthorium Ingots to make a <ItemLink id="puremashtweaks:synthorium_apple" />. Consuming it grants:
* **Absorption II** (2:00)
* **Resistance II** (0:45)
* **Regeneration III** (0:20)
* **Fire Resistance** (3:00)
* **Speed II** (1:00)

<RecipeFor id="puremashtweaks:synthorium_apple" />

---

## 3. Tools & The Synthorium Paxel

Synthorium tools provide high durability (3,000 uses) and fast mining speed (15.0), sitting comfortably above Netherite tier.

<ItemGrid>
  <ItemIcon id="puremashtweaks:synthorium_sword" />
  <ItemIcon id="puremashtweaks:synthorium_pickaxe" />
  <ItemIcon id="puremashtweaks:synthorium_axe" />
  <ItemIcon id="puremashtweaks:synthorium_shovel" />
  <ItemIcon id="puremashtweaks:synthorium_hoe" />
</ItemGrid>

Combine the pickaxe, axe, shovel, an ingot, and a rod to forge the **Synthorium Paxel** (25.0 Base Damage).

<RecipeFor id="puremashtweaks:synthorium_paxel" />

---

## 4. Armor & Flight

Full Synthorium Armor grants **34 Defense points**, **20.0 Armor Toughness**, and **80% Knockback Resistance**.

<ItemGrid>
  <ItemIcon id="puremashtweaks:synthorium_helmet" />
  <ItemIcon id="puremashtweaks:synthorium_chestplate" />
  <ItemIcon id="puremashtweaks:synthorium_leggings" />
  <ItemIcon id="puremashtweaks:synthorium_boots" />
</ItemGrid>

> **Overload Flight:** Enchanting all 4 armor pieces with **Overload** grants creative flight. Press <KeyBind id="key.puremashtweaks.toggle_flight" /> to toggle it on or off.