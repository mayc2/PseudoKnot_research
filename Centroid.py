import sys

def parse_file():
    c1_filename = "output/clusters/c01.ccentroid.bp"
    c2_filename = "output/clusters/c02.ccentroid.bp"
    
    file1 = open(c1_filename,"r")
    file2 = open(c2_filename, "r")
    
    data1 = file1.read().splitlines()
    data2 = file2.read().splitlines()
    
    c1 = []
    c2 = []
    for i in range(len(data1)):
        if i != 0:
            temp = data1[i].split()
            c1.append(int(temp[0]))
            c1.append(int(temp[1]))
    for i in range(len(data2)):
        if i != 0:
            temp = data2[i].split()
            c2.append(int(temp[0]))
            c2.append(int(temp[1]))
    
    
    return c1,c2

def main():
    c1, c2 = parse_file()
    print(c1)
    print(c2)
    return 0

if __name__ == "__main__":
    sys.exit(main())