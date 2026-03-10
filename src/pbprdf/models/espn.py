"""
Pydantic models for ESPN summary API response.

These serve as the contract between the ESPN API and the pbprdf mapper.
When ESPN changes their schema, validation fails here first.
"""

from __future__ import annotations

from typing import Any, Optional

from pydantic import AliasChoices, BaseModel, ConfigDict, Field


# --- Play-level models ---


class PlayType(BaseModel):
    id: str
    text: str

    model_config = ConfigDict(extra="allow")


class PlayClock(BaseModel):
    displayValue: str

    model_config = ConfigDict(extra="allow")


class PlayPeriod(BaseModel):
    number: int
    displayValue: Optional[str] = None

    model_config = ConfigDict(extra="allow")


class PlayTeamRef(BaseModel):
    id: str

    model_config = ConfigDict(extra="allow")


class AthleteRef(BaseModel):
    id: str

    model_config = ConfigDict(extra="allow")


class PlayParticipant(BaseModel):
    athlete: AthleteRef
    type: Optional[str] = None

    model_config = ConfigDict(extra="allow")


class Coordinate(BaseModel):
    x: int
    y: int

    model_config = ConfigDict(extra="allow")


class Play(BaseModel):
    id: str
    sequenceNumber: Optional[str] = None
    type: PlayType
    text: str
    shortText: Optional[str] = Field(
        default=None,
        validation_alias=AliasChoices("shortText", "shortDescription"),
    )
    awayScore: Optional[int] = None
    homeScore: Optional[int] = None
    period: PlayPeriod
    clock: PlayClock
    scoringPlay: bool = False
    shootingPlay: bool = False
    scoreValue: Optional[int] = None
    pointsAttempted: Optional[int] = None
    team: Optional[PlayTeamRef] = None
    participants: list[PlayParticipant] = Field(default_factory=list)
    coordinate: Optional[Coordinate] = None
    wallclock: Optional[str] = None

    model_config = ConfigDict(extra="allow")


# --- Format models ---


class RegulationFormat(BaseModel):
    periods: int
    displayName: str
    slug: str
    clock: float

    model_config = ConfigDict(extra="allow")


class OvertimeFormat(BaseModel):
    clock: float

    model_config = ConfigDict(extra="allow")


class GameFormat(BaseModel):
    regulation: RegulationFormat
    overtime: OvertimeFormat

    model_config = ConfigDict(extra="allow")


# --- GameInfo models ---


class VenueAddress(BaseModel):
    city: Optional[str] = None
    state: Optional[str] = None
    zipCode: Optional[str] = None

    model_config = ConfigDict(extra="allow")


class Venue(BaseModel):
    id: str
    fullName: str
    address: Optional[VenueAddress] = None

    model_config = ConfigDict(extra="allow")


class OfficialPosition(BaseModel):
    name: str
    displayName: str

    model_config = ConfigDict(extra="allow")


class Official(BaseModel):
    fullName: str
    displayName: str
    position: Optional[OfficialPosition] = None
    order: Optional[int] = None

    model_config = ConfigDict(extra="allow")


class GameInfo(BaseModel):
    venue: Optional[Venue] = None
    attendance: Optional[int] = None
    officials: list[Official] = Field(default_factory=list)

    model_config = ConfigDict(extra="allow")


# --- Boxscore models (minimal, for validation only) ---


class BoxscoreTeamInfo(BaseModel):
    id: str
    displayName: str
    abbreviation: str

    model_config = ConfigDict(extra="allow")


class BoxscoreAthleteInfo(BaseModel):
    id: str
    displayName: str
    jersey: Optional[str] = None

    model_config = ConfigDict(extra="allow")


class BoxscoreAthlete(BaseModel):
    athlete: BoxscoreAthleteInfo
    starter: bool = False
    didNotPlay: bool = False
    ejected: bool = False
    stats: list[str] = Field(default_factory=list)

    model_config = ConfigDict(extra="allow")


class BoxscoreStatCategory(BaseModel):
    names: list[str] = Field(default_factory=list)
    keys: list[str] = Field(default_factory=list)
    athletes: list[BoxscoreAthlete] = Field(default_factory=list)

    model_config = ConfigDict(extra="allow")


class BoxscorePlayerEntry(BaseModel):
    team: BoxscoreTeamInfo
    statistics: list[BoxscoreStatCategory] = Field(default_factory=list)

    model_config = ConfigDict(extra="allow")


class Boxscore(BaseModel):
    teams: list[dict[str, Any]] = Field(default_factory=list)
    players: list[BoxscorePlayerEntry] = Field(default_factory=list)

    model_config = ConfigDict(extra="allow")


class WinProbabilityEntry(BaseModel):
    playId: str
    homeWinPercentage: float
    tiePercentage: float = 0.0
    secondsLeft: Optional[int] = None

    model_config = ConfigDict(extra="allow")


# --- Top-level summary response ---


class SummaryResponse(BaseModel):
    """
    Top-level model for the ESPN summary endpoint response.

    Not all sections are modeled in detail. Sections we don't need
    for mapping are left as raw dicts via model_config.
    """

    boxscore: Optional[Boxscore] = None
    format: Optional[GameFormat] = None
    gameInfo: Optional[GameInfo] = None
    plays: list[Play] = Field(default_factory=list)
    winprobability: list[WinProbabilityEntry] = Field(default_factory=list)

    model_config = ConfigDict(extra="allow")

