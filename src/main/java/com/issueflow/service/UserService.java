package com.issueflow.service;

import com.issueflow.dto.request.UpdateUserRequest;
import com.issueflow.dto.response.UserResponse;
import com.issueflow.entity.User;
import com.issueflow.exception.ResourceNotFoundException;
import com.issueflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepo;

    public UserResponse getUserById(Long id) {
        User user = userRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return UserResponse.from(user);
    }

    public List<UserResponse> getAllUsers() {
        return userRepo.findAll().stream()
            .map(UserResponse::from)
            .collect(Collectors.toList());
    }

    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest req) {
        User user = userRepo.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        if (req.getFullName() != null) {
            user.setFullName(req.getFullName());
        }
        if (req.getRole() != null) {
            user.setRole(req.getRole());
        }

        return UserResponse.from(userRepo.save(user));
    }

    public void deleteUser(Long id) {
        if (!userRepo.existsById(id)) {
            throw new ResourceNotFoundException("User", "id", id);
        }
        userRepo.deleteById(id);
    }
}
