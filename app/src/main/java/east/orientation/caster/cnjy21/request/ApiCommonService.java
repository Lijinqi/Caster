package east.orientation.caster.cnjy21.request;

import android.os.Environment;
import android.util.Log;

import com.vise.xsnow.http.ViseHttp;
import com.vise.xsnow.http.callback.ACallback;
import com.vise.xsnow.http.mode.CacheMode;
import com.vise.xsnow.http.mode.CacheResult;
import com.vise.xsnow.http.mode.DownProgress;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import east.orientation.caster.cnjy21.constant.APIConstant;
import east.orientation.caster.cnjy21.model.common.Book;
import east.orientation.caster.cnjy21.model.common.Chapter;
import east.orientation.caster.cnjy21.model.common.KnowledgePoint;
import east.orientation.caster.cnjy21.model.common.Province;
import east.orientation.caster.cnjy21.model.common.Subject;
import east.orientation.caster.cnjy21.model.common.Version;
import east.orientation.caster.cnjy21.model.document.Document;
import east.orientation.caster.cnjy21.model.document.Preview;
import east.orientation.caster.cnjy21.model.question.Question;
import east.orientation.caster.cnjy21.model.question.QuestionType;
import east.orientation.caster.cnjy21.util.SignatureHelper;
import east.orientation.caster.cnjy21.response.BaseResponse;
import east.orientation.caster.local.Common;

/**
 * Created by ljq on 2018/4/26.
 */

public class ApiCommonService  {
    public static int ERROR_NULL_CODE = 0x1001;
    public static String ERROR_NULL_MSG = "NullPointerException";

    /**
     * 学段 1:小学     2:中学    3:高中
     *
     * @param stage
     * @param callback
     */
    public static void getSubjects(int stage,ACallback<List<Subject>> callback){
        Map<String,String> params = new HashMap<>();
        params.put("stage",String.valueOf(stage));
        setSignParams(params);
        ViseHttp.GET(APIConstant.URL_SUBJECTS)
                .tag("getSubjects")
                .setLocalCache(true)
                .cacheMode(CacheMode.FIRST_REMOTE)
                .params(params)
                .request(new ACallback<CacheResult<BaseResponse<List<Subject>>>>() {
                    @Override
                    public void onSuccess(CacheResult<BaseResponse<List<Subject>>> response) {
                        if (response == null || response.getCacheData() == null) {
                            return;
                        }

                        if (response.isCache()){
                            if (response.getCacheData().getData() == null){
                                callback.onFail(ERROR_NULL_CODE, ERROR_NULL_MSG);
                            }else {
                                callback.onSuccess(response.getCacheData().getData());
                            }
                        }else {
                            if (response.getCacheData().getData() == null){
                                callback.onFail(ERROR_NULL_CODE, ERROR_NULL_MSG);
                            }else {
                                callback.onSuccess(response.getCacheData().getData());
                            }
                        }
                    }

                    @Override
                    public void onFail(int errCode, String errMsg) {
                        callback.onFail(errCode,errMsg);
                    }
                });
    }

    /**
     * 获取版本列表
     *
     * @param _stage
     *            学段ID 1:小学，2:初中，3:高中
     * @param _subjectId
     *            科目ID
     * @return
     */
    public static void getVersions(final Integer _stage, final Integer _subjectId,ACallback<List<Version>> callback) {
        Map<String,String> params = new HashMap<>();
        params.put("stage",String.valueOf(_stage));
        params.put("subjectId",String.valueOf(_subjectId));
        setSignParams(params);
        ViseHttp.GET(APIConstant.URL_VERSIONS)
                .tag("getVersions")
                .setLocalCache(true)
                .cacheMode(CacheMode.FIRST_REMOTE)
                .params(params)
                .request(new ACallback<CacheResult<BaseResponse<List<Version>>>>() {
                    @Override
                    public void onSuccess(CacheResult<BaseResponse<List<Version>>> response) {
                        if (response == null || response.getCacheData() == null) {
                            return;
                        }

                        if (response.isCache()){
                            if (response.getCacheData().getData() == null){
                                callback.onFail(ERROR_NULL_CODE, ERROR_NULL_MSG);
                            }else {
                                callback.onSuccess(response.getCacheData().getData());
                            }
                        }else {
                            if (response.getCacheData().getData() == null){
                                callback.onFail(ERROR_NULL_CODE, ERROR_NULL_MSG);
                            }else {
                                callback.onSuccess(response.getCacheData().getData());
                            }
                        }
                    }

                    @Override
                    public void onFail(int errCode, String errMsg) {
                        callback.onFail(errCode,errMsg);
                    }
                });
    }

