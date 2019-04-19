"""
RPN calculator

Generates a python trace of all defined variables and functions

Operators are (currently) defined like this:
operators['+'] = operator(2, lambda x, y: x + y)
operators['not'] = operator(1, lambda x: not x)
"""
from sympy import sympify, Basic, Function, lambdify
from sympy.utilities.lambdify import implemented_function
from textwrap import indent


operators = {}


class _:
    line = 0
    trace = []
    stack = []
    declarations = []
    substitutions = {}
    replacements = {}


def clear():
    """Forget everything"""
    _.line = 0
    for m in _.trace, _.stack, _.substitutions, _.replacements:
        m.clear()


def push(e=None):
    """ --- e"""
    _.stack.append((e, len(_.trace), _.substitutions.copy(), _.replacements.copy()))


def pop():
    """e ---"""
    e, _.line, _.substitutions, _.replacements = _.stack.pop()
    return e


def use(e):
    """sympify, substitute and replace e"""
    e = sympify(e).subs(_.substitutions)
    for r in _.replacements.items():
        try:
            e = e.replace(*r)
        except TypeError as err:
            pass
    return e


def operator(n, op):
    """pars[-n:] --- op(*pars)"""
    return lambda: push(op(*reversed([use(pop()) for i in range(n)])))


def bracket():
    """e --- [e]"""
    push([pop()])


def function():
    """pars[-n:] name n --- <name>(*pars)"""
    n, name = int(pop()), pop()
    if name == 'if':
        name = 'if_'
    f = _.replacements.get(name, None) or Function(name)
    return push(f(*reversed([use(pop()) for i in range(n)])))


def assignment():
    """object expression --- ; use expression for object and trace it as python"""
    expr, obj = pop(), pop()
    if isinstance(expr, Function) and expr.name == 'line':
        expr = expr.args[-3]  # SMath line: <assignments>, expr, <two numbers>.
    if isinstance(obj, Function):
        _.trace = _.trace[:_.line] \
                + ["def %r:" % obj] \
                + [indent(r, "    ") for r in _.trace[_.line:]] \
                + ["    return %r" % expr]
        try:
            tmp = {}
            exec("\n".join(_.trace[_.line:]), {**locals(), **globals()}, tmp)
            f, = tmp.values()
        except:
            f = lambda: NotImplementedError
        obj = Function(obj.name)
        try:
            _.replacements[obj] = implemented_function(obj, f)
            push(Function('let')(obj, _.replacements[obj]))
        except SyntaxError as err:
            pass
    else:
        if len(_.trace) > _.line:
            _.trace = _.trace[:_.line] \
                    + ["def %s():" % obj] \
                    + [indent(r, "    ") for r in _.trace[_.line:]] \
                    + ["    return %r" % expr] \
                    + ["%s = %s()" % (obj, obj)]
        else:
            _.trace.append("%s = %s" % (obj, expr))
        #substitutions[str(obj)] = expr
        try:
            push(Function('let')(obj, expr))
        except Exception as err:
            raise type(err)("%s\n%r %r" % (err, obj, expr))
    _.line = len(_.trace)


operators['('] = bracket
operators[')'] = function
operators[':'] = assignment
for op in "+ - * / % ** // == != > < >= <= & | ^ << >> and or in is".split() + ['not in', 'is not']:
    operators[op] = operator(2, eval("lambda x, y: x %s y" % op))
for op in "~ not".split():
    operators[op] = operator(1, eval("lambda x: %s y" % op))


def execute(*math):
    for e in math:
        op = operators.get(e, None)
        if op:
            op()
        else:
            push(e)
    return _.stack


def rpn(line=""):
    r"""Parse and execute a line of math in reverse polish notation
    Returns a numerically evaluated copy of the stack.

    Operands are appended as-is (here: strings) to the global stack, and operators modify the end of the stack.
    Operands are sympified and evaluated within the current context when consumed by an operator.

    >>> rpn('2 pi * 1 2 / -')
    [5.78318530717959]
    >>> rpn('b a + fun 2 ) 2 3 fun 2 )')
    [fun(-1/2 + 2*pi, a + b), fun(2, 3)]
    >>> rpn('a 0 :'), trace
    ([fun(-1/2 + 2*pi, b), fun(2, 3)], ['a = 0'])
    >>> rpn('x y fun 2 ) x y * :'), trace[-1]
    ([fun(-1/2 + 2*pi, b), 6.00000000000000], 'fun = lambda x, y: x*y')
    >>> rpn('b 1 :'), trace[-1]
    ([5.78318530717959, 6.00000000000000], 'b = 1')
    >>> stack, substitutions, replacements
    ([fun(-1/2 + 2*pi, a + b), fun(2, 3)], {'a': 0, 'b': 1}, {fun: fun})
    >>> for line in trace: print(line)
    a = 0
    fun = lambda x, y: x*y
    b = 1
    >>> for e in stack: print(e, "==", use(e).evalf())
    fun(-1/2 + 2*pi, a + b) == 5.78318530717959
    fun(2, 3) == 6.00000000000000
   """
    execute(*line.split())
    return [use(t).evalf() for t in _.stack]
