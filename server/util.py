import pymongo
import datetime

db = pymongo.MongoClient().schedule
coll = db.history_schedule
dist_coll = db.distribution

def get_mins(dt):
    date = datetime.datetime.combine(dt.date(), datetime.time.min)
    return round((dt - date).total_seconds() / 60)
