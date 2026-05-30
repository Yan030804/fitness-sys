package com.example.fitness.ai.service.impl;

import com.example.fitness.ai.dto.AiChatRequest;
import com.example.fitness.ai.service.AiChatService;
import com.example.fitness.ai.vo.AiAppointmentAssistVO;
import com.example.fitness.ai.vo.AiChatResponseVO;
import com.example.fitness.appointment.dto.AppointmentCreateRequest;
import com.example.fitness.appointment.service.AppointmentService;
import com.example.fitness.appointment.vo.AppointmentVO;
import com.example.fitness.common.exception.BusinessException;
import com.example.fitness.common.utils.SecurityUtils;
import com.example.fitness.system.entity.SysUser;
import com.example.fitness.system.mapper.SysUserMapper;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiChatServiceImpl implements AiChatService {

    private static final String XIAOMI_MIMO_BASE_URL = "https://token-plan-cn.xiaomimimo.com/v1";
    private static final String XIAOMI_MIMO_API_KEY = "";
    private static final String XIAOMI_MIMO_MODEL_NAME = "mimo-v2.5-pro";
    private static final String RESERVATION_SUFFIX = "请问您是否需要进行预约？";
    private static final String FITNESS_TITLE = "【健身建议】";
    private static final String APPOINTMENT_TITLE = "【预约提示】";
    private static final String APPOINTMENT_SUCCESS_TITLE = "【预约成功】";
    private static final String APPOINTMENT_MISSING_TITLE = "【预约信息补充】";
    private static final String OUT_OF_SCOPE_TITLE = "【暂不支持】";
    private static final Pattern PHONE_PATTERN = Pattern.compile("1\\d{10}");
    private static final Pattern AGE_PATTERN = Pattern.compile("(\\d{1,3})\\s*岁");
    private static final Pattern DATE_PATTERN = Pattern.compile("(\\d{4})[-/年](\\d{1,2})[-/月](\\d{1,2})日?");
    private static final Pattern MONTH_DAY_PATTERN = Pattern.compile("(\\d{1,2})月(\\d{1,2})日");
    private static final Pattern TIME_PATTERN = Pattern.compile("([01]?\\d|2[0-3])[:点时]([0-5]?\\d)?分?");
    private static final Pattern NAME_PATTERN = Pattern.compile("(?:我叫|我是|姓名[是为:]?|名字[是为:]?)([\\u4e00-\\u9fa5A-Za-z·]{2,20})");

    private final AppointmentService appointmentService;
    private final SysUserMapper sysUserMapper;

    private final OpenAiChatModel chatModel = OpenAiChatModel.builder()
            .baseUrl(XIAOMI_MIMO_BASE_URL)
            .apiKey(XIAOMI_MIMO_API_KEY)
            .modelName(XIAOMI_MIMO_MODEL_NAME)
            .build();

    private final OpenAiStreamingChatModel streamingChatModel = OpenAiStreamingChatModel.builder()
            .baseUrl(XIAOMI_MIMO_BASE_URL)
            .apiKey(XIAOMI_MIMO_API_KEY)
            .modelName(XIAOMI_MIMO_MODEL_NAME)
            .build();

    @Override
    public AiChatResponseVO chat(AiChatRequest request) {
        String message = normalizeMessage(request.getMessage());
        SysUser currentUser = getCurrentUser();
        AppointmentParseResult parseResult = parseAppointment(message, currentUser);

        if (parseResult.bookingIntent && parseResult.canCreateAppointment()) {
            AppointmentVO appointmentVO = appointmentService.create(parseResult.toCreateRequest());
            return buildResponse(buildAppointmentSuccessAnswer(appointmentVO), "APPOINTMENT");
        }
        if (parseResult.bookingIntent && !parseResult.missingFields.isEmpty()) {
            return buildResponse(buildMissingFieldsAnswer(parseResult), "APPOINTMENT");
        }
        if (!isFitnessRelated(message)) {
            return buildResponse(buildOutOfScopeAnswer(), "OUT_OF_SCOPE");
        }

        String answer;
        try {
            answer = chatModel.chat(buildMessages(message, currentUser)).aiMessage().text();
        } catch (Exception ex) {
            log.error("ai chat failed", ex);
            throw new BusinessException(500, "AI chat failed, please check AI configuration");
        }
        return buildResponse(polishFitnessAnswer(answer), "FITNESS_QA");
    }

    private String buildFinalSuffixChunk(String currentText) {
        String normalized = normalizeStreamText(currentText);

        if (normalized.isBlank()) {
            return "【健身建议】\n\n"
                    + "【目标分析】\n"
                    + "我可以为您提供基础健身建议。"
                    + "\n\n" + APPOINTMENT_TITLE
                    + "\n" + RESERVATION_SUFFIX;
        }

        if (!normalized.endsWith("。") && !normalized.endsWith("！") && !normalized.endsWith("？")) {
            return "。"
                    + "\n\n" + APPOINTMENT_TITLE
                    + "\n" + RESERVATION_SUFFIX;
        }

        return "\n\n" + APPOINTMENT_TITLE + "\n" + RESERVATION_SUFFIX;
    }


    @Override
    public SseEmitter streamChat(AiChatRequest request) {
        String message = normalizeMessage(request.getMessage());
        SysUser currentUser = getCurrentUser();
        AppointmentParseResult parseResult = parseAppointment(message, currentUser);
        SseEmitter emitter = new SseEmitter(0L);

        if (parseResult.bookingIntent) {
            handleAppointmentStream(emitter, parseResult);
            return emitter;
        }

        if (!isFitnessRelated(message)) {
            streamPlainText(emitter, buildOutOfScopeAnswer(), "OUT_OF_SCOPE");
            return emitter;
        }

        List<ChatMessage> messages = buildMessages(message, currentUser);
        StringBuilder rawBuffer = new StringBuilder();
        StringBuilder fullAnswer = new StringBuilder();

        try {
            streamingChatModel.chat(messages, new StreamingChatResponseHandler() {
                @Override
                public void onPartialResponse(String partialResponse) {
                    rawBuffer.append(partialResponse);
                    flushCompletedSentences(rawBuffer, fullAnswer, emitter);
                }

                @Override
                public void onCompleteResponse(ChatResponse completeResponse) {
//                    flushRemainingContent(rawBuffer, fullAnswer, emitter);
//                    sendDone(emitter, buildResponse(fullAnswer.toString(), "FITNESS_QA"));
                    flushRemainingContent(rawBuffer, fullAnswer, emitter);

                    String finalAnswer = polishFitnessAnswer(fullAnswer.toString());
                    String suffixChunk = buildFinalSuffixChunk(fullAnswer.toString());

                    try {
                        if (!suffixChunk.isBlank()) {
                            emitter.send(SseEmitter.event().name("chunk").data(suffixChunk));
                        }
                    } catch (IOException ex) {
                        log.error("send final suffix chunk failed", ex);
                    }

                    sendDone(emitter, buildResponse(finalAnswer, "FITNESS_QA"));
                }

                @Override
                public void onError(Throwable error) {
                    log.error("ai stream failed", error);
                    completeWithError(emitter, "AI chat failed, please check AI configuration");
                }
            });
        } catch (Exception ex) {
            log.error("start ai stream failed", ex);
            completeWithError(emitter, "AI chat failed, please check AI configuration");
        }
        return emitter;
    }

    @Override
    public AiAppointmentAssistVO appointmentAssist(AiChatRequest request) {
        String message = normalizeMessage(request.getMessage());
        SysUser currentUser = getCurrentUser();
        AppointmentParseResult parseResult = parseAppointment(message, currentUser);

        AiAppointmentAssistVO response = new AiAppointmentAssistVO();
        response.setAppointmentType(parseResult.appointmentType);
        response.setReserveDate(parseResult.reserveDate);
        response.setReserveTime(parseResult.reserveTime);
        response.setRemark(parseResult.remark);
        response.setAnswer(parseResult.missingFields.isEmpty()
                ? polishPlainAnswer("我已经帮您整理出预约建议，您确认后即可提交预约。")
                : buildMissingFieldsAnswer(parseResult));
        return response;
    }

    private void handleAppointmentStream(SseEmitter emitter, AppointmentParseResult parseResult) {
        if (parseResult.canCreateAppointment()) {
            try {
                AppointmentVO appointmentVO = appointmentService.create(parseResult.toCreateRequest());
                streamPlainText(emitter, buildAppointmentSuccessAnswer(appointmentVO), "APPOINTMENT");
            } catch (Exception ex) {
                log.error("create appointment by ai failed", ex);
                completeWithError(emitter, "预约创建失败，请检查预约信息或系统配置");
            }
            return;
        }
        streamPlainText(emitter, buildMissingFieldsAnswer(parseResult), "APPOINTMENT");
    }

    private void streamPlainText(SseEmitter emitter, String text, String suggestionType) {
        String polished = polishPlainAnswer(text);
        try {
            for (String sentence : splitSentences(polished)) {
                if (!sentence.isBlank()) {
                    emitter.send(SseEmitter.event().name("chunk").data(sentence));
                }
            }
            sendDone(emitter, buildResponse(polished, suggestionType));
        } catch (IOException ex) {
            log.error("stream plain text failed", ex);
            emitter.completeWithError(ex);
        }
    }

//    private void flushCompletedSentences(StringBuilder rawBuffer, StringBuilder fullAnswer, SseEmitter emitter) {
//        int index = findLastSentenceEnd(rawBuffer);
//        if (index < 0) {
//            return;
//        }
//        String completed = rawBuffer.substring(0, index + 1);
//        rawBuffer.delete(0, index + 1);
//        emitPolishedText(completed, fullAnswer, emitter);
//    }

    private void flushCompletedSentences(StringBuilder rawBuffer, StringBuilder fullAnswer, SseEmitter emitter) {
        int index = findLastSentenceEnd(rawBuffer);
        if (index < 0) {
            return;
        }
        String completed = rawBuffer.substring(0, index + 1);
        rawBuffer.delete(0, index + 1);
        emitRawText(completed, fullAnswer, emitter);
    }


//    private void flushRemainingContent(StringBuilder rawBuffer, StringBuilder fullAnswer, SseEmitter emitter) {
//        if (rawBuffer.length() == 0) {
//            return;
//        }
//        String remaining = rawBuffer.toString();
//        rawBuffer.setLength(0);
//        emitPolishedText(remaining, fullAnswer, emitter);
//    }

    private void flushRemainingContent(StringBuilder rawBuffer, StringBuilder fullAnswer, SseEmitter emitter) {
        if (rawBuffer.length() == 0) {
            return;
        }
        String remaining = rawBuffer.toString();
        rawBuffer.setLength(0);
        emitRawText(remaining, fullAnswer, emitter);
    }


    private void emitPolishedText(String rawText, StringBuilder fullAnswer, SseEmitter emitter) {
        String polished = polishFitnessAnswer(rawText);
        if (polished.isBlank()) {
            return;
        }
        try {
            for (String sentence : splitSentences(polished)) {
                if (!sentence.isBlank()) {
                    emitter.send(SseEmitter.event().name("chunk").data(sentence));
                    fullAnswer.append(sentence);
                }
            }
        } catch (IOException ex) {
            throw new BusinessException(500, "AI stream output failed");
        }
    }
    private void emitRawText(String rawText, StringBuilder fullAnswer, SseEmitter emitter) {
        String normalized = normalizeStreamText(rawText);
        if (normalized.isBlank()) {
            return;
        }
        try {
            emitter.send(SseEmitter.event().name("chunk").data(normalized));
            fullAnswer.append(normalized);
        } catch (IOException ex) {
            throw new BusinessException(500, "AI stream output failed");
        }
    }


    private String normalizeStreamText(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }

        String result = text;

        while (result.contains(RESERVATION_SUFFIX)) {
            result = result.replace(RESERVATION_SUFFIX, "").trim();
        }

        result = formatAnswerLayout(result);

        return result;
    }


    private void sendDone(SseEmitter emitter, AiChatResponseVO response) {
        try {
            emitter.send(SseEmitter.event().name("done").data(response));
        } catch (IOException ex) {
            log.error("send done event failed", ex);
        } finally {
            emitter.complete();
        }
    }

    private void completeWithError(SseEmitter emitter, String message) {
        try {
            emitter.send(SseEmitter.event().name("error").data(message));
        } catch (IOException ex) {
            log.error("send error event failed", ex);
        } finally {
            emitter.complete();
        }
    }

    private AiChatResponseVO buildResponse(String answer, String suggestionType) {
        AiChatResponseVO response = new AiChatResponseVO();
        response.setAnswer(answer);
        response.setSuggestionType(suggestionType);
        response.setReferences(List.of("如需预约，请提供姓名、手机号、预约类型、预约日期和预约时间"));
        return response;
    }

    private List<ChatMessage> buildMessages(String message, SysUser currentUser) {
        StringBuilder userProfile = new StringBuilder();
        userProfile.append("当前用户信息：");
        userProfile.append("昵称=").append(defaultText(currentUser.getNickname())).append("；");
        userProfile.append("年龄=").append(currentUser.getAge() == null ? "未知" : currentUser.getAge()).append("；");
        userProfile.append("性别=").append(defaultText(currentUser.getGender())).append("；");
        userProfile.append("健身目标=").append(defaultText(currentUser.getFitnessGoal())).append("；");
        userProfile.append("运动水平=").append(defaultText(currentUser.getActivityLevel())).append("；");
        userProfile.append("饮食偏好=").append(defaultText(currentUser.getDietPreference())).append("。\n");
        userProfile.append("用户问题：").append(message);

        return List.of(
                SystemMessage.from(buildSystemPrompt()),
                UserMessage.from(userProfile.toString())
        );
    }

    private String buildSystemPrompt() {
        return "你是一个专业、友好、克制的私人健身助手，只能回答健身、运动、减脂、增肌、塑形、体态、康复拉伸、训练安排、饮食营养和预约咨询相关问题。"
                + "如果用户提问与健身无关，请明确拒绝并引导用户咨询健身相关内容。"
                + "回答必须使用中文，适合健身管理系统展示。"
                + "普通健身问答控制在250到450字左右。"
                + "如果用户提供身高、体重、年龄、性别、运动基础、目标周期等信息，请结合这些信息进行个性化分析。"
                + "普通健身问答必须严格使用纯文本结构，不要使用Markdown有序列表，不要输出1. 2. 3.，不要输出1、2、3、，不要使用Markdown无序列表-或*。"
                + "编号必须使用中文括号格式：1）2）3）。\n\n"
                + "普通健身问答必须使用以下格式：\n\n"
                + "【健身建议】\n\n"
                + "【目标分析】\n"
                + "这里写目标分析内容。\n\n"
                + "【训练安排】\n"
                + "1）训练频率：每周训练4次，每次60分钟左右。\n"
                + "2）有氧训练：每周进行3到4次有氧运动。\n"
                + "3）力量训练：每周安排2次全身力量训练。\n\n"
                + "【饮食建议】\n"
                + "1）热量控制：每日热量摄入控制在2000到2200卡路里。\n"
                + "2）蛋白质摄入：每天摄入约130克蛋白质。\n"
                + "3）主食与脂肪：主食选择全谷物，减少高油高糖食物。\n"
                + "4）饮水与习惯：每天饮水2000毫升以上，避免夜宵。\n\n"
                + "【注意事项】\n"
                + "1）循序渐进：从低强度运动开始，逐步增加难度。\n"
                + "2）避免受伤：训练前热身，训练后拉伸。\n"
                + "3）记录与调整：每周记录体重和训练表现。\n"
                + "4）保证休息：每周安排1到2天休息日。\n\n"
                + "特别要求：\n"
                + "1）每个【标题】必须单独占一行。\n"
                + "2）每个标题前后必须有空行。\n"
                + "3）每个模块内编号都从1）开始。\n"
                + "4）不要输出Markdown表格。\n"
                + "5）不要主动输出【预约提示】，预约提示由Java统一追加。\n"
                + "不要输出多余开场白，不要重复用户问题。";
    }


    private AppointmentParseResult parseAppointment(String message, SysUser currentUser) {
        AppointmentParseResult result = new AppointmentParseResult();
        String lowerMessage = message.toLowerCase(Locale.ROOT);
        result.bookingIntent = lowerMessage.contains("预约") || lowerMessage.contains("预定") || lowerMessage.contains("约个") || lowerMessage.contains("appointment");
        result.name = findName(message, currentUser);
        result.phone = findPhone(message, currentUser);
        result.gender = findGender(message, currentUser);
        result.age = findAge(message, currentUser);
        result.appointmentType = findAppointmentType(message);
        result.reserveDate = findDate(message);
        result.reserveTime = findTime(message);
        result.remark = message;

        if (result.bookingIntent) {
            if (isBlank(result.name)) {
                result.missingFields.add("姓名");
            }
            if (isBlank(result.phone)) {
                result.missingFields.add("手机号");
            }
            if (isBlank(result.appointmentType)) {
                result.missingFields.add("预约类型");
            }
            if (result.reserveDate == null) {
                result.missingFields.add("预约日期");
            }
            if (result.reserveTime == null) {
                result.missingFields.add("预约时间");
            }
        }
        return result;
    }

    private String buildAppointmentSuccessAnswer(AppointmentVO appointmentVO) {
        return String.format(
                APPOINTMENT_SUCCESS_TITLE
                        + "\n预约类型：%s"
                        + "\n预约时间：%s %s"
                        + "\n联系电话：%s",
                appointmentVO.getAppointmentType(),
                appointmentVO.getReserveDate(),
                appointmentVO.getReserveTime(),
                appointmentVO.getPhone()
        );
    }

    private String buildMissingFieldsAnswer(AppointmentParseResult parseResult) {
        return APPOINTMENT_MISSING_TITLE
                + "\n我可以继续帮您完成预约。"
                + "\n请继续提供：" + String.join("、", parseResult.missingFields) + "。";
    }

    private String buildOutOfScopeAnswer() {
        return OUT_OF_SCOPE_TITLE
                + "\n抱歉，我目前只回答健身、运动、饮食营养和预约相关问题。";
    }

    private boolean isFitnessRelated(String message) {
        String lower = message.toLowerCase(Locale.ROOT);
        String[] keywords = {
                "健身", "减脂", "增肌", "塑形", "体脂", "训练", "运动", "跑步", "有氧", "无氧", "力量", "拉伸",
                "饮食", "热量", "蛋白", "碳水", "脂肪", "bmi", "体重", "身高", "恢复", "预约", "consult", "fitness",
                "workout", "cardio", "protein", "gym", "diet"
        };
        for (String keyword : keywords) {
            if (lower.contains(keyword.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

//    private String polishAnswer(String text) {
//        if (text == null || text.isBlank()) {
//            return "我可以为您提供基础健身建议。" + RESERVATION_SUFFIX;
//        }
//        List<String> sentences = splitRawSentences(text);
//        List<String> polished = new ArrayList<>();
//        for (String sentence : sentences) {
//            String trimmed = sentence.trim();
//            if (trimmed.isEmpty()) {
//                continue;
//            }
//            trimmed = trimmed.replaceAll("[\\r\\n]+", " ").trim();
//            if (!trimmed.endsWith("。") && !trimmed.endsWith("！") && !trimmed.endsWith("？")) {
//                trimmed = trimmed + "。";
//            }
//            trimmed = trimmed + RESERVATION_SUFFIX;
//            polished.add(trimmed);
//        }
//        if (polished.isEmpty()) {
//            return "我可以为您提供基础健身建议。" + RESERVATION_SUFFIX;
//        }
//        return String.join("", polished);
//    }

    private String removeReservationSuffix(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }

        String result = text;

        while (result.contains(RESERVATION_SUFFIX)) {
            result = result.replace(RESERVATION_SUFFIX, "").trim();
        }

        result = formatAnswerLayout(result);

        return result;
    }

    private String formatAnswerLayout(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }

        String result = text
                .replaceAll("\\r\\n", "\n")
                .replaceAll("\\r", "\n")
                .replaceAll("[ \\t]+", " ")
                .trim();

        String[] titles = {
                "【健身建议】",
                "【目标分析】",
                "【训练安排】",
                "【饮食建议】",
                "【注意事项】",
                "【预约提示】",
                "【预约成功】",
                "【预约信息补充】",
                "【暂不支持】"
        };

        for (String title : titles) {
            result = result.replace(title, "\n\n" + title + "\n\n");
        }

        result = result.replaceAll("(?m)^\\s*1[\\.、]\\s*", "1）");
        result = result.replaceAll("(?m)^\\s*2[\\.、]\\s*", "2）");
        result = result.replaceAll("(?m)^\\s*3[\\.、]\\s*", "3）");
        result = result.replaceAll("(?m)^\\s*4[\\.、]\\s*", "4）");
        result = result.replaceAll("(?m)^\\s*5[\\.、]\\s*", "5）");
        result = result.replaceAll("(?m)^\\s*6[\\.、]\\s*", "6）");
        result = result.replaceAll("(?m)^\\s*7[\\.、]\\s*", "7）");
        result = result.replaceAll("(?m)^\\s*8[\\.、]\\s*", "8）");
        result = result.replaceAll("(?m)^\\s*9[\\.、]\\s*", "9）");

        result = result.replaceAll("(?<=[。！？；])\\s*([1-9]\\d*)[\\.、]\\s*", "\n$1）");

        result = result.replaceAll("(?<=[。！？；])\\s*([1-9]\\d*）)", "\n$1");

        result = result.replaceAll("(?m)^训练频率：", "1）训练频率：");
        result = result.replaceAll("(?m)^有氧训练：", "2）有氧训练：");
        result = result.replaceAll("(?m)^力量训练：", "3）力量训练：");

        result = result.replaceAll("(?m)^热量控制：", "1）热量控制：");
        result = result.replaceAll("(?m)^热量与营养：", "1）热量与营养：");
        result = result.replaceAll("(?m)^蛋白质摄入：", "2）蛋白质摄入：");
        result = result.replaceAll("(?m)^主食与蔬菜：", "3）主食与蔬菜：");
        result = result.replaceAll("(?m)^主食与脂肪：", "3）主食与脂肪：");
        result = result.replaceAll("(?m)^饮食执行：", "2）饮食执行：");
        result = result.replaceAll("(?m)^饮水与习惯：", "4）饮水与习惯：");

        result = result.replaceAll("(?m)^循序渐进：", "1）循序渐进：");
        result = result.replaceAll("(?m)^避免受伤：", "2）避免受伤：");
        result = result.replaceAll("(?m)^恢复与记录：", "2）恢复与记录：");
        result = result.replaceAll("(?m)^记录进度：", "3）记录进度：");
        result = result.replaceAll("(?m)^记录与调整：", "3）记录与调整：");
        result = result.replaceAll("(?m)^坚持执行：", "4）坚持执行：");
        result = result.replaceAll("(?m)^安全第一：", "3）安全第一：");
        result = result.replaceAll("(?m)^保证休息：", "4）保证休息：");

        result = result.replaceAll("(?m)^\\s*[-*+]\\s*", "  · ");

        result = result.replaceAll("(?m)^\\s+(【[^】]+】)", "$1");

        result = result.replaceAll("(?m)\\s+$", "");

        result = result.replaceAll("\n{3,}", "\n\n").trim();

        return result;
    }

    private String polishPlainAnswer(String text) {
        String result = removeReservationSuffix(text);

        if (result.isBlank()) {
            result = "我可以为您提供基础健身建议";
        }

        result = result.trim();

        if (!result.endsWith("。") && !result.endsWith("！") && !result.endsWith("？")) {
            result = result + "。";
        }

        return result;
    }

    private String polishFitnessAnswer(String text) {
        String result = removeReservationSuffix(text);

        if (result.isBlank()) {
            result = "【健身建议】\n\n"
                    + "【目标分析】\n"
                    + "建议先从建立运动习惯开始，不要一开始追求过高强度。\n\n"
                    + "【训练安排】\n"
                    + "1）训练频率：每周安排3到4次训练，每次30到45分钟。\n"
                    + "2）有氧训练：可以选择快走、慢跑、骑车或椭圆机，从低强度开始。\n"
                    + "3）力量训练：可以加入深蹲、俯卧撑、划船、臀桥等基础动作。\n\n"
                    + "【饮食建议】\n"
                    + "1）控制总热量摄入，减少高油、高糖食物。\n"
                    + "2）每餐保证适量蛋白质，比如鸡蛋、鱼肉、鸡胸肉、豆制品。\n\n"
                    + "【注意事项】\n"
                    + "1）训练应循序渐进，避免短时间内强度过大。";
        }

        result = formatAnswerLayout(result);

        if (!result.startsWith("【健身建议】")) {
            result = "【健身建议】\n\n" + result;
        }

        return formatAnswerLayout(result + "\n\n" + APPOINTMENT_TITLE + "\n" + RESERVATION_SUFFIX);
    }


    private List<String> splitSentences(String text) {
        return splitRawSentences(text).stream()
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(s -> {
                    if (!s.endsWith("。") && !s.endsWith("！") && !s.endsWith("？")) {
                        return s + "。";
                    }
                    return s;
                })
                .toList();
    }

    private List<String> splitRawSentences(String text) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (char ch : text.toCharArray()) {
            current.append(ch);
            if (isSentenceEnd(ch)) {
                result.add(current.toString());
                current.setLength(0);
            }
        }
        if (current.length() > 0) {
            result.add(current.toString());
        }
        return result;
    }

    private int findLastSentenceEnd(StringBuilder text) {
        for (int i = text.length() - 1; i >= 0; i--) {
            if (isSentenceEnd(text.charAt(i))) {
                return i;
            }
        }
        return -1;
    }

    private boolean isSentenceEnd(char ch) {
        return ch == '。' || ch == '！' || ch == '？' || ch == '!' || ch == '?' || ch == ';' || ch == '；';
    }

    private SysUser getCurrentUser() {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        SysUser user = sysUserMapper.selectById(currentUserId);
        if (user == null) {
            throw new BusinessException(404, "user not found");
        }
        return user;
    }

    private String findName(String message, SysUser currentUser) {
        Matcher matcher = NAME_PATTERN.matcher(message);
        if (matcher.find()) {
            return matcher.group(1);
        }
        if (!isBlank(currentUser.getRealName())) {
            return currentUser.getRealName();
        }
        return currentUser.getNickname();
    }

    private String findPhone(String message, SysUser currentUser) {
        Matcher matcher = PHONE_PATTERN.matcher(message);
        if (matcher.find()) {
            return matcher.group();
        }
        return currentUser.getPhone();
    }

    private String findGender(String message, SysUser currentUser) {
        if (message.contains("女")) {
            return "女";
        }
        if (message.contains("男")) {
            return "男";
        }
        return currentUser.getGender();
    }

    private Integer findAge(String message, SysUser currentUser) {
        Matcher matcher = AGE_PATTERN.matcher(message);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return currentUser.getAge();
    }

    private String findAppointmentType(String message) {
        if (message.contains("营养")) {
            return "营养咨询";
        }
        if (message.contains("饮食")) {
            return "营养咨询";
        }
        if (message.contains("增肌") || message.contains("减脂") || message.contains("训练") || message.contains("健身")) {
            return "健身咨询";
        }
        return null;
    }

    private LocalDate findDate(String message) {
        if (message.contains("今天")) {
            return LocalDate.now();
        }
        if (message.contains("明天")) {
            return LocalDate.now().plusDays(1);
        }
        if (message.contains("后天")) {
            return LocalDate.now().plusDays(2);
        }

        Matcher dateMatcher = DATE_PATTERN.matcher(message);
        if (dateMatcher.find()) {
            return LocalDate.of(
                    Integer.parseInt(dateMatcher.group(1)),
                    Integer.parseInt(dateMatcher.group(2)),
                    Integer.parseInt(dateMatcher.group(3))
            );
        }

        Matcher monthDayMatcher = MONTH_DAY_PATTERN.matcher(message);
        if (monthDayMatcher.find()) {
            return LocalDate.of(
                    LocalDate.now().getYear(),
                    Integer.parseInt(monthDayMatcher.group(1)),
                    Integer.parseInt(monthDayMatcher.group(2))
            );
        }
        return null;
    }

    private LocalTime findTime(String message) {
        Matcher matcher = TIME_PATTERN.matcher(message);
        if (!matcher.find()) {
            return null;
        }
        int hour = Integer.parseInt(matcher.group(1));
        String minutePart = matcher.group(2);
        int minute = minutePart == null || minutePart.isBlank() ? 0 : Integer.parseInt(minutePart);
        return LocalTime.of(hour, minute);
    }

    private String normalizeMessage(String message) {
        if (message == null) {
            return "";
        }
        return message.trim();
    }

    private String defaultText(String value) {
        return isBlank(value) ? "未知" : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static class AppointmentParseResult {
        private boolean bookingIntent;
        private String name;
        private String phone;
        private String gender;
        private Integer age;
        private String appointmentType;
        private LocalDate reserveDate;
        private LocalTime reserveTime;
        private String remark;
        private final List<String> missingFields = new ArrayList<>();

        private boolean canCreateAppointment() {
            return bookingIntent && missingFields.isEmpty();
        }

        private AppointmentCreateRequest toCreateRequest() {
            AppointmentCreateRequest request = new AppointmentCreateRequest();
            request.setName(name);
            request.setPhone(phone);
            request.setGender(gender);
            request.setAge(age);
            request.setAppointmentType(appointmentType);
            request.setReserveDate(reserveDate);
            request.setReserveTime(reserveTime);
            request.setRemark(remark);
            return request;
        }
    }
}
