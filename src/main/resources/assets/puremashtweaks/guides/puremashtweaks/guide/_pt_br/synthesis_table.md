---
navigation:
  title: Mesa de Síntese
  icon: synthesis_table
  position: 3
item_ids:
  - puremashtweaks:synthesis_table
  - puremashtweaks:memory_card
---

# Mesa de Síntese

A <Color id="puremashtweaks:cyan_glow">Mesa de Síntese</Color> é uma bancada de criação automatizada de 9x9, projetada para fabricar receitas complexas de fim de jogo, núcleos densos e chips criativos.

<Row gap="10">
  <GameScene zoom="3.5" interactive={true}>
    <ImportStructure src="structures/synthesis_table.nbt" />
  </GameScene>
  <Column>
    Com 81 slots na grade, proteção contra travamentos por rollback e auto-ejeção instantânea para redes vizinhas (AE2 e cabos), ela processa receitas automatizadas com máxima estabilidade.
  </Column>
</Row>

<RecipeFor id="puremashtweaks:synthesis_table" />

---

## 1. Interface e Cartões de Memória

* **Grade 9x9 (Slots 0 a 80):** Grade principal para posicionar os ingredientes.
* **Slot de Saída (Slot 81):** Exibe e entrega o item fabricado.
* **Slot do Cartão de Memória (Slot 82):** Fica visível apenas quando a **Automação** está ligada (**On**). Serve para inserir ou gravar receitas.

<RecipeFor id="puremashtweaks:memory_card" />

---

## 2. Modos de Automação

Use os botões no painel esquerdo para ajustar o funcionamento:

#### Botão de Ativação (On / Off)
Liga a automação e libera o slot do Cartão de Memória (Slot 82).

#### Modo de Criação (Modo 0 - Craft)
Operação padrão. Com um Cartão de Memória gravado no slot 82, os tubos e o AE2 só conseguem colocar itens exatamente nos slots corretos do padrão da receita.

#### Modo Gravador (Modo 1 - Add)
Grava o padrão da receita em um cartão virgem:
1. Ligue a Automação (**On**) e defina o modo para **Add**.
2. Posicione os itens físicos na grade 9x9 no formato desejado.
3. Coloque um <ItemLink id="puremashtweaks:memory_card" /> vazio no slot 82.
4. Clique no botão **Save** no painel esquerdo. A receita será salva no cartão e os itens continuarão na grade.

---

## 3. Hologramas e Limpeza de Cartões

* **Projeção Fantasma (Liquid Glass):** Com a automação ligada e um cartão gravado inserido, a mesa exibe uma prévia semitransparente dos itens necessários direto na interface.
* **Holograma 3D no Mundo:** Projeta um modelo giratório em 3D do item de saída flutuando sobre o bloco.
* **Como Limpar Cartões:** Segure o cartão gravado, olhe diretamente para cima (ângulo menor que -60°) e clique com o botão direito para apagá-lo.

---

## 4. Logística e Segurança

* **Auto-Ejeção 0-Tick:** Assim que a receita termina, a mesa empurra o produto de saída para contêineres vizinhos ou Interfaces de Padrão do AE2 sem atraso.
* **Rollback de Segurança:** Se a automação externa fornecer apenas parte dos itens e parar, a mesa restaura o estado anterior para não deixar a grade travada com itens incompletos.