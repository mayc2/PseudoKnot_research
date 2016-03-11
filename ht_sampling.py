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

class Structure_SFold(object):
	"""docstring for Structure_SFold"""
	def __init__(self, arg):
		super(Structure_SFold, self).__init__()
		self.count = int(arg[1])
		self.boltzmann_weight = float(arg[2])
		self.energy = float(arg[3])
		self.pairs = []

	def add_pair(self, arg):
		super(Structure_SFold, self).__init__()
		self.pairs.append(arg)

	def __str__(self):
		return (str(self.count) + ": weight - " + str(self.boltzmann_weight) + ", energy - " + str(self.energy) + "\n" + str(self.pairs))

def get_file():
	if len(sys.argv) != 3:
		print("Incorrect number of arguments.")
		return 1
	input_file = sys.argv[1]
	seq_file = sys.argv[2]
	return input_file, seq_file

def parse_seq_file(seq_file):
	file = open(seq_file)
	data = file.read().splitlines()
	seq = ""
	for i in range(len(data)):
		if i > 0 and data[i] != "":
			seq += data[i]
	return seq

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

def parse_sfold_file(input_file):
	file = open(input_file)
	data = file.read().splitlines()

	for i in range(len(data)):
		data[i] = data[i].split()

	#a list containing the structures
	structures = []
	
	i = 3
	current = -1
	while i < len(data):
		#Create new structure
		if data[i][0] == "Structure":
			current += 1
			temp = Structure_SFold(data[i])
			structures.append(temp)
		else:
			structures[current].add_pair(int(data[i][0])-1)
			structures[current].add_pair(int(data[i][1])-1)
		i += 1
	return structures

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

def DotBracketConversion(pairs, parsed):
	answer = []
	for i in range(len(pairs)):
		# print (len(pairs[i]), parsed[i].num_bases, pairs[i])
		answer.append(StructureFromPairs(pairs[i], parsed[i].num_bases))
	return answer

