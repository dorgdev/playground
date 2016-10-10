#from event import *
from event_sm import *
from event_parser import *
from db_helper import *
from queries import *

DBH = DBHelper()

def run(filename = '/tmp/pat.tsv'):
    global DBH
    parser = EventFileParser(filename)
    events = parser.parse()
    build_event_sm(events)
    return Queries(DBH)


