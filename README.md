# PseudoKnot_research

Goal:
Prototype script to predict pseudoknots in rna structures.

How to run:
You can execute the program through the use of a few scripts.

(1) run_ht.sh -- running MH on a single sequence file
    Usage: run_ht.sh [-r] sequencefile.fa[sta]
                -r : tells script to rerun SFold, if this is not designated
                     MH will grab the SFold output from the last time SFold
                     was run
                     
(2) sampling.sh -- running a set of sequence files at once
    Usage: sampling.sh 
            grabs all fasta files (*.fa) from the input_files directory, 
            runs MH on each 5 times and outputs to a file with the name
            of the sequence to the output_files directory
