import random

from util import coll, dist_coll, get_mins

lateness_offset = 23        # TODO magic number

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
    for x in schedule:
        p = {
            'name': x['mrn'],
            'clazz': '{}_{}'.format(x['service'], x['slot']),
            'appointment': get_mins(x['appointment_time']),
            'originalSite': x['site'],
            'durationDistribution': get_duration_distribution(x['service'], x['slot']),
            'latenessDistribution': get_lateness_distribution(),

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
    return ret

def get_sites(schedule):
    ret = {}
    for x in schedule:
        if x['site'] not in ret:
            ret[x['site']] = []
        if x['machine'] not in ret[x['site']]:
            ret[x['site']].append(x['machine'])
    return ret

def get_replay(req):
    day = req['day']
    seed = req['seed']
    volunteer_prob = req['optimizer']['volunteerProbability']
    random.seed(seed)

    schedule = list(coll.find({'day': day}))
    patients = get_patients(schedule, volunteer_prob)
    state = {
        'time': 0,
        'advanceTime': req['optimizer']['advanceTime'],
        'patients': patients,
        'sites': get_sites(schedule),
        'optimization': req['optimizer']['active'],
        'objective': req['optimizer']['objective'],
        'numSamples': req['optimizer']['numSamples'],
        'bitSeed': random.randint(0, 2**30),
        'confidenceLevel': req['optimizer']['confidenceLevel'],
        'patientConfidenceLevel': req['optimizer']['patientConfidenceLevel']
    }

    return state
