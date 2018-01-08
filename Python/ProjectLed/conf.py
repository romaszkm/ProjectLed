import json
from RoomClass import Room
import os
config_file = os.path.join(os.path.dirname(__file__), '../config.json')

def get_rooms():
    data = json.load(open(config_file))
    rooms = {}
    for room_data in data['rooms']:
	rooms[room_data['name']] = (Room(room_data))
    return rooms

def get_effect_config(name):
    data = json.load(open(config_file))
    if name in data['effects']:
        return data['effects'][name]
