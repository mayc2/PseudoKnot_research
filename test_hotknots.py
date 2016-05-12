import sys
import os

def HotKnots(seq, dot_bracket):
    cmd = '$HOTKNOTS/bin/computeEnergy -s ' + seq + " \"" + dot_bracket + '\" > $RESEARCH/output_files/hotknots_out.txt'
    print(cmd)
    print("Changing to hotknots directory")
    os.chdir("/home/chris/devtree/pseudoKnot_research/HotKnots_v2.0/bin")
    print("Executing HotKnots")
    os.system(cmd)
    print("returning to research directory")
    os.chdir("/home/chris/devtree/pseudoKnot_research")
    energy_file = "output_files/hotknots_out.txt"
    file = open(energy_file,'r',encoding='iso-8859-15')
    data = file.read().splitlines()
    line = data[len(data)-1]
    line = line.split()
    free_energy = float(line[1])
    free_energy_wo_dangling = float(line[2])			
    return  free_energy

def dot_to_bracket(pairs,L):
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
            if pairs[i] > s0_min and pairs[i+1] < s0_max:
                stack0.append((pairs[i],pairs[i+1]))
                s0_min = pairs[i]
                s0_max = pairs[i+1]
            else:
                stack1.append((pairs[i],pairs[i+1]))
                s1_max = pairs[i+1]
                s1_min = pairs[i]
                current = 1
        if current == 1:
            if pairs[i] > s1_min and pairs[i+1] < s1_max:
                stack1.append((pairs[i],pairs[i+1]))
                s1_min = pairs[i]
                s1_max = pairs[i+1]
            else:
                stack0.append((pairs[i],pairs[i+1]))
                s0_max = pairs[i+1]
                s0_min = pairs[i]
                current = 0
    for pair in stack0:
        struct[pair[0]] = "("
        struct[pair[1]] = ")"
    for pair in stack1:
        struct[pair[0]] = "["
        struct[pair[1]] = "]"
    return ''.join(struct)
        
def main():
    seq="CGGUCAUAAGAGAUAAGCUAGCGUCCUAAUCUAUCCCGGGUUAUGGCGCGAAACUCAGGGA" 
    bracket_bracket="(((((((((........................[[[[[[[))))))).))...]]].]]]]"
    dot_bracket="(((((((((........................((((((()))))))).))...))).))))"
    pairs=[4, 447, 5, 450, 7, 444, 8, 446, 10, 442, 13, 438, 17, 433, 24, 391, 29, 182, 31, 180, 36, 176, 56, 149, 57, 148, 59, 147, 62, 145, 64, 141, 66, 140, 69, 138, 72, 136, 73, 135, 77, 130, 88, 124, 93, 119, 102, 110, 152, 165, 184, 387, 188, 383, 190, 380, 197, 208, 210, 369, 218, 356, 223, 348, 227, 343, 232, 337, 240, 326, 242, 325, 244, 321, 247, 318, 249, 320, 252, 316, 255, 299, 256, 298, 262, 293, 267, 287, 270, 283, 301, 313, 399, 426, 406, 421, 407, 420]
    d2 = "....((.((.(..(...(......(....(.(....(...................((.(..(.(.(..(..((...(..........(....(........(.......)........)....).....)....)).).))...).)))..(............)..........)...).).(...(.(......(..........).(.......(....(...(....(.......(.(.(..(.(..(..((.....(....(..(............)...).....)....)).(...........)..).).))...))..........).....)....).......)............)..........)..)...)...).......(......((............))....)......)....)...).).))..)"
    answer = dot_to_bracket(pairs,len(d2))
    # print(HotKnots(seq,bracket_bracket),HotKnots(seq,dot_bracket))
    
if __name__=="__main__":
    sys.exit(main())