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
        room = Room(room)
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

#def init():
    #pobieranie configa
#    global RoomsData
#    RoomsData = conf.getRooms()
#    #tworzenie kontrolera led
#    global ledController
#    ledController = LedController(RoomsData)
#    #tworzenie listenera
#    comm = CommunicationModule(getRoomsData, setRoomState)
#    comm.run()
	
#def getRoomsData():
#    return RoomsData
	
	
#def setRoomState(room):
#    room = Room(room)
#    #sprawdzenie czy istnieje taki pokoj
#    if room.name not in RoomsData:
#        return 400 #ktos kombinuje z nazwami pokoi - nie ma co zwracac
#    try:
#       room.R.pin = RoomsData[room.name].R.pin
#       room.G.pin = RoomsData[room.name].G.pin
#       room.B.pin = RoomsData[room.name].B.pin
#       ledController.setRoom(room)
#   except NameError: #ktos kombinuje z nazwami pokoi - nie ma co zwracac; nie powinno sie wykonac
#       return 400 
#   except NotImplementedError: #nie udalo sie zmienic - zwraca to co jest
#       return 400
#   return 200

if __name__ == '__main__':
    app = MainController()
