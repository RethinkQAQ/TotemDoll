"""Exercise publishing collection without contacting either publishing platform."""
import json
import os
from pathlib import Path
import subprocess
import sys
import tempfile
import unittest
import zipfile

SCRIPT = Path(__file__).with_name('collect_artifacts.py').resolve()


class CollectionTests(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name)
        self.configure(['1.21.1', '26.3'])
        for mc in ('1.21.1', '26.3'):
            config = self.root / 'versions' / mc / 'gradle.properties'
            config.parent.mkdir(parents=True)
            config.write_text(f'minecraft_version={mc}\nenable_platforms=fabric,neoforge\n'
                              f'publish_version={"1.21,1.21.1" if mc == "1.21.1" else "26.3"}\n')

    def configure(self, versions):
        (self.root / 'gradle.properties').write_text(
            'mod.id=totemdoll\nstonecutter_enabled_versions=' + ','.join(versions) + '\n')

    def jar(self, loader, mc, suffix='', valid=True, version='1.3.2'):
        filename = f'totemdoll-{loader}-{version}-mc{mc}{"-" + suffix if suffix else ""}.jar'
        path = self.root / loader / 'versions' / mc / 'build' / 'libs' / filename
        path.parent.mkdir(parents=True, exist_ok=True)
        with zipfile.ZipFile(path, 'w') as jar:
            if not valid:
                jar.writestr('invalid.txt', 'no mod metadata')
            elif loader == 'fabric':
                jar.writestr('fabric.mod.json', json.dumps({
                    'id': 'totemdoll', 'version': version,
                    'depends': {'minecraft': f'>={mc}'},
                }))
            else:
                jar.writestr('META-INF/neoforge.mods.toml',
                             f'[[mods]]\nmodId="totemdoll"\nversion="{version}"\n'
                             f'[[dependencies.totemdoll]]\nmodId="minecraft"\nversionRange="[{mc},)"\n')
        return path

    def run_collector(self, suffix=''):
        env = dict(os.environ, BUILD_VERSION='1.3.2', BUILD_SUFFIX=suffix,
                   GITHUB_OUTPUT=str(self.root / 'output'), GITHUB_STEP_SUMMARY=str(self.root / 'summary'),
                   GITHUB_EVENT_NAME='push' if suffix else 'release')
        return subprocess.run([sys.executable, str(SCRIPT)], cwd=self.root, env=env,
                              capture_output=True, text=True)

    def test_release_and_build_artifacts(self):
        for suffix in ('', 'build.123'):
            for loader in ('fabric', 'neoforge'):
                for mc in ('1.21.1', '26.3'):
                    self.jar(loader, mc, suffix)
            result = self.run_collector(suffix)
            self.assertEqual(result.returncode, 0, result.stderr)
            jars = list((self.root / 'combined-artifacts').glob('*.jar'))
            self.assertEqual(len(jars), 4)
            self.assertTrue(all(('-build.123' in jar.name) == bool(suffix) for jar in jars))
            lines = (self.root / 'output').read_text().splitlines()
            entries = json.loads(lines[-1].split('=', 1)[1])['include']
            self.assertEqual(len(entries), 4)
            fabric = next(e for e in entries if e['group'] == 'fabric-1.21.1')
            self.assertEqual(fabric['game_versions'], '1.21\n1.21.1')
            self.assertEqual(fabric['platform_version'], '1.3.2-mc1.21.1-fabric')
            table_rows = [line for line in (self.root / 'summary').read_text().splitlines() if line.startswith('|')]
            self.assertEqual(len(table_rows), 6)
            self.assertTrue(all(row.count('|') == 8 for row in table_rows))
            with zipfile.ZipFile(self.root / 'mod-jars.zip', 'w') as archive:
                for jar in jars:
                    archive.write(jar, jar.name)
            with zipfile.ZipFile(self.root / 'mod-jars.zip') as archive:
                self.assertTrue(all('/' not in name and name.endswith('.jar') for name in archive.namelist()))

    def test_disabled_version_and_loader_are_not_collected(self):
        for loader in ('fabric', 'neoforge'):
            for mc in ('1.21.1', '26.3'):
                self.jar(loader, mc)
        self.configure(['1.21.1'])
        path = self.root / 'versions' / '1.21.1' / 'gradle.properties'
        path.write_text(path.read_text().replace('fabric,neoforge', 'fabric'))
        result = self.run_collector()
        self.assertEqual(result.returncode, 0, result.stderr)
        self.assertEqual([p.name for p in (self.root / 'combined-artifacts').iterdir()],
                         ['totemdoll-fabric-1.3.2-mc1.21.1.jar'])

    def test_old_jar_cannot_substitute_missing_current_jar(self):
        self.jar('fabric', '1.21.1', version='1.1.1')
        result = self.run_collector()
        self.assertNotEqual(result.returncode, 0)
        self.assertIn('Missing publishable JAR', result.stderr)

    def test_invalid_metadata_stops_collection(self):
        self.jar('fabric', '1.21.1', valid=False)
        result = self.run_collector()
        self.assertNotEqual(result.returncode, 0)
        self.assertIn('Missing fabric.mod.json', result.stderr)


if __name__ == '__main__':
    unittest.main()
