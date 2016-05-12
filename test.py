import sys

def BracketedStructureFromPairs(pairs,L):
    struct = list('.'*L)
    stack0 = []
    stack1 = []
    s0_min = 0
    s1_min = 0
    s0_max = L
    s1_max = L
    current = 0
    for i in range(0,len(pairs), 2):
        if current == 0:
            for pair in stack0:
                print(i,pair,current)
                if (pairs[i] < pair[0] and (pairs[i+1] > pair[0] and pairs[i+1] < pair[1])) \
                    or ((pairs[i] > pair[0] and pairs[i] < pair[1]) and pairs[i+1] > pair[1]):
                    stack1.append((pairs[i],pairs[i+1]))
                    current = 1
                    print(current,pair)
                    break
            if current == 0:
                stack0.append((pairs[i],pairs[i+1]))
        if current == 1:
            for pair in stack1:
                if (pairs[i] < pair[0] and (pairs[i+1] > pair[0] and pairs[i+1] < pair[1])) \
                    or ((pairs[i] > pair[0] and pairs[i] < pair[1]) and pairs[i+1] > pair[1]):
                    stack0.append((pairs[i],pairs[i+1]))
                    current = 0
                    break
            if current == 1:
                stack1.append((pairs[i],pairs[i+1]))
    for pair in stack0:
        struct[pair[0]] = "("
        struct[pair[1]] = ")"
    for pair in stack1:
        struct[pair[0]] = "["
        struct[pair[1]] = "]"
    return ''.join(struct)
    
def main():
    x = [ 4, 16, 5, 15, 6, 14, 9, 19, 10, 18 ]
    y = [7, 48, 15, 20, 22, 42]
    struct = BracketedStructureFromPairs(x, 20)
    print(struct)
    struct1 = BracketedStructureFromPairs(y,49)
    print(struct1)
    return 0

if __name__=="__main__":
    sys.exit(main())