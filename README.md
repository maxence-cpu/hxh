# Hunter x Hunter : Nen — mod NeoForge (Minecraft 26.3)

Mod de fan non officiel : progression RPG autour du Nen, Ren / Zetsu / En, Hatsu à débloquer, Fourmis-Chimères.

## Récupérer le .jar

Chaque push sur `main` lance une compilation GitHub Actions (onglet **Actions**).
Quand elle est verte, le `.jar` est dans l'onglet **Releases** (« build N »).

Installation : launcher NeoForge pour Minecraft **26.3** (Java 25), puis copier le `.jar` dans le dossier `mods`.

## Contenu actuel

- **Éveil** : Feuille de l'Arbre à Eau (5 % sur les Fourmis-Chimères, ou `/give @s hxh:water_leaf`), catégorie tirée au sort.
- **Progression** : 25 XP de Nen par fourmi, 3 points par niveau, menu **H** pour Force / Agilité / Endurance / Maîtrise et les Hatsu.
- **Modes** : **R** Ren (puissance, draine l'Aura), **Z** Zetsu (invisible aux mobs Nen au-delà de 10 blocs, dégâts x2), **G** En (révèle les entités).
- **Hatsu** : Poing de la Destruction (Renforcement, niv. 3, 5 points) — maintenir clic droit main vide, relâcher.
- **Mob** : Fourmi-Chimère en forêt et jungle, chasse en essaim (`/summon hxh:chimera_ant`).
- **HUD** : barre d'Aura au-dessus de la faim.

## Structure

| Dossier | Rôle |
|---|---|
| `nen/` | Données du joueur (attachment NeoForge), règles, événements serveur |
| `network/` | Payloads client ↔ serveur |
| `ability/` | `AbstractNenAbility` + Hatsu |
| `entity/`, `item/`, `registry/` | Contenu et registres |
| `client/` | Touches, HUD, menu, rendu |

## Compiler en local (optionnel)

Java 25 + Gradle récent, puis `gradle build` (ou `gradle runClient` pour tester). Les versions sont dans `gradle.properties`.
