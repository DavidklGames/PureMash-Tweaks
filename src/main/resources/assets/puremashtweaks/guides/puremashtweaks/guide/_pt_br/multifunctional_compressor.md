---
navigation:
  title: Compressor Multifuncional
  icon: multifunctional_compressor
  position: 4
item_ids:
  - puremashtweaks:multifunctional_compressor
---

# Compressor Multifuncional

O <Color id="puremashtweaks:cyan_glow">Compressor Multifuncional</Color> é uma máquina versátil capaz de compactar blocos, condensar matéria em singularidades e pulverizar minérios ou lingotes em pó.

<Row gap="10">
  <GameScene zoom="3.5" interactive={true}>
    <ImportStructure src="structures/multifunctional_compressor.nbt" />
  </GameScene>
  <Column>
    Possui uma reserva interna de **5.000.000 FE**, suporte a upgrades de velocidade e capacidade, além de trava de receita para não entupir linhas automáticas.
  </Column>
</Row>

<RecipeFor id="puremashtweaks:multifunctional_compressor" />

---

## 1. Interface e Slots

* **Slot de Entrada (Slot 0):** Coloque materiais brutos, minérios ou lingotes.
* **Slot de Saída (Slot 1):** Recolhe o produto final processado.
* **Slots de Upgrades (Slots 2, 3 e 4):** Localizados à direita. Aceitam upgrades de Velocidade, Capacidade, Duplicação e Processamento em Pack.

---

## 2. Modos de Operação

Clique no **Botão de Modo** na interface para alternar entre as três funções:

#### Modo de Compressão (Modo 0 - 50 FE/t)
Empacota 9 itens em formato de bloco (ex: 9 <ItemLink id="puremashtweaks:synthorium_ingot" /> em 1 <ItemLink id="puremashtweaks:synthorium_block" />). Escaneia receitas automaticamente para conversões de 9 para 1.

#### Modo de Singularidades (Modo 1 - 250 FE/t)
Condensa grandes quantidades de itens sob pressão gravitacional. A máquina consome os itens inseridos para alimentar um acumulador interno até atingir a meta (padrão: 1.000 itens) e liberar a Singularidade pronta.

#### Modo Triturador de Pó (Modo 2 - 100 FE/t)
Pulveriza lingotes, gemas e minérios brutos em pós finos. Concede duplicação automática ao triturar minérios brutos (1 minério bruto = 2 pós).

---

## 3. Trava de Receita

Clique no **Botão de Cadeado** para evitar misturas indesejadas na automação:
* **Travado (Lock):** Memoriza o item que está no slot 0 e bloqueia a entrada de qualquer outro tipo de material.
* **Destravado (Free):** Aceita qualquer item compatível com o modo ativo.