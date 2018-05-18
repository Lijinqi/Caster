package east.orientation.caster.cnjy21.model.question;

import java.util.List;
import java.util.Map;

import east.orientation.caster.cnjy21.model.common.KnowledgePoint;


/**
 * 试题信息
 * 
 * @author zhoushubing
 *
 */
public class Question {
	/**
	 * 试题编号
	 */
	private Long questionId;

	/**
	 * 试题题干
	 */
	private String stem;

	/**
	 * 题目可选项(适用于 单选题 和 多选题)
	 */
	private List<Map<String, Object>> options;

	/**
	 * 父题ID，默认为0
	 */
	private Integer parentId;

	/**
	 * 题目题型
	 */
	private Integer type;

	/**
	 * 题目题型名称
	 */
	private String typeName;

	/**
	 * 题目基础题型，基础题型是固定不变的 说明：[ 1 = '单选题', 2 = '多选题', 3 = '判断题', 4 = '填空题', 5 =
	 * '解答题', 6 = '完形填空题', 7 = '复合题' ]
	 */
	private Integer baseType;

	/**
	 * 题目基础题型名称
	 */
	private String baseTypeName;

	/**
	 * 所属学段
	 */
	private Integer stage;

	/**
	 * 所属学科ID
	 */
	private Integer subjectId;

	/**
	 * 题目难度值 说明：[1 = '容易', 2 = '较易', 3 = '普通', 4 = '较难', 5 = '困难']
	 */
	private Integer difficult;

	/**
	 * 难度名称
	 */
	private String difficultName;

	/**
	 * 题类 说明：[1 = '真题', 2 = '常考题', 3 = '易错题', 4 = '好题', 5 = '压轴题', 6 = '听力题', 7
	 * = '模拟题']
	 */
	private Integer examType;

	/**
	 * 题类名称
	 */
	private String examTypeName;

	/**
	 * 子题数据集只有复合题题型的题目才会有值子题数据对象的值和其他题型的数据完全一致
	 */
	private List<Question> subsets;

	/**
	 * 知识点数据序列，当前题目包含的知识点数据
	 */
	private List<KnowledgePoint> knowledge;

	/**
	 * 正确答案
	 */
	private String answer;

	/**
	 * 答案JSON数组格式，这个格式是所有题目都遵循方便程序对客观题的处理，比如选择题的答案为D 对应 answerJson
	 * 的第四个元素，判断题也是同样的原理
	 */
	private List<Object> answerJson;

	/**
	 * 答案分析详解
	 */
	private String explanation;
	
	/**
	 * 年级
	 */
	private Integer grade;

	public Integer getDifficult() {
		return difficult;
	}

	public void setDifficult(Integer difficult) {
		this.difficult = difficult;
	}

	public String getStem() {
		return stem;
	}

	public void setStem(String stem) {
		this.stem = stem;
	}

	/**
	 * 答案 : 需要异步加载
	 * 
	 * @return
	 */
	public String getAnswer() {
		return answer;
	}

	public void setAnswer(String answer) {
		this.answer = answer;
	}

	public Long getQuestionId() {
		return questionId;
	}

	public void setQuestionId(Long questionId) {
		this.questionId = questionId;
	}

	public List<Map<String, Object>> getOptions() {
		return options;
	}

	public void setOptions(List<Map<String, Object>> options) {
		this.options = options;
	}

	public Integer getParentId() {
		return parentId;
	}

	public void setParentId(Integer parentId) {
		this.parentId = parentId;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public Integer getBaseType() {
		return baseType;
	}

	public void setBaseType(Integer baseType) {
		this.baseType = baseType;
	}

	public String getBaseTypeName() {
		return baseTypeName;
	}

	public void setBaseTypeName(String baseTypeName) {
		this.baseTypeName = baseTypeName;
	}

	public Integer getStage() {
		return stage;
	}

	public void setStage(Integer stage) {
		this.stage = stage;
	}

	public Integer getSubjectId() {
		return subjectId;
	}

	public void setSubjectId(Integer subjectId) {
		this.subjectId = subjectId;
	}

	public String getDifficultName() {
		return difficultName;
	}

	public void setDifficultName(String difficultName) {
		this.difficultName = difficultName;
	}

	public Integer getExamType() {
		return examType;
	}

	public void setExamType(Integer examType) {
		this.examType = examType;
	}

	public String getExamTypeName() {
		return examTypeName;
	}

	public void setExamTypeName(String examTypeName) {
		this.examTypeName = examTypeName;
	}

	public List<Question> getSubsets() {
		return subsets;
	}

	public void setSubsets(List<Question> subsets) {
		this.subsets = subsets;
	}

	public List<KnowledgePoint> getKnowledge() {
		return knowledge;
	}

	public void setKnowledge(List<KnowledgePoint> knowledge) {
		this.knowledge = knowledge;
	}

	public List<Object> getAnswerJson() {
		return answerJson;
	}

	public void setAnswerJson(List<Object> answerJson) {
		this.answerJson = answerJson;
	}

	public String getExplanation() {
		return explanation;
	}

	public void setExplanation(String explanation) {
		this.explanation = explanation;
	}

	public Integer getGrade() {
		return grade;
	}

	public void setGrade(Integer grade) {
		this.grade = grade;
	}
	
	

}
