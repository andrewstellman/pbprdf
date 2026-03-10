# pbprdf V1 Domain Model and Ontology Specification

## Purpose

This document specifies the **existing V1 ontology and RDF generation behavior** in the legacy Scala `pbprdf` project, as a baseline for a Python V2 rewrite.

It captures:
- ontology classes and properties,
- entity identity rules (IRI construction),
- event-to-triple mapping rules (business semantics),
- time and ordering logic.

## Scope and Exclusions

### In Scope

- Ontology vocabulary and schema metadata generation.
- Instance graph generation for games, rosters, events, and plays.
- Business rules for mapping recognized basketball events into RDF triples.

### Out of Scope (intentionally ignored)

- HTML parsing and file ingestion.
- `EspnPlayByPlay` scraping/parsing concerns.
- Regex parser internals beyond what they imply about event type recognition.

---

## Namespaces and Identity

### Base Namespaces

- Ontology namespace: `http://stellman-greene.com/pbprdf#`
- Entity namespace: `http://stellman-greene.com/pbprdf/`

### Entity IRI Construction Rules

- **Game**: `.../games/{YYYY-MM-dd}_{away}_at_{home}`
  - Spaces replaced with underscores in team names.
- **Event**: `{gameIri}/{eventNumber}`
- **Team**: `.../teams/{team_name_with_underscores}`
- **Player**: `.../players/{player_name_with_underscores}`

---

## Ontology Classes (V1)

Core classes defined in `Ontology.scala`:

- `Game`
- `Event`
- `Play` (subclass of `Event`)
- `Team`
- `Player`
- `Roster`
- `Shot`
- `Block` (subclass of `Shot`)
- `Foul`
- `TechnicalFoul`
- `Turnover`
- `Rebound`
- `JumpBall`
- `Timeout`
- `Enters` (substitution event)
- `Ejection`
- `EndOfPeriod`
- `EndOfGame`
- `FiveSecondViolation`

### Class Modeling Notes

- `Block` is explicitly modeled as a subclass of `Shot`.
- Some basketball concepts are modeled as **properties**, not classes:
  - Assist: `shotAssistedBy`
  - Steal: `stolenBy`

---

## Ontology Properties (V1 Vocabulary)

### Game and Roster Properties

- `gameTime` (`Game -> xsd:dateTime`)
- `gameLocation` (`Game -> xsd:string`)
- `homeTeam` (`Game -> Team`, object property)
- `awayTeam` (`Game -> Team`, object property)
- `hasHomeTeamRoster` (`Game -> Roster`, object property)
- `hasAwayTeamRoster` (`Game -> Roster`, object property)
- `rosterTeam` (`Roster -> Team`, object property)
- `hasPlayer` (`Roster -> Player`, object property)

### Event Core Properties

- `inGame` (`Event -> Game`)
- `forTeam` (`Event -> Team`, object property)
- `time` (`Event -> xsd:string`)
- `period` (`Event -> xsd:int`)
- `secondsIntoGame` (`Event -> xsd:int`)
- `secondsLeftInPeriod` (`Event -> xsd:int`)
- `eventNumber` (`Event -> xsd:int`)
- `previousEvent` (`Event -> Event`, object property)
- `nextEvent` (`Event -> Event`, object property)
- `secondsSincePreviousEvent` (`Event -> xsd:int`)
- `secondsUntilNextEvent` (`Event -> xsd:int`)
- `homeScore` (`Event -> xsd:int`)
- `awayScore` (`Event -> xsd:int`)

### Shot and Block Properties

- `shotMade` (`Shot -> xsd:boolean`)
- `shotBy` (`Shot -> Player`, object property)
- `shotType` (`Shot -> xsd:string`)
- `shotAssistedBy` (`Shot -> Player`, object property)
- `shotPoints` (`Shot -> xsd:int`)
- `shotBlockedBy` (`Block -> Player`, object property)

### Foul and Technical Properties

- `foulCommittedBy` (`Foul/TechnicalFoul -> Player`, object property)
- `foulDrawnBy` (`Foul -> Player`, object property)
- `isShootingFoul` (`Foul -> xsd:boolean`)
- `isLooseBallFoul` (`Foul -> xsd:boolean`)
- `isCharge` (`Foul -> xsd:boolean`)
- `isPersonalBlockingFoul` (`Foul -> xsd:boolean`)
- `isOffensive` (`Foul or Rebound -> xsd:boolean`)
- `isThreeSecond` (`TechnicalFoul -> xsd:boolean`)
- `isDelayOfGame` (`TechnicalFoul -> xsd:boolean`)
- `technicalFoulNumber` (`TechnicalFoul -> xsd:int`)

