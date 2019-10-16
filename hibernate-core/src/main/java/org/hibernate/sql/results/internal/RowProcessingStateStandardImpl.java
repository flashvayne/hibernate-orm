/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sql.results.internal;

import java.sql.SQLException;
import java.util.List;

import org.hibernate.NotYetImplementedFor6Exception;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.loader.plan.spi.EntityFetch;
import org.hibernate.query.NavigablePath;
import org.hibernate.query.spi.QueryOptions;
import org.hibernate.sql.exec.spi.Callback;
import org.hibernate.sql.exec.spi.DomainParameterBindingContext;
import org.hibernate.sql.results.spi.Initializer;
import org.hibernate.sql.results.spi.JdbcValues;
import org.hibernate.sql.results.spi.JdbcValuesSourceProcessingState;
import org.hibernate.sql.results.spi.RowProcessingState;
import org.hibernate.sql.results.spi.RowReader;

import org.jboss.logging.Logger;

/**
 * @author Steve Ebersole
 */
public class RowProcessingStateStandardImpl implements RowProcessingState {
	private static final Logger log = Logger.getLogger( RowProcessingStateStandardImpl.class );
	private static final Initializer[] NO_INITIALIZERS = new Initializer[0];

	private final JdbcValuesSourceProcessingStateStandardImpl resultSetProcessingState;
	private final QueryOptions queryOptions;

	private final Initializer[] initializers;

	private final JdbcValues jdbcValues;
	private Object[] currentRowJdbcValues;

	public RowProcessingStateStandardImpl(
			JdbcValuesSourceProcessingStateStandardImpl resultSetProcessingState,
			QueryOptions queryOptions,
			RowReader<?> rowReader,
			JdbcValues jdbcValues) {
		this.resultSetProcessingState = resultSetProcessingState;
		this.queryOptions = queryOptions;
		this.jdbcValues = jdbcValues;

		final List<Initializer> initializers = rowReader.getInitializers();
		if ( initializers == null || initializers.isEmpty() ) {
			this.initializers = NO_INITIALIZERS;
		}
		else {
			//noinspection ToArrayCallWithZeroLengthArrayArgument
			this.initializers = initializers.toArray( new Initializer[initializers.size()] );
		}
	}

	@Override
	public JdbcValuesSourceProcessingState getJdbcValuesSourceProcessingState() {
		return resultSetProcessingState;
	}

	public boolean next() throws SQLException {
		if ( jdbcValues.next( this ) ) {
			currentRowJdbcValues = jdbcValues.getCurrentRowValuesArray();
			return true;
		}
		else {
			currentRowJdbcValues = null;
			return false;
		}
	}

	@Override
	public Object getJdbcValue(int position) {
		return currentRowJdbcValues[ position ];
	}

	@Override
	public void registerNonExists(EntityFetch fetch) {
	}

	@Override
	public void finishRowProcessing() {
		currentRowJdbcValues = null;
	}

	@Override
	public SharedSessionContractImplementor getSession() {
		return getJdbcValuesSourceProcessingState().getExecutionContext().getSession();
	}

	@Override
	public QueryOptions getQueryOptions() {
		return queryOptions;
	}

	@Override
	public DomainParameterBindingContext getDomainParameterBindingContext() {
		throw new NotYetImplementedFor6Exception();
	}

	@Override
	public Callback getCallback() {
		return afterLoadAction -> {};
	}

	@Override
	public Initializer resolveInitializer(NavigablePath path) {
		for ( Initializer initializer : initializers ) {
			if ( initializer.getNavigablePath().equals( path ) ) {
				return initializer;
			}
		}

		return null;
	}
}
