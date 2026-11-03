"""Native Android adapter over the original headless Pestinha modules."""
import json
import random
import time
import math
from care_state import CareState
from pathlib import Path
from pestinha.core.nervous_system import LIFBrain
from pestinha.core.emotion import EmotionState, EYE_SHAPES
from pestinha.core.homeostasis import Drives
from pestinha.core.motor import MotorTone
from pestinha.core.conditioning import Conditioning
from pestinha.core.episodic import EpisodicMemory
from pestinha.core.protesto import Protesto, STAGE_NAMES
from pestinha.config import PROFILES
from pestinha.store import read_json, write_json


class AndroidBrain:
    def __init__(self, memory_dir, definition):
        pet = json.loads(definition)
        self.name = pet['nome']
        self.root = Path(memory_dir)
        self.root.mkdir(parents=True, exist_ok=True)
        self.prof = dict(PROFILES['normal'])
        self.brain = LIFBrain(seed=pet['seed'], temperament=pet.get('temperamento', {}))
        self.brain.load(self.root / 'nervous.json')
        self.drives = Drives()
        self.drives.load(self.root / 'drives.json', self.prof)
        self.emotion = EmotionState()
        self.motor = MotorTone()
        self.conditioning = Conditioning(self.root / 'conditioning.json', self.prof['cond_rate'])
        self.conditioning.load()
        self.memory = EpisodicMemory(self.root / 'episodes.jsonl', self.root / 'preferences.json')
        self.memory.load()
        if self.name == 'Mimir':
            (self.root/'cartazes.txt').write_text(
                '[reclamacao]\nHmpf. Mais tato, menos cutucadas.\n'
                '[manifestacao]\nATÉ UM SÁBIO TEM LIMITES\n'
                '[greve]\nMEU CONSELHO ESTÁ EM GREVE\n'
                '[revolucao]\nPELOS GALHOS DE YGGDRASIL, CHEGA!\n'
                '[castigo]\nJá vi salões mais hospitaleiros em Jotunheim.\n'
                '[saudade]\nHmpf. Até este velho sente falta de companhia.\n'
                '[chorando]\nO salão ficou silencioso demais.\n'
                '[abandonado]\nAs sagas não se contam sozinhas, sabia?\n'
                '[volto]\nFinalmente. Sente-se, temos histórias a contar.\n', encoding='utf-8')
        self.protest = Protesto(self.root / 'cartazes.txt', self.root / 'protest.json')
        self.protest.load(self.prof)
        self.sad = Protesto(self.root / 'cartazes.txt', self.root / 'sad_protest.json', 'tristeza')
        self.sad_prof = dict(self.prof, grievance_up=self.prof['sad_up'], grievance_down=self.prof['sad_down'], stage_up=self.prof['sad_stage_up'])
        self.sad.load(self.sad_prof)
        data = read_json(self.root / 'android.json', {})
        self.care = CareState(data.get('care') if isinstance(data, dict) else None)
        counters = data.get('counts', {}) if isinstance(data, dict) else {}
        self.counts = {k: max(0, v) for k, v in counters.items() if type(v) is int} if isinstance(counters, dict) else {}
        durations = data.get('durations', {}) if isinstance(data, dict) else {}
        self.durations = {k: max(0.0, float(v)) for k, v in durations.items()
                          if isinstance(v, (int, float)) and math.isfinite(v)} if isinstance(durations, dict) else {}
        self.gaming = False
        self.events = {}
        self.asleep = False
        self.manual_sleep = False
        self.corner = False
        self.corner_until = 0
        self.rancor = 0.0
        self.escapes = data.get('escapes', 0) if isinstance(data, dict) and type(data.get('escapes')) is int else 0
        self.context = 'livre'
        self.notebook = False
        self.mischief = False
        self.as_fast = False
        self.last_touch = time.monotonic()
        self.elapsed = 0.0
        self.next_mischief = 120.0
        self.action_id = 0
        self.action = None
        self.last_sign = 0
        self.last_result = ''
        self.last_activity = {}
        self.pending_thing = None
        self.pending_since = 0
        self.dragging = False
        self.snapshot = {}
        self.paused_at = None

    def _action(self, kind, text=''):
        self.action_id += 1
        self.action = dict(id=self.action_id, kind=kind, text=text)

    def _note(self, force=None):
        if self.pending_thing is not None:
            return
        ctx = self.memory.context(self.context, self.emotion.current)
        allowed = ['bilhete', 'atravessar', 'nada']
        if (self.root/'custom-meme.png').exists(): allowed.append('meme')
        action = force or self.memory.choose(ctx, allowed, self.prof['explore'])
        if action in ('bilhete', 'meme'):
            self.pending_thing = object()
            self.pending_since = self.elapsed
            self.memory.start(ctx, action, self.pending_thing)
            phrases = ['Pausa para um carinho?', 'Estou de olho em você.', 'Bora brincar?', 'Seu pequeno caos está aqui.']
            if self.name == 'Luna':
                phrases = ['Uma ideia brilhante: pausa para brincar?', 'Estou aqui, uma estrelinha de cada vez.', 'Carinho recebido. Vou guardar esse momento.']
            if self.name in ('L.U.L.A', 'Lula (caricatura)'):
                phrases = ['Caricatura em serviço: minha pauta é a bolinha.', 'Uma pausa para o meu lanche virtual?', 'Este pet é uma caricatura fictícia, não uma fala real.']
            if self.name == 'Jorge':
                phrases = ['Minha mentoria custa um carinho.', 'Diversifique seus investimentos em bolinhas.', 'Retorno garantido: mais travessuras.']
            if self.name == 'Mimir':
                phrases = ['Pelos galhos de Yggdrasil... faça uma pausa antes de inventar outra tarefa.',
                           'Até Odin fazia perguntas. Tente uma boa, para variar.',
                           'Hmpf. Uma boa ideia. Não deixe que isso lhe suba ao elmo.',
                           'Nem as Nornas desembaraçam uma lista de tarefas desse tamanho.',
                           'Carinho? Está bem. Mas não amasse o elmo.',
                           'Thor tem um martelo. Você tem uma bolinha. Use-a com juízo.']
            self._action('image' if action == 'meme' else 'note', random.choice(phrases))
        elif action == 'atravessar':
            self.memory.start(ctx, action)
            self._action('roam')
        else:
            self.memory.start(ctx, 'nada')

    def event(self, kind):
        now = time.monotonic()
        if kind.startswith('care:'):
            result = self.care.act(kind.split(':', 1)[1])
            if result:
                self.counts[kind] = self.counts.get(kind, 0) + 1
                self.events['TOY_NEAR'] = .6
                self.emotion.force('contente', 2.0, priority=15)
                self._action('care', ('Hmpf. ' if self.name == 'Mimir' else '') + result)
            return
        if kind in ('game_start', 'game_end', 'game_hit', 'game_brick', 'game_life', 'game_win', 'game_over', 'game_launch'):
            if kind == 'game_start':
                self.gaming = True
                self.asleep = self.manual_sleep = False
                self.last_touch = now
            elif kind == 'game_end':
                self.gaming = False
            if kind not in ('game_end', 'game_launch'):
                self.counts[kind] = self.counts.get(kind, 0) + 1
            if kind in ('game_hit', 'game_brick', 'game_win', 'game_start'):
                self.events['TOY_NEAR'] = 1.0
                self.events['NOVELTY'] = .6
            return
        if kind == 'focus_start':
            self.context = 'trabalho'
            self.notebook = True
            self.asleep = self.manual_sleep = False
            return
        if kind == 'focus_end':
            self.context = 'livre'
            self.notebook = False
            return
        if kind == 'pause':
            self.paused_at = now
            return
        if kind == 'resume':
            if self.paused_at is not None:
                self.memory.deslocar(max(0, now-self.paused_at))
                self.paused_at = None
            return
        if kind.startswith('context:'):
            context = kind.split(':', 1)[1]
            if context in ('livre', 'trabalho', 'jogo', 'musica', 'social'):
                self.context = context
                self.events['NOVELTY'] = 1.0
            return
        if kind == 'fast':
            self.as_fast = not self.as_fast
            self.prof = dict(PROFILES['teste' if self.as_fast else 'normal'])
            self.sad_prof = dict(self.prof, grievance_up=self.prof['sad_up'], grievance_down=self.prof['sad_down'], stage_up=self.prof['sad_stage_up'])
            return
        if kind == 'mischief':
            self.mischief = not self.mischief
            self.next_mischief = self.elapsed + (8 if self.as_fast else 30)
            return
        if kind in ('note', 'meme'):
            if kind == 'meme' and not (self.root/'custom-meme.png').exists(): return
            self._note('meme' if kind == 'meme' else 'bilhete')
            return
        if kind == 'dismiss':
            if self.pending_thing is not None:
                self.memory.on_dismiss(self.pending_thing, self.elapsed-self.pending_since, self.prof['dismiss_patience'])
                self.pending_thing = None
            self.protest.repressed()
            self.events['DISMISS'] = 1.0
            return
        if kind == 'accepted':
            # A timed-out note is resolved by the original episodic tick, after
            # its display interval; closing it explicitly uses on_dismiss above.
            return
        if kind == 'press':
            self.conditioning.set_cue('PRESS', True, now)
            return
        if kind == 'release':
            self.conditioning.set_cue('PRESS', False, now)
            self.dragging = False
            return
        if kind == 'toy_near':
            self.events['TOY_NEAR'] = 1.3
            return
        if kind == 'toy_taken':
            self.events['TOY_TAKEN'] = 1.2
            return
        if kind == 'motion':
            self.events['MOTION'] = 1.0
            self.counts['motion'] = self.counts.get('motion', 0)+1
            return
        if kind == 'impact':
            self.events['IMPACT'] = .5
            return
        if kind == 'work':
            self.notebook = not self.notebook
            self.events['PEDIDO'] = 20.0
            if not self.notebook:
                self.context = 'livre'
            return
        if kind == 'corner':
            self.corner = not self.corner
            self.corner_until = self.elapsed + (15 if self.as_fast else 90)
            if self.corner:
                self.memory.punir(now, self.prof['episode_window'])
                self._action('sign', 'CANTINHO DO CASTIGO')
            return
        if kind == 'sleep':
            self.asleep = not self.asleep
            self.manual_sleep = self.asleep
            if self.asleep:
                self.memory.consolidate()
            return
        if kind == 'consolidate':
            self.memory.consolidate()
            self.save()
            return
        if kind not in ('poke', 'drag', 'caress', 'throw'):
            return
        self.counts[kind] = self.counts.get(kind, 0) + 1
        self.asleep = False
        self.manual_sleep = False
        self.last_touch = now
        self.events[kind.upper()] = 1.6 if kind == 'caress' else .8
        self.drives.stimulate(kind)
        self.drives.touched(kind)
        self.memory.on_interaction()
        if kind in ('poke', 'drag', 'throw'):
            self.conditioning.outcome(now)
        if kind == 'drag':
            self.dragging = True
        if kind == 'caress':
            self.emotion.force('contente', 2.0, priority=15)
            self.corner = False
            self.rancor = max(0, self.rancor-.2)

    def tick(self, dt):
        dt = min(.5, max(.02, float(dt)))
        self.elapsed += dt
        now = time.monotonic()
        self.conditioning.tick(now)
        self.drives.update(dt, self.prof, self.asleep, self.dragging,
                           self.emotion.arousal, now-self.last_touch > 60,
                           engaged=self.notebook, preso=self.corner)
        if self.drives.sleepy() and not self.asleep:
            self.asleep = True
            self.memory.consolidate()
        elif self.asleep and not self.manual_sleep and self.drives.rested():
            self.asleep = False
        channels = self.drives.channels()
        channels['SILENCE'] = .35
        if self.gaming:
            channels['PLAY'] = .8
        channels['ANTICIPATE'] = self.conditioning.strength()
        context_channels = {'trabalho': 'WORK', 'jogo': 'PLAY', 'musica': 'SOUND', 'social': 'SOCIAL'}
        if self.context in context_channels:
            channels[context_channels[self.context]] = .8
        if self.asleep:
            channels['FATIGUE'] = 1.0
        if self.corner:
            channels.update(PRESO=1.0, PLACA=1.0, RANCOR=self.rancor)
            self.rancor = min(1, self.rancor+dt/300)
        if self.notebook and not self.asleep:
            channels['WORK'] = .8
        reward = 0.0
        for name in list(self.events):
            if self.events[name] > 0:
                channels[name] = 1.0
                if name == 'CARESS': reward = 1.0
                if name == 'DISMISS': reward = -.6
                self.events[name] -= dt
            else:
                del self.events[name]
        self.brain.stimulate(channels)
        self.brain.step(dt*1000)
        self.brain.learn(reward, dt, self.prof['learn_rate'])
        activity = self.brain.motor_activity()
        self.last_activity = activity
        self.motor.update(activity, dt)
        emotion = self.emotion.update(activity, dt, self.prof)
        caring = self.events.get('CARESS', 0) > 0
        self.protest.update(dt, max(0, -self.emotion.valence) if emotion == 'bravo' else 0, self.prof, caring, now)
        self.sad.update(dt, max(0, -self.emotion.valence) if emotion == 'triste' else 0, self.sad_prof, caring, now)
        if self.corner and (self.elapsed >= self.corner_until or activity.get('FUGIR', 0) > 9):
            self.corner = False
            self.escapes += 1
            self._action('sign', 'Saí do cantinho!')
        active = self.protest if self.protest.stage else self.sad
        if self.mischief and active.stage and self.elapsed-self.last_sign > 15:
            self._action('sign', active.escolher_cartaz()['texto'])
            self.last_sign = self.elapsed
        if self.mischief and not self.gaming and not self.asleep and not self.corner and self.elapsed >= self.next_mischief:
            self._note()
            interval = 10 if self.as_fast else max(45, 300/(1+activity.get('TRAVESSURA', 0)))
            self.next_mischief = self.elapsed+interval
        self.memory.tick(now, 18, self.prof['episode_window'])
        if self.pending_thing is not None and not any(e['thing'] is self.pending_thing for e in self.memory.open):
            self.pending_thing = None
        for key in ['awake_seconds' if not self.asleep else 'sleep_seconds',
                    'context_' + self.context] + (['notebook_seconds'] if self.notebook else []) + (['corner_seconds'] if self.corner else []):
            self.durations[key] = self.durations.get(key, 0.0) + dt
        self.care.tick(dt)
        self.snapshot = {
            'care': self.care.snapshot(), 'name': self.name, 'emotion': 'dormindo' if self.asleep else emotion,
            'face': EYE_SHAPES['dormindo' if self.asleep else emotion], 'caressing': caring,
            'energy': self.drives.energy, 'stimulation': self.drives.stimulation,
            'contact': self.drives.contact, 'speed': 0 if self.asleep or self.corner or self.notebook else self.motor.speed(),
            'drive': 0 if self.asleep or self.corner or self.notebook else self.motor.drive(),
            'valence': self.emotion.valence, 'arousal': self.emotion.arousal,
            'motors': activity, 'learned': self.brain.learned_summary(12),
            'habituation': self.brain.efficacy(), 'conditioning': self.conditioning.V,
            'preferences': self.memory.preferences(12), 'recent': self.memory.recent[-15:],
            'durations': self.durations, 'counts': self.counts, 'neurons': self.brain.N, 'synapses': int(self.brain.n_synapses),
            'protest': STAGE_NAMES[self.protest.stage], 'grievance': self.protest.grievance,
            'sad_stage': self.sad.stage, 'corner': self.corner, 'escapes': self.escapes,
            'gaming': self.gaming, 'notebook': self.notebook, 'mischief': self.mischief, 'fast': self.as_fast,
            'context': self.context, 'action': self.action,
        }
        return json.dumps(self.snapshot)

    def rest(self, seconds):
        seconds = max(0, min(60, float(seconds)))
        self.drives.energy = min(1, self.drives.energy+seconds/self.prof['sleep_recover'])
        self.drives.stimulation = max(0, self.drives.stimulation-seconds/self.prof['stim_decay'])
        self.drives.contact = min(1, self.drives.contact+.3*seconds/self.prof['contact_span'])

    def save(self):
        self.brain.save(self.root/'nervous.json')
        self.drives.save(self.root/'drives.json')
        self.conditioning.save()
        self.protest.save()
        self.sad.save()
        self.memory.save()
        self.memory._trim_file(self.memory._read_episodes())
        write_json(self.root/'android.json', {'care': self.care.snapshot(), 'durations': self.durations, 'counts': self.counts, 'escapes': self.escapes, 'saved_at': time.time()})
        if self.snapshot:
            write_json(self.root/'snapshot.json', dict(self.snapshot, saved_at=time.time()))
