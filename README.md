# Projet RASSCAS

## Comment l'ouvrir dans Android Studio

1. Dézippe le fichier `Projet_RASSCAS.zip`.
2. Dans Android Studio : **File > Open**, puis sélectionne le dossier `ProjetRASSCAS`
   (celui qui contient `settings.gradle.kts`).
3. Android Studio va synchroniser le projet automatiquement (barre de progression en bas).
   - S'il affiche une pop-up du type *"Gradle wrapper not found — Create Gradle wrapper?"*,
     clique sur **OK / Create** : Android Studio le régénère tout seul, c'est normal (je
     n'ai pas pu inclure le fichier binaire du wrapper depuis mon environnement).
   - Si Android Studio te propose de mettre à jour le plugin Android Gradle (AGP) ou
     Gradle, tu peux accepter — le projet a été configuré avec des versions récentes
     mais Android Studio peut suggérer plus récent encore.
4. Une fois la synchro terminée, lance l'appli (▶) sur un émulateur ou un appareil branché.

C'est un projet complet et autonome — **aucune recopie manuelle n'est nécessaire**, il n'y a
qu'à l'ouvrir.

## Ce que contient le projet

```
ProjetRASSCAS/
├── settings.gradle.kts
├── build.gradle.kts                  → plugins Android/Kotlin
├── gradle.properties
├── gradle/wrapper/gradle-wrapper.properties
└── app/
    ├── build.gradle.kts               → dépendances (Room, Gson, coroutines, AppCompat)
    └── src/main/
        ├── AndroidManifest.xml
        ├── assets/
        │   ├── questions.json          → les 15 questions, MODIFIABLE librement
        │   └── personnages.json         → les 48 personnages générés depuis ton Excel
        ├── java/com/etresdufutur/app/
        │   ├── data/    (Models, QuestionRepository, PersonnageRepository, CalculateurProfil)
        │   ├── db/      (Room : ResultatEntity, ResultatDao, AppDatabase)
        │   └── ui/      (AccueilActivity, QuestionnaireActivity, ResultatActivity)
        └── res/
            ├── layout/  (les 3 écrans)
            ├── drawable/boule_rouge.xml
            └── values/strings.xml
```

## Points à vérifier / compléter avant de tester en conditions réelles

- **Images des 48 personnages** : place tes 48 fichiers dans
  `app/src/main/res/drawable/`, nommés comme le champ `imageFile` de
  `personnages.json` (sans l'extension — minuscules et underscores uniquement, contrainte
  Android). J'ai généré ces noms automatiquement à partir des noms de personnages de ton
  Excel ; **vérifie qu'ils correspondent à ta vraie convention de nommage** et ajuste
  `personnages.json` si besoin.
- **Raspberry Pi en clavier HID** (USB Gadget et/ou BLE HID), avec les boutons mappés sur
  `KEYCODE_DPAD_UP/DOWN/LEFT/RIGHT`, `KEYCODE_ENTER` (validation), `KEYCODE_ESCAPE`
  (retour) — partie non couverte ici (hors Kotlin/Android).
- L'icône de l'application n'a pas été personnalisée (icône par défaut d'Android Studio) —
  facile à changer via **File > New > Image Asset** une fois le projet ouvert.
- Nom de package `com.etresdufutur.app` — renomme-le si tu veux autre chose (clic droit
  sur le package dans Android Studio → Refactor → Rename).

## Rappel du fonctionnement

- Questionnaire piloté par `questions.json` : ajouter/retirer/modifier une question ne
  nécessite aucune modification de code.
- Navigation par `KeyEvent` standard (flèches, Entrée, Échap) — identique en USB ou
  Bluetooth du moment que le Pi émule un clavier HID.
- Égalité entre deux types dans un thème → le premier type sélectionné par l'utilisateur
  l'emporte.
- Retour en arrière : le curseur se replace sur la réponse précédemment choisie.
- Résultats enregistrés en base Room locale (âge, sexe, métier, personnage, décompte
  détaillé par type) au clic sur "Terminer".
