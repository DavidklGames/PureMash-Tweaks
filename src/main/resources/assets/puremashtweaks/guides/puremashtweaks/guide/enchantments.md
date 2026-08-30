---
navigation:
  title: Advanced Enchantments
  icon: overload_book
  position: 13
item_ids:
  - puremashtweaks:overload_book
  - puremashtweaks:overclock_book
  - puremashtweaks:overdrive_book
---

# Advanced Enchantments

PureMash Tweaks introduces three meta-enchantments: **Overload**, **Overclock**, and **Overdrive**.

<ItemGrid>
  <ItemIcon id="puremashtweaks:overload_book" />
  <ItemIcon id="puremashtweaks:overclock_book" />
  <ItemIcon id="puremashtweaks:overdrive_book" />
</ItemGrid>

---

## 1. Overload (Max Level III)

<Color id="puremashtweaks:cyan_glow">Overload</Color> provides extended reach, bedrock breaking, creative flight, and area tick acceleration.

### Tools & Weapons
* **Reach Bonus:** Adds **+1.0 block** of interaction and attack reach per level (+3.0 blocks at Level III).
* **Bedrock Breaking:** Synthorium and Moldelonian Pickaxes and Paxels enchanted with Overload can mine **Bedrock**.

### Armor Flight
Equipping a full 4-piece Synthorium or Moldelonian set enchanted with Overload grants creative flight:
* **Overload I:** 2:00 minutes (2,400 ticks) flight gauge. Recharges on the ground.
* **Overload II:** 3:50 minutes (4,200 ticks) flight gauge. Recharges on the ground.
* **Overload III:** **Infinite Creative Flight (∞)**.
* **Hotkey:** Press <KeyBind id="key.puremashtweaks.toggle_flight" /> to toggle flight on or off.

### Core Block Acceleration
Enchanting a <ItemLink id="puremashtweaks:puremash_core_block" /> with Overload accelerates nearby crops and block entities:
* **Level I:** 3x3x3 Area (+100% speed)
* **Level II:** 5x5x5 Area (+300% speed)
* **Level III:** 7x7x7 Area (+800% mass acceleration)

---

## 2. Overclock (Max Level II)

<Color id="puremashtweaks:cyan_glow">Overclock</Color> is an anvil catalyst that increases the maximum level cap of all other enchantments on an item or book.

### Anvil Mechanics
Combining an item or book with Overclock on an anvil increases the level cap of every other enchantment:
* **Overclock I:** +1 level beyond vanilla limits (e.g., Sharpness VI, Protection V, Fortune IV, Looting IV).
* **Overclock II:** +2 levels beyond vanilla limits (e.g., Sharpness VII, Protection VI, Fortune V, Looting V).

Overclocked enchantments render in a glowing **Bright Cyan** (<Color id="puremashtweaks:cyan_glow">#00E5FF</Color>) in tooltips.

---

## 3. Overdrive (Max Level IV)

<Color id="puremashtweaks:cyan_glow">Overdrive</Color> adds combat cleave to swords, context-aware area mining and tree felling to tools, wireless block teleportation, and mobility perks to armor.

### Swords (Enlarged Combat Cleave)
Swords gain an expanded sweeping attack that damages all nearby hostile mobs around your primary target:
* **Level I:** Standard attack.
* **Level II:** **2.5-block** AoE sweep radius.
* **Level III:** **3.5-block** AoE sweep radius.
* **Level IV:** Massive **5.0-block** AoE sweep radius!

### Tools & Paxels (Smart Mining & Tree Felling)
* **Context-Aware Area Mining:** Scales from **3x3** (Level II), **5x5** (Level III), up to **7x7** (Level IV). Smart filtering ensures tools only break blocks matching the target material category (e.g. mining stone will not break adjacent dirt or wood).
* **Tree Felling:** Breaking logs with an Axe or Paxel automatically fells the entire connected tree and its canopy upward.
* **Wireless Block Teleportation:** **Shift + Right-Click** any container in the world (chests, barrels, AE2 interfaces) with a tool to bind it. Mined block drops teleport directly into the target container.
* **Hotkey:** Press <KeyBind id="key.puremashtweaks.toggle_overdrive" /> to toggle Overdrive mode on or off.

### Armor Perks
* **Mobility:** +5% movement speed per level (**+20%** at Level IV) and +0.5 block step height.
* **Fall Immunity (Boots):** Completely negates 100% of fall damage with a wind burst effect.