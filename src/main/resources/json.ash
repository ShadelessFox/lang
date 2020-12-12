import builtin = std;

class Reader {
    constructor(self, buffer) {
        self.buffer = buffer;
        self.index = 0;
    }

    def next(self) {
        let value = self.peek();

        if value != none {
            self.index += 1;
        }

        return value;
    }

    def peek(self) {
        if self.index >= self.buffer.length {
            return none;
        }
        return self.buffer[self.index];
    }
}

class Lexer : Reader {
    constructor(self, buffer) {
        super(buffer);
        self.ch = none;
        self.read();
    }

    def advance(self) {
        loop {
            let ch = self.ch;

            if ch == '{' { self.read(); return ['{', none]; }
            if ch == '}' { self.read(); return ['}', none]; }
            if ch == '[' { self.read(); return ['[', none]; }
            if ch == ']' { self.read(); return [']', none]; }
            if ch == ',' { self.read(); return [',', none]; }
            if ch == ':' { self.read(); return [':', none]; }

            if ch == none {
                return ['end', none];
            }

            if ch == ' ' {
                self.read();
                continue;
            }

            if ch == '"' {
                let buffer = '';
                self.read();

                loop {
                    if self.ch == '\n' or self.ch == '\r' or self.ch == none {
                        std.panic('String literal is not closed', false);
                    }

                    if self.ch == '"' {
                        self.read();
                        return ['string', buffer];
                    }

                    buffer += self.read();
                }
            }

            if self.is_digit(ch, false) {
                let buffer = '';

                loop while self.is_digit(self.ch, true) {
                    buffer += self.read();
                }

                return ['number', buffer];
            }

            std.panic('Unknown character: \'\{ch}\'', false);
        }
    }

    def read(self) {
        let ch = self.ch;
        self.ch = self.next();
        return ch;
    }
    
    def is_digit(self, ch, allow_zero) {
        if ch == '1' or ch == '2' or ch == '3' or ch == '4' or ch == '5' or ch == '6' or ch == '7' or ch == '8' or ch == '9' {
            return true;
        }
        if ch == '0' and allow_zero {
            return true;
        }
        return false;
    }
}

class Parser {
    constructor(self, buffer) {
        self.lexer = new Lexer(buffer);
        self.token = self.lexer.advance();
    }

    def parse(self) {
        let token = self.expect('[', '{', 'string', 'number');

        if token[0] == '[' {
            return self.list(none, ']', ',', self.parse);
        }

        if token[0] == '{' {
            return self.list(none, '}', ',', self.pair);
        }

        return token[1];
    }

    def pair(self) {
        let key = self.parse();
        self.expect(':');
        let value = self.parse();
        return [key, value];
    }

    def list(self, opening, closing, separator, supplier) {
        let items = [];

        if opening != none {
            self.expect(opening);
        }

        if not self.matches(closing) {
            items = std.add(items, supplier());

            loop while not self.matches(closing, 'end') {
                if separator != none {
                    self.expect(separator);
                }

                items = std.add(items, supplier());
            }
        }

        self.expect(closing);

        return items;
    }

    def advance(self) {
        let token = self.token;
        self.token = self.lexer.advance();
        return token;
    }

    def expect(self, args...) {
        let index = 0;
        let token = self.token[0];
        loop while index < args.length {
            if token == args[index] {
                return self.advance();
            }

            index += 1;
        }

        std.panic('Expected any of \{args} but found \'\{token}\'', false);
    }

    def matches(self, args...) {
        let index = 0;
        let token = self.token[0];
        loop while index < args.length - 1 {
            if token == args[index] {
                return true;
            }

            index += 1;
        }

        return false;
    }
}

def parse(text) {
    return new Parser(text).parse();
}

def main() {
    std.println(parse('[{"id":1,"first_name":"Kelvin","last_name":"Batho","email":"kbatho0@hexun.com","gender":"Male","ip_address":"170.194.121.67"}]'));
}