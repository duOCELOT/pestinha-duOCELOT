import unittest
from care_state import CareState
class CareTest(unittest.TestCase):
    def test_feed_treatment_and_temperature_are_bounded(self):
        c=CareState(dict(hunger=.9,illness=.5,temperature=-.9))
        c.act('feed');c.act('treat');c.act('warm')
        self.assertAlmostEqual(c.hunger,.55);self.assertAlmostEqual(c.illness,.2)
        self.assertGreater(c.temperature,-.9)
        for _ in range(50):c.act('feed');c.act('treat');c.act('warm')
        self.assertEqual(c.hunger,0);self.assertEqual(c.illness,0);self.assertEqual(c.temperature,0)
    def test_saved_state_and_invalid_data(self):
        c=CareState(dict(hunger=float('nan'),temperature=99,illness='bad'))
        self.assertEqual(c.hunger,.2);self.assertEqual(c.temperature,1)
        self.assertEqual(CareState(c.snapshot()).snapshot(),c.snapshot())
    def test_active_tick_only(self):
        c=CareState();before=c.hunger;c.tick(.2);self.assertGreater(c.hunger,before)
        c.act('cold');c.tick(300);self.assertLess(c.temperature,0)
