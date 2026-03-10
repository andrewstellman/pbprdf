# Quality Playbook: AI-Driven Quality Infrastructure for Any Codebase

> **Audience:** An AI coding assistant (Claude Code, Cursor, Copilot, etc.) with access to a project folder.
> **Purpose:** Read this document, explore the codebase, and generate a complete quality infrastructure tailored to this specific project.
> **Time:** ~60–90 minutes of AI agent work.

---

## How to Use This Playbook

This playbook is self-contained. Point your AI assistant at it and tell it what to do:

```
Read @QUALITY_PLAYBOOK.md and generate the quality infrastructure.
```

```
Read @QUALITY_PLAYBOOK.md and update the functional tests.
```

```
Read @QUALITY_PLAYBOOK.md and execute the integration tests.
```

The playbook will guide you through exploring the codebase, finding specifications, and generating all deliverables. You do not need to provide additional context — the playbook tells you how to discover everything you need from the project folder.

**If the quality infrastructure already exists** (i.e., `tests/QUALITY.md`, `tests/test_functional.py`, etc. are already present), do not regenerate from scratch. Instead, read the existing files, understand what's there, and update or extend them based on the current state of the codebase and specs.

---

## Terminology

These definitions are used consistently throughout this playbook:

- **Functional testing** — Verify the product meets its functional requirements. Given specific input, does the code produce the output the specs say it should? Functional tests are automated and executable.
- **Regression testing** — Verify that previously working functionality still works after changes. Regression tests lock in known-correct behavior so that future changes don't silently break it. Every functional test becomes a regression test once it passes.
- **Integration testing** — Verify that components work together, including end-to-end flows across module boundaries. Integration tests exercise the real pipeline, often with real or realistic data, and may involve external systems (databases, APIs, triplestores, etc.).
- **Spec audit** — A static analysis where AI models read the code and compare it against written specifications, looking for divergence, missing features, undocumented behavior, and phantom specs. No code is executed. Valuable but distinct from testing.
- **Acceptance testing** — Verify the system works for the end user's actual use case, under realistic conditions. Often manual or semi-automated.

The key distinction: *testing* executes code and checks results. A *spec audit* reads code and checks alignment with documentation. Both are valuable. This playbook generates infrastructure for both.

---

## What You're Building

You will create six files that together form a repeatable quality system, plus automated test code:

| File | What It Is | Executes Code? |
|------|-----------|----------------|
| `tests/QUALITY.md` | Quality constitution — coverage targets, fitness-to-purpose scenarios, testing discipline | No |
| `tests/test_functional.py` | Automated functional tests derived from specifications | **Yes** |
| `tests/RUN_CODE_REVIEW.md` | Code review protocol with guardrails that prevent hallucinated findings | No |
| `tests/RUN_INTEGRATION_TESTS.md` | Integration test protocol — end-to-end pipeline across all variants | **Yes** |
| `tests/RUN_SPEC_AUDIT.md` | Council of Three multi-model spec audit protocol | No |
| `AGENTS.md` | Bootstrap context for any AI session working on this project | No |

You will also create output directories:
- `tests/code_reviews/`
- `tests/spec_audits/`
- `tests/results/`

**The critical deliverable is `test_functional.py`.** The Markdown protocols are documentation for humans and AI agents. The functional tests are the automated safety net that runs every time.

---

## Before You Start Writing: Explore the Codebase

**Do not write any files yet.** Spend the first phase understanding the project. The quality infrastructure must be grounded in this specific codebase — not generic advice.

### Step 1: Identify the Project's Domain, Stack, and Specifications

Read the README, any existing documentation, and `pyproject.toml` / `package.json` / `Cargo.toml` / equivalent. Answer:

- What does this project do? (One sentence.)
- What language and key dependencies does it use?
- What are the external systems it talks to? (APIs, databases, file formats, etc.)
- What is the project's primary output? (A file, a service response, a UI, transformed data, etc.)

**Find the specifications.** Specifications (intent documents, architecture docs, API specs, design decision records) are the source of truth for functional tests. Search for them in this order:

1. `AGENTS.md` or `CLAUDE.md` in the project root — these often list spec locations
2. `specs/` directory
3. `docs/` directory
4. `spec/`, `design/`, `architecture/`, or `adr/` directories
5. Any `.md` files in the project root that look like specifications (not README, not CHANGELOG)
6. Comments or docstrings in the code that reference spec documents

If you cannot find any specification documents, **ask the user** where the intent specifications live. Do not guess. Do not proceed to generate functional tests without specs — the whole point is that tests are derived from requirements, and requirements live in specs.

Record the spec file paths — you will need them in Step 4 and when generating `test_functional.py` and `RUN_SPEC_AUDIT.md`.

### Step 2: Map the Architecture

List all source directories and their purposes. Read the main entry point and trace the execution flow. Answer:

- What are the 3–5 major subsystems or modules?
- What is the data flow? (Input → Processing → Output)
- Which module is the most complex? (Most lines, most branching logic, most edge cases.)
- Which module is the most fragile? (Depends on external data, uses regex, parses unstructured input.)

### Step 3: Read the Existing Tests

Read every test file. Answer:

- How many tests exist? What do they cover?
- What testing patterns are used? (Fixtures, mocks, parametrized tests, integration tests.)
- What's NOT tested? (Identify gaps.)
- Are tests meaningful or are some "coverage theater"? (See definition below.)

### Step 4: Read the Specifications

Read every specification file you discovered in Step 1. These are the source of truth for functional tests. Answer:

- What does the spec say each module should produce?
- What specific properties, formats, or values does the spec guarantee?
- What edge cases or variants does the spec explicitly address?
- Are there requirements in the spec that no existing test checks?

That last question is the most important. Every spec requirement without a corresponding test is a gap that `test_functional.py` must close.

### Step 5: Find the Skeletons — Real Bugs and Design Decisions

This is the most important step. Search for evidence of:

- **Bugs that were fixed** — Look at git history, TODO comments, workarounds, defensive code that checks for things that "shouldn't happen."
- **Edge cases handled** — Defensive `if None` checks, try/except blocks, data normalization functions. Each one is evidence of a past failure.
- **Design decisions** — Comments explaining "why" not just "what." Configuration that could have been hardcoded but isn't. Abstractions that exist for a reason.
- **External data quirks** — Any place the code normalizes, validates, or rejects input from an external system.

These become your fitness-to-purpose scenarios. Every normalization function, every bounds check, every "skip if None" guard is a scar from a real problem. Document them.

### Step 6: Identify the Project's Specific Quality Risks

Every project has a different failure profile. Ask:

- **What does "silently wrong" look like?** For a data pipeline, it's correct-looking output with wrong values. For a web app, it's a page that renders but shows stale data. For a compiler, it's valid output that behaves differently than the source intended.
- **What external dependencies can change without warning?** Undocumented APIs, third-party schemas, file formats.
- **What looks simple but is actually complex?** Text parsing, date/time logic, coordinate systems, encoding, floating-point comparison.
- **Where do cross-cutting concerns hide?** A change in one module silently breaks another.

---

## File 1: `tests/QUALITY.md` — Quality Constitution

This is the foundational document. It defines what "quality" means for this project and makes the bar explicit, persistent, and inherited by every AI session.

### Structure

#### Section 1: Purpose (2–3 paragraphs)

State what quality means for this project. Ground it in three principles:

- **Deming** ("quality is built in, not inspected in") — Quality is built into context files and test infrastructure so every AI session inherits the same bar.
- **Juran** ("fitness for use") — Define fitness specifically for this project. Not "tests pass" but the actual real-world requirement. Example: "generates correct output that survives input schema changes without silently producing wrong results."
- **Crosby** ("quality is free") — Building quality infrastructure upfront costs less than debugging problems found after deployment.

#### Section 2: Coverage Targets

Create a table mapping each subsystem to a coverage target and rationale:

```markdown
| Subsystem | Target | Why |
|-----------|--------|-----|
| [Most fragile module] | 90–95% | [Explain why — references a real edge case or past bug] |
| [Core logic module] | 85–90% | [Explain] |
| [I/O or integration layer] | 80% | [Explain] |
| [Configuration/utilities] | 75–80% | [Explain] |
```