    /**
     * 获取教材册别信息
     *
     * @param _versionId
     *            版本ID
     * @return
     */
    public static void getBooks(final Integer _versionId,ACallback<List<Book>> callback) {
        Map<String,String> params = new HashMap<>();
        params.put("versionId",String.valueOf(_versionId));
        setSignParams(params);
        ViseHttp.GET(APIConstant.URL_BOOKS)
                .tag("getBooks")
                .setLocalCache(true)
                .cacheMode(CacheMode.FIRST_REMOTE)
                .params(params)
                .request(new ACallback<CacheResult<BaseResponse<List<Book>>>>() {
                    @Override
                    public void onSuccess(CacheResult<BaseResponse<List<Book>>> response) {
                        if (response == null || response.getCacheData() == null) {
                            return;
                        }

                        if (response.isCache()){
                            if (response.getCacheData().getData() == null){
                                callback.onFail(ERROR_NULL_CODE, ERROR_NULL_MSG);
                            }else {
                                callback.onSuccess(response.getCacheData().getData());
                            }
                        }else {
                            if (response.getCacheData().getData() == null){
                                callback.onFail(ERROR_NULL_CODE, ERROR_NULL_MSG);
                            }else {
                                callback.onSuccess(response.getCacheData().getData());
                            }
                        }
                    }

                    @Override
                    public void onFail(int errCode, String errMsg) {
                        callback.onFail(errCode,errMsg);
                    }
                });
    }

    /**
     * 获取章节信息
     *
     * @param _bookId
     * @return
     */
    public static void getChapters(final Integer _bookId,ACallback<List<Chapter>> callback)  {
        Map<String,String> params = new HashMap<>();
        params.put("bookId",String.valueOf(_bookId));
        setSignParams(params);
        ViseHttp.GET(APIConstant.URL_CHAPTERS)
                .tag("getChapters")
                .setLocalCache(true)
                .cacheMode(CacheMode.FIRST_REMOTE)
                .params(params)
                .request(new ACallback<CacheResult<BaseResponse<List<Chapter>>>>() {
                    @Override
                    public void onSuccess(CacheResult<BaseResponse<List<Chapter>>> response) {
                        if (response == null || response.getCacheData() == null) {
                            return;
                        }

                        if (response.isCache()){
                            if (response.getCacheData().getData() == null){
                                callback.onFail(ERROR_NULL_CODE, ERROR_NULL_MSG);
                            }else {
                                callback.onSuccess(response.getCacheData().getData());
                            }
                        }else {
                            if (response.getCacheData().getData() == null){
                                callback.onFail(ERROR_NULL_CODE, ERROR_NULL_MSG);
                            }else {
                                callback.onSuccess(response.getCacheData().getData());
                            }
                        }
                    }

                    @Override
                    public void onFail(int errCode, String errMsg) {
                        callback.onFail(errCode,errMsg);
                    }
                });
    }

    /**
     * 获取知识点列表
     *
     * @param _stage
     *            学段ID 1:小学，2:初中，3:高中
     * @param _subjectId
     *            科目ID
     * @return
     */
    public static void getKnowledgePoints(final Integer _stage, final Integer _subjectId,ACallback<List<KnowledgePoint>> callback) {
        Map<String,String> params = new HashMap<>();
        params.put("stage",String.valueOf(_stage));
        params.put("subjectId",String.valueOf(_subjectId));
        setSignParams(params);
        ViseHttp.GET(APIConstant.URL_KNOWLEDGEPOINTS)
                .tag("getKnowledgePoints")
                .setLocalCache(true)
                .cacheMode(CacheMode.FIRST_REMOTE)
                .params(params)
                .request(new ACallback<CacheResult<BaseResponse<List<KnowledgePoint>>>>() {
                    @Override
                    public void onSuccess(CacheResult<BaseResponse<List<KnowledgePoint>>> response) {
                        if (response == null || response.getCacheData() == null) {
                            return;
                        }

                        if (response.isCache()){
                            if (response.getCacheData().getData() == null){
                                callback.onFail(ERROR_NULL_CODE, ERROR_NULL_MSG);
                            }else {
                                callback.onSuccess(response.getCacheData().getData());
                            }
                        }else {
                            if (response.getCacheData().getData() == null){
                                callback.onFail(ERROR_NULL_CODE, ERROR_NULL_MSG);
                            }else {
                                callback.onSuccess(response.getCacheData().getData());
                            }
                        }
                    }

                    @Override
                    public void onFail(int errCode, String errMsg) {
                        callback.onFail(errCode,errMsg);
                    }
                });
    }

