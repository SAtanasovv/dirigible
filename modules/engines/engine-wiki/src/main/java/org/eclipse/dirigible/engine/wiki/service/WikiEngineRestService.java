/*
 * Copyright (c) 2022 SAP SE or an SAP affiliate company and Eclipse Dirigible contributors
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-FileCopyrightText: 2022 SAP SE or an SAP affiliate company and Eclipse Dirigible contributors
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.dirigible.engine.wiki.service;

import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.eclipse.dirigible.commons.api.service.AbstractRestService;
import org.eclipse.dirigible.commons.api.service.IRestService;
import org.eclipse.dirigible.engine.wiki.processor.WikiEngineProcessor;
import org.eclipse.dirigible.repository.api.IRepositoryStructure;
import org.eclipse.dirigible.repository.api.IResource;
import org.eclipse.dirigible.repository.api.RepositoryNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;

/**
 * Front facing REST service serving the wiki pages.
 */
@Path("/wiki")
@Api(value = "Core - Wiki Engine", authorizations = { @Authorization(value = "basicAuth", scopes = {}) })
@ApiResponses({ @ApiResponse(code = 401, message = "Unauthorized"), @ApiResponse(code = 403, message = "Forbidden"),
		@ApiResponse(code = 404, message = "Not Found") })
public class WikiEngineRestService extends AbstractRestService implements IRestService {

	/** The Constant logger. */
	private static final Logger logger = LoggerFactory.getLogger(WikiEngineRestService.class);

	/** The processor. */
	private WikiEngineProcessor processor = new WikiEngineProcessor();

	/** The response. */
	@Context
	private HttpServletResponse response;

	/**
	 * Gets the wiki page.
	 *
	 * @param path
	 *            the path
	 * @return the wiki page
	 */
	@GET
	@Path("/{path:.*}")
	@ApiOperation("Get Resource Content")
	@ApiResponses({ @ApiResponse(code = 200, message = "Get the content fo the resource", response = byte[].class),
			@ApiResponse(code = 404, message = "No such resource") })
	public Response getWikiPage(@ApiParam(value = "Path of the Resource", required = true) @PathParam("path") String path) {
		return render(path);
	}

	/**
	 * Render.
	 *
	 * @param path
	 *            the path
	 * @return the response
	 */
	protected Response render(@PathParam("path") String path) {
		if ("".equals(path.trim()) || path.trim().endsWith(IRepositoryStructure.SEPARATOR)) {
			String message = "Listing of web folders is forbidden.";
			return createErrorResponseForbidden(message);
		}
		if (processor.existResource(path)) {
			IResource resource = processor.getResource(path);
			if (resource.isBinary()) {
				String message = "Resource found, but it is a binary file: " + path;
				throw new RepositoryNotFoundException(message);
			}
			String content = new String(resource.getContent(), StandardCharsets.UTF_8);
			String html = processor.renderContent(path, content);
			return Response.ok(html).type(resource.getContentType()).build();
		}

		String errorMessage = "Resource not found: " + path;
		try {
			byte[] content = processor.getResourceContent(path);
			if (content != null) {
				String html = processor.renderContent(path, new String(content, StandardCharsets.UTF_8));
				return Response.ok().entity(html).build();
			}
		} catch (RepositoryNotFoundException e) {
			throw new RepositoryNotFoundException(errorMessage, e);
		}
		throw new RepositoryNotFoundException(errorMessage);
	}

	/**
	 * Gets the type.
	 *
	 * @return the type
	 */
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.dirigible.commons.api.service.IRestService#getType()
	 */
	@Override
	public Class<? extends IRestService> getType() {
		return WikiEngineRestService.class;
	}

	/**
	 * Gets the logger.
	 *
	 * @return the logger
	 */
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.dirigible.commons.api.service.AbstractRestService#getLogger()
	 */
	@Override
	protected Logger getLogger() {
		return logger;
	}
}
