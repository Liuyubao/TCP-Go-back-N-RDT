SHELL = /bin/bash -o pipefail
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) *.java

TARGETS = rdt_sim

default: .java.class
	@echo 'java Main $$1 $$2 $$3 $$4 $$5 $$6 $$7' > rdt_sim
	chmod +x rdt_sim

clean:
	$(RM) *.class
	$(RM) sim/*.class
	$(RM) rdt_sim

run:
	./${TARGETS} 1000 0.1 100 0 0 0 2

test:
	./${TARGETS} 1000 0.1 100 0.2 0.2 0.2 1

submission: clean
	@read -p "your firstname? " name; \
	(tar -cf - ./MyRdt*.java writeup.pdf | \gzip > p2j_$$name.tar.gz) \
	|| (rm p2j_$$name.tar.gz; exit 1);
	@echo 'done! please double check before submitting.'
