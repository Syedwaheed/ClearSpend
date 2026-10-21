You are the ORCHESTRATOR for building ClearSpend. Work autonomously from the
first unfinished phase through Phase 7.

FILES TO READ FIRST (in this order, fully):
1. CLAUDE.md
2. designs/DESIGN_SPEC.md
3. BUILD_PROMPTS.md  (Part A = shared context, Part B = Phases 1-7)
4. PROGRESS.md  (if it exists; it is your memory, see below)

HOW THE LOOP WORKS
PROGRESS.md is the single source of truth for state. Create it if missing, with
one row per phase (1-7): status (todo / in-progress / verified / blocked),
commit hash, test summary, deviations, open issues. Update it after every step.
If I restart you, resume from the first phase not marked "verified".

For each phase N in order:

1. PLAN. Tell me in 3-5 lines which phase you are starting. Mark it
   "in-progress" in PROGRESS.md.

2. BUILD. Spawn ONE subagent (Agent tool, general-purpose) so the phase gets
   fresh context. Its prompt must contain, verbatim:
   - the full Part A block from BUILD_PROMPTS.md
   - the full text of Phase N from Part B
   - "Read CLAUDE.md, designs/DESIGN_SPEC.md and the relevant designs/screens
     PNGs yourself. Run ./gradlew build and ./gradlew test and report the real
     output tail. Do not commit. Return: files changed per module, test counts,
     deviations from the spec, anything left undone."
   Run phases sequentially. Never run two phase agents at once, because they
   share the same Gradle project.

3. VERIFY. Do not trust the subagent's report. Check it yourself:
   a. Run ./gradlew build and ./gradlew test and read the actual output.
   b. Run the NON-NEGOTIABLE RULES audit with grep/Glob on code touched in this
      phase: Double used for money, kotlin.Result, .toUi(), formatting inside
      ViewModels/Composables, hardcoded sample values from the screenshots,
      logging of tokens/secrets, feature -> feature dependencies, hardcoded
      versions outside libs.versions.toml, Route references inside feature
      modules, and mixed jvm.library + android plugins in domain modules.
   c. Confirm every screen in the phase has Loading, Empty, Error, Content
      (and Offline where cached data exists), a ViewModel, a UiMapper, and
      tests, as the phase requires.
   d. Open each screenshot for the phase's screens and compare it with the
      @Preview or the emulator output. If an emulator/adb device is available
      (check `adb devices`), run the app and take screenshots to compare.
      If none is available, say so in PROGRESS.md and rely on @Preview and
      Compose UI tests.
   e. For anything the Gate asks me to do by hand (emulator login, a real
      sandbox payment, a TalkBack pass), do it yourself if possible. If it
      needs something only I can provide (a signing key, a device), record it
      as "needs human" in PROGRESS.md and keep going. Do not block the phase
      on it.

4. FIX LOOP. If build, tests, the audit, or the screenshot comparison fail,
   spawn a fix subagent (or fix directly if it's small) with the exact failure
   output, then re-run step 3. Maximum 5 fix rounds per phase. If still
   failing, mark the phase "blocked", write the root cause and what you tried
   to PROGRESS.md, and STOP and ask me. Never skip a failing test, delete a
   test, add @Ignore, weaken an assertion, or lower a rule just to turn the
   build green.

5. COMMIT AND PUSH. Only when the phase is verified:
   - Follow the "Branching" convention in PROGRESS.md: work on
     feature/<slug> branched from an up-to-date main (git switch main &&
     git pull, then git switch -c feature/<slug>). If the branch already
     exists, switch to it.
   - Check `git status` and `git diff --cached` for secrets. Never commit
     local.properties.
   - git add -A, git commit -m "Phase N: <title>", then
     git push -u origin feature/<slug>.
   - Do NOT open the PR, merge, rebase, or force-push. I open and review
     the PR myself.
   - Update PROGRESS.md with the branch name, commit hash and test summary.

6. STOP AFTER EACH PHASE. Report what was built and tell me the branch is
   pushed. Then wait until I type "merged" (meaning I have reviewed and
   merged the PR into main). When I do, run git switch main && git pull,
   confirm the build still passes, and start the next phase from there.

STOP AND ASK ME ONLY WHEN
- a fix loop is exhausted (5 rounds);
- CLAUDE.md, DESIGN_SPEC.md, and the screenshots genuinely conflict and the
  precedence stated in Part A doesn't settle it (code: CLAUDE.md; data:
  DESIGN_SPEC; visuals: screenshots);
- a screen's DESIGN_SPEC section or screenshot is missing;
- something needs a credential or a destructive action I haven't approved
  (force-push, deleting branches, rewriting history).
Otherwise make the sensible decision, record it under "Deviations" in
PROGRESS.md, and keep moving.

HARD RULES
- Never claim a build or test passed without pasting the real command output
  summary into PROGRESS.md.
- Never put real secrets in any file. Credentials only via local.properties.
- Keep your own messages short. Report after each phase with: what was built,
  test counts, deviations, "needs human" items, and the next phase.

FINAL DELIVERY (after Phase 7 is verified)
1. Run the full audit from Phase 7 one more time across the whole repo.
2. Run ./gradlew clean build test and paste the result summary.
3. Walk through the list of all 38 screenshots and mark each one
   matched / deviation / needs human in PROGRESS.md.
4. Give me a final report: what is done, what needs my manual action
   (signing key, real sandbox payment, device checks), and known gaps.

START NOW: read the files, create or load PROGRESS.md, and begin the first
unfinished phase.