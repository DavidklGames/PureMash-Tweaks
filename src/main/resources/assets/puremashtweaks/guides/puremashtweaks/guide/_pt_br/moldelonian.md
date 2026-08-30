---
navigation:
  title: Moldeloniano
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

# Moldeloniano

O <Color id="puremashtweaks:moldelonian_gold">Moldeloniano</Color> é a liga metálica definitiva do mod, resultante da unificação de metais básicos, minérios preciosos e lingotes de outros mods.

---

## 1. Arqueologia no End e Molde de Forja

<Row gap="10">
  <GameScene zoom="3.5" interactive={true}>
    <ImportStructure src="structures/suspicious_end_stone.nbt" />
  </GameScene>
  <Column>
    Use um Pincel na <ItemLink id="puremashtweaks:suspicious_end_stone" /> na superfície das ilhas do End (End Highlands e arredores de End Cities) para encontrar o <ItemLink id="puremashtweaks:moldelonian_smithing_template" />.
  </Column>
</Row>

### Duplicação do Molde
Após obter o molde, você pode duplicá-lo na bancada usando 7x Pedras do Fim e 1x <ItemLink id="puremashtweaks:moldelonian_ingot" />.

<RecipeFor id="puremashtweaks:moldelonian_smithing_template" />

---

## 2. Processamento e Alimentação

Os lingotes de Moldeloniano podem ser criados na Mesa de Síntese 9x9 ou fundidos a partir do pó.

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

### Maçã de Moldeloniano
Cerque uma Maçã de Sintório com 8x Lingotes de Moldeloniano para criar a <ItemLink id="puremashtweaks:moldelonian_apple" />. Ao consumi-la, você recebe:
* **Vida Instantânea II**
* **Absorção V** (3:00)
* **Resistência IV** (0:35)
* **Regeneração V** (0:30)
* **Força III** (1:30)
* **Resistência ao Fogo** (5:00)

<RecipeFor id="puremashtweaks:moldelonian_apple" />

---

## 3. Aprimoramentos de Ferraria e o Pacachado

Aprimore seus equipamentos de Sintório na Mesa de Ferraria convencional usando o Molde de Forja e um Lingote de Moldeloniano.

<ItemGrid>
  <ItemIcon id="puremashtweaks:moldelonian_sword" />
  <ItemIcon id="puremashtweaks:moldelonian_pickaxe" />
  <ItemIcon id="puremashtweaks:moldelonian_axe" />
  <ItemIcon id="puremashtweaks:moldelonian_shovel" />
  <ItemIcon id="puremashtweaks:moldelonian_hoe" />
</ItemGrid>

### Pacachado de Moldeloniano (65.0 de Dano)
O <ItemLink id="puremashtweaks:moldelonian_paxel" /> junta picareta, pá e machado com **65.0 de Dano Base** e 6.500 de durabilidade. Quando encantado com Sobrecarga, ele quebra Rocha Matriz (Bedrock).

---

## 4. Atributos da Armadura de Moldeloniano

O conjunto completo de Armadura de Moldeloniano oferece:
* **100 pontos totais de Defesa** (18 Capacete, 36 Peitoral, 28 Calças, 18 Botas)
* **50.0 de Dureza de Armadura** (12.5 por peça)
* **100% de Imunidade à Repulsão**
* Total compatibilidade com o Voo de Sobrecarga e as habilidades de Overdrive.

<ItemGrid>
  <ItemIcon id="puremashtweaks:moldelonian_helmet" />
  <ItemIcon id="puremashtweaks:moldelonian_chestplate" />
  <ItemIcon id="puremashtweaks:moldelonian_leggings" />
  <ItemIcon id="puremashtweaks:moldelonian_boots" />
</ItemGrid>