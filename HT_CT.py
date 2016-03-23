import math
import sys
import random
import os

"""
Program to read and parse a CT formatted file into a useable data structure.
CT files:
number of base pairs, title of structure
each base has its own line with info:
  -base number: index n
  -Base (A, C, G, T, U, X)
  -Index n-1
  -Index n+1
  -Number of the base to which n is paired. No pairing is indicated by 0.
  -Natural numbering. RNAstructure ignores the actual value given in natural numbering, so it is easiest to repeat n here.

"""

class Base_CT(object):
	"""docstring for Base"""
	def __init__(self, arg):
		super(Base_CT, self).__init__()
		self.base_number = int(arg[0])-1
		self.nucleotide = arg[1]
		self.sub_n = int(arg[2])
		self.post_n = int(arg[3])
		self.pair_index = max(0,int(arg[4])-1)
		self.nat_numbering = int(arg[5])
	def __str__(self):
		super(Base_CT, self).__init__()
		return (self.base_number + ": " + self.nucleotide +" pairs with " + self.pair_index + ", previous: " + self.sub_n + ", next: " + self.post_n +".")

class Structure_CT(object):

	"""docstring for Structure"""
	def __init__(self, arg):
		super(Structure_CT, self).__init__()
		self.name = arg[1]
		self.num_bases = int(arg[0])
		self.bases = []

	def add_base(self, arg):
		super(Structure_CT, self).__init__()
		temp = Base_CT(arg)
		self.bases.append(temp)

	def __str__(self):
		return(self.name + " of length " + self.num_bases)

def get_file():
	if len(sys.argv) != 3:
		print("Incorrect number of arguments.")
		return 1
	input_file = sys.argv[1]
	seq_file = sys.argv[2]
	return input_file, seq_file

def parse_ct_file(input_file):
	file = open(input_file)
	data = file.read().splitlines()
	structures = []
	for i in range(len(data)):
		data[i]= data[i].split()
	
	i = 0
	current = -1
	while i < len(data):
		if len(data[i]) != 6:
			current += 1
			temp = Structure_CT(data[i])
			structures.append(temp)
		else:
			structures[current].add_base(data[i])
		i += 1

	# for sample in structures:
		# print (str(sample))
		# for base in sample.bases:
			# print(base)

	return structures

def DotBracketConversion(pairs, parsed):
	answer = []
	for i in range(len(pairs)):
		# print (len(pairs[i]), parsed[i].num_bases, pairs[i])
		answer.append(StructureFromPairs(pairs[i], parsed[i].num_bases))
	return answer


def get_pairs(structures):
	pairs = []
	for structure in structures:
		temp = []
		check = set()
		for base in structure.bases:
			if base.pair_index != 0:
				if base.pair_index not in check:
					check.add(base.pair_index)
					check.add(base.base_number)
					temp.append(base.base_number)
					temp.append(base.pair_index)
		pairs.append(temp)
	# print (pairs)

	return pairs


def main():
	input_file, seq_file = get_file()
	parsed_ct = parse_ct_file(input_file)
	# pairs = get_pairs(parsed)
	# structs = DotBracketConversion(pairs, parsed)
	return 0

if __name__ == "__main__":
	sys.exit(main())