import json
class Channel:
    def __init__(self, var, is_value):
        if is_value:
	    self.value = var
        else:
            self.value = 0
            self.pin = var

class Room:
    def __init__(self, room, is_value):
        if isinstance(room, basestring):
            room = json.loads(room)
	self.name = room['name']
	self.R = Channel(room['R'], is_value)
	self.G = Channel(room['G'], is_value)
	self.B = Channel(room['B'], is_value)
        if 'effect' in room:
            self.effect = room['effect']
        else:
            self.effect = None
    
    def to_dict(self):
        d = {}
        d['name'] = self.name
        d['R'] = self.R.value
        d['G'] = self.G.value
        d['B'] = self.B.value
        d['effect'] = self.effect
        return d

    def toJSON(self):
        return '{"name": "' + self.name + '", "R": ' + str(self.R.value) + ', "G": ' + str(self.G.value) + ', "B": ' + str(self.B.value) + ', "effect": "' + self.effect + '"}'
