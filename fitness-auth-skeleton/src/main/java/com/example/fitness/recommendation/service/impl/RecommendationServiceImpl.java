package com.example.fitness.recommendation.service.impl;

import com.example.fitness.common.exception.BusinessException;
import com.example.fitness.common.model.PageResponse;
import com.example.fitness.common.utils.SecurityUtils;
import com.example.fitness.diet.mapper.UserDietRecordMapper;
import com.example.fitness.exercise.entity.FitExercise;
import com.example.fitness.food.entity.FitFood;
import com.example.fitness.plan.vo.PlanVO;
import com.example.fitness.record.entity.UserDietRecord;
import com.example.fitness.record.entity.UserWorkoutRecord;
import com.example.fitness.recommendation.dto.RecommendationGenerateRequest;
import com.example.fitness.recommendation.dto.RecommendationHistoryQuery;
import com.example.fitness.recommendation.entity.RecommendationRecord;
import com.example.fitness.recommendation.mapper.RecommendationRecordMapper;
import com.example.fitness.recommendation.service.RecommendationService;
import com.example.fitness.recommendation.vo.LatestRecommendationVO;
import com.example.fitness.recommendation.vo.RecommendationGenerateVO;
import com.example.fitness.recommendation.vo.RecommendationVO;
import com.example.fitness.system.entity.SysUser;
import com.example.fitness.system.mapper.SysUserMapper;
import com.example.fitness.workout.mapper.UserWorkoutRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 智能推荐服务实现类
 * <p>基于 ItemCF（物品协同过滤）算法和规则评分相结合的推荐引擎，
 * 支持运动、饮食和训练计划三种类型的推荐。</p>
 * <p>核心逻辑：
 * <ul>
 *   <li>ItemCF - 通过用户-物品行为矩阵计算物品间的余弦相似度，
 *       为用户推荐与历史行为相似但尚未接触的物品；</li>
 *   <li>规则评分 - 根据用户的健身目标、活动水平、饮食偏好等属性，
 *       对候选物品进行多维度加权打分。</li>
 * </ul>
 * <p>优先使用 ItemCF 结果，不足时由规则评分补充。</p>
 */
