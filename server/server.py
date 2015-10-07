import flask
import pymongo
import datetime
import random
import json

lateness_offset = 23        # TODO magic number

db = pymongo.MongoClient().schedule
coll = db.history_schedule
dist_coll = db.distribution
app = flask.Flask(__name__)
app.debug = True

def get_mins(dt):
    date = datetime.datetime.combine(dt.date(), datetime.time.min)
    return round((dt - date).total_seconds())

def get_duration_distribution(service, slot):
    d = dist_coll.find_one({'name': 'Duration {} {}'.format(service, slot)})
    if d == None:
        d = dist_coll.find_one({'name': 'Duration {}'.format(slot)})
    if d == None:
        print('hello')
        return 'uniform({},{})'.format(slot-5, slot+5)
    return {
        'name': d['name'],
        'base': d['base'],
        'pmf': d['pmf']
    }

def get_lateness_distribution():
    d = dist_coll.find_one({'name': 'Lateness'})
    return {
        'name': 'Lateness',
        'base': d['base'] + lateness_offset,
        'pmf': d['pmf']
    }

def get_patients(schedule, volunteer_prob):
    ret = []
    nid = 0
    for x in schedule:
        p = {
            'name': x['mrn'],
            'clazz': '{}_{}'.format(x['service'], x['slot']),
            'appointment': get_mins(x['appointment_time']),
            'originalSite': x['site'],
            'durationDistribution': get_duration_distribution(x['service'], x['slot']),
            'latenessDistribution': get_lateness_distribution(),
            'id': nid,

            'site': x['site'],
            'machine': None,
            'arrival': None,
            'begin': None,
            'completion': None,

            'optimized': None,
            'volunteer': random.random() < volunteer_prob,

            'duration': x['duration'],
            'lateness': x['lateness'] + lateness_offset,
        }
        ret.append(p)
        nid += 1
    return ret

def get_sites(schedule):
    ret = {}
    for x in schedule:
        if x['site'] not in ret:
            ret[x['site']] = []
        if x['machine'] not in ret[x['site']]:
            ret[x['site']].append(x['machine'])
    return ret

def get_lBits(patients, num_samples):
    return [
        {
            "duration": {
                p['name']: random.random()
                for p in patients
            },
            "lateness": {
                p['name']: random.random()
                for p in patients
            }
        }
        for i in range(num_samples)
    ]


@app.route('/get_history_blob', methods=['POST'])
def get_history_blob():
    req = flask.request.get_json()
    day = req['day']
    seed = req['seed']
    volunteer_prob = req['optimizer']['volunteerProbability']
    random.seed(seed)

    schedule = list(coll.find({'day': day}))
    patients = get_patients(schedule, volunteer_prob)
    state = {
        'time': 0,
        'advancedTime': req['optimizer']['advanceTime'],
        'patients': patients,
        'sites': get_sites(schedule),
        'optimization': req['optimizer']['active'],
        'objective': req['optimizer']['objective']
    }
    blob = {
        'state': state,
        'lBits': get_lBits(patients, req['num_samples'])
    }
    return flask.jsonify({'data': json.dumps(blob)})

#TODO use config instead of hacks

app.run(host='0.0.0.0')
