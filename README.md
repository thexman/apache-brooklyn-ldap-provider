# apache-brooklyn-ldap-provider

LDAP provider for apache brooklyn, that allows to specify the user pattern for LDAP binding.

Creating eclipse project
=================================================================================

Create eclipse project
------------------------------------------------------------
`mvn -DdownloadSources=true eclipse:eclipse`


Build eclipse project
------------------------------------------------------------
`mvn clean install`

Installation in Apache Brooklyn
------------------------------------------------------------
Copy the produced JAR file to `./lib/dropins/` directory.
Add following configuration lines in `~/.brooklyn/brooklyn.properties`

```brooklyn.webconsole.security.provider=com.a9ski.brooklyn.LdapSecurityProvider
brooklyn.webconsole.security.ldap.url=ldap://localhost:10389/????X-BIND-USER=uid=admin%2cou=system,X-BIND-PASSWORD=secret,X-COUNT-LIMIT=1000
brooklyn.webconsole.security.ldap.realm=example.com
brooklyn.webconsole.security.ldap.userPattern=uid={0},ou={1},{2}
#brooklyn.webconsole.security.ldap.userPattern=uid={0},ou={1},ou=People,{2}
#brooklyn.webconsole.security.ldap.userPattern=cn={0},cn=Users,DC=domain,DC=company,DC=com
```



Release
=================================================================================

0. Change maven settings.xml and add account for OSSRH
```<settings>
  <servers>
    <server>
      <id>ossrh</id>
      <username>your-jira-id</username>
      <password>your-jira-pwd</password>
    </server>
  </servers>
</settings>
``` 
More information can be obtain from [OSSRH guide](http://central.sonatype.org/pages/ossrh-guide.html) and [Maven configuration](http://central.sonatype.org/pages/apache-maven.html)

1. `mvn clean install`

2. `mvn release:prepare`

3. checkout the newly created tag

4. `mvn -Prelease clean javadoc:jar source:jar gpg:sign -Dgpg.passphrase=mysecret-password-for-gpg install org.sonatype.plugins:nexus-staging-maven-plugin:deploy` 

**OR** just execute

`release.sh mysecret-password-for-gpg`

Step 2 can be done manually: a) remove -SNAPSHOT from the version in all pom.xml files (the parent pom.xml and all module's pom.xml) b) commit the changes and create new tag with the version c) add -SNAPSHOT to all pom.xml files and increase the version (e.g. 1.0.0 to 1.0.1-SNAPSHOT)