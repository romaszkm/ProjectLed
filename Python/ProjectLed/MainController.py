from RoomClass import Room
from LedController import LedController
from CommunicationModule import CommunicationModule
import conf

class MainController:
    def __init__(self):
        self.rooms = conf.get_rooms()
        self.led_controller = LedController(self.rooms)
        self.comm_module = CommunicationModule(self)
        self.comm_module.run()

    def set_room(self, room):
        room = Room(room, True)
        name = room.name
        if room.name not in self.rooms:
            return 400
        try:
            room.R.pin = self.rooms[name].R.pin
            room.G.pin = self.rooms[name].G.pin
            room.B.pin = self.rooms[name].B.pin
            self.led_controller.set(room)
        except NameError:
            return 400
        except NotImplementedError:
            return 400
        return 200

if __name__ == '__main__':
    app = MainController()
