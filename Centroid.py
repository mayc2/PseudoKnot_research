import sys

def parse_file():
    filename = sys.argv[1]
    file = open(filename,"r")
    data = file.read().splitlines()
    
    seq_file = sys.argv[2]
    file2 = open(seq_file,"r")
    data2 = file2.read().splitlines() 
    
    i = 1
    sequence=""
    while i < len(data2):
         sequence += data2[i]
         i += 1
    c1 = []

    for i in range(len(data)):
        if i != 0:
            temp = data[i].split()
            c1.append(int(temp[0])+1)
            c1.append(int(temp[1])+1)
    
    return c1, len(sequence), sequence

def main():
    c1, L, sequence = parse_file()
    print(L)
    print(sequence)
    print(c1)
    return 0

if __name__ == "__main__":
    sys.exit(main())