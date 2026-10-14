import json
import os
import tomllib
import shutil
import zipfile
from collections import defaultdict
from pathlib import Path

artifacts = Path('artifacts')
if artifacts.exists():
    shutil.rmtree(artifacts)
artifacts.mkdir()
combined = Path('combined-artifacts')
if combined.exists():
    shutil.rmtree(combined)
combined.mkdir()
groups = defaultdict(list)
build_version = os.environ['BUILD_VERSION']
build_suffix = os.environ.get('BUILD_SUFFIX', '').strip()

def read_properties(path):
    values = {}
    for line in path.read_text(encoding='utf-8').splitlines():
        line = line.strip()
        if not line or line.startswith('#') or '=' not in line:
            continue
        key, value = line.split('=', 1)
        values[key.strip()] = value.strip()
    return values

root_config = read_properties(Path('gradle.properties'))
registered = [v.strip() for v in root_config['stonecutter_enabled_versions'].split(',') if v.strip()]
if not registered or len(registered) != len(set(registered)):
    raise SystemExit('Empty or duplicate stonecutter_enabled_versions')
mod_id_expected = root_config['mod.id']
targets = []
for minecraft_version in registered:
    version_properties = Path('versions') / minecraft_version / 'gradle.properties'
    version_config = read_properties(version_properties)
    if version_config.get('minecraft_version') != minecraft_version:
        raise SystemExit(f'Minecraft version mismatch: {version_properties}')
    loaders = [v.strip() for v in version_config.get('enable_platforms', '').split(',') if v.strip()]
    if not loaders or len(loaders) != len(set(loaders)) or any(v not in ('fabric', 'neoforge') for v in loaders):
        raise SystemExit(f'Invalid enable_platforms: {version_properties}')
    for loader in loaders:
        targets.append((loader, minecraft_version, version_config))

for loader, minecraft_version, version_config in targets:
    publish_versions = [v.strip() for v in version_config.get('publish_version', '').split(',') if v.strip()]
    if not publish_versions:
        raise SystemExit(f'Missing publish_version for {minecraft_version}')
    filename = f'{mod_id_expected}-{loader}-{build_version}-mc{minecraft_version}'
    if build_suffix:
        filename += f'-{build_suffix}'
    jar = Path(loader) / 'versions' / minecraft_version / 'build' / 'libs' / f'{filename}.jar'
    if not jar.is_file():
        raise SystemExit(f'Missing publishable JAR: {jar}')
    with zipfile.ZipFile(jar) as zf:
        names = set(zf.namelist())
        if loader == 'fabric':
            if 'fabric.mod.json' not in names:
                raise SystemExit(f'Missing fabric.mod.json: {jar}')
            metadata = json.loads(zf.read('fabric.mod.json'))
            mod_id = metadata.get('id', '')
            mod_version = metadata.get('version', '')
            version_range = metadata.get('depends', {}).get('minecraft', '')
        else:
            metadata_name = 'META-INF/neoforge.mods.toml'
            if metadata_name not in names:
                raise SystemExit(f'Missing {metadata_name}: {jar}')
            metadata = tomllib.loads(zf.read(metadata_name).decode('utf-8'))
            mods = metadata.get('mods', [])
            if len(mods) != 1:
                raise SystemExit(f'Expected exactly one mod entry: {jar}')
            mod_id = mods[0].get('modId', '')
            mod_version = mods[0].get('version', '')
            dependencies = metadata.get('dependencies', {}).get(mod_id, [])
            version_range = next((dep.get('versionRange', '') for dep in dependencies if dep.get('modId') == 'minecraft'), '')


    if not all((mod_id, mod_version, version_range)):
        raise SystemExit(f'Incomplete mod metadata: {jar}')

    if mod_id != mod_id_expected or mod_version != build_version:
        raise SystemExit(f'Mod identity/version mismatch: {jar}: {mod_id} {mod_version}')
    if (loader == 'fabric' and 'META-INF/neoforge.mods.toml' in names) or (loader == 'neoforge' and 'fabric.mod.json' in names):
        raise SystemExit(f'Conflicting loader metadata: {jar}')

    group_name = f'{loader}-{minecraft_version}'
    group_dir = artifacts / group_name
    group_dir.mkdir(parents=True, exist_ok=True)
    output_name = jar.name
    (group_dir / output_name).write_bytes(jar.read_bytes())

    combined_name = output_name
    if (combined / combined_name).exists():
        raise SystemExit(f'Duplicate flattened JAR: {jar}')
    (combined / combined_name).write_bytes(jar.read_bytes())
    groups[group_name].append({
        'loader': loader,
        'minecraft_version': minecraft_version,
        'publish_version': publish_versions,
        'game_versions': '\n'.join(publish_versions),
        'minecraft_version_range': version_range,
        'mod_id': mod_id,
        'mod_version': mod_version,
        'platform_version': f'{mod_version}-mc{minecraft_version}-{loader}',
        'jar': output_name,
        'combined_jar': combined_name,
    })

