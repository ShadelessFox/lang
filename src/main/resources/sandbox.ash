import builtin = std;

def test_locals_helper_a(a, b) {
    let a0 = a;
    let b0 = b;
    return a0;
}

def test_locals_helper_b(a, b) {
    let a0 = a;
    let b0 = b;
    return b0;
}

def test_locals() {
    assert test_locals_helper_a(1, 2) == 1;
    assert test_locals_helper_b(1, 2) == 2;

    a = 5;

    {
        let a = 0;
        assert a == 0;
    }

    {
        let b = 1;
        assert b == 1;
    }

    assert a == 5;

    let c = 3;
    let d = 4;

    let e = test_locals_helper_a(c, d);
    let f = test_locals_helper_a(c, d);
    let g = test_locals_helper_b(c, d);
    let h = test_locals_helper_b(c, d);

    assert a == 5;

    assert c == 3;
    assert d == 4;
    assert e == 3;
    assert f == c;
    assert g == 4;
    assert h == d;
}

def test_relational() {
    assert 1 != 0;
    assert 1 == 1;
    assert 2 >= 1;
    assert 2 >= 2;
    assert 5 <= 7;
    assert true and true;
    assert true and true and true;
    assert true or (false and true);
    assert false or false or true == true;

    assert not false and true;
    assert true and not false;
    assert false or not false;
    assert true and not (not true);
    assert not false and not false;
    assert not false and ((not true) or true);

    assert ('' or 'a') == 'a';
    assert (1 or 'a') == 1;
    assert ([] or [1]) == [1];
    assert (1 and 1.0) == 1.0;
}

def test_precedence() {
    assert (20 + 10) * 15 / 5 == 90;
    assert ((20 + 10) * 15) / 5 == 90;
    assert (20 + 10) * (15 / 5) == 90;
    assert 20 + (10 * 15) / 5 == 50;
    assert 2 + 2 * 2 == 6;
    assert (2 + 2) * 2 == 8;
    assert 2 * 3 + 4 * 5 == 26;
    assert 2 * (3 + 4) * 5 == 70;
    assert ((((((((2)))))))) == ((1) + (1));
}

def test_assignment() {
    let a = 5;
    a = 2;
    assert a == 2;
    a += 7;
    assert a == 9;
    a -= 1;
    assert a == 8;
    a *= 1;
    assert a == 8;
    a += 2 + 2;
    assert a == 12;
}

def test_unary() {
    assert -0 == 0;
    assert +0 == 0;
    assert +1 == 1;
    assert -5 < 5;
    assert -5 < +5;
    assert not true == false;
    assert not false;
    assert not (not true);
}

def test_branch() {
    result = 0;

    if true { result = 1; } else { result = 0; }
    assert result == 1;

    if not false { result = 2; } else { result = 0; }
    assert result == 2;

    if true and true { result = 3; } else { result = 0; }
    assert result == 3;

    if true and true or false { result = 4; } else { result = 0; }
    assert result == 4;

    if false or true or false { result = 5; } else { result = 0; }
    assert result == 5;

    if true or (true and (false or true)) { result = 6; } else { result = 0; }
    assert result == 6;

    let test_branch_return = def (x, a, b) {
        if x {
            return a;
        } else {
            return b;
        }
    };

    assert test_branch_return(true, 1, 2) == 1;
    assert test_branch_return(false, 1, 2) == 2;

    let test_branch_return_complex = def (x, y, z, w, v, a, b) {
        if x and (y or z) and (w or v) {
            return a;
        } else {
            return b;
        }
    };

    assert test_branch_return_complex(true, false, true, true, false, 1, 2) == 1;
    assert test_branch_return_complex(false, false, true, true, false, 1, 2) == 2;
    assert test_branch_return_complex(true, false, false, true, false, 1, 2) == 2;
    assert test_branch_return_complex(true, false, true, false, false, 1, 2) == 2;
}

