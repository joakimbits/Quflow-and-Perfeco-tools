"""
Spurious emissions according to 3GPP TS 45.005 V5.10.0 (2004-11)

Defines:

    Req         Look-up table with implicit stability requirements.

The imlicit requirements behave as functions that return a table of setups and
associated output power limit (polygon of dBm vs Hz). 

The implicit requirement defined in Req has the following call parameters:

    carrier (Hz), band=(800e6, 900e6) -> {setup: [(dBm,Hz)}

Each setup is a table of options and their value to be used during a
measurement. Setups includes zero or more of the following options:

    'bw':       Resolution bandwidth (Hz).
    'average':  Minimum # traces to average.
    'filterBW': Filter bandwidth (Hz).
    'videoBW':  Video bandwidth (Hz)}
"""

from implicit import Inf, NaN, symetric, testCases, \
    Absolute, InBand, OutOfBand, Combined

try:
    Req
except:
    Req = {}

"""
req ::= Absolute|InBand|OutOfBand ({kHz: [(dBm,MHz),...]}, ** setup)
req ::= Combined (req, req, ...)
req ::= Combined (extend=True, req, req, ...)

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
"""

Req['3GPP 4.3.1'] = {
    'in-band': InBand({
        30: symetric([(NaN, 1.8),
                      (Inf, 1.8), (Inf, 6),
                      (NaN, 6), (NaN, Inf)]),
        100: symetric([(NaN, 6),
                       (Inf, 6), (Inf, Inf)])}),
    '100 kHz to 50 MHz': Absolute({
        10: [
            (NaN, -Inf), (NaN, .1),
            (Inf, .1), (Inf, 50),
            (NaN, 50), (NaN, Inf)]}),
    '50 MHz to 500 MHz out-of-band': Combined(
        Absolute({
            None: [
                (NaN, -Inf), (NaN, 50),
                (Inf, 50), (Inf, 500),
                (NaN, 500), (NaN, Inf)]}),
        OutOfBand({
            30: symetric([(NaN, 2),
                          (Inf, 2), (Inf, 5),
                          (NaN, 5), (NaN, Inf)]),
            100: symetric([(NaN, 5),
                           (Inf, 5), (Inf, Inf)])})),
    '>500 MHz out-of-band': Combined(
        Absolute({
            None: [
                (NaN, -Inf), (NaN, 500),
                (Inf, 500), (Inf, Inf)]}),
        OutOfBand({
            30: symetric([(NaN, 2),
                          (Inf, 2), (Inf, 5),
                          (NaN, 5), (NaN, Inf)]),
            100: symetric([(NaN, 5),
                           (Inf, 5), (Inf, 10),
                           (NaN, 10), (NaN, Inf)]),
            300: symetric([(NaN, 10),
                           (Inf, 10), (Inf, 20),
                           (NaN, 20), (NaN, Inf)]),
            1000: symetric([(NaN, 20),
                            (Inf, 20), (Inf, 30),
                            (NaN, 30), (NaN, Inf)]),
            3000: symetric([(NaN, 30),
                            (Inf, 30), (Inf, Inf)])})),
    'General': (
        Absolute({
            None: [
                (NaN, -Inf), (NaN, 30),
                (Inf, 30), (Inf, 4000),
                (NaN, 4000), (NaN, Inf)]}))}

Req['3GPP 4.3.1 a'] = Combined(
    Req['3GPP 4.3.1']['General'],
    Req['3GPP 4.3.1']['in-band'])

Req['3GPP 4.3.1 b'] = Combined(
    Req['3GPP 4.3.1']['General'],
    Combined(
        extend=True,
        *[Req['3GPP 4.3.1'][i] for i in [
            '100 kHz to 50 MHz',
            '50 MHz to 500 MHz out-of-band',
            '>500 MHz out-of-band']]))

