"""
Implicit transmitter requirements

Defines:

    Inf         Unlimited.
    NaN         Not applicable.
    symetric    Mirror shape to a symetric shape.
    Absolute    Transmitter-independent requriements.
    InBand      Carrier-dependent requirement within a band.
    OutOfBand   Band-dependent requirements.
    Combined    Combine implicit requriements.
    testCases   Generate a set of testcases from requirements and stimuli.

Implicit requirements are specified with

req ::= Absolute|InBand|OutOfBand ({kHz: [(dBm,MHz),...]}, ** setup)
req ::= Combined (req, req, ..., ** setup
req ::= Combined (extend=True, req, req, ..., ** setup

where

Absolute (...) specifies transmitter-independent requriements.
InBand (...) specifies carrier-dependent requirement within a band.
OutOfBand (...) specifies band-dependent requirements.
setup are any other named options applicable to the requirement.

Combined (...) combines several implicit requriements.
extend=True extends the applicable frequency ranges rather than reducing them.

NaN: (kHz) means the shape [(dBm,MHz),...] is applicable for all setups.
  x: (kHz) means the shape is applicable at resolution bandwidth x kHz.
[..., (NaN,f0), (NaN,f1), ...] means the interval f0-f1 MHz is not applicable.
[..., (Inf,f0), (Inf,f1), ...] means the interval f0-f1 MHz is applicable.
[..., (  x,f0), (  x,f1), ...] specifies a max limit x dBM in interval f0-f1 MHz.

These requirements behave as functions that return a table of setups and
associated output power limit (polygon of dBm vs Hz). Each setup is a table
of options and their values to be used during a measurement.

These implicit requirements has the following call parameters:

    carrier (Hz), band=(800e6, 900e6) -> {setup: [(dBm,Hz), ...}]

The function 'testCases' can be used to apply different stimuli (carriers
and bands) on a set of implicit requirements. The function returns one lookup
table with explicit test limits as above, for each combination of stimuli. The
function has the following call parameters:

    * requirements, ** stimuli_ranges -> {stimuli: {setup: limits,...},...}

Stimuli specified by a list of values are expanded into differnt test cases:

>>> LB = [800, 850, 900]
>>> testCases (Req['stability'], carrier=list (LB), band=tuple (LB))


"""


## Utilities
# http://www.python.org/tim_one/000350.html

from copy import deepcopy
from UserDict import UserDict 

class Frozen:
    """
    Wrapper for non-hashable objects 
    """
    def __init__(self, obj): 
        self.obj = deepcopy(obj) 
    def __hash__(self):
        return 0
    def __cmp__(self, other):
        if isinstance (other, Frozen):
            return cmp (self.obj, other.obj)
        else:
            return cmp (self.obj, other)
    def __repr__(self): 
        return repr(self.obj)
    def guts(self): 
        return self.obj
    def __getattr__(self, attr):
        return getattr (self.obj, attr)

class SemiFrozen (Frozen):
    """
    Like Frozen, but doesn't make a copy
    Serves to make a value # "look like" it's frozen (but cheaply) just long
    enough for Python # internals to do comparisons
    """
    def __init__(self, obj): 
        self.obj = obj

def wrap_key (key, loosely = 1):
    try: 
        hash (key) 
    except TypeError: 
        key = (Frozen, SemiFrozen)[loosely] (key) 
    return key

class HashableDict (UserDict):
    def __getitem__(self, key):
        return self.data[wrap_key (key)]
    def __setitem__(self, key, item): 
        self.data[wrap_key (key, 0)] = item 
    def __delitem__(self, key): 
        del self.data[wrap_key (key)] 
    def keys(self): 
        temp = self.data.keys () 
        for i in range (len (temp)): 
            val = temp[i] 
            try: 
                if val.__class__ is Frozen: 
                    temp[i] = val.guts () 
            except: 
                pass 
        return temp 
    # something similar should be done for "items" 
    # def items(self): ... 
    def has_key(self, key): 
        return self.data.has_key (wrap_key(key))


## Requirement definition language

Inf = 1e300 * 1e300 # http://mail.python.org/pipermail/python-bugs-list/2004-December/026846.html
NaN = -(Inf - Inf)

class Requirement:
    """
    ** stimuli -> {setup: expected response}
    Base class for requirements
    """

class Stability (Requirement):
    """
    ** transmitter -> {analyzer settings: max spectral power [dBm,Hz]}
    Base class for requirements on spurious emissions
    """

class Absolute (Stability):
    """
    -> {bw (Hz): max spectral power [dBm,Hz]}
    Absolute requirements on spurious emissions
    """
    def __init__(self, limits, ** analyzer):
        """
        {Measurement bandwith (kHz): Power limit (polygon) [dBm,MHz]}, ** other analyzer settings
        """
        self.limits = HashableDict ()
        for bw, shape in limits.iteritems ():
            if bw: bw *= 1e3 
            analyzer.update ({'bw': bw})
            points = shape[:]
            for i, (p, f) in enumerate (points):
                if f != None:
                    points[i] = (p, f * 1e6)
            self.limits[analyzer] = points
    def __call__(self, ** ignore):
        return self.limits

class InBand (Stability):
    """
    carrier (Hz), band [Hz] -> {bw (Hz): max spectral power [dBm,Hz]}
    Relative requirements on in-band spurious emissions
    """
    def __init__(self, shapes, ** analyzer):
        """
        {Measurement bandwith (kHz): Power limit (polygon) centered around carrier [dBm,MHz]}, ** analyzer settings
        """
        self.shapes = shapes
        self.analyzer = analyzer
    def __call__(self, carrier, band, ** ignore):
        limit = HashableDict ()
        f0 = min (band)
        f1 = max (band)
        interval = [(NaN, -Inf), (NaN, f0), (Inf, f0), (Inf, f1), (NaN, f1), (NaN, Inf)]
        for bw, shape in self.shapes.iteritems ():
            if bw: bw *= 1e3 
            self.analyzer.update ({'bw': bw})
            points = shape[:]
            for i, (p, f) in enumerate (points):
                if f != None:
                    points[i] = (p, carrier + f * 1e6)
            limit[self.analyzer] = combine (interval, points)
        return limit

