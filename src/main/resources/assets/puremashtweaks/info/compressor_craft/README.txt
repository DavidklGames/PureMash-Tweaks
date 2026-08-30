======================================================================
    P U R E M A S H   T W E A K S   -   C O M P R E S S O R
======================================================================

The Multifunctional Compressor is a heavy-duty multi-mode machine
designed for high-speed material compaction, gravitational singularity
condensation, and kinetic pulverization.

----------------------------------------------------------------------
  1. MACHINE OVERVIEW & INTERFACE
----------------------------------------------------------------------

- Energy Storage: 5,000,000 FE base capacity (expandable via Capacity Upgrades).
- Slot 0 (Input): Accepts compatible raw materials, ingots, or ores.
- Slot 1 (Output): Holds finished products with instantaneous 0-tick
  auto-ejection to adjacent inventories, cables, and AE2 networks.
- Slots 2 to 4 (Upgrades): Accept Speed, Capacity, Duplication,
  and Stack Processing Upgrades.
- Mode Selector: Cycles between Compression, Singularity, and Dust modes.
- Recipe Lock: Locks the machine to the item currently in Slot 0 to prevent
  automation lines and conduits from inserting unwanted materials.

----------------------------------------------------------------------
  2. THE THREE OPERATING MODES
----------------------------------------------------------------------

- MODE 0: COMPRESSION (Default: 20t @ 50 FE/t)
  Packs large quantities of items into dense block forms (e.g., 9 ingots
  into 1 block). Automatically scans global crafting recipes for 9:1
  storage block conversions.

- MODE 1: SINGULARITY CONDENSING (Default: 40t @ 250 FE/t)
  Condenses dense materials under extreme gravitational pressure.
  Input items are consumed progressively into an internal accumulator
  until the required target cost (default: 1,000 items) is reached.

- MODE 2: DUST CRUSHING (Default: 20t @ 100 FE/t)
  Pulverizes ores, raw metals, and ingots into fine powders.
  Automatically detects metals and gems across all mods and applies
  built-in ore doubling (1 raw ore/ore block -> 2 dusts).

----------------------------------------------------------------------
  3. UPGRADES & PERFORMANCE
----------------------------------------------------------------------

- Speed Upgrade Tier 1: Increases processing speed (+2 power).
- Speed Upgrade Tier 2: Significantly increases processing speed (+4 power).
- Speed Upgrade Tier 3: Exponentially accelerates processing speed
  (+16, +64, up to +256 power when using 3x cards).
- Capacity Upgrades: Multiplies internal energy storage (2x / 5x).
- Duplication Upgrades: Grants a chance to duplicate output items (+15% / +50%).
- Stack Processing Upgrade: Allows processing an entire stack (up to 64 items)
  simultaneously in a single cycle.

----------------------------------------------------------------------
  4. RECIPE DECLARATION: JSON CONFIG & KUBEJS
----------------------------------------------------------------------

A) COMPRESSION RECIPES (Mode 0)

- JSON Config: config/PureMash Tweaks/compressor_recipes/compressor/*.json
[
  {
    "input": "puremashtweaks:synthorium_ingot",
    "input_count": 9,
    "output": "puremashtweaks:synthorium_block",
    "time_cost": 20,
    "enable_recipe": true
  }
]

- KubeJS Script: kubejs/server_scripts/compressor_recipes.js
ServerEvents.recipes(event => {
    event.recipes.puremashtweaks.compression('puremashtweaks:synthorium_block', 'puremashtweaks:synthorium_ingot')
        .inputCount(9)
        .timeCost(20)
        .id('kubejs:compress_synthorium_block')
})


B) SINGULARITY RECIPES (Mode 1)

- JSON Config: config/PureMash Tweaks/compressor_recipes/singularity/*.json
[
  {
    "name": "Synthorium Singularity",
    "item": "puremashtweaks:synthorium_block",
    "cost": 1000,
    "color0": "#101010",
    "color1": "#00FFFF",
    "required_mod_id": "none",
    "enable_recipe": true,
    "enable_item": true
  }
]

* Note on "required_mod_id": If specified (e.g. "ae2", "mysticalagriculture"),
  the recipe is loaded only when that mod is installed. Use "none" otherwise.

- KubeJS Script:
ServerEvents.recipes(event => {
    event.recipes.puremashtweaks.singularity('puremashtweaks:diamond_singularity', 'minecraft:diamond_block')
        .cost(500)
        .timeCost(40)
        .id('kubejs:condense_diamond_singularity')
})


C) DUST CRUSHING RECIPES (Mode 2)

- JSON Config: config/PureMash Tweaks/compressor_recipes/dust/*.json
[
  {
    "input": "puremashtweaks:synthorium_ingot",
    "output": "puremashtweaks:synthorium_dust",
    "time_cost": 20,
    "enable_recipe": true
  }
]

- KubeJS Script:
ServerEvents.recipes(event => {
    event.recipes.puremashtweaks.dust('puremashtweaks:synthorium_dust', 'puremashtweaks:synthorium_ingot')
        .timeCost(20)
        .id('kubejs:crush_synthorium_ingot')
})
======================================================================