def MetropolisHastings(structures, seq):
	
	#samples the structure set and recursively calls if duplicate found
	def sample(a, structures):
		b = random.choice(structures)
		if a == b:
			sample(a,structures)
		return b 
	def backtrack(answer,value,pair,t,check):
		t += 1
		index = int()
		for i in range(len(answer)):
			if answer[i] == value:
				index = i
				break
		if pair in check:
			return
		if random.randint(0,1) == 1:
			if (index % 2 == 1):
				check.remove(answer[index-1])
				check.add(pair)
				answer.remove(value)
				answer.remove(answer[index-1])
				if value < pair:
					answer.append(value)
					answer.append(pair)
				else:
					answer.append(pair)
					answer.append(value)
	#combines structure a and structure b
	def combine(a,b):
		answer = []
		t = 0
		L_max = len(a.pairs)
		L_min = len(b.pairs)
		i =0; j = 0
		check = set()
		while(i < L_max):
			if (j >= L_min):
				if a.pairs[i] in check:
					backtrack(answer,a.pairs[i],a.pairs[i+1],t,check)
				elif a.pairs[i+1] in check:
					backtrack(answer,a.pairs[i+1],a.pairs[i],t,check)
				else:
					answer.append(a.pairs[i])
					answer.append(a.pairs[i+1])
					check.add(a.pairs[i])
					check.add(a.pairs[i+1])
				i += 2

			elif (a.pairs[i] > b.pairs[j]):
				if b.pairs[j] in check:
					backtrack(answer,b.pairs[j],b.pairs[j+1],t,check)
				elif b.pairs[j+1] in check:
					backtrack(answer,b.pairs[j+1],b.pairs[j],t,check)
				else:
					answer.append(b.pairs[j])
					answer.append(b.pairs[j+1])
					check.add(b.pairs[j])
					check.add(b.pairs[j+1])
				j += 2

			elif (a.pairs[i] < b.pairs[j]):
				if a.pairs[i] in check:
					backtrack(answer,a.pairs[i],a.pairs[i+1],t,check)
				elif a.pairs[i+1] in check:
					backtrack(answer,a.pairs[i+1],a.pairs[i],t,check)
				else:
					answer.append(a.pairs[i])
					answer.append(a.pairs[i+1])
					check.add(a.pairs[i])
					check.add(a.pairs[i+1])
				i += 2
			
			elif (a.pairs[i] == b.pairs[j]):
				if random.randint(0,1) == 0:
					if a.pairs[i] in check:
						backtrack(answer,a.pairs[i],a.pairs[i+1],t,check)
					elif a.pairs[i+1] in check:
						backtrack(answer,a.pairs[i+1],a.pairs[i],t,check)
					else:
						answer.append(a.pairs[i])
						answer.append(a.pairs[i+1])
						check.add(a.pairs[i])
						check.add(a.pairs[i+1])
				else:
					if b.pairs[j] in check:
						backtrack(answer,b.pairs[j],b.pairs[j+1],t,check)
					elif b.pairs[j+1] in check:
						backtrack(answer,b.pairs[j+1],b.pairs[j],t,check)
					else:
						answer.append(b.pairs[j])
						answer.append(b.pairs[j+1])
						check.add(b.pairs[j])
						check.add(b.pairs[j+1])
				i += 2
				j += 2
				t += 1

		return answer, t
	#call nupack and return the resulting energy
	def get_structure_energy(seq, pairs):
		cmd = '$NUPACKHOME/bin/energy -pseudo $RESEARCH/input_files/nupack_in > $RESEARCH/output_files/nupack_out.txt'
		dot_bracket = StructureFromPairs(pairs,len(seq))
		if generate_inFile(seq,dot_bracket) != 0:
			print("ERROR: Failed to generate nupack_in.in file")
			return 1
		os.system(cmd)
		energy_file = "output_files/nupack_out.txt"
		file = open(energy_file)
		data = file.read().splitlines()
		if (data[len(data)-2] == "% Energy (kcal/mol):"):
			return float(data[len(data)-1])
		else:
			# print("ERROR: unable to get energy, check nupack_out.txt")
			return 
	def generate_inFile(seq,dot_bracket):
		nupack_file = "input_files/nupack_in.in"
		file = open(nupack_file, "w")
		file.write(seq+"\n")
		file.write(dot_bracket)
		file.close()
		return 0
	def StructureFromPairs(pairs, L):
	    struct = list('.' * L)
	    for i in range(0, len(pairs), 2):
	        # print(i,i+1,pairs[i],pairs[i+1],L)
	        struct[pairs[i]] = '('
	        struct[pairs[i+1]] = ')'
	        
	    return ''.join(struct)

	#Step 1: finding a,b sample for current
	try:
		a = sample(None, structures)
		b = sample(a,structures)
	except IndexError:
		print("ERROR: Structures list is empty.")
		return 1
	# print (len(a), a)
	# print(len(b), b)

	#Step 2: Combine a,b into a new structure, current
	#t_current = # of coin flips to break ties
	if len(a.pairs) > len(b.pairs):
		current, t_current = combine(a,b)
	else:
		current, t_current = combine(b,a)
	# print (current, t_current)
	print(current)
	energy_current = get_structure_energy(seq, current)

	#Step 3: Set sample-list = {}
	sample_list = {}

	#Step 4: Define Burn-in rate to repeat steps 5-7
	#follow with fixed sampling period
	burnin = 1000
	count = 0 
	exists = 0
	while (count < burnin):
		#Step 5: Sample/combine new pair (i, j) into a new structure, proposal
		#t_proposal = # of coins flips required to break ties
		try:
			i = sample(None, structures)
			j = sample(i, structures)
		except IndexError:
			print("ERROR: Structures list is empty.")
			return 1
		
		if len(a.pairs) > len(b.pairs):
			proposal, t_proposal = combine(i,j)
		else:
			proposal, t_proposal = combine(i,j)

		energy_proposal = get_structure_energy(seq, proposal)
		# print("CHECKING FOR DUPLICATES: " + str(len(proposal) == len(set(proposal))) )
		#Step 6: Calculate Transition Functions
		#T(proposal|current) = T(proposal) = P(S_i)*P(S_j)*(0.5)**t_proposal
		#T(current|proposal) = T(current) = P(S_a)*P(S_b)*(0.5)**t_current

		Transition_proposal = i.boltzmann_weight * j.boltzmann_weight * 0.5**t_proposal
		Transition_current = a.boltzmann_weight * b.boltzmann_weight * 0.5**t_current

		# print(Transition_proposal, Transition_current)
		#Step 7: Compute Probability/Select whether to accept 
		if energy_proposal != None:
			R = float(13807E-23) * float(6022E23)
			T = 37.0
			Probability = min(1.0, (Transition_current * (-1.0 * energy_proposal)/(R * T))/ (Transition_proposal * (-1.0* energy_current)/(R * T)))
			if Probability == 1:
				print("accepting proposal")
				a = i
				b = j
				current = proposal
				t_current = t_proposal


		count += 1
	return current, StructureFromPairs(current,len(seq))

def main():
	input_file, seq_file = get_file()
	seq = parse_seq_file(seq_file)
	# parsed_ct = parse_ct_file(input_file)
	parsed_sfold = parse_sfold_file(input_file)
	# print(parsed_sfold[0])
	# print(len(parsed_sfold))
	# pairs = get_pairs(parsed)
	# structs = DotBracketConversion(pairs, parsed)
	# print (len(structs[0]), structs[0])
	# print(parsed_sfold[0].pairs)
	current, dot_bracket = MetropolisHastings(parsed_sfold, seq)
	print(current)
	print(dot_bracket)
	# db_current = StructureFromPairs(current,451)
	# db_proposal = StructureFromPairs(proposal,451)
	# print(db_current)
	# print(db_proposal)
	return 0

if __name__ == "__main__":
	sys.exit(main())