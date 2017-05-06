package forum.controller;

import forum.bindingModel.TopicBindingModel;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Controller
public class TopicController {
    @Autowired
    private TopicRepository topicRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ForumRepository forumRepository;
    @Autowired
    private ReplyRepository replyRepository;

    @GetMapping("/topic/create")
    @PreAuthorize("isAuthenticated()")
    public String create(Model model){

        List<Forum> forums = this.forumRepository.findAll();
        model.addAttribute("forums", forums);
        return "topic/create";
    }

    @PostMapping("/topic/create")
    @PreAuthorize("isAuthenticated()")
    public String createProcess(TopicBindingModel topicBindingModel, RedirectAttributes redirectAttributes){
        UserDetails user = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        User userEntity = this.userRepository.findByEmail(user.getUsername());
        Forum forum = this.forumRepository.findOne(topicBindingModel.getForumId());
        Integer visits = 0;
        Topic topicEntity = new Topic(
                topicBindingModel.getTitle(),
                topicBindingModel.getDescription(),
                userEntity,
                forum,
                visits
        );
        topicEntity.setLastReply("");

        this.topicRepository.saveAndFlush(topicEntity);
        redirectAttributes.addFlashAttribute("newTopic", "New topic was successfully created !");
        return "redirect:/";
    }

    @GetMapping("/topic/{id}")
    public String details(Model model, @PathVariable Integer id){


        if (!this.topicRepository.exists(id)) {
            return "redirect:/";
        }

        if(!(SecurityContextHolder.getContext().getAuthentication()
                instanceof AnonymousAuthenticationToken)){
            UserDetails principal = (UserDetails) SecurityContextHolder.getContext()
                    .getAuthentication().getPrincipal();

            User entityUser = this.userRepository.findByEmail(principal.getUsername());

            model.addAttribute("user", entityUser);
        }
        Topic topic = this.topicRepository.findOne(id);

        /// get forumId of the topic, then get the forum of this forumId and then Inject it to thymeleaf
        /// by this you can create a correct back button in the thymeleaf view
        Integer forumId = topic.getForum().getId();
        Forum forum = this.forumRepository.findOne(forumId);

        List<Reply> replies = this.replyRepository.findByTopic(topic);
        replies.stream().sorted((object1, object2) -> object1.getId().compareTo(object2.getId()));
        Collections.reverse(replies);

        // Set last reply in a topic
        if(replies.size()!=0){
            Reply lastReply = replies.get(0);
            topic.setLastReply(lastReply.getDateUpdated()+" by "+ lastReply.getAuthor().getFullName());
        }
        if(replies.size()==0){
            topic.setLastReply("");
        }

       // System.out.println(replies.get(0).getAuthor().getFullName() + replies.get(0).getDateUpdated());

        Integer visits = topic.getVisits()+1;
        topic.setVisits(visits);

        this.topicRepository.saveAndFlush(topic);


        model.addAttribute("replies", replies);
        model.addAttribute("topic", topic);
        model.addAttribute("forum", forum);  // inject forum to the thymeleaf view


        return "topic/details";
    }

    @GetMapping("/topic/edit/{id}")
    @PreAuthorize("isAuthenticated()")
    public String edit(@PathVariable Integer id, Model model){
        if(!this.topicRepository.exists(id)){
            return "redirect:/";
        }
        Topic topic = this.topicRepository.findOne(id);

        if(!isUserAuthorOrAdmin(topic)){
            return "redirect:/topic/" + id;
        }
        List<Forum> forums = this.forumRepository.findAll();
        //model.addAttribute("view", "topic/edit");
        model.addAttribute("topic", topic);
        model.addAttribute("forums", forums);

        return "topic/edit";
    }

    @PostMapping("/topic/edit/{id}")
    @PreAuthorize("isAuthenticated()")
    public String editProcess(@PathVariable Integer id, TopicBindingModel topicBindingModel){
        if(!this.topicRepository.exists(id)){
            return "redirect:/";
        }

        Topic topic = this.topicRepository.findOne(id);
        Forum forum = this.forumRepository.findOne(topicBindingModel.getForumId());
        if(!isUserAuthorOrAdmin(topic)){
            return "redirect:/topic/" + id;
        }

        topic.setDescription(topicBindingModel.getDescription());
        topic.setTitle(topicBindingModel.getTitle());
        topic.setForum(forum);

        this.topicRepository.saveAndFlush(topic);

        return "redirect:/topic/" + topic.getId();
    }

    @GetMapping("/topic/delete/{id}")
    @PreAuthorize("isAuthenticated()")
    public String delete(Model model, @PathVariable Integer id){
        if(!this.topicRepository.exists(id)){
            return "redirect:/";
        }

        Topic topic = this.topicRepository.findOne(id);

        if(!isUserAuthorOrAdmin(topic)){
            return "redirect:/topic/" + id;
        }

        model.addAttribute("topic", topic);

        return "topic/delete";
    }

    @PostMapping("/topic/delete/{id}")
    @PreAuthorize("isAuthenticated()")
    public String deleteProcess(@PathVariable Integer id, RedirectAttributes redirectAttributes){
        if (!this.topicRepository.exists(id)) {
            return "redirect:/";
        }

        Topic topic = this.topicRepository.findOne(id);

        // delete topic replies:
        List<Reply> replyList = this.replyRepository.findByTopic(topic);
        for (Reply reply: replyList) {
            this.replyRepository.delete(reply);
        }

        if(!isUserAuthorOrAdmin(topic)){
            return "redirect:/topic/" + id;
        }
        Integer idForum = topic.getForum().getId();
        this.topicRepository.delete(topic);
        redirectAttributes.addFlashAttribute("deleteTopic", "Topic was successfully deleted !");
        return "redirect:/forum/" + idForum;
    }


    private boolean isUserAuthorOrAdmin(Topic topic){
        UserDetails user = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        User userEntity = this.userRepository.findByEmail(user.getUsername());

        return userEntity.isAdmin() || userEntity.isAuthor(topic);
    }
}
