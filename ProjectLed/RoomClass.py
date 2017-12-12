from __future__ import print_function
import sys
import json
class Channel:
    def __init__(self, pin):
	self.value = pin
        self.pin = pin

class Room:
    def __init__(self, room):
        if isinstance(room, basestring):
            room = json.loads(room)
	self.name = room['name']
	self.R = Channel(room['R'])
	self.G = Channel(room['G'])
	self.B = Channel(room['B'])
        if 'effect' in room:
            self.effect = room['effect']
        else:
            self.effect = None

    def toJSON(self):
        return '{"name": "' + self.name + '", "R": ' + str(self.R.value) + ', "G": ' + str(self.G.value) + ', "B": ' + str(self.B.value) + ', "effect": "' + self.effect + '"}'
