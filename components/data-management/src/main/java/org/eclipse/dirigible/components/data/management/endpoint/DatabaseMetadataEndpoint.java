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
package org.eclipse.dirigible.components.data.management.endpoint;

import java.sql.SQLException;
import java.util.Set;

import javax.ws.rs.QueryParam;

import org.eclipse.dirigible.components.base.endpoint.BaseEndpoint;
import org.eclipse.dirigible.components.data.management.service.DatabaseMetadataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiParam;

/**
 * The Class DataSourceMetadataEndpoint.
 */
@RestController
@RequestMapping(BaseEndpoint.PREFIX_ENDPOINT_DATA + "metadata")
public class DatabaseMetadataEndpoint extends BaseEndpoint {
	
	/** The databases service. */
	private final DatabaseMetadataService databasesService;

	/**
	 * Instantiates a new data source metadata endpoint.
	 *
	 * @param databasesService the databases service
	 */
	@Autowired
	public DatabaseMetadataEndpoint(DatabaseMetadataService databasesService) {
		this.databasesService = databasesService;
	}
		
	/**
	 * Gets the data sources.
	 *
	 * @return the data sources
	 */
	@GetMapping
	public ResponseEntity<Set<String>> getDataSourcesNames() {
		return ResponseEntity.ok(databasesService.getDataSourcesNames());
	}
	
	/**
	 * Gets the data source metadata.
	 *
	 * @param datasource the datasource
	 * @return the structures
	 * @throws SQLException the SQL exception
	 */
	@GetMapping("/{datasource}")
	public ResponseEntity<String> getSchemaMetadata(@ApiParam(value = "Name of the DataSource", required = true) @PathVariable("datasource") String datasource) throws SQLException {
		return ResponseEntity.ok(databasesService.getDataSourceMetadata(datasource));
	}
	
	/**
	 * Gets the metadata of a schema.
	 *
	 * @param datasource the datasource
	 * @param schema the schema
	 * @return the response entity
	 * @throws SQLException the SQL exception
	 */
	@GetMapping("/{datasource}/{schema}")
	public ResponseEntity<String> getSchemaMetadata(
			@ApiParam(value = "Name of the DataSource", required = true) @PathVariable("datasource") String datasource,
			@ApiParam(value = "Schema of the DataSource", required = true) @PathVariable("schema") String schema) throws SQLException {
		return ResponseEntity.ok(databasesService.getSchemaMetadata(datasource, schema));
	}
	
	/**
	 * Gets the metadata of a structure.
	 *
	 * @param datasource the datasource
	 * @param schema the schema
	 * @param structure the structure
	 * @param kind the kind
	 * @return the response entity
	 * @throws SQLException the SQL exception
	 */
	@GetMapping("/{datasource}/{schema}/{structure}")
	public ResponseEntity<String> getStructureMetadata(
			@ApiParam(value = "Name of the DataSource", required = true) @PathVariable("datasource") String datasource,
			@ApiParam(value = "Schema requested", required = true) @PathVariable("schema") String schema,
			@ApiParam(value = "Structure requested", required = true) @PathVariable("structure") String structure,
			@ApiParam(value = "Kind of the Structure", required = true) @QueryParam("kind") String kind) throws SQLException {
		
		return ResponseEntity.ok(databasesService.getStructureMetadata(datasource, schema, structure, kind));
	}

}
