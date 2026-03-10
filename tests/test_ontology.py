from __future__ import annotations

from rdflib.namespace import RDF, RDFS, OWL

from pbprdf.ontology import PBPRDF, generate_ontology


def test_ontology_classes_present():
    graph = generate_ontology()
    for cls in [
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
    ]:
        assert (PBPRDF[cls], RDF.type, OWL.Class) in graph


def test_involved_player_superproperty_and_subproperties():
    graph = generate_ontology()
    assert (PBPRDF.involvedPlayer, RDF.type, OWL.ObjectProperty) in graph
    for prop in [
        PBPRDF.shotBy,
        PBPRDF.shotAssistedBy,
        PBPRDF.shotBlockedBy,
        PBPRDF.foulCommittedBy,
        PBPRDF.foulDrawnBy,
        PBPRDF.reboundedBy,
        PBPRDF.turnedOverBy,
        PBPRDF.stolenBy,
        PBPRDF.playerEntering,
        PBPRDF.playerExiting,
        PBPRDF.playerEjected,
    ]:
        assert (prop, RDFS.subPropertyOf, PBPRDF.involvedPlayer) in graph


def test_block_is_subclass_of_shot():
    graph = generate_ontology()
    assert (PBPRDF.Block, RDFS.subClassOf, PBPRDF.Shot) in graph


def test_venue_official_winprob_classes_exist():
    graph = generate_ontology()
    assert (PBPRDF.Venue, RDF.type, OWL.Class) in graph
    assert (PBPRDF.Official, RDF.type, OWL.Class) in graph
    assert (PBPRDF.WinProbabilitySnapshot, RDF.type, OWL.Class) in graph


def test_venue_properties():
    graph = generate_ontology()
    assert (PBPRDF.venueName, RDF.type, OWL.DatatypeProperty) in graph
    assert (PBPRDF.venueId, RDF.type, OWL.DatatypeProperty) in graph
    assert (PBPRDF.venue, RDF.type, OWL.ObjectProperty) in graph
    assert (PBPRDF.hasOfficial, RDF.type, OWL.ObjectProperty) in graph


def test_win_probability_properties():
    graph = generate_ontology()
    assert (PBPRDF.homeWinProbability, RDF.type, OWL.DatatypeProperty) in graph
    assert (PBPRDF.tieProbability, RDF.type, OWL.DatatypeProperty) in graph
    assert (PBPRDF.snapshotForPlay, RDF.type, OWL.ObjectProperty) in graph
    assert (PBPRDF.hasWinProbabilitySnapshot, RDF.type, OWL.ObjectProperty) in graph

