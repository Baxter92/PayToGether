# ⚡ Action requise : Configuration Square Payment SDK

## ⚠️ Version du SDK à vérifier

La version `46.0.0.20260122` du Square Java SDK est configurée dans le projet, mais elle doit être **vérifiée** avant utilisation.

## 🔍 Vérification rapide

Exécutez cette commande pour vérifier si la version existe :

```bash
curl -I "https://repo.maven.apache.org/maven2/com/squareup/square/46.0.0.20260122/square-46.0.0.20260122.pom"
```

### ✅ Si vous obtenez "200 OK"
La version existe ! Continuez avec :
```bash
./mvnw clean compile -U
```

### ❌ Si vous obtenez "404 Not Found"
La version n'existe pas encore. Suivez les étapes ci-dessous.

## 🛠️ Solution : Utiliser une version stable

### Étape 1 : Choisir une version

Visitez https://mvnrepository.com/artifact/com.squareup/square et choisissez la dernière version stable.

**Versions connues qui fonctionnent** :
- `30.0.0.20230719` (Juillet 2023) ✅ Recommandé
- `29.0.0.20230628` (Juin 2023) ✅
- `28.0.0.20230517` (Mai 2023) ✅

### Étape 2 : Mettre à jour le pom.xml

Éditez `modules/bff/bff-wsclient/pom.xml` :

```xml
<dependency>
    <groupId>com.squareup</groupId>
    <artifactId>square</artifactId>
    <version>30.0.0.20230719</version> <!-- Remplacer par la version choisie -->
</dependency>
```

### Étape 3 : Nettoyer et recompiler

```bash
# Nettoyer le cache Maven
rm -rf ~/.m2/repository/com/squareup/square/

# Recompiler
./mvnw clean compile -U
```

## 📚 Documentation complète

Pour plus de détails, consultez :

| Document | Contenu |
|----------|---------|
| **[SQUARE_PAYMENT_FINAL_SUMMARY.md](SQUARE_PAYMENT_FINAL_SUMMARY.md)** | 📊 Vue d'ensemble complète |
| **[modules/bff/bff-wsclient/SQUARE_SDK_VERSION_GUIDE.md](modules/bff/bff-wsclient/SQUARE_SDK_VERSION_GUIDE.md)** | 🔧 Guide des versions SDK |
| **[README_SQUARE_PAYMENT.md](README_SQUARE_PAYMENT.md)** | 🚀 Guide de démarrage |
| **[.github/documentation/SQUARE_PAYMENT_INTEGRATION.md](.github/documentation/SQUARE_PAYMENT_INTEGRATION.md)** | 📖 Documentation technique |

## 🎯 Après la compilation réussie

1. **Configurer les clés Square** (voir `README_SQUARE_PAYMENT.md`)
2. **Exécuter la migration de la base de données**
3. **Tester avec les cartes de test Square**
4. **Déployer** 🎉

## 💡 Besoin d'aide ?

- 📖 Lisez le guide complet : `SQUARE_SDK_VERSION_GUIDE.md`
- 🐛 Problème de compilation ? Voir `CORRECTION_SQUARE_PAYMENT_SERVICE.md`
- 🧪 Tester le frontend : `modules/front/SQUARE_PAYMENT_CHECKOUT_IMPLEMENTATION.md`

---

**Status actuel** : ⚠️ Configuration requise  
**Action** : Vérifier et mettre à jour la version Square SDK  
**Temps estimé** : 5 minutes

