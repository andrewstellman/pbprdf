# Schema Report

Generated: 2026-03-10T00:03:56.474520Z

## Per-League Capture Summary

### NBA (`nba`)

- Fixture: `/Users/andrewstellman/Documents/pbprdf/v2/pbprdf-v2/tests/fixtures/nba_401810770.json`
- Game ID: `401810770`
- Game: Orlando Magic at Minnesota Timberwolves
- Date: `2026-03-07T20:00Z`
- Sections present: againstTheSpread, article, boxscore, broadcasts, format, gameInfo, header, injuries, leaders, meta, news, odds, pickcenter, plays, seasonseries, standings, videos, wallclockAvailable, winprobability
- Play count: `506`
- Plays with coordinates: `506`
- Plays with participants: `466`
- format.regulation: `{"clock": 720.0, "displayName": "Quarter", "periods": 4, "slug": "quarter"}`
- Unique `play.type.text` values (58): `Alley Oop Dunk Shot`, `Alley Oop Layup Shot`, `Bad Pass
Turnover`, `Challenge`, `Coach's Challenge (Overturned)`, `Coach's Challenge (Stands)`, `Cutting Dunk Shot`, `Cutting Layup Shot`, `Defensive Goaltending`, `Defensive Rebound`, `Driving Dunk Shot`, `Driving Finger Roll Layup`, `Driving Floating Bank Jump Shot`, `Driving Floating Jump Shot`, `Driving Hook Shot`, `Driving Layup Shot`, `Dunk Shot`, `End Game`, `End Period`, `Fade Away Jump Shot`, `Flagrant Foul Type 1`, `Floating Jump Shot`, `Free Throw - 1 of 1`, `Free Throw - 1 of 2`, `Free Throw - 2 of 2`, `Free Throw - Flagrant 1 of 2`, `Free Throw - Flagrant 2 of 2`, `Full Timeout`, `Jump Shot`, `Jumpball`, `Layup Running Reverse`, `Layup Shot`, `Layup Shot Putback`, `Loose Ball Foul`, `Lost Ball Turnover`, `Offensive Foul`, `Offensive Foul Turnover`, `Offensive Rebound`, `Out of Bounds - Bad Pass Turnover`, `Out of Bounds - Lost Ball Turnover`, `Personal Foul`, `Pullup Jump Shot`, `Ref-Initiated Review (Overturned)`, `Running Dunk Shot`, `Running Finger Roll Layup`, `Running Jump Shot`, `Running Layup Shot`, `Running Pullup Jump Shot`, `Shooting Foul`, `Shot Clock Turnover` ...
- Unique `participant.type` values (0): _none_
- Validation: PASS
- Top-level fields not modeled explicitly (captured via `extra=allow`): `againstTheSpread`, `article`, `broadcasts`, `header`, `injuries`, `leaders`, `meta`, `news`, `odds`, `pickcenter`, `seasonseries`, `standings`, `videos`, `wallclockAvailable`, `winprobability`

#### Sample Play (First Scoring Play)

```json
{
  "awayScore": 2,
  "clock": {
    "displayValue": "11:43"
  },
  "coordinate": {
    "x": 25,
    "y": 2
  },
  "homeScore": 0,
  "id": "4018107707",
  "participants": [
    {
      "athlete": {
        "id": "4432573"
      }
    },
    {
      "athlete": {
        "id": "4432165"
      }
    }
  ],
  "period": {
    "displayValue": "1st Quarter",
    "number": 1
  },
  "pointsAttempted": 2,
  "scoreValue": 2,
  "scoringPlay": true,
  "sequenceNumber": "7",
  "shootingPlay": true,
  "shortDescription": "+2 Points",
  "team": {
    "id": "19"
  },
  "text": "Paolo Banchero makes 1-foot dunk (Jalen Suggs assists)",
  "type": {
    "id": "151",
    "text": "Cutting Dunk Shot"
  },
  "wallclock": "2026-03-07T20:13:24Z"
}
```

### WNBA (`wnba`)

