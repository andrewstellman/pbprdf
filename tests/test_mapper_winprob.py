from __future__ import annotations


def test_win_probability_snapshots_exist(nba_graph):
    results = nba_graph.query(
        """
        PREFIX pbprdf: <http://stellman-greene.com/pbprdf#>
        SELECT (COUNT(?snap) AS ?count) WHERE {
            ?snap a pbprdf:WinProbabilitySnapshot .
        }
        """
    )
    count = int(list(results)[0][0])
    assert count > 0


def test_win_probability_linked_to_game(nba_graph):
    results = nba_graph.query(
        """
        PREFIX pbprdf: <http://stellman-greene.com/pbprdf#>
        SELECT ?game WHERE {
            ?game pbprdf:hasWinProbabilitySnapshot ?snap .
        }
        """
    )
    games = set(str(r[0]) for r in results)
    assert len(games) == 1


def test_win_probability_linked_to_play(nba_graph):
    results = nba_graph.query(
        """
        PREFIX pbprdf: <http://stellman-greene.com/pbprdf#>
        SELECT (COUNT(?snap) AS ?count) WHERE {
            ?snap a pbprdf:WinProbabilitySnapshot .
            ?snap pbprdf:snapshotForPlay ?play .
            ?play a pbprdf:Play .
        }
        """
    )
    count = int(list(results)[0][0])
    assert count > 0


def test_win_probability_values_range(nba_graph):
    results = nba_graph.query(
        """
        PREFIX pbprdf: <http://stellman-greene.com/pbprdf#>
        SELECT ?prob WHERE {
            ?snap pbprdf:homeWinProbability ?prob .
        }
        """
    )
    for row in results:
        prob = float(row[0])
        assert 0.0 <= prob <= 1.0

