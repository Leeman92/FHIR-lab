# Anforderungsdokument

## PraxisSlot – Terminverwaltung mit FHIR-Schnittstelle

**Version:** 1.0

**Erstellt:** ChatGPT (einziges AI erstelltes Dokument)

---

## 1. Ausgangssituation

Eine medizinische Praxis benötigt eine kleine Anwendung, mit der Patienten, Behandler, Behandlungsräume und Termine verwaltet werden können.

Die Anwendung soll sicherstellen, dass Termine nur dann angelegt oder verschoben werden können, wenn alle beteiligten Ressourcen verfügbar sind. Doppelbelegungen von Patienten, Behandlern und Räumen müssen verhindert werden.

Zusätzlich sollen ausgewählte Informationen in einem standardisierten FHIR-Format bereitgestellt werden. Dadurch soll demonstriert werden, wie ein internes Domänenmodell von einem externen Austauschstandard getrennt und auf diesen abgebildet werden kann.

Das Projekt bildet ausdrücklich keine vollständige Praxismanagementsoftware ab.

---

## 2. Projektziele

Die Anwendung soll:

1. Patienten verwalten.
2. Behandler verwalten.
3. Behandlungsräume verwalten.
4. Termine anlegen, anzeigen, suchen, verschieben und absagen.
5. Überschneidungen und Doppelbelegungen verhindern.
6. fachliche Fehler eindeutig und nachvollziehbar zurückmelden.
7. ausgewählte Daten als FHIR-Ressourcen bereitstellen.
8. ein klar abgegrenztes und nachvollziehbares Domänenmodell besitzen.
9. automatisiert überprüfbare Geschäftsregeln enthalten.
10. ohne manuelle Vorbereitung lokal ausführbar sein.

---

## 3. Nichtziele

Folgende Funktionen sind nicht Bestandteil des Projekts:

* vollständige elektronische Patientenakte,
* medizinische Diagnosen oder Befunde,
* Behandlungsdokumentation,
* Abrechnung,
* Versicherungsdaten,
* Rezepte oder Medikationsdaten,
* Benutzerkonten und Authentifizierung,
* Rollen- und Berechtigungssystem,
* Kalender-Synchronisation mit externen Anbietern,
* E-Mail-, SMS- oder Push-Benachrichtigungen,
* wiederkehrende Termine,
* Wartelisten,
* Terminserien,
* mehrere Praxisstandorte,
* Zeitzonenverwaltung über mehrere Länder,
* vollständige Unterstützung aller FHIR-Ressourcen,
* vollständige Implementierung sämtlicher FHIR-Interaktionen,
* produktiver Umgang mit echten Patientendaten,
* grafische Benutzeroberfläche.

Alle verwendeten Daten müssen fiktiv sein.

---

## 4. Benutzergruppen

### 4.1 Praxismitarbeiter

Praxismitarbeiter verwalten Patienten, Behandler, Räume und Termine.

Für das Projekt wird keine Anmeldung benötigt. Alle fachlichen Aktionen werden so behandelt, als würden sie von einem berechtigten Praxismitarbeiter ausgeführt.

### 4.2 Externes Informationssystem

Ein externes System kann ausgewählte Patienten-, Behandler-, Raum- und Termindaten über die FHIR-Schnittstelle abrufen.

Die FHIR-Schnittstelle ist im ersten Projektumfang nur lesend.

---

## 5. Fachliche Begriffe

### Patient

Eine Person, für die Termine vereinbart werden können.

### Behandler

Eine medizinische Fachkraft, die einen Termin durchführt.

### Raum

Ein physischer Behandlungsraum, in dem ein Termin stattfindet.

### Termin

Ein reservierter Zeitraum zwischen einem Patienten, einem Behandler und einem Raum.

### Aktiver Termin

Ein Termin, dessen Status eine zeitliche Belegung darstellt.

### Terminkollision

Eine zeitliche Überschneidung, bei der mindestens ein Patient, Behandler oder Raum gleichzeitig einem anderen aktiven Termin zugeordnet ist.

### FHIR-Ressource

Eine standardisierte Repräsentation medizinischer oder administrativer Informationen. FHIR organisiert austauschbare Informationen in typisierten Ressourcen und beschreibt dafür unter anderem REST-orientierte Interaktionen.

---

## 6. Fachliches Datenmodell

### 6.1 Patient

Ein Patient besitzt mindestens:

