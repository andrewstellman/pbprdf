from __future__ import annotations

import json
import sys
from pathlib import Path

import pytest

PROJECT_ROOT = Path(__file__).resolve().parents[1]
SRC = PROJECT_ROOT / "src"
if str(SRC) not in sys.path:
    sys.path.insert(0, str(SRC))

from pbprdf.mapper.core import map_game_json  # noqa: E402
from pbprdf.models.espn import SummaryResponse  # noqa: E402


def _fixture_path(prefix: str) -> Path:
    fixtures_dir = PROJECT_ROOT / "tests" / "fixtures"
    matches = sorted(fixtures_dir.glob(f"{prefix}_*.json"))
    if not matches:
        raise RuntimeError(f"No fixture found for {prefix} in {fixtures_dir}")
    return matches[0]


@pytest.fixture(scope="session")
def nba_path() -> Path:
    return _fixture_path("nba")


@pytest.fixture(scope="session")
def wnba_path() -> Path:
    return _fixture_path("wnba")


@pytest.fixture(scope="session")
def ncaam_path() -> Path:
    return _fixture_path("ncaam")


@pytest.fixture(scope="session")
def ncaaw_path() -> Path:
    return _fixture_path("ncaaw")


@pytest.fixture(scope="session")
def nba_raw(nba_path: Path) -> dict:
    return json.loads(nba_path.read_text())


@pytest.fixture(scope="session")
def wnba_raw(wnba_path: Path) -> dict:
    return json.loads(wnba_path.read_text())


@pytest.fixture(scope="session")
def ncaam_raw(ncaam_path: Path) -> dict:
    return json.loads(ncaam_path.read_text())


@pytest.fixture(scope="session")
def ncaaw_raw(ncaaw_path: Path) -> dict:
    return json.loads(ncaaw_path.read_text())


@pytest.fixture(scope="session")
def nba_summary(nba_raw: dict) -> SummaryResponse:
    return SummaryResponse.model_validate(nba_raw)


@pytest.fixture(scope="session")
def wnba_summary(wnba_raw: dict) -> SummaryResponse:
    return SummaryResponse.model_validate(wnba_raw)


@pytest.fixture(scope="session")
def ncaam_summary(ncaam_raw: dict) -> SummaryResponse:
    return SummaryResponse.model_validate(ncaam_raw)


@pytest.fixture(scope="session")
def ncaaw_summary(ncaaw_raw: dict) -> SummaryResponse:
    return SummaryResponse.model_validate(ncaaw_raw)


@pytest.fixture(scope="session")
def nba_graph(nba_raw: dict):
    return map_game_json(nba_raw)


@pytest.fixture(scope="session")
def wnba_graph(wnba_raw: dict):
    return map_game_json(wnba_raw)


@pytest.fixture(scope="session")
def ncaam_graph(ncaam_raw: dict):
    return map_game_json(ncaam_raw)


@pytest.fixture(scope="session")
def ncaaw_graph(ncaaw_raw: dict):
    return map_game_json(ncaaw_raw)