The rationale column is essential. It must reference specific risks or past failures, not just "it's important." If you can't explain why a subsystem needs high coverage with a concrete example, the target is arbitrary.

#### Section 3: Coverage Theater Prevention

Define what constitutes a fake test for this project. Generic examples:

- Asserting a function returned *something* without checking what
- Testing with synthetic data that lacks the quirks of real data
- Asserting an import succeeded
- Asserting mock returns what the mock was configured to return
- Calling a function and only asserting no exception was thrown

Add project-specific examples based on what you learned in Step 3. For a data transformation project, this might be "counting output records without checking their values." For a web app, "checking HTTP 200 without checking the response body."

#### Section 4: Fitness-to-Purpose Scenarios

This is the heart of the quality constitution. Write 5–10 scenarios, each following this template:

```markdown
### Scenario N: [Memorable Name]

**What happened:** [Describe the real failure, edge case, or design decision. Reference actual code — function names, file names, line numbers where the fix lives.]

**The requirement:** [What the code must do to prevent this failure. Be specific enough that an AI can verify it.]

**How to verify:** [Concrete test or query that would fail if this scenario regressed. Include exact commands, queries, or test names.]
```

**Where scenarios come from:**

1. **Defensive code** — Every `if value is None: return` guard is a scenario. Why was it needed?
2. **Normalization functions** — Every function that cleans or transforms input data exists because raw input caused problems. What problems?
3. **Configuration that could be hardcoded** — If the code reads a value from config/response instead of hardcoding it, someone learned the hard way that the value varies.
4. **Git blame / commit messages** — "Fix crash when X is missing" → Scenario: X can be missing.
5. **Comments explaining "why"** — "We use hash(id) not sequential index because..." → Scenario about correctness under that constraint.

If you find fewer than 5 scenarios, you haven't looked hard enough. Every non-trivial codebase has at least 5 places where the code handles unexpected input or makes a non-obvious design choice.

**Critical:** Each scenario's "How to verify" section must map to at least one automated test in `test_functional.py`. If a scenario can't be verified by an automated test, note why (it may require the Human Gate) — but most scenarios should be testable.

#### Section 5: AI Session Quality Discipline

Rules for any AI session working on this project:

1. Read QUALITY.md before starting work.
2. Run the full test suite before marking any task complete.
3. Add tests for new functionality (not just happy path — include edge cases from the scenarios).
4. Update this file if new failure modes are discovered.
5. Output a Quality Compliance Checklist before ending a session: which tests ran, which scenarios are affected, which context files were updated.
6. Never remove a fitness-to-purpose scenario. Only add new ones.

#### Section 6: The Human Gate

List things that require human judgment. These vary by project but typically include:

- Output that "looks right" (requires domain knowledge to verify)
- UX and responsiveness
- Documentation accuracy
- Security review of authentication/authorization changes
- Decisions about backward compatibility

---

## File 2: `tests/test_functional.py` — Automated Functional and Regression Tests

**This is the most important deliverable.** The Markdown files are documentation. This file is the safety net.

### What Goes In This File

Functional tests derived directly from the specifications. Each test should be traceable to a specific requirement in the spec or a fitness-to-purpose scenario in QUALITY.md.

### How to Write Spec-Derived Tests

For each requirement you found in Step 4 ("Read the Specifications"), write a test that:

1. **Sets up** the input (load a fixture, create test data, configure the system)
2. **Executes** the code under test (call the function, run the pipeline, make the request)
3. **Asserts specific properties** of the output that the spec requires

Example structure:

```python
class TestSpecRequirements:
    """Tests derived from spec requirements.

    Each test references the specific spec section it verifies.
    """

    def test_requirement_from_spec_section_N(self, fixture):
        """Spec: V2_ARCHITECTURE_SPEC.md Section N says X should produce Y."""
        result = process(fixture)
        # Assert the specific property the spec guarantees
        assert result.property == expected_value
```

### What Makes a Good Functional Test

- **Traceable** — The docstring or comment says which spec requirement it verifies
- **Specific** — It checks a specific property, not just "something happened"
- **Robust** — It uses real data (fixtures from the actual system), not synthetic data
- **Cross-variant** — If the project handles multiple input types, test all of them
- **Regression-ready** — Once this test passes, any future change that breaks it is a regression