    /**
     * 获取省份地区信息
     *
     * @return
     */
    public static void getProvinces(ACallback<List<Province>> callback) {
        ViseHttp.GET(APIConstant.URL_PROVINCES)
                .tag("getProvinces")
                .setLocalCache(true)
                .cacheMode(CacheMode.FIRST_REMOTE)
                .request(new ACallback<CacheResult<BaseResponse<List<Province>>>>() {
                    @Override
                    public void onSuccess(CacheResult<BaseResponse<List<Province>>> response) {
                        if (response == null || response.getCacheData() == null) {
                            return;
                        }

                        if (response.isCache()){
                            if (response.getCacheData().getData() == null){
                                callback.onFail(ERROR_NULL_CODE, ERROR_NULL_MSG);
                            }else {
                                callback.onSuccess(response.getCacheData().getData());
                            }
                        }else {
                            if (response.getCacheData().getData() == null){
                                callback.onFail(ERROR_NULL_CODE, ERROR_NULL_MSG);
                            }else {
                                callback.onSuccess(response.getCacheData().getData());
                            }
                        }
                    }

                    @Override
                    public void onFail(int errCode, String errMsg) {
                        callback.onFail(errCode,errMsg);
                    }
                });
    }

    /**
     * 获取多个资源下载地址，下载地址半小时有效，半小时后需重新获取
     *
     * @param documents
     *            需要获取下载地址的文档
     */
    public static void getDocumentsDownurls(List<Document> documents , ACallback<List<Document>> callback) {
        if (documents == null || documents.size() == 0) {
            return;
        }
        StringBuffer itemIds = new StringBuffer();
        Map<Long, Document> docMap = new HashMap<Long, Document>(documents.size());
        for (Document doc : documents) {
            itemIds.append(doc.getItemId()).append(",");
            docMap.put(doc.getItemId(), doc);
        }
        itemIds.deleteCharAt(itemIds.length() - 1);
        Map<String,String> params = new HashMap<>();
        params.put("itemIds",itemIds.toString());
        setSignParams(params);
        ViseHttp.GET(APIConstant.URL_DOCUMENT_DETAIL)
                .tag("getDocumentsDownurls")
                .setLocalCache(true)
                .cacheMode(CacheMode.FIRST_REMOTE)
                .params(params)
                .request(new ACallback<CacheResult<BaseResponse<List<Document>>>>() {
                    @Override
                    public void onSuccess(CacheResult<BaseResponse<List<Document>>> response) {
                        if (response == null || response.getCacheData() == null) {
                            return;
                        }

                        if (response.isCache()){
                            if (response.getCacheData().getData() == null){
                                callback.onFail(ERROR_NULL_CODE, ERROR_NULL_MSG);
                            }else {
                                callback.onSuccess(response.getCacheData().getData());
                            }
                        }else {
                            if (response.getCacheData().getData() == null){
                                callback.onFail(ERROR_NULL_CODE, ERROR_NULL_MSG);
                            }else {
                                callback.onSuccess(response.getCacheData().getData());
                            }
                        }
                    }

                    @Override
                    public void onFail(int errCode, String errMsg) {
                        callback.onFail(errCode,errMsg);
                    }
                });
    }

