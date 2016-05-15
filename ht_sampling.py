# ==============================================================================
# ==============================================================================
# ht_sampling.py ===============================================================
#
# Parses output from SFold. Then carries out the Metropolis-Hastings algorithm
#	on the structures SFold calculated. Prepares output in the form of a
#	dot-bracket sequence denoting the structures and pseudoknots.
# ==============================================================================
# ==============================================================================

import math
import sys
import random
import os

# ==============================================================================
# Structure_SFold class ========================================================
# ==============================================================================
#
# A simple object that stores data for each structure parsed from the SFold
#	output.
#
# Member variables:
#	self.count - Structure number, a unique id
#	self.boltzmann_weight - Boltzman weight calculated for the structure
#	self.energy - Engergy calculated for the structure
#	self.pairs - List of the indecies of the base pairs that form the structure.
#		The list is not separated for each pair, however the matching bases
#		appear next to eachother ie. if the base pairs are (1, 4) and (2, 4)
#		then the list is [1, 4, 2, 3].
class Structure_SFold(object):

	# ==========================================================================
	# __init__ Constructor returns object ======================================
	#
	# Constructs the objects from the parsed data that is recieved as a list of
	#	arguments.
	def __init__(self, arg):
		super(Structure_SFold, self).__init__()
		self.count = int(arg[1])
		self.boltzmann_weight = float(arg[3])
		self.energy = float(arg[2])
		self.pairs = []

	# ==========================================================================
	# add_pair returns void ====================================================
	#
	# Adds a pair from the parsed SFold output to the structure. Used to build
	#	the structure after the initial data is parsed.
	def add_pair(self, arg):
		super(Structure_SFold, self).__init__()
		x = int(arg[0])-1
		y = int(arg[1])-1
		for n in range(int(arg[2])):
			self.pairs.append(x)
			self.pairs.append(y)
			x += 1
			y -= 1

	# ==========================================================================
	# __str__ convert to string returns String =================================
	#
	# Converts the class into a string for printing.
	def __str__(self):
		return (str(self.count) + ": weight - " + str(self.boltzmann_weight) + \
			", energy - " + str(self.energy) + "\n" + str(self.pairs))

# ==============================================================================
# Functions for preparing data for Metropolis-Hastings =========================
# ==============================================================================

# ==============================================================================
# get_file returns (File, File)
#
# Gets the tuple of files to be read as input. Throws an error if there aren't 3
#	arguments (ht_sampling.py, <SFold_output>, <Sequence_file>) to the program.
def get_file():
	if len(sys.argv) != 3:
		print("Incorrect number of arguments.")
		sys.exit(1)
	input_file = sys.argv[1]
	seq_file = sys.argv[2]
	return input_file, seq_file

# ==============================================================================
# parse_seq_file returns String
#
# Parses the contents of the sequence file and returns the contents of the
#	sequence as a string.
def parse_seq_file(seq_file):
	file = open(seq_file)
	data = file.read().splitlines()
	seq = ""
	for i in range(len(data)):
		if i > 0 and data[i] != "":
			seq += data[i]
	return seq

# ==============================================================================
# parse_sfold_file return List of Structure_SFold
#
# Parses the SFold output file, placing the data into a series of
#	Structure_SFold objects.
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
			structures[current].add_pair(data[i])
		i += 1
	return structures

