JAVAC=javac

JAVA=java

JAR=jar cf

MAIN = Pigzj
SRCS := $(wildcard *.java)
COMPS := $(wildcard *.class)

P=
P_NUM=

all:
	$(JAVAC) ${SRCS}

run:
	$(JAVA) $(MAIN) $(P) $(P_NUM)

clean:
	rm -f ${COMPS} *.gz *.jar

dist: clean
	$(JAR) hw3.jar ${SRCS} README.txt