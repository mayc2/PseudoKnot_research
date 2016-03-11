python_interface:
	python setup.py build_ext --inplace

#to generate your own wrapper files using SWIG
#should not normally be needed
swig:
	swig -python -c++ -keyword RNAstructure.i

clean:
	rm -rf build
