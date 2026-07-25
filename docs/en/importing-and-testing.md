# Importing, Testing, and Troubleshooting

Press `F9`, open the import page, and choose either **Import ZIP** or **Import Folder**. Imported packs are copied to:

```text
config/totemdoll/styles/imported/
```

Folders are copied as well, so re-import the folder after changing the source files. Deletion is performed at pack level.

## Common errors

- `No style.json was found`: the root has neither `pack.json` nor `style.json`.
- `Expected pack format 1`: `pack.json.format` is not `1`.
- `Expected style format 2`: `style.json.format` is not `2`.
- `Path leaves the style pack`: a path contains `..`, is absolute, or uses a drive path.
- `Missing texture/model`: a referenced file is missing or has incorrect casing.
- `Unsupported model type`: only `minecraft_item` and `minecraft_bone` are supported.

Detailed errors are written to `logs/latest.log`. Search for `Totem Doll`, `Could not compile imported style pack`, or `Skipping invalid imported style`.

Importing and deleting a pack triggers one client resource reload. If one style in a multi-style pack is invalid, valid styles remain available and the invalid style is skipped.
