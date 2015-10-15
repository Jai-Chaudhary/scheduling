import requests
import json

r = requests.post('http://localhost:5000/get_history_schedule',
                  json={'day': '2013-04-15'})
print(r.json())