def test_eval_order_a() {
    assert test_eval_order_a_var == 0;
    assert test_eval_order_b_var == 0;
    assert test_eval_order_c_var == 0;
    test_eval_order_a_var = 1;
    return 1;
}

def test_eval_order_b() {
    assert test_eval_order_a_var == 1;
    assert test_eval_order_b_var == 0;
    assert test_eval_order_c_var == 0;
    test_eval_order_b_var = 1;
    return 2;
}

def test_eval_order_c() {
    assert test_eval_order_a_var == 1;
    assert test_eval_order_b_var == 1;
    assert test_eval_order_c_var == 0;
    test_eval_order_c_var = 1;
    return 3;
}

def test_eval_order_helper(a, b, c) {
    assert test_eval_order_a_var == 1;
    assert test_eval_order_b_var == 1;
    assert test_eval_order_c_var == 1;
    return a + b + c;
}

def test_eval_order() {
    test_eval_order_a_var = 0;
    test_eval_order_b_var = 0;
    test_eval_order_c_var = 0;
    assert test_eval_order_helper(
        test_eval_order_a(),
        test_eval_order_b(),
        test_eval_order_c()
    ) == 6;
}

def factorial(x) {
    if x == 0 { return 1; }
    return x * factorial(x - 1);
}

def test_factorial() {
    assert factorial(0) == 1;
    assert factorial(1) == 1;
    assert factorial(2) == 2;
    assert factorial(3) == 6;
    assert factorial(4) == 24;
    assert factorial(5) == 120;
    assert factorial(6) == 720;
    assert factorial(7) == 5040;
    assert factorial(8) == 40320;
    assert factorial(9) == 362880;
    assert factorial(10) == 3628800;
}

def fibonacci(x) {
    if x <= 1 { return 1; }
    return fibonacci(x - 1) + fibonacci(x - 2);
}

def test_fibonacci() {
    assert fibonacci(1) == 1;
    assert fibonacci(2) == 2;
    assert fibonacci(3) == 3;
    assert fibonacci(4) == 5;
    assert fibonacci(5) == 8;
    assert fibonacci(6) == 13;
    assert fibonacci(7) == 21;
    assert fibonacci(8) == 34;
    assert fibonacci(9) == 55;
    assert fibonacci(10) == 89;
    assert fibonacci(11) == 144;
}

def test_interpolation() {
    assert '\{''}' == '';
    assert '\{' '}' == ' ';
    assert ' \{' '} ' == '   ';
    assert '1\{''}' == '1';
    assert '\{''}2' == '2';
    assert '1\{''}2' == '12';
    assert '1\{'2'}3' == '123';
    assert '\{'1'}\{'2'}\{'3'}' == '123';
    assert '1\{'\{'2'}'}3' == '123';
    assert '1\{'2'}3\{'4\{'5'}'}' == '12345';
}

def test_panic_recover() {
    let status = 0;
    try {
        status += 1;
        try {
            status += 2;
            assert 0 == 1;
            status += 3;
        } recover {
            status += 4;
            assert 1 == 2;
        }
    } recover {
        status += 5;
    }
    status += 6;
    assert status == 1 + 2 + 4 + 5 + 6;

    try {
        std.debug_assert(false);
    } recover {
        status = status + 7;
        try {
            std.debug_assert(false);
        } recover {
            status = status + 8;
        }
    }

    assert status == 1 + 2 + 4 + 5 + 6 + 7 + 8;
}

