# 📚 Instructions de développement - PayToGether

Ce dossier contient les **instructions de développement** pour le projet PayToGether. Chaque instruction est un guide pratique et concis pour implémenter une fonctionnalité ou un pattern spécifique.

---

## 📋 Liste des instructions

### 🖼️ Gestion des images avec MinIO

#### 📖 Guide complet (point d'entrée recommandé)
**Fichier** : `GESTION_IMAGES_MINIO_GUIDE_COMPLET.md`

**Description** : Document récapitulatif qui guide vers la bonne documentation selon le contexte.

**Contenu** :
- Vue d'ensemble de toute la documentation disponible
- Parcours recommandés selon les situations
- Tableau comparatif des documents
- Guide de recherche d'informations spécifiques
- Liens rapides vers tous les documents

**Quand l'utiliser** :
- ✅ **Première fois** : Commencer par ce document
- ✅ Ne sait pas quel document consulter
- ✅ Vue d'ensemble de la documentation

---

#### 📘 Instruction complète
**Fichier** : `GESTION_IMAGES_MINIO_INSTRUCTION.md`

**Description** : Documentation exhaustive pour implémenter la gestion des images avec MinIO.

**Contenu** :
- Vue d'ensemble et architecture complète
- Configuration MinIO (application.yml, dépendances)
- Implémentation backend détaillée (FileManager, ProviderAdapter, Service, Resource)
- Implémentation frontend détaillée (Service, Hooks, Composants)
- Statuts des images et transitions
- Checklist d'implémentation complète
- Points d'attention et bonnes pratiques

**Quand l'utiliser** :
- ✅ Première implémentation d'une entité avec images
- ✅ Comprendre en profondeur le système
- ✅ Référence exhaustive
- ✅ Formation de nouveaux développeurs

---

#### ⚡ Quick Reference
**Fichier** : `GESTION_IMAGES_MINIO_QUICK_REF.md`

**Description** : Guide de référence rapide avec code prêt à copier-coller.

**Contenu** :
- Format du nom de fichier
- Flux en 7 étapes
- Code backend essentiel (sauvegarder, endpoints)
- Code frontend essentiel (service, hooks, composants)
- Checklist rapide
- Tableau des pièges à éviter
- 10 points clés à retenir

**Quand l'utiliser** :
- ✅ Rappel rapide pendant le développement
- ✅ Copier-coller de code
- ✅ Aide-mémoire
- ✅ Vérification rapide de conformité

---

#### 🔄 Diagrammes
**Fichier** : `GESTION_IMAGES_MINIO_DIAGRAMMES.md`

**Description** : Diagrammes de séquence pour visualiser les flux.

**Contenu** :
- Diagramme : Flux d'upload complet (21 étapes)
- Diagramme : Flux de lecture (11 étapes)
- Diagramme : Flux de mise à jour (11 étapes)
- Diagrammes de transition des statuts
- Explications des étapes critiques
- Schéma de sécurité (URL présignées)

**Quand l'utiliser** :
- ✅ Visualiser le flux complet
- ✅ Comprendre les interactions entre composants
- ✅ Support de présentation
- ✅ Débugger un problème de flux

---

## 📖 Comment utiliser ces instructions

### 1. Pour un développeur
- Lire l'instruction correspondante avant d'implémenter
- Suivre la checklist d'implémentation
- Référencer l'instruction en cas de doute

### 2. Pour Copilot
Ces instructions sont également utilisées par Copilot pour :
- Générer du code conforme aux standards du projet
- Maintenir la cohérence architecturale
- Proposer des implémentations complètes

### 3. Structure d'une instruction
Chaque instruction contient généralement :
- **Vue d'ensemble** : Principe et objectif
- **Architecture** : Diagrammes et flux
- **Implémentation** : Code backend et frontend
- **Checklist** : Points à vérifier
- **Points d'attention** : Pièges à éviter
- **Références** : Documentation complémentaire

---

## 🔗 Liens connexes

### Documentation technique
- `.github/documentation/` : Documentation détaillée du projet
- `.github/copilot-instructions.md` : Instructions globales pour Copilot

### Code source
- `modules/bff/` : Backend (architecture hexagonale)
- `modules/front/` : Frontend (React/TypeScript)

---

## ✨ Contribuer

Pour ajouter une nouvelle instruction :

1. Créer un fichier `{SUJET}_INSTRUCTION.md` dans ce dossier
2. Suivre la structure des instructions existantes
3. Ajouter une référence dans ce README
4. Mettre à jour `.github/copilot-instructions.md` si nécessaire

---

**Date de dernière mise à jour** : 15 février 2026  
**Maintenu par** : Équipe PayToGether


