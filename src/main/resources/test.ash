import builtin = std;

# Add proper test suite like that:
# expect(def () { assert true;  }).to_succeed();
# expect(def () { assert false; }).to_panic();
# expect(def () { return false; }).to_return(false);

def __test_impl(name, handler) {
    let caption = 'Test "\x{1b}[1m\{name}\x{1b}[0m"';
    std.print('[ ] \{caption}');

    let result = handler();

    if result[0] {
        std.println('\r\x{1b}[1;32m[+]\x{1b}[0m \{caption}');
        tests_passed = tests_passed + 1;
    } else {
        std.println('\r\x{1b}[1;31m[-]\x{1b}[0m \{caption}: \x{1b}[31m\{result[1]}\x{1b}[0m');
    }

    tests_ran = tests_ran + 1;
}

def pass(name, function) {
    __test_impl(name, def () use (function) {
        try {
            function();
            return [true, none];
        } recover error {
            return [false, error];
        }
    });
}

def fail(name, function) {
    __test_impl(name, def () use (function) {
        try {
            function();
            return [false, 'panic expected'];
        } recover error {
            return [true, error];
        }
    });
}

def init() {
    tests_ran = 0;
    tests_passed = 0;
}

def finish() {
    std.println('\nTests passed: \x{1b}[1m\{tests_passed}/\{tests_ran}\x{1b}[0m');
}
