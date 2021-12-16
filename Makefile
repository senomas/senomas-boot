install:
	cd senomas-boot-parent ; mvn install
	cd senomas-boot-util ; mvn install
	cd senomas-boot-security ; mvn install

deploy:
	cd senomas-boot-parent ; mvn deploy
	cd senomas-boot-util ; mvn deploy
	cd senomas-boot-security ; mvn deploy
