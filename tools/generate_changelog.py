#!/usr/bin/env python3
"""Generate a Keep a Changelog section from commits since the previous tag."""

import argparse
import re
import subprocess
from datetime import datetime, timezone
from pathlib import Path


REPOSITORY_ROOT = Path(__file__).resolve().parents[1]
CHANGELOG_PATH = REPOSITORY_ROOT / "CHANGELOG.md"
CONVENTIONAL_COMMIT = re.compile(
    r"^(?P<type>[a-z]+)(?:\([^)]+\))?(?:!)?:\s*(?P<message>.+)$",
    re.IGNORECASE,
)
CHANGE_CATEGORIES = (
    "Adicionado",
    "Corrigido",
    "Melhorado",
    "Refatorado",
    "Documentação",
    "Testes",
    "Estilo",
    "Revertido",
    "Manutenção",
    "Alterado",
)
CATEGORY_BY_TYPE = {
    "feat": "Adicionado",
    "fix": "Corrigido",
    "perf": "Melhorado",
    "refactor": "Refatorado",
    "docs": "Documentação",
    "test": "Testes",
    "style": "Estilo",
    "revert": "Revertido",
    "build": "Manutenção",
    "ci": "Manutenção",
    "chore": "Manutenção",
}


def is_automated_release_commit(subject: str) -> bool:
    normalized = subject.lower()
    return (
        "[skip ci]" in normalized
        or "[ci skip]" in normalized
        or normalized.startswith("chore(release):")
        or normalized.startswith("release:")
    )


def categorize_commit(subject: str) -> tuple[str, str]:
    match = CONVENTIONAL_COMMIT.match(subject)
    if match is None:
        return "Alterado", subject

    commit_type = match.group("type").lower()
    message = match.group("message").strip()
    return CATEGORY_BY_TYPE.get(commit_type, "Alterado"), message


def render_section(version: str, release_date: str, subjects: list[str]) -> str:
    categorized: dict[str, list[str]] = {category: [] for category in CHANGE_CATEGORIES}
    for subject in subjects:
        if is_automated_release_commit(subject):
            continue
        category, message = categorize_commit(subject)
        categorized[category].append(message)

    lines = [f"## [{version}] - {release_date}"]
    for category, messages in categorized.items():
        if messages:
            lines.extend(["", f"### {category}", ""])
            lines.extend(f"- {message}" for message in messages)

    if not any(categorized.values()):
        lines.extend(["", "Nenhuma alteração de produto identificada desde a versão anterior."])

    return "\n".join(lines)


def insert_section(changelog: str, version: str, section: str) -> str:
    version_heading = re.compile(rf"^## \[{re.escape(version)}\](?:\s|$)", re.MULTILINE)
    if version_heading.search(changelog):
        return changelog

    first_version = re.search(r"^## \[", changelog, re.MULTILINE)
    if first_version is None:
        return f"{changelog.rstrip()}\n\n{section}\n"

    header = changelog[: first_version.start()].rstrip()
    history = changelog[first_version.start() :].lstrip("\n")
    return f"{header}\n\n{section}\n\n{history}"


def git_output(*arguments: str) -> str:
    result = subprocess.run(
        ["git", *arguments],
        cwd=REPOSITORY_ROOT,
        check=True,
        capture_output=True,
        text=True,
    )
    return result.stdout.strip()


def generate_changelog(version: str, release_date: str) -> bool:
    previous_tag = git_output("describe", "--tags", "--abbrev=0", "HEAD^")
    subjects_output = git_output(
        "log",
        "--no-merges",
        "--format=%s",
        f"{previous_tag}..HEAD",
    )
    subjects = subjects_output.splitlines() if subjects_output else []

    current_changelog = CHANGELOG_PATH.read_text(encoding="utf-8")
    section = render_section(version, release_date, subjects)
    updated_changelog = insert_section(current_changelog, version, section)
    if updated_changelog == current_changelog:
        print(f"CHANGELOG.md already contains version {version}; leaving it unchanged.")
        return False

    CHANGELOG_PATH.write_text(updated_changelog, encoding="utf-8")
    print(f"Generated changelog for {version} from commits since {previous_tag}.")
    return True


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--version", required=True, help="Release version to document.")
    parser.add_argument(
        "--date",
        default=datetime.now(timezone.utc).date().isoformat(),
        help="Release date in YYYY-MM-DD format (defaults to the current UTC date).",
    )
    arguments = parser.parse_args()

    try:
        generate_changelog(arguments.version, arguments.date)
    except (OSError, subprocess.CalledProcessError) as error:
        detail = getattr(error, "stderr", None) or str(error)
        parser.error(f"Could not generate CHANGELOG.md: {detail.strip()}")


if __name__ == "__main__":
    main()