* eine eindeutige interne ID,
* einen Vornamen,
* einen Nachnamen,
* ein Geburtsdatum,
* einen Aktivstatus,
* einen Erstellungszeitpunkt,
* einen Zeitpunkt der letzten Änderung.

#### Regeln

* Vorname und Nachname dürfen nicht leer sein.
* Das Geburtsdatum darf nicht in der Zukunft liegen.
* Ein deaktivierter Patient darf weiterhin angezeigt werden.
* Für einen deaktivierten Patienten dürfen keine neuen Termine angelegt werden.
* Bestehende Termine eines deaktivierten Patienten bleiben erhalten.

---

### 6.2 Behandler

Ein Behandler besitzt mindestens:

* eine eindeutige interne ID,
* einen Anzeigenamen,
* eine fachliche Bezeichnung oder Spezialisierung,
* einen Aktivstatus,
* einen Erstellungszeitpunkt,
* einen Zeitpunkt der letzten Änderung.

#### Regeln

* Der Anzeigename darf nicht leer sein.
* Ein deaktivierter Behandler darf weiterhin angezeigt werden.
* Für einen deaktivierten Behandler dürfen keine neuen Termine angelegt werden.
* Bereits bestehende Termine bleiben erhalten.

---

### 6.3 Raum

Ein Raum besitzt mindestens:

* eine eindeutige interne ID,
* eine eindeutige Bezeichnung,
* einen Aktivstatus,
* einen Erstellungszeitpunkt,
* einen Zeitpunkt der letzten Änderung.

#### Regeln

* Die Raumbezeichnung darf nicht leer sein.
* Zwei Räume dürfen nicht dieselbe Bezeichnung besitzen.
* Ein deaktivierter Raum darf weiterhin angezeigt werden.
* Für einen deaktivierten Raum dürfen keine neuen Termine angelegt werden.

---

### 6.4 Termin

Ein Termin besitzt mindestens:

* eine eindeutige interne ID,
* einen Patienten,
* einen Behandler,
* einen Raum,
* eine Startzeit,
* eine Endzeit,
* einen Terminstatus,
* einen optionalen Termingrund,
* einen Erstellungszeitpunkt,
* einen Zeitpunkt der letzten Änderung.

#### Terminstatus

Das interne Modell unterstützt:

* `BOOKED`
* `CANCELLED`
* `COMPLETED`
* `NO_SHOW`

Die zugehörige FHIR-Ressource `Appointment` unterscheidet ebenfalls zwischen dem Gesamtstatus eines Termins und dem Teilnahmestatus einzelner Beteiligter. Zu den definierten Gesamtstatus gehören unter anderem `booked`, `cancelled`, `fulfilled` und `noshow`.

#### Regeln

* Ein neuer Termin wird mit dem Status `BOOKED` angelegt.
* Ein neuer Termin muss in der Zukunft beginnen.
* Das Ende muss nach dem Beginn liegen.
* Ein Termin muss mindestens 15 Minuten dauern.
* Ein Termin darf höchstens 180 Minuten dauern.
* Beginn und Ende müssen auf volle fünf Minuten fallen.
* Ein Termin benötigt genau einen Patienten.
* Ein Termin benötigt genau einen Behandler.
* Ein Termin benötigt genau einen Raum.
* Patient, Behandler und Raum müssen aktiv sein.
* Ein abgesagter Termin blockiert keinen Zeitraum.
* Ein abgeschlossener Termin kann nicht mehr verschoben werden.
* Ein als nicht wahrgenommen markierter Termin kann nicht mehr verschoben werden.
* Ein abgesagter Termin kann nicht erneut abgesagt werden.
* Der ursprüngliche Datensatz eines abgesagten Termins bleibt erhalten.

---

## 7. Funktionale Anforderungen

### FR-001 – Patient anlegen

Das System muss das Anlegen eines Patienten ermöglichen.

#### Akzeptanzkriterien

* Bei gültigen Daten wird ein Patient mit eindeutiger ID angelegt.
* Der Patient ist nach dem Anlegen aktiv.
* Der Patient kann anschließend über seine ID abgerufen werden.
* Ungültige Eingaben führen nicht zur Anlage eines Datensatzes.
* Alle festgestellten Validierungsfehler werden verständlich zurückgegeben.

---

### FR-002 – Patient abrufen

Das System muss einen einzelnen Patienten anhand seiner ID bereitstellen.

#### Akzeptanzkriterien

