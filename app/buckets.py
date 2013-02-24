"""
List all possible ways of pouring exactly 4 l water using three buckets whose
volumes are 8, 5 and 3 l. The largest bucket starts full and the other empty.

13 solutions
The best solution has 7 steps.
800 350 323 620 602 152 143
800 503 530 233 251 701 710 413
800 503 053 350 323 620 602 152 143
800 503 530 350 323 620 602 152 143
800 350 053 503 530 233 251 701 710 413
800 350 323 503 530 233 251 701 710 413
800 350 323 053 503 530 233 251 701 710 413
800 503 530 233 053 350 323 620 602 152 143
800 503 530 233 251 350 323 620 602 152 143
800 350 323 620 602 503 530 233 251 701 710 413
800 503 530 233 251 053 350 323 620 602 152 143
800 503 530 233 251 701 710 350 323 620 602 152 143
800 350 323 620 602 152 053 503 530 233 251 701 710 413
"""
S = (8,5,3)
I = ((1,2),(0,2),(0,1))
v = [8,0,0]
def f(V, v):
    V = V[:] + [v]
    if 4 in v:
        yield V
    else:
        for i, vi in enumerate(v):
            if vi:
                for j in range(3):
                    if i != j:
                        if v[j] < S[j]:
                            dv = min(vi, S[j] - v[j])
                            vj = list(v)
                            vj[i] -= dv
                            vj[j] += dv
                            vj = tuple(vj)
                            if not vj in V:
                                for Vj in f(V, vj):
                                    yield Vj
M = [(len(V), V) for V in f([], (8,0,0))]
M.sort()
print len(M), "solutions"
print "The best solution has", M[0][0], "steps."
for n, V in M:
    for v in V:
        print "".join([str(vi) for vi in v]),
    print
