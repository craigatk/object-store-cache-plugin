.PHONY: run_sample publish_local

run_sample: publish_local
	cd sample && ../gradlew clean test --build-cache --info

publish_local:
	./gradlew publishToMavenLocal publishPluginMavenPublicationToMavenLocal
