mvn -Prelease clean javadoc:jar source:jar gpg:sign -Dgpg.passphrase=$1 install org.sonatype.plugins:nexus-staging-maven-plugin:deploy
