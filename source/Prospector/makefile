##### PROSPECTOR (PG04) #####
##### 	  MAKEFILE		#####

# Some commands in this makefile are simply
# wrappers to the equivalent ANT commands

JAR_PKG = Prospector.jar

# show help message by default
Default:
	@echo "make clean: Delete built application"
	@echo "make build: Build java classes (ant wrapper)"
	@echo "make jar: Build jar (ant wrapper)"
	@echo "make run: Run application"

build: 
	ant build
	
jar: 
	ant jar

clean:
	rm -frv bin
	rm -frv jar

run:
	java -jar jar/$(JAR_PKG)