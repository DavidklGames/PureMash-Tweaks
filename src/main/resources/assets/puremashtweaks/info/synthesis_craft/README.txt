======================================================================
    P U R E M A S H   T W E A K S   -   S Y N T H E S I S   T A B L E
======================================================================

The Synthesis Table is an ultra-large 9x9 automated workbench. It is
capable of assembling complex high-tier recipes, projecting 3D in-world
holograms, and interfacing directly with digital warehouse networks (AE2)
using transaction-safe Transfer API protocols.

----------------------------------------------------------------------
  1. INTERFACE & SLOT OVERVIEW
----------------------------------------------------------------------

- 9x9 Matrix (Slots 0 to 80): The primary 81-slot crafting matrix.
- Result Slot (Slot 81): Where the crafted product appears. Features
  high-speed 0-tick auto-ejection to adjacent conduits, cables, and chests.
- Memory Card Slot (Slot 82): Dedicated slot for encoding and reading
  Memory Cards. Active and visible only when Automation is set to "On".

----------------------------------------------------------------------
  2. AUTOMATION & RECIPE ENCODING MODES
----------------------------------------------------------------------

The left panel of the interface provides control buttons for automation:

- ACTIVE TOGGLE (On / Off)
  - Off: Functions as a standard manual 9x9 crafting workbench.
  - On: Enables smart automation, opens Slot 82 for Memory Cards, and
    activates ghost recipe overlays and automated logistics filtering.

- AUTOMATION MODE (Craft / Add)
  - Craft Mode (Mode 0):
    Standard automated processing. When an encoded Memory Card is inserted
    into Slot 82, the table restricts external insertions (AE2 / Universal
    Cables) to only the exact slots and item types required by the pattern.
    Balanced distribution guarantees that items are never misrouted.

  - Add / Encoder Mode (Mode 1):
    Used to write recipes onto a Memory Card.
    1. Turn Automation "On" and switch Mode to "Add".
    2. Arrange physical items in the 9x9 grid in the desired pattern.
    3. Place an empty Memory Card into Slot 82.
    4. Click the "Save" button on the left panel. The recipe pattern and
       result item are permanently encoded onto the card.

----------------------------------------------------------------------
  3. GHOST ITEM VISUALIZATION & 3D HOLOGRAMS
----------------------------------------------------------------------

- In-GUI Liquid Glass Overlay:
  When an encoded Memory Card is placed in Slot 82 during Automation mode,
  the GUI projects a semi-transparent ghost preview over empty slots,
  illustrating the exact required ingredient placement.

- In-World 3D Hologram:
  When an encoded Memory Card is inserted and Automation is active, a 3D
  rotating hologram of the crafted item floats above the physical block.

- Clearing Memory Cards:
  To wipe a configured Memory Card, hold it in hand, look straight up
  (pitch < -60 degrees), and right-click.

----------------------------------------------------------------------
  4. AE2 & TRANSACTION-SAFE LOGISTICS
----------------------------------------------------------------------

The Synthesis Table utilizes NeoForge Transfer APIs and a Snapshot Journal
to safeguard automation pipelines:

- Transaction Rollback: If an external delivery from AE2 or pipes fails
  mid-operation, the table performs an instantaneous rollback, ensuring
  no partial recipe clutter is left behind on the grid.
- Balanced Pattern Feeding: Automatically distributes multi-quantity
  ingredients evenly across matching slots during continuous crafting cycles.

----------------------------------------------------------------------
  5. RECIPE DECLARATION: JSON CONFIG & KUBEJS
----------------------------------------------------------------------

A) 9x9 SHAPED SYNTHESIS RECIPES

- JSON Config: config/PureMash Tweaks/synthesis_recipes/shaped/*.json
[
  {
    "pattern": [
      " MMSDSMM ",
      "MSSDIDSSM",
      "MSDIMIDSM",
      "SDIDSDIDS",
      "DIMSNSMID",
      "SDIDSDIDS",
      "MSDIMIDSM",
      "MSSDIDSSM",
      " MMSDSMM "
    ],
    "key": {
      "M": "puremashtweaks:moldelonian_block",
      "S": "puremashtweaks:synthorium_block",
      "I": "puremashtweaks:synthorium_ingot",
      "D": "minecraft:diamond_block",
      "N": "minecraft:nether_star"
    },
    "result": "puremashtweaks:moldelonian_core",
    "result_count": 1,
    "enable_recipe": true
  }
]

- KubeJS Script: kubejs/server_scripts/synthesis_recipes.js
ServerEvents.recipes(event => {
    event.recipes.puremashtweaks.shaped_synthesis(
        'puremashtweaks:moldelonian_core',
        [
            ' MMSDSMM ',
            'MSSDIDSSM',
            'MSDIMIDSM',
            'SDIDSDIDS',
            'DIMSNSMID',
            'SDIDSDIDS',
            'MSDIMIDSM',
            'MSSDIDSSM',
            ' MMSDSMM '
        ],
        {
            M: 'puremashtweaks:moldelonian_block',
            S: 'puremashtweaks:synthorium_block',
            I: 'puremashtweaks:synthorium_ingot',
            D: 'minecraft:diamond_block',
            N: 'minecraft:nether_star'
        }
    ).id('kubejs:synthesis_moldelonian_core')
})


B) 9x9 SHAPELESS SYNTHESIS RECIPES

- JSON Config: config/PureMash Tweaks/synthesis_recipes/shapeless/*.json
[
  {
    "ingredients": [
      "puremashtweaks:synthorium_ingot",
      "minecraft:netherite_ingot",
      "minecraft:gold_ingot",
      "minecraft:iron_ingot",
      "minecraft:copper_ingot",
      "puremashtweaks:puremash_core"
    ],
    "result": "puremashtweaks:moldelonian_ingot",
    "result_count": 1,
    "enable_recipe": true
  }
]

- KubeJS Script:
ServerEvents.recipes(event => {
    event.recipes.puremashtweaks.shapeless_synthesis(
        'puremashtweaks:moldelonian_ingot',
        [
            'puremashtweaks:synthorium_ingot',
            'minecraft:netherite_ingot',
            'minecraft:gold_ingot',
            'minecraft:iron_ingot',
            'minecraft:copper_ingot',
            'puremashtweaks:puremash_core'
        ]
    ).id('kubejs:synthesis_shapeless_moldelonian_ingot')
})
======================================================================