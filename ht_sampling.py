import math
import sys
import random
import os

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
		self.pairs.append(int(arg[0])-1) 
		self.pairs.append(int(arg[1])-1)

	def __str__(self):
		return (str(self.count) + ": weight - " + str(self.boltzmann_weight) + ", energy - " + str(self.energy) + "\n" + str(self.pairs))

def get_file():
	if len(sys.argv) != 3:
		print("Incorrect number of arguments.")
		sys.exit(1)
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

def MetropolisHastings(structures, seq):
	
	#samples the structure set and recursively calls if duplicate found
	def sample(a, structures):
		b = random.choice(structures)
		if a == b:
			sample(a,structures)
		return b 

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

	#handles ties between pairs in the structures
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

	def helper(a,b,t):
		for key in a.keys():
			if key in b:
				t = handle_ties(key,a,b,t)
				t = helper(a,b,t)
				break
		return t

	#recursively check if there are duplicates in the dictionaries of pairs
	def remove_duplicates(a,b):
		t = 0
		for key in a.keys():
			if key in b:
				t = handle_ties(key,a,b,t)
				t = helper(a,b,t)
				break
		return t

	def trim(a):
		for key in a.keys():
			if a[key] in a.keys():
				if key < a[key]:
					a.pop(a[key])
				else:
					a.pop(key)
				trim(a)
				break

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

	def check(answer):
		zest = set()
		for item in answer:
			if item in zest:
				print(str(item) + "is already in the set.")
			else:
				zest.add(item)

	#combines structure a and structure b
	def combine(a,b):
		t = remove_duplicates(a,b)
		answer = backtrack(a,b)
		check(answer)
		return answer, t

	def HotKnots(pairs):
		dot_bracket = " \"" + BracketedStructureFromPairs(pairs, len(seq)) +"\""
		cmd = '$HOTKNOTS/bin/computeEnergy -s ' + seq + dot_bracket + ' > $RESEARCH/output_files/hotknots_out.txt'
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
		
	#call nupack and return the resulting energy
	def get_structure_energy(pairs):
		cmd = '$NUPACKHOME/bin/energy -pseudo $RESEARCH/input_files/nupack_in > $RESEARCH/output_files/nupack_out.txt'
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

	def generate_inFile(dot_bracket):
		nupack_file = "input_files/nupack_in.in"
		file = open(nupack_file, "w")
		file.write(seq+"\n")
		file.write(dot_bracket)
		file.close()
		return 0
		
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

	def StructureFromPairs(pairs, L):
	    struct = list('.' * L)
	    for i in range(0, len(pairs), 2):
	        struct[pairs[i]] = '('
	        struct[pairs[i+1]] = ')'
	        
	    return ''.join(struct)
	
	def adjust_counts(current,counts):
		for (i,j) in enumerate(current):
			if (i,j) in counts:
				counts[(i,j)] += 1.0
			else:
				counts[(i,j)] = 1.0

	#Step 1: finding a,b sample for current
	try:
		a = sample(None, structures)
		b = sample(a,structures)
	except IndexError:
		print("ERROR: Structures list is empty.")
		sys.exit(1)

	# ###TESTS
	# print (len(a.pairs), a)
	# print("")
	# print(len(b.pairs), b)

	#Step 2: Combine a,b into a new structure, current
	#t_current = # of coin flips to break ties
	pairsA, pairsB = generate_dictionaries(a,b)
	current, t_current = combine(pairsA,pairsB)
	# print(current)
	# print(t_current)

	# energy_current = get_structure_energy(current)
	energy_current = HotKnots(current)

	#Step 3: Set sample-list = {}
	sample_list = {}

	#Step 4: Define Burn-in rate to repeat steps 5-7
	#follow with fixed sampling period
	burnin = 1000
	count = 0 
	exists = 0
	
	counts = {}	
	acceptance_rate = 0.0
	
	print("beginning Burn in iterations")
	while (count < burnin):
		# if(count % (burnin/20)) == 0:
		# 	print("working on iteration " + str(count))
			
		#Step 5: Sample/combine new pair (i, j) into a new structure, proposal
		#t_proposal = # of coins flips required to break ties
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
		#Step 6: Calculate Transition Functions
		#T(proposal|current) = T(proposal) = P(S_i)*P(S_j)*(0.5)**t_proposal
		#T(current|proposal) = T(current) = P(S_a)*P(S_b)*(0.5)**t_current

		Transition_proposal = i.boltzmann_weight * j.boltzmann_weight * 0.5**t_proposal
		Transition_current = a.boltzmann_weight * b.boltzmann_weight * 0.5**t_current
		# print(Transition_proposal,Transition_current)
		# print(Transition_proposal, Transition_current)
		#Step 7: Compute Probability/Select whether to accept 
		R = float(13807E-23) * float(6.022E23)
		# T = 37.0
		T = float(275.928)

		# print("energies:" , str(energy_proposal),str(energy_current))
		temp = (Transition_current * math.exp((-1.0 * energy_proposal)/(R * T))) 
		temp2 =  (Transition_proposal * math.exp((-1.0* energy_current)/(R * T)))
		ans = temp/ temp2
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

	print(centroid)

	# return current
	return current, BracketedStructureFromPairs(current,len(seq))

def main():
	input_file, seq_file = get_file()
	seq = parse_seq_file(seq_file)
	parsed_sfold = parse_sfold_file(input_file)
	current, dot_bracket = MetropolisHastings(parsed_sfold, seq)
	# current = MetropolisHastings(parsed_sfold, seq)
	print(current)
	print(dot_bracket)
	return 0

if __name__ == "__main__":
	sys.exit(main())