/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.orm.docs;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

/**
 * Basically a DAO for the doc pub descriptor
 *
 * @author Steve Ebersole
 */
public class DescriptorAccess {
	public static final String DETAILS_URL = "https://docs.jboss.org/hibernate/_outdated-content/orm.json";

	/**
	 * Load the descriptor
	 * @return
	 */
	public static ProjectDocumentation loadProject() {
		try ( final CloseableHttpClient httpClient = HttpClientBuilder.create().build() ) {
			final HttpGet request = new HttpGet( DETAILS_URL );

			try ( final CloseableHttpResponse response = httpClient.execute( request ) ) {
				final HttpEntity responseEntity = response.getEntity();
				final Jsonb jsonb = JsonbBuilder.create( new JsonbConfig().withFormatting( true ) );
				return jsonb.fromJson( responseEntity.getContent(), ProjectDocumentation.class );

			}
		}
		catch (IOException e) {
			throw new RuntimeException( "Unable to create HttpClient", e );
		}
	}

	public static void storeProject(ProjectDocumentation project, File jsonFile) {
		prepareJsonFile( jsonFile );

		final Jsonb jsonb = JsonbBuilder.create( new JsonbConfig().withFormatting( true ) );

		try ( final FileWriter writer = new FileWriter( jsonFile ) ) {
			jsonb.toJson( project, writer );
		}
		catch (IOException e) {
			throw new RuntimeException( "Unable to open write for JSON file : " + jsonFile.getPath(), e );
		}
	}

	private static void prepareJsonFile(File jsonFile) {
		if ( jsonFile.exists() ) {
			final boolean deleted = jsonFile.delete();
			assert deleted;
		}

		if ( ! jsonFile.getParentFile().exists() ) {
			final boolean dirsMade = jsonFile.getParentFile().mkdirs();
			assert dirsMade;
		}

		try {
			final boolean created = jsonFile.createNewFile();
			assert created;
		}
		catch (IOException e) {
			throw new RuntimeException( "Unable to create JSON file : `" + jsonFile.getPath() + "`", e );
		}
	}
}