### Other Play Properties

- `reboundedBy` (`Rebound -> Player`, object property)
- `turnoverType` (`Turnover -> xsd:string`)
- `stolenBy` (`Turnover -> Player`, object property)
- `turnedOverBy` (`Turnover -> Player`, object property)
- `timeoutDuration` (`Timeout -> xsd:string`)
- `isOfficial` (`Timeout -> xsd:boolean`)
- `jumpBallHomePlayer` (`JumpBall -> Player`)
- `jumpBallAwayPlayer` (`JumpBall -> Player`)
- `jumpBallGainedPossession` (`JumpBall -> Player`)
- `playerEntering` (`Enters -> Player`)
- `playerExiting` (`Enters -> Player`)
- `playerEjected` (`Ejection -> Player`)

---

## Schema (Ontology/TBox) Generation Behavior

Ontology metadata is generated from Scala annotations:

- Class IRIs are emitted as `owl:Class`.
- Property IRIs are emitted as:
  - `owl:ObjectProperty` when annotated as object property,
  - otherwise `owl:DatatypeProperty`.
- `rdfs:label` is emitted for classes/properties.
- `rdfs:domain` and `rdfs:range` are emitted from annotations.
- `rdfs:subClassOf` is emitted for subclasses.
- `rdfs:comment` is emitted where present.

This process is reflection-driven from `Ontology.scala`.

---

## Instance Graph (ABox) Generation

## 1) Game-Level Triples

For each game:

- `(game, rdf:type, pbprdf:Game)`
- `(game, rdfs:label, "...")`
- `(game, pbprdf:gameTime, xsd:dateTime)`
- optional `(game, pbprdf:gameLocation, "...")`

Team links:

- `(game, pbprdf:homeTeam, homeTeamIri)`
- `(game, pbprdf:awayTeam, awayTeamIri)`
- each team typed as `pbprdf:Team`.

## 2) Roster Modeling

Roster nodes are blank nodes, one home and one away:

- `(game, hasHomeTeamRoster, _:homeRoster)`
- `(game, hasAwayTeamRoster, _:awayRoster)`
- each roster typed `pbprdf:Roster`
- `(roster, rosterTeam, teamIri)`
- `(roster, rdfs:label, teamName)`

Players are attached to rosters via `hasPlayer`, with:

- `(playerIri, rdf:type, pbprdf:Player)`
- `(playerIri, rdfs:label, playerName)`

**Critical business rule**: roster membership is inferred **only** from `Enters` events (`playerEntering -> team`).  
If a player never appears in an `Enters` event, V1 may omit them from rosters, even if other plays reference them.

## 3) Universal Event Triples

Every event (matched play or fallback generic event) emits:

- `(event, rdf:type, pbprdf:Event)`
- `(event, pbprdf:inGame, gameIri)`
- `(event, pbprdf:period, int)`
- `(event, pbprdf:time, string clock)`
- `(event, rdfs:label, description)`

If clock parsing succeeds:

- `(event, pbprdf:secondsIntoGame, int)`
- `(event, pbprdf:secondsLeftInPeriod, int)`

If score matches `away-home` format:

- `(event, pbprdf:awayScore, int)`
- `(event, pbprdf:homeScore, int)`

## 4) Universal Play Triples

All recognized plays also emit:

- `(event, rdf:type, pbprdf:Play)`
- `(event, pbprdf:forTeam, teamIri)`

## 5) Event Sequence Triples

After all events are loaded, ordered by `eventNumber`:

- each event gets `pbprdf:eventNumber` (1-based index)
- links to neighbors:
  - `pbprdf:previousEvent`
  - `pbprdf:nextEvent`
- time deltas are generated only when adjacent events are in the same period:
  - `secondsSincePreviousEvent`
  - `secondsUntilNextEvent`

---

## Event-Type Mapping Rules (Play Semantics)

## Shot

Type assertions:

- `rdf:type pbprdf:Shot`

Properties:

- `shotBy` (player)
- `shotMade` (boolean)
- `shotPoints`:
  - 1 if shot text contains `"free throw"`
  - 3 if shot text contains `"three point"`
  - otherwise 2
- optional `shotType` if non-empty text captured
- optional `shotAssistedBy` when assist text is present

## Block

Type assertions:

- `rdf:type pbprdf:Shot`
- `rdf:type pbprdf:Block`

Properties:

- `shotBy` (shooter)
- `shotBlockedBy` (blocker)

