# Project agent memory

This file is the project's committed home for project-intrinsic agent knowledge: build, test, release, architecture, and sharp-edge notes that should travel with the code.

- Local stack, demo data switch and tests: see `README.md` (`./dev.sh`, `./dev.sh --reset`, `app.seed-demo-data`).
- Spotless only checks files changed since `spotless.ratchetFrom` (`back/pom.xml`). Running `./mvnw spotless:apply -Dspotless.ratchetFrom=NONE` (needed in a linked worktree, where the ratchet ref does not resolve) reformats every legacy Java file; revert the files you did not touch before committing.
- `docker-compose.yml` uses fixed `container_name`s while the compose project name comes from the checkout folder name, so a checkout in a differently named folder collides with old containers; `dev.sh` then fails on `docker compose up` and the stale container must be removed with `docker rm -f`.
- Mail templates render message keys from `back/src/main/resources/messages*.properties` via `#messages.msg(...)`; placeholders are MessageFormat `{0}` (so double literal apostrophes as `''` and pass amounts as strings to avoid locale number formatting), and the bundles are UTF-8 (`spring.messages.encoding=UTF-8`).

## Maintaining this file

Keep this file for knowledge useful to almost every future agent session in this project.
Do not repeat what the codebase already shows; point to the authoritative file or command instead.
Prefer rewriting or pruning existing entries over appending new ones.
When updating this file, preserve this bar for all agents and keep entries concise.
