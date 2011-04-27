BASIC:

To learn about python:

	www.python.org

To program in python:

	Use either latest python (for small projects)
	or Eclipse with plugin http://pydev.sf.net/updates/.

INTERMEDIATE:

To use scripts within java:

	http://pnuts.org/~tomatsu/embedding.html.

To connect different run-times and different C, Java or Python libraries:

	Use SPIRO.

To use python in web applications:

	Use Jython's PyServlet.

ADVANCED:

To handle mobile code, secure messaging and distributed services:

	Use JADE.

To use python modules as native java classes:

	Compile them using jythonc: 
 
	> jythonc -a -j test.jar test.py
	> java -cp test.jar test

	Limitation: 
	1. Only one public class per module, with same name as module.
	2. This class must inherit a java class or interface.
	3. Any new public fields or methods need a Java declaration in its 
	   description, see jythonc doc.

To load jython modules as Java without compilation - possible but not easy:

	Study org.jython.imp and 
	http://www.javaworld.com/javaworld/javatips/jw-javatip39.html