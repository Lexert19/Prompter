package com.example.promptengineering.service;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;

public class StreamingStats {

    private static final long WINDOW_MS = 2000;
    private static final long MIN_WINDOW_MS = 1000;

    private final long processStartMs = System.currentTimeMillis();
    private final ArrayDeque<long[]> samples = new ArrayDeque<>();

    private volatile long llmStartMs = 0;
    private volatile long llmElapsedMs = 0;

    private volatile long contentChars = 0;
    private volatile long reasoningChars = 0;
    private volatile long charsSinceLastEmit = 0;
    private volatile long lastEmitMs = processStartMs;

    private volatile long lastReasoningAtMs = 0;
    private volatile long lastContentAtMs = 0;

    private long charsInWindow = 0;

    public void beginLlmCall() {
        llmStartMs = System.currentTimeMillis();
    }

    public void endLlmCall() {
        if (llmStartMs > 0) {
            llmElapsedMs += System.currentTimeMillis() - llmStartMs;
            llmStartMs = 0;
        }
    }

    public long currentLlmElapsed() {
        return llmStartMs > 0
                ? llmElapsedMs + (System.currentTimeMillis() - llmStartMs)
                : llmElapsedMs;
    }

    public boolean isCurrentlyThinking() {
        long now = System.currentTimeMillis();
        return lastReasoningAtMs > 0 && lastReasoningAtMs >= lastContentAtMs
                && (now - lastReasoningAtMs) < 400;
    }

    public void addContent(int n) {
        long now = System.currentTimeMillis();
        lastContentAtMs = now;
        contentChars += n;
        charsSinceLastEmit += n;
        addSample(now, n);
    }

    public void addReasoning(int n) {
        long now = System.currentTimeMillis();
        lastReasoningAtMs = now;
        reasoningChars += n;
        charsSinceLastEmit += n;
        addSample(now, n);
    }

    public boolean shouldEmit() {
        return System.currentTimeMillis() - lastEmitMs >= 500 && charsSinceLastEmit > 0;
    }

    public void markEmit() {
        lastEmitMs = System.currentTimeMillis();
        charsSinceLastEmit = 0;
    }

    public Map<String, Object> snapshot() {
        long wallMs = Math.max(System.currentTimeMillis() - processStartMs, 1);
        long llmMs = Math.max(currentLlmElapsed(), 1);

        double cTok = contentChars / 4.0;
        double rTok = reasoningChars / 4.0;
        double total = cTok + rTok;

        Map<String, Object> m = new HashMap<>();
        m.put("elapsedMs", wallMs);
        m.put("llmElapsedMs", llmMs);
        m.put("contentChars", contentChars);
        m.put("reasoningChars", reasoningChars);
        m.put("contentTokens", Math.round(cTok));
        m.put("reasoningTokens", Math.round(rTok));
        m.put("totalTokens", Math.round(total));
        m.put("tps", round2(currentTps()));
        m.put("avgTps", round2(total * 1000.0 / llmMs));
        m.put("inThinking", isCurrentlyThinking());
        return m;
    }

    private void addSample(long now, int chars) {
        samples.addLast(new long[]{now, chars});
        charsInWindow += chars;
        trimSamples(now);
    }

    private void trimSamples(long now) {
        while (!samples.isEmpty() && now - samples.peekFirst()[0] > WINDOW_MS) {
            charsInWindow -= samples.pollFirst()[1];
        }
    }

    private double currentTps() {
        long now = System.currentTimeMillis();
        trimSamples(now);
        if (samples.isEmpty())
            return 0.0;

        long windowMs = now - samples.peekFirst()[0];
        long useMs = Math.max(windowMs, MIN_WINDOW_MS);
        return (charsInWindow / 4.0) * 1000.0 / useMs;
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
