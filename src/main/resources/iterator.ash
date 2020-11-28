class Iterable {
    constructor(self) {
    }

    def get_iterator(self) {
        return none;
    }
}

class Iterator {
    constructor(self) {
    }

    def has_next(self) {
        return false;
    }

    def get_next(self) {
        return none;
    }
}

class AscendingRangeIterator : Iterator {
    constructor(self, range) {
        self.pos = range.begin;
        self.end = range.end;
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

class DescendingRangeIterator : Iterator {
    constructor(self, range) {
        self.pos = range.begin;
        self.end = range.end;
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

class Range : Iterable {
    constructor(self, begin, end) {
        self.begin = begin;
        self.end = end;
        self.is_ascending = begin < end;
        self.is_descending = begin > end;
    }

    def get_iterator(self) {
        if self.is_ascending {
            return new AscendingRangeIterator(self);
        } else {
            return new DescendingRangeIterator(self);
        }
    }
}
