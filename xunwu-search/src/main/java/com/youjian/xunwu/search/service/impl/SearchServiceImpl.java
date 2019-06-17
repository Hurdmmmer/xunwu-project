package com.youjian.xunwu.search.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.primitives.Longs;
import com.youjian.xunwu.comm.basic.ServiceMultiResult;
import com.youjian.xunwu.comm.entity.House;
import com.youjian.xunwu.comm.entity.HouseDetail;
import com.youjian.xunwu.comm.entity.HouseTag;
import com.youjian.xunwu.comm.entity.search.HouseIndexKey;
import com.youjian.xunwu.comm.entity.search.HouseIndexTemplate;
import com.youjian.xunwu.comm.entity.search.HouseSuggest;
import com.youjian.xunwu.comm.form.RentSearch;
import com.youjian.xunwu.comm.form.RentValueBlock;
import com.youjian.xunwu.comm.vo.ServiceResult;
import com.youjian.xunwu.dao.HouseDetailRepository;
import com.youjian.xunwu.dao.HouseRepository;
import com.youjian.xunwu.dao.HouseTagRepository;
import com.youjian.xunwu.search.service.ISearchService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeAction;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeRequestBuilder;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryAction;
import org.elasticsearch.index.reindex.DeleteByQueryRequestBuilder;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
    public void createOrUpdateIndex(Long houseId) {
        Optional<House> optHouse = houseRepository.findById(houseId);
        if (!optHouse.isPresent()) {
            log.error("createOrUpdateIndex house {} does not exist", houseId);
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
        log.debug("method createOrUpdateIndex search es conditional: {}", searchBuilder.toString());

        SearchResponse searchResponse = searchBuilder.get();
        long totalHits = searchResponse.getHits().totalHits;
        boolean success;
        if (totalHits == 0) {
            success = createIndex(houseIndexTemplate);
        } else if (totalHits == 1) {
            log.debug("update house createOrUpdateIndex : {}", houseId);
            String esId = searchResponse.getHits().getAt(0).getId();
            success = updateIndex(esId, houseIndexTemplate);
        } else {
            log.warn("exist error~! recreate house createOrUpdateIndex: {}", houseId);
            success = deleteAndCreateIndex(totalHits, houseIndexTemplate);
        }

        if (success) {
            log.debug("createOrUpdateIndex success with house: {}", houseId);
        }
    }

    /**
     * 创建索引
     */
    private boolean createIndex(HouseIndexTemplate houseIndexTemplate) {

        if (!updateSuggest(houseIndexTemplate)) {
            return false;
        }

        IndexResponse response = this.esClient.prepareIndex(INDEX_NAME, INDEX_TYPE) // 设置对应的索引名称和类型
                .setSource(JSONObject.toJSONString(houseIndexTemplate), XContentType.JSON) // 设置创建索引的json数据
                .get(); // 获取请求的返回值

        log.debug("create index with house: {}", houseIndexTemplate.getHouseId());
        return response.status() == RestStatus.CREATED;
    }

    /**
     * 更新索引
     */
    private boolean updateIndex(String esId, HouseIndexTemplate template) {

        if (!updateSuggest(template)) {
            return false;
        }

        UpdateResponse updateResponse = this.esClient.prepareUpdate(INDEX_NAME, INDEX_TYPE, esId)
                .setDoc(JSONObject.toJSONString(template), XContentType.JSON)
                .get();
        log.debug("update index with house: {}", template.getHouseId());
        return updateResponse.status() == RestStatus.OK;
    }

    /**
     * 删除并重新创建索引
     */
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
        long deleted = response.getDeleted();
        if (deleted <= 0) {
            throw new RuntimeException("index deletion failed, houseId: " + houseId);
        }
    }

    @Override
    public ServiceMultiResult<List<Long>> query(RentSearch rentSearch) {
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        // 过滤条件
        boolQuery.filter(QueryBuilders.termQuery(HouseIndexKey.CITY_EN_NAME, rentSearch.getCityEnName()));
        // * 表示所有区域
        if (rentSearch.getRegionEnName() != null && !"*".equalsIgnoreCase(rentSearch.getRegionEnName())) {
            boolQuery.filter(
                    QueryBuilders.termQuery(HouseIndexKey.REGION_EN_NAME, rentSearch.getRegionEnName())
            );
        }

        if (rentSearch.getKeywords() != null && rentSearch.getKeywords().trim().length() > 0) {

            // 调整某一个字段再 es 搜索中占用的权重
            boolQuery.should(QueryBuilders.matchQuery(HouseIndexKey.TITLE, rentSearch.getKeywords()));
            // 多条件组合
            boolQuery.should(QueryBuilders.multiMatchQuery(
                    rentSearch.getKeywords(),
                    HouseIndexKey.DISTRICT,
                    HouseIndexKey.TRAFFIC,
                    HouseIndexKey.SUBWAY_STATION_NAME,
                    HouseIndexKey.SUBWAY_LINE_NAME,
                    HouseIndexKey.ROUND_SERVICE
            ).minimumShouldMatch("75%"));
        }

        // 价格面积条件
        RentValueBlock area = RentValueBlock.matchArea(rentSearch.getAreaBlock());
        if (!RentValueBlock.ALL.equals(area)) {
            RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery(HouseIndexKey.AREA);
            if (area.getMax() > 0) {
                rangeQueryBuilder.lte(area.getMax());
            }
            if (area.getMin() > 0) {
                rangeQueryBuilder.gte(area.getMin());
            }
            boolQuery.filter(rangeQueryBuilder);
        }
        RentValueBlock price = RentValueBlock.matchPrice(rentSearch.getPriceBlock());
        // 价格
        if (!RentValueBlock.ALL.equals(price)) {
            RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery(HouseIndexKey.PRICE);
            if (price.getMax() > 0) {
                rangeQueryBuilder.lte(price.getMax());
            }
            if (price.getMin() > 0) {
                rangeQueryBuilder.gte(price.getMin());
            }
            boolQuery.filter(rangeQueryBuilder);
        }
        // 房屋方向
        if (rentSearch.getDirection() > 0) {
            TermQueryBuilder dir = QueryBuilders.termQuery(HouseIndexKey.DIRECTION, rentSearch.getDirection());
            boolQuery.filter(dir);
        }
        // 房屋租赁方式
        if (rentSearch.getRentWay() > -1) {
            boolQuery.filter(QueryBuilders.termQuery(HouseIndexKey.RENT_WAY, rentSearch.getRentWay()));
        }

        // 组合 es 查询条件
        SearchRequestBuilder requestBuilder = this.esClient.prepareSearch(INDEX_NAME)
                .setTypes(INDEX_TYPE)
                .setQuery(boolQuery)
                .addSort(
                        rentSearch.getOrderBy(), SortOrder.fromString(rentSearch.getOrderDirection())
                ).setFrom(rentSearch.getStart())
                .setSize(rentSearch.getSize())
                // 指定 es 返回的数据包含哪些字段, 排除哪些字段, null 默认排除除包含的字段外的所有字段
                .setFetchSource(HouseIndexKey.HOUSE_ID, null);


        log.debug(requestBuilder.toString());
        SearchResponse searchResponse = requestBuilder.get();
        if (searchResponse.status() != RestStatus.OK) {
            log.warn("Search status is not ok for: {}", requestBuilder);
            return new ServiceMultiResult<>(false, "访问elasticsearch异常", 0, 0, null);
        }
        SearchHits hits = searchResponse.getHits();
        List<Long> result = new ArrayList<>();
        for (SearchHit hit : hits) {
            Object houseId = hit.getSourceAsMap().get(HouseIndexKey.HOUSE_ID);
            result.add(Longs.tryParse(String.valueOf(houseId)));
        }

        return new ServiceMultiResult<>(true, null, rentSearch.getStart(), (int) hits.totalHits, result);
    }

    @Override
    public ServiceResult<List<String>> suggest(String prefix) {
        // 构建时传入的参数为 es 创建索引时, 建议字段名称
        CompletionSuggestionBuilder suggestion = SuggestBuilders.completionSuggestion("suggest")
                .prefix(prefix) // 建议的前缀
                .size(5); // 建议条数

        SuggestBuilder suggestBuilder = new SuggestBuilder();
        // 这里的名称随便起, 但是下面获取时要对应
        suggestBuilder.addSuggestion("autocomplete", suggestion);
        // suggest 需要使用 prepareSearch 进行查询
        SearchRequestBuilder suggest = esClient.prepareSearch(INDEX_NAME)
                .setTypes(INDEX_TYPE)
                .suggest(suggestBuilder);
        log.debug(suggestBuilder.toString());

        SearchResponse searchResponse = suggest.get();
        Suggest responseSuggest = searchResponse.getSuggest();
        if (responseSuggest == null) {
            return ServiceResult.ofSuccess(new ArrayList<>());
        }

        Suggest.Suggestion<? extends Suggest.Suggestion.Entry<? extends Suggest.Suggestion.Entry.Option>> autocomplete = responseSuggest.
                getSuggestion("autocomplete"); // 根据名称获取查询的建议, 名称跟上面对应
        Set<String> result = Sets.newHashSet();
        // 获取结果集
        for (Suggest.Suggestion.Entry<? extends Suggest.Suggestion.Entry.Option> entry : autocomplete.getEntries()) {
            if (!entry.getOptions().isEmpty()) {
                for (Suggest.Suggestion.Entry.Option option : entry.getOptions()) {
                    result.add(option.getText().string());
                }
            }
            // 限制返回 5 条建议
            if (result.size() > 5) {
                break;
            }
        }

        return ServiceResult.ofSuccess(Lists.newArrayList(result));
    }

    /**
     * update or create keyword suggest by {@link HouseIndexTemplate}
     */
    private boolean updateSuggest(HouseIndexTemplate template) {
        // 该对象会请求 es 分词接口
        AnalyzeRequestBuilder builder = new AnalyzeRequestBuilder(
                esClient, // es 客服端
                AnalyzeAction.INSTANCE, // 分词器实例
                INDEX_NAME,  // 索引名称
                template.getTitle(), template.getDescription(),  // 分词的字段
                template.getLayoutDesc(), template.getRoundService(),
                template.getSubwayLineName(), template.getSubwayStationName()
        );
        builder.setAnalyzer("ik_max_word"); // 使用 ik 分词器

        AnalyzeResponse analyzeTokens = builder.get();
        List<AnalyzeResponse.AnalyzeToken> tokens = analyzeTokens.getTokens();
        if (null == tokens) {
            log.warn("Can not analyze token for house: {}", template.getHouseId());
            return false;
        }

        List<HouseSuggest> suggests = tokens.stream()
                // 过滤 数字类型, 和分词结果长度为 2 的结果
                .filter(e -> !"<NUM>".equals(e.getType()) && e.getTerm().length() >= 2)
                .map(e -> {
                    HouseSuggest suggest = new HouseSuggest();
                    suggest.setInput(e.getTerm());
                    return suggest;
                }).collect(Collectors.toList());

        // 定制小区智能提示
        HouseSuggest suggest = new HouseSuggest();
        suggest.setInput(template.getDistrict());
        suggests.add(suggest);

        template.setSuggest(suggests);
        return true;
    }

    // 聚合查询时, es 段不能是 text 类型 需要是 keyword 类型,
    @Override
    public ServiceResult<Long> aggregateDistrictHouse(String cityEnName, String regionEnName, String district) {
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery()
                .filter(QueryBuilders.termQuery(HouseIndexKey.CITY_EN_NAME, cityEnName))
                .filter(QueryBuilders.termQuery(HouseIndexKey.REGION_EN_NAME, regionEnName))
                .filter(QueryBuilders.termQuery(HouseIndexKey.DISTRICT, district));


        SearchRequestBuilder requestBuilder = this.esClient.prepareSearch(INDEX_NAME)
                .setTypes(INDEX_TYPE)
                .setQuery(boolQuery)  // 设置查询条件
                .addAggregation(  // 再进行聚合
                        AggregationBuilders.terms(HouseIndexKey.AGG_DISTRICT) // 指定一个聚合名称
                                .field(HouseIndexKey.DISTRICT) // 指定聚合的字段

                ).setSize(0);// 我们不需要查询的数据, 只需要聚合的数据
        log.debug(requestBuilder.toString());

        SearchResponse searchResponse = requestBuilder.get();
        if (searchResponse.status() == RestStatus.OK) {

            Aggregations aggregations = searchResponse.getAggregations();
            Terms term = aggregations.get(HouseIndexKey.AGG_DISTRICT); // 根据聚合的名称获取聚合的结果集
            // 获取聚合结果
            if (term.getBuckets() != null && !term.getBuckets().isEmpty()) {
                // 根据聚合的字段, 获取聚合结果
                return ServiceResult.ofSuccess(term.getBucketByKey(district).getDocCount());
            }


        } else {
            log.warn("Failed to Aggregation for {}", HouseIndexKey.AGG_DISTRICT);
        }
        return ServiceResult.ofSuccess(0L);
    }
}