- Fixture: `/Users/andrewstellman/Documents/pbprdf/v2/pbprdf-v2/tests/fixtures/wnba_401820329.json`
- Game ID: `401820329`
- Game: Las Vegas Aces at Phoenix Mercury
- Date: `2025-10-11T00:00Z`
- Sections present: againstTheSpread, boxscore, broadcasts, format, gameInfo, header, injuries, leaders, meta, news, odds, pickcenter, plays, seasonseries, standings, videos, wallclockAvailable, winprobability
- Play count: `414`
- Plays with coordinates: `414`
- Plays with participants: `378`
- format.regulation: `{"clock": 600.0, "displayName": "Quarter", "periods": 4, "slug": "quarter"}`
- Unique `play.type.text` values (48): `Bad Pass
Turnover`, `Coach's Challenge (Stands)`, `Cutting Layup Shot`, `Defensive Rebound`, `Delay of Game`, `Double Technical Foul`, `Driving Finger Roll Layup`, `Driving Floating Bank Jump Shot`, `Driving Floating Jump Shot`, `Driving Layup Shot`, `End Game`, `End Period`, `Fade Away Jump Shot`, `Free Throw - 1 of 1`, `Free Throw - 1 of 2`, `Free Throw - 2 of 2`, `Free Throw - Technical`, `Full Timeout`, `Jump Shot`, `Jumpball`, `Layup Driving Reverse`, `Layup Shot`, `Layup Shot Putback`, `Lost Ball Turnover`, `No Timeout`, `Offensive Charge`, `Offensive Foul`, `Offensive Foul Turnover`, `Offensive Rebound`, `Official Timeout`, `Out of Bounds - Bad Pass Turnover`, `Out of Bounds - Lost Ball Turnover`, `Personal Foul`, `Pullup Jump Shot`, `Ref-Initiated Review (Stands)`, `Running Jump Shot`, `Running Layup Shot`, `Running Pullup Jump Shot`, `Shooting Foul`, `Step Back Jump Shot`, `Substitution`, `Technical Foul`, `Traveling`, `Turnaround Bank Jump Shot`, `Turnaround Fade Away Jump Shot`, `Turnaround Hook Shot`, `Turnaround Jump Shot`, `ejection`
- Unique `participant.type` values (0): _none_
- Validation: PASS
- Top-level fields not modeled explicitly (captured via `extra=allow`): `againstTheSpread`, `broadcasts`, `header`, `injuries`, `leaders`, `meta`, `news`, `odds`, `pickcenter`, `seasonseries`, `standings`, `videos`, `wallclockAvailable`, `winprobability`

#### Sample Play (First Scoring Play)

```json
{
  "awayScore": 2,
  "clock": {
    "displayValue": "9:21"
  },
  "coordinate": {
    "x": 21,
    "y": 3
  },
  "homeScore": 0,
  "id": "40182032911",
  "participants": [
    {
      "athlete": {
        "id": "4398776"
      }
    }
  ],
  "period": {
    "displayValue": "1st Quarter",
    "number": 1
  },
  "pointsAttempted": 2,
  "scoreValue": 2,
  "scoringPlay": true,
  "sequenceNumber": "11",
  "shootingPlay": true,
  "shortDescription": "+2 Points",
  "team": {
    "id": "17"
  },
  "text": "NaLyssa Smith makes two point shot",
  "type": {
    "id": "125",
    "text": "Layup Shot Putback"
  },
  "wallclock": "2025-10-11T00:08:51Z"
}
```

### NCAAM (`mens-college-basketball`)

- Fixture: `/Users/andrewstellman/Documents/pbprdf/v2/pbprdf-v2/tests/fixtures/ncaam_401825568.json`
- Game ID: `401825568`
- Game: Michigan State Spartans at Michigan Wolverines
- Date: `2026-03-08T20:30Z`
- Sections present: againstTheSpread, article, boxscore, broadcasts, format, gameInfo, header, leaders, meta, news, odds, pickcenter, plays, standings, videos, wallclockAvailable, winprobability
- Play count: `492`
- Plays with coordinates: `492`
- Plays with participants: `460`
- format.regulation: `{"clock": 1200.0, "displayName": "Half", "periods": 2, "slug": "half"}`
- Unique `play.type.text` values (19): `Block Shot`, `Coach's Challenge (Stands)`, `Dead Ball Rebound`, `Defensive Rebound`, `DunkShot`, `End Game`, `End Period`, `JumpShot`, `Jumpball`, `LayUpShot`, `Lost Ball Turnover`, `MadeFreeThrow`, `Offensive Rebound`, `OfficialTVTimeOut`, `PersonalFoul`, `ShortTimeOut`, `Steal`, `Substitution`, `Technical Foul`
- Unique `participant.type` values (0): _none_
- Validation: PASS
- Top-level fields not modeled explicitly (captured via `extra=allow`): `againstTheSpread`, `article`, `broadcasts`, `header`, `leaders`, `meta`, `news`, `odds`, `pickcenter`, `standings`, `videos`, `wallclockAvailable`, `winprobability`

