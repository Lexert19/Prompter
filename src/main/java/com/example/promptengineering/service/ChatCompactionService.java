package com.example.promptengineering.service;

import com.example.promptengineering.dto.UserFileDTO;
import com.example.promptengineering.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatCompactionService {

    public record ExportResult(Long fileId, String fileName, long sizeBytes,
            int keptRounds, int removedRounds) {
    }

    private record Round(int index, SessionMessage user, List<SessionMessage> rest) {
    }

    private final FileStorageService fileStorage;

    public ExportResult export(User user, ChatSession session, int keepRounds) {
        List<Round> rounds = groupIntoRounds(session.getMessages());

        if (rounds.isEmpty()) {
            throw new IllegalArgumentException("No user messages found to export.");
        }

        int actualKeep = Math.min(rounds.size(), keepRounds);

        List<Integer> selected = selectRoundsAlgorithmically(rounds, actualKeep);
        String txt = renderTxt(rounds, selected);
        byte[] bytes = txt.getBytes(StandardCharsets.UTF_8);

        String fname = "chat-export-" + DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")
                .format(LocalDateTime.now()) + ".txt";

        UserFileDTO saved = fileStorage.saveText(user, fname, bytes);

        return new ExportResult(saved.getId(), saved.getFileName(), saved.getSize(),
                selected.size(), rounds.size() - selected.size());
    }

    private List<Round> groupIntoRounds(List<SessionMessage> all) {
        List<Round> result = new ArrayList<>();
        int i = 0;
        while (i < all.size()) {
            if (!"user".equals(all.get(i).getRole())) {
                i++;
                continue;
            }
            int start = i++;
            while (i < all.size() && !"user".equals(all.get(i).getRole()))
                i++;
            result.add(new Round(result.size() + 1, all.get(start),
                    new ArrayList<>(all.subList(start + 1, i))));
        }
        return result;
    }

    private List<Integer> selectRoundsAlgorithmically(List<Round> rounds, int keep) {
        Set<Integer> forced = new HashSet<>();
        int totalRounds = rounds.size();

        forced.add(1);
        if (totalRounds >= 2)
            forced.add(totalRounds - 1);
        forced.add(totalRounds);

        if (forced.size() >= keep) {
            return forced.stream().sorted().limit(keep).toList();
        }

        List<Round> scoredRounds = rounds.stream()
                .filter(r -> !forced.contains(r.index()))
                .sorted(Comparator.comparingInt(this::calculateScore).reversed())
                .toList();

        List<Integer> selected = new ArrayList<>(forced);

        for (Round r : scoredRounds) {
            if (selected.size() >= keep)
                break;
            selected.add(r.index());
        }

        return selected.stream().sorted().toList();
    }

    private int calculateScore(Round round) {
        int score = 0;

        String userText = safe(round.user().getText());
        if (userText.length() > 200)
            score += 10;
        if (userText.length() > 500)
            score += 20;

        String lowerText = userText.toLowerCase();
        if (lowerText.contains("error") || lowerText.contains("błąd")
                || lowerText.contains("exception") || lowerText.contains("fail")) {
            score += 30;
        }
        if (lowerText.contains("must") || lowerText.contains("wymaganie")
                || lowerText.contains("musi") || lowerText.contains("funkcja")) {
            score += 20;
        }
        if (userText.contains("```"))
            score += 40;

        for (SessionMessage m : round.rest()) {
            if ("tool".equals(m.getRole())) {
                score += 15;
            }

            String aiText = safe(m.getText());
            if (aiText.contains("```"))
                score += 20;

            if (aiText.length() < 50 && "assistant".equals(m.getRole())) {
                score -= 10;
            }
        }

        return score;
    }

    private String renderTxt(List<Round> rounds, List<Integer> selected) {
        StringBuilder sb = new StringBuilder();
        sb.append("Chat export — selected rounds\n").append("Generated: ")
                .append(Instant.now()).append('\n').append("Kept ")
                .append(selected.size()).append(" of ").append(rounds.size())
                .append(" rounds\n\n");

        String sep = "=".repeat(60);
        for (int idx : selected) {
            Round r = rounds.get(idx - 1);
            sb.append(sep).append('\n').append("ROUND ").append(idx).append('\n')
                    .append(sep).append('\n').append("\n--- USER ---\n")
                    .append(safe(r.user().getText())).append('\n');

            if (r.user().getDocuments() != null && !r.user().getDocuments().isEmpty()) {
                for (String doc : r.user().getDocuments()) {
                    sb.append("<files>\n").append(doc).append("\n</files>\n");
                }
            }
            sb.append(safe(r.user().getText())).append('\n');

            for (SessionMessage m : r.rest()) {
                switch (m.getRole()) {
                    case "assistant" -> {
                        var pm = m.getProviderMessage();
                        if (pm.getToolCalls() != null) {
                            for (var tc : pm.getToolCalls()) {
                                sb.append("\n[tool_call: ").append(tc.getName())
                                        .append(' ').append(safe(tc.getArguments()))
                                        .append("]\n");
                            }
                        }
                        String t = m.getText();
                        if (t != null && !t.isBlank())
                            sb.append("\n--- ASSISTANT ---\n").append(t).append('\n');
                    }
                    case "tool" -> sb.append("\n[tool_result: ")
                            .append(m.getProviderMessage().getToolName()).append("] ")
                            .append(trunc(safe(m.getStringContent()), 500)).append('\n');
                }
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }
    private static String trunc(String s, int n) {
        return s.length() <= n ? s : s.substring(0, n) + "…";
    }
}
