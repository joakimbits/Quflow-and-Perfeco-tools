"""
RPN calculator

Operators are (currently) defined like this:
operators['+'] = operator(2, lambda x, y: x + y)
operators['not'] = operator(1, lambda x: not x)
"""


from sympy import sympify, Basic, Function
from sympy.utilities.lambdify import implemented_function


stack = []
declarations = []
operators = {}
substitutions = {}
replacements = {}


def use(e):
    """sympify, substitute and replace e"""
    e = sympify(e).subs(substitutions)
    for r in replacements.items():
        e = e.replace(*r)
    return e


def assignment():
    """value name --- ; substitutions[name] = use(value)"""
    name = stack.pop()
    substitutions[name] = use(stack.pop())


def operator(n, op):
    """pars[-n:] --- op(*pars)"""
    return lambda: stack.append(op(*reversed([use(stack.pop()) for i in range(n)])))


def declare():
    """pars[-n:] name n --- ; Begin capturing code for a function <name>(*pars)"""
    global stack
    n, name = int(stack.pop()), stack.pop()
    names, stack = stack[-n:], stack[:-n]
    declarations.append((name, names, []))


def define():
    """ --- ; End capturing code for a function"""
    name, names, code = declarations.pop()
    expression = execute(code).pop()
    f = eval("lambda %s: %r" % (", ".join(names), expression))
    replacements[Function(name)] = implemented_function(name, f)


def call():
    """pars[-n:] name n --- <name>(*pars)"""
    n, name = int(stack.pop()), stack.pop()
    f = replacements.get(name, None) or Function(name)
    return stack.append(f(*reversed([use(stack.pop()) for i in range(n)])))


def parse(line):
    commands = []
    for t in line.split():
        # next line, using default parameters, is intentional.
        commands.append(operators.get(t, lambda t=t: stack.append(t)))
    return commands


def execute(code):
    for t in code:
        if declarations and t != define:
            declarations[-1][-1].append(t)
        elif isinstance(t, Basic):
            stack.append(t)
        else:
            t()
    return stack


operators.update({
    '+': operator(2, lambda x, y: x + y),
    '-': operator(2, lambda x, y: x - y),
    '*': operator(2, lambda x, y: x * y),
    '/': operator(2, lambda x, y: x / y),
    '=': assignment,
    ':': declare,
    ';': define,
    '()': call})


def rpn(line, raw=False):
    """
    Parse and execute math in reverse polish notation for a global stack and context

    The line can contain operands (pushed to the stack) and operators on the stack.

    rpn   --- result
    ================
    a b + --- a+b
    a b - --- a-b
    a b * --- a*b
    a b / --- a/b
    a b = b --- a
    x y f 2 : x y * ; a b f 2 () --- a*b

    Notes:
    * Operands are sympified and evaluated within the current context
    (variable substitutions and function replacements) before being used.
    * The stack is (by default) numerically evaluated before being returned.
    * If raw=True, the stack and context is returned as-is.

    >>> rpn('2 pi * 1 2 / -')
    [5.78318530717959]
    >>> rpn('b a + fun 2 () 2 3 fun 2 ()')
    [fun(-1/2 + 2*pi, a + b), fun(2, 3)]
    >>> rpn('0 a =')
    [fun(-1/2 + 2*pi, b), fun(2, 3)]
    >>> rpn('x y fun 2 : x y * ;')
    [fun(-1/2 + 2*pi, b), 6.00000000000000]
    >>> rpn('1 b =')
    [5.78318530717959, 6.00000000000000]
    >>> rpn('', raw=True)
    ([fun(-1/2 + 2*pi, a + b), fun(2, 3)], {'a': 0, 'b': 1}, {fun: fun})
    """
    execute(parse(line))
    if raw:
        return stack, substitutions, replacements
    return [use(t).evalf() for t in stack]


if __name__ == '__main__':
    import doctest
    doctest.testmod()