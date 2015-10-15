import requests
import json


r = requests.post('http://localhost:5000/get_history_blob',
                  json=json.load(open('replay_config.json')))
print(r.json())
