/**
 * Copyright (c) 2010-2020 SAP and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *   SAP - initial API and implementation
 */
package org.eclipse.dirigible.kyma.oauth.filters;

import java.io.IOException;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.dirigible.api.v3.security.UserFacade;
import org.eclipse.dirigible.commons.api.context.ContextException;
import org.eclipse.dirigible.commons.api.context.ThreadContextFacade;
import org.eclipse.dirigible.commons.api.module.StaticInjector;
import org.eclipse.dirigible.core.security.api.AccessException;
import org.eclipse.dirigible.core.security.api.ISecurityCoreService;
import org.eclipse.dirigible.core.security.definition.AccessDefinition;
import org.eclipse.dirigible.core.security.service.SecurityCoreService;
import org.eclipse.dirigible.core.security.verifier.AccessVerifier;
import org.eclipse.dirigible.kyma.utils.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebFilter(urlPatterns = {
		"/services/v3/*",
		"/public/v3/*",
		"/websockets/v3/*",
		"/services/v4/*",
		"/public/v4/*",
		"/websockets/v4/*",
		"/odata/v2/*"
}, filterName = "XSUAA Security Filter", description = "Check all URIs for the permissions")
public class OAuthFilter extends AbstractOAuthFilter {

	private static final Logger logger = LoggerFactory.getLogger(OAuthFilter.class);

	private static final String PUBLIC = "public";
	private static final String SERVICES_V3_PUBLIC = "/services/v3/public";
	private static final String SERVICES_V4_PUBLIC = "/services/v4/public";
	private static final String SERVICES_V4_OAUTH = "/services/v4/oauth";

	private static final String UNAUTHORIZED_MESSAGE = "No logged in user";

	private static ISecurityCoreService securityCoreService = StaticInjector.getInjector().getInstance(SecurityCoreService.class);
	
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		String jwt = null;
		if (!isPublicEnabledAccess(request) && !isOAuth(request)) {			
			jwt = JwtUtils.getJwt(request);
			if (jwt == null) {
				authenticate(request, response);
				return;
			}
			if (JwtUtils.isValidJwt(jwt)) {
				if (JwtUtils.isExpiredJwt(jwt)) {
					authenticate(request, response);
					return;
				}
			} else {
				unauthorized(request, response, UNAUTHORIZED_MESSAGE);
				return;
				
			}
		}

		try {
			ThreadContextFacade.setUp();
			ThreadContextFacade.set(HttpServletRequest.class.getCanonicalName(), request);
			ThreadContextFacade.set(HttpServletResponse.class.getCanonicalName(), response);

			if (jwt != null) {				
				UserFacade.setName(JwtUtils.getClaim(jwt).getUserName());
			}
		} catch (ContextException e) {
			logger.info("Error while setting userName from XSUAA Filter.", e);
		}
		chain.doFilter(request, response);
	}

	private boolean isPublicEnabledAccess(ServletRequest servletRequest) {
		HttpServletRequest request = (HttpServletRequest) servletRequest;
		String requestURI = request.getRequestURI();
		if (requestURI.startsWith(SERVICES_V3_PUBLIC) || requestURI.startsWith(SERVICES_V4_PUBLIC)) {			
			try {
				String path = requestURI.substring(requestURI.indexOf(PUBLIC) + PUBLIC.length());
				String method = request.getMethod();
				List<AccessDefinition> accessDefinitions = AccessVerifier.getMatchingAccessDefinitions(securityCoreService, ISecurityCoreService.CONSTRAINT_SCOPE_HTTP, path, method);
				for (AccessDefinition next : accessDefinitions) {
					if (next.getRole().equalsIgnoreCase(ISecurityCoreService.ROLE_PUBLIC)) {
						return true;
					}
				}
			} catch (ServletException | AccessException e) {
				logger.error(e.getMessage(), e);
			}
		}
		return false;
	}

	private boolean isOAuth(ServletRequest servletRequest) {
		HttpServletRequest request = (HttpServletRequest) servletRequest;
		String requestURI = request.getRequestURI();
		return requestURI.startsWith(SERVICES_V4_OAUTH);
	}

	@Override
	protected Logger getLogger() {
		return logger;
	}
}
