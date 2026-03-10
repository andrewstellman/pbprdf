from __future__ import annotations

from pbprdf.models.espn import SummaryResponse


def test_all_fixtures_parse(nba_raw, wnba_raw, ncaam_raw, ncaaw_raw):
    for raw in [nba_raw, wnba_raw, ncaam_raw, ncaaw_raw]:
        model = SummaryResponse.model_validate(raw)
        assert isinstance(model.plays, list)
        assert len(model.plays) > 0


def test_key_fields_extraction(nba_summary: SummaryResponse, nba_raw: dict):
    comp = nba_raw["header"]["competitions"][0]
    team_names = [c["team"]["displayName"] for c in comp["competitors"]]
    assert len(team_names) == 2
    assert nba_summary.format is not None
    assert nba_summary.format.regulation.periods in {2, 4}
    assert len(nba_summary.plays) == len(nba_raw["plays"])


def test_points_attempted_present(nba_summary: SummaryResponse):
    assert any(play.pointsAttempted is not None for play in nba_summary.plays)

