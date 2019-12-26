JFLAGS = -d ./ 
JC = javac
CLASSPATH = -classpath ~jorge/apache-tomcat/lib/servlet-api.jar
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $(CLASSPATH) $(CLASSES)
CLASSES = \
	Sint82P2.java Canal.java Programa.java

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class
