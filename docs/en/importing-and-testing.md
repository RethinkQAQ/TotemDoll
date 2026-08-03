# Importing, Testing, and Troubleshooting

Press `F9`, open the import page, and choose **Import ZIP**. The UI intentionally exposes ZIP as the player-facing format. Imported packs are copied to:

```text
config/totemdoll/styles/imported/
```

For development, unpacked packs can still be placed directly in the local styles directory; this is intentionally a developer workflow rather than a player-facing import option. Deletion is performed at pack level.

## Common errors

- `No style.json was found`: the root has neither `pack.json` nor `style.json`.
- `Expected pack format 1`: `pack.json.format` is not `1`.
- `Expected style format 3`: `style.json.format` is not `3`.
- `Path leaves the style pack`: a path contains `..`, is absolute, or uses a drive path.
- `Missing texture/model`: a referenced file is missing or has incorrect casing.
- `Unsupported model type`: only the cross-version `mesh` model is supported.

Detailed errors are written to `logs/latest.log`. Search for `Totem Doll`, `Could not compile imported style pack`, or `Skipping invalid imported style`.

Importing and deleting a pack triggers one client resource reload. If one style in a multi-style pack is invalid, valid styles remain available and the invalid style is skipped.
