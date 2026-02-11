# Changelog

## [0.0.2] - 2026-02-11

### Added
- Data pipeline: parse_gita_data.py (reads from local repo clone, no API calls)
- Data validation: validate_gita_data.py (8-section comprehensive check)
- Generated gita_data.json — 18 chapters, 701 verses, 8183 translations, 11189 commentaries
- .gitignore

### Changed
- Removed dead code (unreachable return) in fetch_gita_data.py

### Validated
- All data verified: structure, chapter metadata, verse continuity, content quality, author coverage, Swift compatibility
- Local-parsed data matches API-fetched data (byte-identical)
- 2 known upstream encoding issues in commentaries (V2 scope)

## [0.0.1] - 2026-02-10

### Added
- Initial project documentation (architecture.md, CLAUDE.md, README.md)
- Data pipeline script (fetch_gita_data.py)
- Project structure and development workflow
