---
navigation:
  title: Cabos Universais e Logística
  icon: synthorium_universal_cable
  position: 7
item_ids:
  - puremashtweaks:synthorium_universal_cable
  - puremashtweaks:moldelonian_universal_cable
  - puremashtweaks:configuration_wrench
  - puremashtweaks:distribution_filter
---

# Cabos Universais e Logística

Os <Color id="puremashtweaks:cyan_glow">Cabos Universais</Color> são dutos 3 em 1 que transportam **Itens**, **Fluidos** e **Energia (FE)** simultaneamente pelo mesmo bloco físico.

<Row gap="10">
  <GameScene zoom="3.5" interactive={true}>
    <ImportStructure src="structures/synthorium_universal_cable.nbt" />
  </GameScene>
  <GameScene zoom="3.5" interactive={true}>
    <ImportStructure src="structures/moldelonian_universal_cable.nbt" />
  </GameScene>
  <Column>
    * **Nível Sintório:** 50.000 FE/t | 1.000 mB/t | 8 itens/ciclo
    * **Nível Moldeloniano:** 100.000 FE/t | 10.000 mB/t | 64 itens/ciclo
  </Column>
</Row>

<RecipeFor id="puremashtweaks:synthorium_universal_cable" />
<RecipeFor id="puremashtweaks:moldelonian_universal_cable" />

---

## 1. Configuração com a Chave

Segure uma <ItemLink id="puremashtweaks:configuration_wrench" /> e use **Shift + Clique Direito** na face conectada do cabo para alternar:
1. **Inserir (Padrão):** Empurra recursos para dentro do contêiner conectado.
2. **Extrair (Bico):** Puxa recursos para fora do contêiner.
3. **Desconectar:** Corta a conexão física daquela face.

<RecipeFor id="puremashtweaks:configuration_wrench" />

---

## 2. Interface e Canais do Cabo

Clique com a mão vazia em qualquer **Bico de Extração** ativo para abrir o menu do cabo. Use as abas da esquerda para escolher o canal:
* **Canal de Energia (Aba 0):** Roteamento elétrico.
* **Canal de Itens (Aba 1):** Filtros e envio de itens.
* **Canal de Fluidos (Aba 2):** Filtros e envio de líquidos.

---

## 3. Modos de Distribuição e Redstone

Instale um **Upgrade de Velocidade** no slot do cabo para liberar os controles de roteamento:
* **Modos de Distribuição:** Alternado (Round-Robin), Aleatório (Random) ou Dinâmico (divisão gradual e equilibrada).
* **Controle de Redstone:** Ignorado, Ativo com Sinal ou Ativo sem Sinal.
* **Política do Filtro:** Whitelist (Permitir) ou Blacklist (Bloquear).

---

## 4. Filtros de Distribuição (Destinos Vinculados)

Use **Shift + Clique Direito** em qualquer contêiner do mundo com um <ItemLink id="puremashtweaks:distribution_filter" /> para vincular as coordenadas e a dimensão.

<RecipeFor id="puremashtweaks:distribution_filter" />

Inserir o filtro vinculado na interface do cabo desbloqueia:
* **Prioridade de Destino (1 a 99):** Contêineres com maior prioridade são abastecidos primeiro.
* **Limite de Estoque (Stock Limit):** Limita a quantidade máxima de itens no destino (padrão: **64**).
* **Sequência de Slots:** Direciona os itens para slots específicos (ex: `6, 1, 4`).
* **Limite de Durabilidade:** Extrai ferramentas danificadas quando a durabilidade cai abaixo do valor configurado (ex: `<= 25%` ou `< 500`).