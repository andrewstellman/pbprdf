from __future__ import annotations

from rdflib import BNode, Literal, URIRef
from rdflib.namespace import RDF, RDFS

from pbprdf.ontology import PBPRDF, PBPRDF_ENTITY


def _address_string(address: dict) -> str | None:
    city = address.get("city") if isinstance(address, dict) else None
    state = address.get("state") if isinstance(address, dict) else None
    if city and state:
        return f"{city}, {state}"
    if city:
        return str(city)
    if state:
        return str(state)
    return None


def map_venue(game_iri, game_info: dict | None, graph) -> None:
    if not isinstance(game_info, dict):
        return

    venue = game_info.get("venue")
    if isinstance(venue, dict) and venue.get("id"):
        vid = str(venue["id"])
        viri = URIRef(f"{PBPRDF_ENTITY}venues/{vid}")
        vname = venue.get("fullName")
        addr = _address_string(venue.get("address", {}))

        graph.add((viri, RDF.type, PBPRDF.Venue))
        graph.add((viri, PBPRDF.venueId, Literal(vid)))
        if vname:
            graph.add((viri, RDFS.label, Literal(str(vname))))
            graph.add((viri, PBPRDF.venueName, Literal(str(vname))))
        if addr:
            graph.add((viri, PBPRDF.venueAddress, Literal(addr)))
        graph.add((game_iri, PBPRDF.venue, viri))

        # Keep V1-compatible flat location string.
        loc_parts = [str(vname)] if vname else []
        if addr:
            loc_parts.append(addr)
        if loc_parts:
            graph.add((game_iri, PBPRDF.gameLocation, Literal(", ".join(loc_parts))))

    attendance = game_info.get("attendance")
    if attendance is not None:
        try:
            graph.add((game_iri, PBPRDF.attendance, Literal(int(attendance))))
        except (TypeError, ValueError):
            pass

    for official in game_info.get("officials", []) if isinstance(game_info.get("officials"), list) else []:
        if not isinstance(official, dict):
            continue
        node = BNode()
        graph.add((node, RDF.type, PBPRDF.Official))
        graph.add((game_iri, PBPRDF.hasOfficial, node))
        display_name = official.get("displayName")
        full_name = official.get("fullName")
        if display_name:
            graph.add((node, RDFS.label, Literal(str(display_name))))
        if full_name:
            graph.add((node, PBPRDF.officialName, Literal(str(full_name))))
        position = official.get("position", {})
        if isinstance(position, dict) and position.get("displayName"):
            graph.add((node, PBPRDF.officialPosition, Literal(str(position["displayName"]))))

