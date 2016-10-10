from db_helper import *
from event import *
from sys import _getframe

class Queries:
    def __init__(self, db_helper):
        self.dbh = db_helper
        self.cache = {}

    def _memoize(f):
        '''
        A decorator for caching values returned by various calls to DB executions.
        :return: Cached values if exists, or new value if don't.
        '''
        def wrap(s):
            key = f.__name__
            if s._is_cached(key):
                return s._cached(key)

            value = f.__call__(s)
            s._do_cache(key, value)
            return value

        return wrap

    def _is_cached(self, key):
        return self.cache.has_key(key)

    def _cached(self, key):
        return self.cache[key]

    def _do_cache(self, key, value):
        self.cache[key] = value
        return value

    @_memoize
    def time_alone(self):
        query = \
            '''
            SELECT sum(b.ts - a.ts)
            FROM (
                SELECT date, ts
                FROM %s
                WHERE etype = '%s'
            ) a
            LEFT JOIN (
                SELECT date, ts
                FROM %s
                WHERE etype = '%s'
            ) b
            ON a.date = b.date
            ''' % (EVENTS_TABLE, EVENT_MEGI_LEFT, EVENTS_TABLE, EVENT_MEGI_BACK)
        res = self.dbh.execute(query)
        return res[0][0]

    @_memoize
    def count_days(self):
        query = \
            '''
            SELECT sum(c.days)
            FROM (
                SELECT IF(b.ts - a.ts < 18000, 0.5, 1) as days
                FROM (
                    SELECT date, ts
                    FROM %s
                    WHERE etype = '%s'
                ) a
                LEFT JOIN (
                    SELECT date, ts
                    FROM %s
                    WHERE etype = '%s'
                ) b
                ON a.date = b.date
            ) c
            ''' % (EVENTS_TABLE, EVENT_MEGI_LEFT, EVENTS_TABLE, 'megi\\\'s back')
        res = self.dbh.execute(query)
        return res[0][0]

    def avg_time_alone_per_day_sec(self):
        return self.time_alone() / self.count_days()

    @_memoize
    def sleep_time(self):
        query = "SELECT sum(length_secs) FROM %s WHERE state = 'sleeping'" % (SLEEP_STATES_TABLE)
        res = self.dbh.execute(query)
        sleep_time_secs = res[0][0]
        return sleep_time_secs

    @_memoize
    def sleep_count(self):
        query = "SELECT count(*) FROM %s WHERE etype = '%s'" % (EVENTS_TABLE, EVENT_SLEPT)
        res = self.dbh.execute(query)
        return res[0][0]

    @_memoize
    def put_to_sleep_time(self):
        query = "SELECT sum(length_secs) FROM %s WHERE state = 'put to sleep'" % (SLEEP_STATES_TABLE)
        res = self.dbh.execute(query)
        return res[0][0]

    @_memoize
    def put_to_sleep_count(self):
        query = "SELECT count(*) FROM %s WHERE etype = '%s'" % (EVENTS_TABLE, EVENT_PUTTING_TO_SLEEP)
        res = self.dbh.execute(query)
        return res[0][0]

    def put_to_sleep_success_rate(self):
        total = self.sleep_count()
        failures = self.put_to_sleep_failures_count()
        return 100.0 * (1 - failures / (total * 1.0))

    @_memoize
    def put_to_sleep_failures_count(self):
        query = "SELECT count(*) FROM %s WHERE etype = '%s'" % (EVENTS_TABLE, EVENT_FAILED_PUTTING_TO_SLEEP)
        res = self.dbh.execute(query)
        return res[0][0]

    @_memoize
    def put_to_sleep_failures_time(self):
        query = "SELECT sum(length_secs) FROM %s WHERE success = '%s'" % (SLEEP_STATES_TABLE, FALSE_TINYINT)
        res = self.dbh.execute(query)
        return res[0][0]

    def sleep_time_per_day(self):
        days = self.count_days()
        sleep_time_secs = self.sleep_time()
        return 1.0 * sleep_time_secs / days

    @_memoize
    def avg_sleep_time(self):
        query = "SELECT avg(length_secs) FROM %s WHERE state = 'Sleeping'" % (SLEEP_STATES_TABLE)
        res = self.dbh.execute(query)
        return res[0][0]

    @_memoize
    def heavy_diapers(self):
        query = "SELECT count(*) FROM %s WHERE etype = '%s' and stype = '%s'" % (
            EVENTS_TABLE, EVENT_DIAPER, EVENT_SUBTYPE_HEAVY_DIAPER)
        res = self.dbh.execute(query)
        return res[0][0]

    @_memoize
    def light_diapers(self):
        query = "SELECT count(*) FROM %s WHERE etype = '%s' and stype = '%s'" % (
            EVENTS_TABLE, EVENT_DIAPER, EVENT_SUBTYPE_LIGHT_DIAPER)
        res = self.dbh.execute(query)
        return res[0][0]

    def heavy_diapers_ratio(self):
        heavy = self.heavy_diapers()
        light = self.light_diapers()
        return heavy * 1.0 / (heavy + light)

    def light_diapers_ratio(self):
        return 1 - self.heavy_diapers_ratio()

    @_memoize
    def milk_drank(self):
        query = "SELECT stype FROM %s WHERE etype = '%s'" % (EVENTS_TABLE, EVENT_BOTTLE)
        res = self.dbh.execute(query)
        accum = 0
        for rec in res:
            accum += int(rec[0])
        return accum

    @_memoize
    def away_time(self):
        query = "SELECT sum(length_secs), whereabout FROM %s GROUP BY whereabout" % (TIME_AWAY_TABLE)
        res = self.dbh.execute(query)
        return dict([[record[1], record[0]] for record in res])

    @_memoize
    def total_away_time(self):
        all_places = self.away_time()
        return sum(all_places.values())

    def query_to_table(self, query, title, cols):
        header = [title, cols + []]

        res = self.dbh.execute(query)
        if len(res) == 0:
            return header
        if len(res[0]) != len(cols):
            raise Exception('Columns in result (%d) does not equal expected columns count (%d)' % (len(res), len(cols)))
        return header + res



