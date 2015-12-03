import requests
import os
import json
import csv
import random
import sys

javaServer = 'http://localhost:4567'

config = json.load(open('exp.json'))

def go(seed, volunteerProbability, advanceTime):
    config['seed'] = seed
    config['patient']['volunteerProbability'] = volunteerProbability
    config['optimizer']['advanceTime'] = advanceTime

    state = requests.post(javaServer + '/parse_synthetic', json=config).json()
    result = requests.post(javaServer + '/simulate', json=state).json()

    return result

config['patient']['cancelProbability'] = 0
config['patient']['SDAOPRate'] = 0

random.seed(0)
numReplication = 100
allVolunteerProbability = [0, .1, .2, .3, .4, .5, .6]
allAdvanceTime = [60, 90]
allSeeds = [random.randint(0, 2**31) for i in range(numReplication)]

for volunteerProbability in allVolunteerProbability:
    for advanceTime in allAdvanceTime:
        for seed in allSeeds:
            filename = '{}_{}_{}.csv'.format(volunteerProbability, advanceTime, seed)

            wait_file = 'data/wait/{}'.format(filename)
            overtime_file = 'data/overtime/{}'.format(filename)

            if os.path.isfile(wait_file) and os.stat(wait_file).st_size > 0:
                continue

            result = go(seed, volunteerProbability, advanceTime)
            patients = result['patients']
            overTime = result['overTime']

            writer = csv.DictWriter(open(wait_file, 'w'), patients[0].keys())
            writer.writeheader()
            for p in patients:
                writer.writerow(p)

            writer = csv.DictWriter(open(overtime_file, 'w'), ['site', 'overtime'])
            writer.writeheader()
            for s in overTime:
                writer.writerow({'site': s, 'overtime': overTime[s]})

        print('finished {} {}'.format(volunteerProbability, advanceTime))