    /**
     * 获取单个资源文档下载地址，下载地址半小时有效，半小时后需重新获取
     *
     * @param itemid
     * @return
     */
    public static void getDocumentDownurl(long itemid,ACallback<Document> callback) {
        Map<String,String> params = new HashMap<>();
        params.put("itemIds",String.valueOf(itemid));
        setSignParams(params);
        ViseHttp.GET(APIConstant.URL_DOCUMENT_DETAIL)
                .tag("getDocumentDownurl")
                .setLocalCache(true)
                .cacheMode(CacheMode.FIRST_REMOTE)
                .params(params)
                .request(new ACallback<CacheResult<BaseResponse<List<Document>>>>() {
                    @Override
                    public void onSuccess(CacheResult<BaseResponse<List<Document>>> response) {
                        if (response == null || response.getCacheData() == null) {
                            return;
                        }

                        if (response.isCache()){
                            if (response.getCacheData().getData() == null){
                                callback.onFail(ERROR_NULL_CODE, ERROR_NULL_MSG);
                            }else {
                                callback.onSuccess(response.getCacheData().getData().get(0));
                            }
                        }else {
                            if (response.getCacheData().getData() == null){
                                callback.onFail(ERROR_NULL_CODE, ERROR_NULL_MSG);
                            }else {
                                callback.onSuccess(response.getCacheData().getData().get(0));
                            }
                        }
                    }

                    @Override
                    public void onFail(int errCode, String errMsg) {
                        callback.onFail(errCode,errMsg);
                    }
                });
    }

    /**
     * 通过章节获取资源列表
     *
     * @param stage
     * @param subjectId
     * @param chapterId
     * @param page
     * @param perPage
     * @param callback
     */
    public static void getDocuments(int stage,int subjectId,String chapterId, int page ,int perPage,ACallback<List<Document>> callback) {
        Map<String,String> params = new HashMap<>();
        params.put("stage",String.valueOf(stage));
        params.put("subjectId",String.valueOf(subjectId));
        params.put("chapterId",chapterId);
        //params.put("knowledgeId",knowledgeId);
        params.put("page",String.valueOf(page));
        params.put("perPage",String.valueOf(perPage));
        setSignParams(params);
        ViseHttp.GET(APIConstant.URL_DOCUMENTS)
                .tag("getDocuments")
                .setLocalCache(true)
                .cacheMode(CacheMode.FIRST_REMOTE)
                .params(params)
                .request(new ACallback<CacheResult<BaseResponse<List<Document>>>>() {
                    @Override
                    public void onSuccess(CacheResult<BaseResponse<List<Document>>> response) {
                        if (response == null || response.getCacheData() == null) {
                            return;
                        }

                        if (response.isCache()){
                            if (response.getCacheData().getData() == null){
                                callback.onFail(ERROR_NULL_CODE, ERROR_NULL_MSG);
                            }else {
                                callback.onSuccess(response.getCacheData().getData());
                            }
                        }else {
                            if (response.getCacheData().getData() == null){
                                callback.onFail(ERROR_NULL_CODE, ERROR_NULL_MSG);
                            }else {
                                callback.onSuccess(response.getCacheData().getData());
                            }
                        }
                    }

                    @Override
                    public void onFail(int errCode, String errMsg) {
                        callback.onFail(errCode,errMsg);
                    }
                });
    }

    /**
     * 资源预览
     *
     * @param _itemId
     * @return
     */
    public static void getPreview(Long _itemId,ACallback<List<Preview>> callback) {
        Map<String,String> params = new HashMap<>();
        params.put("itemId",String.valueOf(_itemId));
        setSignParams(params);
        ViseHttp.GET(APIConstant.URL_DOCUMENT_PREVIEW)
                .tag("getPreviews")
                .setLocalCache(true)
                .cacheMode(CacheMode.FIRST_REMOTE)
                .params(params)
                .request(new ACallback<CacheResult<BaseResponse<List<Preview>>>>() {
                    @Override
                    public void onSuccess(CacheResult<BaseResponse<List<Preview>>> response) {
                        if (response == null || response.getCacheData() == null) {
                            return;
                        }

                        if (response.isCache()){
                            if (response.getCacheData().getData() == null){
                                callback.onFail(ERROR_NULL_CODE, ERROR_NULL_MSG);
                            }else {
                                callback.onSuccess(response.getCacheData().getData());
                            }
                        }else {
                            if (response.getCacheData().getData() == null){
                                callback.onFail(ERROR_NULL_CODE, ERROR_NULL_MSG);
                            }else {
                                callback.onSuccess(response.getCacheData().getData());
                            }
                        }
                    }

                    @Override
                    public void onFail(int errCode, String errMsg) {
                        callback.onFail(errCode,errMsg);
                    }
                });
    }

