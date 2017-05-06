package forum.controller;

import forum.bindingModel.ForumBindingModel;
import forum.entity.*;
import forum.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import forum.bindingModel.TopicBindingModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ForumController {
    @Autowired
    private ForumRepository forumRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private TopicRepository topicRepository;
    @Autowired
    private ReplyRepository replyRepository;

    //// tozi list go iztrii
    @GetMapping("/admin/forums")
    public String list(Model model){

        List<Forum> forums = this.forumRepository.findAll();
        forums = forums.stream()
                .sorted(Comparator.comparingInt(Forum::getId))
                .collect(Collectors.toList());
        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = this.userRepository.findByEmail(principal.getUsername());

        model.addAttribute("user", user);
        model.addAttribute("forums", forums);
        return "admin/forum/list";
    }

    @GetMapping("/forum/{id}")
    public String details(Model model, @PathVariable Integer id){
        if (!this.forumRepository.exists(id)) {
            return "redirect:/";
        }

        if(!(SecurityContextHolder.getContext().getAuthentication()
                instanceof AnonymousAuthenticationToken)){
            UserDetails principal = (UserDetails) SecurityContextHolder.getContext()
                    .getAuthentication().getPrincipal();

            User entityUser = this.userRepository.findByEmail(principal.getUsername());

            model.addAttribute("user", entityUser);
        }

        Forum forum = this.forumRepository.findOne(id);

        List<Topic> topics = this.topicRepository.findByForum(forum);
        topics.stream().sorted((object1, object2) -> object1.getId().compareTo(object2.getId()));
        Collections.reverse(topics);

        /// get categoryId of the category, then get the category of this categoryId and then Inject it to thymeleaf
        /// by this you can create a correct back button in the thymeleaf view
        Integer categoryId = forum.getCategory().getId();
        Category category = this.categoryRepository.findOne(categoryId);


        model.addAttribute("category", category);
        model.addAttribute("topics", topics);
        model.addAttribute("forum", forum);


        return "home/forum-details";
    }

    @GetMapping("/admin/forum/create")
    @PreAuthorize("isAuthenticated()")
    public String create(Model model){

        List<Category> categories = this.categoryRepository.findAll();
        model.addAttribute("categories", categories);
        return "admin/forum/create";
    }

    @PostMapping("/admin/forum/create")
    @PreAuthorize("isAuthenticated()")
    public String createProcess(ForumBindingModel forumBindingModel){
        UserDetails user = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        User userEntity = this.userRepository.findByEmail(user.getUsername());
        Category category = this.categoryRepository.findOne(forumBindingModel.getCategoryId());
        Forum forumEntity = new Forum(
                forumBindingModel.getTitle(),
                forumBindingModel.getDescription(),
                category
        );

        this.forumRepository.saveAndFlush(forumEntity);

        return "redirect:/admin/forums";
    }

    @GetMapping("/admin/forum/edit/{id}")
    @PreAuthorize("isAuthenticated()")
    public String edit(@PathVariable Integer id, Model model){
        if(!this.forumRepository.exists(id)){
            return "redirect:/";
        }
        Forum forum = this.forumRepository.findOne(id);

        if(!isUserAdmin(forum)){
            return "admin/forum/list";
        }
        List<Category> categories = this.categoryRepository.findAll();
        //model.addAttribute("view", "topic/edit");
        model.addAttribute("forum", forum);
        model.addAttribute("categories", categories);

        return "admin/forum/edit";
    }

    @PostMapping("/admin/forum/edit/{id}")
    @PreAuthorize("isAuthenticated()")
    public String editProcess(@PathVariable Integer id, ForumBindingModel forumBindingModel){
        if(!this.forumRepository.exists(id)){
            return "redirect:/";
        }

        Forum forum = this.forumRepository.findOne(id);
        Category category = this.categoryRepository.findOne(forumBindingModel.getCategoryId());
        if(!isUserAdmin(forum)){
            return "admin/forum/list";
        }

        forum.setDescription(forumBindingModel.getDescription());
        forum.setTitle(forumBindingModel.getTitle());
        forum.setCategory(category);

        this.forumRepository.saveAndFlush(forum);

        return "redirect:/admin/forums";
    }
    @GetMapping("/admin/forum/delete/{id}")
    @PreAuthorize("isAuthenticated()")
    public String delete(@PathVariable Integer id, Model model){
        if(!this.forumRepository.exists(id)){
            return "redirect:/";
        }
        Forum forum = this.forumRepository.findOne(id);

        if(!isUserAdmin(forum)){
            return "admin/forum/list";
        }

        model.addAttribute("forum", forum);
        return "admin/forum/delete";
    }

    @PostMapping("/admin/forum/delete/{id}")
    @PreAuthorize("isAuthenticated()")
    public String deleteProcess(@PathVariable Integer id, ForumBindingModel forumBindingModel){
        if(!this.forumRepository.exists(id)){
            return "redirect:/";
        }

        Forum forum = this.forumRepository.findOne(id);
        if(!isUserAdmin(forum)){
            return "admin/forum/list";
        }

        for (Topic topic: forum.getTopics()){
            // delete topic replies:
            List<Reply> replyList = this.replyRepository.findByTopic(topic);
            for (Reply reply: replyList) {
                this.replyRepository.delete(reply);
            }
            // delete topic itself
            this.topicRepository.delete(topic);
        }

        this.forumRepository.delete(forum);

        return "redirect:/admin/forums";
    }
    private boolean isUserAdmin(Forum forum){
        UserDetails user = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        User userEntity = this.userRepository.findByEmail(user.getUsername());
        return userEntity.isAdmin();
    }

}
