package com.aining.service.impl;

import com.aining.dto.AnalyzeRequestDTO;
import com.aining.dto.NamingRequestDTO;
import com.aining.entity.HotName;
import com.aining.entity.NameResult;
import com.aining.entity.NamingRecord;
import com.aining.mapper.HotNameMapper;
import com.aining.mapper.NameResultMapper;
import com.aining.mapper.NamingRecordMapper;
import com.aining.mapper.UserFavoriteMapper;
import com.aining.service.NamingService;
import com.aining.utils.OpenAiClient;
import com.aining.vo.NameResultVO;
import com.aining.vo.StreamTaskVO;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class NamingServiceImpl implements NamingService {

    @Autowired
    private NamingRecordMapper namingRecordMapper;

    @Autowired
    private NameResultMapper nameResultMapper;

    @Autowired
    private HotNameMapper hotNameMapper;

    @Autowired
    private UserFavoriteMapper userFavoriteMapper;

    @Autowired
    private OpenAiClient openAiClient;

    @Autowired
    private Executor executor;

    // 内存级流式任务缓存
    private final ConcurrentHashMap<String, StringBuilder> taskBuffers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Boolean> taskDone = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, CopyOnWriteArrayList<SseEmitter>> taskEmitters = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Object> taskLocks = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "analyze-cleanup");
        t.setDaemon(true);
        return t;
    });

    @PreDestroy
    public void destroy() {
        cleanupExecutor.shutdownNow();
    }

    @Override
    public List<NameResultVO> generateNames(Long userId, NamingRequestDTO dto) {
        NamingRecord record = new NamingRecord();
        record.setUserId(userId);
        record.setFatherSurname(dto.getFatherSurname());
        record.setMotherSurname(dto.getMotherSurname());
        record.setGender(dto.getGender());
        record.setPrompt(dto.getPrompt());
        record.setStyle(dto.getStyle());
        namingRecordMapper.insert(record);

        String systemPrompt = "你是一位精通中国传统文化和现代语言学的顶级取名大师。请严格按照JSON数组格式返回结果，不要包含任何解释性文字。";
        String userPrompt = buildNamingPrompt(dto);

        String aiResponse;
        try {
            aiResponse = openAiClient.chatCompletion(systemPrompt, userPrompt);
        } catch (Exception e) {
            log.error("AI取名失败", e);
            aiResponse = generateDefaultNames(dto);
        }

        List<NameResult> results = parseNameResults(aiResponse, record.getId());
        if (results.isEmpty()) {
            results = parseNameResults(generateDefaultNames(dto), record.getId());
        }

        for (NameResult result : results) {
            nameResultMapper.insert(result);
        }

        return results.stream()
                .map(r -> convertToVO(r, userId))
                .collect(Collectors.toList());
    }

    @Override
    public StreamTaskVO startDeepAnalyze(AnalyzeRequestDTO dto) {
        String taskId = UUID.randomUUID().toString();
        Object lock = taskLocks.computeIfAbsent(taskId, k -> new Object());
        synchronized (lock) {
            taskBuffers.put(taskId, new StringBuilder());
            taskDone.put(taskId, false);
            taskEmitters.put(taskId, new CopyOnWriteArrayList<>());
        }
        executor.execute(() -> runDeepAnalyzeAsync(taskId, dto));
        StreamTaskVO vo = new StreamTaskVO();
        vo.setTaskId(taskId);
        return vo;
    }

    private void runDeepAnalyzeAsync(String taskId, AnalyzeRequestDTO dto) {
        try {
            String systemPrompt = "你是一位精通易经、五行、音韵学和现代心理学的取名分析大师。请对以下名字进行深度分析，从易经八卦、五行八字、字形美学、音韵节奏、心理暗示等角度进行解读。";
            String userPrompt = String.format(
                    "请深度分析名字：%s\n性别：%s\n父姓：%s\n母姓：%s",
                    dto.getName(),
                    dto.getGender(),
                    dto.getFatherSurname(),
                    dto.getMotherSurname() != null ? dto.getMotherSurname() : "无"
            );

            Flux<String> flux = openAiClient.chatCompletionStream(systemPrompt, userPrompt);

            flux.subscribe(
                    chunk -> {
                        if (chunk != null && !chunk.isEmpty()) {
                            Object lock = taskLocks.get(taskId);
                            synchronized (lock != null ? lock : new Object()) {
                                taskBuffers.computeIfAbsent(taskId, k -> new StringBuilder()).append(chunk);
                                CopyOnWriteArrayList<SseEmitter> emitters = taskEmitters.get(taskId);
                                if (emitters != null) {
                                    for (SseEmitter emitter : emitters) {
                                        try {
                                            emitter.send(chunk);
                                        } catch (Exception e) {
                                            emitters.remove(emitter);
                                        }
                                    }
                                }
                            }
                        }
                    },
                    error -> {
                        log.error("深度分析流式调用失败, taskId={}", taskId, error);
                        Object lock = taskLocks.get(taskId);
                        synchronized (lock != null ? lock : new Object()) {
                            taskBuffers.computeIfAbsent(taskId, k -> new StringBuilder())
                                    .append("\n\n分析服务异常，请稍后重试。");
                            finishTask(taskId);
                        }
                    },
                    () -> {
                        Object lock = taskLocks.get(taskId);
                        synchronized (lock != null ? lock : new Object()) {
                            finishTask(taskId);
                        }
                    }
            );
        } catch (Exception e) {
            log.error("深度分析失败, taskId={}", taskId, e);
            Object lock = taskLocks.get(taskId);
            synchronized (lock != null ? lock : new Object()) {
                taskBuffers.computeIfAbsent(taskId, k -> new StringBuilder())
                        .append("\n\n分析服务异常，请稍后重试。");
                finishTask(taskId);
            }
        }
    }

    private void finishTask(String taskId) {
        taskDone.put(taskId, true);
        CopyOnWriteArrayList<SseEmitter> emitters = taskEmitters.remove(taskId);
        if (emitters != null) {
            for (SseEmitter emitter : emitters) {
                try {
                    emitter.send("[DONE]");
                    emitter.complete();
                } catch (Exception ignored) {
                }
            }
        }
        // 10 分钟后清理内存
        cleanupExecutor.schedule(() -> {
            taskBuffers.remove(taskId);
            taskDone.remove(taskId);
            taskEmitters.remove(taskId);
            taskLocks.remove(taskId);
            log.info("清理深度分析任务缓存, taskId={}", taskId);
        }, 10, TimeUnit.MINUTES);
    }

    @Override
    public String pollStreamContent(String taskId) {
        Object lock = taskLocks.get(taskId);
        synchronized (lock != null ? lock : new Object()) {
            StringBuilder sb = taskBuffers.get(taskId);
            if (sb == null) {
                return Boolean.TRUE.equals(taskDone.get(taskId)) ? "[DONE]" : "";
            }
            String content = sb.toString();
            if (Boolean.TRUE.equals(taskDone.get(taskId))) {
                content += "[DONE]";
                cleanupExecutor.schedule(() -> {
                    taskBuffers.remove(taskId);
                    taskDone.remove(taskId);
                    taskEmitters.remove(taskId);
                    taskLocks.remove(taskId);
                }, 30, TimeUnit.SECONDS);
            }
            return content;
        }
    }

    @Override
    public void streamContent(String taskId, SseEmitter emitter) {
        Object lock = taskLocks.get(taskId);
        if (lock == null) {
            completeEmitter(emitter, "任务不存在或已过期");
            return;
        }
        synchronized (lock) {
            Boolean done = taskDone.get(taskId);
            StringBuilder sb = taskBuffers.get(taskId);

            if (done == null || sb == null) {
                completeEmitter(emitter, "任务不存在或已过期");
                return;
            }

            if (done) {
                completeEmitterWithDone(emitter, sb.toString());
                return;
            }

            CopyOnWriteArrayList<SseEmitter> emitters = taskEmitters.computeIfAbsent(taskId, k -> new CopyOnWriteArrayList<>());
            emitters.add(emitter);

            // 再次检查，防止在 add 前一刻任务刚好完成
            if (Boolean.TRUE.equals(taskDone.get(taskId))) {
                completeEmitterWithDone(emitter, sb.toString());
                emitters.remove(emitter);
                return;
            }

            emitter.onCompletion(() -> emitters.remove(emitter));
            emitter.onTimeout(() -> {
                emitters.remove(emitter);
                completeEmitter(emitter, null);
            });
            emitter.onError((e) -> {
                emitters.remove(emitter);
                try {
                    emitter.completeWithError(e);
                } catch (Exception ignored) {
                }
            });
        }
    }

    private void completeEmitter(SseEmitter emitter, String message) {
        try {
            if (message != null) {
                emitter.send(message);
            }
            emitter.complete();
        } catch (Exception ignored) {
        }
    }

    private void completeEmitterWithDone(SseEmitter emitter, String content) {
        try {
            if (content != null && !content.isEmpty()) {
                emitter.send(content);
            }
            emitter.send("[DONE]");
            emitter.complete();
        } catch (Exception ignored) {
        }
    }

    @Override
    public List<NameResultVO> getHotNames(String gender, Integer limit) {
        LambdaQueryWrapper<HotName> wrapper = new LambdaQueryWrapper<>();
        if (gender != null && !gender.isEmpty()) {
            wrapper.eq(HotName::getGender, gender);
        }
        wrapper.orderByDesc(HotName::getSearchCount);
        wrapper.last("LIMIT " + limit);
        List<HotName> hotNames = hotNameMapper.selectList(wrapper);
        return hotNames.stream().map(h -> {
            NameResultVO vo = new NameResultVO();
            vo.setId(h.getId());
            vo.setName(h.getName());
            vo.setMeaning(h.getMeaning());
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<NameResultVO> compareNames(List<Long> nameResultIds, Long userId) {
        if (nameResultIds == null || nameResultIds.size() < 2) {
            return new ArrayList<>();
        }
        return nameResultIds.stream()
                .map(id -> {
                    NameResult result = nameResultMapper.selectById(id);
                    return result != null ? convertToVO(result, userId) : null;
                })
                .filter(r -> r != null)
                .collect(Collectors.toList());
    }

    private String buildNamingPrompt(NamingRequestDTO dto) {
        return String.format(
                "请为%s宝宝取名，父姓：%s，母姓：%s。%s\n风格：%s。生成%d个名字。\n\n" +
                        "请严格按照以下JSON数组格式返回，不要包含任何markdown标记或其他说明文字：\n" +
                        "[{\"name\":\"名字\",\"pinyin\":\"拼音\",\"meaning\":\"寓意\",\"wuxing\":\"五行分析\",\"yinyunScore\":8,\"timesScore\":8,\"totalScore\":85,\"reason\":\"推荐理由\"}]",
                "boy".equals(dto.getGender()) ? "男" : "女",
                dto.getFatherSurname(),
                dto.getMotherSurname() != null && !dto.getMotherSurname().isEmpty() ? dto.getMotherSurname() : "无",
                dto.getPrompt() != null && !dto.getPrompt().isEmpty() ? "家长期望：" + dto.getPrompt() : "",
                dto.getStyle(),
                dto.getCount()
        );
    }

    private String generateDefaultNames(NamingRequestDTO dto) {
        String surname = dto.getFatherSurname();
        if ("boy".equals(dto.getGender())) {
            return "[{\"name\":\"" + surname + "宇轩\",\"pinyin\":\"" + surname + " Yǔ Xuān\",\"meaning\":\"气宇轩昂，胸怀广阔\",\"wuxing\":\"土-木\",\"yinyunScore\":9,\"timesScore\":8,\"totalScore\":88,\"reason\":\"音韵流畅，寓意积极向上\"},{\"name\":\"" + surname + "子墨\",\"pinyin\":\"" + surname + " Zǐ Mò\",\"meaning\":\"才华横溢，温文尔雅\",\"wuxing\":\"水-土\",\"yinyunScore\":8,\"timesScore\":9,\"totalScore\":87,\"reason\":\"古典雅致，书香气息浓厚\"}]";
        } else {
            return "[{\"name\":\"" + surname + "梓涵\",\"pinyin\":\"" + surname + " Zǐ Hán\",\"meaning\":\"涵养深厚，温婉动人\",\"wuxing\":\"木-水\",\"yinyunScore\":9,\"timesScore\":8,\"totalScore\":89,\"reason\":\"音韵优美，寓意温柔贤淑\"},{\"name\":\"" + surname + "诗雅\",\"pinyin\":\"" + surname + " Shī Yǎ\",\"meaning\":\"诗情画意，高雅脱俗\",\"wuxing\":\"金-木\",\"yinyunScore\":8,\"timesScore\":8,\"totalScore\":86,\"reason\":\"文艺气息浓厚，富有内涵\"}]";
        }
    }

    private List<NameResult> parseNameResults(String aiResponse, Long recordId) {
        List<NameResult> results = new ArrayList<>();
        try {
            String jsonStr = aiResponse.trim();
            if (jsonStr.startsWith("```")) {
                jsonStr = jsonStr.replaceAll("```json", "").replaceAll("```", "").trim();
            }
            JSONArray array = JSON.parseArray(jsonStr);
            for (int i = 0; i < array.size(); i++) {
                JSONObject obj = array.getJSONObject(i);
                NameResult result = new NameResult();
                result.setRecordId(recordId);
                result.setName(obj.getString("name"));
                result.setPinyin(obj.getString("pinyin"));
                result.setMeaning(obj.getString("meaning"));
                result.setWuxing(obj.getString("wuxing"));
                result.setYinyunScore(obj.getInteger("yinyunScore"));
                result.setTimesScore(obj.getInteger("timesScore"));
                result.setTotalScore(obj.getInteger("totalScore"));
                result.setReason(obj.getString("reason"));
                results.add(result);
            }
        } catch (Exception e) {
            log.error("解析AI返回结果失败: {}", aiResponse, e);
        }
        return results;
    }

    private NameResultVO convertToVO(NameResult result, Long userId) {
        NameResultVO vo = new NameResultVO();
        vo.setId(result.getId());
        vo.setName(result.getName());
        vo.setPinyin(result.getPinyin());
        vo.setMeaning(result.getMeaning());
        vo.setWuxing(result.getWuxing());
        vo.setYinyunScore(result.getYinyunScore());
        vo.setTimesScore(result.getTimesScore());
        vo.setTotalScore(result.getTotalScore());
        vo.setReason(result.getReason());
        if (userId != null) {
            Integer count = userFavoriteMapper.countByUserAndName(userId, result.getId());
            vo.setIsFavorited(count != null && count > 0);
        }
        return vo;
    }
}
