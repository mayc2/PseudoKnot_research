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
		self.pairs = {}
		self.start = []

	def add_pair(self, arg):
		super(Structure_SFold, self).__init__()
		self.pairs[int(arg[0])-1] = int(arg[1])-1
		self.start.append(int(arg[0])-1)

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

	def check(a,b,temp):
		#accepted i, a[i], check a[i] not in b
		try:
			b.pairs[a.pairs[temp]]
			backtrack(a,b,i,t,"a",0)
		except KeyError:
			pass

	def backtrack(a,b,i,t,a_b,n):
		""" 
		a,b are the two structures
		index is the current index
		t is the number of ties
		a_b is designating which has conflict
		n is first or second of pair
		"""
		print(a_b,str(n),str(i))

		t += 1
		#select a pair
		if random.randint(0,1) == 1:
			if a_b == "a":
				if n == 0:
					print(len(b.pairs))
					b.pairs.pop(a.start[i])
					check(a,b,i)
					print(len(b.pairs))
				else:
					print(len(b.pairs))
					b.pairs.pop(a.pairs[a.start[i]])
					check(a,b,i)
					print(len(b.pairs))
			else:
				if n == 0:
					print(len(a.pairs))
					a.pairs.pop(b.start[i])
					check(b,a,i)
					print(len(a.pairs))
				else:
					print(len(a.pairs))
					a.pairs.pop(b.pairs[b.start[i]])
					check(b,a,i)
					print(len(a.pairs))
		#select b pair
		else:
			if a_b == "a":
				print(len(a.pairs))
				temp = a.start[i]
				a.pairs.pop(a.start[i])
				check(b,a,temp)
				print(len(a.pairs))
			else:
				print(len(b.pairs))
				temp = b.start[i]
				b.pairs.pop(b.start[i])
				check(a,b,temp)
				print(len(b.pairs))

	def get_pairs(a,b,L):
		answer = []
		for i in range(L):
			try:
				a_i = a.pairs[i]
				answer.append(i)
				answer.append(a_i)
			except KeyError:
				pass
			try:
				b_i = b.pairs[i]
				answer.append(i)
				answer.append(b_i)
			except KeyError:
				pass
		return answer

	#combines structure a and structure b
	def combine(a,b):
		answer = []
		t = 0
		L_max = len(a.start)
		L_min = len(b.start)
		i = 0; j = 0
		temp1 = a
		temp2 = b
		while(i < L_max):
			if (j >= L_min):
				try:
					temp2.pairs[temp1.start[i]]
					backtrack(temp1,temp2,i,t,"a",0)
				except KeyError:
					pass
				try:
					temp2.pairs[temp1.pairs[temp1.start[i]]]
					backtrack(temp1,temp2,i,t,"a",1)
				except KeyError:
					pass
				i += 2

			elif (temp1.start[i] > temp2.start[j]):
				try:
					temp1.pairs[temp2.start[j]]
					backtrack(temp1,temp2,j,t,"b",0)
				except KeyError:
					pass
				try:
					temp1.pairs[temp2.pairs[temp2.start[j]]]
					backtrack(temp1,temp2,j,t,"b",1)
				except KeyError:
					pass
				j += 2

			elif (temp1.start[i] < temp2.start[j]):
				try:
					temp2.pairs[temp1.start[i]]
					backtrack(temp1,temp2,i,t,"a",0)
				except KeyError:
					pass
				try:
					temp2.pairs[temp1.pairs[temp1.start[i]]]
					backtrack(temp1,temp2,i,t,"a",1)
				except KeyError:
					pass
				i += 2
			
			elif (temp1.start[i] == temp2.start[j]):
				if random.randint(0,1) == 0:
					try:
						temp2.pairs[temp1.start[i]]
						backtrack(temp1,temp2,i,t,"a",0)
					except KeyError:
						pass
					try:
						temp2.pairs[temp1.pairs[temp1.start[i]]]
						backtrack(temp1,temp2,i,t,"a",1)
					except KeyError:
						pass
				else:
					try:
						temp1.pairs[temp2.start[j]]
						backtrack(temp1,temp2,j,t,"b",0)
					except KeyError:
						pass
					try:
						temp1.pairs[temp2.pairs[temp2.start[j]]]
						backtrack(temp1,temp2,j,t,"b",1)
					except KeyError:
						pass
				i += 2
				j += 2
				t += 1
		answer = get_pairs(temp1,temp2,len(seq))
		print (answer)
		print (t)
		x = set()
		for item in answer:
			if item in x:
				print(str(item), "in set")
			else:
				x.add(item)
		return answer, t

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
		if (data[len(data)-2] == "% Energy (kcal/mol):"):
			return float(data[len(data)-1])
		else:
			# print("ERROR: unable to get energy, check nupack_out.txt")
			return float(10**8)
	def generate_inFile(dot_bracket):
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
		sys.exit(1)
	
	# ###TESTS
	# print (len(a.pairs), a)
	# print("")
	# print(len(b.pairs), b)

	#Step 2: Combine a,b into a new structure, current
	#t_current = # of coin flips to break ties
	if len(a.start) >= len(b.start):
		current, t_current = combine(a,b)
	else:
		current, t_current = combine(b,a)

	energy_current = get_structure_energy(current)

	print("gets here")
	#Step 3: Set sample-list = {}
	sample_list = {}

	#Step 4: Define Burn-in rate to repeat steps 5-7
	#follow with fixed sampling period
	burnin = 1000
	count = 0 
	exists = 0
	
	"""

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

	"""

	return current, StructureFromPairs(current,len(seq))

def main():
	input_file, seq_file = get_file()
	seq = parse_seq_file(seq_file)
	parsed_sfold = parse_sfold_file(input_file)
	current, dot_bracket = MetropolisHastings(parsed_sfold, seq)
	print(current)
	print(dot_bracket)
	return 0

if __name__ == "__main__":
	sys.exit(main())