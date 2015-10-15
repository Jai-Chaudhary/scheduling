from util import get_mins, coll

def get_patients(schedule):
    ret = []
    for x in schedule:
        p = {
            'name': x['mrn'],
            'clazz': '{}_{}'.format(x['service'], x['slot']),
            'appointment': get_mins(x['appointment_time']),

            'site': x['site'],
            'machine': x['machine'],
            'arrival': get_mins(x['arrival_time']),
            'begin': get_mins(x['begin_time']),
            'completion': get_mins(x['completion_time']),

            'slot': x['slot']
        }
        ret.append(p)
    return ret

def get_history(req):
    day = req['day']
    schedule = list(coll.find({'day': day}))
    schedule = get_patients(schedule)
    return {'schedule': schedule}
