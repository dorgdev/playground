import time

EVENT_MEGI_LEFT = 'megi left'
EVENT_MEGI_BACK = "megi's back"
EVENT_AWAY = 'away'
EVENT_BACK_HOME = "back home"
EVENT_DIAPER = 'diaper'
EVENT_PUTTING_TO_SLEEP = 'putting to sleep'
EVENT_SLEPT = 'slept'
EVENT_WOKE_UP = 'woke up'
EVENT_LUNCH = 'lunch'
EVENT_FRUITS = 'fruits'
EVENT_ICE_CREAM = 'ice cream'
EVENT_BOTTLE = 'bottle'
EVENT_BOILED_WATER = 'boiled water'
EVENT_FAILED_PUTTING_TO_SLEEP = 'failed putting to sleep'

EVENT_SUBTYPE_LIGHT_DIAPER = "light"
EVENT_SUBTYPE_HEAVY_DIAPER = "heavy"

ALL_EVENTS = set([
    EVENT_MEGI_LEFT, EVENT_DIAPER, EVENT_PUTTING_TO_SLEEP, EVENT_SLEPT, EVENT_WOKE_UP,
    EVENT_LUNCH, EVENT_FRUITS, EVENT_ICE_CREAM, EVENT_BOTTLE, EVENT_BOILED_WATER,
    EVENT_FAILED_PUTTING_TO_SLEEP, EVENT_MEGI_BACK, EVENT_AWAY, EVENT_BACK_HOME])

class Event:
    def __init__(self, line = '', previous_date = 0):
        self.date = previous_date
        self.ts = 0
        self.type = ''
        self.subtype = ''
        self.from_line(line)

    def from_line(self, line):
        global ALL_EVENTS
        if len(line) == 0:
            return
        args = line.split('\t')
        if args[0] != '':
            self.date = args[0]
        event_time = args[1]
        self.ts = time.mktime(time.strptime(self.date + ' ' + event_time, '%d/%m/%y %H:%M'))
        self.type = args[2].lower()
        if self.type not in ALL_EVENTS:
            print "WARNING: unknown event type: %s" % (self.type)
        self.subtype = self.calc_subtype("" if len(args) < 4 else args[3]).lower()

    def calc_subtype(self, subtype):
        if subtype == '':
            return ''
        if self.type in ['Fruits', 'Lunch']:
            sa = subtype.split(', ')
            sa.sort()
            return ", ".join(sa)
        return subtype

    def get_date(self):
        return self.date

    def get_ts(self):
        return self.ts

    def get_type(self):
        return self.type

    def get_subtype(self):
        return self.subtype
