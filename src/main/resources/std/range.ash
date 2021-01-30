import iterator;

class Range : iterator.Iterable {
    constructor(self, start, end, inclusive) {
        self.start = start;
        self.end = end;
        self.inclusive = inclusive;
    }

    def get_iterator(self) {
        let start = self.start;
        let end = self.end;

        if self.start < self.end {
            if self.inclusive {
                end += 1;
            }

            return new AscendingRangeIterator(start, end);
        } else {
            if self.inclusive {
                end -= 1;
            }

            return new DescendingRangeIterator(start, end);
        }
    }
}

class AscendingRangeIterator : iterator.Iterator {
    constructor(self, start, end) {
        assert start <= end;
        self.pos = start;
        self.end = end;
    }

    def has_next(self) {
        return self.pos < self.end;
    }

    def get_next(self) {
        let value = self.pos;
        self.pos += 1;
        return value;
    }
}

class DescendingRangeIterator : iterator.Iterator {
    constructor(self, start, end) {
        assert start >= end;
        self.pos = start;
        self.end = end;
    }

    def has_next(self) {
        return self.pos > self.end;
    }

    def get_next(self) {
        let value = self.pos;
        self.pos -= 1;
        return value;
    }
}