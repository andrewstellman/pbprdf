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

**If the quality infrastructure already exists** (i.e., `quality/QUALITY.md`, `quality/test_functional.py`, etc. are already present), read the existing files first, then **evaluate them against the self-check benchmarks** in the "Did You Go Deep Enough?" section below. If the existing tests fall short on any benchmark (test count, cross-variant coverage, boundary/negative count, layer correctness, assertion depth), add or rewrite tests until all benchmarks pass. Do not assume existing files are complete — treat them as a starting point that may need significant expansion.

---

## Terminology

These definitions are used consistently throughout this playbook:

- **Functional testing** — Verify the product meets its functional requirements. Given specific input, does the code produce the output the specs say it should? Functional tests are automated and executable.
- **Regression testing** — Verify that previously working functionality still works after changes. Regression tests lock in known-correct behavior so that future changes don't silently break it. Every functional test becomes a regression test once it passes.
- **Integration testing** — Verify that components work together, including end-to-end flows across module boundaries. Integration tests exercise the real pipeline, often with real or realistic data, and may involve external systems (databases, APIs, external services, etc.).
- **Spec audit** — A static analysis where AI models read the code and compare it against written specifications, looking for divergence, missing features, undocumented behavior, and phantom specs. No code is executed. Valuable but distinct from testing.
- **Acceptance testing** — Verify the system works for the end user's actual use case, under realistic conditions. Often manual or semi-automated.

The key distinction: *testing* executes code and checks results. A *spec audit* reads code and checks alignment with documentation. Both are valuable. This playbook generates infrastructure for both.

---

## What You're Building

You will create six files that together form a repeatable quality system, plus automated test code:

| File | What It Is | Executes Code? |
|------|-----------|----------------|
| `quality/QUALITY.md` | Quality constitution — coverage targets, fitness-to-purpose scenarios, testing discipline | No |
| `quality/test_functional.py` | Automated functional tests derived from specifications | **Yes** |
| `quality/RUN_CODE_REVIEW.md` | Code review protocol with guardrails that prevent hallucinated findings | No |
| `quality/RUN_INTEGRATION_TESTS.md` | Integration test protocol — end-to-end pipeline across all variants | **Yes** |
| `quality/RUN_SPEC_AUDIT.md` | Council of Three multi-model spec audit protocol | No |
| `AGENTS.md` | Bootstrap context for any AI session working on this project | No |

You will also create output directories:
- `quality/code_reviews/`
- `quality/spec_audits/`
- `quality/results/`

**The critical deliverable is `test_functional.py`.** The Markdown protocols are documentation for humans and AI agents. The functional tests are the automated safety net that runs every time.

**Note:** The `quality/` folder is separate from the project's unit test folder. If the test framework requires configuration to discover tests here (e.g., a `conftest.py` for pytest, a `jest.config.js` section for Jest), create it. Reuse existing test fixtures from the project's test folder rather than duplicating them.

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

This is the most important step. You are looking for defensive code patterns — each one is evidence of a past failure or a known risk. **Search systematically, not casually.**

