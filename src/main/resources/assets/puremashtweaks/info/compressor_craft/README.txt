======================================================================
    P U R E M A S H   T W E A K S   -   C O M P R E S S O R
======================================================================

The Multifunctional Compressor is a versatile heavy-duty machine
capable of dense material compression, gravity-based singularity
condensation, and kinetic pulverization.

----------------------------------------------------------------------
  1. INTERFACE AND SLOT LAYOUT
----------------------------------------------------------------------

The interface of the Compressor is designed for precise manual and
automated control.

+--[X]---------------------------------------------------------+
|                                                              |
|   [Input Slot]  =======>  [Progress]  =======>  [Output]     |
|     (Slot 0)               (Arrow)               (Slot 1)    |
|     X=39, Y=35                                   X=120, Y=35 |
|                                                              |
|                                                  [Upgrades]  |
|                                                  Slot 2, 3, 4|
|                                                  X=182       |
+--------------------------------------------------------------+

- Input Slot (Slot 0): Place raw materials to be processed.
- Output Slot (Slot 1): Collects finished products. Cannot be
  manually stuffed with inputs.
- Upgrade Slots (Slots 2, 3, and 4): Located on the far right.
  These slots accept exclusive speed upgrade items.

----------------------------------------------------------------------
  2. THE THREE OPERATING MODES
----------------------------------------------------------------------

By clicking the Mode Button (located on the GUI), you can cycle
the machine through three specialized states. The header text at
the top of the screen will dynamically update to display the active mode:

- COMPRESSION (Mode 0)
  Consumes large quantities of items to pack them into block form
  (e.g., 9 Synthorium Ingots into 1 Synthorium Block). The machine
  automatically scans global recipes for standard 9-to-1 conversions.

- SINGULARITY (Mode 1)
  Condenses dense materials under gravitational pressure. In this mode,
  the machine consumes input items one-by-one, slowly filling its
  internal accumulator until the singularity threshold (default: 1000)
  is reached before outputting the finalized Singularity.

- DUST CRUSHING (Mode 2)
  Pulverizes ingots and raw materials into fine powders. It features
  an automatic scan that registers custom ingot-to-dust conversion
  recipes from other installed mods.

----------------------------------------------------------------------
  3. INTEGRATED SPEED AND DUPLICATION UPGRADES
----------------------------------------------------------------------

Installing speed upgrades into Slots 2, 3, or 4 will modify the
machine's operation time and yield:

- Speed Upgrade Tier 1: Speeds up operations.
- Speed Upgrade Tier 2: Accelerates processing speed and grants
  a +10% chance to duplicate outputs per upgrade in Compression and
  Dust modes.
- Speed Upgrade Tier 3: Maximizes processing speed and grants
  a +35% chance to duplicate outputs per upgrade in Compression and
  Dust modes.

----------------------------------------------------------------------
  4. ADVANCED AUTOMATION & SIDED ROUTING
----------------------------------------------------------------------

The Compressor is fully compatible with item transport pipes and
applied logistics systems. Side configuration buttons are available
on the left panel (D: Down, U: Up, N: North, S: South, W: West, E: East):

- OFF: Disables pipe interaction on that specific side.
- IN (Blue / Input): Allows automation pipes to push items into Slot 0.
- OUT (Orange / Output): Allows automation pipes to extract finished
  products from Slot 1.

Locking Feature (Lock Button):
When locked, the machine memorizes the item currently in Slot 0 and
will ONLY accept that specific item type. This prevents automation
pipes from inserting unwanted items and clogging the processing line.

----------------------------------------------------------------------
  5. CUSTOM JSON RECIPE TEMPLATES
----------------------------------------------------------------------

Custom recipes can be placed inside the subfolders of this directory:

A) Compressor Recipes (Folder: compressor_recipes/compressor/*.json)
[
  {
    "input": "puremashtweaks:synthorium_ingot",
    "input_count": 9,
    "output": "puremashtweaks:synthorium_block",
    "time_cost": 100,
    "enable_recipe": true
  }
]

B) Singularity Recipes (Folder: compressor_recipes/singularity/*.json)
[
  {
    "name": "Synthorium Singularity",
    "item": "puremashtweaks:synthorium_block",
    "cost": 1000,
    "color0": "#101010",
    "color1": "#00FFFF",
    "add_to_puremashtweaks_singularity_tab": true,
    "add_to_puremash_and_singularity_tag": true,
    "enable_recipe": true,
    "enable_item": true
  }
]

C) Dust Recipes (Folder: compressor_recipes/dust/*.json)
[
  {
    "input": "puremashtweaks:synthorium_ingot",
    "output": "puremashtweaks:synthorium_dust",
    "time_cost": 100,
    "enable_recipe": true
  }
]