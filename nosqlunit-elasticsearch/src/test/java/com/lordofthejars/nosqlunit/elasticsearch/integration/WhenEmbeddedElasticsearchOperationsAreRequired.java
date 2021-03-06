package com.lordofthejars.nosqlunit.elasticsearch.integration;

import static com.lordofthejars.nosqlunit.elasticsearch.EmbeddedElasticsearch.EmbeddedElasticsearchRuleBuilder.newEmbeddedElasticsearchRule;
import static com.lordofthejars.nosqlunit.elasticsearch.ManagedElasticsearch.ManagedElasticsearchRuleBuilder.newManagedElasticsearchRule;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.util.Map;

import org.elasticsearch.action.deletebyquery.DeleteByQueryRequestBuilder;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.node.Node;
import org.junit.After;
import org.junit.ClassRule;
import org.junit.Test;

import com.lordofthejars.nosqlunit.elasticsearch.ElasticsearchOperation;
import com.lordofthejars.nosqlunit.elasticsearch.EmbeddedElasticsearch;
import com.lordofthejars.nosqlunit.elasticsearch.EmbeddedElasticsearchInstancesFactory;
import com.lordofthejars.nosqlunit.elasticsearch.ManagedElasticsearch;

public class WhenEmbeddedElasticsearchOperationsAreRequired {

	
	private static final String ELASTICSEARCH_DATA = "{\n" + 
			"   \"documents\":[\n" + 
			"      {\n" + 
			"         \"document\":[\n" + 
			"            {\n" + 
			"               \"index\":{\n" + 
			"                  \"indexName\":\"tweeter\",\n" + 
			"                  \"indexType\":\"tweet\",\n" +
			"                  \"indexId\":\"1\"\n" +
			"               }\n" + 
			"            },\n" + 
			"            {\n" + 
			"               \"data\":{\n" + 
			"                  \"name\":\"a\",\n" + 
			"                  \"msg\":\"b\"\n" + 
			"               }\n" + 
			"            }\n" + 
			"         ]\n" + 
			"      }\n" + 
			"   ]\n" + 
			"}";
	
	
	@ClassRule
	public static final EmbeddedElasticsearch EMBEDDED_ELASTICSEARCH = newEmbeddedElasticsearchRule().build();
	
	@After
	public void removeIndexes() {
		Node defaultEmbeddedInstance = EmbeddedElasticsearchInstancesFactory.getInstance().getDefaultEmbeddedInstance();
		Client client = defaultEmbeddedInstance.client();
		
		DeleteByQueryRequestBuilder deleteByQueryRequestBuilder = new DeleteByQueryRequestBuilder(client);
		deleteByQueryRequestBuilder.setQuery(QueryBuilders.matchAllQuery());
		deleteByQueryRequestBuilder.execute().actionGet();
		
		client.admin().indices().prepareRefresh().execute().actionGet();
		
		client.close();
	}
	
	@Test
	public void insert_operation_should_index_all_dataset() {
		
		Node defaultEmbeddedInstance = EmbeddedElasticsearchInstancesFactory.getInstance().getDefaultEmbeddedInstance();
		Client client = defaultEmbeddedInstance.client();
		
		ElasticsearchOperation elasticsearchOperation = new ElasticsearchOperation(client);
		elasticsearchOperation.insert(new ByteArrayInputStream(ELASTICSEARCH_DATA.getBytes()));
		
		GetResponse document = client.prepareGet("tweeter", "tweet", "1").execute().actionGet();
		Map<String, Object> documentSource = document.getSource();
		
		//Strange a cast to Object
		assertThat(documentSource, hasEntry("name", (Object)"a"));
		assertThat(documentSource, hasEntry("msg", (Object)"b"));
		
		client.close();
	}
	
	@Test
	public void delete_operation_should_remove_all_Indexes() {
		
		Node defaultEmbeddedInstance = EmbeddedElasticsearchInstancesFactory.getInstance().getDefaultEmbeddedInstance();
		Client client = defaultEmbeddedInstance.client();
		
		ElasticsearchOperation elasticsearchOperation = new ElasticsearchOperation(client);
		elasticsearchOperation.insert(new ByteArrayInputStream(ELASTICSEARCH_DATA.getBytes()));
		
		elasticsearchOperation.deleteAll();
		
		GetResponse document = client.prepareGet("tweeter", "tweet", "1").execute().actionGet();
		assertThat(document.isSourceEmpty(), is(true));
		
		client.close();
	}
	
	@Test
	public void databaseIs_operation_should_compare_all_Indexes() {
		
		Node defaultEmbeddedInstance = EmbeddedElasticsearchInstancesFactory.getInstance().getDefaultEmbeddedInstance();
		Client client = defaultEmbeddedInstance.client();
		
		ElasticsearchOperation elasticsearchOperation = new ElasticsearchOperation(client);
		elasticsearchOperation.insert(new ByteArrayInputStream(ELASTICSEARCH_DATA.getBytes()));
		
		boolean isEqual = elasticsearchOperation.databaseIs(new ByteArrayInputStream(ELASTICSEARCH_DATA.getBytes()));
		
		assertThat(isEqual, is(true));
		
		client.close();
	}
}
