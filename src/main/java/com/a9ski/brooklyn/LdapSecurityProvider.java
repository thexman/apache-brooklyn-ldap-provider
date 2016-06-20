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

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.InitialDirContext;
import javax.servlet.http.HttpSession;

import org.apache.brooklyn.api.mgmt.ManagementContext;
import org.apache.brooklyn.config.ConfigKey;
import org.apache.brooklyn.config.StringConfigMap;
import org.apache.brooklyn.core.config.ConfigKeys;
import org.apache.brooklyn.rest.BrooklynWebConfig;
import org.apache.brooklyn.rest.security.provider.AbstractSecurityProvider;
import org.apache.brooklyn.util.exceptions.Exceptions;
import org.apache.brooklyn.util.text.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.CharMatcher;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public class LdapSecurityProvider extends AbstractSecurityProvider {
	public static final Logger LOG = LoggerFactory.getLogger(LdapSecurityProvider.class);

	public static final String LDAP_CONTEXT_FACTORY = "com.sun.jndi.ldap.LdapCtxFactory";
	
	public final static ConfigKey<String> LDAP_USER_PATTERN = ConfigKeys.newStringConfigKey("brooklyn.webconsole.security.ldap.userPattern");
	
	private static final String DEFAULT_USER_PATTERN = "uid={0},ou={1},{2}";

	private final String ldapUrl;
	private final String ldapRealm;
	private final String organizationUnit;
	private final String userPattern;

	public LdapSecurityProvider(ManagementContext mgmt) {
		StringConfigMap properties = mgmt.getConfig();
		ldapUrl = properties.getConfig(BrooklynWebConfig.LDAP_URL);
		Strings.checkNonEmpty(ldapUrl,
				"LDAP security provider configuration missing required property " + BrooklynWebConfig.LDAP_URL);
		ldapRealm = CharMatcher.isNot('"').retainFrom(properties.getConfig(BrooklynWebConfig.LDAP_REALM));
		Strings.checkNonEmpty(ldapRealm,
				"LDAP security provider configuration missing required property " + BrooklynWebConfig.LDAP_REALM);

		if (Strings.isBlank(properties.getConfig(BrooklynWebConfig.LDAP_OU))) {
			LOG.info("Setting LDAP ou attribute to: Users");
			organizationUnit = "Users";
		} else {
			organizationUnit = CharMatcher.isNot('"').retainFrom(properties.getConfig(BrooklynWebConfig.LDAP_OU));
		}
		
		final String userPatternProp = properties.getConfig(LDAP_USER_PATTERN);
		if (Strings.isBlank(userPatternProp)) {			
			userPattern = DEFAULT_USER_PATTERN;
		} else {
			userPattern = userPatternProp;
		}
		LOG.info("Setting LDAP user pattern to: " + userPatternProp);
		
		Strings.checkNonEmpty(ldapRealm, "LDAP security provider configuration missing required property " + BrooklynWebConfig.LDAP_OU);
	}

	public LdapSecurityProvider(String ldapUrl, String ldapRealm, String organizationUnit) {
		this(ldapUrl, ldapRealm, organizationUnit, DEFAULT_USER_PATTERN);
	}
	
	public LdapSecurityProvider(String ldapUrl, String ldapRealm, String organizationUnit, String userPattern) {
		this.ldapUrl = ldapUrl;
		this.ldapRealm = ldapRealm;
		this.organizationUnit = organizationUnit;
		if (Strings.isBlank(userPattern)) {			
			this.userPattern = DEFAULT_USER_PATTERN;
		} else {
			this.userPattern = userPattern;
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public boolean authenticate(HttpSession session, String user, String password) {
		if (session == null || user == null)
			return false;
		checkCanLoad();

		Hashtable env = new Hashtable();
		env.put(Context.INITIAL_CONTEXT_FACTORY, LDAP_CONTEXT_FACTORY);
		env.put(Context.PROVIDER_URL, ldapUrl);
		env.put(Context.SECURITY_AUTHENTICATION, "simple");
		env.put(Context.SECURITY_PRINCIPAL, getUserDN(user));
		env.put(Context.SECURITY_CREDENTIALS, password);

		try {
			new InitialDirContext(env);
			return allow(session, user);
		} catch (NamingException e) {
			return false;
		}
	}

	/**
	 * Returns the LDAP path for the user
	 *
	 * @param user
	 * @return String
	 */
	protected String getUserDN(String user) {
		final List<String> domain = Lists.transform(Arrays.asList(ldapRealm.split("\\.")), new Function<String, String>() {
			@Override
			public String apply(String input) {
				return "dc=" + input;
			}
		});

		final String dc = Joiner.on(",").join(domain).toLowerCase();
		return MessageFormat.format(userPattern, user, organizationUnit, dc);
		
	}

	static boolean triedLoading = false;

	public synchronized static void checkCanLoad() {
		if (triedLoading)
			return;
		try {
			Class.forName(LDAP_CONTEXT_FACTORY);
			triedLoading = true;
		} catch (Throwable e) {
			throw Exceptions.propagate(new ClassNotFoundException("Unable to load LDAP classes (" + LDAP_CONTEXT_FACTORY
					+ ") required for Brooklyn LDAP security provider"));
		}
	}

}