# ==============================================================================
# MetropolisHastings(list(Structure_SFold), String)
#
# Contains code for carrying out the Metropolis-Hastings algorithm and
#	functions specific to it.
def MetropolisHastings(structures, seq):

	# ==========================================================================
	# Functions specific to MetropolisHastings =================================
	# ==========================================================================

	# ==========================================================================
	# sample(Structure_SFold, list(Structure_SFold)) returns Structure_SFold
	#
	# Randomly picks a structure in the list given for sampling. If the
	#	structure chosen is the same as the argument a, it randomly chooses
	#	again, doing so until it has two different structures.
	def sample(a, structures):
		b = random.choice(structures)
		if a == b:
			b = sample(a,structures)
		return b

	# ==========================================================================
	# generate_dictionaries(Structure_SFold, Structure_SFold)
	#	returns (dict{int:int}, dict{int:int})
	#
	# Creates a dictionary for both of the structures that maps each base index
	#	to the index of its matching base ie. if the parirs for a structure are
	#	[1, 4, 2, 3] then the dictionary created is {1:4, 2:3, 3:2, 4:1}. This
	#	is done for both structures passed as arguments.
	def generate_dictionaries(a,b):
		pairsA = {}
		pairsB = {}
		for i in range(0,len(a.pairs),2):
			pairsA[a.pairs[i]] = a.pairs[i+1]
			pairsA[a.pairs[i+1]] = a.pairs[i]
		for i in range(0,len(b.pairs),2):
			pairsB[b.pairs[i]] = b.pairs[i+1]
			pairsB[b.pairs[i+1]] = b.pairs[i]
		return pairsA, pairsB

	# ==========================================================================
	# handle_ties(int, dict{int:int}, dict{int:int}, int) returns int
	#
	# Handles ties between pairs in the structures. This function is used
	#	multiple times when combining structures. Flips a coin that determines
	#	which dictionary (structure) to remove a pair of indecies from and
	#	removes it. Increments t, the number of flips (a random choice of 0 or
	#	1) needed to break ties and returns it.
	def handle_ties(key,a,b,t):
		t += 1
		if random.randint(0,1) == 0:
			other = b[key]
			b.pop(key)
			b.pop(other)
			if a[key] in b:
				t = handle_ties(a[key],a,b,t)
		else:
			other = a[key]
			a.pop(key)
			a.pop(other)
			if b[key] in a:
				t = handle_ties(b[key],b,a,t)
		return t

	# ==========================================================================
	# helper(dict{int:int}, dict{int:int}, int) returns int
	#
	# Small helper function for removing duplicates when structures are
	#	combined. Calls handle_ties on each pair of indecies that appears in
	#	both structures, which randomly choses which structure to remove the
	#	pair from. Returns t, the number of coin flips (a random choice of 0 or
	#	1) needed to break ties.
	def helper(a,b,t):
		for key in a.keys():
			if key in b:
				t = handle_ties(key,a,b,t)
				t = helper(a,b,t)
				break
		return t

	# ==========================================================================
	# remove_duplicates(dict{int:int}, dict{int:int}) returns int
	#
	# Checks if there are duplicates in the dictionaries of pairs Returns t, the
	#	number of coin flips (a random choice of 0 or 1) needed to break ties.
	#	Calls handle_ties and helper to perform this task.
	def remove_duplicates(a,b):
		t = 0
		for key in a.keys():
			if key in b:
				t = handle_ties(key,a,b,t)
				t = helper(a,b,t)
				break
		return t

	# ==========================================================================
	# trim(dict{int:int}) returns void
	#
	# Removes the key entries that are also values from the dictionary, so that
	#	a pair only appears once in the set ie. if the dictionary is
	#	{1:2 2:1 3:4 4:3} then it will be changed to {1:2 3:4}.
	def trim(a):
		for key in a.keys():
			if a[key] in a.keys():
				if key < a[key]:
					a.pop(a[key])
				else:
					a.pop(key)
				trim(a)
				break

	# ==========================================================================
	# backtrack(dict{int:int}, dict{int:int}) returns list(int)
	#
	# Combines two dictionaries into a list of the indecies, merging them into
	#	an ordered list of pairs ie if the two dictionaries are {1:3 5:7} and
	#	{2:4, 6:9} then the combined list will be [1, 3, 2, 4, 5, 7, 6, 9]. The
	#	list follows the same format as the list of base pairs in the
	#	Structure_SFold class.
	def backtrack(a,b):
		trim(a)
		trim(b)
		keysA = list(a)
		keysB = list(b)
		keysA.sort()
		keysB.sort()
		answer = []
		i = 0
		j = 0
		while(i < len(keysA) and j <len(keysB)):
			if keysA[i] < keysB[j]:
				answer.append(keysA[i])
				answer.append(a[keysA[i]])
				i += 1
			else:
				answer.append(keysB[j])
				answer.append(b[keysB[j]])
				j += 1
		while i < len(keysA):
			answer.append(keysA[i])
			answer.append(a[keysA[i]])
			i += 1
		while j < len(keysB):
			answer.append(keysB[j])
			answer.append(b[keysB[j]])
			j += 1
		return answer

	# ==========================================================================
	# check(list(int)) returns void
	#
	# Checks that each index in the list is unique, prints a warning if
	#	duplicates are found.
	def check(answer):
		zest = set()
		for item in answer:
			if item in zest:
				print(str(item) + "is already in the set.")
			else:
				zest.add(item)

	# ==========================================================================
	# combine(dict{int:int}, dict{int:int}) returns (list(int), int)
	#
	# Combines structure a and structure b. Removes the duplicate indecies in
	#	each dictionary (structure) and then combines them into a single list
	#	containing all of the pairs of the two structures. Returns a tuple of
	#	the combined list and t, the number of coin flips (a random choice of 0
	#	or 1) needed to break ties.
	def combine(a,b):
		t = remove_duplicates(a,b)
		answer = backtrack(a,b)
		check(answer)
		return answer, t

	# ==========================================================================
	# HotKnots(list(pairs)) returns float
	#
	# Calls HotKnots on the data given by the function. Structure comes in as a
	#	as a list of paired indecies. A dot-bracket representation of the
	#	structure is created and is sent to HotKnots along with the ASCII
	#	representation of the sequence. HotKnots is outputs energy calculations
	#	for the structure. The output of HotKnots is read and the free energy
	#	for the structure is returned.
	def HotKnots(pairs):
		dot_bracket = " \"" + BracketedStructureFromPairs(pairs, len(seq)) +"\""
		cmd = '$HOTKNOTS/bin/computeEnergy -s ' + seq + dot_bracket + \
			' > $RESEARCH/output_files/hotknots_out.txt'
		relative_path = os.getcwd()
		os.chdir(relative_path+"/HotKnots_v2.0/bin")
		os.system(cmd)
		os.chdir(relative_path)
		energy_file = "output_files/hotknots_out.txt"
		file = open(energy_file,'r',encoding='iso-8859-15')
		data = file.read().splitlines()
		line = data[len(data)-1]
		line = line.split()
		if line[1] == "the":
			print("HotKnots failed")
		free_energy = float(line[1])
		free_energy_wo_dangling = float(line[2])
		return  free_energy

	# ==========================================================================
	# get_structure_energy(pairs) returns float
	#
	# Calls Nupack and return the resulting energy calculation. Structure comes
	#	in as a as a list of paired indecies. A dot-bracket representation of
	#	the structure is created and is put in an input file for Nupack. Nupac
	#	is run with the input file and the output from Nupack is read in,
	#	parsed, and the energy calculation is returned.
	def get_structure_energy(pairs):
		cmd = '$NUPACKHOME/bin/energy -pseudo $RESEARCH/input_files/nupack_in \
			> $RESEARCH/output_files/nupack_out.txt'
		dot_bracket = StructureFromPairs(pairs,len(seq))
		if generate_inFile(dot_bracket) != 0:
			print("ERROR: Failed to generate nupack_in.in file")
			sys.exit(1)
		os.system(cmd)
		energy_file = "output_files/nupack_out.txt"
		file = open(energy_file)
		data = file.read().splitlines()
		if len(data) <= 2:
			print("length of Nupack file is 0")
			sys.exit(1)
		if (data[len(data)-2] == "% Energy (kcal/mol):"):
			# print("Nupack Succeeded")
			return float(data[len(data)-1])
		else:
			# print("Nupack failed, establishing large energy value (10**8)")
			return float(10**8)

	# ==========================================================================
	# generate_inFile(String) returns int
	#
	# Writes the dot-bracket structure to the input file for Nupack. Returns 0
	#	if successful.
	def generate_inFile(dot_bracket):
		nupack_file = "input_files/nupack_in.in"
		file = open(nupack_file, "w")
		file.write(seq+"\n")
		file.write(dot_bracket)
		file.close()
		return 0

	# ==========================================================================
	# should_switch_stacks(list(int), int, (int, int)) returns boolean
	#
	# Returns True if the pair crosses over a pair of indecies in the pairs
	#	list. Indicates if the function constructing the dot-bracket format
	#	should switch stacks.
	def should_switch_stacks(pairs, i, pair):
		return (pairs[i] < pair[0] \
					and (pairs[i+1] > pair[0] and pairs[i+1] < pair[1])) \
			or ((pairs[i] > pair[0] and pairs[i] < pair[1]) \
					and pairs[i+1] > pair[1])

	# ==========================================================================
	# BracketedStructureFromPairs(list(int), int) returns String
	#
	# Creates the dot-bracket formatted representation of the combined
	#	structure. Takes the list of base pairs and their length and adds them
	#	to one of two lists (stack0 and stack1). It places pairs into stack0
	#	until it finds a pair that crosses a pair in stack0 then starts adding
	#	to stack1, switching back to stack0 when a pair crosses again. This
	#	method is followed until all of the pairs are added to the stacks.
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
					if should_switch_stacks(pairs, i, pair):
						stack1.append((pairs[i],pairs[i+1]))
						# print(pairs[i],pairs[i+1])
						current = 1
						break
				if current == 0:
					stack0.append((pairs[i],pairs[i+1]))
			if current == 1:
				for pair in stack1:
					if should_switch_stacks(pairs, i, pair):
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

	# ==========================================================================
	# StructureFromPairs(list(int), int) returns String
	#
	# Makes the dot and parenthesis format for visualizing structures
	def StructureFromPairs(pairs, L):
		struct = list('.' * L)
		for i in range(0, len(pairs), 2):
			struct[pairs[i]] = '('
			struct[pairs[i+1]] = ')'

		return ''.join(struct)

	# ==========================================================================
	# adjust_counts(list(int), dict{int:float}) returns void
	#
	# Keeps track of the appearances of pairs in the structure
	def adjust_counts(current,counts):
		for i in range(0,len(current),2):
			if (current[i],current[i+1]) in counts:
				counts[(current[i],current[i+1])] += 1.0
			else:
				counts[(current[i],current[i+1])] = 1.0

	# ==========================================================================
	# ==========================================================================
	# Main code for the Metropolis-Hastings algorithm
	# ==========================================================================
	# ==========================================================================
	#
	# Carries out the operations outlined in the algorithm description in a
	#	a series of steps.

	# ==========================================================================
	# Step 1 ===================================================================
	#
	# Finding a,b sample for current
	try:
		a = sample(None, structures)
		b = sample(a,structures)
	except IndexError:
		print("ERROR: Structures list is empty.")
		sys.exit(1)

	# ==========================================================================
	# Step 2 ===================================================================
	#
	# Combine a,b into a new structure, current
	# t_current = number of coin flips to break ties
	pairsA, pairsB = generate_dictionaries(a,b)
	current, t_current = combine(pairsA,pairsB)

	# print(len(current))
	# print(t_current)

	# energy_current = get_structure_energy(current)
	energy_current = HotKnots(current)

	# ==========================================================================
	# Step 3 ===================================================================
	#
	# Set sample-list = {}
	sample_list = {}

	# ==========================================================================
	# Step 4 ===================================================================
	#
	# Define Burn-in rate to repeat steps 5-7
	# Follow with fixed sampling period
	burnin = 1000
	count = 0
	exists = 0

	counts = {}
	acceptance_rate = 0.0

	print("beginning Burn in iterations")
	R = float(13807E-23) * float(6.022E23)
	T = float(275.928)
	while (count < burnin):
		# if(count % (burnin/20)) == 0:
			# print("working on iteration " + str(count))
			# print(BracketedStructureFromPairs(current, len(seq)))

		# ======================================================================
		# Step 5 ===============================================================
		#
		# Sample/combine new pair (i, j) into a new structure, proposal
		# t_proposal = # of coins flips required to break ties
		try:
			i = sample(None, structures)
			j = sample(i, structures)
		except IndexError:
			print("ERROR: Structures list is empty.")
			return 1

		pairsI, pairsJ = generate_dictionaries(i,j)
		proposal, t_proposal = combine(pairsI,pairsJ)

		#TO-DO HotKnots Implementation
		energy_proposal = HotKnots(proposal)
		# print("CHECKING FOR DUPLICATES: " + str(len(proposal) == len(set(proposal))) )

		# ======================================================================
		# Step 6: ==============================================================
		#
		# Calculate Transition Functions
		# T(proposal|current) = T(proposal) = P(S_i)*P(S_j)*(0.5)**t_proposal
		# T(current|proposal) = T(current) = P(S_a)*P(S_b)*(0.5)**t_current

		Transition_proposal = i.boltzmann_weight * j.boltzmann_weight * 0.5**t_proposal
		Transition_current = a.boltzmann_weight * b.boltzmann_weight * 0.5**t_current
		# print(Transition_proposal,Transition_current)
		# print(Transition_proposal, Transition_current)

		# ======================================================================
		# Step 7 ===============================================================
		#
		# Compute Probability/Select whether to accept
		# T = 37.0

		# print("energies:" , str(energy_proposal),str(energy_current))
		# print(Transition_current, Transition_proposal, energy_current, energy_proposal)
		ans = (Transition_current * math.exp((-1.0 * energy_proposal)/(R * T))) / (Transition_proposal * math.exp((-1.0* energy_current)/(R * T)))
		# print("temp: " + str(ans))
		Probability = min(1.0, ans )
		# print(Probability)
		if Probability == 1:
			# print("accepting proposal")
			a = i
			b = j
			current = proposal
			t_current = t_proposal
			energy_current = energy_proposal
			acceptance_rate += 1.0
		else:
			x = random.random()
			# print(x, Probability)
			if Probability > x:
				# print("accepting proposal after failing")
				a = i
				b = j
				current = proposal
				t_current = t_proposal
				energy_current = energy_proposal
				acceptance_rate += 1.0
			else:
				# print("doesn't accept proposal")
				pass
		adjust_counts(current,counts)
		count += 1

	centroid = []
	keys = list(counts)
	keys.sort()
	for key in keys:
		if (counts[key]/count) >= 0.5:
			centroid.append(key)
	answer = []
	for pair in centroid:
		answer.append(pair[0])
		answer.append(pair[1])
	print("\nCentroid Structure")
	print("Length:\n",len(centroid))
	print("Pairs:\n",centroid)
	print("Pairs:\n",answer)
	print("Dot Bracket:\n",BracketedStructureFromPairs(answer,len(seq)))

	check(answer)

	# return current
	return current, BracketedStructureFromPairs(current,len(seq))

# ==============================================================================
# End of Metropolis-Hastings
# ==============================================================================

def main():
	input_file, seq_file = get_file()
	seq = parse_seq_file(seq_file)
	parsed_sfold = parse_sfold_file(input_file)
	current, dot_bracket = MetropolisHastings(parsed_sfold, seq)
	print(len(seq))
	# current = MetropolisHastings(parsed_sfold, seq)
	# print("\nCurrent Structure")
	# print("Length:\n",len(current)/2)
	# print("Pairs:\n",current)
	# print("Dot Bracket:\n",dot_bracket)
	return 0

if __name__ == "__main__":
	sys.exit(main())

# ==============================================================================
# ==============================================================================
# ==============================================================================
