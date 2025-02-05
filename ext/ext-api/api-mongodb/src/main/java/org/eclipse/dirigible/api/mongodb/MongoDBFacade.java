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
package org.eclipse.dirigible.api.mongodb;

import org.eclipse.dirigible.commons.api.scripting.IScriptingFacade;
import org.eclipse.dirigible.commons.config.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

/**
 * The Class MongoDBFacade.
 */
public class MongoDBFacade implements IScriptingFacade {
	
	/** The Constant DIRIGIBLE_MONGODB_CLIENT_URI. */
	private static final String DIRIGIBLE_MONGODB_CLIENT_URI = "DIRIGIBLE_MONGODB_CLIENT_URI";
	
	/** The Constant CLIENT_URI. */
	private static final String CLIENT_URI = "mongodb://localhost:27017";
	
	/** The Constant DIRIGIBLE_MONGODB_DATABASE_DEFAULT. */
	private static final String DIRIGIBLE_MONGODB_DATABASE_DEFAULT = "DIRIGIBLE_MONGODB_DATABASE_DEFAULT";
	
	/** The Constant DIRIGIBLE_MONGODB_DATABASE_DEFAULT_DB. */
	private static final String DIRIGIBLE_MONGODB_DATABASE_DEFAULT_DB = "db";
	
	/** The Constant logger. */
	private static final Logger logger = LoggerFactory.getLogger(MongoDBFacade.class);
	
	
	/**
	 * Gets the client.
	 *
	 * @return the client
	 */
	public static MongoClient getClient() {
		
		String clientUri = Configuration.get(DIRIGIBLE_MONGODB_CLIENT_URI, CLIENT_URI);
		
		MongoClient mongoClient = new MongoClient(new MongoClientURI(clientUri));
		
		return mongoClient;
	}
	
	/**
	 * Creates the basic DB object.
	 *
	 * @return the DB object
	 */
	public static DBObject createBasicDBObject() {
		return new BasicDBObject();
	}
	
	/**
	 * Gets the default database name.
	 *
	 * @return the default database name
	 */
	public static String getDefaultDatabaseName() {
		return Configuration.get(DIRIGIBLE_MONGODB_DATABASE_DEFAULT, DIRIGIBLE_MONGODB_DATABASE_DEFAULT_DB);
	}

}
