# pbprdf V2 Target Ontology Delta (ESPN JSON API)

## Intent

This document defines the **target ontology delta** from V1 (`v2/V1_SPEC.md`) to a V2 model backed by ESPN's JSON summary API.

It captures:
- what to keep from V1 for compatibility,
- what to add from new ESPN data,
- what to normalize/improve in the model.

---

## ESPN API Details (Current Source)

## Endpoint Pattern

- `https://site.api.espn.com/apis/site/v2/sports/basketball/{sport}/summary?event={game_id}`

Example sports tested:
- `womens-college-basketball`
- `wnba`

Target sports for V2 support:
- `mens-college-basketball` (NCAAM)
- `womens-college-basketball` (NCAAW)
- `nba`
- `wnba`

## Important Notes

- This is an **unofficial/undocumented public endpoint**; schema can change without notice.
- Payload is rich and nested; many sections are optional by sport/game state.
- Some arrays are often present but empty (`odds`, `pickcenter`, `broadcasts`).

## Top-Level Sections Observed

- `header`
- `gameInfo`
- `boxscore`
- `plays`
- `winprobability`
- `leaders`
- `standings`
- `news`
- `videos`
- plus metadata and optional sections: `againstTheSpread`, `odds`, `injuries`, `seasonseries`, `article`, `format`, `meta`, `wallclockAvailable`

---

## New Data Available vs V1

V1 was mostly event text + period/clock/score and regex-derived semantics.  
V2 source includes structured fields not previously modeled:

- **Play identity and ordering**
  - `plays[].id`
  - `plays[].sequenceNumber`
  - `plays[].wallclock` (ISO timestamp)

- **Play typing and flags**
  - `plays[].type.id`
  - `plays[].type.text`
  - `plays[].shortDescription`
  - `plays[].scoringPlay` (bool)
  - `plays[].shootingPlay` (bool)
  - `plays[].scoreValue` (int)
  - `plays[].pointsAttempted` (int)

- **Spatial data**
  - `plays[].coordinate.x`
  - `plays[].coordinate.y`

- **Actor references**
  - `plays[].team` (team object/id reference)
  - `plays[].participants[].athlete.id` (athlete IDs involved)

- **Game context and metadata**
  - Venue details (`gameInfo.venue.*`)
  - Officials (`gameInfo.officials[]`)
  - Competition status and availability flags (`header.competitions[0].status`, `...Available`)
  - Competitor/linescore details (`header.competitions[0].competitors[]`)

- **Derived feeds**
  - `winprobability[]` timeline tied to play IDs
  - Team/player boxscore structures
  - Leaders, standings, news, videos

---

## V2 Compatibility Strategy

## Keep As-Is (Core V1 Semantics)

Keep these classes/properties for backwards compatibility:

- Core classes: `Game`, `Event`, `Play`, `Team`, `Player`, `Roster`
- Core event properties: `inGame`, `period`, `time`, `secondsIntoGame`, `secondsLeftInPeriod`, `awayScore`, `homeScore`
- Event chain: `eventNumber`, `previousEvent`, `nextEvent`, `secondsSincePreviousEvent`, `secondsUntilNextEvent`
- Existing play-type classes and relations (`Shot`, `Foul`, `Turnover`, etc.)

## Keep But Source Differently

- `forTeam`: now sourced from `plays[].team.id` (fallback to legacy resolution as needed)
- `period` and `time`: sourced from structured `plays[].period.number` and `plays[].clock.displayValue`
- score fields: sourced directly from `plays[].awayScore/homeScore`

---

## Target Ontology Additions

Prefix examples assume existing namespace: `pbprdf:`.

## 1) Stable External IDs

### New properties

- `espnEventId` (`Game -> xsd:string`)
- `espnPlayId` (`Event -> xsd:string`)
- `espnSequenceNumber` (`Event -> xsd:string`)
- `espnTeamId` (`Team -> xsd:string`)
- `espnAthleteId` (`Player -> xsd:string`)

### Rationale

V1 IRIs were name-based and can collide; V2 should preserve source IDs for deterministic joins.

## 2) Structured Play Metadata

### New class

- `PlayType`

### New properties

