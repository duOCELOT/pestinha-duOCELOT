"""Android adapter. Original LIF network and learning, with touch-only senses."""
import json
import time
from pathlib import Path

from pestinha.core.nervous_system import LIFBrain
from pestinha.core.emotion import EmotionState
from pestinha.core.homeostasis import Drives
from pestinha.core.motor import MotorTone
from pestinha.config import PROFILES


class AndroidBrain:
    def __init__(self, memory_dir, definition):
        pet = json.loads(definition)
        self.root = Path(memory_dir)
        self.root.mkdir(parents=True, exist_ok=True)
        self.prof = PROFILES['normal']
        self.brain = LIFBrain(seed=pet['seed'], temperament=pet.get('temperamento', {}))
        self.brain.load(self.root / 'nervous.json')
        self.drives = Drives()
        self.drives.load(self.root / 'drives.json', self.prof)
        self.emotion = EmotionState()
        self.motor = MotorTone()
        self.events = {}
        self.asleep = False
        self.last_touch = time.monotonic()

    def event(self, kind):
        if kind == 'sleep':
            self.asleep = not self.asleep
            return
        if kind not in ('poke', 'drag', 'caress', 'throw'):
            return
        self.asleep = False
        self.last_touch = time.monotonic()
        self.events[kind.upper()] = 1.6 if kind == 'caress' else 0.8
        self.drives.stimulate(kind)
        self.drives.touched(kind)
        if kind == 'caress':
            self.emotion.force('contente', 2.0, priority=15)

    def tick(self, dt):
        dt = min(0.5, max(0.02, float(dt)))
        self.drives.update(dt, self.prof, self.asleep, False,
                           self.emotion.arousal, time.monotonic() - self.last_touch > 60)
        if self.drives.sleepy():
            self.asleep = True
        elif self.asleep and self.drives.rested():
            self.asleep = False
        channels = self.drives.channels()
        channels['SILENCE'] = 0.35
        if self.asleep:
            channels['FATIGUE'] = 1.0
        reward = 0.0
        for name in list(self.events):
            if self.events[name] > 0:
                channels[name] = 1.0
                if name == 'CARESS':
                    reward = 1.0
                self.events[name] -= dt
            else:
                del self.events[name]
        self.brain.stimulate(channels)
        self.brain.step(dt * 1000)
        self.brain.learn(reward, dt, self.prof['learn_rate'])
        activity = self.brain.motor_activity()
        self.motor.update(activity, dt)
        emotion = self.emotion.update(activity, dt, self.prof)
        return json.dumps({
            'emotion': 'dormindo' if self.asleep else emotion,
            'energy': self.drives.energy,
            'speed': 0.0 if self.asleep else self.motor.speed(),
            'drive': 0.0 if self.asleep else self.motor.drive(),
            'valence': self.emotion.valence,
        })

    def save(self):
        self.brain.save(self.root / 'nervous.json')
        self.drives.save(self.root / 'drives.json')
