======================================================================
    P U R E M A S H   T W E A K S   -   A L C H E M I C A L
                      S Y N T H E S I Z E R
======================================================================

The Alchemical Synthesizer is an advanced thermodynamic machine.
It processes mineral materials using physical tools, fluid catalysts,
and Forge Energy (FE) to execute kinetic, thermal, and hydraulic actions.

----------------------------------------------------------------------
  1. INTERFACE LAYOUT AND POWER INDICATORS
----------------------------------------------------------------------

This machine features a dual-resource layout designed to separate
operating power from process reagents:

- Energy Gauge (Far Left Bar): Shows stored Forge Energy (FE).
  Holds up to 5,000,000 FE. The machine consumes exactly 100 FE multiplied
  by the summed speed multiplier of the upgrades per tick during
  operation (minimum of 100 FE/tick).
- Fluid Catalyst Indicator (Slot 0, X=30, Y=35): Visually displays
  the amount and type of fluid in the internal 8000 mB tank.
- Material Input (Slot 1, X=30, Y=56): Place the item/block to process.
- Tool Input (Slot 2, X=30, Y=77): Place the tool required for the route.
- Output Grid (Slots 3 to 22): A large 5x4 inventory for outputs.
- Upgrade Slots (Slots 23 to 25): Located on the right (X=182) for
  Speed Upgrades.

----------------------------------------------------------------------
  2. INTERACTIVE FLUID ENGINE (CURSOR DRAIN)
----------------------------------------------------------------------

The Fluid Catalyst slot (Slot 0) does not physically hold bucket items.
Instead, it acts as a direct interactable valve:

- Draining: Click Slot 0 with a Water or Lava bucket to instantly
  empty it into the machine. The fluid is added to the tank, and your
  cursor is updated to an empty bucket.
- Filling: Click Slot 0 with an empty bucket while the machine contains
  at least 1000 mB of a fluid to extract it. The cursor becomes a
  filled bucket.

----------------------------------------------------------------------
  3. THE THREE PROCESSING ROUTES (ARROW FEEDBACK)
----------------------------------------------------------------------

Three distinct progress arrows on the GUI provide visual feedback on
the active processing route:

[Top Arrow]    : Hydro-Thermal Route (requires fluid catalyst).
[Middle Arrow] : Central Processing Route (active in all operations).
[Bottom Arrow] : Mechanical-Kinetic Route (requires physical tools).

- ROUTE A: Sifting and Hydraulic Washing (Block Processing Only)
  - Requirements: Water + Shovel (or Paxel) + Block (e.g., Gravel).
  - Outcome: Separates minerals (e.g., Gravel into Flint or Clay).
  - Visual: Top, Middle, and Bottom arrows light up.

- ROUTE B: Kinetic Crushing and Cutting (Block Processing Only)
  - Requirements: Block + Tool (no fluid).
  - Outcome: Crushes stones or mills wood (e.g., Cobblestone + Pickaxe
    into Gravel; Logs + Axe into 6x Planks). Consumes tool durability.
  - Visual: Middle and Bottom arrows light up.

- ROUTE C: Hydrothermal Smelting (Furnace)
  - Requirements: Lava + Input Item (no tool).
  - Outcome: Automatically smelts items (e.g., Raw Ores into Ingots).
    The machine contains an auto-scanner that registers every furnace
    recipe in the game. No physical tools are involved in this route.
  - Visual: Top and Middle arrows light up.

----------------------------------------------------------------------
  4. UPGRADES AND EXTRA OUTPUT DUPLICATION
----------------------------------------------------------------------

Resource duplication has been decoupled from manual tools. Extra output
generation relies strictly on the Speed Upgrades installed in slots
23 to 25:

- Speed Upgrade Tier 2: Grants +10% chance of extra output generation per upgrade.
- Speed Upgrade Tier 3: Grants +35% chance of extra output generation per upgrade.

- Digital Power: Connect standard FE-compatible power cables to any
  side of the block to charge the 5,000,000 FE internal battery.
- Manual Ignition (Redstone Fuel): If you are playing in a testing or
  standalone environment without energy cables, place Redstone Dust
  into the Input Slot (Slot 1) to instantly charge the machine with
  +5,000 FE, or a Redstone Block for +45,000 FE.

----------------------------------------------------------------------
  5. CUSTOM JSON RECIPE TEMPLATE
----------------------------------------------------------------------

Custom recipes can be placed inside the "alchemical_recipes" folder:

Folder: alchemical_recipes/*.json
[
  {
    "input": "minecraft:gravel",
    "fluid": "minecraft:water",
    "fluid_amount": 250,
    "tool_type": "shovel",
    "output": "minecraft:flint",
    "output_count": 1,
    "double_output": false,
    "enable_recipe": true
  }
]