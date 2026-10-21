import importlib.util
import unittest
from pathlib import Path


SCRIPT_PATH = Path(__file__).resolve().parents[1] / "generate_changelog.py"
SPEC = importlib.util.spec_from_file_location("generate_changelog", SCRIPT_PATH)
generate_changelog = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(generate_changelog)


class GenerateChangelogTest(unittest.TestCase):
    def test_conventional_commits_are_categorized_and_simplified(self):
        subjects = [
            "feat(catalog): add offer sharing",
            "fix: handle missing coupon",
            "chore(release): bump version to 1.2.0 [skip ci]",
            "Update privacy policy",
        ]

        section = generate_changelog.render_section("1.2.0", "2026-10-21", subjects)

        self.assertIn("### Adicionado\n\n- add offer sharing", section)
        self.assertIn("### Corrigido\n\n- handle missing coupon", section)
        self.assertIn("### Alterado\n\n- Update privacy policy", section)
        self.assertNotIn("bump version", section)

    def test_release_markers_are_filtered(self):
        self.assertTrue(generate_changelog.is_automated_release_commit("chore(release): bump version"))
        self.assertTrue(generate_changelog.is_automated_release_commit("docs: generated [skip ci]"))
        self.assertTrue(generate_changelog.is_automated_release_commit("build: generated [ci skip]"))
        self.assertFalse(generate_changelog.is_automated_release_commit("chore: update dependencies"))

    def test_insert_preserves_history_and_places_new_release_first(self):
        existing = (
            "# Changelog\n\n"
            "Intro text.\n\n"
            "## [1.1.0] - 2026-10-01\n\n"
            "### Adicionado\n\n- Existing feature.\n"
        )

        updated = generate_changelog.insert_section(existing, "1.2.0", "## [1.2.0] - 2026-10-21")

        self.assertTrue(updated.startswith("# Changelog\n\nIntro text.\n\n## [1.2.0] - 2026-10-21"))
        self.assertIn("## [1.1.0] - 2026-10-01\n\n### Adicionado\n\n- Existing feature.\n", updated)

    def test_insert_is_idempotent_for_existing_version(self):
        existing = "# Changelog\n\n## [1.2.0] - 2026-10-20\n\nExisting content.\n"

        self.assertEqual(
            existing,
            generate_changelog.insert_section(existing, "1.2.0", "## [1.2.0] - 2026-10-21"),
        )

    def test_empty_release_range_is_explicit(self):
        section = generate_changelog.render_section(
            "1.2.0",
            "2026-10-21",
            ["chore(release): bump version [skip ci]"],
        )

        self.assertIn("Nenhuma alteração de produto identificada", section)


if __name__ == "__main__":
    unittest.main()