def test_try_finally() {
    assert (def () {
        try {
            return 1;
        } finally {
            return 2;
        }
    })() == 2;

    assert (def () {
        try {
            return 123;
        } finally {
        }
        assert false, 'unreachable';
    })() == 123;

    assert (def () {
        let value = 0;
        for i in 0..5 {
            value += 1;
            try {
                continue;
            } finally {
                return 'you shall not pass';
            }
        }
        return value;
    })() == 'you shall not pass';

    assert (def () {
        let value = 0;
        try {
            value += 5;
        } finally {
            value += 7;
        }
        return value;
    })() == 12;

    assert (def () {
        let value = 0;
        try {
            value += 5;
            assert false;
        } recover {
            value += 7;
        } finally {
            value += 9;
        }
        return value;
    })() == 21;

    assert (def () {
        let value = 0;
        try {
            value += 5;
            assert false;
        } finally {
            return value + 7;
        }
        assert false, 'unreachable';
    })() == 12;

    assert (def () {
        let value = 0;
        try {
            value += 5;
            assert false;
        } recover e {
            value = value + 7;
            throw e;
        } finally {
            return value + 9;
        }
        assert false, 'unreachable';
    })() == 21;

    assert (def () {
        let value = 0;
        try {
            try {
                value += 5;
                assert false;
            } finally {
                value += 7;
            }
        } recover {
            return value;
        }
        assert false, 'unreachable';
    })() == 12;
}

def test_throw() {
    let value = 0;
    try {
        throw 5;
        assert false, 'unreachable';
    } recover e {
        assert e == 5;
        value = e + 7;
    }
    assert value == 12;
}

def factorial_iterative(x) {
    let product = 1;
    loop while x > 0 {
        product *= x;
        x -= 1;
    }
    return product;
}

def test_loops() {
    let result = 0;

    result += 1;
    loop {
        result += 2;
        break;
        assert 0, 'unreachable';
    }
    result += 3;

    assert result == 1 + 2 + 3;

    result = 0;

    loop {
        if result < 10 {
            result += 1;
            continue;
            assert 0, 'unreachable';
        }
        break;
        assert 0, 'unreachable';
    }

    assert result == 10;

    result = 0;

    loop {
        result += 1;

        if result > 100 {
            break;
        }
    }

    assert result == 101;

    loop while false {
        assert false, 'unreachable';
    }

    assert factorial_iterative(0) == 1;
    assert factorial_iterative(1) == 1;
    assert factorial_iterative(2) == 2;
    assert factorial_iterative(3) == 6;
    assert factorial_iterative(4) == 24;
    assert factorial_iterative(5) == 120;
    assert factorial_iterative(6) == 720;
    assert factorial_iterative(7) == 5040;
    assert factorial_iterative(8) == 40320;
    assert factorial_iterative(9) == 362880;
    assert factorial_iterative(10) == 3628800;

    result = 0;

    loop : outer {
        result += 2;

        loop {
            result *= 2;

            break outer;
            assert false, 'unreachable';
        }

        assert false, 'unreachable';
    }

    assert result == 4;
}

def test_local_import() {
    import builtin = blt;
    let print = blt.print;
    import builtin { println };
    assert blt.println == println;
}

def test_lambda_functions() {
    let x = 5;
    let y = 6;

    assert (def (a, b) { return a * b; })(5, 6) == 30;
    assert (def () use (x, y) { return x * y; })() == 30;
    assert (def () { return def () { return 123; }; })()() == 123;

    let add = def (a, b) { return a + b; };
    let foo = def (z) use (x, y, add) { return add(x * y, z); };

    assert add(x, y) == 11;
    assert foo(3) == 33;

    x = 0;
    y = 0;

    assert add(x, y) == 0;
    assert foo(3) == 33;

    let factorial = def (func, x) {
        if x == 0 { return 1; }
        return x * func(func, x - 1);
    };

    assert factorial(factorial, 5) == 120;
}

class Self {
    constructor(self) {
    }

    def get_self(self) {
        return self;
    }
}

class Foo : Self {
    constructor(self, value) {
        self.value = value;
    }

    def foo(self) {
        return 'foo';
    }
}

class Bar : Foo {
    constructor(self, value) {
        self.value = value;
    }

    def foo(self) {
        return 'oof';
    }

    def bar(self) {
        return 'bar';
    }
}

class Multi : Foo, Bar {
    constructor(self, a, b) {
        super[Foo](a);
        super[Bar](b);
        self.a = a;
        self.b = b;
    }
}

