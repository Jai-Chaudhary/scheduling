import requests
import json
import sys

config = ''.join(open('../example_config.json'))
stateStr = requests.post('http://localhost:4567/configparser',
                         data={'data': config}).json()['data']
framesStr = requests.post('http://localhost:4567/simulate_frames',
                          data={'data':stateStr, 'step':1}).json()['data']
frames = json.loads(framesStr)
print(framesStr)
print(frames[-1])
print(len(frames))


#while True:
#    r = requests.post('http://localhost:4567/simulate',
#                      data={'data': json.dumps(state), 'until': state['time'] + 1})
#    state = json.loads(r.json()['data'])
#    # print(state['time'])
#    if r.json()['finished']:
#        break
