---
navigation:
  title: Sintetizador Alquímico
  icon: alchemical_synthesizer
  position: 5
item_ids:
  - puremashtweaks:alchemical_synthesizer
---

# Sintetizador Alquímico

O <Color id="puremashtweaks:cyan_glow">Sintetizador Alquímico</Color> é uma máquina termodinâmica de quatro rotas. Ele processa minérios, pós e blocos combinando ferramentas, fluidos catalisadores e energia elétrica (FE).

<Row gap="10">
  <GameScene zoom="3.5" interactive={true}>
    <ImportStructure src="structures/alchemical_synthesizer.nbt" />
  </GameScene>
  <Column>
    Conta com um tanque hidráulico interno de **16.000 mB**, buffer de **5.000.000 FE** e uma grade de saída com 20 slots com auto-ejeção automática.
  </Column>
</Row>

<RecipeFor id="puremashtweaks:alchemical_synthesizer" />

---

## 1. Interface e Válvula de Fluidos

* **Válvula de Fluido (Slot 0):** Conector do tanque interno. Clique com um balde de Água ou Lava para abastecer, ou clique com um balde vazio para retirar 1.000 mB.
* **Entrada de Materiais (Slot 1):** Local para colocar pós, minérios ou blocos.
* **Catalisador de Ferramenta (Slot 2):** Aceita picaretas, pás, machados ou pacachados.
* **Grade de Saída (Slots 3 a 22):** 20 slots de saída com envio automático para inventários adjacentes.
* **Slots de Upgrades (Slots 23 a 25):** Localizados à direita, para upgrades de Velocidade, Capacidade, Duplicação e Processamento em Pack.

---

## 2. As Quatro Rotas de Processamento

As três setas indicam visualmente qual rota está ativa durante a operação:

#### Rota A: Peneiramento e Lavagem (Hidrotérmica)
* **Requer:** Água (250 mB) + Pá/Pacachado + Bloco de entrada.
* **Resultado:** Lava e separa materiais (ex: Cascalho vira Pederneira ou Argila).
* **Visual:** Acende as setas Superior, Central e Inferior.

#### Rota B: Trituração Mecânica e Corte (Cinética)
* **Requer:** Ferramenta + Item/Bloco (sem fluidos).
* **Resultado:** Esmaga pedras ou corta madeiras (ex: Pedregulho + Picareta vira Cascalho; Troncos + Machado viram 6x Tábuas). Gasta durabilidade da ferramenta.
* **Visual:** Acende as setas Central e Inferior.

#### Rota C: Fundição Termodinâmica (Modo Fornalha Elétrica)
* **Requer:** Item de entrada + Energia (sem fluidos ou ferramentas).
* **Resultado:** Funde qualquer pó ou minério vanilla/modded em lingotes usando eletricidade pura (100 a 500 FE/t).
* **Visual:** Acende apenas a seta Central.

#### Rota D: Síntese Alquímica
* **Requer:** Fluido catalisador + Ferramenta + Reagentes específicos.
* **Resultado:** Executa receitas customizadas via JSON, fusão de ligas e rotas de rendimento duplo.

---

## 3. Carga Manual com Redstone

Caso esteja operando sem cabos de energia, coloque **Pó de Redstone** no slot 1 para receber **+5.000 FE**, ou um **Bloco de Redstone** para **+45.000 FE**.