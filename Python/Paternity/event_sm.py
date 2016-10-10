from db_helper import DBHelper
from event import Event

class EventSM:
    AWAKE = "awake"
    PUTTING_TO_SLEEP = "put to sleep"
    SLEEPING = "sleeping"

    def __init__(self):
        self.db_helper = DBHelper()
        self._init_counters()
        self._init_callbacks()
        self._alone_time = 0
        self._where_away = None
        self._away_ts = 0

    def _init_counters(self):
        self.state = EventSM.AWAKE
        self.last_rec_ts = 0
        self.accum_sleeping_time = 0
        self.accum_put_to_sleep_time = 0
        self.days = set()

    def _init_callbacks(self):
        self.cb = {
            'putting to sleep': self._put_to_sleep,
            'failed putting to sleep': self._failed_to_put_to_sleep,
            'slept': self._fell_asleep,
            'woke up': self._woke_up,
            'megi left': self._alone,
            'megi\'s back': self._not_alone,
            'away': self._away,
            'back home': self._back_home
        }

    def _add_sleep_state(self, old_state, end_ts, new_state):
        success = not (old_state == EventSM.PUTTING_TO_SLEEP and new_state == EventSM.AWAKE)
        length_secs = end_ts - self.last_rec_ts
        self.db_helper.add_sleep_state(old_state, self.last_rec_ts, length_secs, success)
        self.last_rec_ts = end_ts
        self.state = new_state
        return length_secs

    def _put_to_sleep(self, ts, event):
        if self.state != EventSM.AWAKE:
            raise Exception('Cannot put to sleep an awake baby...')
        self._add_sleep_state(EventSM.AWAKE, ts, EventSM.PUTTING_TO_SLEEP)

    def _failed_to_put_to_sleep(self, ts, event):
        if self.state != EventSM.PUTTING_TO_SLEEP:
            raise Exception('Cannot fail to put baby to sleep without trying first...')
        s = self._add_sleep_state(EventSM.PUTTING_TO_SLEEP, ts, EventSM.AWAKE)
        self.accum_put_to_sleep_time += s

    def _fell_asleep(self, ts, event):
        if self.state != EventSM.PUTTING_TO_SLEEP:
            raise Exception('Baby cannot sleep without being put to sleep...')
        s = self._add_sleep_state(EventSM.PUTTING_TO_SLEEP, ts, EventSM.SLEEPING)
        self.accum_put_to_sleep_time += s

    def _woke_up(self, ts, event):
        if self.state != EventSM.SLEEPING:
            raise Exception('Cannot wake up without sleeping first...')
        s = self._add_sleep_state(EventSM.SLEEPING, ts, EventSM.AWAKE)
        self.accum_sleeping_time += s

    def _alone(self, ts, event):
        if self._alone_time != 0:
            raise Exception('Already alone, cannot be left alone again...')
        self._alone_time = ts

    def _not_alone(self, ts, event):
        if self._alone_time == 0:
            raise Exception('Cannpt be not alone with being left alone first...')
        t = ts - self._alone_time
        self._alone_time = 0
        self.db_helper.add_time_alone(ts, t)

    def _away(self, ts, event):
        if self._where_away != None:
            if self._away_ts == 0:
                raise Exception('Cannot go away again without leaving first...')
            self.db_helper.add_time_away(self._away_ts, ts - self._away_ts, self._where_away)
        self._where_away = event.get_subtype()
        self._away_ts = ts

    def _back_home(self, ts, event):
        if self._away_ts == 0 or self._where_away == None:
            raise Exception('Cannot get back home without leaving...')
        self.db_helper.add_time_away(self._away_ts, ts - self._away_ts, self._where_away)
        self._where_away = None
        self._away_ts = 0

    def digest(self, event):
        self.db_helper.add_event(event)

        event_date = event.get_date()
        event_ts = event.get_ts()
        event_type = event.get_type()
        event_subtype = event.get_subtype()

        if self.last_rec_ts == 0:
            self.last_rec_ts = event_ts

        self.days.add(event_date)

        if not self.cb.has_key(event_type):
            return
        cb = self.cb[event_type]
        if type(cb) == dict:
            cb = cb[event_subtype]
        cb(event_ts, event)

    def get_state(self):
        return self.state

    def get_different_days(self):
        return len(self.days)

    def get_sleep_time(self):
        return self.accum_sleeping_time

    def get_put_to_slee_time(self):
        return self.accum_put_to_sleep_time

def build_event_sm(events):
    d = DBHelper()
    d.recreate_tables()
    ts = 0
    event_i = 0
    sm = EventSM()
    for event in events:
        event_i += 1
        try:
            sm.digest(event)
        except Exception as e:
            print "Exception in event #%d: %s" % (event_i, str(e))
            raise e
    return sm  

