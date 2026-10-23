# CRV6 — Cube Reborn (Paper 26.3)

Plugin **Paper** + **resource pack** pour le serveur Cube Reborn.

## Pourquoi ce dépôt

Tout le travail (plugin, pack, variantes joueurs, meubles, RF…) est centralisé ici pour ne plus se perdre entre les dizaines de zips.

## Structure

```
crv6-cube-reborn/
├── plugin/          # Code source Maven (CRV6Menu)
├── builds/          # Binaires à jour (jar + pack)
│   ├── CRV6Menu.jar
│   └── cube_reborn_v7_26.3_doll.zip
├── docs/            # Docs optionnelles
├── README.md
└── .gitignore
```

## Installation serveur

1. Copier `builds/CRV6Menu.jar` → dossier `plugins/`
2. Distribuer `builds/cube_reborn_v7_26.3_doll.zip` (pack client / serveur)
3. Redémarrer le serveur
4. Clients : **F3 + T** pour recharger les ressources

### Commandes

| Commande | Description |
|----------|-------------|
| `/crv6` | Menu admin des items custom |
| `/crreload` | Recharge le plugin |
| `/crfurniture` | Give socle, table, chaise, boîte aux lettres |
| `/crclear` | Supprime les display entities dans un rayon de 3 blocs |

**Permissions** : `crv6.menu` · `crv6.give` · `crv6.reload`

## Build plugin

```bash
cd plugin
mvn -q package -DskipTests
# → plugin/target/CRV6Menu.jar
```

Java 21+ recommandé.

## Modifier le resource pack

```bash
cd builds
mkdir -p ../resourcepack && cd ../resourcepack
unzip -qo ../builds/cube_reborn_v7_26.3_doll.zip
# … éditer assets/ …
zip -qr -9 ../builds/cube_reborn_v7_26.3_doll.zip .
```

## Variantes poupées (joueurs)

Fichiers JSON dans `plugin/src/main/resources/` :

- `benji_variants.json` → Benjixh02  
- `bulbily_variants.json` → Bulbily  
- `cryzopha_variants.json` → Cryzopha  
- `kaulose_variants.json` → Kaulose  
- `cecile_variants.json` → cecile71000  
- `kama_variants.json` → Kama  
- `lily_variants.json` → LillyChouquette  

Chargement dans le plugin :

```java
loadPlayerVariants("kama_variants.json", "Kama");
```

## Soft-depend

- Simple Voice Chat  
- TAB / NametagEdit  

## Créer le dépôt GitHub

```bash
# 1. Créer un repo vide sur github.com (ex: CubeReborn/crv6-cube-reborn)

# 2. Dans ce dossier :
git init -b main
git add .
git commit -m "Initial: plugin CRV6Menu + pack 26.3"
git remote add origin https://github.com/TON_ORG/crv6-cube-reborn.git
git push -u origin main
```

Tags utiles :

```bash
git tag v26.3-1
git push origin v26.3-1
```

## Règles pour ne pas se perdre

1. **Une seule source de vérité** : branche `main` de ce repo  
2. Ne pas garder 10 versions de pack dans le repo — seulement `builds/` à jour  
3. Chaque grosse feature = un commit clair (ex. `feat: variantes Kama`)  
4. Après build local, remplacer `builds/CRV6Menu.jar` et commit  

## Auteurs

Cube Reborn / CRV6