* Existiert der Patient, werden seine vollständigen administrativen Daten zurückgegeben.
* Existiert der Patient nicht, wird ein eindeutiger Nicht-gefunden-Fehler zurückgegeben.
* Der Fehler enthält die angefragte ID.

---

### FR-003 – Patienten suchen

Das System muss die Suche nach Patienten ermöglichen.

Unterstützte Kriterien:

* Vorname,
* Nachname,
* Teil eines Namens,
* Aktivstatus.

#### Akzeptanzkriterien

* Die Suche darf mehrere Patienten zurückgeben.
* Eine Suche ohne Treffer liefert ein leeres Ergebnis und keinen Fehler.
* Die Suche nach Namen erfolgt ohne Unterscheidung von Groß- und Kleinschreibung.
* Mehrere Suchkriterien werden gemeinsam angewendet.

---

### FR-004 – Patient deaktivieren

Das System muss einen Patienten deaktivieren können.

#### Akzeptanzkriterien

* Der Patient bleibt abrufbar.
* Neue Termine für diesen Patienten werden abgelehnt.
* Bestehende Termine bleiben unverändert erhalten.
* Eine erneute Deaktivierung verändert den Zustand nicht weiter.

---

### FR-005 – Behandler anlegen

Das System muss das Anlegen eines Behandlers ermöglichen.

#### Akzeptanzkriterien

* Bei gültigen Daten wird ein aktiver Behandler angelegt.
* Name und fachliche Bezeichnung werden gespeichert.
* Der Behandler kann anschließend über seine ID abgerufen werden.

---

### FR-006 – Behandler anzeigen

Das System muss Folgendes ermöglichen:

* Abruf eines einzelnen Behandlers,
* Auflistung aller Behandler,
* Filterung nach Aktivstatus.

Eine leere Liste ist ein gültiges Ergebnis.

---

### FR-007 – Behandler deaktivieren

Das System muss einen Behandler deaktivieren können.

#### Akzeptanzkriterien

* Neue Termine mit diesem Behandler werden abgelehnt.
* Bestehende Termine bleiben erhalten.
* Der Behandler bleibt abrufbar.

---

### FR-008 – Raum anlegen

Das System muss das Anlegen eines Behandlungsraums ermöglichen.

#### Akzeptanzkriterien

* Die Raumbezeichnung ist innerhalb der Anwendung eindeutig.
* Bei einer bereits verwendeten Bezeichnung wird die Anlage abgelehnt.
* Der Konflikt benennt die betroffene Raumbezeichnung.

---

### FR-009 – Räume anzeigen

Das System muss Folgendes ermöglichen:

* Abruf eines einzelnen Raums,
* Auflistung aller Räume,
* Filterung nach Aktivstatus.

---

### FR-010 – Raum deaktivieren

Das System muss einen Raum deaktivieren können.

#### Akzeptanzkriterien

* Neue Termine in diesem Raum werden abgelehnt.
* Bestehende Termine bleiben erhalten.
* Der Raum bleibt abrufbar.

---

### FR-011 – Termin anlegen

Das System muss das Anlegen eines Termins ermöglichen.

Für die Anlage werden mindestens benötigt:

* Patient,
* Behandler,
* Raum,
* Beginn,
* Ende oder Dauer,
* optionaler Termingrund.

#### Akzeptanzkriterien

* Alle referenzierten Datensätze müssen existieren.
* Patient, Behandler und Raum müssen aktiv sein.
* Die zeitlichen Regeln müssen erfüllt sein.
* Es darf keine Terminkollision vorliegen.
* Der Termin wird mit dem Status `BOOKED` angelegt.
* Der angelegte Termin kann anschließend abgerufen werden.
* Bei einer Kollision wird kein Termin gespeichert.

---

### FR-012 – Terminkollision erkennen

Das System muss vor jeder Anlage und Verschiebung prüfen, ob sich der gewünschte Zeitraum mit einem anderen aktiven Termin überschneidet.

Eine Kollision liegt vor, wenn mindestens eines der folgenden Objekte bereits im angefragten Zeitraum gebucht ist:

* Patient,
* Behandler,
* Raum.

#### Akzeptanzkriterien

* Zwei aktive Termine dürfen nicht denselben Patienten überlappend verwenden.
* Zwei aktive Termine dürfen nicht denselben Behandler überlappend verwenden.
* Zwei aktive Termine dürfen nicht denselben Raum überlappend verwenden.
* Mehrere gleichzeitig auftretende Konfliktgründe dürfen gemeinsam zurückgegeben werden.
* Der Konflikt benennt mindestens den Typ der blockierten Ressource.
* Der Konflikt darf keine unnötigen Patientendaten eines anderen Termins offenlegen.

