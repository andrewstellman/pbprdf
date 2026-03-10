from __future__ import annotations

from dataclasses import dataclass

from rdflib import Graph, Literal, Namespace, URIRef
from rdflib.namespace import OWL, RDF, RDFS, XSD

PBPRDF = Namespace("http://stellman-greene.com/pbprdf#")
PBPRDF_ENTITY = Namespace("http://stellman-greene.com/pbprdf/")


@dataclass(frozen=True)
class PropertySpec:
    name: str
    domain: str
    range: str
    label: str
    object_property: bool = False
    subproperty_of: str | None = None


CLASS_NAMES = [
    "Game",
    "Event",
    "Play",
    "Team",
    "Player",
    "Roster",
    "Shot",
    "Block",
    "Foul",
    "TechnicalFoul",
    "Turnover",
    "Rebound",
    "JumpBall",
    "Timeout",
    "Enters",
    "Ejection",
    "EndOfPeriod",
    "EndOfGame",
    "FiveSecondViolation",
    "PlayType",
    "CourtCoordinate",
    "GameFormat",
    "WinProbabilitySnapshot",
    "Venue",
    "Official",
]


PROPERTY_SPECS = [
    # Game + roster
    PropertySpec("gameTime", "Game", str(XSD.dateTime), "gameTime"),
    PropertySpec("gameLocation", "Game", str(XSD.string), "gameLocation"),
    PropertySpec("homeTeam", "Game", "Team", "homeTeam", object_property=True),
    PropertySpec("awayTeam", "Game", "Team", "awayTeam", object_property=True),
    PropertySpec("hasHomeTeamRoster", "Game", "Roster", "hasHomeTeamRoster", object_property=True),
    PropertySpec("hasAwayTeamRoster", "Game", "Roster", "hasAwayTeamRoster", object_property=True),
    PropertySpec("rosterTeam", "Roster", "Team", "rosterTeam", object_property=True),
    PropertySpec("hasPlayer", "Roster", "Player", "hasPlayer", object_property=True),
    PropertySpec("espnEventId", "Game", str(XSD.string), "espnEventId"),
    PropertySpec("hasGameFormat", "Game", "GameFormat", "hasGameFormat", object_property=True),
    # Game format
    PropertySpec("leagueCode", "GameFormat", str(XSD.string), "leagueCode"),
    PropertySpec("regulationPeriodType", "GameFormat", str(XSD.string), "regulationPeriodType"),
    PropertySpec("regulationPeriodCount", "GameFormat", str(XSD.int), "regulationPeriodCount"),
    PropertySpec("regulationPeriodLengthMinutes", "GameFormat", str(XSD.int), "regulationPeriodLengthMinutes"),
    PropertySpec("overtimePeriodLengthMinutes", "GameFormat", str(XSD.int), "overtimePeriodLengthMinutes"),
    # Event core
    PropertySpec("inGame", "Event", "Game", "inGame", object_property=True),
    PropertySpec("forTeam", "Event", "Team", "forTeam", object_property=True),
    PropertySpec("time", "Event", str(XSD.string), "time"),
    PropertySpec("period", "Event", str(XSD.int), "period"),
    PropertySpec("secondsIntoGame", "Event", str(XSD.int), "secondsIntoGame"),
    PropertySpec("secondsLeftInPeriod", "Event", str(XSD.int), "secondsLeftInPeriod"),
    PropertySpec("eventNumber", "Event", str(XSD.int), "eventNumber"),
    PropertySpec("previousEvent", "Event", "Event", "previousEvent", object_property=True),
    PropertySpec("nextEvent", "Event", "Event", "nextEvent", object_property=True),
    PropertySpec("secondsSincePreviousEvent", "Event", str(XSD.int), "secondsSincePreviousEvent"),
    PropertySpec("secondsUntilNextEvent", "Event", str(XSD.int), "secondsUntilNextEvent"),
    PropertySpec("homeScore", "Event", str(XSD.int), "homeScore"),
    PropertySpec("awayScore", "Event", str(XSD.int), "awayScore"),
    PropertySpec("espnPlayId", "Event", str(XSD.string), "espnPlayId"),
    PropertySpec("espnSequenceNumber", "Event", str(XSD.string), "espnSequenceNumber"),
    PropertySpec("isScoringPlay", "Event", str(XSD.boolean), "isScoringPlay"),
    PropertySpec("isShootingPlay", "Event", str(XSD.boolean), "isShootingPlay"),
    PropertySpec("wallclockTime", "Event", str(XSD.dateTime), "wallclockTime"),
    # Team/player ids
    PropertySpec("espnTeamId", "Team", str(XSD.string), "espnTeamId"),
    PropertySpec("espnAthleteId", "Player", str(XSD.string), "espnAthleteId"),
    # Play type + coordinate
    PropertySpec("hasPlayType", "Play", "PlayType", "hasPlayType", object_property=True),
    PropertySpec("playTypeId", "PlayType", str(XSD.string), "playTypeId"),
    PropertySpec("playTypeText", "PlayType", str(XSD.string), "playTypeText"),
    PropertySpec("shortDescription", "Play", str(XSD.string), "shortDescription"),
    PropertySpec("hasCoordinate", "Play", "CourtCoordinate", "hasCoordinate", object_property=True),
    PropertySpec("coordinateX", "CourtCoordinate", str(XSD.int), "coordinateX"),
    PropertySpec("coordinateY", "CourtCoordinate", str(XSD.int), "coordinateY"),
    PropertySpec("pointsAttempted", "Play", str(XSD.int), "pointsAttempted"),
    PropertySpec("scoreValue", "Play", str(XSD.int), "scoreValue"),
    # Player involvement
    PropertySpec("involvedPlayer", "Play", "Player", "involvedPlayer", object_property=True),
    PropertySpec("shotBy", "Shot", "Player", "shotBy", object_property=True, subproperty_of="involvedPlayer"),
    PropertySpec(
        "shotAssistedBy", "Shot", "Player", "shotAssistedBy", object_property=True, subproperty_of="involvedPlayer"
    ),
    PropertySpec(
        "shotBlockedBy", "Block", "Player", "shotBlockedBy", object_property=True, subproperty_of="involvedPlayer"
    ),
    PropertySpec(
        "foulCommittedBy", "Foul", "Player", "foulCommittedBy", object_property=True, subproperty_of="involvedPlayer"
    ),
    PropertySpec("foulDrawnBy", "Foul", "Player", "foulDrawnBy", object_property=True, subproperty_of="involvedPlayer"),
    PropertySpec("reboundedBy", "Rebound", "Player", "reboundedBy", object_property=True, subproperty_of="involvedPlayer"),
    PropertySpec("turnedOverBy", "Turnover", "Player", "turnedOverBy", object_property=True, subproperty_of="involvedPlayer"),
    PropertySpec("stolenBy", "Turnover", "Player", "stolenBy", object_property=True, subproperty_of="involvedPlayer"),
    PropertySpec("playerEntering", "Enters", "Player", "playerEntering", object_property=True, subproperty_of="involvedPlayer"),
    PropertySpec("playerExiting", "Enters", "Player", "playerExiting", object_property=True, subproperty_of="involvedPlayer"),
    PropertySpec("playerEjected", "Ejection", "Player", "playerEjected", object_property=True, subproperty_of="involvedPlayer"),
    PropertySpec(
        "jumpBallHomePlayer", "JumpBall", "Player", "jumpBallHomePlayer", object_property=True, subproperty_of="involvedPlayer"
    ),
    PropertySpec(
        "jumpBallAwayPlayer", "JumpBall", "Player", "jumpBallAwayPlayer", object_property=True, subproperty_of="involvedPlayer"
    ),
    PropertySpec(
        "jumpBallGainedPossession",
        "JumpBall",
        "Player",
        "jumpBallGainedPossession",
        object_property=True,
        subproperty_of="involvedPlayer",
    ),
    # Play-specific data
    PropertySpec("shotMade", "Shot", str(XSD.boolean), "shotMade"),
    PropertySpec("shotType", "Shot", str(XSD.string), "shotType"),
    PropertySpec("shotPoints", "Shot", str(XSD.int), "shotPoints"),
    PropertySpec("turnoverType", "Turnover", str(XSD.string), "turnoverType"),
    PropertySpec("timeoutDuration", "Timeout", str(XSD.string), "timeoutDuration"),
    PropertySpec("isOfficial", "Timeout", str(XSD.boolean), "isOfficial"),
    PropertySpec("isShootingFoul", "Foul", str(XSD.boolean), "isShootingFoul"),
    PropertySpec("isLooseBallFoul", "Foul", str(XSD.boolean), "isLooseBallFoul"),
    PropertySpec("isCharge", "Foul", str(XSD.boolean), "isCharge"),
    PropertySpec("isPersonalBlockingFoul", "Foul", str(XSD.boolean), "isPersonalBlockingFoul"),
    PropertySpec("isOffensive", "Foul", str(XSD.boolean), "isOffensive"),
    PropertySpec("isThreeSecond", "TechnicalFoul", str(XSD.boolean), "isThreeSecond"),
    PropertySpec("isDelayOfGame", "TechnicalFoul", str(XSD.boolean), "isDelayOfGame"),
    PropertySpec("technicalFoulNumber", "TechnicalFoul", str(XSD.int), "technicalFoulNumber"),
    # Phase 2: win probability
    PropertySpec(
        "hasWinProbabilitySnapshot",
        "Game",
        "WinProbabilitySnapshot",
        "hasWinProbabilitySnapshot",
        object_property=True,
    ),
    PropertySpec(
        "snapshotForPlay",
        "WinProbabilitySnapshot",
        "Event",
        "snapshotForPlay",
        object_property=True,
    ),
    PropertySpec("homeWinProbability", "WinProbabilitySnapshot", str(XSD.decimal), "homeWinProbability"),
    PropertySpec("tieProbability", "WinProbabilitySnapshot", str(XSD.decimal), "tieProbability"),
    # Phase 2: venue + officials
    PropertySpec("venue", "Game", "Venue", "venue", object_property=True),
    PropertySpec("venueName", "Venue", str(XSD.string), "venueName"),
    PropertySpec("venueId", "Venue", str(XSD.string), "venueId"),
    PropertySpec("venueAddress", "Venue", str(XSD.string), "venueAddress"),
    PropertySpec("attendance", "Game", str(XSD.int), "attendance"),
    PropertySpec("hasOfficial", "Game", "Official", "hasOfficial", object_property=True),
    PropertySpec("officialName", "Official", str(XSD.string), "officialName"),
    PropertySpec("officialPosition", "Official", str(XSD.string), "officialPosition"),
]


