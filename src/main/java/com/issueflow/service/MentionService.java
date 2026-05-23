package com.issueflow.service;

import com.issueflow.dto.response.CommentResponse;
import com.issueflow.dto.response.MentionedUserResponse;
import com.issueflow.dto.response.MentionsPaginatedResponse;
import com.issueflow.entity.Comment;
import com.issueflow.entity.Mention;
import com.issueflow.entity.User;
import com.issueflow.repository.MentionRepository;
import com.issueflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MentionService {

    private final MentionRepository mentionRepo;
    private final UserRepository userRepo;

    private static final Pattern MENTION_PATTERN = Pattern.compile("@([a-zA-Z0-9_]+)");

    @Transactional
    public List<MentionedUserResponse> extractAndSaveMentions(Comment comment, String content) {
        List<MentionedUserResponse> mentionedUsers = new ArrayList<>();
        Matcher matcher = MENTION_PATTERN.matcher(content);

        while (matcher.find()) {
            String username = matcher.group(1);
            userRepo.findByUsername(username).ifPresent(user -> {
                Mention mention = new Mention();
                mention.setComment(comment);
                mention.setMentionedUser(user);
                mentionRepo.save(mention);
                mentionedUsers.add(MentionedUserResponse.from(user));
            });
        }

        return mentionedUsers;
    }

    public List<MentionedUserResponse> getMentionsByComment(Comment comment) {
        return mentionRepo.findAll().stream()
            .filter(m -> m.getComment().getId().equals(comment.getId()))
            .map(m -> MentionedUserResponse.from(m.getMentionedUser()))
            .collect(Collectors.toList());
    }

    public MentionsPaginatedResponse getMentionsByUser(Long userId, Integer page, Integer pageSize) {
        int currentPage = page != null ? page : 1;
        int size = pageSize != null ? pageSize : 20;

        Pageable pageable = PageRequest.of(currentPage - 1, size);
        Page<Mention> mentionsPage = mentionRepo.findByMentionedUserId(userId, pageable);

        List<CommentResponse> comments = mentionsPage.getContent().stream()
            .map(m -> {
                Comment comment = m.getComment();
                List<MentionedUserResponse> mentions = getMentionsByComment(comment);
                return CommentResponse.from(comment, mentions);
            })
            .collect(Collectors.toList());

        return new MentionsPaginatedResponse(
            comments,
            mentionsPage.getTotalElements(),
            currentPage
        );
    }
}