---

### FR-013 – Zeitliche Überschneidung bestimmen

Zwei Termine überschneiden sich, wenn der Beginn des ersten Termins vor dem Ende des zweiten liegt und das Ende des ersten Termins nach dem Beginn des zweiten liegt.

#### Beispiele

Ein bestehender Termin läuft von 10:00 Uhr bis 10:30 Uhr.

* 09:30–10:00 Uhr: erlaubt
* 09:45–10:15 Uhr: Kollision
* 10:00–10:30 Uhr: Kollision
* 10:15–10:45 Uhr: Kollision
* 10:30–11:00 Uhr: erlaubt

Direkt aneinander anschließende Termine sind zulässig.

---

### FR-014 – Termin abrufen

Das System muss einen einzelnen Termin anhand seiner ID bereitstellen.

Die Darstellung enthält mindestens:

* Termin-ID,
* Patient,
* Behandler,
* Raum,
* Beginn,
* Ende,
* Status,
* Termingrund.

Existiert der Termin nicht, wird ein eindeutiger Nicht-gefunden-Fehler zurückgegeben.

---

### FR-015 – Termine suchen

Das System muss eine Suche nach Terminen ermöglichen.

Unterstützte Kriterien:

* Patient,
* Behandler,
* Raum,
* Terminstatus,
* Zeitraum.

#### Akzeptanzkriterien

* Die Kriterien können kombiniert werden.
* Der Zeitraum kann durch Start und Ende begrenzt werden.
* Ein Ergebnis ohne Treffer ist kein Fehler.
* Die Ergebnisse werden chronologisch nach Beginn sortiert.
* Abgesagte Termine können ein- oder ausgeschlossen werden.

---

### FR-016 – Termin verschieben

Das System muss einen gebuchten Termin auf einen anderen Zeitraum verschieben können.

#### Akzeptanzkriterien

* Nur Termine mit Status `BOOKED` können verschoben werden.
* Die allgemeinen zeitlichen Regeln gelten weiterhin.
* Vor der Änderung wird erneut auf Kollisionen geprüft.
* Der zu verschiebende Termin kollidiert nicht mit sich selbst.
* Bei einem Konflikt bleiben die bisherigen Termindaten unverändert.
* Bei erfolgreicher Verschiebung wird der Änderungszeitpunkt aktualisiert.

---

### FR-017 – Termin absagen

Das System muss einen gebuchten Termin absagen können.

Optional kann ein Absagegrund angegeben werden.

#### Akzeptanzkriterien

* Der Status wird auf `CANCELLED` gesetzt.
* Der Termin bleibt weiterhin abrufbar.
* Der Zeitraum steht anschließend wieder zur Verfügung.
* Patient, Behandler und Raum bleiben unverändert referenziert.
* Eine erneute Absage erzeugt keine weitere Zustandsänderung.

---

### FR-018 – Termin abschließen

Das System muss einen vergangenen gebuchten Termin als abgeschlossen markieren können.

#### Akzeptanzkriterien

* Der Status wird auf `COMPLETED` gesetzt.
* Ein zukünftiger Termin kann nicht abgeschlossen werden.
* Ein abgesagter Termin kann nicht abgeschlossen werden.
* Ein abgeschlossener Termin kann nicht verschoben oder abgesagt werden.

---

### FR-019 – Nichterscheinen dokumentieren

Das System muss einen vergangenen gebuchten Termin als nicht wahrgenommen markieren können.

#### Akzeptanzkriterien

* Der Status wird auf `NO_SHOW` gesetzt.
* Ein zukünftiger Termin kann nicht entsprechend markiert werden.
* Ein abgesagter oder abgeschlossener Termin kann nicht entsprechend markiert werden.

---

### FR-020 – Verfügbarkeit prüfen

Das System muss prüfen können, ob Patient, Behandler und Raum in einem gewünschten Zeitraum verfügbar sind, ohne direkt einen Termin anzulegen.

#### Akzeptanzkriterien

* Das Ergebnis gibt an, ob der Zeitraum vollständig verfügbar ist.
* Bei Nichtverfügbarkeit werden die blockierten Ressourcentypen angegeben.
* Die Prüfung verändert keine Daten.
* Eine positive Verfügbarkeitsprüfung garantiert keine spätere Buchung, wenn zwischen Prüfung und Buchung ein anderer Termin angelegt wird.