Req['3GPP 4.3.3.1'] = {
    'in-band': Combined(
        Req['3GPP 4.3.1 a'],
        Absolute({
            None: [
                (-36, 0), (-36, Inf)]})),
    'out-of-band': Combined(
        Req['3GPP 4.3.1 b'],
        Absolute({
            None: [
                (-36, .009), (-36, 1000),
                (-30, 1000), (-30, 12750)]})),
    'in-band for R-GSM 900': Combined(
        Req['3GPP 4.3.1 a'],
        Absolute({
            None: [
                (-42, 0), (-42, Inf)]})),
    'idle mode, 100-kHz bandwidth': Combined(
        Req['3GPP 4.3.1 b'],
        Absolute({
            100: [
                (-57, .009), (-57, 1000),
                (-47, 1000), (-47, 12750)]}),
        Absolute({
            100: [
                (-59, 880), (-59, 915),
                (NaN, 915), (NaN, 1710),
                (-53, 1710), (-53, 1785),
                (NaN, 1785), (NaN, 1900),
                (-76, 1900), (-76, 1980),
                (NaN, 1980), (NaN, 2010),
                (-76, 2010), (-76, 2025),
                (NaN, 2025), (NaN, 2110),
                (-76, 2110), (-76, 2170)]}),
        carrier=None),
    '100-kHz bandwidth': Combined(
        Req['3GPP 4.3.1 b'],
        Absolute({
            100: [
                (-60, 921), (-60, 925),
                (-67, 925), (-67, 935),
                (-79, 935), (-79, 960),
                (NaN, 960), (NaN, 1805),
                (-71, 1805), (-71, 1880),
                (NaN, 1880), (NaN, 1900),
                (-66, 1900), (-66, 1980),
                (NaN, 1980), (NaN, 2010),
                (-66, 2010), (-66, 2025),
                (NaN, 2025), (NaN, 2110),
                (-66, 2110), (-66, 2170)]},
            average=50,  # minimum # traces to average
            filterBW=100e3,  # filter bandwidth (Hz)
            videoBW=100e3))}  # video bandwidth (Hz)

Req['stability'] = Combined(
    Req['3GPP 4.3.3.1']['in-band'],
    Req['3GPP 4.3.3.1']['out-of-band'],
    extend=True)


def describe(setup):
    params = []
    for key, value in setup.items():
        if key == 'carrier':
            if value:
                params.append("with %g MHz" % (value / 1e6))
            else:
                params.append("without RF output")
        elif key == 'band':
            params.append("for %g-%g MHz" % tuple([x / 1e6 for x in value]))
        elif key == 'bw':
            if value:
                params.append("at %g kHz" % (value / 1e3))
        else:
            params.append("%s=%s" % (key, str(value)))
    return " ".join(params)


def view(*reqPath, **transmitter):
    req = Req
    for name in reqPath:
        try:
            req = req[name]
        except:
            req = req.__dict__[name]
    if type(req) is dict:
        r = req.keys()
        r.sort()
        return [view(*(list(reqPath) + [i]), **transmitter) for i in r]
    else:
        req.path = reqPath
        transmitter.setdefault('carrier', 850e6)
        transmitter.setdefault('band', [800e6, 900e6])
        print
        "%s - %s %s:" % (
            ' '.join(reqPath),
            req.__class__.__name__,
            describe(transmitter))
        dump(req, **transmitter)
        return req


def dump(req, **transmitter):
    transmitter.setdefault('carrier', 849e6)
    transmitter.setdefault('band', [849e6, 849e6])
    find = transmitter.copy()
    del find['carrier'], find['band']
    r = req(**transmitter).items()
    r.sort()
    for setup, shape in r:
        if [setup[k] for k in find.keys()] == find.values() \
                or [setup[k] for k in find.keys()] == [None] * len(find):
            description = describe(setup)
            if description: print
            " %s:" % description
            for (p0, f0), (p1, f1) in zip(shape[::2], shape[1::2]):
                if p0 == p1 < Inf and f0 < f1:
                    print
                    ' ', p0, 'in %g-%g MHz' % (f0 / 1e6, f1 / 1e6)
                elif p0 == p1 == Inf:
                    print
                    "  %g-%g MHz" % (f0 / 1e6, f1 / 1e6)
    print


def trace(*reqPath, **transmitter):
    route = transmitter.pop('route', [])
    req = view(*reqPath, **transmitter)
    if isinstance(req, Combined):
        try:
            print
            " ".join(req.path),
        except:
            print
            req,
        if req.extend: print
        "is an extension of the following requirements:"
        else:          print
        "is a combination of the following requirements:"
        print
        for i, subReq in enumerate(req.stabilities):
            subRoute = route + [str(i + 1)]
            print
            "-".join(subRoute)
            options = transmitter.copy()
            options['route'] = subRoute
            path = list(reqPath) + [str(i)]
            if not hasattr(subReq, 'path'):
                subReq.path = path
                req.__dict__[str(i)] = subReq
            subReq.__dict__.setdefault('path', list(reqPath) + [str(i)])
            trace(*subReq.path, **options)


if __name__ == '__main__':
    view()
    trace('stability', bw=10e3)
