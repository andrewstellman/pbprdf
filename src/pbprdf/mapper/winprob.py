from __future__ import annotations

from decimal import Decimal

from rdflib import BNode, Literal
from rdflib.namespace import RDF

from pbprdf.models.espn import WinProbabilityEntry
from pbprdf.ontology import PBPRDF


def map_win_probability(game_iri, play_id_to_iri: dict[str, any], winprob_entries: list[WinProbabilityEntry], graph) -> None:
    for entry in winprob_entries:
        play_iri = play_id_to_iri.get(str(entry.playId))
        if play_iri is None:
            # Skip unmatched play IDs (pregame anchors can appear).
            continue

        snap = BNode()
        graph.add((snap, RDF.type, PBPRDF.WinProbabilitySnapshot))
        graph.add((snap, PBPRDF.homeWinProbability, Literal(Decimal(str(entry.homeWinPercentage)))))
        graph.add((snap, PBPRDF.tieProbability, Literal(Decimal(str(entry.tiePercentage)))))
        graph.add((snap, PBPRDF.snapshotForPlay, play_iri))
        graph.add((game_iri, PBPRDF.hasWinProbabilitySnapshot, snap))

