import datetime
import mysql.connector

EVENTS_TABLE = 'events'
SLEEP_STATES_TABLE = 'sleep_states'
TIME_ALONE_TABLE = 'time_alone'
TIME_AWAY_TABLE = 'time_away'
FALSE_TINYINT = '0'
TRUE_TINYINT = '1'

class DBHelper:
    def __init__(self, user = 'root', pwd = 'password', host = '127.0.0.1', db = 'paternity'):
        self.user = user
        self.pwd = pwd
        self.host = host
        self.db = db
        self.cnx = None
        self.cursor = None

    def disconnect(self):
        self.cursor.close()
        self.cursor = None
        self.cnx.close()
        self.cnx = None

    def connect(self):
        if self.cnx != None:
            raise Exception('Already connected. Call disconnect first!')
        self.cnx = mysql.connector.MySQLConnection(user = self.user, password = self.pwd, host = self.host, database = self.db)
        self.cursor = self.cnx.cursor()

    def _run_simple_statement(self, statement, values = ()):
        self.connect()
        self.cursor.execute(statement, values)
        result = []
        for rec in self.cursor:
            rec_result = []
            for f in rec:
                rec_result.append(f)
            result.append(rec_result)

        self.cnx.commit()
        self.disconnect()

        return None if len(result) == 0 else result


    def recreate_events_table(self):
        '''
        Delates (if applicable) and creates the main events table. Structure:
        +------------+--------------+-----------------------------+
        | Field Name |  Field Type  | Constraints                 |
        +------------+--------------+-----------------------------+
        |     id     |      INT     | AUTO INCREMENT, PRIMARY KEY
        +------------+--------------+-----------------------------+
        |    date    |      DATE    | NOT NULL                    |
        +------------+--------------+-----------------------------+
        |     ts     |      INT     | NOT NULL                    |
        +------------+--------------+-----------------------------+
        |    etype   | VARCHAR(100) | NOT NULL                    |
        +------------+--------------+-----------------------------+
        |    stype   | VARCHAR(100) | N/A                         |
        +------------+--------------+-----------------------------+
        '''
        self._run_simple_statement('DROP TABLE IF EXISTS %s' % (EVENTS_TABLE))

        statement = 'CREATE TABLE %s (                              \
                        id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, \
                        date DATE NOT NULL,                         \
                        ts INT NOT NULL,                            \
                        etype VARCHAR(100) NOT NULL,                \
                        stype VARCHAR(100)                          \
                    );' % (EVENTS_TABLE)
        self._run_simple_statement(statement)

    def recreate_sleep_states_table(self):
        '''
        Deletes (if applicable) and creates the sleep states table. Structure:
        +-------------+--------------+-----------------------------+
        | Field Name  |  Field Type  | Constraints                 |
        +-------------+--------------+-----------------------------+
        |     id      |      INT     | AUTO INCREMENT, PRIMARY KEY |
        +-------------+--------------+-----------------------------+
        |    state    | VARCHAR(100) | NOT NULL                    |
        +-------------+--------------+-----------------------------+
        |  start_ts   |      INT     | NOT NULL                    |
        +-------------+--------------+-----------------------------+
        | length_secs |      INT     | NOT NULL                    |
        +-------------+--------------+-----------------------------+
        |   success   |   BOOLEAN    | NOT NULL                    |
        +-------------+--------------+-----------------------------+
        '''
        self._run_simple_statement('DROP TABLE IF EXISTS %s' % (SLEEP_STATES_TABLE))

        statement = 'CREATE TABLE %s(                               \
                        id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, \
                        state VARCHAR(100) NOT NULL,                \
                        start_ts INT NOT NULL,                      \
                        length_secs INT NOT NULL,                   \
                        success BOOLEAN NOT NULL                    \
                    );' % (SLEEP_STATES_TABLE)
        self._run_simple_statement(statement)

    def recreate_time_alone_table(self):
        '''
        Deletes (if applicable) and creates the sleep states table. Structure:
        +-------------+--------------+-----------------------------+
        | Field Name  |  Field Type  | Constraints                 |
        +-------------+--------------+-----------------------------+
        |     id      |      INT     | AUTO INCREMENT, PRIMARY KEY |
        +-------------+--------------+-----------------------------+
        |    day      |     DATE     | NOT NULL                    |
        +-------------+--------------+-----------------------------+
        | length_secs |      INT     | NOT NULL                    |
        +-------------+--------------+-----------------------------+
        '''
        self._run_simple_statement('DROP TABLE IF EXISTS %s' % (TIME_ALONE_TABLE))

        statement = 'CREATE TABLE %s(                               \
                        id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, \
                        day DATE NOT NULL,                          \
                        length_secs INT NOT NULL                    \
                    );' % (TIME_ALONE_TABLE)
        self._run_simple_statement(statement)

    def recreate_time_away_table(self):
        '''
        Deletes (if applicable) and creates the time away table. Structure:
        +-------------+--------------+-----------------------------+
        | Field Name  |  Field Type  | Constraints                 |
        +-------------+--------------+-----------------------------+
        |     id      |      INT     | AUTO INCREMENT, PRIMARY KEY |
        +-------------+--------------+-----------------------------+
        |  start_ts   |      INT     | NOT NULL                    |
        +-------------+--------------+-----------------------------+
        | length_secs |      INT     | NOT NULL                    |
        +-------------+--------------+-----------------------------+
        | whereabout  | VARCAHR(100) | NOT NULL                    |
        +-------------+--------------+-----------------------------+
        '''
        self._run_simple_statement('DROP TABLE IF EXISTS %s' % (TIME_AWAY_TABLE))

        statement = 'CREATE TABLE %s(                               \
                        id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, \
                        start_ts INT NOT NULL,                      \
                        length_secs INT NOT NULL,                   \
                        whereabout VARCHAR(100) NOT NULL            \
                    );' % (TIME_AWAY_TABLE)
        self._run_simple_statement(statement)

    def recreate_tables(self):
        self.recreate_events_table()
        self.recreate_sleep_states_table()
        self.recreate_time_alone_table()
        self.recreate_time_away_table()

    def add_event(self, event):
        date = datetime.datetime.fromtimestamp(event.get_ts()).strftime('%Y-%m-%d')
        ts = str(event.get_ts())
        etype = event.get_type()
        stype = event.get_subtype()

        statement = 'INSERT INTO ' + EVENTS_TABLE + ' (date, ts, etype, stype) VALUES (%s, %s, %s, %s)'
        values = (date, ts, etype, stype)
        self._run_simple_statement(statement, values)

    def add_sleep_state(self, state, start_ts, length_secs, success = True):
        if length_secs == 0:
            return
        statement = \
            'INSERT INTO ' + SLEEP_STATES_TABLE + ' (state, start_ts, length_secs, success) ' + \
            'VALUES (%s, %s, %s, %s)'
        values = (state, start_ts, length_secs, TRUE_TINYINT if success else FALSE_TINYINT)
        self._run_simple_statement(statement, values)

    def add_time_alone(self, day_ts, length_secs):
        statement = 'INSERT INTO ' + TIME_ALONE_TABLE + ' (day, length_secs) VALUES (%s, %s)'
        values = (datetime.datetime.fromtimestamp(day_ts).strftime('%Y-%m-%d'), length_secs)
        self._run_simple_statement(statement, values)

    def add_time_away(self, start_ts, legnth_secs, whereabout):
        statement = 'INSERT INTO ' + TIME_AWAY_TABLE + ' (start_ts, length_secs, whereabout) VALUES (%s, %s, %s)'
        values = (start_ts, legnth_secs, whereabout)
        self._run_simple_statement(statement, values)

    def execute(self, statement):
        return self._run_simple_statement(statement)

def con():
    return mysql.connector.MySQLConnection(user = 'root', password = 'password', host = '127.0.0.1', database = 'paternity')
