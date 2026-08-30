======================================================================
    P U R E M A S H   T W E A K S   -   A L C H E M I C A L
                      S Y N T H E S I Z E R
======================================================================

The Alchemical Synthesizer is an ultra-fast thermodynamic machine.
It processes raw ores, dusts, and mineral materials using physical
tools, fluid catalysts, and Forge Energy (FE) to execute kinetic,
thermal, and hydraulic reactions.

----------------------------------------------------------------------
  1. INTERFACE LAYOUT AND POWER INDICATORS
----------------------------------------------------------------------

This machine features a multi-resource layout designed to separate
operating electrical power from chemical process reagents:

- Energy Gauge (Far Left Bar): Displays stored Forge Energy (FE).
  Base capacity is 5,000,000 FE (expandable with Capacity Upgrades).
  Energy consumption scales proportionally with recipe difficulty
  (100 to 500 FE/tick base) multiplied by the active speed multiplier.
- Fluid Catalyst Indicator (Slot 0, X=30, Y=35): Visually displays
  the amount and type of fluid inside the internal 16,000 mB tank.
- Material Input (Slot 1, X=30, Y=56): Place the item/dust/ore to process.
- Tool Input (Slot 2, X=30, Y=77): Place the tool required for the route.
- Output Grid (Slots 3 to 22): A large 5x4 inventory (20 slots) for outputs
  with high-throughput 0-tick auto-ejection to adjacent networks (AE2/Cables).
- Upgrade Slots (Slots 23 to 25): Located on the right (X=182) for Speed,
  Capacity, Duplication, and Stack Processing Upgrades.

----------------------------------------------------------------------
  2. HYDRAULIC VALVE & PIPELINE INTEGRATION
----------------------------------------------------------------------

The Fluid Catalyst slot (Slot 0) acts as an interactive hydraulic valve
and supports automated fluid pipelines (Universal Cables, pipes, etc.):

- Draining: Click Slot 0 with any fluid container (Water, Lava, Molten
  Synthorium, etc.) to immediately drain it into the tank.
- Extraction: Click Slot 0 with an empty bucket when the tank contains
  at least 1,000 mB to extract a filled bucket.
- Fluid Automation: The block accepts all fluids dynamically via NeoForge
  capabilities from any side.

----------------------------------------------------------------------
  3. THE FOUR PROCESSING ROUTES (ARROW FEEDBACK)
----------------------------------------------------------------------

Three distinct animated arrows on the GUI provide real-time visual
feedback on the active operational route:

[Top Arrow]    : Hydro-Thermal Route (active when fluid is consumed).
[Middle Arrow] : Central Processing Route (active in all operations).
[Bottom Arrow] : Mechanical-Kinetic Route (active when tools are used).

- ROUTE A: Sifting and Hydraulic Washing (Block Processing)
  - Requirements: Water (250 mB) + Shovel/Paxel + Input Block (e.g., Gravel).
  - Outcome: Separates minerals (e.g., Gravel into Flint or Clay).
  - Visual: Top, Middle, and Bottom arrows light up.

- ROUTE B: Kinetic Crushing and Cutting (Mechanical Processing)
  - Requirements: Tool + Input Item/Block (no fluid needed).
  - Outcome: Crushes stone or cuts timber (e.g., Cobblestone + Pickaxe
    into Gravel; Logs + Axe into 6x Planks). Consumes tool durability.
  - Visual: Middle and Bottom arrows light up.

- ROUTE C: Thermodynamic Electric Smelting (Furnace Mode)
  - Requirements: Input Item + Forge Energy (no fluids or tools required).
  - Outcome: Automatically smelts any vanilla/modded dust or raw ore
    into its ingot/gem form using pure electricity.
  - Dynamic Smelting Duration & Energy Scaling:
    * Tier 1 (Standard: Iron, Copper, Gold, Food): 1.0s (20t) @ 100 FE/t.
    * Tier 2 (Rare: Diamond, Emerald, Uranium, Platinum): 1.25s (25t) @ 180 FE/t.
    * Tier 3 (Ancient: Netherite, Ancient Debris): 1.5s (30t) @ 300 FE/t.
    * Tier 4 (Supreme/Endgame: Synthorium, Moldelonian, Allthemodium,
      Vibranium, Unobtainium, Insanium): 1.75s (35t) @ 500 FE/t.
  - Visual: Middle arrow lights up.

- ROUTE D: Alchemical Synthesis & Super-Smelting
  - Requirements: Fluid (Lava/Water) + Physical Tool + Custom Reagents.
  - Outcome: Executes advanced custom JSON recipes, alloy synthesis,
    and high-yield double output routes.
  - Visual: Top, Middle, and Bottom arrows light up based on route.

----------------------------------------------------------------------
  4. UPGRADES AND OUTPUT SCALING
----------------------------------------------------------------------

- Speed Upgrade Tier 1: Increases operation speed (+2 power).
- Speed Upgrade Tier 2: Significantly increases operation speed (+4 power).
- Speed Upgrade Tier 3: Exponentially accelerates processing speed
  (+16, +64, and up to +256 power when fully loaded with 3x cards).
- Capacity Upgrades: Multiplies internal energy capacity (up to 100M+ FE).
- Duplication Upgrades: Grants a chance to duplicate output items (+15% / +50%).
- Stack Processing Upgrade: Processes an entire stack (up to 64 items)
  simultaneously in a single cycle.
- Manual Redstone Charging: Place Redstone Dust into Slot 1 for +5,000 FE,
  or a Redstone Block for +45,000 FE.

----------------------------------------------------------------------
  5. RECIPE DECLARATION: JSON CONFIG & KUBEJS
----------------------------------------------------------------------

A. CUSTOM JSON CONFIG TEMPLATE:
Place custom JSON files inside: config/PureMash Tweaks/alchemical_recipes/

[
  {
    "input": "minecraft:gravel",
    "fluid": "minecraft:water",
    "fluid_amount": 250,
    "tool_type": "shovel",
    "output": "minecraft:flint",
    "output_count": 1,
    "time": 20,
    "energy": 100,
    "double_output": false,
    "enable_recipe": true
  },
  {
    "input": "minecraft:raw_iron",
    "fluid": "minecraft:lava",
    "fluid_amount": 250,
    "tool_type": "pickaxe",
    "output": "minecraft:iron_ingot",
    "output_count": 2,
    "time": 30,
    "energy": 150,
    "double_output": true,
    "enable_recipe": true
  }
]

B. KUBEJS RECIPE SCRIPT:
Place inside: kubejs/server_scripts/puremash_alchemical.js

ServerEvents.recipes(event => {
    event.recipes.puremashtweaks.alchemical('minecraft:flint', 'minecraft:gravel')
        .outputCount(1)
        .fluid('minecraft:water')
        .fluidAmount(250)
        .toolType('shovel')
        .time(20)
        .energy(100)
        .doubleOutput(false)
        .id('kubejs:alchemical_gravel_to_flint')
})
======================================================================