---
navigation:
  title: Compatibilidade entre Mods
  icon: synthorium_plate
  position: 12
---

# Compatibilidade entre Mods

O PureMash Tweaks foi desenvolvido para funcionar em conjunto com grandes mods técnicos, mágicos e de geração de recursos.

---

## 1. Productive Bees

Duas abelhas exclusivas automatizam a produção de recursos do mod:

### SynthBee (Abelha Nível 4)
* **Cruzamento:** Cruze uma **Netherite Bee** com uma **Diamond Bee**.
* **Bloco de Flor:** Exige um <ItemLink id="puremashtweaks:synthorium_block" />.
* **Produção:** Centrifugar favos de Sintório gera <ItemLink id="puremashtweaks:synthorium_nugget" /> e cera.

### MoldelBee (Abelha Nível 5)
* **Conversão:** Use um <ItemLink id="puremashtweaks:moldelonian_block" /> em uma **SynthBee** no mundo.
* **Bloco de Flor:** Exige um <ItemLink id="puremashtweaks:moldelonian_block" />.
* **Produção:** Centrifugar favos de Moldeloniano gera <ItemLink id="puremashtweaks:moldelonian_nugget" />.

---

## 2. Productive Metalworks

Os metais do mod podem ser derretidos, fundidos e moldados em fundições de alta temperatura:

* **Pontos de Fusão:** 2.200 °C para Sintório / 2.900 °C para Moldeloniano.
* **Moldes de Fundição:** Produza Blocos (810 mB), Lingotes (90 mB), Placas (270 mB), Bastões (135 mB), Pepitas (10 mB) e Baldes (1.000 mB).
* **Fundição Alquímica:** Despeje 80 mB de metal derretido sobre uma Maçã comum para obter a <ItemLink id="puremashtweaks:synthorium_apple" /> ou a <ItemLink id="puremashtweaks:moldelonian_apple" />.

---

## 3. Silent Gear

Os lingotes de Sintório e Moldeloniano atuam como materiais de alto nível no Silent Gear:

* **Material de Sintório:** Possui os traços Resistente IV, Flexível III, Sinergia III e Magnético II.
* **Bastão de Sintório:** Substituto de hastes de ferramentas (+50% durabilidade, +20% velocidade de mineração, +0.2 velocidade de ataque).
* **Material de Moldeloniano:** Material de fim de jogo com **65.0 de Dano de Ataque**, **100 de Armadura** e **50.0 de Dureza**.

---

## 4. Scripts no KubeJS

Criadores de modpacks podem registrar receitas personalizadas para todas as máquinas via `server_scripts`:

* **Mesa de Síntese 9x9:** `event.recipes.puremashtweaks.shaped_synthesis` e `shapeless_synthesis`.
* **Compressor Multifuncional:** `event.recipes.puremashtweaks.compression`, `singularity` e `dust`.
* **Sintetizador Alquímico:** `event.recipes.puremashtweaks.alchemical`.