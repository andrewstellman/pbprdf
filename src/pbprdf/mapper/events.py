from __future__ import annotations

from rdflib import Literal

from pbprdf.mapper.plays import EventRecord
from pbprdf.ontology import PBPRDF


def add_event_chain(graph, records: list[EventRecord]) -> None:
    ordered = sorted(records, key=lambda r: r.sequence_value)
    for index, record in enumerate(ordered):
        graph.add((record.event_iri, PBPRDF.eventNumber, Literal(index + 1)))

        prev = ordered[index - 1] if index > 0 else None
        nxt = ordered[index + 1] if index + 1 < len(ordered) else None

        if prev is not None:
            graph.add((record.event_iri, PBPRDF.previousEvent, prev.event_iri))
            if (
                prev.period == record.period
                and prev.seconds_left_in_period is not None
                and record.seconds_left_in_period is not None
            ):
                graph.add(
                    (
                        record.event_iri,
                        PBPRDF.secondsSincePreviousEvent,
                        Literal(prev.seconds_left_in_period - record.seconds_left_in_period),
                    )
                )

        if nxt is not None:
            graph.add((record.event_iri, PBPRDF.nextEvent, nxt.event_iri))
            if (
                nxt.period == record.period
                and nxt.seconds_left_in_period is not None
                and record.seconds_left_in_period is not None
            ):
                graph.add(
                    (
                        record.event_iri,
                        PBPRDF.secondsUntilNextEvent,
                        Literal(record.seconds_left_in_period - nxt.seconds_left_in_period),
                    )
                )

