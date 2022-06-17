package com.example.cheerik.controller;

import com.example.cheerik.dto.CommentDto;
import com.example.cheerik.dto.PostCommentsDto;
import com.example.cheerik.dto.PostDto;
import com.example.cheerik.dto.UserDto;
import com.example.cheerik.model.User;
import com.example.cheerik.service.CommentService;
import com.example.cheerik.service.PostService;
import com.example.cheerik.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/profile")
public class UserProfileController {
    @Autowired
    private UserService userService;
    @Autowired
    private PostService postService;
    @Autowired
    private CommentService commentService;

    @GetMapping
    public String getPosts(Model model, @AuthenticationPrincipal UserDetails userDetails,
                           @RequestParam(defaultValue = "1") int page,
                           @RequestParam(defaultValue = "5") int size) {
        var user = userService.findByLogin(userDetails.getUsername());
        Pageable pageable = PageRequest.of(page -1,size, Sort.by("id").descending());
        final Page<PostDto> posts = postService.findAllByUser(pageable, user);
        final int totalPages = posts.getTotalPages();
        final List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                .boxed()
                .toList();
        model.addAttribute("pages", pageNumbers);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("userDto", new UserDto(user));
        model.addAttribute("commentDto", new CommentDto());
        model.addAttribute("posts", posts);
        return "profile";
    }
    @GetMapping("/create-post")
    public String createPost(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        model.addAttribute("postDto", new PostDto());
        return "create-post";
    }
    @PostMapping("/create-post")
    public String createPost(@AuthenticationPrincipal UserDetails userDetails,  @ModelAttribute @Valid PostDto postDto,
                             BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("errors", bindingResult.getAllErrors());
            return "create-post";
        }
        var user = userService.findByLogin(userDetails.getUsername());
        postDto.setUser(new UserDto(user));
        postService.createPost(postDto);

        return "redirect:/profile";
    }
    @GetMapping("/edit-post/{id}")
    public String editPost(@PathVariable() Long id,
                               Model model) {
        model.addAttribute("postId", id);
        model.addAttribute("postDto", new PostDto(postService.findPost(id)));

        return "edit-post";
    }

    @PostMapping("edit-post/{id}")
    public String editPost(@PathVariable() Long id,
                               @ModelAttribute @Valid PostDto postDto,
                               BindingResult bindingResult,
                               Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("errors", bindingResult.getAllErrors());
            return "edit-post";
        }

        postService.updatePost(postDto);

        return "redirect:/profile";
    }
    @PostMapping("/delete-post/{id}")
    public String deletePosition(@PathVariable Long id) {
        postService.deletePost(id);
        return "redirect:/profile";
    }
    @GetMapping("/edit-profile")
    public String getProfile(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        var user = userService.findByLogin(userDetails.getUsername());
        model.addAttribute("userDto", new UserDto(user));

        return "edit-profile";
    }

    @PostMapping("/edit-profile")
    public String updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String password) {
        var user = userService.findByLogin(userDetails.getUsername());
        userService.updateUser(user, password);
        return "redirect:/profile";
    }
    @PostMapping("/add-comment/{id}")
    public String addComment(@PathVariable Long id,@AuthenticationPrincipal UserDetails userDetails,
                             @ModelAttribute @Valid CommentDto commentDto,
                             BindingResult bindingResult, Model model){
        if (bindingResult.hasErrors()) {
            model.addAttribute("errors", bindingResult.getAllErrors());
            return "profile";
        }
        var user = userService.findByLogin(userDetails.getUsername());
        commentDto.setPost(postService.findPost(id));
        commentDto.setUser(user);
        commentService.createComment(commentDto);

        return "redirect:/profile";
    }
}