    /**
     * 根据获取试题列表
     *
     * @param stage	Number
     学段 (1:小学，2:初中，3:高中)

     * @param subjectId	Number
     科目ID

     * @param //type	Number
     试卷类型 (通过试卷类型接口获取)

     * @param //versionId	Number
     教材版本ID

     * @param //province	Number
     试卷所属省份(可通过公共接口 "获取省级地区数据" 获取)

     * @param //bookId	Number
     教材册别ID

     * @param //categoryId	Number
     同步章节ID

     * @param //year	Number
     试卷所属年份 (平台提供试卷资源自 2009 年开始)

     * @param //title	String
     搜索试卷标题关键词，关键词匹配的结果会在题干 title 中 使用 "<keyword> 关键词<\keyword>" 包裹，客户端可以直接操作该标签处理样式

     取值范围: 最大长度 255

     * @param page	Number
     分页页码

     * @param perPage	Number
     指定分页列表每页显示数据条数
     *
     * @return
     */
    public static void getQuestions(int stage,int subjectId,int page,int perPage,ACallback<List<Question>> callback) {
        Map<String,String> params = new HashMap<>();
        params.put("stage",String.valueOf(stage));
        params.put("subjectId",String.valueOf(subjectId));
        params.put("page",String.valueOf(page));
        params.put("perPage",String.valueOf(perPage));
        setSignParams(params);
        ViseHttp.GET(APIConstant.URL_QUESTIONS)
                .tag("getQuestions")
                .setLocalCache(true)
                .cacheMode(CacheMode.FIRST_REMOTE)
                .params(params)
                .request(new ACallback<CacheResult<BaseResponse<List<Question>>>>() {
                    @Override
                    public void onSuccess(CacheResult<BaseResponse<List<Question>>> response) {
                        if (response == null || response.getCacheData() == null) {
                            return;
                        }

                        if (response.isCache()){
                            if (response.getCacheData().getData() == null){
                                callback.onFail(ERROR_NULL_CODE, ERROR_NULL_MSG);
                            }else {
                                callback.onSuccess(response.getCacheData().getData());
                            }
                        }else {
                            if (response.getCacheData().getData() == null){
                                callback.onFail(ERROR_NULL_CODE, ERROR_NULL_MSG);
                            }else {
                                callback.onSuccess(response.getCacheData().getData());
                            }
                        }
                    }

                    @Override
                    public void onFail(int errCode, String errMsg) {
                        callback.onFail(errCode,errMsg);
                    }
                });
    }

    /**
     * 获取试题类型列表
     *
     * @param _stage
     *            学段ID APIConstant.STAGE_
     * @param _subjectId
     *            学科ID
     * @return
     */
    public static void getQuestionTypes(final int _stage, final int _subjectId,ACallback<List<QuestionType>> callback) {
        Map<String,String> params = new HashMap<>();
        params.put("stage",String.valueOf(_stage));
        params.put("subjectId",String.valueOf(_subjectId));
        setSignParams(params);
        ViseHttp.GET(APIConstant.URL_QUESTION_TYPES)
                .tag("getQuestionTypes")
                .setLocalCache(true)
                .cacheMode(CacheMode.FIRST_REMOTE)
                .params(params)
                .request(new ACallback<CacheResult<BaseResponse<List<QuestionType>>>>() {
                    @Override
                    public void onSuccess(CacheResult<BaseResponse<List<QuestionType>>> response) {
                        if (response == null || response.getCacheData() == null) {
                            return;
                        }

                        if (response.isCache()){
                            if (response.getCacheData().getData() == null){
                                callback.onFail(ERROR_NULL_CODE, ERROR_NULL_MSG);
                            }else {
                                callback.onSuccess(response.getCacheData().getData());
                            }
                        }else {
                            if (response.getCacheData().getData() == null){
                                callback.onFail(ERROR_NULL_CODE, ERROR_NULL_MSG);
                            }else {
                                callback.onSuccess(response.getCacheData().getData());
                            }
                        }
                    }

                    @Override
                    public void onFail(int errCode, String errMsg) {
                        callback.onFail(errCode,errMsg);
                    }
                });
    }

