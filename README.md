# SimpleAdderWithPython
An updated version of the Simple Adder tutorial project for UCEF.

Updates
- sending and receiving string of variables between federates
- running python within a federate
- sending string to and returning string from python
- adding time delay for federate to receive response before beginning next timestep

The RandNumGenerator federate creates two random numbers, turns them into a string, and sends them to the SimpleController federate

The SimpleController federate receives the string, runs a python script that adds the two numbers and returns the sum, and sends the result to the RandNumGenerator federate.

The RandNumGenerator federate displays the sum before repeating the process and sending two more random numbers.

