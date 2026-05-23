package com.issueflow.service;

import com.issueflow.dto.response.MentionedUserResponse;
import com.issueflow.dto.response.MentionsPaginatedResponse;
import com.issueflow.entity.Comment;
import com.issueflow.entity.Mention;
import com.issueflow.entity.Ticket;
import com.issueflow.entity.User;
import com.issueflow.repository.MentionRepository;
import com.issueflow.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MentionServiceTest {

    @Mock
    private MentionRepository mentionRepo;

    @Mock
    private UserRepository userRepo;

    @InjectMocks
    private MentionService mentionService;

    private User mentionedUser;
    private Comment testComment;

    @BeforeEach
    void setUp() {
        mentionedUser = new User();
        mentionedUser.setId(1L);
        mentionedUser.setUsername("jdoe");
        mentionedUser.setFullName("John Doe");

        User author = new User();
        author.setId(2L);
        author.setUsername("author");
        author.setFullName("Comment Author");

        Ticket testTicket = new Ticket();
        testTicket.setId(1L);

        testComment = new Comment();
        testComment.setId(1L);
        testComment.setContent("Hey @jdoe check this out");
        testComment.setTicket(testTicket);
        testComment.setAuthor(author);
    }

    @Test
    void testExtractAndSaveMentions() {
        when(userRepo.findByUsername("jdoe")).thenReturn(Optional.of(mentionedUser));
        when(mentionRepo.save(any(Mention.class))).thenAnswer(i -> i.getArgument(0));

        List<MentionedUserResponse> mentions = mentionService.extractAndSaveMentions(
                testComment, 
                "Hey @jdoe check this out"
        );

        assertNotNull(mentions);
        assertEquals(1, mentions.size());
        assertEquals("jdoe", mentions.get(0).getUsername());
        verify(mentionRepo, times(1)).save(any(Mention.class));
    }

    @Test
    void testExtractMultipleMentions() {
        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("asmith");
        user2.setFullName("Alice Smith");

        when(userRepo.findByUsername("jdoe")).thenReturn(Optional.of(mentionedUser));
        when(userRepo.findByUsername("asmith")).thenReturn(Optional.of(user2));
        when(mentionRepo.save(any(Mention.class))).thenAnswer(i -> i.getArgument(0));

        List<MentionedUserResponse> mentions = mentionService.extractAndSaveMentions(
                testComment,
                "Hey @jdoe and @asmith check this"
        );

        assertEquals(2, mentions.size());
        verify(mentionRepo, times(2)).save(any(Mention.class));
    }

    @Test
    void testExtractMentionsNonExistentUser() {
        when(userRepo.findByUsername("unknown")).thenReturn(Optional.empty());

        List<MentionedUserResponse> mentions = mentionService.extractAndSaveMentions(
                testComment,
                "Hey @unknown user"
        );

        assertTrue(mentions.isEmpty());
        verify(mentionRepo, never()).save(any());
    }

    @Test
    void testGetMentionsByUser() {
        Mention mention = new Mention();
        mention.setId(1L);
        mention.setComment(testComment);
        mention.setMentionedUser(mentionedUser);

        Page<Mention> mentionsPage = new PageImpl<>(Arrays.asList(mention));
        when(mentionRepo.findByMentionedUserId(eq(1L), any(Pageable.class))).thenReturn(mentionsPage);
        when(mentionRepo.findAll()).thenReturn(Arrays.asList(mention));

        MentionsPaginatedResponse response = mentionService.getMentionsByUser(1L, 1, 20);

        assertNotNull(response);
        assertEquals(1, response.getTotal());
        assertEquals(1, response.getPage());
        assertNotNull(response.getData());
        assertEquals(1, response.getData().size());
    }
}