#### Sample Play (First Scoring Play)

```json
{
  "awayScore": 2,
  "clock": {
    "displayValue": "18:47"
  },
  "coordinate": {
    "x": 25,
    "y": 3
  },
  "homeScore": 0,
  "id": "401825568120103719",
  "participants": [
    {
      "athlete": {
        "id": "5105817"
      }
    },
    {
      "athlete": {
        "id": "4711255"
      }
    }
  ],
  "period": {
    "displayValue": "1st Half",
    "number": 1
  },
  "pointsAttempted": 2,
  "scoreValue": 2,
  "scoringPlay": true,
  "sequenceNumber": "120103719",
  "shootingPlay": true,
  "shortDescription": "+2 Points",
  "team": {
    "id": "127"
  },
  "text": "Carson Cooper makes layup (Jeremy Fears Jr. assists)",
  "type": {
    "id": "572",
    "text": "LayUpShot"
  },
  "wallclock": "2026-03-08T20:41:52Z"
}
```

### NCAAW (`womens-college-basketball`)

- Fixture: `/Users/andrewstellman/Documents/pbprdf/v2/pbprdf-v2/tests/fixtures/ncaaw_401851327.json`
- Game ID: `401851327`
- Game: Creighton Bluejays at UConn Huskies
- Date: `2026-03-08T18:30Z`
- Sections present: againstTheSpread, article, boxscore, broadcasts, format, gameInfo, header, leaders, meta, news, odds, pickcenter, plays, standings, videos, wallclockAvailable, winprobability
- Play count: `427`
- Plays with coordinates: `427`
- Plays with participants: `402`
- format.regulation: `{"clock": 600.0, "displayName": "Quarter", "periods": 4, "slug": "quarter"}`
- Unique `play.type.text` values (18): `Block Shot`, `Coach's Challenge (Overturned)`, `Dead Ball Rebound`, `Defensive Rebound`, `End Game`, `End Period`, `JumpShot`, `Jumpball`, `LayUpShot`, `Lost Ball Turnover`, `MadeFreeThrow`, `Offensive Rebound`, `OfficialTVTimeOut`, `PersonalFoul`, `ShortTimeOut`, `Steal`, `Substitution`, `TipShot`
- Unique `participant.type` values (0): _none_
- Validation: PASS
- Top-level fields not modeled explicitly (captured via `extra=allow`): `againstTheSpread`, `article`, `broadcasts`, `header`, `leaders`, `meta`, `news`, `odds`, `pickcenter`, `standings`, `videos`, `wallclockAvailable`, `winprobability`

#### Sample Play (First Scoring Play)

```json
{
  "awayScore": 0,
  "clock": {
    "displayValue": "9:20"
  },
  "coordinate": {
    "x": 24,
    "y": 25
  },
  "homeScore": 3,
  "id": "401851327120093458",
  "participants": [
    {
      "athlete": {
        "id": "4433790"
      }
    },
    {
      "athlete": {
        "id": "5174491"
      }
    }
  ],
  "period": {
    "displayValue": "1st Quarter",
    "number": 1
  },
  "pointsAttempted": 3,
  "scoreValue": 3,
  "scoringPlay": true,
  "sequenceNumber": "120093458",
  "shootingPlay": true,
  "shortDescription": "+3 Points",
  "team": {
    "id": "41"
  },
  "text": "Azzi Fudd makes 25-foot three point jumper (KK Arnold assists)",
  "type": {
    "id": "558",
    "text": "JumpShot"
  },
  "wallclock": "2026-03-08T18:34:02Z"
}
```

## Cross-League Comparison

- Top-level fields present in all four leagues: `againstTheSpread`, `boxscore`, `broadcasts`, `format`, `gameInfo`, `header`, `leaders`, `meta`, `news`, `odds`, `pickcenter`, `plays`, `standings`, `videos`, `wallclockAvailable`, `winprobability`
- Top-level fields present in some but not all leagues: `article`, `injuries`, `seasonseries`
- Fields present in JSON but not explicitly in Pydantic top-level model: `againstTheSpread`, `article`, `broadcasts`, `header`, `injuries`, `leaders`, `meta`, `news`, `odds`, `pickcenter`, `seasonseries`, `standings`, `videos`, `wallclockAvailable`, `winprobability`
- Fields present in JSON but not explicitly in Pydantic `Play` model: _none_