---

## 8. FHIR-Anforderungen

### 8.1 Allgemeiner Umfang

Die Anwendung muss eine abgegrenzte, lesende FHIR-Schnittstelle bereitstellen.

Unterstützte Ressourcentypen:

* `Patient`
* `Practitioner`
* `Location`
* `Appointment`

Die FHIR-Schnittstelle bildet die internen Daten auf die jeweiligen FHIR-Ressourcen ab. Das interne Fachmodell muss dabei nicht alle möglichen Eigenschaften der FHIR-Ressourcen unterstützen.

Die FHIR-Ressource `Patient` ist für administrative und demografische Informationen einer Person vorgesehen, die Gesundheitsleistungen erhält.

---

### FR-021 – Patient als FHIR-Ressource abrufen

Ein interner Patient muss anhand seiner ID als FHIR-`Patient` abrufbar sein.

Mindestens abzubilden sind:

* logische ID,
* Aktivstatus,
* Name,
* Geburtsdatum.

#### Akzeptanzkriterien

* Die Ressource enthält den Ressourcentyp `Patient`.
* Die logische ID entspricht eindeutig dem internen Patienten.
* Ein unbekannter Patient führt zu einer FHIR-konformen Fehlerantwort.
* Die Ressource enthält keine nicht benötigten internen Felder.

---

### FR-022 – Behandler als FHIR-Ressource abrufen

Ein interner Behandler muss anhand seiner ID als FHIR-`Practitioner` abrufbar sein.

Mindestens abzubilden sind:

* logische ID,
* Aktivstatus,
* Name,
* fachliche Bezeichnung, soweit sinnvoll abbildbar.

---

### FR-023 – Raum als FHIR-Ressource abrufen

Ein interner Raum muss anhand seiner ID als FHIR-`Location` abrufbar sein.

Mindestens abzubilden sind:

* logische ID,
* Aktivstatus,
* Raumbezeichnung.

---

### FR-024 – Termin als FHIR-Ressource abrufen

Ein interner Termin muss anhand seiner ID als FHIR-`Appointment` abrufbar sein.

Mindestens abzubilden sind:

* logische ID,
* Gesamtstatus,
* Beginn,
* Ende,
* Patient als Teilnehmer,
* Behandler als Teilnehmer,
* Raum als Teilnehmer oder Ort,
* optionaler Termingrund.

FHIR-`Appointment` verwendet Teilnehmerreferenzen unter anderem für Patienten, Behandler und Orte. Für Teilnehmer wird zusätzlich ein eigener Teilnahmestatus geführt.

---

### FR-025 – Terminstatus auf FHIR abbilden

Die internen Terminstatus müssen wie folgt abgebildet werden:

| Interner Status | FHIR-Status |
| --------------- | ----------- |
| `BOOKED`        | `booked`    |
| `CANCELLED`     | `cancelled` |
| `COMPLETED`     | `fulfilled` |
| `NO_SHOW`       | `noshow`    |

Die Zuordnung muss zentral dokumentiert und automatisiert überprüfbar sein.

---

### FR-026 – FHIR-Termine suchen

Die FHIR-Schnittstelle muss eine begrenzte Suche nach Terminen ermöglichen.

Mindestens unterstützte Suchkriterien:

* Patient,
* Beginn ab einem bestimmten Zeitpunkt,
* Beginn bis zu einem bestimmten Zeitpunkt,
* Status.

#### Akzeptanzkriterien

* Mehrere Treffer werden als FHIR-Suchergebnis zurückgegeben.
* Referenzen auf Patient, Behandler und Raum sind eindeutig.
* Eine Suche ohne Treffer liefert ein gültiges leeres Suchergebnis.
* Nicht unterstützte Suchparameter werden entweder eindeutig abgelehnt oder ausdrücklich ignoriert. Das gewählte Verhalten muss dokumentiert sein.

---

### FR-027 – FHIR-Fehlerantworten

Fehler innerhalb der FHIR-Schnittstelle müssen in einer FHIR-geeigneten Fehlerdarstellung ausgegeben werden.

Die Antwort muss mindestens enthalten:

* Schweregrad,
* Fehlercode,
* verständliche Beschreibung,
* soweit möglich das betroffene Feld oder Suchkriterium.

Interne Implementierungsdetails, Datenbankmeldungen oder Stacktraces dürfen nicht ausgegeben werden.

---

### FR-028 – Trennung zwischen internem Modell und FHIR-Modell