def test_class_instantiate() {
    let foo = new Foo(12);
    let bar = new Bar(34);

    assert Foo.get_self(foo) == foo;
    assert foo.get_self() == foo;
    assert foo.value == 12;

    assert Bar.get_self(bar) == bar;
    assert bar.get_self() == bar;
    assert bar.value == 34;

    assert foo.foo() == 'foo';
    assert bar.foo() == 'oof';
    assert bar.bar() == 'bar';

    let mul = new Multi(5, 7);

    assert mul.foo() == 'oof';
    assert mul.bar() == 'bar';
    assert mul.a == 5;
    assert mul.b == 7;
    assert mul.value == 7;
}

def test_ranged_loop() {
    let acc = 0;

    for i in 0..10 {
        acc += i;
    }

    assert acc == 45;

    acc = 0;

    for i in 0..=10 {
        acc += i;
    }

    assert acc == 55;

    acc = 0;

    for i in 10..0 {
        acc += i;
    }

    assert acc == 55;

    acc = 0;

    for i in 100..=1 {
        if i == 10 { break; }
        acc += i;
    }

    assert acc == 4995;

    let begin = 10;
    let end = 20;

    acc = 0;

    for i in begin..end {
        acc += i;
    }

    assert acc == 145;
}

def test_array_access() {
    let arr = [1, 2, 3];
    assert arr[0] == 1;
    assert arr[1] == 2;
    assert arr[2] == 3;
    arr[0] = none;
    assert arr[0] == none;
    try {
        arr[-1] = none;
        assert false, 'unreachable';
    } recover { }
}

def test_none_type() {
    assert none == none;
    if none {
        assert false, 'unreachable';
    }
    let stub = def () { };
    assert stub() == none;
}

def test_variadic_call() {
    (def (a, b, c, args...) {
        assert a == 1;
        assert b == 2;
        assert c == 3;
        assert args.length == 2;
        assert args[0+0] == 4;
        assert args[10-9] == 5;
    })(1, 2, 3, 4, 5);

    (def (a, b...) {
        assert a == 1;
        assert b[0] == 2;
    })(1, 2);

    (def (a, b, c...) {
        assert a == 1;
        assert b == 2;
        assert c[0] == 3;
    })(1, 2, 3);

    (def (a, b, c...) {
        assert a == 1;
        assert b == 2;
        assert c[0] == 3;
        assert c[1] == 4;
    })(1, 2, 3, 4);

    (def (a...) {
        assert a.length == 0;
    })();

    (def (a...) {
        assert a[0] == 1;
    })(1);

    (def (a...) {
        assert a[0] == 1;
        assert a[1] == 2;
    })(1, 2);
}

def test_iterator_range() {
    import iterator;
    import range;

    let index = 10;

    let it = new range.Range(index, 0, false).get_iterator();
    assert it is iterator.Iterator;
    assert it is range.DescendingRangeIterator;

    loop while it.has_next() {
        assert it.get_next() == index;
        index -= 1;
    }

    assert index == 0;
}

def test_unwrap_operator() {
    let helper = def (a, b, c) {
        return a? + b? + c?;
    };

    assert helper(none, none, none) == none;
    assert helper(1, 2, none) == none;
    assert helper(none, 2, 3) == none;
    assert helper(1, 2, 3) == 6;

    let helper_call = def (value) {
        return value()?()?()?;
    };

    assert helper_call(def () { return none; }) == none;
    assert helper_call(def () { return def () { return none; }; }) == none;
    assert helper_call(def () { return def () { return def () { return none; }; }; }) == none;
    assert helper_call(def () { return def () { return def () { return 123; }; }; }) == 123;
}

def test_parse_json() {
    import json;

    let result = json.parse('[{"id":1,"first_name":"Hermie","last_name":"Groomebridge"}]');
    assert result.length == 1;
    assert result[0].length == 3;
    assert result[0][0] == ['id', '1'];
    assert result[0][1] == ['first_name', 'Hermie'];
    assert result[0][2] == ['last_name', 'Groomebridge'];
}