@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    // ==================== 推荐类型常量 ====================
    /** 运动推荐类型标识 */
    private static final String REC_TYPE_EXERCISE = "EXERCISE";
    /** 饮食推荐类型标识 */
    private static final String REC_TYPE_FOOD = "FOOD";
    /** 训练计划推荐类型标识 */
    private static final String REC_TYPE_PLAN = "PLAN";

    // ==================== 算法类型常量 ====================
    /** 基于规则的推荐算法标识 */
    private static final String ALGORITHM_TYPE_RULE_BASED = "RULE_BASED";
    /** 基于物品协同过滤的推荐算法标识 */
    private static final String ALGORITHM_TYPE_ITEM_CF = "ITEM_CF";

    // ==================== 推荐数量上限 ====================
    /** 单次生成运动推荐的最大数量 */
    private static final int EXERCISE_RECOMMEND_LIMIT = 3;
    /** 单次生成饮食推荐的最大数量 */
    private static final int FOOD_RECOMMEND_LIMIT = 3;
    /** 单次生成计划推荐的最大数量 */
    private static final int PLAN_RECOMMEND_LIMIT = 1;

    // ==================== 注入的 Mapper 依赖 ====================
    /** 推荐记录 Mapper，负责推荐结果的持久化及复杂查询 */
    private final RecommendationRecordMapper recommendationRecordMapper;
    /** 系统用户 Mapper，用于查询用户信息 */
    private final SysUserMapper sysUserMapper;
    /** 运动记录 Mapper，用于获取用户历史运动数据 */
    private final UserWorkoutRecordMapper userWorkoutRecordMapper;
    /** 饮食记录 Mapper，用于获取用户历史饮食数据 */
    private final UserDietRecordMapper userDietRecordMapper;

    /**
     * 生成推荐结果。
     * <p>流程：
     * <ol>
     *   <li>校验当前用户状态；</li>
     *   <li>按推荐类型依次生成候选推荐（优先 ItemCF，不足时规则补充）；</li>
     *   <li>对候选列表去重（同一 recType + targetId 只保留一条）；</li>
     *   <li>逐条写入数据库并返回完整推荐详情。</li>
     * </ol>
     *
     * @param request 包含需要生成的推荐类型列表（EXERCISE / FOOD / PLAN）
     * @return 生成结果，包含生成数量和推荐详情列表
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public RecommendationGenerateVO generate(RecommendationGenerateRequest request) {
        // 校验当前登录用户是否存在且状态正常
        Long currentUserId = SecurityUtils.getCurrentUserId();
        SysUser user = checkCurrentUserUsable(currentUserId);

        // 校验并规范化推荐类型列表，去重且统一转大写
        List<String> recommendTypes = normalizeRecommendTypes(request.getRecommendTypes());
        if (recommendTypes.isEmpty()) {
            throw new BusinessException(400, "recommendTypes cannot be empty");
        }

        List<RecommendationRecord> toInsert = new ArrayList<>();

        // --- 运动推荐：ItemCF 优先，规则评分补充不足部分 ---
        if (recommendTypes.contains(REC_TYPE_EXERCISE)) {
            List<RecommendationRecord> itemCfRecords = generateExerciseByItemCF(currentUserId, EXERCISE_RECOMMEND_LIMIT);
            toInsert.addAll(itemCfRecords);
            if (itemCfRecords.size() < EXERCISE_RECOMMEND_LIMIT) {
                toInsert.addAll(buildExerciseRecommendations(user, EXERCISE_RECOMMEND_LIMIT - itemCfRecords.size(), targetIds(itemCfRecords)));
            }
        }

        // --- 饮食推荐：ItemCF 优先，规则评分补充不足部分 ---
        if (recommendTypes.contains(REC_TYPE_FOOD)) {
            List<RecommendationRecord> itemCfRecords = generateFoodByItemCF(currentUserId, FOOD_RECOMMEND_LIMIT);
            toInsert.addAll(itemCfRecords);
            if (itemCfRecords.size() < FOOD_RECOMMEND_LIMIT) {
                toInsert.addAll(buildFoodRecommendations(user, FOOD_RECOMMEND_LIMIT - itemCfRecords.size(), targetIds(itemCfRecords)));
            }
        }

        // --- 计划推荐：直接按规则生成 ---
        if (recommendTypes.contains(REC_TYPE_PLAN)) {
            toInsert.addAll(buildPlanRecommendations(currentUserId));
        }

        // 按 recType:targetId 去重，避免重复推荐同一物品
        toInsert = deduplicateRecommendations(toInsert);

        // 逐条持久化并查询完整详情
        List<RecommendationVO> list = new ArrayList<>();
        for (RecommendationRecord record : toInsert) {
            record.setUserId(currentUserId);
            int inserted = recommendationRecordMapper.insert(record);
            if (inserted <= 0 || record.getId() == null) {
                throw new BusinessException(500, "generate recommendation failed");
            }
            RecommendationVO detail = recommendationRecordMapper.selectDetailByIdAndUserId(record.getId(), currentUserId);
            if (detail != null) {
                list.add(detail);
            }
        }

        // 组装返回结果
        RecommendationGenerateVO response = new RecommendationGenerateVO();
        response.setGeneratedCount(list.size());
        response.setList(list);
        return response;
    }

    /**
     * 查询当前用户最近一次推荐结果。
     * <p>先查该类型下最新推荐时间，再返回该时间点的所有推荐记录。</p>
     *
     * @param recType 推荐类型（EXERCISE / FOOD / PLAN）
     * @return 最近推荐时间及对应推荐列表
     */
    @Override
    public LatestRecommendationVO latest(String recType) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        String normalizedType = normalizeRecType(recType, false);

        // 查询该用户该类型下最新一条推荐的生成时间
        LocalDateTime latestTime = recommendationRecordMapper.selectLatestTime(currentUserId, normalizedType);

        // 如果存在历史推荐，则查询该时间点的全部推荐记录
        List<RecommendationVO> list = latestTime == null
                ? List.of()
                : recommendationRecordMapper.selectLatestList(currentUserId, normalizedType, latestTime);

        LatestRecommendationVO response = new LatestRecommendationVO();
        response.setLatestTime(latestTime);
        response.setList(list);
        return response;
    }

    /**
     * 分页查询当前用户的推荐历史记录。
     *
     * @param query 查询条件（包含 recType、pageNum、pageSize 等）
     * @return 分页结果
     */
    @Override
    public PageResponse<RecommendationVO> history(RecommendationHistoryQuery query) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (query.getRecType() != null) {
            query.setRecType(normalizeRecType(query.getRecType(), false));
        }

        // 先查总数，总数为 0 时跳过列表查询以提升性能
        long total = recommendationRecordMapper.countUserPage(currentUserId, query);
        List<RecommendationVO> list = total == 0
                ? List.of()
                : recommendationRecordMapper.selectUserPage(currentUserId, query);
        return PageResponse.of(list, total, query.getPageNum(), query.getPageSize());
    }

    /**
     * 查询单条推荐详情。
     *
     * @param id 推荐记录 ID
     * @return 推荐详情
     * @throws BusinessException 记录不存在时抛出 404
     */
    @Override
    public RecommendationVO detail(Long id) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        RecommendationVO detail = recommendationRecordMapper.selectDetailByIdAndUserId(id, currentUserId);
        if (detail == null) {
            throw new BusinessException(404, "recommendation not found");
        }
        return detail;
    }

    // ==================== 用户校验 ====================

    /**
     * 校验当前用户是否存在且状态正常（status=1）。
     *
     * @param userId 用户 ID
     * @return 用户实体
     * @throws BusinessException 用户不存在抛 404，已禁用抛 403
     */
    private SysUser checkCurrentUserUsable(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "user not found");
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException(403, "user is disabled");
        }
        return user;
    }

    // ==================== 推荐类型规范化 ====================

    /**
     * 将前端传入的推荐类型列表规范化：去除空白、转大写、去重。
     *
     * @param rawTypes 原始类型列表
     * @return 去重后的规范化类型列表
     */
    private List<String> normalizeRecommendTypes(List<String> rawTypes) {
        Set<String> normalized = new LinkedHashSet<>();
        if (rawTypes != null) {
            for (String rawType : rawTypes) {
                String recType = normalizeRecType(rawType, true);
                if (recType != null) {
                    normalized.add(recType);
                }
            }
        }
        return new ArrayList<>(normalized);
    }

    /**
     * 规范化单个推荐类型字符串。
     *
     * @param rawType  原始类型字符串
     * @param strict   为 true 时，非法值抛出严格校验异常（用于生成接口）；
     *                 为 false 时，非法值抛出通用校验异常（用于查询接口）
     * @return 规范化后的类型字符串，null 表示输入为空或空白
     */
    private String normalizeRecType(String rawType, boolean strict) {
        if (rawType == null || rawType.isBlank()) {
            return null;
        }
        String recType = rawType.trim().toUpperCase(Locale.ROOT);
        if (REC_TYPE_EXERCISE.equals(recType) || REC_TYPE_FOOD.equals(recType) || REC_TYPE_PLAN.equals(recType)) {
            return recType;
        }
        if (strict) {
            throw new BusinessException(400, "recommend type must be EXERCISE, FOOD or PLAN");
        }
        throw new BusinessException(400, "recType must be EXERCISE, FOOD or PLAN");
    }

    // ==================== ItemCF 运动推荐 ====================

    /**
     * 基于物品协同过滤（ItemCF）生成运动推荐。
     * <p>算法步骤：
     * <ol>
     *   <li>获取所有上架运动作为候选集；</li>
     *   <li>构建全体用户-运动行为矩阵（权重由完成度、反馈评分、时长等综合计算）；</li>
     *   <li>对每个候选运动，计算其与用户历史运动的加权余弦相似度总分；</li>
     *   <li>按分数降序返回 Top-N。</li>
     * </ol>
     *
     * @param userId 当前用户 ID
     * @param limit  最大返回数量
     * @return ItemCF 推荐记录列表
     */
    private List<RecommendationRecord> generateExerciseByItemCF(Long userId, int limit) {
        // 获取所有上架运动并建立 ID -> 运动对象映射
        List<FitExercise> activeExercises = recommendationRecordMapper.selectActiveExercises();
        if (activeExercises == null || activeExercises.isEmpty()) {
            return List.of();
        }
        Map<Long, FitExercise> activeExerciseMap = new HashMap<>();
        for (FitExercise exercise : activeExercises) {
            if (exercise.getId() != null && Integer.valueOf(1).equals(exercise.getStatus())) {
                activeExerciseMap.put(exercise.getId(), exercise);
            }
        }
        if (activeExerciseMap.isEmpty()) {
            return List.of();
        }

        // 构建用户-运动行为矩阵，获取当前用户的运动权重
        Map<Long, Map<Long, Double>> matrix = buildWorkoutBehaviorMatrix();
        Map<Long, Double> currentUserWeights = matrix.get(userId);

        // 矩阵中不足 2 个用户或当前用户无历史数据时，无法进行协同过滤
        if (matrix.size() < 2 || currentUserWeights == null || currentUserWeights.isEmpty()) {
            return List.of();
        }

        // 计算 ItemCF 分数：排除用户已接触过的运动
        Set<Long> historyIds = currentUserWeights.keySet();
        Map<Long, Double> scores = calculateItemCfScores(matrix, currentUserWeights, historyIds, activeExerciseMap.keySet());

        // 按分数降序取 Top-N，构建推荐记录
        return scores.entrySet().stream()
                .filter(entry -> activeExerciseMap.containsKey(entry.getKey()))
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed().thenComparing(Map.Entry::getKey))
                .limit(limit)
                .map(entry -> {
                    FitExercise exercise = activeExerciseMap.get(entry.getKey());
                    return createItemCfRecord(
                            REC_TYPE_EXERCISE,
                            exercise.getId(),
                            exercise.getExerciseName(),
                            entry.getValue(),
                            "根据您的历史训练记录和相似动作偏好生成"
                    );
                })
                .toList();
    }

    // ==================== ItemCF 饮食推荐 ====================

    /**
     * 基于物品协同过滤（ItemCF）生成饮食推荐。
     * <p>逻辑与 {@link #generateExerciseByItemCF} 类似，
     * 区别在于使用饮食行为矩阵（摄入量、是否推荐食物等因素计算权重）。</p>
     *
     * @param userId 当前用户 ID
     * @param limit  最大返回数量
     * @return ItemCF 推荐记录列表
     */
    private List<RecommendationRecord> generateFoodByItemCF(Long userId, int limit) {
        // 获取所有上架食物并建立 ID -> 食物对象映射
        List<FitFood> activeFoods = recommendationRecordMapper.selectActiveFoods();
        if (activeFoods == null || activeFoods.isEmpty()) {
            return List.of();
        }
        Map<Long, FitFood> activeFoodMap = new HashMap<>();
        for (FitFood food : activeFoods) {
            if (food.getId() != null && Integer.valueOf(1).equals(food.getStatus())) {
                activeFoodMap.put(food.getId(), food);
            }
        }
        if (activeFoodMap.isEmpty()) {
            return List.of();
        }

        // 构建用户-饮食行为矩阵，获取当前用户的饮食权重
        Map<Long, Map<Long, Double>> matrix = buildDietBehaviorMatrix();
        Map<Long, Double> currentUserWeights = matrix.get(userId);

        // 矩阵中不足 2 个用户或当前用户无历史数据时，无法进行协同过滤
        if (matrix.size() < 2 || currentUserWeights == null || currentUserWeights.isEmpty()) {
            return List.of();
        }

        // 计算 ItemCF 分数：排除用户已接触过的食物
        Set<Long> historyIds = currentUserWeights.keySet();
        Map<Long, Double> scores = calculateItemCfScores(matrix, currentUserWeights, historyIds, activeFoodMap.keySet());

        // 按分数降序取 Top-N，构建推荐记录
        return scores.entrySet().stream()
                .filter(entry -> activeFoodMap.containsKey(entry.getKey()))
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed().thenComparing(Map.Entry::getKey))
                .limit(limit)
                .map(entry -> {
                    FitFood food = activeFoodMap.get(entry.getKey());
                    return createItemCfRecord(
                            REC_TYPE_FOOD,
                            food.getId(),
                            food.getFoodName(),
                            entry.getValue(),
                            "根据您的历史饮食记录和相似食材偏好生成"
                    );
                })
                .toList();
    }

    // ==================== 行为矩阵构建 ====================

    /**
     * 构建用户-运动行为矩阵。
     * <p>矩阵结构：Map&lt;userId, Map&lt;exerciseId, weight&gt;&gt;，
     * 权重由完成状态、反馈评分、运动时长等多个维度综合得出。</p>
     *
     * @return 用户-运动行为矩阵
     */
    private Map<Long, Map<Long, Double>> buildWorkoutBehaviorMatrix() {
        List<UserWorkoutRecord> records = userWorkoutRecordMapper.selectAllForRecommendation();
        Map<Long, Map<Long, Double>> matrix = new HashMap<>();
        if (records == null || records.isEmpty()) {
            return matrix;
        }
        for (UserWorkoutRecord record : records) {
            if (record.getUserId() == null || record.getExerciseId() == null) {
                continue;
            }
            double weight = calculateWorkoutWeight(record);
            // 同一用户对同一运动的多次记录权重累加
            matrix.computeIfAbsent(record.getUserId(), key -> new HashMap<>())
                    .merge(record.getExerciseId(), weight, Double::sum);
        }
        return matrix;
    }

    /**
     * 构建用户-饮食行为矩阵。
     * <p>矩阵结构：Map&lt;userId, Map&lt;foodId, weight&gt;&gt;，
     * 权重由摄入量和是否为推荐食物等因素综合得出。</p>
     *
     * @return 用户-饮食行为矩阵
     */
    private Map<Long, Map<Long, Double>> buildDietBehaviorMatrix() {
        List<UserDietRecord> records = userDietRecordMapper.selectAllForRecommendation();
        Map<Long, Map<Long, Double>> matrix = new HashMap<>();
        if (records == null || records.isEmpty()) {
            return matrix;
        }
        for (UserDietRecord record : records) {
            if (record.getUserId() == null || record.getFoodId() == null) {
                continue;
            }
            double weight = calculateDietWeight(record);
            // 同一用户对同一食物的多次记录权重累加
            matrix.computeIfAbsent(record.getUserId(), key -> new HashMap<>())
                    .merge(record.getFoodId(), weight, Double::sum);
        }
        return matrix;
    }

    // ==================== ItemCF 核心算法 ====================

    /**
     * 计算 ItemCF 推荐分数。
     * <p>对每个候选物品，计算其与用户历史物品的加权相似度之和：
     * <pre>score(candidate) = Σ similarity(candidate, historyItem) × weight(historyItem)</pre>
     * 其中相似度通过 {@link #calculateCosineSimilarity} 余弦相似度计算。</p>
     *
     * @param matrix           全体用户行为矩阵
     * @param currentUserWeights 当前用户对各物品的权重
     * @param historyIds        当前用户已交互的物品 ID 集合（用于排除）
     * @param activeCandidateIds 上架候选物品 ID 集合
     * @return 候选物品 ID -> 推荐分数映射
     */
    private Map<Long, Double> calculateItemCfScores(
            Map<Long, Map<Long, Double>> matrix,
            Map<Long, Double> currentUserWeights,
            Set<Long> historyIds,
            Set<Long> activeCandidateIds
    ) {
        Map<Long, Double> scores = new HashMap<>();
        for (Long candidateId : activeCandidateIds) {
            // 跳过用户已交互过的物品
            if (historyIds.contains(candidateId)) {
                continue;
            }
            double score = 0D;
            // 遍历用户历史物品，累加与候选物品的加权相似度
            for (Map.Entry<Long, Double> historyEntry : currentUserWeights.entrySet()) {
                double similarity = calculateCosineSimilarity(matrix, historyEntry.getKey(), candidateId);
                if (similarity > 0D) {
                    score += similarity * historyEntry.getValue();
                }
            }
            if (score > 0D) {
                scores.put(candidateId, score);
            }
        }
        return scores;
    }

    /**
     * 计算两个物品之间的余弦相似度。
     * <p>公式：similarity(A, B) = (Σ wA·wB) / (||wA|| × ||wB||)
     * 其中 wA、wB 分别为各用户对物品 A、B 的行为权重向量。</p>
     *
     * @param matrix 全体用户行为矩阵
     * @param itemA  物品 A 的 ID
     * @param itemB  物品 B 的 ID
     * @return 余弦相似度，范围 [0, 1]；无共同用户交互时返回 0
     */
    private double calculateCosineSimilarity(Map<Long, Map<Long, Double>> matrix, Long itemA, Long itemB) {
        double numerator = 0D;   // 分子：两个物品的向量点积 Σ(wA * wB)
        double sumA = 0D;        // 物品 A 的向量模平方 Σ(wA²)
        double sumB = 0D;        // 物品 B 的向量模平方 Σ(wB²)
        for (Map<Long, Double> userWeights : matrix.values()) {
            Double weightA = userWeights.get(itemA);
            Double weightB = userWeights.get(itemB);
            if (weightA != null) {
                sumA += weightA * weightA;
            }
            if (weightB != null) {
                sumB += weightB * weightB;
            }
            if (weightA != null && weightB != null) {
                numerator += weightA * weightB;
            }
        }
        if (numerator <= 0D || sumA <= 0D || sumB <= 0D) {
            return 0D;
        }
        return numerator / (Math.sqrt(sumA) * Math.sqrt(sumB));
    }

    // ==================== 行为权重计算 ====================

    /**
     * 计算单条运动记录的行为权重。
     * <p>基础分 1.0，以下因素加分：
     * <ul>
     *   <li>完成状态为 DONE / 已完成 → +1.0</li>
     *   <li>反馈评分（1-5 分）→ +score/5</li>
     *   <li>运动时长 → +min(duration/60, 1.0)</li>
     * </ul>
     */
    private double calculateWorkoutWeight(UserWorkoutRecord record) {
        double weight = 1D;
        String completionStatus = safeUpper(record.getCompletionStatus());
        // 完成状态加分
        if ("DONE".equals(completionStatus) || "已完成".equals(record.getCompletionStatus())) {
            weight += 1D;
        }
        // 反馈评分加分（归一化到 0-1）
        if (record.getFeedbackScore() != null) {
            weight += record.getFeedbackScore() / 5D;
        }
        // 运动时长加分（归一化到 0-1，最多 60 分钟计满）
        if (record.getDurationMin() != null) {
            weight += Math.min(record.getDurationMin() / 60D, 1D);
        }
        return weight;
    }

    /**
     * 计算单条饮食记录的行为权重。
     * <p>基础分 1.0，以下因素加分：
     * <ul>
     *   <li>摄入量 → +min(grams/500, 1.0)</li>
     *   <li>非系统推荐食物 → +0.5（体现用户自主选择偏好）</li>
     * </ul>
     */
    private double calculateDietWeight(UserDietRecord record) {
        double weight = 1D;
        // 摄入量加分（归一化到 0-1，500g 计满）
        if (record.getIntakeGrams() != null) {
            weight += Math.min(record.getIntakeGrams().doubleValue() / 500D, 1D);
        }
        // 用户主动选择的食物（非推荐）加分，反映真实偏好
        if (Integer.valueOf(0).equals(record.getIsRecommended())) {
            weight += 0.5D;
        }
        return weight;
    }

    // ==================== 规则评分：运动推荐 ====================

    /**
     * 基于规则评分生成运动推荐（作为 ItemCF 的补充）。
     * <p>评分维度：用户健身目标匹配度、活动水平匹配度、近期训练类别偏好、
     * 近期训练部位偏好。排除用户最近已推荐和已训练的运动。</p>
     *
     * @param user             当前用户实体
     * @param limit            最大返回数量
     * @param extraExcludeIds  需额外排除的运动 ID（如 ItemCF 已推荐的）
     * @return 按分数降序排列的推荐记录列表
     */
    private List<RecommendationRecord> buildExerciseRecommendations(SysUser user, int limit, Set<Long> extraExcludeIds) {
        if (limit <= 0) {
            return List.of();
        }
        List<FitExercise> candidates = recommendationRecordMapper.selectActiveExercises();
        if (candidates == null || candidates.isEmpty()) {
            return List.of();
        }

        // 获取用户画像信息
        String goal = safeUpper(user.getFitnessGoal());           // 健身目标
        String activityLevel = safeUpper(user.getActivityLevel()); // 活动水平

        // 获取用户近期训练偏好
        List<String> recentCategories = safeUpperList(recommendationRecordMapper.selectRecentWorkoutCategories(user.getId()));
        List<String> recentBodyParts = safeUpperList(recommendationRecordMapper.selectRecentWorkoutBodyParts(user.getId()));

        // 构建排除集合：最近推荐过的 + 历史训练过的 + ItemCF 已生成的
        Set<Long> excludeIds = new LinkedHashSet<>(recommendationRecordMapper.selectRecentRecommendedTargetIds(user.getId(), REC_TYPE_EXERCISE, 10));
        Map<Long, Double> historyWeights = buildWorkoutBehaviorMatrix().get(user.getId());
        if (historyWeights != null) {
            excludeIds.addAll(historyWeights.keySet());
        }
        if (extraExcludeIds != null) {
            excludeIds.addAll(extraExcludeIds);
        }

        // 对候选运动评分、过滤、排序后取 Top-N
        return candidates.stream()
                .map(candidate -> buildExerciseRecommendation(candidate, goal, activityLevel, recentCategories, recentBodyParts, excludeIds))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(RecommendationRecord::getScore).reversed().thenComparing(RecommendationRecord::getTargetId))
                .limit(limit)
                .toList();
    }

    /**
     * 对单个运动候选进行规则评分。
     * <p>基础分 50，根据以下维度加分，总分低于 40 则过滤掉：
     * <ul>
     *   <li>健身目标匹配（减脂/塑形→有氧+25；增肌/力量→力量+25）</li>
     *   <li>活动水平与难度匹配（初/中/高级对应难度各+15）</li>
     *   <li>近期训练类别一致性+6，部位一致性+6</li>
     * </ul>
     */
    private RecommendationRecord buildExerciseRecommendation(
            FitExercise candidate,
            String goal,
            String activityLevel,
            List<String> recentCategories,
            List<String> recentBodyParts,
            Set<Long> excludeIds
    ) {
        int score = 50;  // 基础分
        List<String> reasons = new ArrayList<>();
        String category = safeUpper(candidate.getCategory());
        String difficulty = safeUpper(candidate.getDifficulty());
        String bodyPart = safeUpper(candidate.getBodyPart());

        // 排除已推荐/已训练的运动
        if (excludeIds.contains(candidate.getId())) {
            return null;
        }

        // --- 健身目标匹配加分 ---
        // 减脂/塑形目标：优先推荐有氧运动和高热量消耗运动
        if (goal.contains("减脂") || goal.contains("塑形") || goal.contains("FAT")) {
            if (category.contains("有氧") || category.contains("CARDIO")) {
                score += 25;
                reasons.add("更贴合减脂/塑形目标");
            }
            if (candidate.getCaloriesPerHour() != null && candidate.getCaloriesPerHour() >= 400) {
                score += 8;
                reasons.add("热量消耗较高");
            }
        }
        // 增肌/力量目标：优先推荐力量训练
        if (goal.contains("增肌") || goal.contains("力量") || goal.contains("MUSCLE")) {
            if (category.contains("力量") || category.contains("STRENGTH")) {
                score += 25;
                reasons.add("更贴合增肌目标");
            }
            if (candidate.getDefaultSets() != null && candidate.getDefaultSets() >= 3) {
                score += 5;
            }
        }

        // --- 活动水平与难度匹配加分 ---
        if (activityLevel.contains("初") || activityLevel.contains("BEGINNER")) {
            if (difficulty.contains("初") || difficulty.contains("简") || difficulty.contains("BEGINNER") || difficulty.contains("EASY")) {
                score += 15;
                reasons.add("难度适合当前运动水平");
            }
        } else if (activityLevel.contains("中") || activityLevel.contains("INTERMEDIATE")) {
            if (difficulty.contains("中") || difficulty.contains("INTERMEDIATE") || difficulty.contains("MEDIUM")) {
                score += 15;
                reasons.add("难度与当前水平匹配");
            }
        } else if (activityLevel.contains("高") || activityLevel.contains("ADVANCED")) {
            if (difficulty.contains("高") || difficulty.contains("ADVANCED") || difficulty.contains("HARD")) {
                score += 15;
                reasons.add("可满足进阶训练强度");
            }
        }

        // --- 近期训练偏好加分 ---
        if (!recentCategories.isEmpty() && recentCategories.stream().anyMatch(category::contains)) {
            score += 6;
            reasons.add("与近期训练偏好一致");
        }
        if (!recentBodyParts.isEmpty() && recentBodyParts.stream().anyMatch(bodyPart::contains)) {
            score += 6;
        }

        // 若无明确匹配理由，添加通用推荐理由
        if (reasons.isEmpty()) {
            reasons.add("结合目标、运动水平与近期训练情况生成");
        }

        // 过滤掉总分过低的候选
        if (score < 40) {
            return null;
        }
        return createRecord(REC_TYPE_EXERCISE, candidate.getId(), candidate.getExerciseName(), score, String.join("；", reasons));
    }

    // ==================== 规则评分：饮食推荐 ====================

    /**
     * 基于规则评分生成饮食推荐（作为 ItemCF 的补充）。
     * <p>评分维度：健身目标匹配、热量/蛋白质指标、饮食偏好匹配、
     * 近期用餐时段偏好。排除用户最近已推荐的饮食。</p>
     *
     * @param user             当前用户实体
     * @param limit            最大返回数量
     * @param extraExcludeIds  需额外排除的食物 ID（如 ItemCF 已推荐的）
     * @return 按分数降序排列的推荐记录列表
     */
    private List<RecommendationRecord> buildFoodRecommendations(SysUser user, int limit, Set<Long> extraExcludeIds) {
        if (limit <= 0) {
            return List.of();
        }
        List<FitFood> candidates = recommendationRecordMapper.selectActiveFoods();
        if (candidates == null || candidates.isEmpty()) {
            return List.of();
        }

        // 获取用户画像信息
        String goal = safeUpper(user.getFitnessGoal());           // 健身目标
        String dietPreference = safeUpper(user.getDietPreference()); // 饮食偏好

        // 获取用户近期用餐时段偏好
        List<String> recentMealTypes = safeUpperList(recommendationRecordMapper.selectRecentMealTypes(user.getId()));

        // 构建排除集合
        Set<Long> excludeIds = new LinkedHashSet<>(recommendationRecordMapper.selectRecentRecommendedTargetIds(user.getId(), REC_TYPE_FOOD, 10));
        Map<Long, Double> historyWeights = buildDietBehaviorMatrix().get(user.getId());
        if (historyWeights != null) {
            excludeIds.addAll(historyWeights.keySet());
        }
        if (extraExcludeIds != null) {
            excludeIds.addAll(extraExcludeIds);
        }

        // 对候选食物评分、过滤、排序后取 Top-N
        return candidates.stream()
                .map(candidate -> buildFoodRecommendation(candidate, goal, dietPreference, recentMealTypes, excludeIds))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(RecommendationRecord::getScore).reversed().thenComparing(RecommendationRecord::getTargetId))
                .limit(limit)
                .toList();
    }

    /**
     * 对单个食物候选进行规则评分。
     * <p>基础分 50，根据以下维度加分，总分低于 40 则过滤掉：
     * <ul>
     *   <li>适用目标匹配+22</li>
     *   <li>减脂场景下低热量（≤200kcal/100g）+10</li>
     *   <li>增肌场景下高蛋白（≥15g/100g）+12</li>
     *   <li>饮食偏好匹配+12</li>
     *   <li>近期用餐时段匹配+6</li>
     * </ul>
     */
    private RecommendationRecord buildFoodRecommendation(
            FitFood candidate,
            String goal,
            String dietPreference,
            List<String> recentMealTypes,
            Set<Long> excludeIds
    ) {
        int score = 50;  // 基础分
        List<String> reasons = new ArrayList<>();
        String suitableGoal = safeUpper(candidate.getSuitableGoal());
        String suitableTime = safeUpper(candidate.getSuitableTime());
        String category = safeUpper(candidate.getCategory());
        String description = safeUpper(candidate.getDescription());

        // 排除已推荐的食物
        if (excludeIds.contains(candidate.getId())) {
            return null;
        }

        // --- 健身目标匹配加分 ---
        if (!goal.isBlank() && suitableGoal.contains(goal)) {
            score += 22;
            reasons.add("适用目标与用户目标一致");
        }

        // --- 营养指标加分 ---
        // 减脂场景：低热量食物加分
        if ((goal.contains("减脂") || goal.contains("FAT")) && candidate.getCaloriesPer100g() != null) {
            if (candidate.getCaloriesPer100g().compareTo(BigDecimal.valueOf(200)) <= 0) {
                score += 10;
                reasons.add("热量相对更友好");
            }
        }
        // 增肌场景：高蛋白食物加分
        if ((goal.contains("增肌") || goal.contains("MUSCLE")) && candidate.getProteinPer100g() != null) {
            if (candidate.getProteinPer100g().compareTo(BigDecimal.valueOf(15)) >= 0) {
                score += 12;
                reasons.add("蛋白质含量较高");
            }
        }

        // --- 饮食偏好匹配加分 ---
        if (!dietPreference.isBlank() && (category.contains(dietPreference) || description.contains(dietPreference))) {
            score += 12;
            reasons.add("符合饮食偏好");
        }

        // --- 近期用餐时段匹配加分 ---
        if (!recentMealTypes.isEmpty() && recentMealTypes.stream().anyMatch(suitableTime::contains)) {
            score += 6;
        }

        // 若无明确匹配理由，添加通用推荐理由
        if (reasons.isEmpty()) {
            reasons.add("结合目标、饮食偏好与近期饮食习惯生成");
        }

        // 过滤掉总分过低的候选
        if (score < 40) {
            return null;
        }
        return createRecord(REC_TYPE_FOOD, candidate.getId(), candidate.getFoodName(), score, String.join("；", reasons));
    }

    // ==================== 规则评分：计划推荐 ====================

    /**
     * 生成训练计划推荐。
     * <p>优先推荐当前用户正在进行中的计划，按计划排序取前 {@link #PLAN_RECOMMEND_LIMIT} 条。
     * 基础分 90，后续计划依次递减 4 分。</p>
     *
     * @param currentUserId 当前用户 ID
     * @return 计划推荐记录列表
     */
    private List<RecommendationRecord> buildPlanRecommendations(Long currentUserId) {
        List<PlanVO> plans = recommendationRecordMapper.selectRecommendablePlansByUserId(currentUserId);
        if (plans == null || plans.isEmpty()) {
            return List.of();
        }
        List<RecommendationRecord> result = new ArrayList<>();
        int baseScore = 90;
        for (int i = 0; i < plans.size() && i < PLAN_RECOMMEND_LIMIT; i++) {
            PlanVO plan = plans.get(i);
            // 拼接推荐理由：包含计划目标和执行周期
            StringBuilder reason = new StringBuilder("推荐优先查看当前有效计划");
            if (plan.getTargetGoal() != null && !plan.getTargetGoal().isBlank()) {
                reason.append("，目标为").append(plan.getTargetGoal());
            }
            if (plan.getStartDate() != null && plan.getEndDate() != null) {
                reason.append("，执行周期 ").append(plan.getStartDate()).append(" 至 ").append(plan.getEndDate());
            }
            result.add(createRecord(REC_TYPE_PLAN, plan.getId(), plan.getPlanName(), baseScore - (i * 4), reason.toString()));
        }
        return result;
    }

    // ==================== 推荐记录工厂方法 ====================

    /**
     * 创建基于规则算法的推荐记录。
     *
     * @param recType    推荐类型
     * @param targetId   推荐目标 ID
     * @param targetName 推荐目标名称
     * @param score      评分
     * @param reason     推荐理由
     * @return 推荐记录实体
     */
    private RecommendationRecord createRecord(String recType, Long targetId, String targetName, int score, String reason) {
        RecommendationRecord record = new RecommendationRecord();
        record.setRecType(recType);
        record.setTargetId(targetId);
        record.setTargetName(targetName);
        record.setAlgorithmType(ALGORITHM_TYPE_RULE_BASED);
        record.setScore(BigDecimal.valueOf(score).setScale(2, RoundingMode.HALF_UP));
        record.setReason(reason);
        return record;
    }

    /**
     * 创建基于 ItemCF 算法的推荐记录。
     *
     * @param recType    推荐类型
     * @param targetId   推荐目标 ID
     * @param targetName 推荐目标名称
     * @param score      ItemCF 计算得出的分数
     * @param reason     推荐理由
     * @return 推荐记录实体
     */
    private RecommendationRecord createItemCfRecord(String recType, Long targetId, String targetName, double score, String reason) {
        RecommendationRecord record = new RecommendationRecord();
        record.setRecType(recType);
        record.setTargetId(targetId);
        record.setTargetName(targetName);
        record.setAlgorithmType(ALGORITHM_TYPE_ITEM_CF);
        record.setScore(BigDecimal.valueOf(score).setScale(2, RoundingMode.HALF_UP));
        record.setReason(reason);
        return record;
    }

    // ==================== 工具方法 ====================

    /**
     * 提取推荐记录列表中所有 targetId，用于构建排除集合。
     */
    private Set<Long> targetIds(List<RecommendationRecord> records) {
        if (records == null || records.isEmpty()) {
            return Set.of();
        }
        Set<Long> result = new LinkedHashSet<>();
        for (RecommendationRecord record : records) {
            if (record.getTargetId() != null) {
                result.add(record.getTargetId());
            }
        }
        return result;
    }

    /**
     * 对推荐记录列表按 recType:targetId 去重，保留先出现的记录。
     */
    private List<RecommendationRecord> deduplicateRecommendations(List<RecommendationRecord> records) {
        if (records == null || records.isEmpty()) {
            return List.of();
        }
        List<RecommendationRecord> result = new ArrayList<>();
        Set<String> keys = new LinkedHashSet<>();
        for (RecommendationRecord record : records) {
            if (record == null || record.getRecType() == null || record.getTargetId() == null) {
                continue;
            }
            String key = record.getRecType() + ":" + record.getTargetId();
            if (keys.add(key)) {
                result.add(record);
            }
        }
        return result;
    }

    /**
     * 将字符串列表中的每个元素安全转大写，null 元素被过滤掉。
     */
    private List<String> safeUpperList(List<String> values) {
        if (values == null || values.isEmpty()) {
            return List.of();
        }
        return values.stream()
                .filter(Objects::nonNull)
                .map(this::safeUpper)
                .toList();
    }

    /**
     * 安全地将字符串转为大写，null 值返回空字符串。
     */
    private String safeUpper(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }
}
