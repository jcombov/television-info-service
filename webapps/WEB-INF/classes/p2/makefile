JC = javac
CLASSPATH = -classpath /Applications/apache-tomcat-9.0.11/lib/servlet-api.jar
#CLASSPATH = -classpath ~jorge/apache-tomcat/lib/servlet-api.jar
.SUFFIXES: .java .class
.java.class:
	$(JC) $(CLASSPATH) $(CLASSES)
CLASSES = \
	Sint82P2.java Canal.java Programa.java

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class
