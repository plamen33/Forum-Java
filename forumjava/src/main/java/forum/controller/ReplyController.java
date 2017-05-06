package forum.controller;

import forum.bindingModel.ReplyBindingModel;
import forum.entity.Reply;
import forum.entity.Topic;
import forum.entity.User;
import forum.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Transient;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

@Controller
public class ReplyController {

    @Autowired
    private TopicRepository topicRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ReplyRepository replyRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ForumRepository forumRepository;

    @GetMapping("/reply/create/{id}")
    @PreAuthorize("isAuthenticated()")
    public String create(Model model, @PathVariable Integer id){    // public String create(@PathVariable Integer id, Model model){
        Topic topic = this.topicRepository.findOne(id);

        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = this.userRepository.findByEmail(principal.getUsername());

        model.addAttribute("topic", topic);
        model.addAttribute("user", user);
        return "reply/create";
    }
    @PostMapping("/reply/create/{id}")
    public String createProcess(ReplyBindingModel replyBindingModel, @PathVariable Integer id){
        Topic topic = this.topicRepository.findOne(id);
        UserDetails principal = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        User user = this.userRepository.findByEmail(principal.getUsername());
        LocalDateTime ldt = LocalDateTime.now();
        Timestamp datePosted = Timestamp.valueOf(ldt);
        Timestamp dateUpdated = datePosted;
        Reply reply = new Reply(
                replyBindingModel.getMessage(),
                datePosted,
                dateUpdated,
                topic,
                user
        );

        this.replyRepository.saveAndFlush(reply);
        return "redirect:/topic/"+id;
    }

    @GetMapping("/reply/edit/{id}")
    @PreAuthorize("isAuthenticated()")
    public String edit(@PathVariable Integer id, Model model){

        if(!this.replyRepository.exists(id)){
            return "redirect:/";
        }
        System.out.println(id);
        Reply reply = this.replyRepository.findOne(id);
        Topic topic = this.topicRepository.findOne(reply.getTopic().getId());


        if(!this.isAuthorOrAdmin(reply)){  // only admin or author can edit certain replies
            return "redirect:/topic/"+id;
        }
        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = this.userRepository.findByEmail(principal.getUsername());

        model.addAttribute("user", user);
        model.addAttribute("reply", reply);
        model.addAttribute("topic", topic);

        return "reply/edit";
    }
    @PostMapping("/reply/edit/{id}")
    @PreAuthorize("isAuthenticated()")
    public String editProcess(@PathVariable Integer id, ReplyBindingModel replyBindingModel){


        if(!this.replyRepository.exists(id)){
            return "redirect:/";
        }

        Reply reply = this.replyRepository.findOne(id);
        Integer topicId = reply.getTopic().getId();

        if(!this.isAuthorOrAdmin(reply)){  // only admin or author can edit certain replies
            return "redirect:/topic/"+id;
        }
        LocalDateTime ldt = LocalDateTime.now();
        Timestamp dateUpdated = Timestamp.valueOf(ldt);

        reply.setMessage(replyBindingModel.getMessage());
        reply.setDateUpdated(dateUpdated);


        this.replyRepository.saveAndFlush(reply);
        return "redirect:/topic/"+topicId;
    }

    @GetMapping("/reply/delete/{id}")
    @PreAuthorize("isAuthenticated()")
    public String delete(@PathVariable Integer id, Model model){

        if(!this.replyRepository.exists(id)){
            return "redirect:/";
        }
        Reply reply = this.replyRepository.findOne(id);
        Topic topic = this.topicRepository.findOne(reply.getTopic().getId());


        if(!this.isAuthorOrAdmin(reply)){  // only admin or author can edit certain replies
            return "redirect:/topic/"+id;
        }
        UserDetails principal = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = this.userRepository.findByEmail(principal.getUsername());

        model.addAttribute("user", user);
        model.addAttribute("reply", reply);
        model.addAttribute("topic", topic);

        return "reply/delete";
    }
    @PostMapping("/reply/delete/{id}")
    @PreAuthorize("isAuthenticated()")
    public String deleteProcess(@PathVariable Integer id, ReplyBindingModel replyBindingModel){


        if(!this.replyRepository.exists(id)){
            return "redirect:/";
        }

        Reply reply = this.replyRepository.findOne(id);
        Integer topicId = reply.getTopic().getId();

        if(!this.isAuthorOrAdmin(reply)){  // only admin or author can edit certain replies
            return "redirect:/topic/"+id;
        }
        this.replyRepository.delete(reply);
        return "redirect:/topic/"+topicId;
    }


    @Transient
    public boolean isAuthorOrAdmin(Reply reply) {
        UserDetails user = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User userEntity = this.userRepository.findByEmail(user.getUsername());
        return userEntity.isAdmin() || userEntity.isReplyAuthor(reply);
    }
}