matrix_entries = []
for group_name in sorted(groups):
    items = groups[group_name]
    manifest = {
        'loader': items[0]['loader'],
        'minecraft_version': items[0]['minecraft_version'],
        'publish_version': items[0]['publish_version'],
        'game_versions': items[0]['game_versions'],
        'minecraft_version_range': items[0]['minecraft_version_range'],
        'mod_id': items[0]['mod_id'],
        'mod_version': items[0]['mod_version'],
        'platform_version': items[0]['platform_version'],
        'jars': [item['jar'] for item in items],
    }
    group_dir = artifacts / group_name
    (group_dir / 'manifest.json').write_text(json.dumps(manifest, indent=2) + '\n')
    publish_versions_display = ', '.join(manifest['publish_version'])
    summary = [
        f'# {group_name}', '',
        f'- Loader: `{manifest["loader"]}`',
        f'- Minecraft: `{manifest["minecraft_version"]}`',
        f'- Publish versions: `{publish_versions_display}`',
        f'- Minecraft range: `{manifest["minecraft_version_range"]}`',
        f'- Mod: `{manifest["mod_id"]}`',
        f'- Mod version: `{manifest["mod_version"]}`', '',
        f'- Platform version: `{manifest["platform_version"]}`',
        f'- Game versions sent to mc-publish: `{publish_versions_display}`',
        '- Game version filter: `none` (explicit compatibility list)', '',
        '## Files', '',
    ]
    summary.extend(f'- `{jar_name}`' for jar_name in manifest['jars'])
    (group_dir / 'summary.md').write_text('\n'.join(summary) + '\n')
    matrix_entries.append({
        'group': group_name,
        'loader': manifest['loader'],
        'minecraft_version': manifest['minecraft_version'],
        'publish_version': manifest['publish_version'],
        'game_versions': manifest['game_versions'],
        'minecraft_version_range': manifest['minecraft_version_range'],
        'mod_version': manifest['mod_version'],
        'platform_version': manifest['platform_version'],
        'path': group_name,
    })

lines = [
    '# Build Summary', '',
    f'- Event: `{os.environ.get("GITHUB_EVENT_NAME", "unknown")}`',
    f'- Commit: `{os.environ.get("GITHUB_SHA", "unknown")}`',
    f'- Ref: `{os.environ.get("GITHUB_REF_NAME", "unknown")}`',
    f'- Mod version: `{os.environ.get("BUILD_VERSION", "unknown")}`',
    f'- Build suffix: `{os.environ.get("BUILD_SUFFIX") or "none"}`', '',
    '## Build Artifacts Summary', '',
    '| Loader | Minecraft | Publish versions | Mod version | Platform version | JAR | Artifact |',
    '| --- | --- | --- | --- | --- | --- | --- |',
]
for entry in matrix_entries:
    for item in groups[entry['group']]:
        artifact_name = f'mod-jars-{entry["group"]}' if os.environ.get('GITHUB_EVENT_NAME') == 'release' else 'mod-jars'
        publish_versions_display = ', '.join(entry['publish_version'])
        lines.append(f'| {item["loader"]} | {item["minecraft_version"]} | `{publish_versions_display}` | {item["mod_version"]} | `{entry["platform_version"]}` | `{item["jar"]}` | `{artifact_name}` |')
Path(os.environ['GITHUB_STEP_SUMMARY']).write_text('\n'.join(lines) + '\n')

with open(os.environ['GITHUB_OUTPUT'], 'a') as output:
    output.write('publish_matrix=' + json.dumps({'include': matrix_entries}, separators=(',', ':')) + '\n')
