# Vehicle roster

**Built.** 55 tanks across 11 nations (48 added in this update), plus support vehicles (a UAV team, a Recon vehicle and a
Resupply truck) for each new nation, so every nation can field a full deck and the 12-tank minimum is easy to meet under the copy
limits (3 Common/Uncommon, 2 Rare/Epic, 1 Legendary). Names are real vehicles; **all numbers are placeholders to tune**.
Anti-air and air units are left for later. All vehicle lists are registered in `Data/CardLibrary/Vehicles/VehicleLibrary.java`.

## Stat bands (so rarity means something and Artillery stays within its design goal)
| Rarity | Light tank (armor / HP) | Medium tank | Main battle tank |
|---|---|---|---|
| Common | 25-30 / 120-130 | 45 / 175-180 | 60 / 200 |
| Uncommon | 35-40 / 150-160 | 50-55 / 195-200 | 65 / 215 |
| Rare | - | - | 80-82 / 240-245 |
| Epic | - | - | 88-95 / 260-280 |
| Legendary | - | - | 100-105 / 290-300 |

Only Common and Uncommon light tanks (120-160 HP) can be one-shot, and only by Legendary Artillery (144); Epic Artillery (112) can't
one-shot any tank. Attacks follow the existing pattern: coax MG (1 Ammo, suppresses), main gun (2 Ammo), and for Rare and up a
heavy shot (3 Ammo + 1 Fuel, pierce).

## The tanks (new ones in **bold**)
| Nation | Common | Uncommon | Rare | Epic | Legendary |
|---|---|---|---|---|---|
| United States | **M8 Buford AGS** (light, 105mm) | M1126 Stryker; **M10 Booker** (light, 105mm) | M60 Patton | M3 Bradley; M1A1 Abrams | M1A3 Abrams |
| Russia | **T-72A** (MBT, 125mm) | **2S25 Sprut-SD** (light, 125mm) | **T-72B3** (MBT, 125mm) | **T-80BVM**, **T-90M** (MBT, 125mm) | T-14 Armata |
| Germany | **Leopard 1A1** (medium, 105mm) | **Leopard 1A5** (medium, 105mm) | **Leopard 2A4** (MBT, 120mm) | **Leopard 2A6** (MBT, 120mm) | Leopard 2A7V |
| Japan | **Type 16 MCV** (light, 105mm) | **Type 74** (medium, 105mm) | - | **Type 90** (MBT, 120mm) | **Type 10** (MBT, 120mm) |
| China | **Type 59-II** (medium, 105mm) | **ZTQ-15** (light, 105mm) | **Type 96B** (MBT, 125mm) | **ZTZ-99** (MBT, 125mm) | **ZTZ-99A** (MBT, 125mm) |
| United Kingdom | - | **Chieftain Mk11** (MBT, 120mm rifled) | **Challenger 1** (MBT, 120mm rifled) | **Challenger 2** (MBT, 120mm rifled) | **Challenger 3** (MBT, 120mm smoothbore) |
| France | **AMX-10 RC** (light, 105mm) | **AMX-30B2** (medium, 105mm) | - | **Leclerc** (MBT, 120mm) | **Leclerc XLR** (MBT, 120mm) |
| Israel | **Sho't Kal** (medium, 105mm) | **Magach 6B** (medium, 105mm) | **Merkava Mk2** (MBT, 105mm) | **Merkava Mk3** (MBT, 120mm) | **Merkava Mk4 Barak** (MBT, 120mm) |
| Sweden | **Strv 102** (medium, 105mm) | **CV90120** (light, 120mm) | **Strv 103C** (MBT, 105mm) | **Strv 121** (MBT, 120mm) | **Strv 122** (MBT, 120mm) |
| Italy | **OF-40** (medium, 105mm) | **Centauro B1** (light, 105mm) | **Centauro II** (light, 120mm) | **C1 Ariete** (MBT, 120mm) | **Ariete AMV** (MBT, 120mm) |
| South Korea | **M48A5K** (medium, 105mm) | **K1** (MBT, 105mm) | **K1A1** (MBT, 120mm) | **K1A2** (MBT, 120mm) | **K2 Black Panther** (MBT, 120mm) |

## Support vehicles of the new nations
| Nation | UAV team (reveals 1/2/3 by rarity) | Recon vehicle | Resupply truck |
|---|---|---|---|
| Japan | FFRS Team (U) | Type 87 Recon Vehicle (U) | Type 73 Heavy Truck (R) |
| China | CH-4 Rainbow Flight (E) | ZBL-08 Recon Vehicle (R) | Shaanxi SX2190 Truck (R) |
| United Kingdom | Watchkeeper WK450 Flight (R) | FV107 Scimitar (C) | MAN SV Support Vehicle (R) |
| France | Patroller Flight (R) | VBL Scout Car (C) | Arquus Armis Truck (R) |
| Israel | Hermes 900 Flight (L) | Sand Cat Scout (U) | Namer Armored Logistics Carrier (L) |
| Sweden | UAV 03 Ornen Team (U) | Patgb 360 Recon (R) | Scania SBAT 111 Truck (R) |
| Italy | Falco Flight (R) | Lince Recon Vehicle (C) | Iveco Trakker Truck (R) |
| South Korea | RQ-101 Songgolmae Team (C) | K151 Recon Vehicle (C) | KM500 Cargo Truck (R) |

## Weapons and ammunition
- Almost everything uses guns that already have ammo cards: 105mm rifled, 120mm smoothbore, 125mm smoothbore.
- **New:** a 120mm rifled gun for the British tanks (Chieftain, Challenger 1 and 2) with **120mm HESH** as its special round,
  so 4 new ammo cards (1x / 5x / 10x / 20x HESH crates). They can also fire the existing 120mm APFSDS.
- **New:** a generic 7.62mm NATO coax MG (it uses the existing 7.62x51mm ammo cards) for US, Japanese, British and French tanks;
  Russian and Chinese tanks use the existing PKT.
