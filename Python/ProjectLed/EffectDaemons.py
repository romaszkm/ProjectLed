from RoomClass import Room
import threading
import time
import pigpio
from conf import get_effect_config

class ConstantEffect:
    def __init__(self, room):
        self.is_active = False
        self.room = room
    def start(self):
        set_values(self.room)
	
class BreathingEffect (threading.Thread):	
    def __init__(self, room):
        threading.Thread.__init__(self)
        self.is_active = True
	self.room = room
    def run(self):
        conf = get_effect_config('breathing')
	delay = conf['delay']
        steps = conf['steps']
	delta_R = self.room.R.value / float(steps)
	delta_G = self.room.G.value / float(steps)
	delta_B = self.room.B.value / float(steps)
        while self.is_active:
            for i in range(0,steps):
	        if self.is_active == False:
	            break
                self.room.R.value -= delta_R
		self.room.G.value -= delta_G
		self.room.B.value -= delta_B
		set_values(self.room)
		time.sleep(delay)
	    for i in range(0,steps):
	        if self.is_active == False:
	            break
                self.room.R.value += delta_R
		self.room.G.value += delta_G
		self.room.B.value += delta_B
		set_values(self.room)
		time.sleep(delay)
    def stop(self):
        self.is_active = False
        time.sleep(0.2)
	
class SwipeEffect (threading.Thread):
    def __init__(self, room):
        threading.Thread.__init__(self)
        self.is_active = True
	self.room = room
    def run(self):
        conf = get_effect_config('swipe')
	delay = conf['delay']
        steps = conf['steps']
        self.room.R.value = 250
	self.room.G.value = 0
	self.room.B.value = 0
        delta = 250 / float(steps)
	while self.is_active:
	    for i in range (0,steps):
	        if self.is_active == False:
	            break
	        time.sleep(delay)
                self.room.R.value -= delta
                self.room.G.value += delta
                set_values(self.room)
            for i in range (0,steps):
                if self.is_active == False:
                    break
	        time.sleep(delay)
                self.room.G.value -= delta
                self.room.B.value += delta
                set_values(self.room)
	    for i in range (0,steps):
    	        if self.is_active == False:
                    break
		time.sleep(delay)
                self.room.B.value -= delta
                self.room.R.value += delta
                set_values(self.room)
    def stop(self):
        self.is_active = False
        time.sleep(0.2)

pi = pigpio.pi()		
def set_values(room):
    #print('R - {} G - {} B - {}\n'.format(room.R.value, room.G.value, room.B.value))
    pi.set_PWM_dutycycle(room.R.pin, room.R.value)
    pi.set_PWM_dutycycle(room.G.pin, room.G.value)
    pi.set_PWM_dutycycle(room.B.pin, room.B.value)
