---
navigation:
  title: Armazenamento e Utilitários
  icon: puremash_battery
  position: 8
item_ids:
  - puremashtweaks:puremash_battery
  - puremashtweaks:creative_battery
  - puremashtweaks:fluid_tank
  - puremashtweaks:creative_fluid_tank
  - puremashtweaks:chunk_loader
---

# Armazenamento e Utilitários

Baterias de grande porte, reservatórios de fluidos com renderização 3D e carregadores de chunks.

---

## 1. Armazenamento de Energia (Baterias)

<Row gap="10">
  <GameScene zoom="3.5" interactive={true}>
    <ImportStructure src="structures/puremash_battery.nbt" />
  </GameScene>
  <GameScene zoom="3.5" interactive={true}>
    <ImportStructure src="structures/creative_battery.nbt" />
  </GameScene>
  <Column>
    * **Bateria PureMash:** Armazena **50.000.000 FE** com transferência de 1.500.000 FE/t. Mantém a carga armazenada ao ser quebrada.
    * **Bateria Criativa:** Fornece energia infinita (**∞ FE**) a 10.000.000 FE/t.
  </Column>
</Row>

---

## 2. Reservatórios de Fluidos

<Row gap="10">
  <GameScene zoom="3.5" interactive={true}>
    <ImportStructure src="structures/fluid_tank.nbt" />
  </GameScene>
  <GameScene zoom="3.5" interactive={true}>
    <ImportStructure src="structures/creative_fluid_tank.nbt" />
  </GameScene>
  <Column>
    * **Tanque de Fluidos (32.000 mB):** Renderiza o líquido em 3D dentro do vidro no mundo e no inventário. Guarda o conteúdo ao ser quebrado.
    * **Tanque Criativo:** Fornecimento infinito. Clique com qualquer balde de líquido para travar a fonte infinita; use **Shift + Clique Direito** com a mão vazia para limpá-lo.
  </Column>
</Row>

---

## 3. Carregador de Chunks PureMash

O <ItemLink id="puremashtweaks:chunk_loader" /> mantém áreas carregadas e processando constantemente ao seu redor.

<Row gap="10">
  <GameScene zoom="3.5" interactive={true}>
    <ImportStructure src="structures/puremash_chunk_loader.nbt" />
  </GameScene>
  <Column>
    * **Níveis 1 a 3 (1x1 a 5x5 Chunks):** Opera de forma independente.
    * **Níveis 4 a 6 (9x9 a 17x17 Chunks):** Exigem um <ItemLink id="puremashtweaks:moldelonian_core" /> no slot interno do bloco.
    * **Visualizador:** Clique no botão **B** na interface para projetar paredes 3D brilhantes no mundo.
  </Column>
</Row>