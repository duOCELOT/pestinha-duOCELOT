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


if __name__ == '__main__':
    unittest.main()
