import json
import threading
import uuid
from RoomClass import Room
from flask import Flask, request
from bluetooth import *

class CommunicationModule:
    def __init__(self, main_controller):
        self.main_controller = main_controller
        self.rest_module = RestModule(self)
	self.bt_module = BTModule(self)
        #self.rest_module = None
	#self.bt_module = None
    
    def run(self):
        if self.rest_module is not None:
	    self.rest_module.start()
	if self.bt_module is not None:
	    self.bt_module.start()

    def set(self, data):
        #TODO flagi dla blokowania jednoczesnych requestow albo ich kolejkowanie
	self.main_controller.set_room(data)

    def get(self):
        return self.main_controller.rooms


class RestModule(threading.Thread):
    def __init__(self, father_controller):
        threading.Thread.__init__(self)
        self.father_controller = father_controller
    
    def run(self):
        app = Flask('rest')

        @app.route('/rooms/set', methods=['POST'])
        def set():
            data = request.data
            if not data:
                data = request.form.keys()[0]
            code = self.father_controller.set(data)
            return 'Operation result {}'.format(code), code 
            
        @app.route('/rooms/get')
        def get():
            response = toJson(self.father_controller.get())
            print(response)
            return response, 200

        app.run(port=2525, host='0.0.0.0', debug=False, use_reloader=False)

class BTModule(threading.Thread):
    def __init__(self, father_controller):
        threading.Thread.__init__(self)
        self.father_controller = father_controller

    def run(self):
        server_sock = BluetoothSocket(RFCOMM)
        server_sock.bind(("", PORT_ANY))
        server_sock.listen(1)
        port = server_sock.getsockname()[1]
        id = 'f4eee4c3-8d72-479d-b6ec-41e960ad0967'
        advertise_service(server_sock, 'LedControlServer',
                service_id = id,
                service_classes = [id, SERIAL_PORT_CLASS],
                profiles = [SERIAL_PORT_PROFILE]
                )
        while True:
            print('Waiting for connection')
            client_sock, client_info = server_sock.accept()
            print('Connection from ', client_info)
            client_sock.send(toJson(self.father_controller.get()))
            try:
                while True:
                    data = client_sock.recv(1024)
                    if len(data) == 0:
                        break
                    self.father_controller.set(data)
                    print('received [%s]' % data)
            except IOError:
                pass
            print('Disconnected')

def toJson(rooms):
    array = []
    for room in rooms:
        array.append(rooms[room].to_dict())
    return json.dumps({'rooms':array})
