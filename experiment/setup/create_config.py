import json
import sys

top_scans = 15

def expectation(d):
    return sum(k*x for k, x in enumerate(d['pmf'])) + d['base']

dist = json.load(open('dist.json'))
config = json.load(open('example_config.json'))

scans = [
    dist[i]['name']
    for i in range(1, top_scans+1)
]

pc = [
    {
        'name': d['name'].split('_')[1],
        'percent': d['n'],
        'durationDistribution': d['name'],
#       'latenessDistribution': 'Lateness',
        'latenessDistribution': 'uniform(0,0)',
#       'slot': int(d['name'].split('_')[2])
        'slot': round(expectation(d) / 15) * 15
    } for d in dist if d['name'] in scans
]

sumn = sum(x['percent'] for x in pc)
for p in pc:
    p['percent'] /= sumn

config['patient']['classes'] = pc

if len(sys.argv) > 1 and sys.argv[1] == '7sites':
    config['sites'] = {
        "Site1": {
            "machines": ["MR1"],
            "horizon": {
                "begin": 450,
                "end": 1320
            }
        },
        "Site2": {
            "machines": ["MR1"],
            "horizon": {
                "begin": 450,
                "end": 1320
            }
        },
        "Site3": {
            "machines": ["MR1"],
            "horizon": {
                "begin": 450,
                "end": 1320
            }
        },
        "Site4": {
            "machines": ["MR1"],
            "horizon": {
                "begin": 450,
                "end": 1320
            }
        },
        "Site5": {
            "machines": ["MR1"],
            "horizon": {
                "begin": 450,
                "end": 1320
            }
        },
        "Site6": {
            "machines": ["MR1"],
            "horizon": {
                "begin": 450,
                "end": 1320
            }
        },
        "Site7": {
            "machines": ["MR1"],
            "horizon": {
                "begin": 450,
                "end": 1320
            }
        }
    }

json.dump(config, open('../exp.json', 'w'), indent=4)
