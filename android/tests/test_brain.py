import json
import tempfile
import unittest
from pathlib import Path
import numpy as np
from android_brain import AndroidBrain

DEFINITION = (Path(__file__).resolve().parents[2] / 'pets/bryan.json').read_text()


class AndroidBrainTest(unittest.TestCase):
    def test_touch_learning_and_persistence(self):
        with tempfile.TemporaryDirectory() as directory:
            brain = AndroidBrain(directory, DEFINITION)
            for _ in range(15):
                brain.tick(0.2)
            before = brain.brain.learned.copy()
            brain.event('caress')
            for _ in range(8):
                state = json.loads(brain.tick(0.2))
            self.assertFalse(np.array_equal(before, brain.brain.learned))
            self.assertTrue(0 <= state['energy'] <= 1)
            brain.save()
            restored = AndroidBrain(directory, DEFINITION)
            np.testing.assert_allclose(restored.brain.learned, brain.brain.learned, atol=1e-6)

    def test_sleep_and_wake_touch(self):
        with tempfile.TemporaryDirectory() as directory:
            brain = AndroidBrain(directory, DEFINITION)
            brain.drives.energy = .4
            brain.event('sleep')
            self.assertEqual(json.loads(brain.tick(.2))['emotion'], 'dormindo')
            brain.event('poke')
            self.assertNotEqual(json.loads(brain.tick(.2))['emotion'], 'dormindo')

    def test_corrupt_state_is_tolerated(self):
        with tempfile.TemporaryDirectory() as directory:
            (Path(directory)/'nervous.json').write_text('{broken')
            brain = AndroidBrain(directory, DEFINITION)
            self.assertIn('emotion', json.loads(brain.tick(.2)))


class FullAndroidFeaturesTest(unittest.TestCase):
    def test_manual_sleep_at_full_energy_and_rest(self):
        with tempfile.TemporaryDirectory() as directory:
            brain=AndroidBrain(directory, DEFINITION)
            brain.event('sleep')
            self.assertEqual(json.loads(brain.tick(.2))['emotion'], 'dormindo')
            brain.drives.energy=.2
            brain.rest(60)
            self.assertGreater(brain.drives.energy,.2)
            brain.event('poke')
            self.assertNotEqual(json.loads(brain.tick(.2))['emotion'], 'dormindo')

    def test_note_dismiss_and_accept_are_distinct_memories(self):
        with tempfile.TemporaryDirectory() as directory:
            brain=AndroidBrain(directory, DEFINITION)
            brain.event('note')
            brain.event('dismiss')
            self.assertLess(brain.memory.recent[-1]['valencia'],0)
            brain.event('note')
            brain.memory.open[-1]['inicio']-=20
            brain.tick(.2)
            self.assertGreater(brain.memory.recent[-1]['valencia'],0)
            brain.save()
            restored=AndroidBrain(directory,DEFINITION)
            self.assertEqual(len(restored.memory.recent),2)
            self.assertTrue(restored.memory.preferences())

    def test_work_corner_and_mischief_controls(self):
        with tempfile.TemporaryDirectory() as directory:
            brain=AndroidBrain(directory,DEFINITION)
            self.assertFalse(json.loads(brain.tick(.2))['mischief'])
            brain.event('work')
            s=json.loads(brain.tick(.2))
            self.assertTrue(s['notebook']);self.assertEqual(s['speed'],0)
            brain.event('work');brain.event('corner')
            s=json.loads(brain.tick(.2));self.assertTrue(s['corner']);self.assertEqual(s['speed'],0)
            brain.event('caress');brain.event('mischief');brain.event('context:musica')
            s=json.loads(brain.tick(.2));self.assertFalse(s['corner']);self.assertTrue(s['mischief']);self.assertEqual(s['context'],'musica')

    def test_characters_have_separate_memories(self):
        jorge=(Path(__file__).resolve().parents[2]/'pets/jorge.json').read_text()
        with tempfile.TemporaryDirectory() as directory:
            bryan=AndroidBrain(str(Path(directory)/'bryan'),DEFINITION)
            bryan.event('caress');bryan.tick(.2);bryan.save()
            other=AndroidBrain(str(Path(directory)/'jorge'),jorge)
            self.assertEqual(other.counts,{})
            self.assertEqual(json.loads(other.tick(.2))['name'],'Jorge')
            self.assertEqual(AndroidBrain(str(Path(directory)/'bryan'),DEFINITION).counts['caress'],1)

    def test_snapshot_has_inspectable_original_network(self):
        with tempfile.TemporaryDirectory() as directory:
            brain=AndroidBrain(directory,DEFINITION)
            s=json.loads(brain.tick(.2))
            self.assertGreater(s['neurons'],400);self.assertGreater(s['synapses'],0)
            self.assertIn('CHEER',s['motors']);self.assertIn('POKE',s['habituation'])
            brain.save()
            self.assertTrue((Path(directory)/'snapshot.json').exists())

if __name__ == '__main__':
    unittest.main()
