from sys import stdout

from db_helper import *
from event_parser import *
from event_sm import *
from queries import *

class Report:
    def __init__(self, db_helper = None):
        self.dbh = db_helper if db_helper is not None else DBHelper()
        self.queries = Queries(self.dbh)

    def print_table(self, table, filename = None):
        fh = stdout
        if filename != None:
            fh = open(filename, 'w')
        fh.writelines([table[0] + '\n'])
        for row in table[1:]:
            fh.writelines(['\t'.join([str(col) for col in row]) + '\n'])
        if fh is not sys.stdout:
            fh.close()

    def create_sleep_per_day_table(self, filename = None):
        query = '''SELECT DATE(FROM_UNIXTIME(start_ts)) AS day, sum(length_secs) AS sleep_time
                   FROM sleep_states
                   WHERE state = 'Sleeping'
                   GROUP BY day'''
        self.print_table(self.queries.query_to_table(query, 'Sleep time per day', ['day', 'sleep secs']), filename)

    def create_sleep_per_day_and_failures_table(self, filename = None):
        query = '''SELECT a.day, sleep_time, IFNULL(failures, 0)
                   FROM (
                      SELECT sum(length_secs) AS sleep_time, DATE(FROM_UNIXTIME(start_ts)) AS day
                      FROM sleep_states
                      WHERE state = 'Sleeping'
                      GROUP BY day) a
                   LEFT JOIN (
                      SELECT count(*) AS failures, DATE(FROM_UNIXTIME(ts)) AS day
                      FROM events
                      WHERE etype = 'failed putting to sleep'
                      GROUP BY day) b
                   ON a.day = b.day;'''
        self.print_table(self.queries.query_to_table(query, 'Sleep time and failures', ['day', 'sleep secs', 'failures']), filename)

    def fruits_report(self):
        query = '''SELECT stype
                   FROM events
                   WHERE etype = 'fruits' '''
        res = self.dbh.execute(query)
        fruits = {}
        fruits_list = reduce(lambda x,y: x + y[0].split(', '), res, [])
        fruits_set = set(fruits_list)
        for fruit in fruits_set:
            fruits[fruit] = fruits_list.count(fruit)
        conversion_table = {
            'apple'  : [1, 'apple(s)'],
            'pear'   : [2, 'pear(s)'],
            'melon'  : [0.125, 'melon(s)'],
            'almonds': [1, 'teaspoon(s) almonds juice'],
            'date'   : [1.5, 'date(s)'],
            'tahini' : [1, 'teaspoon(s) tahini'],
            'banana' : [1, 'banana(s)'],
            'mango'  : [1, 'mango(s)']
        }
        breakdown = {}
        for fruit in fruits:
            count = fruits[fruit] * conversion_table[fruit][0]
            breakdown[fruit] = '%d %s' % (count, conversion_table[fruit][1])
        return breakdown

    def time_pp(self, length_sec, print_all = False):
        res = ''
        length_sec = int(length_sec)
        hours = length_sec / 3600
        if hours > 0 or print_all:
            res += '%dH:' % (int(hours))
            print_all = True
        mins = (length_sec % 3600) / 60
        if mins > 0 or print_all:
            res += '%dM:' % (int(mins))
            print_all = True
        secs = length_sec % 60
        if secs > 0 or print_all:
            res += '%dS' % (int(secs))
        return res

    def create_report(self, rerun = True, filename = '/tmp/pat.tsv'):
        if rerun:
            parser = EventFileParser(filename)
            events = parser.parse()
            build_event_sm(events)

        # Time alone
        print
        print '=== TIME ALONE ==='

        query = 'SELECT sum(length_secs) FROM time_alone'
        alont_time_secs = self.dbh.execute(query)[0][0]
        print 'Time alone: %s' % (self.time_pp(alont_time_secs))

        # Longest time alone
        query = 'SELECT max(length_secs) FROM time_alone'
        longest_time_alone = self.dbh.execute(query)[0][0]
        print 'Longest time alone: %s' % (self.time_pp(longest_time_alone))

        # Average time alone
        days = int(self.queries.count_days())
        print 'Average time alone: %s' % (self.time_pp(alont_time_secs / days))

        # Days
        print 'Overall spent %d days alone.' % (days)

        # Sleep time
        print
        print
        print '=== EMMA\'S SLEEP TIME ==='

        sleep_time_secs = self.queries.sleep_time()
        print 'Overall Emma\'s sleep time: %s' % (self.time_pp(sleep_time_secs))

        # Sleep time per day
        print 'Average sleep time per day: %s' % (self.time_pp(sleep_time_secs / days))

        # Sleeps per day (average + time-series)
        sleeps_count = self.queries.sleep_count()
        print 'Emma slept %d times.' % (sleeps_count)
        print 'Emma slept, on average, %.2f times a day' % (float(sleeps_count) / days)

        # Put to sleep time (average + time-series)
        put_to_sleep_time = self.queries.put_to_sleep_time()
        print 'Spent %s on putting Emma to sleep.' % (self.time_pp(put_to_sleep_time))
        print 'Spent, on average, %s on putting Emma to sleep per day.' % (self.time_pp(put_to_sleep_time / days))

        # Failure count
        failure_count = self.queries.put_to_sleep_failures_count()
        print 'Failed putting Emma to sleep %d times' % (failure_count)
        print 'On average, failed to put Emma to sleep %.2f times a day.' % (float(failure_count) / days)

        # Failure wasted time
        failure_time = self.queries.put_to_sleep_failures_time()
        print 'Spent %s on failing to put Emma to sleep.' % (self.time_pp(failure_time))
        print 'Spent, on average, %s on failing to put Emma to sleep per day.' % (self.time_pp(failure_time / days))

        # Diapers
        print
        print
        print '=== DIAPERS ==='

        light_diapers = self.queries.light_diapers()
        light_diapers_ratio = self.queries.light_diapers_ratio()
        heavy_diapers = self.queries.heavy_diapers()
        heavy_diapers_ratio = self.queries.heavy_diapers_ratio()
        total_diapers = light_diapers + heavy_diapers
        # Total
        print 'Changed %d diapers, average %.2f a day.' % (total_diapers, float(total_diapers) / days)
        # Light
        print 'Out of which, %d (average %.2f) were \'light\' diapers.' % (light_diapers, float(light_diapers) / days)
        # Heavy
        print 'And the rest, %d (average %.2f) were \'heavy\' diapers.' % (heavy_diapers, float(heavy_diapers) / days)
        # Ratios
        print 'Overall: heavy diapers - %.2f%%, light diapers - %.2f%%' % (100 * heavy_diapers_ratio, 100 * light_diapers_ratio)


        # Fruits breakdown
        print
        print
        print '=== FRUITS ==='

        print 'Every day, made fruits breakfast. Totally consumed:'
        fruits = self.fruits_report()
        for fruit in fruits:
            print ' - %s' % (fruits[fruit])

        # Time outside - total
        print
        print
        print '=== TIME OUTSIDE ==='

        total_away_time = self.queries.total_away_time()
        print 'Overall, spent %s outside.' % (self.time_pp(total_away_time))
        print 'On average, spent %s outside a day.' % (self.time_pp(total_away_time / days))
        print 'Time away spent on:'

        away_locations = self.queries.away_time()
        for location, secs in away_locations.iteritems():
            print ' - %s : %s' % (location, self.time_pp(secs))
