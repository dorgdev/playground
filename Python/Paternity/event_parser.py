from event import Event

class EventFileParser:
    def __init__(self, filename):
        self.filename = filename

    def _clear_newline(self, line):
        if line.endswith('\r\n'):
            return line[:-2]
        if line.endswith('\n'):
            return line[:-1]
        return line

    def _parse_lines(self, lines):
        lines = map(self._clear_newline, filter(lambda line: line not in ['', '\n'], lines))
        events = [0] * len(lines)
        event = Event()
        i = 0
        for line in lines:
            event = Event(line, event.get_date())
            events[i] = event
            i += 1
        return events

    def parse(self):
        with open(self.filename, 'r') as f:
            lines = f.readlines()[1:]
            return self._parse_lines(lines)
