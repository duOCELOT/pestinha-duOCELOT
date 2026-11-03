"""Fictional pet care, advancing only during active simulation; no offline punishment."""
import math

class CareState:
    def __init__(self, raw=None):
        raw = raw if isinstance(raw, dict) else {}
        def number(key, default, low, high):
            value = raw.get(key, default)
            return max(low, min(high, float(value))) if type(value) in (int, float) and math.isfinite(value) else default
        self.hunger = number('hunger', .2, 0, 1)
        self.illness = number('illness', 0, 0, 1)
        self.temperature = number('temperature', 0, -1, 1)
        self.ambient = number('ambient', 0, -1, 1)

    def tick(self, seconds):
        self.hunger = min(1, self.hunger + seconds / 7200)
        self.temperature += (self.ambient - self.temperature) * min(1, seconds / 300)
        if self.hunger > .85 or abs(self.temperature) > .8:
            self.illness = min(1, self.illness + seconds / 18000)
        else:
            self.illness = max(0, self.illness - seconds / 36000)

    def act(self, action):
        if action == 'feed':
            self.hunger = max(0, self.hunger - .35)
            return 'Lanchinho servido!'
        if action == 'treat':
            self.illness = max(0, self.illness - .3)
            return 'Cuidado e descanso. É uma simulação, não orientação médica.'
        if action == 'warm':
            self.ambient = 0
            self.temperature = min(0, self.temperature + .4) if self.temperature < 0 else self.temperature
            return 'Cobertor preparado.'
        if action == 'cool':
            self.ambient = 0
            self.temperature = max(0, self.temperature - .4) if self.temperature > 0 else self.temperature
            return 'Uma sombra e uma brisa.'
        if action in ('cold', 'hot', 'mild'):
            self.ambient = {'cold': -.9, 'hot': .9, 'mild': 0}[action]
            return 'Clima de brincadeira ajustado. Não é um sensor do aparelho.'
        return None

    def snapshot(self):
        return dict(hunger=self.hunger, illness=self.illness, temperature=self.temperature, ambient=self.ambient)
