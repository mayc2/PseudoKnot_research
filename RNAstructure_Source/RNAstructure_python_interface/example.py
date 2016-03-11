#fold an RNA
import RNAstructure
import sys

if len(sys.argv) != 3:
	print "Usage: python example.py <input> <output>"
	print "Input should be in .seq format"
	sys.exit()

#argument 1 should be an input sequence in .seq format
#argument 2 should be the output ct file
infile,outfile = sys.argv[1:]

#one annoying thing about the RNAstructure constructors is
#that they take a lot of boolean or integer arguments
#One thing I intend to do is add sane default arguments. 
#suggestions are welcome

#also, SWIG doesn't work nicely with overloaded functions
#in a lot of cases

#constructors must be called with all arguments
#and can't use keyword arguments

strand = RNAstructure.RNA(infile,2,True)
print strand.GetErrorCode()

#constructor fails silently if the input is invalid!!
#have to check error codes -- not very pythonic
#error = strand.GetErrorCode()
#if error!=0:
#	print strand.GetErrorMessage(error),
#	sys.exit()

#likewise for most of the methods. Something like 
#FoldSingleStrand should have the same defaults
#as the Fold program
strand.FoldSingleStrand(percent=0.0,
						maximumstructures=1,
						window=0,
						mfeonly=True)
print strand.GetErrorCode()
	
#ideally calling python methods should look more like this
strand.WriteCt(outfile)
print strand.GetErrorCode()