def test_instance_of() {
    let s = new Self();
    assert s is Self;
    assert not (s is Foo);
    assert not (s is Bar);
    assert not (s is Multi);
    let f = new Foo(1);
    assert f is Foo;
    assert f is Self;
    assert not (f is Bar);
    assert not (f is Multi);
    let b = new Bar(2);
    assert b is Foo;
    assert b is Bar;
    assert b is Self;
    assert not (b is Multi);
    let m = new Multi(1, 2);
    assert m is Multi;
    assert m is Foo;
    assert m is Bar;
    assert m is Self;
}

def test_cycling_string_representation() {
    {
        let a = ['a', 'b'];
        assert std.to_string(a) == '[\'a\', \'b\']';
    }

    {
        let a = ['a', none];
        a[1] = a;
        assert std.to_string(a) == '[\'a\', [...]]';
    }

    {
        let a = ['a', none];
        a[0] = a;
        a[1] = a;
        assert std.to_string(a) == '[[...], [...]]';
    }

    {
        let a = ['a', none];
        let b = ['b', none];
        a[1] = b;
        b[1] = a;

        assert std.to_string(a) == '[\'a\', [\'b\', [...]]]';
        assert std.to_string(b) == '[\'b\', [\'a\', [...]]]';
    }
}

def main() {
    import test;

    test.init();
    test.pass('Local variables and slot binding', test_locals);
    test.pass('Operator precedence', test_precedence);
    test.pass('Relational operators', test_relational);
    test.pass('Assignment operators', test_assignment);
    test.pass('Unary operators', test_unary);
    test.pass('If-else branching', test_branch);
    test.pass('Argument evaluation order', test_eval_order);
    test.pass('Execute factorial', test_factorial);
    test.pass('Execute fibonacci', test_fibonacci);
    test.pass('String interpolation', test_interpolation);
    test.pass('Panic recovery', test_panic_recover);
    test.pass('Try & Finally', test_try_finally);
    test.pass('Throw & Recover', test_throw);
    test.pass('Loop while & break & continue', test_loops);
    test.pass('Local import', test_local_import);
    test.pass('Anonymous functions & variable capturing', test_lambda_functions);
    test.pass('Class single/multi inheritance & instantiation', test_class_instantiate);
    test.pass('None type', test_none_type);
    test.pass('Variadic arguments count', test_variadic_call);
    test.pass('Iterator & Range class', test_iterator_range);
    test.pass('Ranged exclusive/inclusive loops', test_ranged_loop);
    test.pass('Array access & assignment', test_array_access);
    test.pass('Unwrap operator', test_unwrap_operator);
    test.pass('Parse json', test_parse_json);
    test.pass('Instance of', test_instance_of);
    test.pass('Cyclic string representation', test_cycling_string_representation);
    test.fail('Throw value', def () { throw 'hello'; });
    test.fail('Fail assert', def () { assert false; });
    test.fail('No such attribute', def () { std.println(none.hello); });
    test.fail('No such global', def () { std.println(hello); });
    test.fail('Index accessing', def () { none[0]; });
    test.fail('Index assignment', def () { none[0] = 'a'; });
    test.finish();

#    std.println('𐍈 = \u{10348} (UTF-8)');
#    std.println('𐍈 = \u{f0}\u{90}\u{8d}\u{88} (UTF-8)');
#    std.println('1e-6 = \{1e-6}');
#    std.println('100_000_000 * 1e-6 = \{100_000_000 * 1e-6}');
#    std.println('100_000_000 / 1e+6 = \{100_000_000 / 1e+6}');
#    std.println('подожди, это всё юникод? always has been \u{1f921}');

#    let rem = def (num, divisor) { return num - divisor * (num / divisor); };
#    for num in 1..100 {
#        if rem(num, 3) == 0 { std.print('fizz'); }
#        if rem(num, 5) == 0 { std.print('buzz'); }
#        if rem(num, 3) != 0 and rem(num, 5) != 0 { std.print(num); }
#        std.println();
#    }
}
