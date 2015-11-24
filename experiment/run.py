import requests
import os
import json
import csv
import random

javaServer = 'http://localhost:4567'

config = json.load(open('exp.json'))

def go(seed, volunteerProbability, patientConfidenceLevel, advanceTime):
    config['seed'] = seed
    config['patient']['volunteerProbability'] = volunteerProbability
    config['optimizer']['patientConfidenceLevel'] = patientConfidenceLevel
    config['optimizer']['advanceTime'] = advanceTime

    state = requests.post(javaServer + '/parse_synthetic', json=config).json()
    result = requests.post(javaServer + '/simulate', json=state).json()

    return result

random.seed(0)
numReplication = 100
allVolunteerProbability = [0]
allPatientConfidenceLevel = [0.7]
allAdvanceTime = [60]
allSeeds = [random.randint(0, 2**31) for i in range(numReplication)]

for volunteerProbability in allVolunteerProbability:
    for patientConfidenceLevel in allPatientConfidenceLevel:
        for advanceTime in allAdvanceTime:
            for seed in allSeeds:
                filename = 'data/wait/{}.csv'.format(seed)

                if os.path.isfile(filename) and os.stat(filename).st_size > 0:
                    continue

                result = go(seed, volunteerProbability, patientConfidenceLevel, advanceTime)
                patients = result['patients']
                overTime = result['overTime']

                writer = csv.DictWriter(open(filename, 'w'), patients[0].keys())
                writer.writeheader()
                for p in patients:
                    writer.writerow(p)

                filename = 'data/overtime/{}.csv'.format(seed)
                writer = csv.DictWriter(open(filename, 'w'), ['site', 'overtime'])
                writer.writeheader()
                for s in overTime:
                    writer.writerow({'site': s, 'overtime': overTime[s]})

            print('finished {} {} {}'.format(volunteerProbability, patientConfidenceLevel, advanceTime))
