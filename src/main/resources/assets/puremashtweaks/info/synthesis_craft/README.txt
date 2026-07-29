======================================================================
    P U R E M A S H   T W E A K S   -   S Y N T H E S I S   T A B L E
======================================================================

The Synthesis Table is an ultra-large 9x9 automated workbench. It is
capable of crafting complex high-tier recipes and interfacing directly
with digital warehouse storage networks using transaction-safe protocols.

----------------------------------------------------------------------
  1. INTERFACE OVERVIEW
----------------------------------------------------------------------

- 9x9 Matrix (Slots 0 to 80): The main layout grid for ingredients.
- Result Slot (Slot 81): Where the processed item appears.
- Card Slot (Slot 82): Only accepts Memory Cards. Visible only when
  Automation is turned "On".

----------------------------------------------------------------------
  2. AUTOMATION MODES AND RECIPE SAVING
----------------------------------------------------------------------

The Synthesis Table features two main buttons on the left panel to
control automation behaviors:

- ACTIVE BUTTON (On / Off)
  - Off: Behaves like a standard manual 9x9 crafting table.
  - On: Enables automation. It opens Slot 82 for Memory Card insertion
    and locks standard player interactions depending on the mode below.

- AUTOMATION MODE BUTTON (Craft / Add)
  - Craft Mode (Mode 0):
    Standard automated processing. When a recipe is saved to a
    Memory Card and placed in Slot 82, the table restricts automated
    inputs (pipes/AE2) to only fit the exact matching pattern. This
    guarantees that ingredients are never placed in the wrong slot.

  - Add / Encoder Mode (Mode 1):
    Used to write recipes onto a card.
    1. Turn Automation "On" and set the Mode to "Add".
    2. Place your physical items in the 9x9 grid in the desired pattern.
    3. Put an empty Memory Card in Slot 82.
    4. Click the "Save" button on the left. The recipe details are
       permanently saved on the card, and your items remain safe on the grid.

----------------------------------------------------------------------
  3. GHOST ITEM VISUALIZATION (LIQUID GLASS OVERLAY)
----------------------------------------------------------------------

When a configured Memory Card is placed in Slot 82 and Automation is
active, the table displays a semi-transparent "Liquid Glass" projection
of the recipe onto the grid.

- This shows exactly where items need to be inserted.
- The projection is strictly visual and does not block standard item
  placement or manual crafting.
- To clear a configured Memory Card, hold it in your hand, look
  directly up (angle less than -60 degrees), and right-click.

----------------------------------------------------------------------
  4. INTER-MOD SYSTEM TRANSPORTS (AE2 & PIPES)
----------------------------------------------------------------------

The Synthesis Table communicates natively with logistics networks
using modern Transfer APIs. It utilizes a Snapshot Journal to secure
operations:

- Transaction Safety: If an external system tries to craft a recipe
  but fails halfway (e.g., runs out of a specific ingredient), the table
  performs an instantaneous rollback. This prevents half-crafted patterns
  from leaving clutter on the grid.
- Recipe Cycling ("Change" Button): If a pattern matches multiple
  vanilla recipes (such as custom wood templates), the "Change" button
  becomes visible, allowing the player to cycle through all valid outputs.

----------------------------------------------------------------------
  5. CUSTOM JSON RECIPE TEMPLATES (SHAPED & SHAPELESS)
----------------------------------------------------------------------

Custom 9x9 recipes can be placed inside the "shaped" and "shapeless"
subdirectories.

A) Shaped Synthesis Recipe (Folder: synthesis_recipes/shaped/*.json)
[
  {
    "pattern": [
      "         ",
      "         ",
      "         ",
      "         ",
      "         ",
      "         ",
      "         ",
      "         ",
      "         "
    ],
    "key": {},
    "result": "minecraft:air",
    "result_count": 1,
    "enable_recipe": false
  }
]

B) Shapeless Synthesis Recipe (Folder: synthesis_recipes/shapeless/*.json)
[
  {
    "ingredients": [],
    "result": "minecraft:air",
    "result_count": 1,
    "enable_recipe": false
  }
]