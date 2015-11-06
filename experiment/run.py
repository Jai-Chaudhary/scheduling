import requests
import os
import json
import csv
import random

javaServer = 'http://localhost:4567'

config = json.load(open('exp.json'))

def go(seed, volunteerProbability, patientConfidenceLevel, advanceTime):
    config['seed'] = seed
    config['optimizer']['volunteerProbability'] = volunteerProbability
    config['optimizer']['patientConfidenceLevel'] = patientConfidenceLevel
    config['optimizer']['advanceTime'] = advanceTime

    state = requests.post(javaServer + '/parse_synthetic', json=config).json()
    result = requests.post(javaServer + '/simulate', json=state).json()

    return result['patients']

random.seed(0)
numReplication = 100
allVolunteerProbability = [0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6]
allPatientConfidenceLevel = [0, 0.7]
allAdvanceTime = [30, 60, 90]
allSeeds = [random.randint(0, 2**31) for i in range(numReplication)]

for volunteerProbability in allVolunteerProbability:
    for patientConfidenceLevel in allPatientConfidenceLevel:
        for advanceTime in allAdvanceTime:
            for seed in allSeeds:
                filename = 'data/{}_{}_{}_{}.csv'.format(volunteerProbability,
                                                         patientConfidenceLevel,
                                                         advanceTime,
                                                         seed)

                if os.path.isfile(filename) and os.stat(filename).st_size > 0:
                    continue

                patients = go(seed, volunteerProbability, patientConfidenceLevel, advanceTime)
                writer = csv.DictWriter(open(filename, 'w'), patients[0].keys())
                writer.writeheader()
                for p in patients:
                    writer.writerow(p)

            print('finished {} {} {}'.format(volunteerProbability, patientConfidenceLevel, advanceTime))