## Foul

Type assertions:

- `rdf:type pbprdf:Foul`

Properties:

- `foulCommittedBy` (always)
- optional `foulDrawnBy`
- conditional boolean flags:
  - `isShootingFoul`
  - `isPersonalBlockingFoul`
  - `isOffensive`
  - `isCharge` (with offensive charge)
  - `isLooseBallFoul`

Notes:

- Many foul text variants are recognized, but only selected variants map to explicit flags.

## Rebound

Type assertions:

- `rdf:type pbprdf:Rebound`

Properties:

- `reboundedBy` (string mapped to a Player IRI)
- `isOffensive=true` only for offensive rebounds

Important V1 behavior:

- Team rebounds are still mapped through `reboundedBy` to a Player IRI derived from team text.

## Turnover

Type assertions:

- `rdf:type pbprdf:Turnover`

Properties:

- usually `turnedOverBy` + `turnoverType` (lowercased type text)
- optional `stolenBy`
- special case `"shot clock"`:
  - emits `turnoverType="shot clock"`
  - does not emit `turnedOverBy`

## Timeout

Type assertions:

- `rdf:type pbprdf:Timeout`

Properties:

- official timeout: `isOfficial=true`
- team timeout with explicit duration:
  - `timeoutDuration="Full"` or
  - `timeoutDuration="20 Sec."`

## Enters (Substitution)

Type assertions:

- `rdf:type pbprdf:Enters`

Properties:

- `playerEntering`
- `playerExiting`

Also used as the source of truth for roster construction (see roster rules).

## JumpBall

Type assertions:

- `rdf:type pbprdf:JumpBall`

Properties:

- `jumpBallAwayPlayer`
- `jumpBallHomePlayer`
- optional `jumpBallGainedPossession`

## TechnicalFoul (standard)

Type assertions:

- `rdf:type pbprdf:TechnicalFoul`

Properties:

- `technicalFoulNumber` (int)
- optional `foulCommittedBy`

## DoubleTechnicalFoul

Type assertions:

- `rdf:type pbprdf:TechnicalFoul`

Properties:

- two `foulCommittedBy` triples (one per player)

## ThreeSecondViolation

Type assertions:

- `rdf:type pbprdf:TechnicalFoul`

Properties:

- `isThreeSecond=true`
- `foulCommittedBy`

## DelayOfGame

Type assertions:

- `rdf:type pbprdf:TechnicalFoul`

Properties:

- `isDelayOfGame=true`

## FiveSecondViolation

Type assertions:

- `rdf:type pbprdf:FiveSecondViolation`

## Ejection

Type assertions:

- `rdf:type pbprdf:Ejection`

Properties:

- `playerEjected`

## EndOfPlay

If text is `End of Game`:

- `rdf:type pbprdf:EndOfGame`

Otherwise (`End of ...`):

- `rdf:type pbprdf:EndOfPeriod`

## Unrecognized Events

When no play type matcher is satisfied:

- event is still emitted as a generic `pbprdf:Event`
- no play-specific type/properties are added
- no `pbprdf:Play` type or `pbprdf:forTeam` triple

---

## Time and Period Business Rules

`GamePeriodInfo` presets:

- WNBA: 4 regulation periods x 10 min, OT 5 min
- NBA: 4 regulation periods x 12 min, OT 5 min
- NCAAW: 4 x 10, OT 5
- NCAAM: 2 x 20, OT 5

Clock parsing accepts:

- `MM:SS`
- `SS.fraction`

For each parsed clock:

- compute `secondsIntoPeriod`
- derive `secondsIntoGame` from:
  - elapsed regulation periods,
  - elapsed overtimes (if any),
  - current period elapsed time
- derive `secondsLeftInPeriod` as period length minus `secondsIntoPeriod`

Overtime is detected when `period > regulationPeriods`.

---

## V1 Behavioral Quirks to Preserve or Intentionally Redesign in V2

- Roster membership depends only on `Enters` events.
- Team rebounds are represented via `reboundedBy` with a Player-like IRI from team text.
- Some recognized foul/turnover textual variants do not produce dedicated boolean properties beyond base type + actors.
- Play matching is strict full-string matching against each event type's pattern.
- Unknown plays still produce valid generic event triples.

---

## Recommended V2 Use of This Spec

Treat this as the **compatibility baseline** for V1 semantics:

- If V2 aims for strict backward compatibility, reproduce these triples and quirks.
- If V2 aims to improve modeling quality, use this as a changelog baseline and explicitly document each intentional divergence.

