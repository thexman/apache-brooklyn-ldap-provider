/*
 * #%L
 * LDAP provider for apache brooklyn
 * %%
 * Copyright (C) 2016 Kiril Arabadzhiyski
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.a9ski.brooklyn;

import static org.junit.Assert.*;

import org.junit.Test;

public class LdapSecurityProviderTest {

	private LdapSecurityProvider createProvider(String userPattern) {
		final LdapSecurityProvider p = new LdapSecurityProvider("ldap://localhost:10389", "a9ski.com", "users", userPattern);
		return p;
	}
	
	@Test
	public void testGetUserDN() {
		assertEquals("uid=kiril,ou=users,dc=a9ski,dc=com", createProvider(null).getUserDN("kiril"));
		assertEquals("cn=kiril,cn=users,DC=domain,DC=company,DC=com", createProvider("cn={0},cn=users,DC=domain,DC=company,DC=com").getUserDN("kiril"));
	}

}