### Fitness-to-Purpose Scenario Tests

For each scenario in QUALITY.md that can be automated, write a test:

```python
class TestFitnessScenarios:
    """Tests for fitness-to-purpose scenarios from QUALITY.md.

    Each test prevents a known failure mode from recurring.
    """

    def test_scenario_1_memorable_name(self, fixture):
        """QUALITY.md Scenario 1: [Name].
        Requirement: [What the code must do].
        """
        # Execute the code path that triggered the original failure
        result = process(fixture)
        # Assert the fix is still in place
        assert condition_that_prevents_the_failure
```

### Running the Tests

These tests should run as part of the normal test suite:

```bash
# Run all tests including functional tests
pytest tests/ -v

# Run only functional tests
pytest tests/test_functional.py -v
```

---

## File 3: `tests/RUN_CODE_REVIEW.md` — Code Review Protocol

### Structure

#### Bootstrap Section
List 3–5 files the reviewer must read first for context. Always include QUALITY.md and the main architectural docs.

#### "What to Check" Section
Define 4–6 review focus areas specific to this project. Each should map to a subsystem or risk area identified in Step 2 and Step 6. For each focus area, provide:
- **Where:** Which files and functions
- **What:** Specific things to look for
- **Why:** What goes wrong if this is incorrect

#### Guardrails Section (Use These Exactly)

These four guardrails are proven to dramatically improve AI code review quality. In one project, they tripled the defect detection rate of the weakest reviewer:

```markdown
## Guardrails

- **Line numbers are mandatory.** If you cannot cite a specific line, do not include the finding.
- **Read function bodies, not just signatures.** Don't assume a function works correctly based on its name.
- **If unsure whether something is a bug or intentional**, flag it as a QUESTION rather than a BUG.
- **Grep before claiming missing.** If you think a feature is absent, search the codebase. If found in a different file, that's a location defect, not a missing feature.
- **Do NOT suggest style changes, refactors, or improvements.** Only flag things that are incorrect or could cause failures.
```

#### Output Format Section
Specify the output file naming convention and the format for findings:

```markdown
### filename.py
- **Line NNN:** [BUG / QUESTION / INCOMPLETE] Description. Expected vs. actual. Why it matters.
```

End with a summary: total by severity, files with no findings, overall assessment (SHIP IT / FIX FIRST / NEEDS DISCUSSION).

---

## File 4: `tests/RUN_INTEGRATION_TESTS.md` — Integration Test Protocol

Integration tests verify that components work together end-to-end. Unlike the functional tests in `test_functional.py` (which test individual requirements), integration tests exercise the full pipeline with real data across all variants.

### Structure

#### Safety Constraints
If this protocol will be run with elevated permissions (e.g., `--dangerously-skip-permissions`), define absolute constraints:
- DO NOT modify source code
- DO NOT delete files
- ONLY create files in the test results directory
- If something fails, record it and move on — DO NOT fix it

#### Pre-Flight Check
What must be verified before tests run? API keys? External services? Test fixtures? Dependencies installed?

#### Test Matrix
Create a table of specific checks with pass criteria. Each row should be:

```markdown
| Check | Method | Pass Criteria |
|-------|--------|---------------|
| [What you're verifying] | [How to verify — command, query, or assertion] | [Specific expected result] |
```

Design checks that exercise the full pipeline end-to-end. Include:
- **Happy path** — Does the primary flow work from input to output?
- **Cross-variant consistency** — If the project handles multiple input types/formats/configurations, does each variant produce correct output?
- **Output correctness** — Don't just check "output exists" — verify specific properties of the output.
- **Component boundaries** — Does the output of module A correctly feed into module B?

Where possible, encode integration checks as automated tests in a `test_integration.py` file so they can run with `pytest`. The Markdown protocol should describe both the automated tests and any manual verification steps that require external systems.

#### Reporting Format
Define a structured report format so results are comparable across runs. Include a summary table and a place for detailed findings.

---

## File 5: `tests/RUN_SPEC_AUDIT.md` — Council of Three Spec Audit Protocol

This is a static analysis protocol — AI models read the code and compare it to specifications. No code is executed. It catches a different class of problem than testing: spec-code divergence, undocumented features, phantom specs, and missing implementations.

