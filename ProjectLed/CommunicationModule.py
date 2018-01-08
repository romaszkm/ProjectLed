import json
import threading
from RoomClass import Room
from flask import Flask, request

class CommunicationModule:
    def __init__(self, main_controller):
        self.main_controller = main_controller
        self.rest_module = RestModule(self)
	self.bt_module = None
    
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
            return response, 200

        def toJson(rooms):
            array = []
            for room in rooms:
                array.append(rooms[room].to_dict())
            return json.dumps({'rooms':array})


        app.run(port=2525, host='0.0.0.0', debug=False, use_reloader=False)
