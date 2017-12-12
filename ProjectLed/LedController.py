from RoomClass import Room
from EffectDaemons import ConstantEffect, BreathingEffect, SwipeEffect
from copy import deepcopy

class LedController:
    def __init__(self, rooms):
        self.rooms = {}
	for room in rooms:
	    self.rooms[room] = None
	
    def set(self, room):
        name = room.name
        #teoretycznie nadmiarowe sprawdzenie
        if name in self.rooms:
	    if self.rooms[name] is not None:
	        if self.rooms[name].is_active:
		    #zatrzymanie istniejacego watku efektu
		    self.rooms[name].stop()
	    self.rooms[name] = self._effect_daemon(deepcopy(room))
            self.rooms[name].start()
	else:
            raise NameError
			
			
    def _effect_daemon(self, room):
        #tworzenie odpowiedniego daemona zgodnie z room.effect
        if room.effect == 'constant' or room.effect is None:
	    return ConstantEffect(room)
	elif room.effect == 'breathing':
	    return BreathingEffect(room)
	elif room.effect == 'swipe':
	    return SwipeEffect(room)
	else:
	    raise NotImplementedError