**Grep the codebase for defensive patterns.** Use these searches (adapt to the project's language):

```bash
# Find null/None guards
grep -rn "null\|nil\|None\|undefined" src/

# Find try/catch/exception blocks
grep -rn "catch\|except\|rescue" src/

# Find normalization/sanitization functions
grep -rn "private\|internal\|def _\|function _" src/    # private helper functions are often defensive

# Find sentinel value checks
grep -rn "== 0" src/
grep -rn "< 0" src/
grep -rn "> " src/

# Find fallback/default logic
grep -rn "default" src/
grep -rn "fallback" src/
grep -rn "else:" src/
```

**For each match, ask:** "What failure does this prevent? What input would make this code path execute?" That's a fitness-to-purpose scenario.

Beyond grep, also look for:

- **Bugs that were fixed** — Look at git history, TODO comments, workarounds, defensive code that checks for things that "shouldn't happen."
- **Design decisions** — Comments explaining "why" not just "what." Configuration that could have been hardcoded but isn't. Abstractions that exist for a reason.
- **External data quirks** — Any place the code normalizes, validates, or rejects input from an external system.
- **Parsing functions** — Every parser (regex, string splitting, format detection) has failure modes. What happens with malformed input? Empty input? Unexpected types?
- **Boundary conditions** — What happens at the edges? Zero values, empty strings, maximum ranges, first/last elements, type boundaries.

**Minimum bar:** You should find at least 2–3 defensive patterns **per source file** in the core logic modules. If you're finding fewer, you're skimming — read the function bodies, not just the signatures.

### Step 5b: Map the Schema Types for Mutation-Sensitive Fields

If the project has a schema validation layer (Pydantic models, JSON Schema, TypeScript interfaces, etc.), **read the schema definitions now** — before you write any tests. For every field you found a defensive pattern for in Step 5, record:

- The field name and its schema type (e.g., `metadata: Optional[MetadataType]`)
- What values the schema accepts (e.g., `MetadataType` object, `None`)
- What values the schema rejects (e.g., `string`, `number`, `list`)

You will need this in File 2 when writing mutation-based tests. If you skip this step, you will write mutations that the schema rejects before they reach the code you're trying to test — producing validation errors instead of meaningful boundary tests.

**Example mapping:**

| Field | Schema Type | Accepts | Rejects |
|-------|-----------|---------|---------|
| `metadata` | `Optional[MetadataObject]` | `MetadataObject`, `null` | `string`, `number`, `array` |
| `count_field` | `Optional[integer]` | `integer`, `null` | `string`, `object` |
| `child_list` | `array[ChildObject]` | array of objects, `[]` | `[null, "invalid"]`, `null` |
| `optional_object` | `Optional[object]` | `{"key": value, ...}`, `null` | `"bad"`, `[1,2]` |

When you get to writing boundary/negative tests, use the "Accepts" column to choose mutation values that reach the mapper.

### Step 6: Identify the Project's Specific Quality Risks

Every project has a different failure profile. Ask:

- **What does "silently wrong" look like?** For a data pipeline, it's correct-looking output with wrong values. For a web app, it's a page that renders but shows stale data. For a compiler, it's valid output that behaves differently than the source intended.
- **What external dependencies can change without warning?** Undocumented APIs, third-party schemas, file formats.
- **What looks simple but is actually complex?** Text parsing, date/time logic, coordinate systems, encoding, floating-point comparison.
- **Where do cross-cutting concerns hide?** A change in one module silently breaks another.

---

## File 1: `quality/QUALITY.md` — Quality Constitution

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

**Minimum bar:** You should find at least **2 scenarios per core module** (the modules identified in Step 2 as most complex or most fragile). If you find fewer than 8–10 scenarios total, you haven't looked hard enough. Every non-trivial codebase has at least 8 places where the code handles unexpected input or makes a non-obvious design choice. Grep for `if.*is None`, `try/except`, and private helper functions — each one is a candidate scenario.

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

## File 2: `quality/test_functional.py` — Automated Functional and Regression Tests

**This is the most important deliverable.** The Markdown files are documentation. This file is the safety net.

### What Goes In This File

Functional tests derived directly from the specifications. Each test should be traceable to a specific requirement in the spec or a fitness-to-purpose scenario in QUALITY.md.

### Minimum Thresholds

**Do not stop at 5–10 tests.** Calculate your minimum test count using this formula:

**Your minimum test count = (testable spec sections) + (QUALITY.md scenarios) + (defensive patterns from Step 5)**

For example: 12 spec sections + 10 scenarios + 15 defensive patterns found = 37 tests minimum.

For a medium-sized project (5–15 source files), this typically yields **35–50 functional tests.** If you've written fewer than your formula minimum, you've almost certainly missed spec requirements or skimmed the defensive code patterns. Count the defensive code patterns you found in Step 5 — each one should have at least one dedicated test.

### How to Write Spec-Derived Tests

**Walk each spec document section by section.** For each section, ask: "What testable requirement does this section state?" Then write a test. Don't cherry-pick — be systematic.

For each requirement, write a test that:

1. **Sets up** the input (load a fixture, create test data, configure the system)
2. **Executes** the code under test (call the function, run the pipeline, make the request)
3. **Asserts specific properties** of the output that the spec requires

Example structure:

```
# Pseudocode — adapt to your test framework (pytest, Jest, JUnit, etc.)

TestSpecRequirements:
    test_requirement_from_spec_section_N(fixture):
        """Spec: [Document] Section N says X should produce Y."""
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
- **Tests at the right layer** — Test the *behavior* you care about, not a side effect. If the requirement is "invalid data doesn't produce wrong output," test the pipeline output — don't just test that the schema validator rejects the input. Schema validation is an implementation detail; the requirement is about the output.

### Cross-Variant Testing Strategy

If the project handles multiple input types (configurations, formats, locales, etc.), cross-variant coverage is where silent bugs hide. **At least 30% of your tests should exercise all variants**, not just one.

**Rationale:** If a requirement applies to all variants, it should be tested on all variants. Testing on one variant and hoping it works on the others is a classic source of silent bugs.

**Use your framework's parametrization** to avoid duplicating test logic:

```
# Pseudocode — use your framework's parametrization
# pytest: @pytest.mark.parametrize
# Jest: test.each([...])
# JUnit: @ParameterizedTest @MethodSource

for each variant in [variant_a, variant_b, variant_c]:
    test_feature_works(variant):
        output = process(variant.input)
        assert output has expected_property
```

If parametrization doesn't fit your fixture pattern, loop explicitly:

```
def test_feature_works_across_all_variants(self, variant_a, variant_b, variant_c):
    for variant in [variant_a, variant_b, variant_c]:
        # Assert the property holds for every variant
```

**Which tests should be cross-variant?** Any test that verifies a property that *should* hold regardless of input type: entity identity, structural properties, required links, temporal fields, domain-specific semantics. Only tests that verify variant-*specific* behavior should be single-variant.

**After writing all your tests, do a cross-variant audit.** Go through every test in `TestSpecRequirements` and ask: "Does this requirement apply to all input variants?" If yes and the test only uses one variant's fixture, convert it. Common candidates include: structural completeness, identity verification, required field presence, data relationships, semantic correctness, and integration points. These are universal requirements — testing them on one variant and hoping the others work is exactly the kind of silent bug this playbook exists to prevent.

**Target:** Count your cross-variant tests (those that loop or parametrize over all variants) and divide by total tests. If the result is below 30%, convert more single-variant tests before you're done.

### Common Anti-Patterns to Avoid

These patterns look like tests but don't catch real bugs. **Do not write tests that do any of these:**

- **Existence-only checks.** Finding one correct result doesn't mean all results are correct. If you need to check existence, also check count or verify the common case.
- **Presence-only assertions.** Asserting a value is present only proves it exists — not that it's correct. Assert the actual value.
- **Single-variant testing.** If the project handles multiple input types but you only test one, you're leaving coverage gaps unverified. Use parametrization or loop over all variants.
- **Positive-only testing.** You must test that invalid input does NOT produce bad output. If invalid values should be dropped, assert the field is absent. If unknown types should not be classified, assert every expected type is absent.
- **Incomplete negative assertions.** When testing that something is rejected, assert ALL consequences are absent — not just one. If unrecognized input should have no special properties, check for absence of type, related-entity links, role references, etc.
- **Catching exceptions instead of checking output.** If a test does `try: process(bad_input); except Error: pass`, it's testing that the code *crashes in a specific way* — not that it *handles the input correctly*. The spec says "bad input should not produce bad output." Test the output, not the exception. Here's the pattern to avoid and the fix:

```
# WRONG — tests the validation mechanism, not the requirement
test_bad_value_rejected(fixture):
    fixture.field = "invalid"  # Schema rejects this before processing!
    try:
        process(fixture)
        fail("Expected validation error")
    catch ValidationError:
        pass  # Tells you nothing about the output

# RIGHT — tests the requirement using a schema-valid mutation (see Step 5b)
test_bad_value_not_in_output(fixture):
    fixture.field = null  # Schema accepts null for optional fields
    output = process(fixture)
    assert output does not contain field_property  # Bad data is absent
    assert output contains expected_type  # Rest still works
```

The WRONG test fails with a validation error because `"invalid"` isn't a valid type for the field. The RIGHT test uses `null` (which the schema accepts for `Optional` fields) so the mutation reaches the mapper. If someone later refactors the validation, the RIGHT test still catches bugs while the WRONG test breaks for no reason. **Always check your Step 5b schema map before choosing mutation values.**

### Fitness-to-Purpose Scenario Tests

For **each** scenario in QUALITY.md that can be automated, write a test. This must be a 1:1 mapping — every scenario gets its own test, named to match the scenario.

```
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

### Boundary and Negative Tests

Write a dedicated test class for edge cases and error handling. **This class should be substantial** — aim for at least as many boundary/negative tests as you have defensive patterns from Step 5.

```
class TestBoundariesAndEdgeCases:
    """Tests for boundary conditions, malformed input, and error handling.

    Each test targets a defensive code pattern found in Step 5.
    """

    def test_malformed_input_handled_gracefully(self, fixture):
        """Defensive pattern: function_name() guards against X."""
        # Mutate fixture to trigger the defensive code path
        # Assert the system handles it gracefully (no crash, correct fallback)
```

For every null check, try/catch, or normalization function you found in Step 5, there should be at least one test that actually triggers that code path.

**Critical: Use your Step 5b schema map when choosing mutation values.** Every mutation must produce a value the schema accepts — otherwise the test fails with a validation error instead of testing the defensive code you care about. Refer to the field type mapping you built in Step 5b. Use values from the "Accepts" column, never the "Rejects" column.

For example, if you want to test that missing fields don't produce bad output, don't set the field to an invalid type (the schema rejects it). Set it to `null` (which the schema accepts) and verify the mapper produces correct output without that field. The test should verify the *output*, not provoke a schema crash.

**Systematic approach for finding boundary tests:** Walk each mapper module and list every guard clause, type check, and fallback. Then write tests for:

- **Missing fields**: What if an optional field is absent? (Set to `null` or remove the key.)
- **Wrong types**: What if a field of one type gets a different type?
- **Empty values**: What if a list is empty? A string is empty? A dict has no keys?
- **Boundary values**: Zero, negative, maximum, first element, last element.
- **Cross-module boundaries**: What if Module A produces unusual but valid output — does Module B handle it?

If you found 10+ defensive patterns in Step 5 but only wrote 4 boundary tests, go back and write more. The ratio of defensive patterns to boundary tests should be close to 1:1.

**After writing all your tests, do a boundary audit.** List every null check, type check, try/catch, and fallback default in the core mapper modules. Count them. Then count your boundary/negative tests. If the test count is less than your defensive pattern count, you need more tests.

### Testing at the Right Layer

A common mistake is testing an *implementation mechanism* instead of the *requirement*. For example, if the requirement is "invalid values must not produce bad output," there are two ways to test this:

- **Wrong layer (testing the mechanism):** Assert that the schema validator rejects the bad value. This tests the validation *mechanism*, not the *requirement*. If someone later changes the validation approach, this test breaks even though the requirement is still met.
- **Right layer (testing the requirement):** Feed a schema-valid bad value through the full mapping pipeline and assert the output doesn't contain bad data. This tests the *outcome* the requirement specifies.

When writing tests, ask: "What does the *spec* say should happen?" The spec says "invalid data should not appear in output" — not "validation layer should reject it." Test the spec, not the implementation.

**Exception:** When a spec explicitly mandates a specific mechanism (e.g., "must fail-fast at the schema layer"), testing that mechanism is appropriate. But this is rare — most specs describe outcomes, not mechanisms.

### Running the Tests

These tests should run as part of the normal test suite:

```
# Run all tests including functional tests
[your test runner] [test directory] --verbose

# Run only functional tests
[your test runner] [functional test file] --verbose
```

Adapt the commands to your project's test framework. Examples:
- pytest: `pytest quality/ -v` and `pytest quality/test_functional.py -v`
- Jest: `npm test` and `npm test -- test_functional.js`
- JUnit: `mvn test` and `mvn test -Dtest=TestFunctional`

---

## File 3: `quality/RUN_CODE_REVIEW.md` — Code Review Protocol

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

## File 4: `quality/RUN_INTEGRATION_TESTS.md` — Integration Test Protocol

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

Where possible, encode integration checks as automated tests in a `test_integration.py` file so they can run with your test runner. The Markdown protocol should describe both the automated tests and any manual verification steps that require external systems.

#### Reporting Format
Define a structured report format so results are comparable across runs. Include a summary table and a place for detailed findings.

---

## File 5: `quality/RUN_SPEC_AUDIT.md` — Council of Three Spec Audit Protocol

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
- Found by all three → highest confidence
- Found by two → high confidence
- Found by one → needs verification

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

**If `AGENTS.md` already exists**, do not regenerate it from scratch. Instead, read the existing file and verify that the Quality Docs section accurately points to all generated quality files (`quality/QUALITY.md`, `quality/test_functional.py`, `quality/RUN_CODE_REVIEW.md`, `quality/RUN_INTEGRATION_TESTS.md`, `quality/RUN_SPEC_AUDIT.md`, and spec document locations). Update paths or add missing entries if needed, but preserve the existing content — it was likely curated by a human or a prior session.

**If `AGENTS.md` does not exist**, create it with the following structure:

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
Things from external systems that surprised you. Example: "The API returns sentinel values for missing fields instead of null."

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

### Self-Check: Did You Go Deep Enough?

Before verifying, honestly assess your work against these benchmarks:

**test_functional.py size check:**
- Fewer than your formula minimum → You almost certainly missed spec requirements. Go back to Step 4 and walk each spec section.
- At your formula minimum → Review whether you tested negative cases and boundaries.
- Well above your minimum → This is where a thorough job lands for a medium-sized project. Aim here.

**Scenario coverage check:**
- Count the scenarios in QUALITY.md. Count the `test_scenario_*` functions. The numbers must match exactly.

**Cross-variant check:**
- If the project handles N input variants, what percentage of tests exercise all N? **If less than 30%, you need more parametrized tests.** Count them explicitly: tests that loop or parametrize over all variants, divided by total tests. This is the most common gap.

**Boundary and negative test check:**
- How many tests verify that invalid/missing/malformed input is handled correctly? Count the defensive patterns from Step 5. Your boundary/negative test count should approach your defensive pattern count. If significantly lower, go back to Step 5 and identify the patterns you haven't tested.

**Assertion depth check:**
- Scan your assertions. How many are presence checks vs. value checks? If more than half are presence-only, strengthen them.

**Layer check:**
- For each test, ask: "Am I testing the *requirement* or the *mechanism*?" If any test asserts that a specific error type is raised rather than asserting the output is correct, reconsider whether you're testing at the right layer.

**Mutation validity check:**
- For every test that mutates a fixture, verify the mutation value is in the "Accepts" column of your Step 5b schema map. If any mutation uses a type the schema rejects, the test will fail with a validation error instead of testing the defensive code. Fix the mutation to use a schema-valid value like `null`, `0`, or `[]`.

### Verify the Documentation

1. **QUALITY.md scenarios reference real code.** Every scenario should mention actual function names, file names, or patterns that exist in the codebase. Grep for them.

2. **RUN_CODE_REVIEW.md is self-contained.** An AI with no prior context should be able to read it and perform a useful review.

3. **RUN_INTEGRATION_TESTS.md is executable.** Every command in it should work. Every check should have a concrete pass/fail criterion, not "verify it looks right."

4. **RUN_SPEC_AUDIT.md prompt is copy-pasteable.** The definitive audit prompt should work when pasted into Claude Code, Cursor, and Copilot without modification (except file reference syntax).

5. **AGENTS.md setup instructions work.** From a clean clone, following the setup section should result in passing tests.

### Verify the Tests

6. **All existing tests still pass.** Your new files should not break anything.

7. **`test_functional.py` passes.** Run it with your test runner. Every test should pass.

8. **Every QUALITY.md scenario has a named test.** For each fitness-to-purpose scenario N, there must be a test named `test_scenario_N_*` in `test_functional.py`. If the QUALITY.md has 10 scenarios, there must be at least 10 scenario tests. If a scenario can't be automated, it must be explicitly marked as requiring the Human Gate in QUALITY.md.

9. **Every spec section was considered.** Walk each spec document section by section. For each section, verify either (a) a test covers it, or (b) the section contains no testable requirements. If you find more than 3 testable spec sections without tests, add more tests.

10. **Tests are not theater.** For each test, ask: "If I deleted the function body being tested, would this test fail?" If the answer is no, rewrite the test. Specifically check:
    - Do any tests use `LIMIT 1` to find one example and call it done? If so, add a count or exhaustive check.
    - Do any tests assert presence without checking the actual value? If so, check the value.
    - Do any negative tests assert only one consequence of rejection? If so, assert all consequences.

11. **Cross-variant coverage exists.** If the project handles multiple input types/formats/configurations, at least 30% of tests should parametrize or loop across all variants. Count them explicitly. If you have 40 tests, at least 12 should be cross-variant.

12. **Negative and boundary tests exist.** Count the defensive patterns from Step 5. Your boundary/negative test count should be close to this number. If significantly lower, write more tests targeting the defensive patterns you haven't covered.

13. **Tests verify outcomes, not mechanisms.** Scan each test for assertions about error types. If a test only checks that a specific error is raised without also verifying the pipeline output, it's testing the mechanism, not the requirement. Rewrite it to test the outcome the spec requires.

---

## How to Run Each Protocol After Creation

### Automated Tests (Functional + Regression)
```bash
[your test runner] [test directory] --verbose
```
Run every time. This is your automated safety net. Once a functional test passes, it becomes a regression test — any future change that breaks it is a regression.

### Code Review (Static)
```
Read quality/RUN_CODE_REVIEW.md and perform a code review.
```
Give this to 2+ AI tools independently. Cross-reference findings. Run before releases and after significant changes.

### Integration Tests (Automated + Manual)
```
Read quality/RUN_INTEGRATION_TESTS.md and run the full integration test suite.
```
Exercises the full pipeline end-to-end. Run before releases.

### Spec Audit — Council of Three (Static)
```
Read quality/RUN_SPEC_AUDIT.md and perform a spec audit.
```
Give to 3 independent AI tools. Merge findings. Triage. Convert confirmed findings into new functional tests. Run before major releases or after architectural changes. Budget 2+ hours.

### The Workflow for a Typical Release

1. **Run automated tests** — `[your test runner] [test directory]` (~1 min)
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