- `hasPlayType` (`Play -> PlayType`)
- `playTypeId` (`PlayType -> xsd:string`)
- `playTypeText` (`PlayType -> xsd:string`)
- `shortDescription` (`Play -> xsd:string`)
- `wallclockTime` (`Play -> xsd:dateTime`)
- `isScoringPlay` (`Play -> xsd:boolean`)
- `isShootingPlay` (`Play -> xsd:boolean`)
- `scoreValue` (`Play -> xsd:int`)
- `pointsAttempted` (`Play -> xsd:int`)

### Rationale

Avoids text parsing for semantics already provided as structured fields.

## 3) Court Spatial Model

### New class

- `CourtCoordinate`

### New properties

- `hasCoordinate` (`Play -> CourtCoordinate`)
- `coordinateX` (`CourtCoordinate -> xsd:int`)
- `coordinateY` (`CourtCoordinate -> xsd:int`)

### Rationale

Supports shot chart and spatial analytics.

## 4) Play Participants

### New class

- `Participation`

### New properties

- `hasParticipation` (`Play -> Participation`)
- `participantPlayer` (`Participation -> Player`)
- `participantRole` (`Participation -> xsd:string`, optional when derivable)
- `participantOrder` (`Participation -> xsd:int`, optional)

### Rationale

V1 had single-purpose properties (`shotBy`, `stolenBy`, etc.). V2 can preserve those and also model generic multi-actor participation.

## 5) Win Probability Timeline

### New class

- `WinProbabilitySnapshot`

### New properties

- `hasWinProbabilitySnapshot` (`Game -> WinProbabilitySnapshot`)
- `snapshotForPlay` (`WinProbabilitySnapshot -> Event`)
- `homeWinProbability` (`WinProbabilitySnapshot -> xsd:decimal`)
- `tieProbability` (`WinProbabilitySnapshot -> xsd:decimal`)

### Rationale

Adds a first-class analytical timeline keyed by play ID.

## 6) Venue and Officials

### New classes

- `Venue`
- `Official`

### New properties

- `venue` (`Game -> Venue`)
- `venueName` (`Venue -> xsd:string`)
- `venueId` (`Venue -> xsd:string`)
- `venueAddress` (`Venue -> xsd:string`)
- `attendance` (`Game -> xsd:int`)
- `hasOfficial` (`Game -> Official`)
- `officialName` (`Official -> xsd:string`)
- `officialPosition` (`Official -> xsd:string`)

### Rationale

V1 only had free-text location; V2 should model venue and officiating entities.

## 7) Boxscore + Leaders (Optional Module)

### New classes

- `TeamGameStats`
- `PlayerGameStats`
- `StatLine`
- `LeaderCategory`

### New properties

- `hasTeamGameStats` (`Team -> TeamGameStats`)
- `hasPlayerGameStats` (`Player -> PlayerGameStats`)
- `statName`, `statLabel`, `statValue`
- `isStarter`, `didNotPlay`, `wasEjected`
- `hasLeaderCategory` (`Team -> LeaderCategory`)

### Rationale

This is high-value but can be implemented as phase 2 if play graph is priority.

## 8) League and Period/Clock Format (Required for NCAAM/NCAAW/NBA/WNBA)

### New class

- `GameFormat`

### New properties

- `hasGameFormat` (`Game -> GameFormat`)
- `leagueCode` (`GameFormat -> xsd:string`)  
  Recommended values: `NCAAM`, `NCAAW`, `NBA`, `WNBA`
- `regulationPeriodType` (`GameFormat -> xsd:string`)  
  Recommended values: `HALF`, `QUARTER`
- `regulationPeriodCount` (`GameFormat -> xsd:int`)  
  Examples: 2 (NCAAM), 4 (NCAAW/NBA/WNBA)
- `regulationPeriodLengthMinutes` (`GameFormat -> xsd:int`)  
  Examples: 20 (NCAAM), 10 (NCAAW/WNBA), 12 (NBA)
- `overtimePeriodLengthMinutes` (`GameFormat -> xsd:int`)  
  Usually 5

### Optional event-level properties

- `isOvertimePeriod` (`Event -> xsd:boolean`)
- `regulationPeriodIndex` (`Event -> xsd:int`)  
  1..N for regulation periods only; omit during OT
- `overtimeIndex` (`Event -> xsd:int`)  
  1 for first OT, 2 for second OT, etc.

### Rationale