Änderungen am FHIR-Austauschformat dürfen nicht automatisch Änderungen am internen Datenmodell erzwingen.

#### Akzeptanzkriterien

* Interne Datensätze besitzen eine eigene fachliche Repräsentation.
* Die FHIR-Darstellung wird aus dem internen Modell erzeugt.
* FHIR-spezifische Statuswerte oder Strukturen werden nicht unkontrolliert im gesamten Fachmodell verwendet.
* Die Abbildungsregeln sind dokumentiert und getestet.

---

## 9. Allgemeine Fehleranforderungen

### FR-029 – Konsistente Fehlerstruktur

Alle fachlichen Fehler außerhalb der FHIR-Schnittstelle müssen eine konsistente Struktur besitzen.

Die Fehlerantwort enthält mindestens:

* eindeutigen Fehlertyp,
* verständlichen Titel,
* fachliche Beschreibung,
* Statuscode,
* Zeitpunkt,
* Request-ID,
* optionale feldbezogene Validierungsfehler.

---

### FR-030 – Validierungsfehler sammeln

Enthält eine Anfrage mehrere voneinander unabhängige ungültige Felder, sollen alle erkennbaren Validierungsfehler gemeinsam zurückgegeben werden.

Beispiel:

* Vorname fehlt,
* Geburtsdatum liegt in der Zukunft,
* Nachname ist leer.

Die anfragende Person soll nicht jeden Fehler einzeln durch wiederholte Anfragen entdecken müssen.

---

### FR-031 – Fachliche Konflikte unterscheiden

Folgende Fehlerarten müssen unterscheidbar sein:

* ungültige Eingabe,
* Datensatz nicht gefunden,
* bereits vorhandener Datensatz,
* deaktivierte Ressource,
* Terminkollision,
* ungültiger Statuswechsel,
* interner Fehler.

---

### FR-032 – Keine internen Details offenlegen

Fehlerantworten dürfen nicht enthalten:

* Stacktraces,
* interne Klassennamen,
* Datenbanktabellen,
* SQL-Anweisungen,
* Zugangsdaten,
* interne Dateipfade.

---

## 10. Nebenläufigkeit und Datenkonsistenz

### NFR-001 – Parallele Terminbuchungen

Die Anwendung muss verhindern, dass zwei zeitgleich verarbeitete Buchungsanfragen erfolgreich dieselbe Ressource für überlappende Zeiträume reservieren.

#### Akzeptanzkriterien

* Werden zwei kollidierende Buchungen nahezu gleichzeitig eingereicht, darf höchstens eine erfolgreich sein.
* Die abgelehnte Anfrage erhält einen fachlichen Konflikt.
* Nach Abschluss beider Anfragen existiert kein inkonsistenter Doppeltermin.

---

### NFR-002 – Atomare Änderungen

Das Anlegen oder Verschieben eines Termins muss vollständig oder gar nicht erfolgen.

Bei einem Fehler dürfen keine teilweise vorgenommenen Änderungen zurückbleiben.

---

### NFR-003 – Idempotentes Verhalten

Wiederholte Zustandsänderungen sollen soweit fachlich sinnvoll ein stabiles Ergebnis liefern.

Beispiele:

* Ein bereits abgesagter Termin bleibt abgesagt.
* Ein bereits deaktivierter Patient bleibt deaktiviert.
* Wiederholtes Abrufen verändert keine Daten.

Das mehrfache Anlegen desselben fachlichen Termins muss nicht automatisch als idempotent behandelt werden.

---

## 11. Nachvollziehbarkeit

### NFR-004 – Request-ID

Jede eingehende Anfrage muss einer Request-ID zugeordnet werden.

Die Request-ID muss:

* in der Antwort enthalten sein,
* in Fehlerantworten enthalten sein,
* für die zugehörige Protokollierung verwendet werden.

Eine vom aufrufenden System übergebene gültige Request-ID darf übernommen werden. Andernfalls wird eine neue erzeugt.

---

### NFR-005 – Zeitstempel

Neu angelegte und geänderte Datensätze müssen nachvollziehbare Zeitstempel besitzen.

Mindestens erforderlich:

* Erstellungszeitpunkt,
* letzter Änderungszeitpunkt.

Die Zeitpunkte müssen eindeutig und unabhängig von der lokalen Darstellung speicherbar sein.

---

### NFR-006 – Protokollierung

Die Anwendung muss mindestens folgende Ereignisse nachvollziehbar protokollieren:

* Anwendungsstart,
* unerwartete Fehler,
* abgelehnte Buchungen aufgrund von Terminkollisionen,
* Statusänderungen eines Termins,
* Dauer und Ergebnis eingehender Anfragen.

Sensible Patienteninformationen dürfen nicht unnötig in Protokollen ausgegeben werden.

---

## 12. Qualitätsanforderungen

### NFR-007 – Automatisierte Überprüfbarkeit

Die zentralen Geschäftsregeln müssen automatisiert überprüfbar sein.

Besonders relevant sind:

* Validierung von Patienten,
* Terminstatuswechsel,
* Berechnung zeitlicher Überschneidungen,
* Konflikte für Patienten,
* Konflikte für Behandler,
* Konflikte für Räume,
* direkt aneinander angrenzende Termine,
* Verhalten abgesagter Termine,
* parallele Buchungsversuche,
* Zuordnung interner Daten zu FHIR-Ressourcen,
* Statusabbildung zwischen internem Modell und FHIR.

---

### NFR-008 – Reproduzierbare Ausführung

Eine andere Person muss das Projekt anhand der Dokumentation lokal ausführen können.

Die Dokumentation enthält mindestens:

* Projektzweck,
* Voraussetzungen,
* Startanleitung,
* Testanleitung,
* Beispielanfragen,
* bekannte Einschränkungen,
* unterstützte FHIR-Version,
* unterstützte FHIR-Ressourcen und Suchparameter.

---

### NFR-009 – Verständliche Schnittstellendokumentation

Alle bereitgestellten Schnittstellen müssen maschinenlesbar und für Menschen nachvollziehbar dokumentiert sein.

Die Dokumentation muss enthalten:

* mögliche Anfragen,
* Pflichtfelder,
* optionale Felder,
* Erfolgsantworten,
* Fehlerantworten,
* fachliche Regeln,
* Beispielwerte.

---

### NFR-010 – Datenschutzgerechte Testdaten

Das Projekt darf ausschließlich synthetische Daten verwenden.

* Keine realen Patienteninformationen.
* Keine aus realen Personen abgeleiteten vollständigen Datensätze.
* Beispieldaten müssen klar als fiktiv erkennbar sein.
* Protokolle und Fehlermeldungen sollen möglichst wenige personenbezogene Inhalte enthalten.

---

### NFR-011 – Wartbarkeit

Die Fachbereiche Patient, Behandler, Raum, Termin und FHIR-Abbildung müssen klar voneinander unterscheidbar sein.

Änderungen an der externen FHIR-Darstellung sollen die Terminlogik nicht unnötig beeinflussen.

---

## 13. Beispielszenarien

### Szenario 1 – Erfolgreiche Buchung

Gegeben sind:

* ein aktiver Patient,
* ein aktiver Behandler,
* ein aktiver Raum,
* keine vorhandene Buchung von 10:00 bis 10:30 Uhr.

Wenn ein Termin für diesen Zeitraum angelegt wird, wird der Termin erfolgreich mit Status `BOOKED` gespeichert.

---

### Szenario 2 – Behandler ist bereits gebucht

Ein Behandler besitzt einen aktiven Termin von 10:00 bis 10:30 Uhr.

Eine zweite Buchung für denselben Behandler von 10:15 bis 10:45 Uhr wird abgelehnt.

Ein anderer Raum verhindert den Konflikt nicht.

---

### Szenario 3 – Raum ist bereits gebucht

Raum A besitzt einen aktiven Termin von 10:00 bis 10:30 Uhr.

Eine zweite Buchung in Raum A von 10:15 bis 10:45 Uhr wird abgelehnt, auch wenn Patient und Behandler unterschiedlich sind.

---

### Szenario 4 – Direkt anschließender Termin

Ein Behandler besitzt einen Termin von 10:00 bis 10:30 Uhr.

Ein weiterer Termin ab 10:30 Uhr ist erlaubt.

---

### Szenario 5 – Abgesagter Termin blockiert nicht

Ein Termin von 10:00 bis 10:30 Uhr wird abgesagt.

Anschließend kann derselbe Zeitraum erneut für Patient, Behandler und Raum gebucht werden.

---

### Szenario 6 – Konflikt beim Verschieben

Ein Termin soll auf einen Zeitraum verschoben werden, in dem der Raum bereits belegt ist.

Die Verschiebung wird abgelehnt. Der ursprüngliche Zeitraum des Termins bleibt unverändert.

---

### Szenario 7 – Gleichzeitige Buchungen

