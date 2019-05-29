package com.youjian.xunwu.search.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.youjian.xunwu.comm.entity.House;
import com.youjian.xunwu.comm.entity.HouseDetail;
import com.youjian.xunwu.comm.entity.HouseTag;
import com.youjian.xunwu.comm.entity.search.HouseIndexKey;
import com.youjian.xunwu.comm.entity.search.HouseIndexTemplate;
import com.youjian.xunwu.dao.HouseDetailRepository;
import com.youjian.xunwu.dao.HouseRepository;
import com.youjian.xunwu.dao.HouseTagRepository;
import com.youjian.xunwu.search.service.ISearchService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryAction;
import org.elasticsearch.index.reindex.DeleteByQueryRequestBuilder;
import org.elasticsearch.rest.RestStatus;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SearchServiceImpl implements ISearchService {

    private static final String INDEX_NAME = "xunwu";
    private static final String INDEX_TYPE = "house";

    @Autowired
    private HouseRepository houseRepository;
    @Autowired
    private HouseDetailRepository houseDetailRepository;
    @Autowired
    private HouseTagRepository houseTagRepository;
    @Autowired
    private TransportClient esClient;

    private ModelMapper modelMapper = new ModelMapper();


    @Override
    public void index(Long houseId) {
        Optional<House> optHouse = houseRepository.findById(houseId);
        if (!optHouse.isPresent()) {
            log.error("index house {} does not exist", houseId);
            return;
        }
        HouseDetail houseDetail = houseDetailRepository.findByHouseId(houseId);
        List<HouseTag> housetags = houseTagRepository.findAllByHouseId(houseId);
        HouseIndexTemplate houseIndexTemplate = new HouseIndexTemplate();
        modelMapper.map(optHouse.get(), houseIndexTemplate);
        modelMapper.map(houseDetail, houseIndexTemplate);
        houseIndexTemplate.setTags(housetags.stream().map(HouseTag::getName).collect(Collectors.toList()));

        SearchRequestBuilder searchBuilder = this.esClient.prepareSearch(INDEX_NAME).setTypes(INDEX_TYPE)
                .setQuery(QueryBuilders.termQuery(HouseIndexKey.HOUSE_ID, houseId));
        log.debug("method index search es conditional: {}", searchBuilder.toString());

        SearchResponse searchResponse = searchBuilder.get();
        long totalHits = searchResponse.getHits().totalHits;
        boolean success;
        if (totalHits == 0) {
            success = createIndex(houseIndexTemplate);
        } else if (totalHits == 1) {
            log.debug("update house index : {}", houseId);
            String esId = searchResponse.getHits().getAt(0).getId();
            success = updateIndex(esId, houseIndexTemplate);
        } else {
            log.warn("exist error~! recreate house index: {}", houseId);
            success = deleteAndCreateIndex(totalHits, houseIndexTemplate);
        }

        if (success) {
            log.debug("index success with house: {}", houseId);
        }
    }

    private boolean createIndex(HouseIndexTemplate houseIndexTemplate) {
        IndexResponse response = this.esClient.prepareIndex(INDEX_NAME, INDEX_TYPE) // 设置对应的索引名称和类型
                .setSource(JSONObject.toJSONString(houseIndexTemplate), XContentType.JSON) // 设置创建索引的json数据
                .get(); // 获取请求的返回值

        log.debug("create index with house: {}", houseIndexTemplate.getHouseId());
        return response.status() == RestStatus.CREATED;
    }

    private boolean updateIndex(String esId, HouseIndexTemplate template) {
        UpdateResponse updateResponse = this.esClient.prepareUpdate(INDEX_NAME, INDEX_TYPE, esId)
                .setDoc(JSONObject.toJSONString(template), XContentType.JSON)
                .get();
        log.debug("update index with house: {}", template.getHouseId());
        return updateResponse.status() == RestStatus.OK;
    }

    private boolean deleteAndCreateIndex(long totalHit, HouseIndexTemplate template) {
        DeleteByQueryRequestBuilder source = DeleteByQueryAction.INSTANCE
                .newRequestBuilder(this.esClient)
                // termQuery 精确查询
                .filter(QueryBuilders.termQuery(HouseIndexKey.HOUSE_ID, template.getHouseId()))
                .source(INDEX_NAME);// 设置需要删除的索引名称
        log.debug("delete index for house: {}", source);
        BulkByScrollResponse response = source.get();
        long deleted = response.getDeleted();
        if (deleted != totalHit) {
            log.warn("must delete {}, but {} document deleted!", totalHit, deleted);
            return false;
        }
        return createIndex(template);
    }



    @Override
    public void remove(Long houseId) {
        DeleteByQueryRequestBuilder source = DeleteByQueryAction.INSTANCE
                .newRequestBuilder(this.esClient)
                // termQuery 精确查询
                .filter(QueryBuilders.termQuery(HouseIndexKey.HOUSE_ID, houseId))
                .source(INDEX_NAME);// 设置需要删除的索引名称
        log.debug("delete index for house: {}", source);
        BulkByScrollResponse response = source.get();
    }
}
