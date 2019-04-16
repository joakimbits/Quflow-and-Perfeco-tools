"""
Smath tools

First step: Handle a sequence of operands and operators in reverse polish notation

ToDo: def sm2py(s): "Convert s (text or filename) from Smath syntax to python3 syntax. Fails if not Python compatible."
ToDo: def py2sm(s): "Convert s (text or filename) from python3 syntax to Smath syntax. Fails if not Smath compatible."
"""

# from xml.etree.ElementTree import parse, iterparse
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
    """pars[-n:] name n --- ; Declare a function and begin capturing code for it"""
    global stack
    n, name = int(stack.pop()), stack.pop()
    names, stack = stack[-n:], stack[:-n]
    declarations.append((name, names, []))


def define():
    """ --- ; End capturing code and define a function"""
    name, names, code = declarations.pop()
    expression = execute(code).pop()
    f = eval("lambda %s: %r" % (", ".join(names), expression))
    replacements[Function(name)] = implemented_function(name, f)


def call():
    """pars[-n:] name n --- name(*pars)"""
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


def rpn(line):
    """
    >>> rpn('2 pi * 1 2 / -')
    [5.78318530717959]
    >>> rpn('b a + fun 2 () 2 3 fun 2 ()')
    [fun(5.78318530717959, a + b), fun(2, 3)]
    >>> rpn('0 a =')
    [fun(5.78318530717959, b), fun(2, 3)]
    >>> rpn('x y fun 2 : x y * ;')
    [fun(5.78318530717959, b), 6.00000000000000]
    >>> rpn('1 b =')
    [5.78318530717959, 6.00000000000000]
    """
    execute(parse(line))
    for i, t in enumerate(stack):
        stack[i] = use(t).evalf()
    return stack


if __name__ == '__main__':
    import doctest
    doctest.testmod()
