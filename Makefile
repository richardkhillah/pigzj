JAVAC=javac

JAVA=java

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
	rm -f ${COMPS} test.gz