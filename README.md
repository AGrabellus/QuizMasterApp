# QuizMasterApp

QuizMasterApp ist eine leichtgewichtige Desktop-Anwendung zur Durchführung und Verwaltung von interaktiven Quizzes.

## Anwendung starten

### Option 1: Als ausführbare JAR-Datei (Empfohlen für Releases)
Für ein schnelles Starten ohne Entwicklungsumgebung kannst du die vorkompilierte `.jar`-Datei aus den [Releases](https://github.com/AGrabellus/QuizMasterApp/releases) herunterladen.

1. Lade die aktuelle `QuizMasterApp.jar` herunter.
2. Stelle sicher, dass eine aktuelle **Java Runtime Environment (JRE/JDK 17 oder neuer)** auf deinem System installiert ist.
3. Starte die Anwendung per Doppelklick auf die `.jar`-Datei oder über das Terminal/die Eingabeaufforderung:
   ```bash
   java -jar QuizMasterApp.jar
   ```

### Option 2: Aus dem Quellcode bauen
Falls du das Projekt selbst kompilieren möchtest:
1. Repository klonen:
   ```bash
   git clone https://github.com/AGrabellus/QuizMasterApp.git
   ```
2. Im Projektverzeichnis mit Maven oder Gradle (je nach Konfiguration) bauen und ausführen.

---

## 💡 Tipp: Eigene Quizzes hinzufügen

Um deine eigenen Fragen und Quizzes dauerhaft in der Anwendung zu nutzen, erstelle einfach folgendes Verzeichnis auf deinem Computer und lege dort deine Quiz-Dateien ab:

```plaintext
C:/QuizMasterApp
```

Die Anwendung sucht standardmäßig an diesem Ort nach benutzerdefinierten Quiz-Inhalten und lädt diese automatisch beim Start.