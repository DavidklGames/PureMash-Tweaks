---
navigation:
  title: Gerador PureMash
  icon: puremash_generator
  position: 6
item_ids:
  - puremashtweaks:puremash_generator
---

# Gerador PureMash

O <Color id="puremashtweaks:cyan_glow">Gerador PureMash</Color> converte combustíveis de alta densidade mineral, urânio e ligas energéticas em grandes cargas de Forge Energy (FE).

<Row gap="10">
  <GameScene zoom="3.5" interactive={true}>
    <ImportStructure src="structures/puremash_generator.nbt" />
  </GameScene>
  <Column>
    Equipado com capacidade base de **400.000.000 FE** (expansível até 2 Bilhões de FE), tanque de resfriamento de 20.000 mB e saída de Vapor de 100.000 mB.
  </Column>
</Row>

---

## 1. Espectro de Combustíveis

O gerador consome desde combustíveis clássicos até minerais de altíssima densidade:

* **Bloco / Lingote de Moldeloniano:** 15.000 FE/t (12.000t) / 5.000 FE/t (1.200t)
* **Bloco / Lingote de Urânio:** 8.000 FE/t (18.000t) / 3.000 FE/t (1.800t)
* **Bloco / Lingote de Sintório:** 2.500 FE/t (6.000t) / 1.000 FE/t (600t)
* **Redstone e Carvão:** Taxas convencionais de queima.

---

## 2. Termodinâmica e Resfriamento por Água

A queima de combustíveis densos eleva a temperatura do núcleo até **1.500 °C**.
* **Superaquecimento:** Sem refrigeração, temperaturas acima de 200 °C reduzem a eficiência da geração de energia.
* **Resfriamento com Água:** Abastecer o tanque de 20.000 mB com água absorve o calor, mantém o gerador operando em 100% de eficiência e transforma a água em **Vapor**.

---

## 3. Subprodutos Minerais e Carregamento

* **Subprodutos (Slots 4 a 6):** A queima de combustíveis densos tem chance de gerar pepitas residuais (<ItemLink id="puremashtweaks:moldelonian_nugget" />, <ItemLink id="puremashtweaks:synthorium_nugget" /> ou Pepitas de Urânio).
* **Porta de Carregamento (Slot 7):** Insira itens elétricos recarregáveis (como o <ItemLink id="puremashtweaks:moldelonian_core" />) para carregá-los a 50.000 FE/t.