    /**
     * 获取试题答案和解析
     *
     * @param questions
     *            试题列表
     * @return
     */
    public static void getQuestionsAnswer(List<Question> questions,ACallback<List<Question>> callback) {
        if (questions.isEmpty()) {
            return;
        }
        final StringBuffer _questionIds = new StringBuffer();
        for (Question question : questions) {
            if (question.getQuestionId() == null) {
                throw new RuntimeException("question ID is null.");
            }
            _questionIds.append(question.getQuestionId()).append(",");
        }

        Map<String,String> params = new HashMap<>();
        params.put("questionIds",_questionIds.toString());
        setSignParams(params);
        ViseHttp.GET(APIConstant.URL_QUESTION_DETAIL)
                .tag("getQuestionsAnswer")
                .setLocalCache(true)
                .cacheMode(CacheMode.FIRST_REMOTE)
                .params(params)
                .request(new ACallback<CacheResult<BaseResponse<List<Question>>>>() {
                    @Override
                    public void onSuccess(CacheResult<BaseResponse<List<Question>>> response) {
                        if (response == null || response.getCacheData() == null) {
                            return;
                        }

                        if (response.isCache()){
                            if (response.getCacheData().getData() == null){
                                callback.onFail(ERROR_NULL_CODE, ERROR_NULL_MSG);
                            }else {
                                callback.onSuccess(response.getCacheData().getData());
                            }
                        }else {
                            if (response.getCacheData().getData() == null){
                                callback.onFail(ERROR_NULL_CODE, ERROR_NULL_MSG);
                            }else {
                                callback.onSuccess(response.getCacheData().getData());
                            }
                        }
                    }

                    @Override
                    public void onFail(int errCode, String errMsg) {
                        callback.onFail(errCode,errMsg);
                    }
                });
    }

    /**
     * 获取试题答案和解析
     *
     * @param question
     *            试题对象
     * @return
     */
    public static void getQuestionAnswer(Question question,ACallback<Question> callback) {
        if (question == null)
            return;
        Map<String,String> params = new HashMap<>();
        params.put("questionId",String.valueOf(question.getQuestionId()));
        setSignParams(params);
        ViseHttp.GET(APIConstant.URL_QUESTION_DETAIL)
                .tag("getQuestionAnswer")
                .setLocalCache(true)
                .cacheMode(CacheMode.FIRST_REMOTE)
                .params(params)
                .request(new ACallback<CacheResult<BaseResponse<List<Question>>>>() {
                    @Override
                    public void onSuccess(CacheResult<BaseResponse<List<Question>>> response) {
                        if (response == null || response.getCacheData() == null) {
                            return;
                        }
                        
                        if (response.isCache()){
                            if (response.getCacheData().getData() == null){
                                callback.onFail(ERROR_NULL_CODE, ERROR_NULL_MSG);
                            }else {
                                callback.onSuccess(response.getCacheData().getData().get(0));
                            }
                        }else {
                            if (response.getCacheData().getData() == null){
                                callback.onFail(ERROR_NULL_CODE, ERROR_NULL_MSG);
                            }else {
                                callback.onSuccess(response.getCacheData().getData().get(0));
                            }
                        }
                    }

                    @Override
                    public void onFail(int errCode, String errMsg) {
                        callback.onFail(errCode,errMsg);
                    }
                });

    }

    /**
     * 下载资源
     *
     * @param url
     * @param fileName
     * @param callback
     */
    public static void downLoad(String url,String fileName,ACallback<DownProgress> callback){
        File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        ViseHttp.DOWNLOAD(url)
                .setFileName(fileName)//文件名
                .setDirName(Common.SAVE_DIR_NAME)// 文件夹
                .setRootName(file.getAbsolutePath())// 根目录
                .request(new ACallback<DownProgress>() {
                    @Override
                    public void onSuccess(DownProgress response) {
                        callback.onSuccess(response);
                    }

                    @Override
                    public void onFail(int errCode, String errMsg) {
                        callback.onFail(errCode,errMsg);
                    }
                });
    }

    /**
     * 设置签名参数
     *
     * @param params
     */
    private static void setSignParams(Map<String, String> params) {
        params.put(APIConstant.PARAM_ACCESS_KEY, APIConstant.ACCESS_KEY);
        params.put(APIConstant.PARAM_SALT, String.valueOf(Math.random()));
        params.put(APIConstant.PARAM_TIMESTAMP, String.valueOf(System.currentTimeMillis() / 1000));
        params.put(APIConstant.PARAM_SIGN, SignatureHelper.generator(params, APIConstant.ACCESS_SECRET));
    }
}