Zwei Anfragen versuchen gleichzeitig, denselben Behandler und Raum von 10:00 bis 10:30 Uhr zu buchen.

Genau eine Anfrage ist erfolgreich. Die andere erhält einen Konflikt.

---

### Szenario 8 – FHIR-Patient abrufen

Ein vorhandener interner Patient wird über die FHIR-Schnittstelle angefragt.

Das Ergebnis ist eine gültige `Patient`-Ressource mit:

* Ressourcentyp,
* ID,
* Aktivstatus,
* Name,
* Geburtsdatum.

---

### Szenario 9 – FHIR-Termin abrufen

Ein vorhandener Termin wird über die FHIR-Schnittstelle angefragt.

Das Ergebnis ist eine `Appointment`-Ressource mit:

* Terminstatus,
* Beginn,
* Ende,
* Referenz auf den Patienten,
* Referenz auf den Behandler,
* Referenz auf den Raum.

---

### Szenario 10 – Unbekannte FHIR-Ressource

Ein nicht vorhandener Patient oder Termin wird über die FHIR-Schnittstelle angefragt.

Das System liefert eine strukturierte FHIR-Fehlerantwort und keinen allgemeinen internen Serverfehler.

---

## 14. Priorisierung

### Muss-Anforderungen

* Patienten anlegen und abrufen
* Behandler anlegen und abrufen
* Räume anlegen und abrufen
* Termine anlegen und abrufen
* Terminkollisionen erkennen
* Termine suchen
* Termine verschieben
* Termine absagen
* konsistente Fehlerantworten
* parallele Buchungen absichern
* FHIR-`Patient` abrufen
* FHIR-`Appointment` abrufen
* automatisierte Tests der Geschäftsregeln
* dokumentierte lokale Ausführung

### Soll-Anforderungen

* Verfügbarkeitsprüfung ohne Buchung
* Patienten-, Behandler- und Raumsuche
* Termine abschließen
* Nichterscheinen dokumentieren
* FHIR-`Practitioner`
* FHIR-`Location`
* FHIR-Terminsuche
* Request-ID und strukturierte Protokollierung

### Kann-Anforderungen

* Absagegrund
* optimistische Änderungsprüfung bei parallelen Updates
* paginierte Suchergebnisse
* sortierbare Suchergebnisse
* Seed-Daten für eine Demo
* maschinenlesbare Beispielsammlung
* zusätzliche FHIR-Suchparameter

---

## 15. Definition of Done

Das Projekt gilt als abgeschlossen, wenn:

1. alle Muss-Anforderungen umgesetzt sind,
2. die Anwendung lokal anhand der Dokumentation gestartet werden kann,
3. die zentralen Geschäftsregeln automatisiert getestet sind,
4. parallele kollidierende Buchungen nicht zu einer Doppelbelegung führen,
5. Patienten und Termine als FHIR-Ressourcen abrufbar sind,
6. die unterstützte FHIR-Version dokumentiert ist,
7. die Abbildung interner Statuswerte auf FHIR dokumentiert und getestet ist,
8. keine realen Patientendaten enthalten sind,
9. die Schnittstellen und Fehlerfälle nachvollziehbar dokumentiert sind,
10. bekannte Einschränkungen offen aufgeführt sind.

---

## 16. Offene fachliche Entscheidungen

Folgende Punkte müssen vor oder während der Umsetzung bewusst entschieden und dokumentiert werden:

1. Wird die Endzeit direkt übergeben oder aus einer Dauer berechnet?
2. Soll eine Anfrage beide Varianten unterstützen?
3. Welche maximale Länge darf ein Termingrund besitzen?
4. Werden deaktivierte Ressourcen standardmäßig in Listen angezeigt?
5. Wie werden Ergebnisse mit sehr vielen Treffern begrenzt?
6. Wie wird mit unbekannten FHIR-Suchparametern umgegangen?
7. Soll die FHIR-Schnittstelle ausschließlich R5 unterstützen oder bewusst auf eine andere Version ausgerichtet werden?
8. Welche FHIR-Felder werden bewusst nicht unterstützt?
9. Soll eine FHIR-Suche Referenzen direkt einbetten oder ausschließlich referenzieren?
10. Soll die FHIR-Schnittstelle später auch schreibende Interaktionen unterstützen?

Diese Entscheidungen sind Teil der fachlichen und architektonischen Auseinandersetzung und werden durch dieses Anforderungsdokument nicht vorweggenommen.

