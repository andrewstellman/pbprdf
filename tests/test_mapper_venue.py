from __future__ import annotations


def test_venue_exists(nba_graph):
    results = nba_graph.query(
        """
        PREFIX pbprdf: <http://stellman-greene.com/pbprdf#>
        SELECT ?venue ?name WHERE {
            ?game a pbprdf:Game .
            ?game pbprdf:venue ?venue .
            ?venue pbprdf:venueName ?name .
        }
        """
    )
    rows = list(results)
    assert len(rows) == 1
    assert "Target Center" in str(rows[0][1])


def test_officials_exist(nba_graph):
    results = nba_graph.query(
        """
        PREFIX pbprdf: <http://stellman-greene.com/pbprdf#>
        SELECT (COUNT(?off) AS ?count) WHERE {
            ?game a pbprdf:Game .
            ?game pbprdf:hasOfficial ?off .
            ?off a pbprdf:Official .
        }
        """
    )
    count = int(list(results)[0][0])
    assert count >= 1


def test_attendance(nba_graph):
    results = nba_graph.query(
        """
        PREFIX pbprdf: <http://stellman-greene.com/pbprdf#>
        SELECT ?att WHERE {
            ?game a pbprdf:Game .
            ?game pbprdf:attendance ?att .
        }
        """
    )
    rows = list(results)
    assert len(rows) == 1
    assert int(rows[0][0]) > 0


def test_game_location_v1_compat(nba_graph):
    results = nba_graph.query(
        """
        PREFIX pbprdf: <http://stellman-greene.com/pbprdf#>
        SELECT ?loc WHERE {
            ?game a pbprdf:Game .
            ?game pbprdf:gameLocation ?loc .
        }
        """
    )
    rows = list(results)
    assert len(rows) == 1

