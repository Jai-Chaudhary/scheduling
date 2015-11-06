import json

offset = 2

config = json.load(open('template.json'))
for pc in config['patientClasses']:
    pc['durationDistribution']['base'] -= offset
    print(pc['durationDistribution']['base'])

json.dump(config, open('exp.json', 'w'), indent=4)