class OutOfBand (Stability):
    """
    band (Hz) -> {bw (Hz): max spectral power [dBm,Hz]}
    Relative requirements on out-of-band spurious emissions
    """
    def __init__(self, shapes, ** analyzer):
        """
        {Measurement bandwith (kHz): Power limit (polygon) centered around band [dBm,MHz]}
        """
        self.shapes = shapes
        self.analyzer = analyzer
    def __call__(self, band, ** ignore):
        limit = HashableDict ()
        for bw, shape in self.shapes.iteritems ():
            if bw: bw *= 1e3 
            self.analyzer.update ({'bw': bw})
            points = shape[:]
            edge = -1
            for i, (p, f) in enumerate (points):
                if f < 0: edge = 0
                if f > 0: edge = -1
                if f != None:
                    points[i] = (p, band[edge] + f * 1e6)
            limit[self.analyzer] = points
        return limit

class Combined (Stability):
    """
    carrier=(Hz), band=[Hz] -> {bw(Hz): maxPower[dBm,Hz]}
    Init: [extend=False,] * stabilities [, ** setup={}]
    Each stability can reduce dBm values. If extend=True, applicable ranges
    are extended, otherwise reduced. bw=None applies to all bandwidth.
    """
    def __init__(self, * stabilities, ** setup):
        """
        [extend=False,] * stabilities [, ** setup]
        """
        self.stabilities = stabilities
        if setup.pop ('extend', False):
            self.extend = {'extend': True}
        else:
            self.extend = {}
        self.setup = setup
    def __call__(self, ** transmitter):
        explicits = [stability (** transmitter) for stability in self.stabilities]
        if self.extend:
            general = [(NaN, -Inf), (NaN, Inf)]
        else:
            general = [(Inf, -Inf), (Inf, Inf)]
        generalSetup = {'bw': None}
        for explicit in explicits:
            for setup, limit in explicit.iteritems ():
                if setup == generalSetup:
                    general = combine (general, limit, ** self.extend)
        combined = HashableDict ()
        combined[generalSetup] = general
        for explicit in explicits:
            for setup, limit in explicit.iteritems ():
                if setup.get ('bw', None) != None:
                    s = self.setup.copy ()
                    s.update (setup)
                    combined.setdefault (s, general)
                    combined[s] = combine (combined[s], limit, ** self.extend)
        if len (combined) > 1:
            del combined[generalSetup]
        return combined

def symetric (shape):
    """
    shape for positive frequencies [dBm,MHz] -> symetric shape [dBm,MHz]
    """
    return [(p, -f) for p, f in shape[::-1]] + shape

def combine (* shapes, ** extend):
    """
    extend=False, * shapes [dBm,Hz] -> combined shape [dBm,Hz]

    Reduce to lowest values and smallest applicable ranges.
    Extend the applicable ranges if extend=True.
    
    [..., (NaN,f0), (NaN,f1), ...] means the frequency interval f0-f1 MHz is not applicable.
    [..., (Inf,f0), (Inf,f1), ...] means the frequency interval f0-f1 MHz is applicable.
    [..., (  x,f0), (  x,f1), ...] specifies a max limit x dBM in the frequency interval f0-f1.
    """
    glue = extend == {'extend': True}
    cut = not glue
    combined = shapes[0][:]
    for shape in shapes[1:]:
        shape = shape[:]
        if shape[0][1] < combined[0][1]:
            combined, shape = shape, combined
        c = 0
        while c < len (combined) and len (shape) >= 2:
            (p0, f0), (p1, f1) = combined[c:c+2]
            (P0, F0), (P1, F1) = shape[:2]
            if F1 < f0:
                shape = shape[2:]
            elif f1 < F0:
                c += 2
            elif p0 == p1 and (
                P0 == P1 < p0 or (cut and P0 != P0 != P1 != P1)) or (
                    glue and P0 == P1 != p0 != p0 != p1 != p1):
                if F0 <= f0:
                    combined[c:], shape = [(P0, f0)] + shape[1:], combined[c:]
                elif f1 <= F1:
                    combined[c+1:], shape = [(p1, F0)] + shape, combined[c:]
                    c += 2
                else:
                    combined[c+1:c+1], shape = [(p1, F0)] + shape[:2] + [(p1, F1)], shape[2:]
                    c += 2
            elif (f0 <= F0 and f1 <= F1) or (cut and p0 != p0 != p1 != p1):
                c += 2
            else:
                shape = shape[2:]
    return combined

def testCases (* requirements, ** stimuli):
    """
    * requirements, ** stimuli -> {{setup: [(dBm,Hz),...],...},...}
    Return a lookup table with explicit test limits for each combination
    of stimuli that shiould be applied to the implicit requirements. Stimuli
    ranges are specified by lists.
    """
    if len (requirements) > 1:
        requirement = Combined (extend=True, * requirements)
    else:
        requirement = requirements[0]
    cases = HashableDict ()
    for name, values in stimuli.iteritems ():
        if type(values) is list:
            for value in values:
                stimuli[name] = value
                cases.update (testCases (requirement, ** stimuli))
            return cases
    cases[stimuli] = requirement (** stimuli)
    return cases