**Important:** This is not a substitute for functional or regression testing. It complements testing by finding gaps that tests don't cover yet. Findings from a spec audit often become new functional tests.

### Why Three Models?

In practice, 74% of defects are found by only one of three auditors. AI models have different blind spots — they're confident about different things and miss different things. Cross-referencing three independent reviews catches defects that any single model would miss.

### Structure

#### The Definitive Audit Prompt

Write a prompt that will be given identically to all three AI tools. It must include:

1. **Context files to read** — List the intent specs (architectural docs, API specs, design decisions).

2. **The task** — "Act as the Tester. Read the actual code and strictly compare it against these specs."

3. **Rules with the four guardrails:**
```
- ONLY list defects. Do not summarize what matches.
- For EVERY defect, cite specific file and line number(s).
  If you cannot cite a line number, do not include the finding.
- Before claiming missing, grep the codebase.
- Before claiming exists, read the actual function body.
- Classify: MISSING / DIVERGENT / UNDOCUMENTED / PHANTOM
```

4. **Project-specific scrutiny areas** — 5–10 specific questions that force the auditor to read the most critical code. These should target:
   - The most fragile module (text parsing, data transformation, etc.)
   - External data handling (validation, normalization, error recovery)
   - Assumptions that might not hold (field presence, value ranges, format consistency)
   - Features that cross module boundaries
   - The gap between documentation and implementation

#### Triage Process

After all three models report, merge findings:
- 🟢 Found by all three → highest confidence
- 🟡 Found by two → high confidence
- 🔴 Found by one → needs verification

Categorize each finding:
- **Spec bug** — Spec is wrong, code is fine → update spec
- **Design decision** — Human judgment needed → discuss and decide
- **Real code bug** — Fix in small batches by subsystem
- **Documentation gap** — Feature exists but undocumented → update docs
- **Missing test** — Code is correct but no test verifies it → add to `test_functional.py`

That last category is the bridge between the spec audit and the test suite. Every confirmed finding that isn't already covered by a test should become one.

#### The Verification Probe

When models disagree on factual claims: deploy a read-only probe (give one model the disputed claim and ask it to read the code and report ground truth). Never resolve factual disputes by majority vote.

#### Fix Execution Rules

- Group fixes by subsystem, not by defect number
- Never one mega-prompt for all fixes
- Each batch: implement, test, have all three reviewers verify the diff
- At least two auditors must confirm fixes pass before marking complete

---

## File 6: `AGENTS.md` — AI Agent Bootstrap

This is the shortest file but arguably the most-read. Every AI session starts here.

### Structure

#### Paragraph 1: What Is This Project? (3–4 sentences)
What it does, what language/stack, what its primary input/output is.

#### Setup Section
Clone, install, configure — the minimum commands to get from zero to runnable.

#### Build & Test Section
```bash
# Run all tests (functional + regression)
[test command]

# Run with coverage
[coverage command]

# Quick smoke test
[command that exercises the primary flow]
```

#### Architecture Section
The data flow in one diagram or description. List each module with a one-line purpose.

#### Key Design Decisions (Bulleted)
5–7 non-obvious decisions. These are the things an AI would "fix" if it didn't know the context. Example: "We use hash(id) not sequential index for seeding because sequential seeds create correlated random streams."

#### Known Quirks / Gotchas (Bulleted)
Things from external systems that surprised you. Example: "The API returns sentinel values (-2147483647) instead of null for missing coordinates."

#### Quality Docs Pointer
Tell the agent where to find:
- QUALITY.md (quality constitution and fitness-to-purpose scenarios)
- test_functional.py (automated functional tests derived from specs)
- Code review protocol
- Integration test protocol
- Spec audit protocol
- Specs / intent documents

---

## After Creating All Files: Verification

### Verify the Documentation

1. **QUALITY.md scenarios reference real code.** Every scenario should mention actual function names, file names, or patterns that exist in the codebase. Grep for them.

2. **RUN_CODE_REVIEW.md is self-contained.** An AI with no prior context should be able to read it and perform a useful review.