This generalizes halves and quarters with one model.  
Your suggested "quarter length property" becomes robust when paired with period type and period count.

---

## Model Corrections from V1

## 1) Team Rebounds

V1 behavior creates `Player` IRIs from team rebound text.  
V2 change:

- add `reboundCreditedToTeam` (`Rebound -> Team`) for team rebounds,
- keep `reboundedBy` only when a player athlete ID exists.

## 2) Player Identity

V1 player IRIs are name-derived.  
V2 change:

- canonicalize player identity by ESPN athlete ID,
- keep name-based aliasing only as fallback.

## 3) Event Typing Source of Truth

V1 infers type from text patterns.  
V2 change:

- primary type from `plays[].type`,
- optional text-derived enrichment remains secondary for backward-compatible specialized properties.

---

## Proposed New/Updated Property Inventory (Condensed)

- Identity: `espnEventId`, `espnPlayId`, `espnSequenceNumber`, `espnTeamId`, `espnAthleteId`
- Play metadata: `hasPlayType`, `shortDescription`, `wallclockTime`, `isScoringPlay`, `isShootingPlay`, `scoreValue`, `pointsAttempted`
- Spatial: `hasCoordinate`, `coordinateX`, `coordinateY`
- Participants: `hasParticipation`, `participantPlayer`
- Timeline: `hasWinProbabilitySnapshot`, `snapshotForPlay`, `homeWinProbability`, `tieProbability`
- Context: `venue`, `attendance`, `hasOfficial`
- Game format: `hasGameFormat`, `leagueCode`, `regulationPeriodType`, `regulationPeriodCount`, `regulationPeriodLengthMinutes`, `overtimePeriodLengthMinutes`
- Normalization: `reboundCreditedToTeam`

---

## Ingestion-to-Graph Mapping Rules (V2 Target)

## Game

- Build `Game` from `header` + competition/gameInfo context.
- Attach `espnEventId` from summary event id.
- Preserve V1 `gameTime`, `homeTeam`, `awayTeam` semantics.
- Attach a `GameFormat` node and set format properties by sport:
  - NCAAM: `HALF`, count 2, length 20, OT 5
  - NCAAW: `QUARTER`, count 4, length 10, OT 5
  - NBA: `QUARTER`, count 4, length 12, OT 5
  - WNBA: `QUARTER`, count 4, length 10, OT 5

## Teams and Players

- Materialize teams using ESPN team IDs from competitors/plays/boxscore.
- Materialize players using ESPN athlete IDs from participants and boxscore.
- Maintain labels from display names.

## Plays/Events

- One `Event/Play` node per `plays[]` element.
- `espnPlayId` and `espnSequenceNumber` stored verbatim.
- Keep V1 chain properties; order by `sequenceNumber` if present, else original array order.
- Attach structured play metadata/flags/coordinates directly.
- Compute `secondsIntoGame` and `secondsLeftInPeriod` from `GameFormat` instead of league-specific hardcoding.
- Keep `period` as the raw period number from ESPN; derive `isOvertimePeriod` when `period > regulationPeriodCount`.

## Specialized Basketball Semantics

- Keep V1 specialized properties (`shotBy`, `shotMade`, etc.) where confidently derivable.
- Prefer structured flags and participant IDs over description parsing.

## Analytical Timelines

- Link each `winprobability[]` row to the play via `playId -> espnPlayId`.

---

## Phased Delivery Plan

## Phase 1 (Minimum Viable V2 Graph)

- V1 core classes/properties
- ESPN IDs
- league/period format (`GameFormat`)
- structured play metadata
- coordinates
- participants (athlete IDs)
- win probability snapshots

## Phase 2 (Context Expansion)

- venue and officials
- team/player boxscore stat entities
- leaders and standings linkage

## Phase 3 (Quality and Reasoning)

- robust identity reconciliation
- optional inferencing rules for shot/foul taxonomy
- provenance metadata per triple source

---

## Open Questions (Decide Before Implementation Freeze)

- Should V2 IRIs be ESPN-ID-first, or keep name-based V1 IRIs with ID properties attached?
- Should `scoreValue` override or complement legacy `shotPoints` inference when they differ?
- Do we want full boxscore/stat ontology now, or park for phase 2?
- Should we model participants with explicit roles now, or start role-agnostic and enrich later?