def _class_iri(name: str) -> URIRef:
    return PBPRDF[name]


def _range_iri(range_name_or_iri: str) -> URIRef:
    if range_name_or_iri.startswith("http://"):
        return URIRef(range_name_or_iri)
    return _class_iri(range_name_or_iri)


def generate_ontology() -> Graph:
    graph = Graph()
    graph.bind("pbprdf", PBPRDF)
    graph.bind("owl", OWL)
    graph.bind("rdfs", RDFS)
    graph.bind("xsd", XSD)

    for class_name in CLASS_NAMES:
        class_iri = _class_iri(class_name)
        graph.add((class_iri, RDF.type, OWL.Class))
        graph.add((class_iri, RDFS.label, Literal(class_name)))

    graph.add((PBPRDF.Block, RDFS.subClassOf, PBPRDF.Shot))
    graph.add((PBPRDF.Play, RDFS.subClassOf, PBPRDF.Event))

    for spec in PROPERTY_SPECS:
        prop = PBPRDF[spec.name]
        graph.add((prop, RDF.type, OWL.ObjectProperty if spec.object_property else OWL.DatatypeProperty))
        graph.add((prop, RDFS.label, Literal(spec.label)))
        graph.add((prop, RDFS.domain, _class_iri(spec.domain)))
        graph.add((prop, RDFS.range, _range_iri(spec.range)))
        if spec.subproperty_of:
            graph.add((prop, RDFS.subPropertyOf, PBPRDF[spec.subproperty_of]))

    return graph

