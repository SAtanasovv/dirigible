/*
 * Copyright (c) 2010-2021 SAP SE or an SAP affiliate company and Eclipse Dirigible contributors
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-FileCopyrightText: 2010-2021 SAP SE or an SAP affiliate company and Eclipse Dirigible contributors
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.dirigible.database.sql.dialects.hana;

import org.eclipse.dirigible.database.sql.DatabaseArtifactTypes;
import org.eclipse.dirigible.database.sql.builders.AlterBranchingBuilder;
import org.eclipse.dirigible.database.sql.builders.DropBranchingBuilder;
import org.eclipse.dirigible.database.sql.builders.records.DeleteBuilder;
import org.eclipse.dirigible.database.sql.builders.records.InsertBuilder;
import org.eclipse.dirigible.database.sql.builders.records.SelectBuilder;
import org.eclipse.dirigible.database.sql.builders.records.UpdateBuilder;
import org.eclipse.dirigible.database.sql.dialects.DefaultSqlDialect;
import org.eclipse.dirigible.database.sql.dialects.derby.DerbyDropBranchingBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * The HANA SQL Dialect.
 */
public class HanaSqlDialect extends
        DefaultSqlDialect<SelectBuilder, InsertBuilder, UpdateBuilder, DeleteBuilder, HanaCreateBranchingBuilder, AlterBranchingBuilder, HanaDropBranchingBuilder, HanaNextValueSequenceBuilder, HanaLastValueIdentityBuilder>
        implements DatabaseArtifactTypes {

    private static final String IDENTITY_ARGUMENT = "GENERATED BY DEFAULT AS IDENTITY (START WITH 1 INCREMENT BY 1)";
    private static final Logger logger = LoggerFactory.getLogger(HanaSqlDialect.class);

    private boolean isSequenceExisting(Connection connection, String sequence) throws SQLException {
        String sql = "ALTER SEQUENCE \"" + sequence + "\"";
        PreparedStatement statement = connection.prepareStatement(sql);
        try {
            statement.executeUpdate();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isProcedureExisting(Connection connection, String procedure) throws SQLException {
        DatabaseMetaData metadata = connection.getMetaData();
        ResultSet procedureDescription = metadata.getProcedures(null, null, procedure);
        return procedureDescription.next();
    }

    private boolean isFunctionExisting(Connection connection, String function) throws SQLException {
        DatabaseMetaData metadata = connection.getMetaData();
        ResultSet funcDescription = metadata.getFunctions(null, null, function);
        return funcDescription.next();
    }

    public static final Set<String> FUNCTIONS = Collections.synchronizedSet(new HashSet<String>(Arrays.asList(new String[]{
            "abap_alphanum",
            "abap_numc",
            "abap_lower",
            "abap_upper",
            "abs",
            "acos",
            "add_days",
            "add_months",
            "add_months_last",
            "add_nano100",
            "add_seconds",
            "add_workdays",
            "add_years",
            "ascii",
            "asin",
            "atan",
            "atan2",
            "auto_corr",
            "bintohex",
            "bintonhex",
            "bintostr",
            "bitand",
            "bitcount",
            "bitnot",
            "bitor",
            "bitset",
            "bitunset",
            "bitxor",
            "cardinality",
            "cast",
            "ceil",
            "char",
            "coalesce",
            "concat",
            "concat_naz",
            "convert_currency",
            "convert_unit",
            "corr",
            "corr_spearman",
            "cos",
            "cosh",
            "cot",
            "cross_corr",
            "current_connection",
            "current_date",
            "current_identity_value",
            "current_mvcc_snapshot_timestamp",
            "current_object_schema",
            "current_schema",
            "current_time",
            "current_timestamp",
            "current_transaction_isolation_level",
            "current_update_statement_sequence",
            "current_update_transaction",
            "current_user",
            "current_utcdate",
            "current_utctime",
            "current_utctimestamp",
            "dayname",
            "dayofmonth",
            "dayofyear",
            "days_between",
            "dft",
            "encryption_root_keys_extract_keys",
            "encryption_root_keys_has_backup_password",
            "escape_double_quotes",
            "escape_single_quotes",
            "exp",
            "expression_macro",
            "extract",
            "first_value",
            "floor",
            "generate_password",
            "greatest",
            "grouping",
            "grouping_id",
            "hamming_distance",
            "hash_md5",
            "hash_sha256",
            "hextobin",
            "hour",
            "ifnull",
            "indexing_error_code",
            "indexing_error_message",
            "indexing_status",
            "initcap",
            "is_sql_injection_safe",
            "isoweek",
            "json_query",
            "json_table",
            "json_value",
            "language",
            "last_day",
            "last_value",
            "lcase",
            "least",
            "left",
            "length",
            "ln",
            "localtoutc",
            "locate",
            "locate_regexpr",
            "log",
            "lower",
            "lpad",
            "ltrim",
            "map",
            "median",
            "member_at",
            "mimetype",
            "minute",
            "mod",
            "month",
            "monthname",
            "months_between",
            "nano100_between",
            "nchar",
            "ndiv0",
            "next_day",
            "newuid",
            "normalize",
            "now",
            "nth_value",
            "nullif",
            "occurrences_regexpr",
            "plaintext",
            "power",
            "quarter",
            "rand",
            "rand_secure",
            "record_commit_timestamp",
            "record_id",
            "replace",
            "replace_regexpr",
            "result_cache_id",
            "result_cache_refresh_time",
            "right",
            "round",
            "rpad",
            "rtrim",
            "score",
            "second",
            "seconds_between",
            "series_disaggregate",
            "series_element_to_period",
            "series_generate",
            "series_period_to_element",
            "series_round",
            "session_context",
            "session_user",
            "sign",
            "sin",
            "sinh",
            "soundex",
            "sqrt",
            "stddev_pop",
            "stddev_samp",
            "string_agg",
            "strtobin",
            "subarray",
            "substr_after",
            "substr_before",
            "substring_regexpr",
            "substring",
            "sysuuid",
            "tan",
            "tanh",
            "to_alphanum",
            "to_bigint",
            "to_binary",
            "to_blob",
            "to_boolean",
            "to_clob",
            "to_date",
            "to_dats",
            "to_decimal",
            "to_double",
            "to_fixedchar",
            "to_int",
            "to_integer",
            "to_json_boolean",
            "to_nclob",
            "to_nvarchar",
            "to_real",
            "to_seconddate",
            "to_smalldecimal",
            "to_smallint",
            "to_time",
            "to_timestamp",
            "to_tinyint",
            "to_varchar",
            "trim",
            "trim_array",
            "ucase",
            "uminus",
            "unicode",
            "upper",
            "utctolocal",
            "var_pop",
            "var_samp",
            "week",
            "weekday",
            "width_bucket",
            "workdays_between",
            "xmlextract",
            "xmlextractvalue",
            "xmltable",
            "year",
            "years_between",

            "count",
            "sum",
            "avg",
            "min",
            "max",

            "and",
            "or",
            "between",
            "binary",
            "case",
            "div",
            "in",
            "is",
            "not",
            "null",
            "like",
            "rlike",
            "xor"

    })));


    /*
     * (non-Javadoc)
     * @see org.eclipse.dirigible.database.sql.dialects.DefaultSqlDialect#nextval(java.lang.String)
     */
    @Override
    public HanaNextValueSequenceBuilder nextval(String sequence) {
        return new HanaNextValueSequenceBuilder(this, sequence);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.dirigible.database.sql.dialects.DefaultSqlDialect#create()
     */
    @Override
    public HanaCreateBranchingBuilder create() {
        return new HanaCreateBranchingBuilder(this);
    }

    @Override
    public HanaDropBranchingBuilder drop() {
        return new HanaDropBranchingBuilder(this);
    }

    @Override
    public String getIdentityArgument() {
        return IDENTITY_ARGUMENT;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.dirigible.database.sql.dialects.DefaultSqlDialect#nextval(java.lang.String)
     */
    @Override
    public HanaLastValueIdentityBuilder lastval(String... args) {
        return new HanaLastValueIdentityBuilder(this, args);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.dirigible.database.sql.ISqlDialect#exists(java.sql.Connection, java.lang.String)
     */
    @Override
    public boolean exists(Connection connection, String table) throws SQLException {
        return exists(connection, table, DatabaseArtifactTypes.TABLE);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.dirigible.database.sql.ISqlDialect#exists(java.sql.Connection, java.lang.String, java.lang.int)
     */
    @Override
    public boolean exists(Connection connection, String artefact, int type) throws SQLException {
        boolean exists = false;
        try {
            switch (type) {
                case DatabaseArtifactTypes.TABLE:
                case DatabaseArtifactTypes.VIEW:
                case DatabaseArtifactTypes.SYNONYM:
                    exists = count(connection, artefact) >= 0;
                    break;
                case DatabaseArtifactTypes.FUNCTION:
                    exists = isFunctionExisting(connection, artefact);
                    break;
                case DatabaseArtifactTypes.PROCEDURE:
                    exists = isProcedureExisting(connection, artefact);
                    break;
                case DatabaseArtifactTypes.SEQUENCE:
                    exists = isSequenceExisting(connection, artefact);
                    break;
            }
        } catch (Exception e) {
            // Do nothing, because the artifact do not exist
        }
        return exists;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.dirigible.database.sql.ISqlDialect#isSchemaFilterSupported()
     */
    @Override
    public boolean isSchemaFilterSupported() {
        return true;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.dirigible.database.sql.ISqlDialect#getSchemaFilterScript()
     */
    @Override
    public String getSchemaFilterScript() {
        return "SELECT * FROM \"SYS\".\"SCHEMAS\"";
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.dirigible.database.sql.ISqlDialect#getFunctionsNames()
     */
    @Override
    public Set<String> getFunctionsNames() {
        return FUNCTIONS;
    }

}

