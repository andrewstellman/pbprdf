from __future__ import annotations

from pathlib import Path

LEAGUE_SLUGS: dict[str, str] = {
    "nba": "nba",
    "wnba": "wnba",
    "ncaam": "mens-college-basketball",
    "ncaaw": "womens-college-basketball",
}

USER_AGENT = "pbprdf-v2/0.1"
SUMMARY_URL = "https://site.api.espn.com/apis/site/v2/sports/basketball/{slug}/summary"
SCOREBOARD_URL = "https://site.api.espn.com/apis/site/v2/sports/basketball/{slug}/scoreboard"

PROJECT_ROOT = Path(__file__).resolve().parents[2]
DATA_RAW_DIR = PROJECT_ROOT / "data" / "raw"

