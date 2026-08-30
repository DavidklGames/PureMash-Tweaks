---
navigation:
  title: Sintório
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

# Sintório

O <Color id="puremashtweaks:cyan_glow">Sintório</Color> é um metal energético encontrado nas camadas mais profundas do Overworld. Ele serve como base para toda a tecnologia avançada do PureMash Tweaks.

---

## 1. Localização e Forja de Lingotes

<Row gap="10">
  <GameScene zoom="3.5" interactive={true}>
    <ImportStructure src="structures/synthorium_debris.nbt" />
  </GameScene>
  <Column>
    O <ItemLink id="puremashtweaks:synthorium_debris" /> é gerado naturalmente entre as camadas **Y = -64** e **Y = 0**. Funda-o em uma fornalha comum ou alto-forno para obter <ItemLink id="puremashtweaks:synthorium_scrap" />.
  </Column>
</Row>

<RecipeFor id="puremashtweaks:synthorium_scrap" />

### Receita do Lingote
Una **4x Fragmentos de Sintório** e **4x Diamantes** em qualquer grade de criação para forjar um <ItemLink id="puremashtweaks:synthorium_ingot" />.

<RecipeFor id="puremashtweaks:synthorium_ingot" />

---

## 2. Componentes e Alimentação

O Sintório pode ser compactado em blocos, dividido em pepitas, moldado em bastões reforçados ou moído em pó fino.

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

### Maçã de Sintório
Cerque uma Maçã comum com 8x Lingotes de Sintório para sintetizar a <ItemLink id="puremashtweaks:synthorium_apple" />. Ao consumi-la, você recebe:
* **Absorção II** (2:00)
* **Resistência II** (0:45)
* **Regeneração III** (0:20)
* **Resistência ao Fogo** (3:00)
* **Velocidade II** (1:00)

<RecipeFor id="puremashtweaks:synthorium_apple" />

---

## 3. Ferramentas e o Pacachado de Sintório

As ferramentas de Sintório possuem alta durabilidade (3.000 usos) e velocidade de mineração de 15.0, superando facilmente os equipamentos de Netherita.

<ItemGrid>
  <ItemIcon id="puremashtweaks:synthorium_sword" />
  <ItemIcon id="puremashtweaks:synthorium_pickaxe" />
  <ItemIcon id="puremashtweaks:synthorium_axe" />
  <ItemIcon id="puremashtweaks:synthorium_shovel" />
  <ItemIcon id="puremashtweaks:synthorium_hoe" />
</ItemGrid>

Junte a picareta, o machado, a pá, um lingote e um bastão na bancada para criar o versátil **Pacachado de Sintório** (25.0 de Dano Base).

<RecipeFor id="puremashtweaks:synthorium_paxel" />

---

## 4. Armadura e Voo Criativo

O conjunto completo de armadura de Sintório concede **34 pontos de Defesa**, **20.0 de Dureza de Armadura** e **80% de Resistência à Repulsão**.

<ItemGrid>
  <ItemIcon id="puremashtweaks:synthorium_helmet" />
  <ItemIcon id="puremashtweaks:synthorium_chestplate" />
  <ItemIcon id="puremashtweaks:synthorium_leggings" />
  <ItemIcon id="puremashtweaks:synthorium_boots" />
</ItemGrid>

> **Voo com Sobrecarga:** Encantar todas as 4 peças da armadura com **Sobrecarga** desbloqueia o voo criativo. Pressione <KeyBind id="key.puremashtweaks.toggle_flight" /> para ligar ou desligar.