3. **RUN_INTEGRATION_TESTS.md is executable.** Every command in it should work. Every check should have a concrete pass/fail criterion, not "verify it looks right."

4. **RUN_SPEC_AUDIT.md prompt is copy-pasteable.** The definitive audit prompt should work when pasted into Claude Code, Cursor, and Copilot without modification (except file reference syntax).

5. **AGENTS.md setup instructions work.** From a clean clone, following the setup section should result in passing tests.

### Verify the Tests

6. **All existing tests still pass.** Your new files should not break anything.

7. **`test_functional.py` passes.** Run it: `pytest tests/test_functional.py -v`. Every test should pass.

8. **Every QUALITY.md scenario has a test.** For each fitness-to-purpose scenario, there should be at least one test in `test_functional.py` that would fail if the scenario regressed. If a scenario can't be automated, it should be explicitly marked as requiring the Human Gate.

9. **Every spec requirement has a test.** Review the specs and verify that key requirements are covered. 100% spec-to-test traceability isn't always practical, but high-risk requirements (identified in Step 6) must have tests.

10. **Tests are not theater.** For each test, ask: "If I deleted the function body being tested, would this test fail?" If the answer is no, rewrite the test.

---

## How to Run Each Protocol After Creation

### Automated Tests (Functional + Regression)
```bash
pytest tests/ -v
```
Run every time. This is your automated safety net. Once a functional test passes, it becomes a regression test — any future change that breaks it is a regression.

### Code Review (Static)
```
Read tests/RUN_CODE_REVIEW.md and perform a code review.
```
Give this to 2+ AI tools independently. Cross-reference findings. Run before releases and after significant changes.

### Integration Tests (Automated + Manual)
```
Read tests/RUN_INTEGRATION_TESTS.md and run the full integration test suite.
```
Exercises the full pipeline end-to-end. Run before releases.

### Spec Audit — Council of Three (Static)
```
Read tests/RUN_SPEC_AUDIT.md and perform a spec audit.
```
Give to 3 independent AI tools. Merge findings. Triage. Convert confirmed findings into new functional tests. Run before major releases or after architectural changes. Budget 2+ hours.

### The Workflow for a Typical Release

1. **Run automated tests** — `pytest tests/ -v` (~1 min)
2. **Code review** — 2+ AI tools review recent changes (~10 min)
3. **Fix** — Write a fix spec, implement in batches, verify
4. **Integration test** — Full pipeline tests (~15 min)
5. **Run automated tests again** — Confirm fixes didn't break anything
6. **Ship**

For major architectural changes, add a spec audit (Council of Three) before step 2. Budget 2+ hours. Convert findings into new tests before shipping.

---

## Principles This Playbook Is Built On

1. **Quality is built into context, not inspected after the fact.** These files ensure every AI session inherits the same quality bar.

2. **No single AI model is sufficient.** Three models catch 3x more defects than one. 74% of defects are found by only one auditor.

3. **Fitness-to-purpose over code coverage.** Coverage percentages are a proxy. The real question is: "Does this code do what it's supposed to do under real-world conditions?" Scenarios answer that question; coverage numbers don't.

4. **Every defensive code pattern is a past failure.** Normalization functions, null checks, bounds validation — each one is a scar from a real problem. Document them as scenarios so the knowledge persists across sessions.

5. **Guardrails transform AI review quality.** Four simple rules (line numbers, read bodies, grep before claiming missing, classify findings) tripled the defect detection rate of the weakest reviewer in practice.

6. **Triage before fixing.** Many "defects" are actually spec bugs or design decisions. A human architect must decide which is which.

7. **Small batches, not mega-prompts.** Fix by subsystem in focused sessions. Verify each batch before starting the next.

8. **The quality ratchet only goes up.** Never remove a scenario. Only add new ones as new failure modes are discovered.

9. **Tests beat audits.** A spec audit finds problems. A functional test *prevents* problems from recurring. Every audit finding should become a test. The audit is the discovery mechanism; the test is the permanent fix.

10. **Every test was a requirement first.** Functional tests are derived from specifications. If a test can't be traced to a spec requirement or a fitness-to-purpose scenario, it may be testing an implementation detail rather than a requirement. Implementation-detail tests are brittle; requirement-derived tests are durable.
